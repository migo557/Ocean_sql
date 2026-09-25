package com.example.extensions

import com.example.models.ExtensionItem
import org.json.JSONArray
import org.json.JSONObject

data class ExtensionAppPreset(
  val id: String,
  val name: String,
  val author: String,
  val version: String,
  val description: String,
  val iconEmoji: String,
  val category: String,
  val manifestJson: String,
  val jsCode: String,
  val sampleSnippet: String? = null
)

object ExtensionAppImporter {

  val PRESETS = listOf(
    ExtensionAppPreset(
      id = "imported.pomodoro_timer",
      name = "Pomodoro Focus Timer App",
      author = "Productivity Labs",
      version = "1.2.0",
      description = "Interactive 25-minute Pomodoro timer directly in your status bar with bell alerts and toolbar control.",
      iconEmoji = "⏱️",
      category = "Tools",
      manifestJson = """{
  "name": "pomodoro-focus-timer",
  "displayName": "Pomodoro Focus Timer App",
  "version": "1.2.0",
  "publisher": "Productivity Labs",
  "description": "Interactive 25-minute Pomodoro timer directly in your status bar with bell alerts and toolbar control.",
  "category": "Tools",
  "icon": "⏱️",
  "contributes": {
    "commands": [
      { "command": "extension.startPomodoro", "title": "Pomodoro: Start 25m Timer" },
      { "command": "extension.resetPomodoro", "title": "Pomodoro: Reset Timer" }
    ],
    "buttons": [
      { "id": "btn_pomodoro", "label": "⏱️ Pomodoro", "command": "extension.startPomodoro" }
    ],
    "statusBar": [
      { "id": "status_pomo", "text": "⏱️ 25:00 Focus", "command": "extension.startPomodoro" }
    ]
  }
}""",
      jsCode = """// Pomodoro Focus Timer Extension App
var pomoMinutes = 25;
var pomoSeconds = 0;
var pomoTimer = null;
var pomoRunning = false;

function updatePomoStatus() {
  var minStr = (pomoMinutes < 10 ? '0' : '') + pomoMinutes;
  var secStr = (pomoSeconds < 10 ? '0' : '') + pomoSeconds;
  vscode.window.updateStatusBarItem('status_pomo', '⏱️ ' + minStr + ':' + secStr + (pomoRunning ? ' (Active)' : ' (Paused)'));
}

vscode.commands.registerCommand('extension.startPomodoro', function() {
  if (pomoRunning) {
    pomoRunning = false;
    if (pomoTimer) clearInterval(pomoTimer);
    updatePomoStatus();
    vscode.window.showInformationMessage('Pomodoro paused at ' + pomoMinutes + 'm remaining.');
    return;
  }
  pomoRunning = true;
  vscode.window.showInformationMessage('Pomodoro session started! 25 minutes of deep focus.');
  updatePomoStatus();
  
  pomoTimer = setInterval(function() {
    if (!pomoRunning) return;
    if (pomoSeconds === 0) {
      if (pomoMinutes === 0) {
        clearInterval(pomoTimer);
        pomoRunning = false;
        vscode.window.showInformationMessage('🎉 Pomodoro complete! Great work. Time for a 5-minute break.');
        pomoMinutes = 25;
        pomoSeconds = 0;
        updatePomoStatus();
        return;
      }
      pomoMinutes--;
      pomoSeconds = 59;
    } else {
      pomoSeconds--;
    }
    updatePomoStatus();
  }, 1000);
});

vscode.commands.registerCommand('extension.resetPomodoro', function() {
  if (pomoTimer) clearInterval(pomoTimer);
  pomoRunning = false;
  pomoMinutes = 25;
  pomoSeconds = 0;
  updatePomoStatus();
  vscode.window.showInformationMessage('Pomodoro timer reset to 25:00.');
});

vscode.ui.addButton({
  id: 'btn_pomodoro',
  label: '⏱️ Pomodoro',
  command: 'extension.startPomodoro',
  tooltip: 'Start or Pause Pomodoro Focus Timer'
});

vscode.window.createStatusBarItem({
  id: 'status_pomo',
  text: '⏱️ 25:00 Focus',
  command: 'extension.startPomodoro',
  tooltip: 'Click to start or pause Pomodoro Timer'
});
"""
    ),
    ExtensionAppPreset(
      id = "imported.markdown_preview",
      name = "Markdown Pro & Doc Tools",
      author = "Docs Community",
      version = "2.0.4",
      description = "Transforms active Markdown documents into clean styled HTML cards with table generator.",
      iconEmoji = "📝",
      category = "Formatters",
      manifestJson = """{
  "name": "markdown-pro-doc-tools",
  "displayName": "Markdown Pro & Doc Tools",
  "version": "2.0.4",
  "publisher": "Docs Community",
  "description": "Transforms active Markdown documents into clean styled HTML cards with table generator.",
  "category": "Formatters",
  "icon": "📝",
  "contributes": {
    "commands": [
      { "command": "extension.insertMarkdownTable", "title": "Markdown: Insert Table" },
      { "command": "extension.formatHeadingBanner", "title": "Markdown: Format Heading Banner" }
    ],
    "buttons": [
      { "id": "btn_md_table", "label": "📊 Add Table", "command": "extension.insertMarkdownTable" }
    ],
    "snippets": [
      { "label": "md-card", "body": "# 🚀 Project Title\n\n> Quick summary of your application.\n\n| Feature | Status | Notes |\n| :--- | :---: | ---: |\n| Engine | Ready | Fast load |\n" }
    ]
  }
}""",
      jsCode = """// Markdown Pro Extension App
vscode.commands.registerCommand('extension.insertMarkdownTable', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var table = '\n| Header 1 | Header 2 | Header 3 |\n| :--- | :---: | ---: |\n| Item Alpha | Active | Primary |\n| Item Beta | Done | Secondary |\n';
  editor.edit(function(builder) {
    builder.insert(editor.document.getText().length, table);
  });
  vscode.window.showInformationMessage('Markdown Table appended to document!');
});

vscode.commands.registerCommand('extension.formatHeadingBanner', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var content = editor.document.getText();
  var banner = '<!-- ========================================= -->\n<!-- Easy Code Project Documentation Banner -->\n<!-- ========================================= -->\n\n';
  editor.edit(function(builder) {
    builder.insert(0, banner);
  });
  vscode.window.showInformationMessage('Documentation banner inserted at top of document!');
});

vscode.ui.addButton({
  id: 'btn_md_table',
  label: '📊 Add Table',
  command: 'extension.insertMarkdownTable',
  tooltip: 'Insert standard Markdown data table'
});
""",
      sampleSnippet = """# 🚀 Easy Code App\n\n| Module | Status |\n|---|---|\n| Core | Active |\n"""
    ),
    ExtensionAppPreset(
      id = "imported.glassmorphism",
      name = "Glassmorphism UI Styler",
      author = "Creative UI Lab",
      version = "1.0.8",
      description = "Injects sleek frosted glass CSS cards and backdrop-filter glow styles with one click.",
      iconEmoji = "✨",
      category = "UI Libraries",
      manifestJson = """{
  "name": "glassmorphism-ui-styler",
  "displayName": "Glassmorphism UI Styler",
  "version": "1.0.8",
  "publisher": "Creative UI Lab",
  "description": "Injects sleek frosted glass CSS cards and backdrop-filter glow styles with one click.",
  "category": "UI Libraries",
  "icon": "✨",
  "contributes": {
    "commands": [
      { "command": "extension.injectGlassmorphism", "title": "Glass UI: Inject Frosted Glass Card" }
    ],
    "buttons": [
      { "id": "btn_glass_inject", "label": "✨ Glass Card", "command": "extension.injectGlassmorphism" }
    ]
  }
}""",
      jsCode = """// Glassmorphism UI Styler App
vscode.commands.registerCommand('extension.injectGlassmorphism', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var glassStyles = '\n/* Frosted Glass Component */\n.glass-card {\n  background: rgba(255, 255, 255, 0.08);\n  backdrop-filter: blur(16px);\n  -webkit-backdrop-filter: blur(16px);\n  border: 1px solid rgba(255, 255, 255, 0.18);\n  border-radius: 16px;\n  box-shadow: 0 8px 32px 0 rgba(0, 0, 0, 0.37);\n  padding: 24px;\n  color: #f3f4f6;\n}\n';
  editor.edit(function(builder) {
    builder.insert(editor.document.getText().length, glassStyles);
  });
  vscode.window.showInformationMessage('Injected .glass-card CSS styling!');
});

vscode.ui.addButton({
  id: 'btn_glass_inject',
  label: '✨ Glass Card',
  command: 'extension.injectGlassmorphism',
  tooltip: 'Append Frosted Glass styling to active file'
});
"""
    ),
    ExtensionAppPreset(
      id = "imported.clean_code",
      name = "Console Log & Comment Stripper",
      author = "CodeClean Pro",
      version = "1.5.0",
      description = "Strips console.log statements and trailing white space for production-ready distribution.",
      iconEmoji = "🧹",
      category = "Tools",
      manifestJson = """{
  "name": "console-comment-stripper",
  "displayName": "Console Log & Comment Stripper",
  "version": "1.5.0",
  "publisher": "CodeClean Pro",
  "description": "Strips console.log statements and trailing white space for production-ready distribution.",
  "category": "Tools",
  "icon": "🧹",
  "contributes": {
    "commands": [
      { "command": "extension.stripConsoleLogs", "title": "Cleaner: Strip console.log statements" }
    ],
    "buttons": [
      { "id": "btn_strip_logs", "label": "🧹 Strip Logs", "command": "extension.stripConsoleLogs" }
    ]
  }
}""",
      jsCode = """// Console Log Stripper Extension App
vscode.commands.registerCommand('extension.stripConsoleLogs', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var text = editor.document.getText();
  // Strip console.log(...) lines
  var cleaned = text.replace(/console\.(log|debug|info)\([^)]*\);?/g, '');
  editor.edit(function(builder) {
    builder.replace(cleaned);
  });
  vscode.window.showInformationMessage('Stripped all console.log statements from active file!');
});

vscode.ui.addButton({
  id: 'btn_strip_logs',
  label: '🧹 Strip Logs',
  command: 'extension.stripConsoleLogs',
  tooltip: 'Strip all console.log statements'
});
"""
    )
  )

