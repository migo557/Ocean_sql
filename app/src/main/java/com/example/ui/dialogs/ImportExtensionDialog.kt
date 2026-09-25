package com.example.ui.dialogs

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.extensions.ExtensionAppImporter
import com.example.extensions.ExtensionAppPreset
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

@Composable
fun ImportExtensionDialog(
  onImportContent: (rawContent: String, fileName: String?) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var rawContent by remember {
    mutableStateOf(ExtensionAppImporter.PRESETS[0].manifestJson)
  }
  var selectedPresetId by remember { mutableStateOf<String?>(ExtensionAppImporter.PRESETS[0].id) }
  var importedFileName by remember { mutableStateOf<String?>("pomodoro-manifest.json") }
  var selectedTab by remember { mutableStateOf(0) } // 0: Presets, 1: File / Paste

  // File Picker Launcher for importing .json, .js, .vsix from device
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      try {
        var name = "imported-extension.json"
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
          val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
          if (nameIndex != -1 && cursor.moveToFirst()) {
            name = cursor.getString(nameIndex)
          }
        }
        val inputStream = context.contentResolver.openInputStream(uri)
        val text = inputStream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
        if (text.isNotBlank()) {
          rawContent = text
          importedFileName = name
          selectedPresetId = null
          selectedTab = 1
        }
      } catch (_: Exception) {}
    }
  }

  // Parsed preview
  val parsedExtension by remember(rawContent, importedFileName) {
    derivedStateOf {
      try {
        if (rawContent.isBlank()) null
        else ExtensionAppImporter.parseExtension(rawContent, importedFileName)
      } catch (_: Exception) {
        null
      }
    }
  }

  val isValid = parsedExtension != null && rawContent.isNotBlank()

  AlertDialog(
    onDismissRequest = onDismiss,
    modifier = Modifier.testTag("import_extension_dialog"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(VsCodeLavender.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Extension,
              contentDescription = "Import",
              tint = VsCodeLavender,
              modifier = Modifier.size(18.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = "Import Extension App",
              color = VsCodeTextPrimary,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Easy Code Extension Package Manager",
              color = VsCodeTextMuted,
              fontSize = 11.sp
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState())
      ) {
        // Mode Selector: Presets vs File / Custom
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
        ) {
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (selectedTab == 0) VsCodeActiveTabBg else Color.Transparent)
              .border(1.dp, if (selectedTab == 0) VsCodeLavender else VsCodeBorder, RoundedCornerShape(6.dp))
              .clickable { selectedTab = 0 }
              .padding(vertical = 7.dp)
              .testTag("tab_import_presets"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "⭐ Featured Apps",
              color = if (selectedTab == 0) VsCodeLavender else VsCodeTextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(6.dp))
              .background(if (selectedTab == 1) VsCodeActiveTabBg else Color.Transparent)
              .border(1.dp, if (selectedTab == 1) VsCodeLavender else VsCodeBorder, RoundedCornerShape(6.dp))
              .clickable { selectedTab = 1 }
              .padding(vertical = 7.dp)
              .testTag("tab_import_custom"),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "📁 Device File / JSON",
              color = if (selectedTab == 1) VsCodeLavender else VsCodeTextSecondary,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }

        if (selectedTab == 0) {
          // Featured Apps Selection
          Text(
            text = "Select an extension app to inspect and import:",
            color = VsCodeTextSecondary,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            ExtensionAppImporter.PRESETS.forEach { preset ->
              val isSelected = selectedPresetId == preset.id
              Card(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    selectedPresetId = preset.id
                    rawContent = preset.manifestJson
                    importedFileName = "${preset.id}.json"
                  }
                  .testTag("preset_${preset.id}"),
                colors = CardDefaults.cardColors(
                  containerColor = if (isSelected) VsCodeActiveTabBg else VsCodeSurfaceDark
                ),
                border = BorderStroke(
                  1.dp,
                  if (isSelected) VsCodeLavender else VsCodeBorder
                ),
                shape = RoundedCornerShape(8.dp)
              ) {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(preset.iconEmoji, fontSize = 20.sp)
                  Spacer(modifier = Modifier.width(10.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                        text = preset.name,
                        color = VsCodeTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text = "v${preset.version}",
                        color = VsCodeLavender,
                        fontSize = 10.sp
                      )
                    }
                    Text(
                      text = preset.description,
                      color = VsCodeTextSecondary,
                      fontSize = 10.sp,
                      lineHeight = 13.sp,
                      maxLines = 2
                    )
                  }
                }
              }
            }
          }
        } else {
          // File Picker Button
          Button(
            onClick = {
              try {
                filePickerLauncher.launch("*/*")
              } catch (_: Exception) {}
            },
            modifier = Modifier
              .fillMaxWidth()
              .height(44.dp)
              .testTag("pick_file_button"),
            colors = ButtonDefaults.buttonColors(
              containerColor = VsCodeSurfaceDark
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, VsCodeLavender.copy(alpha = 0.5f))
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = "Open File",
              tint = VsCodeLavender,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = if (importedFileName != null) "Imported: $importedFileName (Click to re-select)" else "Pick Extension File (.json, .js, .vsix)",
              color = VsCodeLavender,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "Or paste Extension Manifest JSON / Script:",
            color = VsCodeTextMuted,
            fontSize = 11.sp,
            modifier = Modifier.padding(bottom = 4.dp)
          )

          OutlinedTextField(
            value = rawContent,
            onValueChange = {
              rawContent = it
              selectedPresetId = null
            },
            maxLines = 8,
            modifier = Modifier
              .fillMaxWidth()
              .testTag("import_raw_content_input"),
            textStyle = androidx.compose.ui.text.TextStyle(
              fontFamily = FontFamily.Monospace,
              fontSize = 10.sp
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

        Spacer(modifier = Modifier.height(12.dp))

        // Manifest Validation Preview Card
        parsedExtension?.let { ext ->
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .testTag("manifest_validation_card"),
            colors = CardDefaults.cardColors(containerColor = VsCodeSurfaceDark),
            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
            shape = RoundedCornerShape(8.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Valid",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Valid Extension App Manifest",
                    color = Color(0xFF10B981),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }

                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(VsCodeLavender.copy(alpha = 0.2f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                  Text(ext.category, color = VsCodeLavender, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
              }

              Spacer(modifier = Modifier.height(6.dp))

              Text(
                text = "${ext.iconEmoji} ${ext.name} (v${ext.version}) by ${ext.author}",
                color = VsCodeTextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
              )

              if (ext.contributedCommands.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Commands (${ext.contributedCommands.size}): ${ext.contributedCommands.joinToString(", ")}",
                  color = VsCodeTextSecondary,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }

              if (ext.contributedButtons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Buttons (${ext.contributedButtons.size}): ${ext.contributedButtons.joinToString(", ")}",
                  color = VsCodeCyan,
                  fontSize = 10.sp,
                  fontFamily = FontFamily.Monospace
                )
              }
            }
          }
        } ?: run {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(Color(0xFFEF4444).copy(alpha = 0.1f))
              .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
              .padding(10.dp)
          ) {
            Text(
              text = "Invalid or empty extension content. Please select an app preset or choose a valid JSON/JS file.",
              color = Color(0xFFEF4444),
              fontSize = 11.sp
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (isValid) {
            // If preset is selected and has special jsCode, inject script into the payload
            val finalContent = if (selectedPresetId != null) {
              val preset = ExtensionAppImporter.PRESETS.find { it.id == selectedPresetId }
              if (preset != null) {
                // Ensure the script is embedded so it executes
                try {
                  val obj = org.json.JSONObject(preset.manifestJson)
                  obj.put("script", preset.jsCode)
                  obj.toString(2)
                } catch (_: Exception) {
                  rawContent
                }
              } else rawContent
            } else rawContent

            onImportContent(finalContent, importedFileName)
            onDismiss()
          }
        },
        enabled = isValid,
        colors = ButtonDefaults.buttonColors(
          containerColor = VsCodeLavender,
          disabledContainerColor = VsCodeLavender.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.testTag("confirm_import_extension_button")
      ) {
        Icon(
          imageVector = Icons.Default.PlayArrow,
          contentDescription = null,
          tint = Color(0xFF1E1035),
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "Import & Activate App",
          color = Color(0xFF1E1035),
          fontWeight = FontWeight.Bold
        )
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        shape = RoundedCornerShape(8.dp)
      ) {
        Text("Cancel", color = VsCodeTextSecondary)
      }
    },
    containerColor = VsCodeEditorBg,
    shape = RoundedCornerShape(12.dp)
  )
}
