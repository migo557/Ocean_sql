package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import com.example.models.CodeFile
import com.example.models.ContributedButton
import com.example.syntax.SyntaxHighlighter
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeButtonSecondary
import com.example.ui.theme.VsCodeCodeText
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeEmerald
import com.example.ui.theme.VsCodeGutter
import com.example.ui.theme.VsCodeInactiveTabBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeLavenderDark
import com.example.ui.theme.VsCodeLavenderPressed
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTabsBg
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun CodeEditorView(
  file: CodeFile?,
  allFiles: List<CodeFile>,
  openTabIds: List<String>,
  onFileSelect: (String) -> Unit,
  onCloseTab: (String) -> Unit,
  onContentChange: (String) -> Unit,
  onCopyHtml: (bundleAll: Boolean) -> Unit,
  onRunWeb: () -> Unit,
  onFormat: () -> Unit,
  onUndo: () -> Unit,
  onRedo: () -> Unit,
  onOpenExtensions: () -> Unit,
  onToggleSidebar: () -> Unit,
  contributedButtons: List<ContributedButton> = emptyList(),
  onExecuteCommand: (String) -> Unit = {},
  onOpenCommandPalette: () -> Unit = {},
  onOpenGlobalFileSearch: () -> Unit = {},
  isFindReplaceOpen: Boolean = false,
  findQuery: String = "",
  replaceQuery: String = "",
  matchCase: Boolean = false,
  matchWholeWord: Boolean = false,
  useRegex: Boolean = false,
  currentMatchIndex: Int = 0,
  totalMatches: Int = 0,
  onToggleFindReplace: () -> Unit = {},
  onFindQueryChange: (String) -> Unit = {},
  onReplaceQueryChange: (String) -> Unit = {},
  onToggleMatchCase: () -> Unit = {},
  onToggleMatchWholeWord: () -> Unit = {},
  onToggleUseRegex: () -> Unit = {},
  onFindNext: () -> Unit = {},
  onFindPrevious: () -> Unit = {},
  onReplaceCurrent: () -> Unit = {},
  onReplaceAll: () -> Unit = {},
  isLivePreviewOpen: Boolean = false,
  onToggleLivePreview: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showCopyMenu by remember { mutableStateOf(false) }
  val tabsScrollState = rememberScrollState()
  val editorScrollState = rememberScrollState()
  val horizontalCodeScrollState = rememberScrollState()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(VsCodeEditorBg)
  ) {
    // 1. Top Editor Action Bar & Tabs Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(40.dp)
        .background(VsCodeTabsBg)
        .border(1.dp, VsCodeBorder),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Sidebar toggle button
      IconButton(
        onClick = onToggleSidebar,
        modifier = Modifier
          .size(36.dp)
          .testTag("editor_toggle_sidebar_btn")
      ) {
        Icon(
          imageVector = Icons.Default.FolderOpen,
          contentDescription = "Toggle Explorer",
          tint = VsCodeLavender,
          modifier = Modifier.size(18.dp)
        )
      }

      // Horizontal Tabs
      Row(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
          .horizontalScroll(tabsScrollState),
        verticalAlignment = Alignment.CenterVertically
      ) {
        openTabIds.forEach { tabId ->
          val tabFile = allFiles.find { it.id == tabId }
          if (tabFile != null) {
            val isActive = tabFile.id == file?.id
            EditorTabItem(
              file = tabFile,
              isActive = isActive,
              onClick = { onFileSelect(tabFile.id) },
              onClose = { onCloseTab(tabFile.id) }
            )
          }
        }
      }

      // Right Quick Actions: Format, Undo, Redo
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(end = 8.dp)
      ) {
        // Undo / Redo
        IconButton(
          onClick = onUndo,
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_undo_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Undo,
            contentDescription = "Undo",
            tint = VsCodeTextSecondary,
            modifier = Modifier.size(15.dp)
          )
        }
        IconButton(
          onClick = onRedo,
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_redo_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Redo,
            contentDescription = "Redo",
            tint = VsCodeTextSecondary,
            modifier = Modifier.size(15.dp)
          )
        }

        // Find & Replace
        IconButton(
          onClick = onToggleFindReplace,
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_find_replace_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Find and Replace",
            tint = if (isFindReplaceOpen) VsCodeLavender else VsCodeTextSecondary,
            modifier = Modifier.size(16.dp)
          )
        }

        // Quick Open File Search
        IconButton(
          onClick = onOpenGlobalFileSearch,
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_quick_file_search_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Quick Open File (Ctrl+P)",
            tint = VsCodeCyan,
            modifier = Modifier.size(16.dp)
          )
        }

        // Command Palette
        IconButton(
          onClick = onOpenCommandPalette,
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_command_palette_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Terminal,
            contentDescription = "Command Palette",
            tint = VsCodeLavender,
            modifier = Modifier.size(16.dp)
          )
        }

        // Format
        IconButton(
          onClick = onFormat,
          modifier = Modifier
            .size(28.dp)
            .testTag("editor_format_btn")
        ) {
          Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = "Format code",
            tint = VsCodeLavender,
            modifier = Modifier.size(16.dp)
          )
        }

        // Live Preview Toggle Button
        Button(
          onClick = onToggleLivePreview,
          colors = ButtonDefaults.buttonColors(
            containerColor = if (isLivePreviewOpen) VsCodeCyan else Color(0xFF262A36),
            contentColor = if (isLivePreviewOpen) Color(0xFF0F172A) else VsCodeCyan
          ),
          shape = RoundedCornerShape(8.dp),
          contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
          modifier = Modifier
            .height(28.dp)
            .testTag("editor_live_preview_toggle_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Bolt,
            contentDescription = "Toggle Live Preview",
            tint = if (isLivePreviewOpen) Color(0xFF0F172A) else VsCodeCyan,
            modifier = Modifier.size(13.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = if (isLivePreviewOpen) "Preview: ON" else "Live Preview",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }

        // Header copy button with dropdown
        Box {
          Button(
            onClick = { showCopyMenu = true },
            colors = ButtonDefaults.buttonColors(
              containerColor = Color(0xFF2D2D2D),
              contentColor = VsCodeLavender
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
            modifier = Modifier
              .height(28.dp)
              .testTag("editor_copy_html_btn")
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy HTML",
              tint = VsCodeLavender,
              modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "Copy HTML",
              color = VsCodeLavender,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          DropdownMenu(
            expanded = showCopyMenu,
            onDismissRequest = { showCopyMenu = false },
            modifier = Modifier
              .background(VsCodeTabsBg)
              .border(1.dp, VsCodeBorder)
          ) {
            DropdownMenuItem(
              text = {
                Column {
                  Text(
                    text = "Copy Bundled Standalone HTML",
                    color = VsCodeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Inlines style.css & script.js into single HTML",
                    color = VsCodeTextMuted,
                    fontSize = 10.sp
                  )
                }
              },
              onClick = {
                showCopyMenu = false
                onCopyHtml(true)
              }
            )
            DropdownMenuItem(
              text = {
                Column {
                  Text(
                    text = "Copy Raw active file",
                    color = VsCodeTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                  Text(
                    text = "Copies current file contents",
                    color = VsCodeTextMuted,
                    fontSize = 10.sp
                  )
                }
              },
              onClick = {
                showCopyMenu = false
                onCopyHtml(false)
              }
            )
          }
        }
      }
    }

    // 2. Breadcrumbs path
    if (file != null) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(Color(0xFF141414))
          .border(1.dp, VsCodeBorder)
          .padding(horizontal = 14.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "studio-code > src > ${file.name}",
          color = VsCodeTextMuted,
          fontSize = 11.sp,
          fontFamily = FontFamily.Monospace,
          modifier = Modifier.weight(1f)
        )
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(Color(file.iconColorHex).copy(alpha = 0.15f))
            .border(0.5.dp, Color(file.iconColorHex).copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("language_mode_badge")
        ) {
          Text(
            text = file.language.uppercase(),
            color = Color(file.iconColorHex),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
          )
        }
      }

      // Contributed Extension Ribbon
      if (contributedButtons.isNotEmpty()) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1B1824))
            .border(1.dp, VsCodeBorder)
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag("extension_contributed_ribbon"),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Extension,
            contentDescription = "Extension Tools",
            tint = VsCodeLavender,
            modifier = Modifier.size(13.dp)
          )
          Text(
            text = "EXT TOOLS:",
            color = VsCodeLavender,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
          contributedButtons.forEach { btn ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(VsCodeLavender.copy(alpha = 0.15f))
                .border(0.5.dp, VsCodeLavender.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .clickable { onExecuteCommand(btn.command) }
                .padding(horizontal = 8.dp, vertical = 3.dp)
                .testTag("contributed_btn_${btn.id}")
            ) {
              Text(
                text = btn.label,
                color = VsCodeLavender,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // 3. Editor Canvas with Line Numbers and Floating Bottom-End Actions
    if (file != null) {
      val content = file.content
      val lineCount = remember(content) { content.lines().size.coerceAtLeast(1) }

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(VsCodeEditorBg)
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(editorScrollState)
        ) {
          // Gutter: Line numbers column (w-11, bg-[#1E1E1E], text-[#858585])
          Column(
            modifier = Modifier
              .width(44.dp)
              .background(VsCodeEditorBg)
              .padding(top = 12.dp, bottom = 12.dp, end = 12.dp),
            horizontalAlignment = Alignment.End
          ) {
            for (i in 1..lineCount) {
              Text(
                text = "$i",
                color = VsCodeGutter,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 20.sp
              )
            }
          }

          Spacer(modifier = Modifier.width(6.dp))

          // Code text input with syntax visual transformation
          Box(
            modifier = Modifier
              .weight(1f)
              .padding(top = 12.dp, bottom = 100.dp, end = 16.dp)
              .horizontalScroll(horizontalCodeScrollState)
          ) {
            BasicTextField(
              value = file.content,
              onValueChange = onContentChange,
              textStyle = TextStyle(
                color = VsCodeCodeText,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 20.sp
              ),
              cursorBrush = SolidColor(VsCodeLavender),
              visualTransformation = VisualTransformation { text ->
                val annotated = SyntaxHighlighter.highlight(
                  code = text.text,
                  language = file.language,
                  findQuery = if (isFindReplaceOpen) findQuery else "",
                  matchCase = matchCase,
                  matchWholeWord = matchWholeWord,
                  useRegex = useRegex,
                  currentMatchIndex = currentMatchIndex
                )
                TransformedText(annotated, OffsetMapping.Identity)
              },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("code_editor_text_field")
            )
          }
        }

        // Floating Find & Replace Toolbar (VS Code Style)
        if (isFindReplaceOpen) {
          EditorFindReplaceBar(
            findQuery = findQuery,
            replaceQuery = replaceQuery,
            matchCase = matchCase,
            matchWholeWord = matchWholeWord,
            useRegex = useRegex,
            currentMatchIndex = currentMatchIndex,
            totalMatches = totalMatches,
            onFindQueryChange = onFindQueryChange,
            onReplaceQueryChange = onReplaceQueryChange,
            onToggleMatchCase = onToggleMatchCase,
            onToggleMatchWholeWord = onToggleMatchWholeWord,
            onToggleUseRegex = onToggleUseRegex,
            onFindNext = onFindNext,
            onFindPrevious = onFindPrevious,
            onReplaceCurrent = onReplaceCurrent,
            onReplaceAll = onReplaceAll,
            onClose = onToggleFindReplace,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(top = 8.dp, end = 16.dp)
          )
        }

        // Floating Action Buttons (from Sophisticated Dark Design HTML)
        Column(
          modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp, bottom = 16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          horizontalAlignment = Alignment.End
        ) {
          // Floating Live Preview quick toggle button
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isLivePreviewOpen) VsCodeCyan else VsCodeButtonSecondary)
              .clickable(onClick = onToggleLivePreview)
              .testTag("floating_live_preview_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Bolt,
              contentDescription = "Toggle Live Preview",
              tint = if (isLivePreviewOpen) Color(0xFF0F172A) else Color.White,
              modifier = Modifier.size(22.dp)
            )
          }

          // Secondary floating round button (48x48 rounded-2xl bg-[#333333])
          Box(
            modifier = Modifier
              .size(48.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(VsCodeButtonSecondary)
              .clickable(onClick = onRunWeb)
              .testTag("floating_run_btn"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.PlayArrow,
              contentDescription = "Run Web",
              tint = Color.White,
              modifier = Modifier.size(24.dp)
            )
          }

          // Primary floating button: Copy HTML (h-14 px-6 rounded-2xl bg-[#D0BCFF] text-[#381E72])
          Row(
            modifier = Modifier
              .height(52.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(VsCodeLavender)
              .clickable { onCopyHtml(true) }
              .padding(horizontal = 20.dp)
              .testTag("floating_copy_html_btn"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              imageVector = Icons.Default.ContentCopy,
              contentDescription = "Copy HTML",
              tint = VsCodeLavenderDark,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "Copy HTML",
              color = VsCodeLavenderDark,
              fontSize = 14.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }

      // 4. Quick Accessory Toolbar: Keyboard symbols and Snippets
      QuickAccessoryBar(
        language = file.language,
        onInsertSymbol = { symbol ->
          onContentChange(file.content + symbol)
        },
        onInsertSnippet = { snippet ->
          onContentChange(file.content + "\n" + snippet)
        },
        onOpenExtensions = onOpenExtensions
      )
    } else {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(VsCodeEditorBg),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "No file selected. Open a file from the Explorer.",
          color = VsCodeTextMuted
        )
      }
    }
  }
}

