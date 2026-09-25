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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.models.CodeFile
import com.example.models.ProjectFolder
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeEmerald
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTabsBg
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

/**
 * Resolves the hierarchical directory path for a file by traversing project folders.
 */
fun getFileDirectoryPath(file: CodeFile, folders: List<ProjectFolder>): String {
  val segments = mutableListOf<String>()
  var parentId = file.parentFolderId
  while (parentId != null) {
    val parent = folders.find { it.id == parentId }
    if (parent != null) {
      segments.add(0, parent.name)
      parentId = parent.parentFolderId
    } else {
      break
    }
  }
  return if (segments.isEmpty()) "root" else segments.joinToString("/")
}

/**
 * Resolves full relative path (e.g., "src/styles/style.css" or "index.html").
 */
fun getFileFullPath(file: CodeFile, folders: List<ProjectFolder>): String {
  val dir = getFileDirectoryPath(file, folders)
  return if (dir == "root") file.name else "$dir/${file.name}"
}

@Composable
fun GlobalFileSearchOverlay(
  files: List<CodeFile>,
  folders: List<ProjectFolder>,
  activeFileId: String,
  openTabIds: List<String>,
  recentFileIds: List<String> = emptyList(),
  onOpenFile: (String) -> Unit,
  onCreateAndOpenFile: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("ALL") }
  val focusRequester = remember { FocusRequester() }

  LaunchedEffect(Unit) {
    try {
      focusRequester.requestFocus()
    } catch (_: Exception) {}
  }

  // Pre-calculate file metadata and paths
  data class FileSearchResult(
    val file: CodeFile,
    val dirPath: String,
    val fullPath: String,
    val isRecent: Boolean,
    val isActive: Boolean,
    val isOpenTab: Boolean
  )

  val allResults = remember(files, folders, activeFileId, openTabIds, recentFileIds) {
    files.map { f ->
      FileSearchResult(
        file = f,
        dirPath = getFileDirectoryPath(f, folders),
        fullPath = getFileFullPath(f, folders),
        isRecent = f.id in recentFileIds,
        isActive = f.id == activeFileId,
        isOpenTab = f.id in openTabIds
      )
    }
  }

  // Filter based on search query and category
  val filteredResults = remember(allResults, searchQuery, selectedCategory) {
    val query = searchQuery.trim().lowercase()

    allResults.filter { item ->
      val matchesCategory = when (selectedCategory) {
        "HTML" -> item.file.extension.equals("html", ignoreCase = true) || item.file.extension.equals("htm", ignoreCase = true)
        "CSS" -> item.file.extension.equals("css", ignoreCase = true)
        "JS" -> item.file.extension.lowercase() in listOf("js", "javascript", "ts", "json")
        "RECENT" -> item.isRecent || item.isOpenTab
        else -> true
      }

      if (!matchesCategory) return@filter false

      if (query.isEmpty()) return@filter true

      // Matching file name, extension, directory path, or full path
      item.file.name.lowercase().contains(query) ||
        item.file.extension.lowercase().contains(query.removePrefix(".")) ||
        item.fullPath.lowercase().contains(query) ||
        item.dirPath.lowercase().contains(query)
    }.sortedWith(
      compareByDescending<FileSearchResult> {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
          if (it.isActive) 3 else if (it.isRecent) 2 else if (it.isOpenTab) 1 else 0
        } else {
          // Exact name match highest priority
          if (it.file.name.lowercase() == q) 4
          // Name starts with query
          else if (it.file.name.lowercase().startsWith(q)) 3
          // Name contains query
          else if (it.file.name.lowercase().contains(q)) 2
          // Path contains query
          else 1
        }
      }
    )
  }

  val canCreateFile = remember(searchQuery, files) {
    val trimmed = searchQuery.trim()
    trimmed.isNotEmpty() &&
      !trimmed.contains("/") &&
      !trimmed.contains("\\") &&
      !files.any { it.name.equals(trimmed, ignoreCase = true) }
  }

  AlertDialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
    modifier = Modifier
      .fillMaxWidth(0.94f)
      .testTag("global_file_search_overlay"),
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(28.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(VsCodeCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search Files",
              tint = VsCodeCyan,
              modifier = Modifier.size(16.dp)
            )
          }
          Column {
            Text(
              text = "Go to File",
              color = VsCodeTextPrimary,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Quick Open project files",
              color = VsCodeTextMuted,
              fontSize = 10.sp
            )
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // Shortcut badge
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF27272A))
              .border(1.dp, VsCodeBorder, RoundedCornerShape(4.dp))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "Ctrl+P",
              color = VsCodeLavender,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace
            )
          }

          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(28.dp)
              .testTag("quick_open_close_btn")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = VsCodeTextMuted,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Search input text field with auto-focus
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = "Type file name or path (e.g. index, .css, script)...",
              fontSize = 12.sp,
              color = VsCodeTextMuted
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = VsCodeCyan,
              modifier = Modifier.size(18.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(
                onClick = { searchQuery = "" },
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear",
                  tint = VsCodeTextMuted,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          },
          singleLine = true,
          keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
          keyboardActions = KeyboardActions(
            onSearch = {
              if (filteredResults.isNotEmpty()) {
                onOpenFile(filteredResults.first().file.id)
                onDismiss()
              } else if (canCreateFile) {
                onCreateAndOpenFile(searchQuery.trim())
                onDismiss()
              }
            }
          ),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = VsCodeCyan,
            unfocusedBorderColor = VsCodeBorder,
            focusedTextColor = VsCodeTextPrimary,
            unfocusedTextColor = VsCodeTextPrimary,
            focusedContainerColor = VsCodeSurfaceDark,
            unfocusedContainerColor = VsCodeSurfaceDark
          ),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
            .testTag("global_file_search_input")
        )

        // Filter categories row (All, Recent, HTML, CSS, JS)
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
        ) {
          val categories = listOf("ALL", "RECENT", "HTML", "CSS", "JS")
          items(categories) { cat ->
            val isSelected = selectedCategory == cat
            val count = when (cat) {
              "ALL" -> allResults.size
              "RECENT" -> allResults.count { it.isRecent || it.isOpenTab }
              "HTML" -> allResults.count { it.file.extension.equals("html", true) || it.file.extension.equals("htm", true) }
              "CSS" -> allResults.count { it.file.extension.equals("css", true) }
              "JS" -> allResults.count { it.file.extension.lowercase() in listOf("js", "json", "ts") }
              else -> 0
            }
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = if (isSelected) VsCodeCyan.copy(alpha = 0.2f) else Color(0xFF222225),
              border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSelected) VsCodeCyan else VsCodeBorder
              ),
              modifier = Modifier
                .clickable { selectedCategory = cat }
                .testTag("quick_open_filter_${cat.lowercase()}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                if (cat == "RECENT") {
                  Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = null,
                    tint = if (isSelected) VsCodeCyan else VsCodeTextMuted,
                    modifier = Modifier.size(12.dp)
                  )
                }
                Text(
                  text = "$cat ($count)",
                  color = if (isSelected) VsCodeCyan else VsCodeTextSecondary,
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
              }
            }
          }
        }

        // Section header info
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = if (searchQuery.isEmpty()) {
              if (selectedCategory == "RECENT") "RECENTLY OPENED FILES" else "PROJECT FILES (${filteredResults.size})"
            } else {
              "MATCHES (${filteredResults.size})"
            },
            color = VsCodeTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
          Text(
            text = "Tap file to open",
            color = VsCodeTextMuted,
            fontSize = 9.sp
          )
        }

        // Results LazyColumn or Empty state
        if (filteredResults.isEmpty()) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(min = 140.dp)
              .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.SearchOff,
              contentDescription = null,
              tint = VsCodeTextMuted,
              modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "No files found matching \"$searchQuery\"",
              color = VsCodeTextSecondary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
            Text(
              text = "Check the spelling or try searching by extension (.html, .css, .js)",
              color = VsCodeTextMuted,
              fontSize = 11.sp
            )

            if (canCreateFile) {
              Spacer(modifier = Modifier.height(12.dp))
              Button(
                onClick = {
                  onCreateAndOpenFile(searchQuery.trim())
                  onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = VsCodeCyan,
                  contentColor = Color(0xFF0F172A)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("quick_open_create_file_btn")
              ) {
                Icon(
                  imageVector = Icons.Default.Add,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Create & open \"${searchQuery.trim()}\"",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        } else {
          LazyColumn(
            modifier = Modifier
              .fillMaxWidth()
              .heightIn(max = 300.dp)
          ) {
            items(filteredResults, key = { it.file.id }) { item ->
              val file = item.file
              val isItemActive = item.isActive

              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .heightIn(min = 52.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(
                    if (isItemActive) VsCodeCyan.copy(alpha = 0.12f)
                    else Color(0xFF1B1B1E)
                  )
                  .border(
                    width = 1.dp,
                    color = if (isItemActive) VsCodeCyan.copy(alpha = 0.4f) else VsCodeBorder.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                  )
                  .clickable {
                    onOpenFile(file.id)
                    onDismiss()
                  }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("quick_open_item_${file.id}"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                // Left: Extension pill + Name & Directory
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  // File extension badge
                  Box(
                    modifier = Modifier
                      .size(34.dp)
                      .clip(RoundedCornerShape(6.dp))
                      .background(Color(file.iconColorHex).copy(alpha = 0.2f))
                      .border(1.dp, Color(file.iconColorHex).copy(alpha = 0.5f), RoundedCornerShape(6.dp)),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = file.extension.take(4).uppercase(),
                      color = Color(file.iconColorHex),
                      fontSize = 9.sp,
                      fontWeight = FontWeight.Bold,
                      fontFamily = FontFamily.Monospace
                    )
                  }

                  Spacer(modifier = Modifier.width(10.dp))

                  Column {
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                      Text(
                        text = file.name,
                        color = if (isItemActive) VsCodeCyan else VsCodeTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                      )

                      if (item.isRecent && !item.isActive) {
                        Box(
                          modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF27272A))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                          Text(
                            text = "Recent",
                            color = VsCodeTextMuted,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Medium
                          )
                        }
                      }
                    }

                    // Directory breadcrumb
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                      Icon(
                        imageVector = Icons.Default.Folder,
                        contentDescription = null,
                        tint = VsCodeTextMuted,
                        modifier = Modifier.size(10.dp)
                      )
                      Text(
                        text = if (item.dirPath == "root") "easy-code/src" else "easy-code/${item.dirPath}",
                        color = VsCodeTextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                      )
                    }
                  }
                }

                // Right: Status chips (Active / Open / Line Count)
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                  val lineCount = remember(file.content) { file.content.lines().size }
                  Text(
                    text = "$lineCount lines",
                    color = VsCodeTextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                  )

                  if (isItemActive) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(VsCodeCyan.copy(alpha = 0.2f))
                        .border(1.dp, VsCodeCyan, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "ACTIVE",
                        color = VsCodeCyan,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                      )
                    }
                  } else if (item.isOpenTab) {
                    Box(
                      modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(VsCodeLavender.copy(alpha = 0.15f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                      Text(
                        text = "OPEN",
                        color = VsCodeLavender,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                      )
                    }
                  }
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "${allResults.size} files in workspace",
          color = VsCodeTextMuted,
          fontSize = 10.sp
        )

        TextButton(
          onClick = onDismiss,
          modifier = Modifier.testTag("quick_open_dismiss_btn")
        ) {
          Text(
            text = "Cancel",
            color = VsCodeTextSecondary,
            fontSize = 12.sp
          )
        }
      }
    },
    containerColor = VsCodeEditorBg,
    shape = RoundedCornerShape(16.dp)
  )
}
