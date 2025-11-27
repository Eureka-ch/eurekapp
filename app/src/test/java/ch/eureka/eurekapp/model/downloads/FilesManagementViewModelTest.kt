package ch.eureka.eurekapp.model.downloads

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class FilesManagementViewModelTest {

  private lateinit var dao: DownloadedFileDao
  private lateinit var application: Application
  private lateinit var viewModel: FilesManagementViewModel
  private val testDispatcher = UnconfinedTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    dao = mockk(relaxed = true)
    application = mockk(relaxed = true)
    every { application.packageName } returns "ch.eureka.eurekapp"
    every { application.applicationContext } returns application
    mockkStatic(FileProvider::class)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun filesManagementViewModel_uiStateInitiallyEmpty() = runTest {
    coEvery { dao.getAll() } returns flowOf(emptyList())
    viewModel = FilesManagementViewModel(dao, application)

    val state = viewModel.uiState.first()
    assertTrue(state.files.isEmpty())
  }

  @Test
  fun filesManagementViewModel_processFileHandlesRegularFile() = runTest {
    val downloadedFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "content://downloads/test.pdf")
    coEvery { dao.getAll() } returns flowOf(listOf(downloadedFile))

    viewModel = FilesManagementViewModel(dao, application)

    val state = viewModel.uiState.first()
    assertEquals(1, state.files.size)
    assertFalse(state.files[0].isImage)
  }

  @Test
  fun filesManagementViewModel_processFileHandlesImageFile() = runTest {
    val downloadedFile =
        DownloadedFile(
            url = "http://example.com/photo.jpg",
            fileName = "photo.jpg",
            localPath = "content://downloads/photo.jpg")
    coEvery { dao.getAll() } returns flowOf(listOf(downloadedFile))

    viewModel = FilesManagementViewModel(dao, application)

    val state = viewModel.uiState.first()
    assertEquals(1, state.files.size)
    assertTrue(state.files[0].isImage)
  }

  @Test
  fun filesManagementViewModel_processFileHandlesContentUri() = runTest {
    val downloadedFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "content://downloads/test.pdf")
    coEvery { dao.getAll() } returns flowOf(listOf(downloadedFile))

    viewModel = FilesManagementViewModel(dao, application)

    val state = viewModel.uiState.first()
    assertEquals(1, state.files.size)
    assertEquals("test.pdf", state.files[0].displayName)
  }

  @Test
  fun filesManagementViewModel_deleteFileCallsDaoDelete() = runTest {
    coEvery { dao.getAll() } returns flowOf(emptyList())
    viewModel = FilesManagementViewModel(dao, application)

    val file =
        DownloadedFile(
            url = "http://example.com/test.pdf", localPath = "/path", fileName = "test.pdf")
    val fileItem = FileItem(file = file, displayName = "test.pdf", isImage = false, uri = mockk())

    viewModel.deleteFile(fileItem) {}

    coVerify { dao.delete(file) }
  }

  @Test
  fun filesManagementViewModel_displayNameDecodesUrlEncodedNames() = runTest {
    val downloadedFile =
        DownloadedFile(
            url = "http://example.com/my%20file.pdf",
            fileName = "my%20file.pdf",
            localPath = "content://downloads/file.pdf")
    coEvery { dao.getAll() } returns flowOf(listOf(downloadedFile))

    viewModel = FilesManagementViewModel(dao, application)

    val state = viewModel.uiState.first()
    assertEquals("my file.pdf", state.files[0].displayName)
  }

  @Test
  fun filesManagementViewModel_recognizesVariousImageFormats() = runTest {
    val imageFormats = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp")

    imageFormats.forEach { format ->
      val file =
          DownloadedFile(
              url = "http://example.com/image.$format",
              localPath = "content://downloads/image.$format",
              fileName = "image.$format")
      coEvery { dao.getAll() } returns flowOf(listOf(file))
      viewModel = FilesManagementViewModel(dao, application)

      val state = viewModel.uiState.first()
      assertTrue("$format should be recognized as image", state.files[0].isImage)
    }
  }

  @Test
  fun filesManagementViewModel_getOpenFileIntentReturnsCorrectIntentForPdf() = runTest {
    coEvery { dao.getAll() } returns flowOf(emptyList())
    val mimeTypeResolver = { extension: String ->
      if (extension == "pdf") "application/pdf" else null
    }
    viewModel = FilesManagementViewModel(dao, application, mimeTypeResolver)

    val file =
        DownloadedFile(
            url = "http://example.com/test.pdf", localPath = "/path", fileName = "test.pdf")
    val fileItem =
        FileItem(
            file = file,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("content://example.com/test.pdf"))

    val result = viewModel.getOpenFileIntent(fileItem)

    assertEquals(fileItem.uri, result.data)
    assertEquals("application/pdf", result.type)
    assertEquals(Intent.ACTION_VIEW, result.action)
    assertTrue(result.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
  }

  @Test
  fun filesManagementViewModel_getOpenFileIntentReturnsIntentForUnknownFile() = runTest {
    coEvery { dao.getAll() } returns flowOf(emptyList())
    val mimeTypeResolver = { extension: String ->
      if (extension == "unknown") null else "application/pdf"
    }
    viewModel = FilesManagementViewModel(dao, application, mimeTypeResolver)

    val file =
        DownloadedFile(
            url = "http://example.com/test.unknown", localPath = "/path", fileName = "test.unknown")
    val fileItem =
        FileItem(
            file = file,
            displayName = "test.unknown",
            isImage = false,
            uri = Uri.parse("content://example.com/test.unknown"))

    val result = viewModel.getOpenFileIntent(fileItem)

    assertEquals(fileItem.uri, result.data)
    assertEquals("*/*", result.type)
    assertEquals(Intent.ACTION_VIEW, result.action)
    assertTrue(result.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0)
  }
}
