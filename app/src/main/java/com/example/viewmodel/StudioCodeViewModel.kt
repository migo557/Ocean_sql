package com.example.viewmodel

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.extensions.ExtensionAppImporter
import com.example.extensions.ExtensionHost
import com.example.extensions.ExtensionHostListener
import com.example.models.CodeFile
import com.example.models.ContributedButton
import com.example.models.ContributedStatusBarItem
import com.example.models.ExtensionItem
import com.example.models.ProjectFolder
import com.example.models.RegisteredCommand
import com.example.storage.LocalStorageManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AutoSaveStatus(val label: String) {
  SAVED("Saved"),
  SAVING("Saving..."),
  UNSAVED("Unsaved")
}

enum class ActivePanel {
  EXPLORER,
  SEARCH,
  EXTENSIONS,
  RUNNER,
  TERMINAL,
  CONSOLE,
  SETTINGS
}

enum class StudioVersion(
  val versionLabel: String,
  val editionName: String,
  val themeStyle: String,
  val description: String,
  val accentColorHex: Long
) {
  SOPHISTICATED_DARK(
    versionLabel = "v1.94.2",
    editionName = "Sophisticated Dark Edition",
    themeStyle = "Sophisticated Dark",
    description = "Refined obsidian canvas, soft lavender and electric accents, tailored for modern web and mobile engineering.",
    accentColorHex = 0xFFD0BCFF
  ),
  INSIDERS_EDITION(
    versionLabel = "v1.95.0-insiders",
    editionName = "Studio Code Insiders",
    themeStyle = "Cyberpunk Emerald",
    description = "Bleeding edge release with experimental web tools, fast HTML live preview, and AI developer tools.",
    accentColorHex = 0xFF34D399
  ),
  MONOKAI_PRO(
    versionLabel = "v1.92.1",
    editionName = "Monokai Pro Edition",
    themeStyle = "Monokai Pro",
    description = "Classic programmer palette with high-contrast amber, pink, and yellow syntax highlights.",
    accentColorHex = 0xFFFBBF24
  ),
  ONE_DARK(
    versionLabel = "v1.91.4",
    editionName = "One Dark Pro Edition",
    themeStyle = "Atom One Dark",
    description = "Balanced deep indigo tones with soft pastel syntax accents.",
    accentColorHex = 0xFF818CF8
  )
}

enum class PreviewDevice(val label: String, val widthDp: Int?) {
  MOBILE("Mobile 375px", 375),
  TABLET("Tablet 640px", 640),
  RESPONSIVE("Full Responsive", null)
}

enum class PreviewSplitOrientation(val label: String) {
  HORIZONTAL("Side by Side"),
  VERTICAL("Top & Bottom")
}

data class ConsoleMessage(
  val id: String,
  val level: String, // "LOG", "WARN", "ERROR", "INFO"
  val text: String,
  val timestamp: String
)

data class StudioCodeUiState(
  val files: List<CodeFile> = emptyList(),
  val folders: List<ProjectFolder> = emptyList(),
  val activeFileId: String = "index.html",
  val openTabIds: List<String> = listOf("index.html", "style.css", "script.js"),
  val activePanel: ActivePanel = ActivePanel.EXPLORER,
  val currentVersion: StudioVersion = StudioVersion.SOPHISTICATED_DARK,
  val showVersionPicker: Boolean = false,
  val searchQuery: String = "",
  val searchResultsCount: Int = 0,
  val installedExtensions: List<ExtensionItem> = emptyList(),
  val marketplaceExtensions: List<ExtensionItem> = emptyList(),
  val registeredExtensionCommands: List<RegisteredCommand> = emptyList(),
  val contributedUiButtons: List<ContributedButton> = emptyList(),
  val contributedStatusBarItems: List<ContributedStatusBarItem> = emptyList(),
  val showCommandPalette: Boolean = false,
  val showGlobalFileSearch: Boolean = false,
  val recentFileIds: List<String> = listOf("index.html", "style.css", "script.js"),
  val showAddExtensionDialog: Boolean = false,
  val showImportExtensionDialog: Boolean = false,
  val showNewFileDialog: Boolean = false,
  val showNewFolderDialog: Boolean = false,
  val targetParentFolderId: String? = null,
  val isFindReplaceOpen: Boolean = false,
  val findQuery: String = "",
  val replaceQuery: String = "",
  val matchCase: Boolean = false,
  val matchWholeWord: Boolean = false,
  val useRegex: Boolean = false,
  val currentMatchIndex: Int = 0,
  val totalMatches: Int = 0,
  val consoleLogs: List<ConsoleMessage> = emptyList(),
  val previewDevice: PreviewDevice = PreviewDevice.RESPONSIVE,
  val runnerReloadTrigger: Int = 0,
  val isLivePreviewOpen: Boolean = true,
  val isLivePreviewUpdating: Boolean = false,
  val livePreviewHtml: String = "",
  val previewSplitOrientation: PreviewSplitOrientation = PreviewSplitOrientation.HORIZONTAL,
  val snackbarMessage: String? = null,
  val terminalLogs: List<String> = emptyList(),
  val autoSaveStatus: AutoSaveStatus = AutoSaveStatus.SAVED,
  val isAutoSaveEnabled: Boolean = true,
  val lastSavedTime: String = "Just now"
)

class StudioCodeViewModel : ViewModel() {

  private val _uiState = MutableStateFlow(StudioCodeUiState())
  val uiState: StateFlow<StudioCodeUiState> = _uiState.asStateFlow()

  // History for undo/redo
  private val undoMap = mutableMapOf<String, ArrayDeque<String>>()
  private val redoMap = mutableMapOf<String, ArrayDeque<String>>()

  // Local storage & Auto-save
  private var localStorageManager: LocalStorageManager? = null
  private var autoSaveJob: Job? = null
  private var periodicSaveJob: Job? = null

  init {
    loadInitialProject()
    loadExtensions()
    initTerminal()
  }

