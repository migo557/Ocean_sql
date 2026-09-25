package com.example.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ActivityBar
import com.example.ui.components.CodeEditorView
import com.example.ui.components.ConsolePanel
import com.example.ui.components.ExtensionsView
import com.example.ui.components.FileExplorerView
import com.example.ui.components.LiveHtmlPreviewPane
import com.example.ui.components.LiveWebRunnerView
import com.example.ui.components.SearchView
import com.example.ui.components.StatusBar
import com.example.ui.components.TerminalView
import com.example.ui.components.TopBar
import com.example.ui.dialogs.AddExtensionDialog
import com.example.ui.dialogs.CommandPaletteDialog
import com.example.ui.dialogs.GlobalFileSearchOverlay
import com.example.ui.dialogs.ImportExtensionDialog
import com.example.ui.dialogs.VersionChoiceDialog
import com.example.ui.theme.VsCodeBg
import com.example.ui.theme.VsCodeBorder
import com.example.viewmodel.ActivePanel
import com.example.viewmodel.PreviewSplitOrientation
import com.example.viewmodel.StudioCodeViewModel

@Composable
fun StudioCodeApp(
  modifier: Modifier = Modifier,
  viewModel: StudioCodeViewModel = viewModel()
) {
  val context = LocalContext.current
  val state by viewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }
  var showMobileExplorer by remember { mutableStateOf(false) }

  // Initialize Local Storage & Extension Host runtime with Android Webview
  LaunchedEffect(Unit) {
    viewModel.initLocalStorage(context)
    viewModel.initExtensionHost(context)
  }

  // Handle snackbar feedback
  LaunchedEffect(state.snackbarMessage) {
    state.snackbarMessage?.let { msg ->
      snackbarHostState.showSnackbar(
        message = msg,
        duration = SnackbarDuration.Short
      )
    }
  }

  val activeFile = state.files.find { it.id == state.activeFileId } ?: state.files.firstOrNull()

  Scaffold(
    modifier = modifier.testTag("studio_code_main_scaffold"),
    snackbarHost = { SnackbarHost(snackbarHostState) },
    topBar = {
      TopBar(
        currentVersion = state.currentVersion,
        onOpenVersionPicker = { viewModel.toggleVersionPicker(true) },
        onCopyHtml = { ctx, fullBundle -> viewModel.copyHtmlToClipboard(ctx, fullBundle) },
        onOpenRunner = {
          if (state.activePanel == ActivePanel.RUNNER) {
            viewModel.setActivePanel(ActivePanel.EXPLORER)
          } else {
            viewModel.setActivePanel(ActivePanel.RUNNER)
          }
        },
        onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) },
        activeFileName = activeFile?.name ?: "index.html"
      )
    },
    bottomBar = {
      StatusBar(
        activeLanguage = activeFile?.extension ?: "html",
        currentVersion = state.currentVersion,
        onOpenRunner = {
          if (state.activePanel == ActivePanel.RUNNER) {
            viewModel.setActivePanel(ActivePanel.EXPLORER)
          } else {
            viewModel.setActivePanel(ActivePanel.RUNNER)
          }
        },
        onOpenVersionPicker = { viewModel.toggleVersionPicker(true) },
        isRunnerActive = state.activePanel == ActivePanel.RUNNER,
        contributedItems = state.contributedStatusBarItems,
        onExecuteCommand = { viewModel.executeCommand(it) },
        autoSaveStatus = state.autoSaveStatus,
        isAutoSaveEnabled = state.isAutoSaveEnabled,
        lastSavedTime = state.lastSavedTime,
        isLivePreviewOpen = state.isLivePreviewOpen,
        onToggleLivePreview = { viewModel.toggleLivePreview() },
        onManualSave = { viewModel.saveActiveFileImmediately() },
        onToggleAutoSave = { viewModel.toggleAutoSave() }
      )
    },
    containerColor = VsCodeBg
  ) { innerPadding ->
    BoxWithConstraints(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      val isWideScreen = maxWidth > 680.dp

      if (isWideScreen) {
        // Desktop / Tablet / Wide Layout: Vertical ActivityBar + Side Panel + Editor
        Row(modifier = Modifier.fillMaxSize()) {
          ActivityBar(
            activePanel = state.activePanel,
            onSelectPanel = { panel ->
              viewModel.setActivePanel(panel)
            },
            installedExtensionCount = state.installedExtensions.size,
            onOpenVersionPicker = { viewModel.toggleVersionPicker(true) },
            isVertical = true,
            consoleErrorCount = state.consoleLogs.count { it.level.equals("ERROR", ignoreCase = true) }
          )

          // Side drawer panel (Explorer, Search, Extensions, Terminal)
          if (state.activePanel != ActivePanel.RUNNER) {
            Box(
              modifier = Modifier
                .width(280.dp)
                .fillMaxHeight()
                .border(1.dp, VsCodeBorder)
            ) {
              when (state.activePanel) {
                ActivePanel.EXPLORER -> {
                  FileExplorerView(
                    folders = state.folders,
                    files = state.files,
                    activeFileId = state.activeFileId,
                    onSelectFile = { viewModel.selectFile(it) },
                    onToggleFolder = { viewModel.toggleFolder(it) },
                    onExpandAllFolders = { viewModel.expandAllFolders() },
                    onCollapseAllFolders = { viewModel.collapseAllFolders() },
                    onCreateFile = { name, parentId -> viewModel.createNewFile(name, parentId) },
                    onCreateFolder = { name, parentId -> viewModel.createNewFolder(name, parentId) },
                    onRenameFile = { id, name -> viewModel.renameFile(id, name) },
                    onRenameFolder = { id, name -> viewModel.renameFolder(id, name) },
                    onDeleteFile = { viewModel.deleteFile(it) },
                    onDeleteFolder = { viewModel.deleteFolder(it) },
                    onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) }
                  )
                }
                ActivePanel.SEARCH -> {
                  SearchView(
                    searchQuery = state.searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    files = state.files,
                    onSelectFile = { viewModel.selectFile(it) }
                  )
                }
                ActivePanel.EXTENSIONS -> {
                  ExtensionsView(
                    installedExtensions = state.installedExtensions,
                    marketplaceExtensions = state.marketplaceExtensions,
                    onInstallExtension = { viewModel.installExtension(it) },
                    onUninstallExtension = { viewModel.uninstallExtension(it) },
                    onToggleEnabled = { viewModel.toggleExtensionEnabled(it) },
                    onOpenAddExtensionDialog = { viewModel.toggleAddExtensionDialog(true) },
                    onOpenImportExtensionDialog = { viewModel.toggleImportExtensionDialog(true) },
                    onApplySnippet = { viewModel.insertSnippet(it) },
                    onExecuteCommand = { viewModel.executeCommand(it) },
                    onReloadExtension = { viewModel.reloadExtension(it) }
                  )
                }
                ActivePanel.TERMINAL -> {
                  TerminalView(
                    terminalLogs = state.terminalLogs,
                    onRunCommand = { viewModel.runTerminalCommand(it) },
                    onClear = { viewModel.clearTerminalLogs() }
                  )
                }
                ActivePanel.CONSOLE -> {
                  ConsolePanel(
                    consoleLogs = state.consoleLogs,
                    onClearConsole = { viewModel.clearConsole() },
                    onOpenRunner = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                    onEvaluateJs = { viewModel.evaluateConsoleJs(it) }
                  )
                }
                else -> {}
              }
            }
          }

          // Center Work Area: Editor (with optional Live HTML Preview split) OR Full Live Web Runner
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
          ) {
            if (state.activePanel == ActivePanel.RUNNER) {
              LiveWebRunnerView(
                bundledHtml = state.livePreviewHtml.ifEmpty { viewModel.generateStandaloneHtml() },
                reloadTrigger = state.runnerReloadTrigger,
                previewDevice = state.previewDevice,
                onSelectPreviewDevice = { viewModel.setPreviewDevice(it) },
                onReload = { viewModel.reloadLivePreview() },
                onCopyHtml = { ctx, fullBundle -> viewModel.copyHtmlToClipboard(ctx, fullBundle) },
                consoleLogs = state.consoleLogs,
                onAddConsoleLog = { lvl, msg -> viewModel.addConsoleLog(lvl, msg) },
                onClearConsole = { viewModel.clearConsole() }
              )
            } else {
              if (state.isLivePreviewOpen) {
                if (state.previewSplitOrientation == PreviewSplitOrientation.HORIZONTAL) {
                  // Side-by-side: Editor on left, Live HTML preview on right
                  Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                    ) {
                      CodeEditorView(
                        file = activeFile,
                        allFiles = state.files,
                        openTabIds = state.openTabIds,
                        onFileSelect = { viewModel.selectFile(it) },
                        onCloseTab = { viewModel.closeTab(it) },
                        onContentChange = { viewModel.updateActiveFileContent(it) },
                        onCopyHtml = { fullBundle -> viewModel.copyHtmlToClipboard(context, fullBundle) },
                        onRunWeb = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                        onFormat = { viewModel.formatCurrentCode() },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onOpenExtensions = { viewModel.setActivePanel(ActivePanel.EXTENSIONS) },
                        onToggleSidebar = {
                          if (state.activePanel == ActivePanel.EXPLORER) {
                            viewModel.setActivePanel(ActivePanel.RUNNER)
                          } else {
                            viewModel.setActivePanel(ActivePanel.EXPLORER)
                          }
                        },
                        contributedButtons = state.contributedUiButtons,
                        onExecuteCommand = { viewModel.executeCommand(it) },
                        onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                        onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) },
                        isFindReplaceOpen = state.isFindReplaceOpen,
                        findQuery = state.findQuery,
                        replaceQuery = state.replaceQuery,
                        matchCase = state.matchCase,
                        matchWholeWord = state.matchWholeWord,
                        useRegex = state.useRegex,
                        currentMatchIndex = state.currentMatchIndex,
                        totalMatches = state.totalMatches,
                        onToggleFindReplace = { viewModel.toggleFindReplace() },
                        onFindQueryChange = { viewModel.setFindQuery(it) },
                        onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                        onToggleMatchCase = { viewModel.toggleMatchCase() },
                        onToggleMatchWholeWord = { viewModel.toggleMatchWholeWord() },
                        onToggleUseRegex = { viewModel.toggleUseRegex() },
                        onFindNext = { viewModel.findNextMatch() },
                        onFindPrevious = { viewModel.findPreviousMatch() },
                        onReplaceCurrent = { viewModel.replaceCurrentMatch() },
                        onReplaceAll = { viewModel.replaceAllMatches() },
                        isLivePreviewOpen = state.isLivePreviewOpen,
                        onToggleLivePreview = { viewModel.toggleLivePreview() }
                      )
                    }

                    // Vertical Divider
                    Box(
                      modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(VsCodeBorder)
                    )

                    // Live HTML Preview Pane
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                    ) {
                      LiveHtmlPreviewPane(
                        bundledHtml = state.livePreviewHtml.ifEmpty { viewModel.generateStandaloneHtml() },
                        isUpdating = state.isLivePreviewUpdating,
                        previewDevice = state.previewDevice,
                        splitOrientation = state.previewSplitOrientation,
                        onSelectPreviewDevice = { viewModel.setPreviewDevice(it) },
                        onToggleOrientation = { viewModel.togglePreviewOrientation() },
                        onReload = { viewModel.reloadLivePreview() },
                        onOpenFullRunner = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                        onCopyHtml = { ctx, bundle -> viewModel.copyHtmlToClipboard(ctx, bundle) },
                        onClosePreview = { viewModel.toggleLivePreview(false) },
                        onAddConsoleLog = { lvl, msg -> viewModel.addConsoleLog(lvl, msg) }
                      )
                    }
                  }
                } else {
                  // Top & bottom split
                  Column(modifier = Modifier.fillMaxSize()) {
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                    ) {
                      CodeEditorView(
                        file = activeFile,
                        allFiles = state.files,
                        openTabIds = state.openTabIds,
                        onFileSelect = { viewModel.selectFile(it) },
                        onCloseTab = { viewModel.closeTab(it) },
                        onContentChange = { viewModel.updateActiveFileContent(it) },
                        onCopyHtml = { fullBundle -> viewModel.copyHtmlToClipboard(context, fullBundle) },
                        onRunWeb = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                        onFormat = { viewModel.formatCurrentCode() },
                        onUndo = { viewModel.undo() },
                        onRedo = { viewModel.redo() },
                        onOpenExtensions = { viewModel.setActivePanel(ActivePanel.EXTENSIONS) },
                        onToggleSidebar = {
                          if (state.activePanel == ActivePanel.EXPLORER) {
                            viewModel.setActivePanel(ActivePanel.RUNNER)
                          } else {
                            viewModel.setActivePanel(ActivePanel.EXPLORER)
                          }
                        },
                        contributedButtons = state.contributedUiButtons,
                        onExecuteCommand = { viewModel.executeCommand(it) },
                        onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                        onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) },
                        isFindReplaceOpen = state.isFindReplaceOpen,
                        findQuery = state.findQuery,
                        replaceQuery = state.replaceQuery,
                        matchCase = state.matchCase,
                        matchWholeWord = state.matchWholeWord,
                        useRegex = state.useRegex,
                        currentMatchIndex = state.currentMatchIndex,
                        totalMatches = state.totalMatches,
                        onToggleFindReplace = { viewModel.toggleFindReplace() },
                        onFindQueryChange = { viewModel.setFindQuery(it) },
                        onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                        onToggleMatchCase = { viewModel.toggleMatchCase() },
                        onToggleMatchWholeWord = { viewModel.toggleMatchWholeWord() },
                        onToggleUseRegex = { viewModel.toggleUseRegex() },
                        onFindNext = { viewModel.findNextMatch() },
                        onFindPrevious = { viewModel.findPreviousMatch() },
                        onReplaceCurrent = { viewModel.replaceCurrentMatch() },
                        onReplaceAll = { viewModel.replaceAllMatches() },
                        isLivePreviewOpen = state.isLivePreviewOpen,
                        onToggleLivePreview = { viewModel.toggleLivePreview() }
                      )
                    }

                    // Horizontal Divider
                    Box(
                      modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(VsCodeBorder)
                    )

                    // Live HTML Preview Pane
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                    ) {
                      LiveHtmlPreviewPane(
                        bundledHtml = state.livePreviewHtml.ifEmpty { viewModel.generateStandaloneHtml() },
                        isUpdating = state.isLivePreviewUpdating,
                        previewDevice = state.previewDevice,
                        splitOrientation = state.previewSplitOrientation,
                        onSelectPreviewDevice = { viewModel.setPreviewDevice(it) },
                        onToggleOrientation = { viewModel.togglePreviewOrientation() },
                        onReload = { viewModel.reloadLivePreview() },
                        onOpenFullRunner = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                        onCopyHtml = { ctx, bundle -> viewModel.copyHtmlToClipboard(ctx, bundle) },
                        onClosePreview = { viewModel.toggleLivePreview(false) },
                        onAddConsoleLog = { lvl, msg -> viewModel.addConsoleLog(lvl, msg) }
                      )
                    }
                  }
                }
              } else {
                CodeEditorView(
                  file = activeFile,
                  allFiles = state.files,
                  openTabIds = state.openTabIds,
                  onFileSelect = { viewModel.selectFile(it) },
                  onCloseTab = { viewModel.closeTab(it) },
                  onContentChange = { viewModel.updateActiveFileContent(it) },
                  onCopyHtml = { fullBundle -> viewModel.copyHtmlToClipboard(context, fullBundle) },
                  onRunWeb = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                  onFormat = { viewModel.formatCurrentCode() },
                  onUndo = { viewModel.undo() },
                  onRedo = { viewModel.redo() },
                  onOpenExtensions = { viewModel.setActivePanel(ActivePanel.EXTENSIONS) },
                  onToggleSidebar = {
                    if (state.activePanel == ActivePanel.EXPLORER) {
                      viewModel.setActivePanel(ActivePanel.RUNNER)
                    } else {
                      viewModel.setActivePanel(ActivePanel.EXPLORER)
                    }
                  },
                  contributedButtons = state.contributedUiButtons,
                  onExecuteCommand = { viewModel.executeCommand(it) },
                  onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                  onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) },
                  isFindReplaceOpen = state.isFindReplaceOpen,
                  findQuery = state.findQuery,
                  replaceQuery = state.replaceQuery,
                  matchCase = state.matchCase,
                  matchWholeWord = state.matchWholeWord,
                  useRegex = state.useRegex,
                  currentMatchIndex = state.currentMatchIndex,
                  totalMatches = state.totalMatches,
                  onToggleFindReplace = { viewModel.toggleFindReplace() },
                  onFindQueryChange = { viewModel.setFindQuery(it) },
                  onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                  onToggleMatchCase = { viewModel.toggleMatchCase() },
                  onToggleMatchWholeWord = { viewModel.toggleMatchWholeWord() },
                  onToggleUseRegex = { viewModel.toggleUseRegex() },
                  onFindNext = { viewModel.findNextMatch() },
                  onFindPrevious = { viewModel.findPreviousMatch() },
                  onReplaceCurrent = { viewModel.replaceCurrentMatch() },
                  onReplaceAll = { viewModel.replaceAllMatches() },
                  isLivePreviewOpen = false,
                  onToggleLivePreview = { viewModel.toggleLivePreview() }
                )
              }
            }
          }
        }
      } else {
        // Mobile Layout: Bottom navigation for ActivityBar, full screen active panel
        Column(modifier = Modifier.fillMaxSize()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
          ) {
            when (state.activePanel) {
              ActivePanel.EXPLORER -> {
                if (showMobileExplorer) {
                  FileExplorerView(
                    folders = state.folders,
                    files = state.files,
                    activeFileId = state.activeFileId,
                    onSelectFile = {
                      viewModel.selectFile(it)
                      showMobileExplorer = false
                    },
                    onToggleFolder = { viewModel.toggleFolder(it) },
                    onExpandAllFolders = { viewModel.expandAllFolders() },
                    onCollapseAllFolders = { viewModel.collapseAllFolders() },
                    onCreateFile = { name, parentId -> viewModel.createNewFile(name, parentId) },
                    onCreateFolder = { name, parentId -> viewModel.createNewFolder(name, parentId) },
                    onRenameFile = { id, name -> viewModel.renameFile(id, name) },
                    onRenameFolder = { id, name -> viewModel.renameFolder(id, name) },
                    onDeleteFile = { viewModel.deleteFile(it) },
                    onDeleteFolder = { viewModel.deleteFolder(it) },
                    onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) }
                  )
                } else {
                  if (state.isLivePreviewOpen) {
                    // Mobile Split View: Editor in top half, Live HTML Preview in bottom half!
                    Column(modifier = Modifier.fillMaxSize()) {
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .fillMaxWidth()
                      ) {
                        CodeEditorView(
                          file = activeFile,
                          allFiles = state.files,
                          openTabIds = state.openTabIds,
                          onFileSelect = { viewModel.selectFile(it) },
                          onCloseTab = { viewModel.closeTab(it) },
                          onContentChange = { viewModel.updateActiveFileContent(it) },
                          onCopyHtml = { fullBundle -> viewModel.copyHtmlToClipboard(context, fullBundle) },
                          onRunWeb = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                          onFormat = { viewModel.formatCurrentCode() },
                          onUndo = { viewModel.undo() },
                          onRedo = { viewModel.redo() },
                          onOpenExtensions = { viewModel.setActivePanel(ActivePanel.EXTENSIONS) },
                          onToggleSidebar = { showMobileExplorer = !showMobileExplorer },
                          contributedButtons = state.contributedUiButtons,
                          onExecuteCommand = { viewModel.executeCommand(it) },
                          onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                          onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) },
                          isFindReplaceOpen = state.isFindReplaceOpen,
                          findQuery = state.findQuery,
                          replaceQuery = state.replaceQuery,
                          matchCase = state.matchCase,
                          matchWholeWord = state.matchWholeWord,
                          useRegex = state.useRegex,
                          currentMatchIndex = state.currentMatchIndex,
                          totalMatches = state.totalMatches,
                          onToggleFindReplace = { viewModel.toggleFindReplace() },
                          onFindQueryChange = { viewModel.setFindQuery(it) },
                          onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                          onToggleMatchCase = { viewModel.toggleMatchCase() },
                          onToggleMatchWholeWord = { viewModel.toggleMatchWholeWord() },
                          onToggleUseRegex = { viewModel.toggleUseRegex() },
                          onFindNext = { viewModel.findNextMatch() },
                          onFindPrevious = { viewModel.findPreviousMatch() },
                          onReplaceCurrent = { viewModel.replaceCurrentMatch() },
                          onReplaceAll = { viewModel.replaceAllMatches() },
                          isLivePreviewOpen = state.isLivePreviewOpen,
                          onToggleLivePreview = { viewModel.toggleLivePreview() }
                        )
                      }

                      // Divider
                      Box(
                        modifier = Modifier
                          .height(1.dp)
                          .fillMaxWidth()
                          .background(VsCodeBorder)
                      )

                      // Mobile Live Preview Pane
                      Box(
                        modifier = Modifier
                          .weight(1f)
                          .fillMaxWidth()
                      ) {
                        LiveHtmlPreviewPane(
                          bundledHtml = state.livePreviewHtml.ifEmpty { viewModel.generateStandaloneHtml() },
                          isUpdating = state.isLivePreviewUpdating,
                          previewDevice = state.previewDevice,
                          splitOrientation = state.previewSplitOrientation,
                          onSelectPreviewDevice = { viewModel.setPreviewDevice(it) },
                          onToggleOrientation = { viewModel.togglePreviewOrientation() },
                          onReload = { viewModel.reloadLivePreview() },
                          onOpenFullRunner = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                          onCopyHtml = { ctx, bundle -> viewModel.copyHtmlToClipboard(ctx, bundle) },
                          onClosePreview = { viewModel.toggleLivePreview(false) },
                          onAddConsoleLog = { lvl, msg -> viewModel.addConsoleLog(lvl, msg) }
                        )
                      }
                    }
                  } else {
                    CodeEditorView(
                      file = activeFile,
                      allFiles = state.files,
                      openTabIds = state.openTabIds,
                      onFileSelect = { viewModel.selectFile(it) },
                      onCloseTab = { viewModel.closeTab(it) },
                      onContentChange = { viewModel.updateActiveFileContent(it) },
                      onCopyHtml = { fullBundle -> viewModel.copyHtmlToClipboard(context, fullBundle) },
                      onRunWeb = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                      onFormat = { viewModel.formatCurrentCode() },
                      onUndo = { viewModel.undo() },
                      onRedo = { viewModel.redo() },
                      onOpenExtensions = { viewModel.setActivePanel(ActivePanel.EXTENSIONS) },
                      onToggleSidebar = { showMobileExplorer = !showMobileExplorer },
                      contributedButtons = state.contributedUiButtons,
                      onExecuteCommand = { viewModel.executeCommand(it) },
                      onOpenCommandPalette = { viewModel.toggleCommandPalette(true) },
                      onOpenGlobalFileSearch = { viewModel.toggleGlobalFileSearch(true) },
                      isFindReplaceOpen = state.isFindReplaceOpen,
                      findQuery = state.findQuery,
                      replaceQuery = state.replaceQuery,
                      matchCase = state.matchCase,
                      matchWholeWord = state.matchWholeWord,
                      useRegex = state.useRegex,
                      currentMatchIndex = state.currentMatchIndex,
                      totalMatches = state.totalMatches,
                      onToggleFindReplace = { viewModel.toggleFindReplace() },
                      onFindQueryChange = { viewModel.setFindQuery(it) },
                      onReplaceQueryChange = { viewModel.setReplaceQuery(it) },
                      onToggleMatchCase = { viewModel.toggleMatchCase() },
                      onToggleMatchWholeWord = { viewModel.toggleMatchWholeWord() },
                      onToggleUseRegex = { viewModel.toggleUseRegex() },
                      onFindNext = { viewModel.findNextMatch() },
                      onFindPrevious = { viewModel.findPreviousMatch() },
                      onReplaceCurrent = { viewModel.replaceCurrentMatch() },
                      onReplaceAll = { viewModel.replaceAllMatches() },
                      isLivePreviewOpen = false,
                      onToggleLivePreview = { viewModel.toggleLivePreview() }
                    )
                  }
                }
              }
              ActivePanel.SEARCH -> {
                SearchView(
                  searchQuery = state.searchQuery,
                  onSearchQueryChange = { viewModel.setSearchQuery(it) },
                  files = state.files,
                  onSelectFile = {
                    viewModel.selectFile(it)
                    viewModel.setActivePanel(ActivePanel.EXPLORER)
                  }
                )
              }
              ActivePanel.EXTENSIONS -> {
                ExtensionsView(
                  installedExtensions = state.installedExtensions,
                  marketplaceExtensions = state.marketplaceExtensions,
                  onInstallExtension = { viewModel.installExtension(it) },
                  onUninstallExtension = { viewModel.uninstallExtension(it) },
                  onToggleEnabled = { viewModel.toggleExtensionEnabled(it) },
                  onOpenAddExtensionDialog = { viewModel.toggleAddExtensionDialog(true) },
                  onOpenImportExtensionDialog = { viewModel.toggleImportExtensionDialog(true) },
                  onApplySnippet = {
                    viewModel.insertSnippet(it)
                    viewModel.setActivePanel(ActivePanel.EXPLORER)
                  },
                  onExecuteCommand = { viewModel.executeCommand(it) },
                  onReloadExtension = { viewModel.reloadExtension(it) }
                )
              }
              ActivePanel.RUNNER -> {
                LiveWebRunnerView(
                  bundledHtml = viewModel.generateStandaloneHtml(),
                  reloadTrigger = state.runnerReloadTrigger,
                  previewDevice = state.previewDevice,
                  onSelectPreviewDevice = { viewModel.setPreviewDevice(it) },
                  onReload = { viewModel.reloadWebRunner() },
                  onCopyHtml = { ctx, fullBundle -> viewModel.copyHtmlToClipboard(ctx, fullBundle) },
                  consoleLogs = state.consoleLogs,
                  onAddConsoleLog = { lvl, msg -> viewModel.addConsoleLog(lvl, msg) },
                  onClearConsole = { viewModel.clearConsole() }
                )
              }
              ActivePanel.TERMINAL -> {
                TerminalView(
                  terminalLogs = state.terminalLogs,
                  onRunCommand = { viewModel.runTerminalCommand(it) },
                  onClear = { viewModel.clearTerminalLogs() }
                )
              }
              ActivePanel.CONSOLE -> {
                ConsolePanel(
                  consoleLogs = state.consoleLogs,
                  onClearConsole = { viewModel.clearConsole() },
                  onOpenRunner = { viewModel.setActivePanel(ActivePanel.RUNNER) },
                  onEvaluateJs = { viewModel.evaluateConsoleJs(it) }
                )
              }
              else -> {}
            }
          }

          // Horizontal ActivityBar at bottom on compact screens
          ActivityBar(
            activePanel = state.activePanel,
            onSelectPanel = { panel ->
              if (panel == ActivePanel.EXPLORER) {
                if (state.activePanel == ActivePanel.EXPLORER) {
                  showMobileExplorer = !showMobileExplorer
                } else {
                  showMobileExplorer = true
                  viewModel.setActivePanel(panel)
                }
              } else {
                viewModel.setActivePanel(panel)
              }
            },
            installedExtensionCount = state.installedExtensions.size,
            onOpenVersionPicker = { viewModel.toggleVersionPicker(true) },
            isVertical = false,
            consoleErrorCount = state.consoleLogs.count { it.level.equals("ERROR", ignoreCase = true) }
          )
        }
      }
    }

    // Modal dialogs
    if (state.showImportExtensionDialog) {
      ImportExtensionDialog(
        onImportContent = { raw, filename ->
          viewModel.importExtensionApp(raw, filename)
        },
        onDismiss = { viewModel.toggleImportExtensionDialog(false) }
      )
    }

    if (state.showAddExtensionDialog) {
      AddExtensionDialog(
        onAddExtension = { name, author, desc, cat, snippet, jsCode ->
          viewModel.addCustomExtension(name, author, desc, cat, snippet, jsCode)
        },
        onDismiss = { viewModel.toggleAddExtensionDialog(false) }
      )
    }

    if (state.showGlobalFileSearch) {
      GlobalFileSearchOverlay(
        files = state.files,
        folders = state.folders,
        activeFileId = state.activeFileId,
        openTabIds = state.openTabIds,
        recentFileIds = state.recentFileIds,
        onOpenFile = { fileId ->
          viewModel.openFileFromGlobalSearch(fileId)
          showMobileExplorer = false
        },
        onCreateAndOpenFile = { fileName ->
          viewModel.createAndOpenFileFromSearch(fileName)
          showMobileExplorer = false
        },
        onDismiss = { viewModel.toggleGlobalFileSearch(false) }
      )
    }

    if (state.showCommandPalette) {
      CommandPaletteDialog(
        registeredCommands = state.registeredExtensionCommands,
        onExecuteCommand = { viewModel.executeCommand(it) },
        onDismiss = { viewModel.toggleCommandPalette(false) }
      )
    }

    if (state.showVersionPicker) {
      VersionChoiceDialog(
        currentVersion = state.currentVersion,
        onSelectVersion = { ver ->
          viewModel.setVersionChoice(ver)
        },
        onDismiss = { viewModel.toggleVersionPicker(false) }
      )
    }
  }
}
