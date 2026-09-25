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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExtensionDialog(
  onAddExtension: (name: String, author: String, description: String, category: String, codeSnippet: String?, jsCode: String?) -> Unit,
  onDismiss: () -> Unit
) {
  var name by remember { mutableStateOf("") }
  var author by remember { mutableStateOf("Local Developer") }
  var description by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Tools") }
  var codeSnippet by remember { mutableStateOf("") }
  var jsCode by remember {
    mutableStateOf(
      """// VS Code Extension API Script
// Register a custom command
vscode.commands.registerCommand('extension.myCustomAction', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var text = editor.document.getText();
  editor.edit(function(builder) {
    builder.replace('/* Processed by ' + '${'$'}' + ' */\n' + text);
  });
  vscode.window.showInformationMessage('Custom extension executed successfully!');
});

// Contribute an action button to the editor toolbar
vscode.ui.addButton({
  id: 'btn_custom_action',
  label: '⚡ Run Custom',
  command: 'extension.myCustomAction',
  tooltip: 'Execute my custom extension'
});
""".trimIndent()
    )
  }

  val categories = listOf("Tools", "Formatters", "UI Libraries", "Snippets", "Linters")
  var categoryExpanded by remember { mutableStateOf(false) }

  fun applyPreset(presetName: String) {
    when (presetName) {
      "Uppercase" -> {
        name = "Uppercase Transformer"
        description = "Registers uppercase transformation command and adds a toolbar button."
        category = "Tools"
        jsCode = """vscode.commands.registerCommand('extension.toUpperCase', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var text = editor.document.getText();
  editor.edit(function(builder) {
    builder.replace(text.toUpperCase());
  });
  vscode.window.showInformationMessage('Converted active file to UPPERCASE!');
});

vscode.ui.addButton({
  id: 'btn_upper',
  label: '🔠 UPPERCASE',
  command: 'extension.toUpperCase',
  tooltip: 'Convert code to uppercase'
});"""
      }
      "Timestamp" -> {
        name = "Timestamp Header Generator"
        description = "Adds timestamp comment to the top of the file and status bar item."
        category = "Tools"
        jsCode = """vscode.commands.registerCommand('extension.addTimestamp', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var banner = '/* Generated: ' + new Date().toLocaleString() + ' */\n';
  editor.edit(function(builder) {
    builder.insert(0, banner);
  });
  vscode.window.showInformationMessage('Timestamp header inserted!');
});

vscode.window.createStatusBarItem({
  id: 'status_ts',
  text: '🕒 Add Timestamp',
  command: 'extension.addTimestamp',
  tooltip: 'Click to add timestamp to active file'
});"""
      }
      "DivWrapper" -> {
        name = "HTML Tag Encloser"
        description = "Encloses active editor code inside a styled container division."
        category = "Formatters"
        jsCode = """vscode.commands.registerCommand('extension.wrapDiv', function() {
  var editor = vscode.window.activeTextEditor;
  if (!editor) return;
  var text = editor.document.getText();
  editor.edit(function(builder) {
    builder.replace('<div class="app-wrapper">\n' + text + '\n</div>');
  });
  vscode.window.showInformationMessage('Enclosed code inside <div>!');
});

vscode.ui.addButton({
  id: 'btn_wrap_div',
  label: '📦 Wrap <div>',
  command: 'extension.wrapDiv',
  tooltip: 'Wrap inside HTML div container'
});"""
      }
    }
  }

  val isValid = name.isNotBlank() && description.isNotBlank()

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("add_extension_dialog"),
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Extension,
          contentDescription = "Add Extension",
          tint = VsCodeLavender,
          modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Load & Register Extension",
          color = VsCodeTextPrimary,
          fontSize = 16.sp,
          fontWeight = FontWeight.Bold
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = "Create an extension using the JavaScript VS Code Extension API to register commands, access editor content, and contribute UI elements.",
          color = VsCodeTextSecondary,
          fontSize = 11.sp,
          lineHeight = 15.sp,
          modifier = Modifier.padding(bottom = 8.dp)
        )

        // Presets
        Text(
          text = "Quick Presets:",
          color = VsCodeTextMuted,
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold
        )
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("Uppercase", "Timestamp", "DivWrapper").forEach { preset ->
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(VsCodeSurfaceDark)
                .border(1.dp, VsCodeBorder, RoundedCornerShape(6.dp))
                .clickable { applyPreset(preset) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = preset,
                color = VsCodeLavender,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Extension Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Extension Name *") },
          placeholder = { Text("e.g., Code Transformer") },
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .testTag("extension_name_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VsCodeLavender,
            unfocusedBorderColor = VsCodeBorder,
            focusedTextColor = VsCodeTextPrimary,
            unfocusedTextColor = VsCodeTextPrimary,
            focusedLabelColor = VsCodeLavender,
            unfocusedLabelColor = VsCodeTextMuted
          )
        )

        // Author & Category in Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = author,
            onValueChange = { author = it },
            label = { Text("Author") },
            singleLine = true,
            modifier = Modifier
              .weight(1f)
              .testTag("extension_author_input"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = VsCodeLavender,
              unfocusedBorderColor = VsCodeBorder,
              focusedTextColor = VsCodeTextPrimary,
              unfocusedTextColor = VsCodeTextPrimary,
              focusedLabelColor = VsCodeLavender,
              unfocusedLabelColor = VsCodeTextMuted
            )
          )

          ExposedDropdownMenuBox(
            expanded = categoryExpanded,
            onExpandedChange = { categoryExpanded = it },
            modifier = Modifier.weight(1f)
          ) {
            OutlinedTextField(
              value = category,
              onValueChange = {},
              readOnly = true,
              label = { Text("Category") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
              modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = VsCodeLavender,
                unfocusedBorderColor = VsCodeBorder,
                focusedTextColor = VsCodeTextPrimary,
                unfocusedTextColor = VsCodeTextPrimary,
                focusedLabelColor = VsCodeLavender,
                unfocusedLabelColor = VsCodeTextMuted
              )
            )
            ExposedDropdownMenu(
              expanded = categoryExpanded,
              onDismissRequest = { categoryExpanded = false },
              modifier = Modifier.background(VsCodeEditorBg)
            ) {
              categories.forEach { cat ->
                DropdownMenuItem(
                  text = { Text(cat, color = VsCodeTextPrimary) },
                  onClick = {
                    category = cat
                    categoryExpanded = false
                  }
                )
              }
            }
          }
        }

        // Description
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Description *") },
          placeholder = { Text("Brief description of features") },
          maxLines = 2,
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp)
            .testTag("extension_description_input"),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VsCodeLavender,
            unfocusedBorderColor = VsCodeBorder,
            focusedTextColor = VsCodeTextPrimary,
            unfocusedTextColor = VsCodeTextPrimary,
            focusedLabelColor = VsCodeLavender,
            unfocusedLabelColor = VsCodeTextMuted
          )
        )

        // JavaScript API Code field
        Text(
          text = "JavaScript Extension Code (vscode API):",
          color = VsCodeLavender,
          fontSize = 11.sp,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
        )
        OutlinedTextField(
          value = jsCode,
          onValueChange = { jsCode = it },
          maxLines = 8,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("extension_js_code_input"),
          textStyle = androidx.compose.ui.text.TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VsCodeLavender,
            unfocusedBorderColor = VsCodeBorder,
            focusedTextColor = VsCodeTextPrimary,
            unfocusedTextColor = VsCodeTextPrimary,
            focusedContainerColor = VsCodeSurfaceDark,
            unfocusedContainerColor = VsCodeSurfaceDark
          )
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (isValid) {
            onAddExtension(
              name.trim(),
              author.trim().ifEmpty { "Local Dev" },
              description.trim(),
              category,
              codeSnippet.trim().ifEmpty { null },
              jsCode.trim().ifEmpty { null }
            )
            onDismiss()
          }
        },
        enabled = isValid,
        colors = ButtonDefaults.buttonColors(
          containerColor = VsCodeLavender,
          disabledContainerColor = VsCodeLavender.copy(alpha = 0.3f)
        ),
        modifier = Modifier.testTag("confirm_add_extension_button")
      ) {
        Text("Load & Run Extension", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      OutlinedButton(onClick = onDismiss) {
        Text("Cancel", color = VsCodeTextSecondary)
      }
    },
    containerColor = VsCodeEditorBg,
    shape = RoundedCornerShape(12.dp)
  )
}