  private fun loadInitialProject() {
    val initialFiles = listOf(
      CodeFile(
        id = "index.html",
        name = "index.html",
        extension = "html",
        content = """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Studio Code Web App</title>
  <link rel="stylesheet" href="style.css">
</head>
<body>
  <div class="app-card">
    <div class="badge">Studio Code v1.94</div>
    <h1>Sophisticated Dark</h1>
    <p class="subtitle">Next-generation Web & Android Code Studio</p>

    <div class="counter-box">
      <button id="decrementBtn" class="btn secondary">-</button>
      <span id="counterValue">0</span>
      <button id="incrementBtn" class="btn primary">+</button>
    </div>

    <div class="actions">
      <button id="magicBtn" class="btn action-btn">✨ Generate Particle Glow</button>
      <button id="resetBtn" class="btn ghost">Reset</button>
    </div>

    <div id="outputLog" class="log-display">Ready for live execution...</div>
  </div>

  <script src="script.js"></script>
</body>
</html>"""
      ),
      CodeFile(
        id = "style.css",
        name = "style.css",
        extension = "css",
        content = """* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  background: radial-gradient(circle at top, #1e2235, #0d0f17);
  color: #f1f5f9;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.app-card {
  background: rgba(26, 29, 45, 0.85);
  backdrop-filter: blur(16px);
  border: 1px solid rgba(56, 189, 248, 0.25);
  border-radius: 20px;
  padding: 32px 28px;
  width: 100%;
  max-width: 420px;
  text-align: center;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.6), 0 0 30px rgba(56, 189, 248, 0.1);
}

.badge {
  display: inline-block;
  background: rgba(56, 189, 248, 0.15);
  color: #38bdf8;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 1px;
  text-transform: uppercase;
  padding: 4px 12px;
  border-radius: 20px;
  border: 1px solid rgba(56, 189, 248, 0.3);
  margin-bottom: 12px;
}

h1 {
  font-size: 26px;
  font-weight: 800;
  background: linear-gradient(135deg, #ffffff, #94a3b8);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 6px;
}

.subtitle {
  color: #94a3b8;
  font-size: 13px;
  margin-bottom: 24px;
}

.counter-box {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin: 20px 0;
  background: rgba(15, 17, 26, 0.6);
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #282c3f;
}

#counterValue {
  font-size: 36px;
  font-weight: 800;
  color: #38bdf8;
  min-width: 60px;
  text-align: center;
}

.btn {
  cursor: pointer;
  font-weight: 600;
  border: none;
  outline: none;
  transition: all 0.2s ease;
  font-size: 15px;
}

.btn.primary, .btn.secondary {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.btn.primary {
  background: #38bdf8;
  color: #0f172a;
}

.btn.primary:active {
  transform: scale(0.92);
  background: #0ea5e9;
}

.btn.secondary {
  background: #272a3d;
  color: #e2e8f0;
}

.btn.secondary:active {
  transform: scale(0.92);
  background: #333852;
}

.actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 16px;
}

.action-btn {
  background: linear-gradient(135deg, #0284c7, #6366f1);
  color: white;
  padding: 12px 18px;
  border-radius: 12px;
  box-shadow: 0 4px 15px rgba(99, 102, 241, 0.3);
}

.action-btn:active {
  transform: scale(0.97);
}

.ghost {
  background: transparent;
  color: #64748b;
  font-size: 13px;
  padding: 8px;
}

.ghost:hover {
  color: #94a3b8;
}

.log-display {
  margin-top: 20px;
  font-family: monospace;
  font-size: 11px;
  color: #34d399;
  background: #0d0e15;
  padding: 10px;
  border-radius: 8px;
  border: 1px solid #1f2333;
}"""
      ),
      CodeFile(
        id = "script.js",
        name = "script.js",
        extension = "js",
        content = """// Studio Code interactive runtime script
let count = 0;
const counterEl = document.getElementById('counterValue');
const logEl = document.getElementById('outputLog');

function updateLog(message) {
  console.log('[StudioCode] ' + message);
  if (logEl) {
    logEl.innerText = message;
  }
}

document.getElementById('incrementBtn')?.addEventListener('click', () => {
  count++;
  counterEl.innerText = count;
  updateLog(`Incremented to ${'$'}{count}`);
});

document.getElementById('decrementBtn')?.addEventListener('click', () => {
  count--;
  counterEl.innerText = count;
  updateLog(`Decremented to ${'$'}{count}`);
});

document.getElementById('resetBtn')?.addEventListener('click', () => {
  count = 0;
  counterEl.innerText = count;
  updateLog('Counter reset to 0');
});

document.getElementById('magicBtn')?.addEventListener('click', () => {
  const card = document.querySelector('.app-card');
  if (card) {
    const randomHue = Math.floor(Math.random() * 360);
    card.style.boxShadow = `0 20px 40px rgba(0,0,0,0.7), 0 0 45px hsl(${'$'}{randomHue}, 90%, 60%)`;
    card.style.borderColor = `hsl(${'$'}{randomHue}, 90%, 65%)`;
    updateLog(`Glow transformed to hue ${'$'}{randomHue}°!`);
  }
});

console.log("Studio Code Web App initialized successfully!");
"""
      ),
      CodeFile(
        id = "README.md",
        name = "README.md",
        extension = "md",
        content = """# Easy Code

A high-performance Visual Studio Code clone built for Android and Web.

## Features
- **Sophisticated Dark Theme**: Elegant deep charcoal & neon blue IDE aesthetic.
- **Extension App Import**: Import VS Code packages, manifests, or JS extension apps (.json, .js, .vsix).
- **Live Web Runner**: Full interactive WebView engine to run HTML/CSS/JS in real time.
- **Instant HTML Copy**: Bundles standalone HTML with inlined CSS & JS ready to export or paste anywhere.
- **Extensions Store**: Install, manage, build, and import your own custom extensions.
- **Integrated Terminal**: Execute development commands and inspect logs.
- **Code Accessory Toolbar**: Fast symbol input tailored for mobile & web.
"""
      ),
      CodeFile(
        id = "package.json",
        name = "package.json",
        extension = "json",
        content = """{
  "name": "easy-code-web-app",
  "version": "1.94.2",
  "private": true,
  "description": "Easy Code Android & Web Project",
  "scripts": {
    "dev": "easy-code live-server",
    "build": "easy-code bundle-html"
  },
  "dependencies": {}
}"""
      ),
      CodeFile(
        id = "sample-extension.json",
        name = "sample-extension.json",
        extension = "json",
        content = """{
  "name": "clock-status-app",
  "displayName": "Live Status Clock App",
  "version": "1.0.0",
  "publisher": "easycode.dev",
  "description": "Live time status bar widget for Easy Code with one-click inspection",
  "category": "Tools",
  "icon": "⏰",
  "contributes": {
    "commands": [
      { "command": "extension.showTime", "title": "Easy Code: Show Current Time" }
    ],
    "buttons": [
      { "id": "btn_show_time", "label": "⏰ Time", "command": "extension.showTime" }
    ]
  },
  "script": "vscode.commands.registerCommand('extension.showTime', function() { vscode.window.showInformationMessage('Current time: ' + new Date().toLocaleTimeString()); }); vscode.ui.addButton({ id: 'btn_show_time', label: '⏰ Time', command: 'extension.showTime' });"
}"""
      )
    )

    val initialFolders = listOf(
      ProjectFolder(id = "folder_src", name = "src", parentFolderId = null, isExpanded = true),
      ProjectFolder(id = "folder_styles", name = "styles", parentFolderId = "folder_src", isExpanded = true),
      ProjectFolder(id = "folder_scripts", name = "scripts", parentFolderId = "folder_src", isExpanded = true)
    )

    // Map files into folders
    val filesWithFolders = initialFiles.map { file ->
      when (file.id) {
        "index.html" -> file.copy(parentFolderId = "folder_src")
        "style.css" -> file.copy(parentFolderId = "folder_styles")
        "script.js" -> file.copy(parentFolderId = "folder_scripts")
        else -> file.copy(parentFolderId = null)
      }
    }

    _uiState.value = _uiState.value.copy(
      files = filesWithFolders,
      folders = initialFolders,
      activeFileId = "index.html",
      openTabIds = listOf("index.html", "style.css", "script.js")
    )
  }

  private fun loadExtensions() {
    val initialInstalled = listOf(
      ExtensionItem(
        id = "prettier.formatter",
        name = "Prettier - Code Formatter",
        author = "Prettier",
        version = "3.2.0",
        description = "Opinionated code formatter for HTML, CSS, JavaScript, and JSON with standard 2-space indentation.",
        iconEmoji = "🎨",
        category = "Formatters",
        isInstalled = true,
        isEnabled = true,
        downloads = "38.5M",
        rating = 4.8f,
        codeSnippet = "<!-- Formatted with Prettier -->"
      ),
      ExtensionItem(
        id = "live.server",
        name = "Live Server HTML Runner",
        author = "Ritwick Dey",
        version = "5.7.9",
        description = "Launch a local development live server with live reload feature for static & dynamic HTML web pages.",
        iconEmoji = "⚡",
        category = "Web Servers",
        isInstalled = true,
        isEnabled = true,
        downloads = "44.1M",
        rating = 4.9f
      ),
      ExtensionItem(
        id = "vscode.power_tools",
        name = "Studio Code Power Tools",
        author = "VS Code Community",
        version = "1.0.0",
        description = "Provides Uppercase, Timestamp, and Div Wrapper commands, plus toolbar action buttons.",
        iconEmoji = "🛠️",
        category = "Tools",
        isInstalled = true,
        isEnabled = true,
        downloads = "120K",
        rating = 4.9f,
        jsCode = """
vscode.commands.registerCommand('extension.toUpperCase', function() {
  var content = vscode.editor.getContent();
  vscode.editor.setContent(content.toUpperCase());
  vscode.window.showInformationMessage('Converted code to UPPERCASE');
});

vscode.commands.registerCommand('extension.addHeaderTimestamp', function() {
  var content = vscode.editor.getContent();
  var ts = '/* Last updated: ' + new Date().toLocaleTimeString() + ' */\n';
  vscode.editor.setContent(ts + content);
  vscode.window.showInformationMessage('Timestamp added to header');
});

vscode.commands.registerCommand('extension.wrapInDiv', function() {
  var content = vscode.editor.getContent();
  vscode.editor.setContent('<div class="studio-wrapper">\n' + content + '\n</div>');
  vscode.window.showInformationMessage('Wrapped code in <div class="studio-wrapper">');
});

vscode.ui.addButton('btn_upper', 'UPPERCASE', 'extension.toUpperCase');
vscode.ui.addButton('btn_time', 'ADD TIMESTAMP', 'extension.addHeaderTimestamp');
vscode.ui.addStatusBarItem('status_tools', '🛠️ PowerTools: Ready', 'extension.toUpperCase');
""".trimIndent()
      ),
      ExtensionItem(
        id = "emmet.snippets",
        name = "Emmet Toolkit & Snippets",
        author = "Studio Code Team",
        version = "2.1.0",
        description = "Essential toolkit for web developers to expand abbreviations into structured HTML & CSS tags.",
        iconEmoji = "🚀",
        category = "Snippets",
        isInstalled = true,
        isEnabled = true,
        downloads = "22.3M",
        rating = 4.9f,
        codeSnippet = "<div class=\"container\">\n  <h1>Title</h1>\n  <p>Content</p>\n</div>"
      )
    )

    val initialMarketplace = listOf(
      ExtensionItem(
        id = "tailwind.cdn",
        name = "Tailwind CSS CDN Injector",
        author = "Tailwind Labs",
        version = "3.4.1",
        description = "Injects modern utility-first CSS framework into HTML documents with a single click.",
        iconEmoji = "🌊",
        category = "CSS Frameworks",
        isInstalled = false,
        downloads = "12.4M",
        rating = 4.9f,
        codeSnippet = "<script src=\"https://cdn.tailwindcss.com\"></script>"
      ),
      ExtensionItem(
        id = "fontawesome.icons",
        name = "Font Awesome Icons",
        author = "Fonticons",
        version = "6.5.1",
        description = "Instant vector icons and social logos CDN injection for your HTML layouts.",
        iconEmoji = "💎",
        category = "Icons",
        isInstalled = false,
        downloads = "8.7M",
        rating = 4.8f,
        codeSnippet = "<link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\">"
      ),
      ExtensionItem(
        id = "bracket.colorizer",
        name = "Bracket Pair Colorizer 2",
        author = "CoenraadS",
        version = "2.0.2",
        description = "Colorizes matching brackets with vivid hues to easily identify nesting depth.",
        iconEmoji = "🌈",
        category = "UI Customization",
        isInstalled = false,
        downloads = "15.9M",
        rating = 4.7f
      ),
      ExtensionItem(
        id = "dracula.theme",
        name = "Dracula Official Theme",
        author = "Dracula Theme",
        version = "2.24.2",
        description = "Famous gothic dark theme for Studio Code with purple and pink accents.",
        iconEmoji = "🧛",
        category = "Themes",
        isInstalled = false,
        downloads = "7.2M",
        rating = 4.9f
      )
    )

    val initialCommands = listOf(
      RegisteredCommand(id = "workbench.action.quickOpen", title = "Go to File... (Quick Open)", extensionId = "builtin", description = "File: Quick Open / Search Files by Name"),
      RegisteredCommand(id = "workbench.action.files.save", title = "Save", extensionId = "builtin", description = "File: Save to Local Storage"),
      RegisteredCommand(id = "workbench.action.toggleAutoSave", title = "Toggle Auto Save", extensionId = "builtin", description = "File: Toggle Debounced Auto-Save"),
      RegisteredCommand(id = "workbench.action.formatDocument", title = "Format Document", extensionId = "builtin", description = "Developer: Format Document"),
      RegisteredCommand(id = "editor.action.find", title = "Find", extensionId = "builtin", description = "Editor: Find in File"),
      RegisteredCommand(id = "editor.action.replace", title = "Replace", extensionId = "builtin", description = "Editor: Find and Replace"),
      RegisteredCommand(id = "workbench.action.findInFiles", title = "Find in Files", extensionId = "builtin", description = "Search across project files"),
      RegisteredCommand(id = "workbench.action.toggleSidebar", title = "Toggle Primary Side Bar", extensionId = "builtin", description = "View: Toggle Sidebar"),
      RegisteredCommand(id = "workbench.action.toggleLivePreview", title = "Toggle Live HTML Preview", extensionId = "builtin", description = "View: Toggle Live HTML Split Preview"),
      RegisteredCommand(id = "workbench.action.togglePreviewOrientation", title = "Toggle Preview Layout", extensionId = "builtin", description = "View: Toggle Side-by-Side / Top-Bottom Split"),
      RegisteredCommand(id = "workbench.action.reloadPreview", title = "Reload Preview", extensionId = "builtin", description = "View: Reload Live HTML Preview"),
      RegisteredCommand(id = "workbench.action.openExtensions", title = "Show Installed Extensions", extensionId = "builtin", description = "View: Show Extensions"),
      RegisteredCommand(id = "workbench.action.showConsole", title = "Show Debug Console", extensionId = "builtin", description = "View: Show Debug Console Panel")
    )

    _uiState.value = _uiState.value.copy(
      installedExtensions = initialInstalled,
      marketplaceExtensions = initialMarketplace,
      registeredExtensionCommands = initialCommands
    )
    _uiState.value = _uiState.value.copy(
      livePreviewHtml = generateStandaloneHtml()
    )
  }

