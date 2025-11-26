package ch.eureka.eurekapp.model.downloads

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
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

@OptIn(ExperimentalCoroutinesApi::class)
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
    mockkStatic(Uri::class)
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
    val mockUri = mockk<Uri>()
    every { Uri.decode("test.pdf") } returns "test.pdf"
    every { Uri.parse("content://downloads/test.pdf") } returns mockUri
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
    val mockUri = mockk<Uri>()
    every { Uri.decode("photo.jpg") } returns "photo.jpg"
    every { Uri.parse("content://downloads/photo.jpg") } returns mockUri
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
    val mockUri = mockk<Uri>()
    every { Uri.decode("test.pdf") } returns "test.pdf"
    every { Uri.parse("content://downloads/test.pdf") } returns mockUri
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

    viewModel.deleteFile(fileItem)

    coVerify { dao.delete(file) }
  }

  @Test
  fun filesManagementViewModel_displayNameDecodesUrlEncodedNames() = runTest {
    val downloadedFile =
        DownloadedFile(
            url = "http://example.com/my%20file.pdf",
            fileName = "my%20file.pdf",
            localPath = "content://downloads/file.pdf")
    val mockUri = mockk<Uri>()
    every { Uri.decode("my%20file.pdf") } returns "my file.pdf"
    every { Uri.parse("content://downloads/file.pdf") } returns mockUri
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
      val mockUri = mockk<Uri>()
      every { Uri.decode("image.$format") } returns "image.$format"
      every { Uri.parse("content://downloads/image.$format") } returns mockUri
      coEvery { dao.getAll() } returns flowOf(listOf(file))
      viewModel = FilesManagementViewModel(dao, application)

      val state = viewModel.uiState.first()
      assertTrue("$format should be recognized as image", state.files[0].isImage)
    }
  }

  @Test
  fun filesManagementViewModel_openFileOpensFileWithAvailableApp() = runTest {
    coEvery { dao.getAll() } returns flowOf(emptyList())
    viewModel = FilesManagementViewModel(dao, application)

    val file =
        DownloadedFile(
            url = "http://example.com/test.pdf", localPath = "/path", fileName = "test.pdf")
    val fileItem = FileItem(file = file, displayName = "test.pdf", isImage = false, uri = mockk())
    val mockIntent = mockk<Intent>(relaxed = true)

    mockkStatic(MimeTypeMap::class)
    val mockMimeTypeMap = mockk<MimeTypeMap>()
    every { MimeTypeMap.getSingleton() } returns mockMimeTypeMap
    every { mockMimeTypeMap.getMimeTypeFromExtension("pdf") } returns "application/pdf"
    mockkStatic(Intent::class)
    every { Intent.createChooser(any(), any()) } returns mockIntent

    viewModel.openFile(fileItem)

    verify { application.startActivity(mockIntent) }
  }

  @Test
  fun filesManagementViewModel_openFileShowsToastWhenNoAppFound() = runTest {
    coEvery { dao.getAll() } returns flowOf(emptyList())
    viewModel = FilesManagementViewModel(dao, application)

    val file =
        DownloadedFile(
            url = "http://example.com/test.unknown", localPath = "/path", fileName = "test.unknown")
    val fileItem =
        FileItem(file = file, displayName = "test.unknown", isImage = false, uri = mockk())
    val mockToast = mockk<Toast>(relaxed = true)

    every { application.startActivity(any()) } throws android.content.ActivityNotFoundException()
    mockkStatic(MimeTypeMap::class)
    val mockMimeTypeMap = mockk<MimeTypeMap>()
    every { MimeTypeMap.getSingleton() } returns mockMimeTypeMap
    every { mockMimeTypeMap.getMimeTypeFromExtension("unknown") } returns null
    mockkStatic(Toast::class)
    every { Toast.makeText(any(), any<String>(), any()) } returns mockToast

    viewModel.openFile(fileItem)

    verify { mockToast.show() }
  }
}
