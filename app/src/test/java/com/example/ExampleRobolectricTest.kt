package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.models.CodeFile
import com.example.models.ProjectFolder
import com.example.storage.LocalStorageManager
import com.example.syntax.SyntaxHighlighter
import com.example.ui.theme.SyntaxAttr
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxDoctype
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxProperty
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxTag
import com.example.viewmodel.AutoSaveStatus
import com.example.viewmodel.StudioCodeViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Easy Code", appName)
  }

  @Test
  fun `test bundle standalone html includes css and js`() {
    val vm = StudioCodeViewModel()
    val bundled = vm.generateStandaloneHtml()
    assertTrue(bundled.contains("<style>"))
    assertTrue(bundled.contains("<script>"))
    assertTrue(bundled.contains("Studio Code"))
  }

  @Test
  fun `test add custom extension`() {
    val vm = StudioCodeViewModel()
    vm.addCustomExtension(
      name = "React JSX Helpers",
      author = "Developer",
      description = "Fast JSX snippets",
      category = "Snippets",
      snippet = "const App = () => <div>Hello</div>;"
    )
    val installed = vm.uiState.value.installedExtensions
    val custom = installed.find { it.name == "React JSX Helpers" }
    assertNotNull(custom)
    assertTrue(custom!!.isCustom)
  }

  @Test
  fun `test local storage manager save and load`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = LocalStorageManager(context)
    storage.clearStorage()

    val testFiles = listOf(
      CodeFile(id = "app.js", name = "app.js", extension = "js", content = "console.log('test');"),
      CodeFile(id = "index.html", name = "index.html", extension = "html", content = "<h1>Title</h1>")
    )
    val testFolders = listOf(
      ProjectFolder(id = "src", name = "src", isExpanded = true)
    )

    val saved = storage.saveProject(
      files = testFiles,
      folders = testFolders,
      activeFileId = "app.js",
      openTabIds = listOf("app.js")
    )
    assertTrue(saved)
    assertTrue(storage.hasPersistedData())

    val loaded = storage.loadProject()
    assertNotNull(loaded)
    assertEquals(2, loaded!!.files.size)
    assertEquals("app.js", loaded.activeFileId)
    val jsFile = loaded.files.find { it.id == "app.js" }
    assertEquals("console.log('test');", jsFile?.content)
  }

  @Test
  fun `test auto save status transitions and manual save`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val vm = StudioCodeViewModel()
    vm.initLocalStorage(context)
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    // Editing file triggers UNSAVED auto-save status
    vm.updateActiveFileContent("<h1>Updated Auto-Save Test</h1>")
    assertEquals(AutoSaveStatus.UNSAVED, vm.uiState.value.autoSaveStatus)

    // Immediate save transitions to SAVED
    vm.performSave()
    org.robolectric.shadows.ShadowLooper.idleMainLooper()
    assertEquals(AutoSaveStatus.SAVED, vm.uiState.value.autoSaveStatus)

    // Toggle auto save
    assertTrue(vm.uiState.value.isAutoSaveEnabled)
    vm.toggleAutoSave()
    assertFalse(vm.uiState.value.isAutoSaveEnabled)
    vm.toggleAutoSave()
    assertTrue(vm.uiState.value.isAutoSaveEnabled)
  }

  @Test
  fun `test html syntax highlighting with doctype tags attributes and comments`() {
    val html = """
      <!DOCTYPE html>
      <!-- Main application container -->
      <div id="app" class="container">
        <h1>Title</h1>
      </div>
    """.trimIndent()

    val annotated = SyntaxHighlighter.highlight(html, "html")
    assertNotNull(annotated)
    assertTrue(annotated.text.contains("<!DOCTYPE html>"))
    // Ensure styles were attached
    val styles = annotated.spanStyles
    assertTrue("Should contain multiple syntax color spans", styles.isNotEmpty())

    // Check DOCTYPE style
    val hasDoctype = styles.any { it.item.color == SyntaxDoctype }
    assertTrue("Should highlight doctype", hasDoctype)

    // Check tag style
    val hasTag = styles.any { it.item.color == SyntaxTag }
    assertTrue("Should highlight HTML tags", hasTag)

    // Check comment style
    val hasComment = styles.any { it.item.color == SyntaxComment }
    assertTrue("Should highlight HTML comments", hasComment)

    // Check attribute style
    val hasAttr = styles.any { it.item.color == SyntaxAttr }
    assertTrue("Should highlight attributes", hasAttr)
  }

  @Test
  fun `test css syntax highlighting with selectors properties and values`() {
    val css = """
      /* Base Theme styles */
      .card {
        background-color: #1e1e1e;
        color: #ffffff;
        padding: 16px;
        border-radius: 8px;
        --custom-accent: #d0bcff;
      }
    """.trimIndent()

    val annotated = SyntaxHighlighter.highlight(css, "css")
    assertNotNull(annotated)
    val styles = annotated.spanStyles
    assertTrue("Should contain syntax styles", styles.isNotEmpty())

    // Check comments
    val hasComment = styles.any { it.item.color == SyntaxComment }
    assertTrue("Should highlight CSS comments", hasComment)

    // Check properties
    val hasProperty = styles.any { it.item.color == SyntaxProperty }
    assertTrue("Should highlight CSS properties", hasProperty)

    // Check numbers and units
    val hasNumber = styles.any { it.item.color == SyntaxNumber }
    assertTrue("Should highlight numbers and units", hasNumber)
  }

  @Test
  fun `test javascript syntax highlighting with keywords functions and strings`() {
    val js = """
      // Calculates total
      const computeTotal = (items) => {
        console.log("Processing items:", items);
        let total = 42;
        return total;
      };
    """.trimIndent()

    val annotated = SyntaxHighlighter.highlight(js, "javascript")
    assertNotNull(annotated)
    val styles = annotated.spanStyles
    assertTrue("Should contain syntax styles", styles.isNotEmpty())

    // Check keywords (const, let, return)
    val hasKeyword = styles.any { it.item.color == SyntaxKeyword }
    assertTrue("Should highlight JS keywords", hasKeyword)

    // Check comment
    val hasComment = styles.any { it.item.color == SyntaxComment }
    assertTrue("Should highlight JS comments", hasComment)

    // Check strings
    val hasString = styles.any { it.item.color == SyntaxString }
    assertTrue("Should highlight JS strings", hasString)

    // Check numbers
    val hasNumber = styles.any { it.item.color == SyntaxNumber }
    assertTrue("Should highlight JS numbers", hasNumber)
  }

  @Test
  fun `test file explorer create new file in workspace`() {
    val vm = StudioCodeViewModel()
    val initialFileCount = vm.uiState.value.files.size
    vm.createNewFile("utils.js")

    val state = vm.uiState.value
    assertEquals(initialFileCount + 1, state.files.size)
    val created = state.files.find { it.name == "utils.js" }
    assertNotNull(created)
    assertEquals("js", created!!.extension)
    assertTrue(created.content.contains("utils.js loaded"))
    assertEquals("utils.js", state.activeFileId)
    assertTrue(state.openTabIds.contains("utils.js"))
    assertEquals("Created utils.js", state.snackbarMessage)
  }

  @Test
  fun `test file explorer rename file in workspace`() {
    val vm = StudioCodeViewModel()
    vm.createNewFile("component.html")
    assertEquals("component.html", vm.uiState.value.activeFileId)

    vm.renameFile("component.html", "widget.html")
    val state = vm.uiState.value
    assertFalse(state.files.any { it.name == "component.html" })
    val renamed = state.files.find { it.name == "widget.html" }
    assertNotNull(renamed)
    assertEquals("widget.html", renamed!!.id)
    assertEquals("widget.html", state.activeFileId)
    assertTrue(state.openTabIds.contains("widget.html"))
    assertFalse(state.openTabIds.contains("component.html"))
    assertEquals("Renamed to widget.html", state.snackbarMessage)
  }

  @Test
  fun `test file explorer delete file in workspace`() {
    val vm = StudioCodeViewModel()
    vm.createNewFile("temporary.txt")
    assertTrue(vm.uiState.value.files.any { it.id == "temporary.txt" })
    assertEquals("temporary.txt", vm.uiState.value.activeFileId)

    val countBeforeDelete = vm.uiState.value.files.size
    vm.deleteFile("temporary.txt")
    val state = vm.uiState.value
    assertEquals(countBeforeDelete - 1, state.files.size)
    assertFalse(state.files.any { it.id == "temporary.txt" })
    assertFalse(state.openTabIds.contains("temporary.txt"))
    assertTrue(state.activeFileId.isNotEmpty())
    assertFalse(state.activeFileId == "temporary.txt")
    assertEquals("Deleted temporary.txt", state.snackbarMessage)
  }

  @Test
  fun `test file explorer prevents deleting last remaining file`() {
    val vm = StudioCodeViewModel()
    // Delete files until only 1 remains
    while (vm.uiState.value.files.size > 1) {
      val fileToDelete = vm.uiState.value.files.first()
      vm.deleteFile(fileToDelete.id)
    }

    assertEquals(1, vm.uiState.value.files.size)
    val lastFileId = vm.uiState.value.files.first().id
    vm.deleteFile(lastFileId)

    // Verify file was NOT deleted
    assertEquals(1, vm.uiState.value.files.size)
    assertEquals(lastFileId, vm.uiState.value.files.first().id)
    assertTrue(vm.uiState.value.snackbarMessage?.contains("Cannot delete the only file") == true)
  }

  @Test
  fun `test file explorer folder creation and nested file operations`() {
    val vm = StudioCodeViewModel()
    vm.createNewFolder("components")
    val folder = vm.uiState.value.folders.find { it.name == "components" }
    assertNotNull(folder)

    vm.createNewFile("Button.js", parentFolderId = folder!!.id)
    val nestedFile = vm.uiState.value.files.find { it.name == "Button.js" }
    assertNotNull(nestedFile)
    assertEquals(folder.id, nestedFile!!.parentFolderId)

    // Deleting folder should clean up child folder and nested file
    vm.deleteFolder(folder.id)
    assertFalse(vm.uiState.value.folders.any { it.id == folder.id })
    assertFalse(vm.uiState.value.files.any { it.name == "Button.js" })
  }

  @Test
  fun `test live html preview toggle and layout orientation`() {
    val vm = StudioCodeViewModel()
    assertFalse(vm.uiState.value.isLivePreviewOpen)

    vm.toggleLivePreview()
    assertTrue(vm.uiState.value.isLivePreviewOpen)

    val initialOrientation = vm.uiState.value.previewSplitOrientation
    vm.togglePreviewOrientation()
    val toggledOrientation = vm.uiState.value.previewSplitOrientation
    assertTrue(initialOrientation != toggledOrientation)

    vm.toggleLivePreview(false)
    assertFalse(vm.uiState.value.isLivePreviewOpen)
  }

  @Test
  fun `test live preview html updates when active content changes`() {
    val vm = StudioCodeViewModel()
    vm.toggleLivePreview(true)
    assertTrue(vm.uiState.value.isLivePreviewOpen)

    vm.updateActiveFileContent("<h1>Live Preview Test Header</h1>")
    org.robolectric.shadows.ShadowLooper.idleMainLooper()

    val previewHtml = vm.uiState.value.livePreviewHtml
    assertTrue("Preview should contain newly typed content", previewHtml.contains("Live Preview Test Header"))
  }

  @Test
  fun `test global file search toggle and quick open command`() {
    val vm = StudioCodeViewModel()
    assertFalse(vm.uiState.value.showGlobalFileSearch)

    vm.toggleGlobalFileSearch(true)
    assertTrue(vm.uiState.value.showGlobalFileSearch)

    vm.toggleGlobalFileSearch(false)
    assertFalse(vm.uiState.value.showGlobalFileSearch)

    // Trigger via registered quickOpen command
    vm.executeCommand("workbench.action.quickOpen")
    assertTrue(vm.uiState.value.showGlobalFileSearch)
  }

  @Test
  fun `test open file from global search updates active file and recent list`() {
    val vm = StudioCodeViewModel()
    assertEquals("index.html", vm.uiState.value.activeFileId)

    vm.toggleGlobalFileSearch(true)
    assertTrue(vm.uiState.value.showGlobalFileSearch)

    vm.openFileFromGlobalSearch("style.css")
    assertEquals("style.css", vm.uiState.value.activeFileId)
    assertFalse(vm.uiState.value.showGlobalFileSearch)
    assertTrue(vm.uiState.value.openTabIds.contains("style.css"))
    assertEquals("style.css", vm.uiState.value.recentFileIds.first())
  }

  @Test
  fun `test create and open file from search overlay`() {
    val vm = StudioCodeViewModel()
    assertFalse(vm.uiState.value.files.any { it.name == "app.component.html" })

    vm.createAndOpenFileFromSearch("app.component.html")
    assertTrue(vm.uiState.value.files.any { it.name == "app.component.html" })
    assertEquals("app.component.html", vm.uiState.value.activeFileId)
    assertFalse(vm.uiState.value.showGlobalFileSearch)
  }

  @Test
  fun `test file path resolution helper for search overlay`() {
    val rootFile = CodeFile(id = "root.html", name = "root.html", extension = "html", content = "")
    val folder1 = ProjectFolder(id = "f1", name = "src", parentFolderId = null)
    val folder2 = ProjectFolder(id = "f2", name = "components", parentFolderId = "f1")
    val nestedFile = CodeFile(id = "comp.js", name = "comp.js", extension = "js", parentFolderId = "f2", content = "")

    val folders = listOf(folder1, folder2)
    assertEquals("root", com.example.ui.dialogs.getFileDirectoryPath(rootFile, folders))
    assertEquals("root.html", com.example.ui.dialogs.getFileFullPath(rootFile, folders))

    assertEquals("src/components", com.example.ui.dialogs.getFileDirectoryPath(nestedFile, folders))
    assertEquals("src/components/comp.js", com.example.ui.dialogs.getFileFullPath(nestedFile, folders))
  }
}