  fun initTerminal() {
    _uiState.value = _uiState.value.copy(
      terminalLogs = listOf(
        "Studio Code [Version 1.94.2-sophisticated-dark]",
        "Host: Android / Web Hybrid Engine",
        "Workspace: /storage/emulated/0/StudioCodeProjects/web-app",
        "Type 'help' for available commands or 'bundle' to build HTML.",
        "$ ready."
      )
    )
  }

  // --- Version & Edition Choice ---
  fun setVersionChoice(version: StudioVersion) {
    _uiState.value = _uiState.value.copy(
      currentVersion = version,
      showVersionPicker = false,
      snackbarMessage = "Switched to ${version.editionName}"
    )
  }

  fun toggleVersionPicker(show: Boolean) {
    _uiState.value = _uiState.value.copy(showVersionPicker = show)
  }

  // --- File and Tab Actions ---
  fun selectFile(fileId: String) {
    val currentOpen = _uiState.value.openTabIds.toMutableList()
    if (!currentOpen.contains(fileId)) {
      currentOpen.add(fileId)
    }
    val currentRecent = _uiState.value.recentFileIds.toMutableList()
    currentRecent.remove(fileId)
    currentRecent.add(0, fileId)

    _uiState.value = _uiState.value.copy(
      activeFileId = fileId,
      openTabIds = currentOpen,
      recentFileIds = currentRecent
    )
    triggerDebouncedLivePreview(0L)
  }

  fun closeTab(fileId: String) {
    val currentOpen = _uiState.value.openTabIds.toMutableList()
    currentOpen.remove(fileId)
    val nextActive = if (fileId == _uiState.value.activeFileId) {
      currentOpen.lastOrNull() ?: _uiState.value.files.firstOrNull()?.id ?: ""
    } else {
      _uiState.value.activeFileId
    }
    _uiState.value = _uiState.value.copy(
      openTabIds = currentOpen,
      activeFileId = nextActive
    )
    triggerDebouncedLivePreview(0L)
  }

  fun updateActiveFileContent(newContent: String) {
    val activeId = _uiState.value.activeFileId
    val currentFiles = _uiState.value.files.map { file ->
      if (file.id == activeId) {
        // Save undo step
        val stack = undoMap.getOrPut(activeId) { ArrayDeque() }
        if (stack.size > 20) stack.removeFirst()
        stack.addLast(file.content)
        file.copy(content = newContent, isModified = true)
      } else file
    }
    _uiState.value = _uiState.value.copy(
      files = currentFiles,
      isLivePreviewUpdating = true
    )
    if (_uiState.value.isFindReplaceOpen && _uiState.value.findQuery.isNotEmpty()) {
      recalculateMatches()
    }
    triggerDebouncedAutoSave(800L)
    triggerDebouncedLivePreview(150L)
  }

  fun undo() {
    val activeId = _uiState.value.activeFileId
    val stack = undoMap[activeId] ?: return
    if (stack.isNotEmpty()) {
      val prev = stack.removeLast()
      val currentContent = _uiState.value.files.find { it.id == activeId }?.content ?: ""
      val redoStack = redoMap.getOrPut(activeId) { ArrayDeque() }
      redoStack.addLast(currentContent)

      val currentFiles = _uiState.value.files.map { file ->
        if (file.id == activeId) file.copy(content = prev, isModified = true) else file
      }
      _uiState.value = _uiState.value.copy(files = currentFiles)
      if (_uiState.value.isFindReplaceOpen && _uiState.value.findQuery.isNotEmpty()) {
        recalculateMatches()
      }
      triggerDebouncedAutoSave(800L)
      triggerDebouncedLivePreview(0L)
    }
  }

  fun redo() {
    val activeId = _uiState.value.activeFileId
    val redoStack = redoMap[activeId] ?: return
    if (redoStack.isNotEmpty()) {
      val next = redoStack.removeLast()
      val currentContent = _uiState.value.files.find { it.id == activeId }?.content ?: ""
      val undoStack = undoMap.getOrPut(activeId) { ArrayDeque() }
      undoStack.addLast(currentContent)

      val currentFiles = _uiState.value.files.map { file ->
        if (file.id == activeId) file.copy(content = next, isModified = true) else file
      }
      _uiState.value = _uiState.value.copy(files = currentFiles)
      if (_uiState.value.isFindReplaceOpen && _uiState.value.findQuery.isNotEmpty()) {
        recalculateMatches()
      }
      triggerDebouncedAutoSave(800L)
      triggerDebouncedLivePreview(0L)
    }
  }

  fun createNewFile(fileName: String, parentFolderId: String? = null) {
    val trimmed = fileName.trim()
    if (trimmed.isEmpty()) return
    val ext = if (trimmed.contains(".")) trimmed.substringAfterLast(".").lowercase() else "txt"

    // Prevent duplicate file names in the same folder
    if (_uiState.value.files.any { it.name.equals(trimmed, ignoreCase = true) && it.parentFolderId == parentFolderId }) {
      _uiState.value = _uiState.value.copy(snackbarMessage = "A file named '$trimmed' already exists")
      return
    }

    val defaultContent = when (ext) {
      "html", "htm" -> "<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n  <meta charset=\"UTF-8\">\n  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n  <title>$trimmed</title>\n</head>\n<body>\n  <h1>Hello from $trimmed</h1>\n</body>\n</html>"
      "css" -> "/* Styles for $trimmed */\nbody {\n  margin: 0;\n  padding: 16px;\n  font-family: sans-serif;\n}"
      "js", "javascript" -> "// JavaScript logic\nconsole.log(\"$trimmed loaded\");"
      "json" -> "{\n  \"name\": \"${if (trimmed.contains(".")) trimmed.substringBeforeLast(".") else trimmed}\",\n  \"version\": \"1.0.0\"\n}"
      "md", "markdown" -> "# ${if (trimmed.contains(".")) trimmed.substringBeforeLast(".") else trimmed}\n\nDocumentation."
      else -> ""
    }

    val newFile = CodeFile(
      id = trimmed,
      name = trimmed,
      extension = ext,
      parentFolderId = parentFolderId,
      content = defaultContent
    )
    val updatedFiles = _uiState.value.files + newFile
    val updatedTabs = if (newFile.id !in _uiState.value.openTabIds) _uiState.value.openTabIds + newFile.id else _uiState.value.openTabIds
    _uiState.value = _uiState.value.copy(
      files = updatedFiles,
      openTabIds = updatedTabs,
      activeFileId = newFile.id,
      showNewFileDialog = false,
      targetParentFolderId = null,
      snackbarMessage = "Created $trimmed"
    )
    triggerDebouncedAutoSave(200L)
  }