  /**
   * Parses an extension from raw text (JSON manifest, VS Code package.json, or JavaScript script).
   */
  fun parseExtension(rawContent: String, fileName: String? = null): ExtensionItem {
    val trimmed = rawContent.trim()

    // 1. Try parsing as JSON manifest
    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
      try {
        val json = JSONObject(trimmed)
        val name = json.optString("displayName", json.optString("name", fileName?.substringBeforeLast(".") ?: "Imported App"))
        val rawId = json.optString("name", name.lowercase().replace("[^a-z0-9]".toRegex(), "."))
        val id = if (rawId.startsWith("imported.")) rawId else "imported.$rawId"
        val author = json.optString("publisher", json.optString("author", "External Developer"))
        val version = json.optString("version", "1.0.0")
        val description = json.optString("description", "Imported extension application package.")
        val icon = json.optString("icon", json.optString("iconEmoji", "📦"))
        
        var category = "Tools"
        if (json.has("categories")) {
          val catArray = json.optJSONArray("categories")
          if (catArray != null && catArray.length() > 0) {
            category = catArray.getString(0)
          }
        } else if (json.has("category")) {
          category = json.optString("category", "Tools")
        }

        val commands = mutableListOf<String>()
        val buttons = mutableListOf<String>()
        var snippet: String? = null

        // Parse contributes
        val contributes = json.optJSONObject("contributes")
        if (contributes != null) {
          val cmds = contributes.optJSONArray("commands")
          if (cmds != null) {
            for (i in 0 until cmds.length()) {
              val cmdObj = cmds.optJSONObject(i)
              if (cmdObj != null) {
                val cmdId = cmdObj.optString("command")
                if (cmdId.isNotBlank()) commands.add(cmdId)
              } else {
                val cmdStr = cmds.optString(i)
                if (cmdStr.isNotBlank()) commands.add(cmdStr)
              }
            }
          }

          val btns = contributes.optJSONArray("buttons")
          if (btns != null) {
            for (i in 0 until btns.length()) {
              val btnObj = btns.optJSONObject(i)
              if (btnObj != null) {
                val btnId = btnObj.optString("id", btnObj.optString("label"))
                if (btnId.isNotBlank()) buttons.add(btnId)
              }
            }
          }

          val snips = contributes.optJSONArray("snippets")
          if (snips != null && snips.length() > 0) {
            val snipObj = snips.optJSONObject(0)
            if (snipObj != null) {
              snippet = snipObj.optString("body", snipObj.optString("snippet"))
            }
          }
        }

        // Script might be inline in "script", "jsCode", or bundled
        val jsCode = json.optString("script", json.optString("jsCode", ""))

        // If no inline script but commands exist, synthesize handler if empty
        val finalJs = if (jsCode.isNotBlank()) {
          jsCode
        } else if (commands.isNotEmpty()) {
          buildSynthesizedScript(name, commands)
        } else null

        return ExtensionItem(
          id = id,
          name = name,
          author = author,
          version = version,
          description = description,
          iconEmoji = icon,
          category = category,
          isInstalled = true,
          isEnabled = true,
          downloads = "1",
          rating = 5.0f,
          codeSnippet = snippet,
          isCustom = true,
          jsCode = finalJs,
          contributedCommands = commands,
          contributedButtons = buttons,
          isImported = true,
          sourceFileName = fileName
        )
      } catch (_: Exception) {
        // Fall back to JS parsing below
      }
    }

