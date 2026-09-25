package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.models.CodeFile
import com.example.models.ProjectFolder
import com.example.ui.theme.VsCodeActiveTabBg
import com.example.ui.theme.VsCodeBorder
import com.example.ui.theme.VsCodeCyan
import com.example.ui.theme.VsCodeEditorBg
import com.example.ui.theme.VsCodeLavender
import com.example.ui.theme.VsCodeSidebarBg
import com.example.ui.theme.VsCodeSurfaceDark
import com.example.ui.theme.VsCodeTextMuted
import com.example.ui.theme.VsCodeTextPrimary
import com.example.ui.theme.VsCodeTextSecondary

sealed class RenameTarget {
  data class FileItem(val file: CodeFile) : RenameTarget()
  data class FolderItem(val folder: ProjectFolder) : RenameTarget()
}

sealed class DeleteTarget {
  data class FileItem(val file: CodeFile, val isOnlyFile: Boolean = false) : DeleteTarget()
  data class FolderItem(val folder: ProjectFolder, val childCount: Int) : DeleteTarget()
}

@Composable
fun FileExplorerView(
  folders: List<ProjectFolder>,
  files: List<CodeFile>,
  activeFileId: String,
  onSelectFile: (String) -> Unit,
  onToggleFolder: (String) -> Unit,
  onExpandAllFolders: () -> Unit,
  onCollapseAllFolders: () -> Unit,
  onCreateFile: (fileName: String, parentFolderId: String?) -> Unit,
  onCreateFolder: (folderName: String, parentFolderId: String?) -> Unit,
  onRenameFile: (fileId: String, newName: String) -> Unit,
  onRenameFolder: (folderId: String, newName: String) -> Unit,
  onDeleteFile: (fileId: String) -> Unit,
  onDeleteFolder: (folderId: String) -> Unit,
  onOpenGlobalFileSearch: () -> Unit = {}
) {
  var showNewFileDialog by remember { mutableStateOf(false) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var targetParentFolder by remember { mutableStateOf<ProjectFolder?>(null) }
  var renameTarget by remember { mutableStateOf<RenameTarget?>(null) }
  var deleteTarget by remember { mutableStateOf<DeleteTarget?>(null) }

  var inputFileName by remember { mutableStateOf("") }
  var inputFolderName by remember { mutableStateOf("") }
  var renameInputName by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .background(VsCodeSidebarBg)
      .testTag("file_explorer_view")
  ) {
    // 1. Explorer Header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.FolderOpen,
          contentDescription = "Project Folder",
          tint = VsCodeLavender,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "EXPLORER",
          color = VsCodeTextSecondary,
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.sp
        )
      }

      Row(verticalAlignment = Alignment.CenterVertically) {
        // Quick Open File Search
        IconButton(
          onClick = onOpenGlobalFileSearch,
          modifier = Modifier.size(26.dp).testTag("explorer_quick_search_file_btn")
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Quick Open File (Ctrl+P)",
            tint = VsCodeCyan,
            modifier = Modifier.size(16.dp)
          )
        }

        // New File in Root
        IconButton(
          onClick = {
            targetParentFolder = null
            inputFileName = ""
            showNewFileDialog = true
          },
          modifier = Modifier.size(26.dp).testTag("explorer_add_file_btn")
        ) {
          Icon(
            imageVector = Icons.Default.NoteAdd,
            contentDescription = "New File",
            tint = VsCodeLavender,
            modifier = Modifier.size(16.dp)
          )
        }

        // New Folder in Root
        IconButton(
          onClick = {
            targetParentFolder = null
            inputFolderName = ""
            showNewFolderDialog = true
          },
          modifier = Modifier.size(26.dp).testTag("explorer_add_folder_btn")
        ) {
          Icon(
            imageVector = Icons.Default.CreateNewFolder,
            contentDescription = "New Folder",
            tint = VsCodeLavender,
            modifier = Modifier.size(16.dp)
          )
        }

        // Collapse All
        IconButton(
          onClick = onCollapseAllFolders,
          modifier = Modifier.size(26.dp).testTag("explorer_collapse_all_btn")
        ) {
          Icon(
            imageVector = Icons.Default.UnfoldLess,
            contentDescription = "Collapse All",
            tint = VsCodeTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }

        // Expand All
        IconButton(
          onClick = onExpandAllFolders,
          modifier = Modifier.size(26.dp).testTag("explorer_expand_all_btn")
        ) {
          Icon(
            imageVector = Icons.Default.UnfoldMore,
            contentDescription = "Expand All",
            tint = VsCodeTextMuted,
            modifier = Modifier.size(16.dp)
          )
        }
      }
    }

    // 2. Workspace Root Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(VsCodeSurfaceDark)
        .padding(horizontal = 12.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(
        text = "▾ STUDIO-CODE-WORKSPACE",
        color = VsCodeTextPrimary,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = "${files.size} files",
        color = VsCodeTextMuted,
        fontSize = 10.sp
      )
    }

    // 3. Hierarchical Tree
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
    ) {
      if (files.isEmpty() && folders.isEmpty()) {
        item {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
          ) {
            Icon(
              imageVector = Icons.Default.FolderOpen,
              contentDescription = null,
              tint = VsCodeTextMuted,
              modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
              text = "No files in workspace",
              color = VsCodeTextSecondary,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
              onClick = {
                targetParentFolder = null
                inputFileName = "index.html"
                showNewFileDialog = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = VsCodeLavender),
              modifier = Modifier.testTag("explorer_empty_create_file_btn")
            ) {
              Text("Create File", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        item {
          HierarchicalNodeRenderer(
            parentId = null,
            depth = 0,
            folders = folders,
            files = files,
            activeFileId = activeFileId,
            onSelectFile = onSelectFile,
            onToggleFolder = onToggleFolder,
            onAddFileToFolder = { folder ->
              targetParentFolder = folder
              inputFileName = ""
              showNewFileDialog = true
            },
            onAddSubfolder = { folder ->
              targetParentFolder = folder
              inputFolderName = ""
              showNewFolderDialog = true
            },
            onStartRenameFile = { file ->
              renameTarget = RenameTarget.FileItem(file)
              renameInputName = file.name
            },
            onStartRenameFolder = { folder ->
              renameTarget = RenameTarget.FolderItem(folder)
              renameInputName = folder.name
            },
            onStartDeleteFile = { file ->
              deleteTarget = DeleteTarget.FileItem(file, files.size <= 1)
            },
            onStartDeleteFolder = { folder ->
              val childCount = files.count { it.parentFolderId == folder.id } +
                folders.count { it.parentFolderId == folder.id }
              deleteTarget = DeleteTarget.FolderItem(folder, childCount)
            }
          )
        }
      }
    }
  }

  // --- DIALOG: Create New File ---
  if (showNewFileDialog) {
    AlertDialog(
      onDismissRequest = { showNewFileDialog = false },
      title = {
        Text(
          text = if (targetParentFolder != null) "New File in /${targetParentFolder?.name}/" else "New File in Root",
          color = VsCodeTextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = "Enter file name with extension (e.g., component.html, app.js, theme.css):",
            color = VsCodeTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          // Template chips
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("html", "js", "css", "json", "md").forEach { ext ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(VsCodeSurfaceDark)
                  .clickable {
                    inputFileName = if (inputFileName.contains(".")) {
                      inputFileName.substringBeforeLast(".") + ".$ext"
                    } else if (inputFileName.isNotBlank()) {
                      "$inputFileName.$ext"
                    } else {
                      "file.$ext"
                    }
                  }
                  .padding(horizontal = 6.dp, vertical = 3.dp)
              ) {
                Text(
                  text = ".$ext",
                  color = VsCodeLavender,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }

          OutlinedTextField(
            value = inputFileName,
            onValueChange = { inputFileName = it },
            placeholder = { Text("newfile.html", fontSize = 12.sp) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
              onDone = {
                if (inputFileName.isNotBlank()) {
                  onCreateFile(inputFileName.trim(), targetParentFolder?.id)
                  showNewFileDialog = false
                  inputFileName = ""
                }
              }
            ),
            modifier = Modifier.fillMaxWidth().testTag("input_new_file_name"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = VsCodeLavender,
              unfocusedBorderColor = VsCodeBorder,
              focusedTextColor = VsCodeTextPrimary,
              unfocusedTextColor = VsCodeTextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (inputFileName.isNotBlank()) {
              onCreateFile(inputFileName.trim(), targetParentFolder?.id)
              showNewFileDialog = false
              inputFileName = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = VsCodeLavender),
          modifier = Modifier.testTag("confirm_create_file_btn")
        ) {
          Text("Create File", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showNewFileDialog = false }) {
          Text("Cancel", color = VsCodeTextSecondary)
        }
      },
      containerColor = VsCodeEditorBg
    )
  }

  // --- DIALOG: Create New Folder ---
  if (showNewFolderDialog) {
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      title = {
        Text(
          text = if (targetParentFolder != null) "New Folder in /${targetParentFolder?.name}/" else "New Folder in Root",
          color = VsCodeTextPrimary,
          fontSize = 15.sp,
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = "Enter folder name (e.g., components, utils, assets):",
            color = VsCodeTextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )
          OutlinedTextField(
            value = inputFolderName,
            onValueChange = { inputFolderName = it },
            placeholder = { Text("folder_name", fontSize = 12.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_new_folder_name"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = VsCodeLavender,
              unfocusedBorderColor = VsCodeBorder,
              focusedTextColor = VsCodeTextPrimary,
              unfocusedTextColor = VsCodeTextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (inputFolderName.isNotBlank()) {
              onCreateFolder(inputFolderName.trim(), targetParentFolder?.id)
              showNewFolderDialog = false
              inputFolderName = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = VsCodeLavender),
          modifier = Modifier.testTag("confirm_create_folder_btn")
        ) {
          Text("Create Folder", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { showNewFolderDialog = false }) {
          Text("Cancel", color = VsCodeTextSecondary)
        }
      },
      containerColor = VsCodeEditorBg
    )
  }

  // --- DIALOG: Rename File or Folder ---
  renameTarget?.let { target ->
    val titleText = when (target) {
      is RenameTarget.FileItem -> "Rename File '${target.file.name}'"
      is RenameTarget.FolderItem -> "Rename Folder '${target.folder.name}'"
    }

    AlertDialog(
      onDismissRequest = { renameTarget = null },
      title = {
        Text(titleText, color = VsCodeTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
      },
      text = {
        Column {
          Text("Enter new name:", color = VsCodeTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
          OutlinedTextField(
            value = renameInputName,
            onValueChange = { renameInputName = it },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
              onDone = {
                if (renameInputName.isNotBlank()) {
                  when (target) {
                    is RenameTarget.FileItem -> onRenameFile(target.file.id, renameInputName.trim())
                    is RenameTarget.FolderItem -> onRenameFolder(target.folder.id, renameInputName.trim())
                  }
                  renameTarget = null
                }
              }
            ),
            modifier = Modifier.fillMaxWidth().testTag("rename_input_field"),
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = VsCodeLavender,
              unfocusedBorderColor = VsCodeBorder,
              focusedTextColor = VsCodeTextPrimary,
              unfocusedTextColor = VsCodeTextPrimary
            )
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (renameInputName.isNotBlank()) {
              when (target) {
                is RenameTarget.FileItem -> onRenameFile(target.file.id, renameInputName.trim())
                is RenameTarget.FolderItem -> onRenameFolder(target.folder.id, renameInputName.trim())
              }
              renameTarget = null
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = VsCodeLavender),
          modifier = Modifier.testTag("confirm_rename_btn")
        ) {
          Text("Rename", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { renameTarget = null }) {
          Text("Cancel", color = VsCodeTextSecondary)
        }
      },
      containerColor = VsCodeEditorBg
    )
  }

  // --- DIALOG: Delete Confirmation ---
  deleteTarget?.let { target ->
    val isBlocked = target is DeleteTarget.FileItem && target.isOnlyFile
    val (titleText, bodyText) = when (target) {
      is DeleteTarget.FileItem -> if (target.isOnlyFile) {
        Pair(
          "Cannot Delete File",
          "Cannot delete '${target.file.name}'. The workspace requires at least one active file."
        )
      } else {
        Pair(
          "Delete '${target.file.name}'?",
          "Are you sure you want to delete '${target.file.name}'? This action cannot be undone."
        )
      }
      is DeleteTarget.FolderItem -> Pair(
        "Delete folder '${target.folder.name}'?",
        "Are you sure you want to delete folder '${target.folder.name}' and all its contents (${target.childCount} items)? This action cannot be undone."
      )
    }

    AlertDialog(
      onDismissRequest = { deleteTarget = null },
      title = { Text(titleText, color = VsCodeTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
      text = { Text(bodyText, color = VsCodeTextSecondary, fontSize = 12.sp) },
      confirmButton = {
        if (isBlocked) {
          Button(
            onClick = { deleteTarget = null },
            colors = ButtonDefaults.buttonColors(containerColor = VsCodeLavender),
            modifier = Modifier.testTag("dismiss_delete_blocked_btn")
          ) {
            Text("OK", color = Color(0xFF1E1035), fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = {
              when (target) {
                is DeleteTarget.FileItem -> onDeleteFile(target.file.id)
                is DeleteTarget.FolderItem -> onDeleteFolder(target.folder.id)
              }
              deleteTarget = null
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            modifier = Modifier.testTag("confirm_delete_btn")
          ) {
            Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
          }
        }
      },
      dismissButton = {
        if (!isBlocked) {
          OutlinedButton(onClick = { deleteTarget = null }) {
            Text("Cancel", color = VsCodeTextSecondary)
          }
        }
      },
      containerColor = VsCodeEditorBg
    )
  }
}

@Composable
private fun HierarchicalNodeRenderer(
  parentId: String?,
  depth: Int,
  folders: List<ProjectFolder>,
  files: List<CodeFile>,
  activeFileId: String,
  onSelectFile: (String) -> Unit,
  onToggleFolder: (String) -> Unit,
  onAddFileToFolder: (ProjectFolder) -> Unit,
  onAddSubfolder: (ProjectFolder) -> Unit,
  onStartRenameFile: (CodeFile) -> Unit,
  onStartRenameFolder: (ProjectFolder) -> Unit,
  onStartDeleteFile: (CodeFile) -> Unit,
  onStartDeleteFolder: (ProjectFolder) -> Unit
) {
  val currentFolders = folders.filter { it.parentFolderId == parentId }
  val currentFiles = files.filter { it.parentFolderId == parentId }

  // Render Folders first
  currentFolders.forEach { folder ->
    val isExpanded = folder.isExpanded
    val childCount = files.count { it.parentFolderId == folder.id } +
      folders.count { it.parentFolderId == folder.id }

    Column(modifier = Modifier.fillMaxWidth()) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onToggleFolder(folder.id) }
          .padding(
            start = (12 + depth * 14).dp,
            end = 8.dp,
            top = 4.dp,
            bottom = 4.dp
          )
          .testTag("folder_item_${folder.name}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Arrow icon
          Icon(
            imageVector = if (isExpanded) Icons.Default.KeyboardArrowDown else Icons.Default.KeyboardArrowRight,
            contentDescription = if (isExpanded) "Collapse" else "Expand",
            tint = VsCodeTextMuted,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(4.dp))

          // Folder icon
          Icon(
            imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
            contentDescription = null,
            tint = Color(0xFFFBBF24), // Warm folder amber
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))

          Text(
            text = folder.name,
            color = VsCodeTextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
          )

          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "($childCount)",
            color = VsCodeTextMuted,
            fontSize = 10.sp
          )
        }

        // Folder inline actions
        Row(verticalAlignment = Alignment.CenterVertically) {
          // Add file into folder
          IconButton(
            onClick = { onAddFileToFolder(folder) },
            modifier = Modifier.size(22.dp).testTag("folder_add_file_${folder.name}")
          ) {
            Icon(
              imageVector = Icons.Default.NoteAdd,
              contentDescription = "New File in Folder",
              tint = VsCodeTextMuted,
              modifier = Modifier.size(13.dp)
            )
          }

          // Add subfolder into folder
          IconButton(
            onClick = { onAddSubfolder(folder) },
            modifier = Modifier.size(22.dp).testTag("folder_add_subfolder_${folder.name}")
          ) {
            Icon(
              imageVector = Icons.Default.CreateNewFolder,
              contentDescription = "New Subfolder",
              tint = VsCodeTextMuted,
              modifier = Modifier.size(13.dp)
            )
          }

          // Rename folder
          IconButton(
            onClick = { onStartRenameFolder(folder) },
            modifier = Modifier.size(22.dp).testTag("folder_rename_${folder.name}")
          ) {
            Icon(
              imageVector = Icons.Default.DriveFileRenameOutline,
              contentDescription = "Rename Folder",
              tint = VsCodeTextMuted,
              modifier = Modifier.size(13.dp)
            )
          }

          // Delete folder
          IconButton(
            onClick = { onStartDeleteFolder(folder) },
            modifier = Modifier.size(22.dp).testTag("folder_delete_${folder.name}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete Folder",
              tint = VsCodeTextMuted,
              modifier = Modifier.size(13.dp)
            )
          }
        }
      }

      // If expanded, recursively render children
      if (isExpanded) {
        HierarchicalNodeRenderer(
          parentId = folder.id,
          depth = depth + 1,
          folders = folders,
          files = files,
          activeFileId = activeFileId,
          onSelectFile = onSelectFile,
          onToggleFolder = onToggleFolder,
          onAddFileToFolder = onAddFileToFolder,
          onAddSubfolder = onAddSubfolder,
          onStartRenameFile = onStartRenameFile,
          onStartRenameFolder = onStartRenameFolder,
          onStartDeleteFile = onStartDeleteFile,
          onStartDeleteFolder = onStartDeleteFolder
        )
      }
    }
  }

  // Render Files next
  currentFiles.forEach { file ->
    val isSelected = file.id == activeFileId

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(if (isSelected) VsCodeActiveTabBg else Color.Transparent)
        .clickable { onSelectFile(file.id) }
        .padding(
          start = (24 + depth * 14).dp,
          end = 8.dp,
          top = 4.dp,
          bottom = 4.dp
        )
        .testTag("file_item_${file.name}"),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        // File extension badge
        Box(
          modifier = Modifier
            .size(18.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Color(file.iconColorHex).copy(alpha = 0.2f)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = file.extension.uppercase().take(3),
            color = Color(file.iconColorHex),
            fontSize = 7.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = file.name,
            color = if (isSelected) VsCodeLavender else VsCodeTextPrimary,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
          )

          if (file.isModified) {
            Spacer(modifier = Modifier.width(4.dp))
            Box(
              modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(VsCodeLavender)
            )
          }
        }
      }

      // File inline actions
      Row(verticalAlignment = Alignment.CenterVertically) {
        // Rename file
        IconButton(
          onClick = { onStartRenameFile(file) },
          modifier = Modifier.size(22.dp).testTag("file_rename_${file.name}")
        ) {
          Icon(
            imageVector = Icons.Default.DriveFileRenameOutline,
            contentDescription = "Rename File",
            tint = VsCodeTextMuted,
            modifier = Modifier.size(13.dp)
          )
        }

        // Delete file
        IconButton(
          onClick = { onStartDeleteFile(file) },
          modifier = Modifier.size(22.dp).testTag("file_delete_${file.name}")
        ) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete File",
            tint = VsCodeTextMuted,
            modifier = Modifier.size(13.dp)
          )
        }
      }
    }
  }
}