@Composable
fun EditorTabItem(
  file: CodeFile,
  isActive: Boolean,
  onClick: () -> Unit,
  onClose: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxHeight()
      .background(if (isActive) VsCodeActiveTabBg else VsCodeInactiveTabBg)
      .border(
        width = 1.dp,
        color = if (isActive) VsCodeBorder else Color.Transparent
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp)
  ) {
    if (file.isModified) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(Color(0xFFF59E0B))
      )
    } else if (isActive) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(VsCodeLavender)
      )
    }

    Text(
      text = file.name,
      color = if (isActive) VsCodeLavender else VsCodeTextSecondary,
      fontSize = 12.sp,
      fontFamily = FontFamily.Monospace,
      fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
    )

    // Close button
    Icon(
      imageVector = Icons.Default.Close,
      contentDescription = "Close ${file.name}",
      tint = VsCodeTextMuted,
      modifier = Modifier
        .size(14.dp)
        .clickable(onClick = onClose)
    )
  }
}

@Composable
fun QuickAccessoryBar(
  language: String,
  onInsertSymbol: (String) -> Unit,
  onInsertSnippet: (String) -> Unit,
  onOpenExtensions: () -> Unit
) {
  val symbols = listOf("<", ">", "</", ">", "{", "}", "(", ")", "=", "\"", "'", ";", ":", "/", ".", "!")
  val scrollState = rememberScrollState()

  Column(
    modifier = Modifier
      .fillMaxWidth()
      .background(VsCodeSidebarBg)
      .border(1.dp, VsCodeBorder)
      .padding(vertical = 5.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(scrollState)
        .padding(horizontal = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      // Snippets according to language
      when (language) {
        "html" -> {
          SnippetButton("<div>") { onInsertSnippet("<div class=\"box\">\n  \n</div>") }
          SnippetButton("<button>") { onInsertSnippet("<button class=\"btn\">Click Me</button>") }
          SnippetButton("<span>") { onInsertSymbol("<span></span>") }
          SnippetButton("Tailwind CDN") { onInsertSnippet("<script src=\"https://cdn.tailwindcss.com\"></script>") }
        }
        "css" -> {
          SnippetButton("display: flex") { onInsertSnippet("display: flex;\nalign-items: center;\njustify-content: center;") }
          SnippetButton("border-radius") { onInsertSnippet("border-radius: 8px;") }
          SnippetButton("gradient") { onInsertSnippet("background: linear-gradient(135deg, #d0bcff, #381e72);") }
        }
        "javascript" -> {
          SnippetButton("console.log") { onInsertSnippet("console.log('');") }
          SnippetButton("addEventListener") { onInsertSnippet("element.addEventListener('click', (e) => {\n  \n});") }
          SnippetButton("fetch()") { onInsertSnippet("fetch('url').then(res => res.json()).then(data => console.log(data));") }
        }
      }

      // Extension quick add button
      Button(
        onClick = onOpenExtensions,
        colors = ButtonDefaults.buttonColors(containerColor = VsCodeSurfaceDark),
        shape = RoundedCornerShape(8.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(28.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Extension,
          contentDescription = null,
          tint = VsCodeLavender,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text("+ Extension", color = VsCodeLavender, fontSize = 10.sp)
      }

      // Symbols
      symbols.forEach { sym ->
        Box(
          modifier = Modifier
            .size(width = 28.dp, height = 28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF222222))
            .clickable { onInsertSymbol(sym) },
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = sym,
            color = VsCodeTextPrimary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
  }
}

@Composable
fun SnippetButton(label: String, onClick: () -> Unit) {
  Button(
    onClick = onClick,
    colors = ButtonDefaults.buttonColors(containerColor = VsCodeSurfaceDark),
    shape = RoundedCornerShape(8.dp),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
    modifier = Modifier.height(28.dp)
  ) {
    Text(
      text = label,
      color = VsCodeLavender,
      fontSize = 10.sp,
      fontFamily = FontFamily.Monospace
    )
  }
}