    // 2. Parse as JavaScript Extension App
    val detectedName = extractJsComment(trimmed, "name")
      ?: fileName?.substringBeforeLast(".")
      ?: "Imported JS Extension"
    val detectedAuthor = extractJsComment(trimmed, "author") ?: "Imported"
    val detectedVersion = extractJsComment(trimmed, "version") ?: "1.0.0"
    val detectedDesc = extractJsComment(trimmed, "description")
      ?: "Imported JavaScript extension script for Easy Code."
    val detectedCategory = extractJsComment(trimmed, "category") ?: "Tools"
    val detectedIcon = extractJsComment(trimmed, "icon") ?: "⚡"

    val detectedCommands = mutableListOf<String>()
    val cmdRegex = Regex("""vscode\.commands\.registerCommand\(\s*['"]([^'"]+)['"]""")
    cmdRegex.findAll(trimmed).forEach { match ->
      val cmdId = match.groupValues[1]
      if (!detectedCommands.contains(cmdId)) {
        detectedCommands.add(cmdId)
      }
    }

    val detectedButtons = mutableListOf<String>()
    val btnRegex = Regex("""vscode\.ui\.addButton\(\s*['"]([^'"]+)['"]|id:\s*['"]([^'"]+)['"]""")
    btnRegex.findAll(trimmed).forEach { match ->
      val btnId = match.groupValues.drop(1).firstOrNull { it.isNotEmpty() }
      if (btnId != null && !detectedButtons.contains(btnId)) {
        detectedButtons.add(btnId)
      }
    }