  fun createNewFolder(folderName: String, parentFolderId: String? = null) {
    val trimmed = folderName.trim()
    if (trimmed.isEmpty()) return

    if (_uiState.value.folders.any { it.name.equals(trimmed, ignoreCase = true) && it.parentFolderId == parentFolderId }) {
      _uiState.value = _uiState.value.copy(snackbarMessage = "A folder named '$trimmed' already exists")
      return
    }

    val newFolder = ProjectFolder(
      id = "folder_${System.currentTimeMillis()}",
      name = trimmed,
      parentFolderId = parentFolderId,
      isExpanded = true
    )
    _uiState.value = _uiState.value.copy(
      folders = _uiState.value.folders + newFolder,
      showNewFolderDialog = false,
      targetParentFolderId = null,
      snackbarMessage = "Created folder $trimmed"
    )
    triggerDebouncedAutoSave(200L)
  }

  fun renameFile(fileId: String, newName: String) {
    val trimmed = newName.trim()
    if (trimmed.isEmpty()) return
    val ext = if (trimmed.contains(".")) trimmed.substringAfterLast(".").lowercase() else "txt"

    val currentFile = _uiState.value.files.find { it.id == fileId } ?: return
    if (_uiState.value.files.any { it.id != fileId && it.parentFolderId == currentFile.parentFolderId && it.name.equals(trimmed, ignoreCase = true) }) {
      _uiState.value = _uiState.value.copy(snackbarMessage = "A file named '$trimmed' already exists")
      return
    }

    val newId = trimmed
    val updatedFiles = _uiState.value.files.map {
      if (it.id == fileId) it.copy(id = newId, name = trimmed, extension = ext) else it
    }
    val updatedTabs = _uiState.value.openTabIds.map { if (it == fileId) newId else it }
    val newActiveId = if (_uiState.value.activeFileId == fileId) newId else _uiState.value.activeFileId

    _uiState.value = _uiState.value.copy(
      files = updatedFiles,
      openTabIds = updatedTabs,
      activeFileId = newActiveId,
      snackbarMessage = "Renamed to $trimmed"
    )
    triggerDebouncedAutoSave(200L)
  }

  fun renameFolder(folderId: String, newName: String) {
    val trimmed = newName.trim()
    if (trimmed.isEmpty()) return
    val updated = _uiState.value.folders.map {
      if (it.id == folderId) it.copy(name = trimmed) else it
    }
    _uiState.value = _uiState.value.copy(folders = updated, snackbarMessage = "Renamed folder to $trimmed")
    triggerDebouncedAutoSave(200L)
  }

  fun deleteFolder(folderId: String) {
    val childFolderIds = mutableSetOf(folderId)
    var changed = true
    while (changed) {
      changed = false
      _uiState.value.folders.forEach { f ->
        if (f.parentFolderId in childFolderIds && f.id !in childFolderIds) {
          childFolderIds.add(f.id)
          changed = true
        }
      }
    }

    val updatedFolders = _uiState.value.folders.filterNot { it.id in childFolderIds }
    val updatedFiles = _uiState.value.files.filterNot { it.parentFolderId in childFolderIds }
    val updatedTabs = _uiState.value.openTabIds.filter { tabId -> updatedFiles.any { it.id == tabId } }
    val activeId = if (_uiState.value.activeFileId !in updatedTabs) updatedTabs.firstOrNull() ?: "" else _uiState.value.activeFileId

    _uiState.value = _uiState.value.copy(
      folders = updatedFolders,
      files = updatedFiles,
      openTabIds = updatedTabs,
      activeFileId = activeId,
      snackbarMessage = "Deleted folder and contents"
    )
    triggerDebouncedAutoSave(200L)
  }

  fun toggleFolder(folderId: String) {
    val updated = _uiState.value.folders.map {
      if (it.id == folderId) it.copy(isExpanded = !it.isExpanded) else it
    }
    _uiState.value = _uiState.value.copy(folders = updated)
  }

  fun expandAllFolders() {
    _uiState.value = _uiState.value.copy(folders = _uiState.value.folders.map { it.copy(isExpanded = true) })
  }

  fun collapseAllFolders() {
    _uiState.value = _uiState.value.copy(folders = _uiState.value.folders.map { it.copy(isExpanded = false) })
  }

  fun deleteFile(fileId: String) {
    if (_uiState.value.files.size <= 1) {
      _uiState.value = _uiState.value.copy(snackbarMessage = "Cannot delete the only file in workspace")
      return
    }
    val targetFile = _uiState.value.files.find { it.id == fileId }
    val displayName = targetFile?.name ?: fileId
    val updatedFiles = _uiState.value.files.filterNot { it.id == fileId }
    val updatedTabs = _uiState.value.openTabIds.filterNot { it == fileId }
    val nextActive = if (fileId == _uiState.value.activeFileId) {
      updatedTabs.lastOrNull() ?: updatedFiles.first().id
    } else {
      _uiState.value.activeFileId
    }
    val finalTabs = if (updatedTabs.isEmpty()) listOf(nextActive) else updatedTabs
    _uiState.value = _uiState.value.copy(
      files = updatedFiles,
      openTabIds = finalTabs,
      activeFileId = nextActive,
      snackbarMessage = "Deleted $displayName"
    )
    triggerDebouncedAutoSave(200L)
  }

  fun toggleNewFileDialog(show: Boolean, parentFolderId: String? = null) {
    _uiState.value = _uiState.value.copy(showNewFileDialog = show, targetParentFolderId = parentFolderId)
  }

  fun toggleNewFolderDialog(show: Boolean, parentFolderId: String? = null) {
    _uiState.value = _uiState.value.copy(showNewFolderDialog = show, targetParentFolderId = parentFolderId)
  }

  // --- Live HTML Preview & Standalone Bundle ---
  private var livePreviewJob: Job? = null

  fun triggerDebouncedLivePreview(delayMs: Long = 150L) {
    livePreviewJob?.cancel()
    livePreviewJob = viewModelScope.launch {
      if (delayMs > 0) {
        delay(delayMs)
      }
      val updatedHtml = generateStandaloneHtml()
      _uiState.value = _uiState.value.copy(
        livePreviewHtml = updatedHtml,
        isLivePreviewUpdating = false
      )
    }
  }

  fun toggleLivePreview(enable: Boolean? = null) {
    val next = enable ?: !_uiState.value.isLivePreviewOpen
    _uiState.value = _uiState.value.copy(
      isLivePreviewOpen = next,
      livePreviewHtml = if (next) generateStandaloneHtml() else _uiState.value.livePreviewHtml,
      snackbarMessage = if (next) "⚡ Live HTML Preview: Enabled" else "Live Preview closed"
    )
    if (next) {
      triggerDebouncedLivePreview(0L)
    }
  }

  fun togglePreviewOrientation() {
    val next = if (_uiState.value.previewSplitOrientation == PreviewSplitOrientation.HORIZONTAL) {
      PreviewSplitOrientation.VERTICAL
    } else {
      PreviewSplitOrientation.HORIZONTAL
    }
    _uiState.value = _uiState.value.copy(
      previewSplitOrientation = next,
      snackbarMessage = "Preview layout: ${next.label}"
    )
  }

  fun reloadLivePreview() {
    val updated = generateStandaloneHtml()
    _uiState.value = _uiState.value.copy(
      livePreviewHtml = updated,
      runnerReloadTrigger = _uiState.value.runnerReloadTrigger + 1,
      isLivePreviewUpdating = false,
      snackbarMessage = "🔄 Live preview refreshed"
    )
  }

