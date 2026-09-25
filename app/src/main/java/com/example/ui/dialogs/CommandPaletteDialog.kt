package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.RegisteredCommand
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

data class PaletteCommandItem(
  val id: String,
  val title: String,
  val source: String, // "Extension" or "Built-in"
  val category: String,
  val extensionId: String? = null
)

@Composable
fun CommandPaletteDialog(
  registeredCommands: List<RegisteredCommand>,
  onExecuteCommand: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val focusRequester = remember { FocusRequester() }

  val builtInCommands = listOf(
    PaletteCommandItem("workbench.action.quickOpen", "File: Go to File... / Quick Open (Ctrl+P)", "Built-in", "File"),
    PaletteCommandItem("workbench.action.newFile", "File: New File", "Built-in", "File"),
    PaletteCommandItem("workbench.action.newFolder", "File: New Folder", "Built-in", "File"),
    PaletteCommandItem("editor.action.formatDocument", "Format Document (Prettier)", "Built-in", "Editor"),
    PaletteCommandItem("workbench.action.reloadRunner", "Web Runner: Reload Live Preview", "Built-in", "Runner"),
    PaletteCommandItem("workbench.action.toggleLivePreview", "View: Toggle Live HTML Preview (Split Pane)", "Built-in", "View"),
    PaletteCommandItem("workbench.action.togglePreviewOrientation", "View: Toggle Split Orientation (Side-by-Side / Top-Bottom)", "Built-in", "View"),
    PaletteCommandItem("workbench.action.copyHtml", "Easy Code: Export Standalone HTML", "Built-in", "Export"),
    PaletteCommandItem("workbench.action.importExtension", "Extensions: Import Extension App (.json, .js, .vsix)", "Built-in", "Extensions"),
    PaletteCommandItem("workbench.action.showExtensions", "View: Show Extensions", "Built-in", "View"),
    PaletteCommandItem("workbench.action.showExplorer", "View: Show File Explorer", "Built-in", "View"),
    PaletteCommandItem("workbench.action.showTerminal", "View: Show Terminal", "Built-in", "View")
  )

  val extensionCommandItems = registeredCommands.map { cmd ->
    PaletteCommandItem(
      id = cmd.id,
      title = cmd.title.ifBlank { cmd.id },
      source = "Extension",
      category = cmd.extensionId,
      extensionId = cmd.extensionId
    )
  }

  val allCommands = extensionCommandItems + builtInCommands
  val filteredCommands = allCommands.filter {
    it.title.contains(searchQuery, ignoreCase = true) ||
      it.id.contains(searchQuery, ignoreCase = true) ||
      it.category.contains(searchQuery, ignoreCase = true)
  }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {}
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("command_palette_dialog"),
    title = {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Icon(
          imageVector = Icons.Default.Terminal,
          contentDescription = "Command Palette",
          tint = VsCodeLavender,
          modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Command Palette",
          color = VsCodeTextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(VsCodeLavender.copy(alpha = 0.15f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${filteredCommands.size} commands",
            color = VsCodeLavender,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
          )
        }
      }
    },
    text = {
      Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Type a command to run...", fontSize = 12.sp) },
          leadingIcon = {
            Text(
              text = ">",
              color = VsCodeLavender,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(start = 12.dp)
            )
          },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("command_palette_search_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VsCodeLavender,
            unfocusedBorderColor = VsCodeBorder,
            focusedTextColor = VsCodeTextPrimary,
            unfocusedTextColor = VsCodeTextPrimary,
            focusedContainerColor = VsCodeSurfaceDark,
            unfocusedContainerColor = VsCodeSurfaceDark
          )
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (filteredCommands.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(120.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No matching commands found.",
              color = VsCodeTextMuted,
              fontSize = 12.sp
            )
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .height(280.dp)
          ) {
            items(filteredCommands, key = { it.id }) { cmd ->
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(6.dp))
                  .clickable {
                    onExecuteCommand(cmd.id)
                    onDismiss()
                  }
                  .padding(horizontal = 10.dp, vertical = 8.dp)
                  .testTag("command_item_${cmd.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    imageVector = if (cmd.source == "Extension") Icons.Default.Extension else Icons.Default.Code,
                    contentDescription = null,
                    tint = if (cmd.source == "Extension") VsCodeCyan else VsCodeLavender,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Column {
                    Text(
                      text = cmd.title,
                      color = VsCodeTextPrimary,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                    Text(
                      text = cmd.id,
                      color = VsCodeTextMuted,
                      fontSize = 10.sp,
                      fontFamily = FontFamily.Monospace
                    )
                  }
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (cmd.source == "Extension") VsCodeCyan.copy(alpha = 0.15f) else Color(0xFF27272A))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(
                    text = cmd.source,
                    color = if (cmd.source == "Extension") VsCodeCyan else VsCodeTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Close", color = VsCodeTextSecondary)
      }
    },
    containerColor = VsCodeEditorBg,
    shape = RoundedCornerShape(12.dp)
  )
}