    val id = "imported." + detectedName.lowercase().replace("[^a-z0-9]".toRegex(), ".")

    return ExtensionItem(
      id = id,
      name = detectedName,
      author = detectedAuthor,
      version = detectedVersion,
      description = detectedDesc,
      iconEmoji = detectedIcon,
      category = detectedCategory,
      isInstalled = true,
      isEnabled = true,
      downloads = "1",
      rating = 5.0f,
      codeSnippet = null,
      isCustom = true,
      jsCode = trimmed,
      contributedCommands = detectedCommands,
      contributedButtons = detectedButtons,
      isImported = true,
      sourceFileName = fileName
    )
  }

  private fun extractJsComment(code: String, key: String): String? {
    val regex1 = Regex("""//\s*@$key\s+([^\r\n]+)""", RegexOption.IGNORE_CASE)
    val match1 = regex1.find(code)
    if (match1 != null) return match1.groupValues[1].trim()

    val regex2 = Regex("""//\s*$key:\s*([^\r\n]+)""", RegexOption.IGNORE_CASE)
    val match2 = regex2.find(code)
    if (match2 != null) return match2.groupValues[1].trim()

    return null
  }

  private fun buildSynthesizedScript(name: String, commands: List<String>): String {
    val sb = StringBuilder()
    sb.append("// Auto-generated script for $name\n")
    for (cmd in commands) {
      sb.append("vscode.commands.registerCommand('$cmd', function() {\n")
      sb.append("  vscode.window.showInformationMessage('Executed $cmd from $name');\n")
      sb.append("});\n\n")
    }
    return sb.toString()
  }
}