  fun generateStandaloneHtml(targetFileId: String? = null): String {
    val activeId = targetFileId ?: _uiState.value.activeFileId
    val activeFile = _uiState.value.files.find { it.id == activeId }

    // Prioritize active file if it's an HTML file or contains HTML tags
    val htmlFile = if (activeFile != null && (activeFile.extension.lowercase() in listOf("html", "htm") || activeFile.content.contains("<html", ignoreCase = true) || activeFile.content.contains("<div", ignoreCase = true) || activeFile.content.contains("<p", ignoreCase = true) || activeFile.content.contains("<h1", ignoreCase = true))) {
      activeFile
    } else {
      _uiState.value.files.find { it.name.equals("index.html", ignoreCase = true) }
        ?: _uiState.value.files.find { it.extension.lowercase() in listOf("html", "htm") }
    } ?: return """
      <!DOCTYPE html>
      <html>
      <head>
        <meta charset='utf-8'>
        <style>
          body { font-family: -apple-system, sans-serif; background: #0f172a; color: #94a3b8; display: flex; align-items: center; justify-content: center; height: 100vh; margin: 0; }
        </style>
      </head>
      <body>
        <p>No HTML file found. Open or create an .html file to preview live.</p>
      </body>
      </html>
    """.trimIndent()

    val cssFile = _uiState.value.files.find { it.extension.lowercase() == "css" }
    val jsFile = _uiState.value.files.find { it.extension.lowercase() in listOf("js", "javascript") }

    var html = htmlFile.content

    // If blank, show an informative live preview placeholder
    if (html.isBlank()) {
      return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <style>
            body {
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
              background: #0d1117;
              color: #8b949e;
              display: flex;
              align-items: center;
              justify-content: center;
              height: 100vh;
              margin: 0;
              text-align: center;
            }
            .placeholder {
              border: 1px dashed #30363d;
              padding: 24px;
              border-radius: 12px;
              max-width: 320px;
            }
            h4 { color: #58a6ff; margin: 0 0 8px 0; }
            p { font-size: 13px; line-height: 1.5; }
            code { background: #161b22; padding: 2px 6px; border-radius: 4px; color: #79c0ff; }
          </style>
        </head>
        <body>
          <div class="placeholder">
            <h4>⚡ Live HTML Preview</h4>
            <p>Start typing HTML code in the editor (like <code>&lt;h1&gt;</code> or <code>&lt;button&gt;</code>). Rendered output will update automatically in real-time!</p>
          </div>
        </body>
        </html>
      """.trimIndent()
    }

    // Wrap partial HTML fragments that do not have <html> or <body>
    if (!html.contains("<html", ignoreCase = true) && !html.contains("<body", ignoreCase = true)) {
      html = """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <style>
            body {
              font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
              margin: 16px;
              color: #e6edf3;
              background-color: #0d1117;
            }
          </style>
        </head>
        <body>
          $html
        </body>
        </html>
      """.trimIndent()
    }

    // Inline CSS
    if (cssFile != null && cssFile.content.isNotBlank()) {
      if (html.contains("<link rel=\"stylesheet\" href=\"style.css\">")) {
        html = html.replace(
          "<link rel=\"stylesheet\" href=\"style.css\">",
          "<style>\n${cssFile.content}\n</style>"
        )
      } else if (html.contains("</head>")) {
        html = html.replace("</head>", "<style>\n${cssFile.content}\n</style>\n</head>")
      } else {
        html = "<style>\n${cssFile.content}\n</style>\n" + html
      }
    }

    // Inline JS
    if (jsFile != null && jsFile.content.isNotBlank()) {
      if (html.contains("<script src=\"script.js\"></script>")) {
        html = html.replace(
          "<script src=\"script.js\"></script>",
          "<script>\n${jsFile.content}\n</script>"
        )
      } else if (html.contains("</body>")) {
        html = html.replace("</body>", "<script>\n${jsFile.content}\n</script>\n</body>")
      } else {
        html = html + "\n<script>\n${jsFile.content}\n</script>"
      }
    }

    return html
  }

  fun copyHtmlToClipboard(context: Context, fullStandaloneBundle: Boolean = true) {
    val contentToCopy = if (fullStandaloneBundle) {
      generateStandaloneHtml()
    } else {
      _uiState.value.files.find { it.extension.lowercase() in listOf("html", "htm") }?.content
        ?: "<!-- No HTML file -->"
    }

    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Studio Code HTML", contentToCopy)
    clipboard.setPrimaryClip(clip)

    val label = if (fullStandaloneBundle) "Complete Standalone HTML" else "Raw HTML"
    _uiState.value = _uiState.value.copy(
      snackbarMessage = "📋 $label copied to clipboard!"
    )
  }

  fun copyCurrentActiveFile(context: Context) {
    val active = _uiState.value.files.find { it.id == _uiState.value.activeFileId } ?: return
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(active.name, active.content)
    clipboard.setPrimaryClip(clip)
    _uiState.value = _uiState.value.copy(
      snackbarMessage = "📋 ${active.name} copied to clipboard!"
    )
  }

  // --- Code Formatting & Snippet Insertion ---
  fun formatCurrentCode() {
    val activeId = _uiState.value.activeFileId
    val currentFile = _uiState.value.files.find { it.id == activeId } ?: return
    val formatted = formatText(currentFile.content, currentFile.extension)
    updateActiveFileContent(formatted)
    _uiState.value = _uiState.value.copy(snackbarMessage = "✨ Formatted with Prettier (2 spaces)")
  }

  private fun formatText(code: String, ext: String): String {
    val lines = code.split("\n")
    val result = StringBuilder()
    var indentLevel = 0

    for (line in lines) {
      val trimmed = line.trim()
      if (trimmed.isEmpty()) {
        result.append("\n")
        continue
      }

      // Check closing bracket decrease
      if (trimmed.startsWith("}") || trimmed.startsWith("</") || trimmed.startsWith("]") || trimmed.startsWith(");")) {
        indentLevel = maxOf(0, indentLevel - 1)
      }

      val indent = "  ".repeat(indentLevel)
      result.append(indent).append(trimmed).append("\n")

      // Check opening bracket increase
      if (trimmed.endsWith("{") || (trimmed.contains("<") && !trimmed.contains("</") && !trimmed.endsWith("/>") && !trimmed.startsWith("<!") && trimmed.endsWith(">")) || trimmed.endsWith("[")) {
        indentLevel++
      }
    }
    return result.toString().trimEnd()
  }

  fun insertSnippet(snippet: String) {
    val activeId = _uiState.value.activeFileId
    val active = _uiState.value.files.find { it.id == activeId } ?: return
    val newContent = active.content + "\n" + snippet
    updateActiveFileContent(newContent)
    _uiState.value = _uiState.value.copy(snackbarMessage = "Inserted snippet")
  }

  // --- Extensions Management ---
  fun installExtension(extensionId: String) {
    val ext = _uiState.value.marketplaceExtensions.find { it.id == extensionId } ?: return
    val updatedMarketplace = _uiState.value.marketplaceExtensions.filterNot { it.id == extensionId }
    val updatedInstalled = _uiState.value.installedExtensions + ext.copy(isInstalled = true, isEnabled = true)

    // If extension has code snippet, offer auto-injection
    _uiState.value = _uiState.value.copy(
      installedExtensions = updatedInstalled,
      marketplaceExtensions = updatedMarketplace,
      snackbarMessage = "Installed ${ext.name}"
    )

    // If it's a CDN injector (e.g. Tailwind or FontAwesome), automatically inject it into index.html
    ext.codeSnippet?.let { snippet ->
      if (ext.id == "tailwind.cdn" || ext.id == "fontawesome.icons") {
        injectSnippetIntoIndexHtml(snippet)
      }
    }
  }

  fun uninstallExtension(extensionId: String) {
    val ext = _uiState.value.installedExtensions.find { it.id == extensionId } ?: return
    val updatedInstalled = _uiState.value.installedExtensions.filterNot { it.id == extensionId }
    val updatedMarketplace = if (!ext.isCustom) {
      _uiState.value.marketplaceExtensions + ext.copy(isInstalled = false)
    } else {
      _uiState.value.marketplaceExtensions
    }
    _uiState.value = _uiState.value.copy(
      installedExtensions = updatedInstalled,
      marketplaceExtensions = updatedMarketplace,
      snackbarMessage = "Uninstalled ${ext.name}"
    )
  }

  fun toggleExtensionEnabled(extensionId: String) {
    val updatedInstalled = _uiState.value.installedExtensions.map {
      if (it.id == extensionId) it.copy(isEnabled = !it.isEnabled) else it
    }
    _uiState.value = _uiState.value.copy(installedExtensions = updatedInstalled)
  }

  fun addCustomExtension(
    name: String,
    author: String,
    description: String,
    category: String,
    snippet: String? = null,
    jsCode: String? = null
  ) {
    val trimmedName = name.trim()
    if (trimmedName.isEmpty()) return
    val customId = "custom." + trimmedName.lowercase().replace("\\s+".toRegex(), ".")
    val newExt = ExtensionItem(
      id = customId,
      name = trimmedName,
      author = if (author.isBlank()) "You" else author.trim(),
      version = "1.0.0",
      description = if (description.isBlank()) "Custom user-defined extension." else description.trim(),
      iconEmoji = "🧩",
      category = if (category.isBlank()) "Custom" else category.trim(),
      isInstalled = true,
      isEnabled = true,
      downloads = "1",
      rating = 5.0f,
      codeSnippet = snippet?.ifBlank { null },
      jsCode = jsCode?.ifBlank { null },
      isCustom = true
    )

    _uiState.value = _uiState.value.copy(
      installedExtensions = _uiState.value.installedExtensions + newExt,
      showAddExtensionDialog = false,
      snackbarMessage = "Added custom extension: ${newExt.name}"
    )

    if (!jsCode.isNullOrBlank()) {
      extensionHost?.loadExtension(customId, jsCode)
    }
  }

  fun toggleAddExtensionDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showAddExtensionDialog = show)
  }

  fun toggleImportExtensionDialog(show: Boolean) {
    _uiState.value = _uiState.value.copy(showImportExtensionDialog = show)
  }

  fun reloadExtension(extensionId: String) {
    val ext = _uiState.value.installedExtensions.find { it.id == extensionId }
    if (ext != null && !ext.jsCode.isNullOrBlank()) {
      extensionHost?.loadExtension(ext.id, ext.jsCode)
      _uiState.value = _uiState.value.copy(snackbarMessage = "Reloaded extension: ${ext.name}")
    }
  }

  private fun appendTerminalLog(log: String) {
    _uiState.value = _uiState.value.copy(
      terminalLogs = _uiState.value.terminalLogs + log
    )
  }

  fun importExtensionApp(rawContent: String, sourceFileName: String? = null): Boolean {
    return try {
      val item = ExtensionAppImporter.parseExtension(rawContent, sourceFileName)
      val updatedList = _uiState.value.installedExtensions.filterNot { it.id == item.id } + item
      _uiState.value = _uiState.value.copy(
        installedExtensions = updatedList,
        showImportExtensionDialog = false,
        snackbarMessage = "Imported extension app: ${item.name}"
      )

      appendTerminalLog("[EasyCode] Successfully imported extension app: ${item.name} (v${item.version})")
      if (item.contributedCommands.isNotEmpty()) {
        appendTerminalLog("[EasyCode] Registered commands: ${item.contributedCommands.joinToString(", ")}")
      }

      if (!item.jsCode.isNullOrBlank()) {
        extensionHost?.loadExtension(item.id, item.jsCode)
      }
      true
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        snackbarMessage = "Failed to import extension: ${e.message}"
      )
      false
    }
  }

  fun importExtensionFromUri(context: Context, uri: Uri): Boolean {
    return try {
      var fileName: String? = null
      context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex != -1 && cursor.moveToFirst()) {
          fileName = cursor.getString(nameIndex)
        }
      }
      val text = context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
      if (!text.isNullOrBlank()) {
        importExtensionApp(text, fileName ?: "imported-extension.json")
      } else false
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        snackbarMessage = "Could not read extension file: ${e.message}"
      )
      false
    }
  }

  // --- Extension Host Integration ---
  private var extensionHost: ExtensionHost? = null

  fun initExtensionHost(context: Context) {
    if (extensionHost == null) {
      val host = ExtensionHost(context, object : ExtensionHostListener {
        override fun onCommandRegistered(command: RegisteredCommand) {
          val current = _uiState.value.registeredExtensionCommands.toMutableList()
          current.removeAll { it.id == command.id }
          current.add(command)
          _uiState.value = _uiState.value.copy(registeredExtensionCommands = current)
        }

        override fun onButtonContributed(button: ContributedButton) {
          val current = _uiState.value.contributedUiButtons.toMutableList()
          current.removeAll { it.id == button.id }
          current.add(button)
          _uiState.value = _uiState.value.copy(contributedUiButtons = current)
        }

        override fun onStatusBarItemContributed(item: ContributedStatusBarItem) {
          val current = _uiState.value.contributedStatusBarItems.toMutableList()
          current.removeAll { it.id == item.id }
          current.add(item)
          _uiState.value = _uiState.value.copy(contributedStatusBarItems = current)
        }

        override fun onStatusBarItemUpdated(id: String, text: String) {
          val current = _uiState.value.contributedStatusBarItems.map {
            if (it.id == id) it.copy(text = text) else it
          }
          _uiState.value = _uiState.value.copy(contributedStatusBarItems = current)
        }

        override fun onShowNotification(type: String, message: String) {
          _uiState.value = _uiState.value.copy(snackbarMessage = message)
        }

        override fun onEditorContentChangeRequested(newContent: String) {
          updateActiveFileContent(newContent)
        }

        override fun getCurrentEditorContent(): String {
          val active = _uiState.value.files.find { it.id == _uiState.value.activeFileId }
            ?: _uiState.value.files.firstOrNull()
          return active?.content ?: ""
        }

        override fun getCurrentEditorFileName(): String {
          val active = _uiState.value.files.find { it.id == _uiState.value.activeFileId }
            ?: _uiState.value.files.firstOrNull()
          return active?.name ?: "index.html"
        }
      })
      extensionHost = host

      // Load active extensions into host
      _uiState.value.installedExtensions.forEach { ext ->
        if (ext.isEnabled && !ext.jsCode.isNullOrBlank()) {
          host.loadExtension(ext.id, ext.jsCode)
        }
      }
    }
  }

  // --- Local Storage & Debounced Auto-Save Integration ---
  fun initLocalStorage(context: Context): Job {
    if (localStorageManager == null) {
      val manager = LocalStorageManager(context.applicationContext)
      localStorageManager = manager

      return viewModelScope.launch {
        val saved = manager.loadProject()
        if (saved != null && saved.files.isNotEmpty()) {
          _uiState.value = _uiState.value.copy(
            files = saved.files,
            folders = saved.folders,
            activeFileId = if (saved.files.any { it.id == saved.activeFileId }) saved.activeFileId else saved.files.first().id,
            openTabIds = saved.openTabIds,
            autoSaveStatus = AutoSaveStatus.SAVED,
            lastSavedTime = "Restored"
          )
        } else {
          // Persist initial template files immediately
          manager.saveProject(
            files = _uiState.value.files,
            folders = _uiState.value.folders,
            activeFileId = _uiState.value.activeFileId,
            openTabIds = _uiState.value.openTabIds
          )
          val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
          _uiState.value = _uiState.value.copy(
            autoSaveStatus = AutoSaveStatus.SAVED,
            lastSavedTime = timeStr
          )
        }

        startPeriodicAutoSave()
      }
    }
    return Job().apply { complete() }
  }

  fun triggerDebouncedAutoSave(delayMs: Long = 800L): Job? {
    if (!_uiState.value.isAutoSaveEnabled) return null

    _uiState.value = _uiState.value.copy(autoSaveStatus = AutoSaveStatus.UNSAVED)
    autoSaveJob?.cancel()
    val job = viewModelScope.launch(Dispatchers.IO) {
      delay(delayMs)
      performSave()
    }
    autoSaveJob = job
    return job
  }

  fun saveActiveFileImmediately(): Job {
    autoSaveJob?.cancel()
    return viewModelScope.launch(Dispatchers.IO) {
      performSave()
      withContext(Dispatchers.Main) {
        _uiState.value = _uiState.value.copy(snackbarMessage = "💾 All files saved to local storage")
      }
    }
  }

  fun toggleAutoSave() {
    val newEnabled = !_uiState.value.isAutoSaveEnabled
    _uiState.value = _uiState.value.copy(
      isAutoSaveEnabled = newEnabled,
      snackbarMessage = if (newEnabled) "Auto-Save enabled (800ms debounce)" else "Auto-Save disabled"
    )
    if (newEnabled) {
      triggerDebouncedAutoSave(0L)
    }
  }

  internal suspend fun performSave(): Boolean {
    val manager = localStorageManager ?: return false
    val currentState = _uiState.value

    _uiState.value = _uiState.value.copy(autoSaveStatus = AutoSaveStatus.SAVING)

    val success = manager.saveProject(
      files = currentState.files,
      folders = currentState.folders,
      activeFileId = currentState.activeFileId,
      openTabIds = currentState.openTabIds
    )

    val nowStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
    withContext(Dispatchers.Main) {
      if (success) {
        val clearedFiles = _uiState.value.files.map { it.copy(isModified = false) }
        _uiState.value = _uiState.value.copy(
          files = clearedFiles,
          autoSaveStatus = AutoSaveStatus.SAVED,
          lastSavedTime = nowStr
        )
      } else {
        _uiState.value = _uiState.value.copy(autoSaveStatus = AutoSaveStatus.UNSAVED)
      }
    }
    return success
  }

  private fun startPeriodicAutoSave() {
    periodicSaveJob?.cancel()
    periodicSaveJob = viewModelScope.launch(Dispatchers.IO) {
      while (isActive) {
        delay(15_000L)
        if (_uiState.value.isAutoSaveEnabled && _uiState.value.files.any { it.isModified }) {
          performSave()
        }
      }
    }
  }

  fun executeCommand(commandId: String) {
    when (commandId) {
      "workbench.action.quickOpen" -> toggleGlobalFileSearch(true)
      "workbench.action.files.save" -> saveActiveFileImmediately()
      "workbench.action.toggleAutoSave" -> toggleAutoSave()
      "workbench.action.formatDocument" -> formatCurrentCode()
      "workbench.action.findInFiles" -> setActivePanel(ActivePanel.SEARCH)
      "editor.action.find", "editor.action.replace" -> toggleFindReplace(true)
      "workbench.action.toggleSidebar" -> {
        setActivePanel(if (_uiState.value.activePanel == ActivePanel.EXPLORER) ActivePanel.RUNNER else ActivePanel.EXPLORER)
      }
      "workbench.action.openExtensions" -> setActivePanel(ActivePanel.EXTENSIONS)
      "workbench.action.importExtension" -> toggleImportExtensionDialog(true)
      "workbench.action.showConsole" -> setActivePanel(ActivePanel.CONSOLE)
      "workbench.action.toggleLivePreview" -> toggleLivePreview()
      "workbench.action.togglePreviewOrientation" -> togglePreviewOrientation()
      "workbench.action.reloadPreview", "workbench.action.reloadRunner" -> reloadLivePreview()
      else -> {
        extensionHost?.executeCommand(commandId)
      }
    }
    _uiState.value = _uiState.value.copy(showCommandPalette = false)
  }

  fun toggleCommandPalette(show: Boolean) {
    _uiState.value = _uiState.value.copy(showCommandPalette = show)
  }

  fun toggleGlobalFileSearch(show: Boolean) {
    _uiState.value = _uiState.value.copy(showGlobalFileSearch = show)
  }

  fun openFileFromGlobalSearch(fileId: String) {
    selectFile(fileId)
    _uiState.value = _uiState.value.copy(
      showGlobalFileSearch = false,
      activePanel = if (_uiState.value.activePanel == ActivePanel.RUNNER) ActivePanel.EXPLORER else _uiState.value.activePanel
    )
  }

  fun createAndOpenFileFromSearch(fileName: String) {
    createNewFile(fileName, null)
    _uiState.value = _uiState.value.copy(
      showGlobalFileSearch = false,
      activePanel = if (_uiState.value.activePanel == ActivePanel.RUNNER) ActivePanel.EXPLORER else _uiState.value.activePanel
    )
  }

  // --- Editor Find & Replace ---
  fun toggleFindReplace(open: Boolean? = null) {
    val isOpen = open ?: !_uiState.value.isFindReplaceOpen
    _uiState.value = _uiState.value.copy(isFindReplaceOpen = isOpen)
    if (isOpen && _uiState.value.findQuery.isNotEmpty()) {
      recalculateMatches()
    }
  }

  fun setFindQuery(query: String) {
    _uiState.value = _uiState.value.copy(findQuery = query)
    recalculateMatches()
  }

  fun setReplaceQuery(query: String) {
    _uiState.value = _uiState.value.copy(replaceQuery = query)
  }

  fun toggleMatchCase() {
    _uiState.value = _uiState.value.copy(matchCase = !_uiState.value.matchCase)
    recalculateMatches()
  }

  fun toggleMatchWholeWord() {
    _uiState.value = _uiState.value.copy(matchWholeWord = !_uiState.value.matchWholeWord)
    recalculateMatches()
  }

  fun toggleUseRegex() {
    _uiState.value = _uiState.value.copy(useRegex = !_uiState.value.useRegex)
    recalculateMatches()
  }

  fun findNextMatch() {
    val total = _uiState.value.totalMatches
    if (total <= 0) return
    val next = (_uiState.value.currentMatchIndex + 1) % total
    _uiState.value = _uiState.value.copy(currentMatchIndex = next)
  }

  fun findPreviousMatch() {
    val total = _uiState.value.totalMatches
    if (total <= 0) return
    val prev = if (_uiState.value.currentMatchIndex <= 0) total - 1 else _uiState.value.currentMatchIndex - 1
    _uiState.value = _uiState.value.copy(currentMatchIndex = prev)
  }

  private fun calculateMatchRanges(
    code: String,
    query: String,
    matchCase: Boolean,
    wholeWord: Boolean,
    useRegex: Boolean
  ): List<IntRange> {
    if (query.isEmpty() || code.isEmpty()) return emptyList()
    return try {
      val flags = if (matchCase) 0 else Pattern.CASE_INSENSITIVE
      val patternString = when {
        useRegex && wholeWord -> "\\b(?:$query)\\b"
        useRegex -> query
        wholeWord -> "\\b" + Pattern.quote(query) + "\\b"
        else -> Pattern.quote(query)
      }
      val pattern = Pattern.compile(patternString, flags)
      val matcher = pattern.matcher(code)
      val ranges = mutableListOf<IntRange>()
      while (matcher.find()) {
        val s = matcher.start()
        val e = matcher.end()
        if (s < e) {
          ranges.add(s until e)
        }
      }
      ranges
    } catch (_: Exception) {
      emptyList()
    }
  }

  private fun recalculateMatches() {
    val active = _uiState.value.files.find { it.id == _uiState.value.activeFileId } ?: return
    val ranges = calculateMatchRanges(
      code = active.content,
      query = _uiState.value.findQuery,
      matchCase = _uiState.value.matchCase,
      wholeWord = _uiState.value.matchWholeWord,
      useRegex = _uiState.value.useRegex
    )
    val newIndex = if (ranges.isEmpty()) 0 else _uiState.value.currentMatchIndex.coerceIn(0, ranges.size - 1)
    _uiState.value = _uiState.value.copy(
      totalMatches = ranges.size,
      currentMatchIndex = newIndex
    )
  }

  fun replaceCurrentMatch() {
    val active = _uiState.value.files.find { it.id == _uiState.value.activeFileId } ?: return
    val ranges = calculateMatchRanges(
      code = active.content,
      query = _uiState.value.findQuery,
      matchCase = _uiState.value.matchCase,
      wholeWord = _uiState.value.matchWholeWord,
      useRegex = _uiState.value.useRegex
    )
    if (ranges.isEmpty()) return
    val index = _uiState.value.currentMatchIndex.coerceIn(0, ranges.size - 1)
    val targetRange = ranges[index]

    val newContent = active.content.replaceRange(targetRange.first, targetRange.last + 1, _uiState.value.replaceQuery)
    updateActiveFileContent(newContent)
    recalculateMatches()
  }

  fun replaceAllMatches() {
    val active = _uiState.value.files.find { it.id == _uiState.value.activeFileId } ?: return
    val query = _uiState.value.findQuery
    if (query.isEmpty()) return

    try {
      val flags = if (_uiState.value.matchCase) 0 else Pattern.CASE_INSENSITIVE
      val patternString = when {
        _uiState.value.useRegex && _uiState.value.matchWholeWord -> "\\b(?:$query)\\b"
        _uiState.value.useRegex -> query
        _uiState.value.matchWholeWord -> "\\b" + Pattern.quote(query) + "\\b"
        else -> Pattern.quote(query)
      }
      val pattern = Pattern.compile(patternString, flags)
      val matcher = pattern.matcher(active.content)
      var count = 0
      while (matcher.find()) {
        count++
      }
      if (count > 0) {
        val replaced = pattern.matcher(active.content).replaceAll(Matcher.quoteReplacement(_uiState.value.replaceQuery))
        updateActiveFileContent(replaced)
        _uiState.value = _uiState.value.copy(
          snackbarMessage = "Replaced $count occurrences of '$query'"
        )
        recalculateMatches()
      }
    } catch (_: Exception) {}
  }

  private fun injectSnippetIntoIndexHtml(snippet: String) {
    val htmlFile = _uiState.value.files.find { it.id == "index.html" } ?: return
    if (!htmlFile.content.contains(snippet)) {
      val newContent = if (htmlFile.content.contains("</head>")) {
        htmlFile.content.replace("</head>", "  $snippet\n</head>")
      } else {
        "$snippet\n${htmlFile.content}"
      }
      val updatedFiles = _uiState.value.files.map {
        if (it.id == "index.html") it.copy(content = newContent, isModified = true) else it
      }
      _uiState.value = _uiState.value.copy(
        files = updatedFiles,
        snackbarMessage = "Injected extension script into index.html"
      )
    }
  }

  // --- Panels & UI navigation ---
  fun setActivePanel(panel: ActivePanel) {
    _uiState.value = _uiState.value.copy(activePanel = panel)
  }

  fun setSearchQuery(query: String) {
    val count = if (query.isBlank()) 0 else {
      _uiState.value.files.sumOf { file ->
        val regex = Regex(Regex.escape(query), RegexOption.IGNORE_CASE)
        regex.findAll(file.content).count()
      }
    }
    _uiState.value = _uiState.value.copy(
      searchQuery = query,
      searchResultsCount = count
    )
  }

  fun setPreviewDevice(device: PreviewDevice) {
    _uiState.value = _uiState.value.copy(previewDevice = device)
  }

  fun reloadWebRunner() {
    _uiState.value = _uiState.value.copy(
      runnerReloadTrigger = _uiState.value.runnerReloadTrigger + 1,
      snackbarMessage = "Reloading Live Web Runner..."
    )
  }

  fun addConsoleLog(level: String, text: String) {
    val newLog = ConsoleMessage(
      id = System.currentTimeMillis().toString() + "_" + (0..999).random(),
      level = level,
      text = text,
      timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
    )
    val updated = (_uiState.value.consoleLogs + newLog).takeLast(100)
    _uiState.value = _uiState.value.copy(consoleLogs = updated)
  }

  fun clearConsole() {
    _uiState.value = _uiState.value.copy(consoleLogs = emptyList())
  }

  fun evaluateConsoleJs(input: String) {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) return
    // Log input command
    addConsoleLog("INFO", "> $trimmed")
    try {
      // Execute in extensionHost if available or evaluate simple math / JS
      if (trimmed.startsWith("console.log(") && trimmed.endsWith(")")) {
        val arg = trimmed.removeSurrounding("console.log(", ")").trim().removeSurrounding("\"", "\"").removeSurrounding("'", "'")
        addConsoleLog("LOG", arg)
      } else if (trimmed.startsWith("console.error(") && trimmed.endsWith(")")) {
        val arg = trimmed.removeSurrounding("console.error(", ")").trim().removeSurrounding("\"", "\"").removeSurrounding("'", "'")
        addConsoleLog("ERROR", arg)
      } else if (trimmed.startsWith("console.warn(") && trimmed.endsWith(")")) {
        val arg = trimmed.removeSurrounding("console.warn(", ")").trim().removeSurrounding("\"", "\"").removeSurrounding("'", "'")
        addConsoleLog("WARN", arg)
      } else {
        extensionHost?.evaluateScript("try { var __res = eval(${org.json.JSONObject.quote(trimmed)}); if (__res !== undefined) console.log(String(__res)); } catch(e) { console.error(e.message); }")
          ?: run {
            addConsoleLog("LOG", "Executed: $trimmed")
          }
      }
    } catch (e: Exception) {
      addConsoleLog("ERROR", e.message ?: "Evaluation error")
    }
  }

  fun runTerminalCommand(command: String) {
    val cmd = command.trim()
    if (cmd.isEmpty()) return
    val current = _uiState.value.terminalLogs.toMutableList()
    current.add("$ $cmd")

    when {
      cmd == "clear" -> {
        _uiState.value = _uiState.value.copy(terminalLogs = listOf("$ clear"))
        return
      }
      cmd == "help" -> {
        current.add("Available commands:")
        current.add("  find / open / p     Open Global File Search overlay (Quick Open)")
        current.add("  ls                  List project files")
        current.add("  cat <file>          Show file contents")
        current.add("  bundle              Generate standalone HTML")
        current.add("  preview             Toggle real-time HTML Live Preview")
        current.add("  preview-layout      Toggle Live Preview orientation (side-by-side / top-bottom)")
        current.add("  run / live          Launch live web runner")
        current.add("  console             Open Debug Console panel")
        current.add("  extensions          List installed extensions")
        current.add("  import-ext <preset> Import extension app (pomodoro, markdown, glass, clean, or file)")
        current.add("  import-ext dialog   Open Import Extension dialog")
        current.add("  version             Display current Easy Code edition")
        current.add("  wc -l <file>        Count lines in file")
        current.add("  clear               Clear terminal")
      }
      cmd == "find" || cmd == "open" || cmd == "quickopen" || cmd == "p" || cmd == "goto" -> {
        toggleGlobalFileSearch(true)
        current.add("🔍 Opening Global File Search overlay (Quick Open Ctrl+P)...")
      }
      cmd == "ls" -> {
        _uiState.value.files.forEach { file ->
          current.add("  ${file.name.padEnd(16)} ${(file.content.length)} bytes")
        }
      }
      cmd.startsWith("cat ") -> {
        val target = cmd.substringAfter("cat ").trim()
        val file = _uiState.value.files.find { it.name == target || it.id == target }
        if (file != null) {
          current.add(file.content.take(400) + if (file.content.length > 400) "\n... [truncated]" else "")
        } else {
          current.add("cat: $target: No such file")
        }
      }
      cmd == "bundle" -> {
        val bundled = generateStandaloneHtml()
        current.add("Successfully bundled ${bundled.length} chars of standalone HTML.")
        current.add("Tip: Use the top bar 'Copy HTML' button to copy to Android clipboard.")
      }
      cmd == "preview" || cmd == "live-preview" -> {
        toggleLivePreview()
        current.add("⚡ Live HTML Preview is now: ${if (_uiState.value.isLivePreviewOpen) "ENABLED (Split View Active)" else "DISABLED"}")
      }
      cmd == "preview-layout" || cmd == "split" -> {
        togglePreviewOrientation()
        current.add("Preview layout switched to: ${_uiState.value.previewSplitOrientation.label}")
      }
      cmd == "run" || cmd == "live" -> {
        current.add("Starting Live Web Runner on http://localhost:3000...")
        _uiState.value = _uiState.value.copy(activePanel = ActivePanel.RUNNER)
      }
      cmd == "console" -> {
        current.add("Opening Debug Console panel...")
        _uiState.value = _uiState.value.copy(activePanel = ActivePanel.CONSOLE)
      }
      cmd == "extensions" -> {
        current.add("Installed Extensions (${_uiState.value.installedExtensions.size}):")
        _uiState.value.installedExtensions.forEach { ext ->
          current.add("  • [${if (ext.isEnabled) "ACTIVE" else "DISABLED"}] ${ext.name} (v${ext.version})")
        }
      }
      cmd.startsWith("import-ext") || cmd.startsWith("import ") -> {
        val arg = if (cmd.startsWith("import-ext")) cmd.removePrefix("import-ext").trim() else cmd.removePrefix("import ").trim()
        when {
          arg.isEmpty() || arg == "dialog" -> {
            toggleImportExtensionDialog(true)
            current.add("[EasyCode] Opened Import Extension App dialog.")
          }
          arg == "pomodoro" || arg == "timer" -> {
            val preset = ExtensionAppImporter.PRESETS[0]
            val obj = org.json.JSONObject(preset.manifestJson)
            obj.put("script", preset.jsCode)
            importExtensionApp(obj.toString(2), "pomodoro-manifest.json")
            current.add("[EasyCode] Imported Pomodoro Focus Timer App!")
          }
          arg == "markdown" || arg == "md" -> {
            val preset = ExtensionAppImporter.PRESETS[1]
            val obj = org.json.JSONObject(preset.manifestJson)
            obj.put("script", preset.jsCode)
            importExtensionApp(obj.toString(2), "markdown-manifest.json")
            current.add("[EasyCode] Imported Markdown Pro & Doc Tools App!")
          }
          arg == "glass" || arg == "glassmorphism" -> {
            val preset = ExtensionAppImporter.PRESETS[2]
            val obj = org.json.JSONObject(preset.manifestJson)
            obj.put("script", preset.jsCode)
            importExtensionApp(obj.toString(2), "glass-manifest.json")
            current.add("[EasyCode] Imported Glassmorphism UI Styler App!")
          }
          arg == "clean" || arg == "logs" -> {
            val preset = ExtensionAppImporter.PRESETS[3]
            val obj = org.json.JSONObject(preset.manifestJson)
            obj.put("script", preset.jsCode)
            importExtensionApp(obj.toString(2), "clean-manifest.json")
            current.add("[EasyCode] Imported Console Log & Comment Stripper App!")
          }
          else -> {
            val file = _uiState.value.files.find { it.name == arg || it.id == arg }
            if (file != null) {
              importExtensionApp(file.content, file.name)
              current.add("[EasyCode] Successfully imported extension from workspace file: ${file.name}")
            } else {
              current.add("[EasyCode] Unknown preset or file '$arg'. Available presets: pomodoro, markdown, glass, clean, or type 'import-ext dialog'.")
            }
          }
        }
      }
      cmd == "version" -> {
        current.add("Easy Code: ${_uiState.value.currentVersion.versionLabel}")
        current.add("Edition: ${_uiState.value.currentVersion.editionName}")
        current.add("Theme: ${_uiState.value.currentVersion.themeStyle}")
      }
      cmd.startsWith("wc -l ") -> {
        val target = cmd.substringAfter("wc -l ").trim()
        val file = _uiState.value.files.find { it.name == target }
        if (file != null) {
          val lines = file.content.lines().size
          current.add("  $lines $target")
        } else {
          current.add("wc: $target: No such file")
        }
      }
      else -> {
        current.add("bash: $cmd: command not found. Type 'help' for commands.")
      }
    }

    _uiState.value = _uiState.value.copy(terminalLogs = current)
  }

  fun clearTerminalLogs() {
    _uiState.value = _uiState.value.copy(
      terminalLogs = listOf(
        "Easy Code [Version 1.94.2-sophisticated-dark]",
        "Host: Android / Web Hybrid Engine",
        "Workspace: /storage/emulated/0/EasyCodeProjects/web-app",
        "$ ready."
      )
    )
  }

  fun dismissSnackbar() {
    _uiState.value = _uiState.value.copy(snackbarMessage = null)
  }
}
