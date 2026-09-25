package com.example.storage

import android.content.Context
import android.util.Log
import com.example.models.CodeFile
import com.example.models.ProjectFolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class SavedProject(
  val files: List<CodeFile>,
  val folders: List<ProjectFolder>,
  val activeFileId: String,
  val openTabIds: List<String>,
  val lastSavedTimestamp: Long
)

class LocalStorageManager(private val context: Context) {

  private val storageDir: File by lazy {
    File(context.filesDir, "workspace_storage").apply {
      if (!exists()) {
        mkdirs()
      }
    }
  }

  private val metaFile: File by lazy {
    File(storageDir, "project_metadata.json")
  }

  private val filesDir: File by lazy {
    File(storageDir, "files").apply {
      if (!exists()) {
        mkdirs()
      }
    }
  }

  fun hasPersistedData(): Boolean {
    return metaFile.exists() && metaFile.length() > 0
  }

  suspend fun saveProject(
    files: List<CodeFile>,
    folders: List<ProjectFolder>,
    activeFileId: String,
    openTabIds: List<String>
  ): Boolean = withContext(Dispatchers.IO) {
    try {
      // 1. Save each individual file content to disk
      for (file in files) {
        val diskFile = File(filesDir, file.id)
        diskFile.writeText(file.content, Charsets.UTF_8)
      }

      // Remove orphaned files on disk if any were deleted
      val existingIds = files.map { it.id }.toSet()
      filesDir.listFiles()?.forEach { diskFile ->
        if (diskFile.isFile && !existingIds.contains(diskFile.name)) {
          diskFile.delete()
        }
      }

      // 2. Build metadata JSON
      val rootJson = JSONObject()
      rootJson.put("version", 1)
      rootJson.put("activeFileId", activeFileId)
      rootJson.put("lastSavedTimestamp", System.currentTimeMillis())

      val tabsArray = JSONArray()
      openTabIds.forEach { tabsArray.put(it) }
      rootJson.put("openTabIds", tabsArray)

      val filesArray = JSONArray()
      for (f in files) {
        val fObj = JSONObject()
        fObj.put("id", f.id)
        fObj.put("name", f.name)
        fObj.put("extension", f.extension)
        fObj.put("parentFolderId", f.parentFolderId ?: JSONObject.NULL)
        fObj.put("isReadOnly", f.isReadOnly)
        filesArray.put(fObj)
      }
      rootJson.put("files", filesArray)

      val foldersArray = JSONArray()
      for (folder in folders) {
        val foldObj = JSONObject()
        foldObj.put("id", folder.id)
        foldObj.put("name", folder.name)
        foldObj.put("parentFolderId", folder.parentFolderId ?: JSONObject.NULL)
        foldObj.put("isExpanded", folder.isExpanded)
        foldersArray.put(foldObj)
      }
      rootJson.put("folders", foldersArray)

      // Atomic write: write to temp file then rename
      val tempMeta = File(storageDir, "project_metadata.json.tmp")
      tempMeta.writeText(rootJson.toString(2), Charsets.UTF_8)
      if (tempMeta.renameTo(metaFile)) {
        Log.d("LocalStorageManager", "Auto-saved ${files.size} files to local storage.")
        true
      } else {
        // Fallback standard write
        metaFile.writeText(rootJson.toString(2), Charsets.UTF_8)
        tempMeta.delete()
        true
      }
    } catch (e: Exception) {
      Log.e("LocalStorageManager", "Failed to save project to local storage", e)
      false
    }
  }

  suspend fun saveSingleFile(file: CodeFile): Boolean = withContext(Dispatchers.IO) {
    try {
      val diskFile = File(filesDir, file.id)
      val tempFile = File(filesDir, "${file.id}.tmp")
      tempFile.writeText(file.content, Charsets.UTF_8)
      if (!tempFile.renameTo(diskFile)) {
        diskFile.writeText(file.content, Charsets.UTF_8)
        tempFile.delete()
      }
      true
    } catch (e: Exception) {
      Log.e("LocalStorageManager", "Failed to save single file ${file.id}", e)
      false
    }
  }

  suspend fun loadProject(): SavedProject? = withContext(Dispatchers.IO) {
    try {
      if (!metaFile.exists()) return@withContext null

      val rawJson = metaFile.readText(Charsets.UTF_8)
      if (rawJson.isBlank()) return@withContext null

      val rootJson = JSONObject(rawJson)
      val activeFileId = rootJson.optString("activeFileId", "index.html")
      val lastSaved = rootJson.optLong("lastSavedTimestamp", System.currentTimeMillis())

      val tabsArray = rootJson.optJSONArray("openTabIds")
      val openTabIds = mutableListOf<String>()
      if (tabsArray != null) {
        for (i in 0 until tabsArray.length()) {
          openTabIds.add(tabsArray.getString(i))
        }
      }

      val foldersArray = rootJson.optJSONArray("folders")
      val folders = mutableListOf<ProjectFolder>()
      if (foldersArray != null) {
        for (i in 0 until foldersArray.length()) {
          val fObj = foldersArray.getJSONObject(i)
          val parentId = if (fObj.isNull("parentFolderId")) null else fObj.optString("parentFolderId")
          folders.add(
            ProjectFolder(
              id = fObj.getString("id"),
              name = fObj.getString("name"),
              parentFolderId = parentId,
              isExpanded = fObj.optBoolean("isExpanded", true)
            )
          )
        }
      }

      val filesArray = rootJson.optJSONArray("files")
      val files = mutableListOf<CodeFile>()
      if (filesArray != null) {
        for (i in 0 until filesArray.length()) {
          val fObj = filesArray.getJSONObject(i)
          val id = fObj.getString("id")
          val name = fObj.getString("name")
          val extension = fObj.optString("extension", "txt")
          val parentId = if (fObj.isNull("parentFolderId")) null else fObj.optString("parentFolderId")
          val isReadOnly = fObj.optBoolean("isReadOnly", false)

          // Read content from disk
          val diskFile = File(filesDir, id)
          val content = if (diskFile.exists()) {
            diskFile.readText(Charsets.UTF_8)
          } else {
            ""
          }

          files.add(
            CodeFile(
              id = id,
              name = name,
              extension = extension,
              content = content,
              isModified = false,
              isReadOnly = isReadOnly,
              parentFolderId = parentId
            )
          )
        }
      }

      if (files.isEmpty()) return@withContext null

      SavedProject(
        files = files,
        folders = folders,
        activeFileId = activeFileId,
        openTabIds = if (openTabIds.isNotEmpty()) openTabIds else listOf(files.first().id),
        lastSavedTimestamp = lastSaved
      )
    } catch (e: Exception) {
      Log.e("LocalStorageManager", "Failed to load project from local storage", e)
      null
    }
  }

  suspend fun clearStorage(): Boolean = withContext(Dispatchers.IO) {
    try {
      storageDir.deleteRecursively()
      storageDir.mkdirs()
      filesDir.mkdirs()
      true
    } catch (e: Exception) {
      Log.e("LocalStorageManager", "Failed to clear local storage", e)
      false
    }
  }
}
