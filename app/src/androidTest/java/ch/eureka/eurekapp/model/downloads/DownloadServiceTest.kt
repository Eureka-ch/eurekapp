// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.model.downloads

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.io.File
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DownloadServiceTest {

  private lateinit var context: Context
  private lateinit var downloadService: DownloadService
  private lateinit var mockWebServer: MockWebServer

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext()
    downloadService = DownloadService(context)
    mockWebServer = MockWebServer()
    mockWebServer.start()
  }

  @After
  fun tearDown() {
    mockWebServer.shutdown()
    // Clean up downloads directory
    val downloadsDir = File(context.cacheDir, "downloads")
    downloadsDir.deleteRecursively()
  }

  @Test
  fun downloadService_successfulDownload() = runBlocking {
    // Given
    val fileContent = "Test file content"
    mockWebServer.enqueue(MockResponse().setBody(fileContent))
    val url = mockWebServer.url("/test-file.txt").toString()
    val fileName = "test-file.txt"

    // When
    val uri = downloadService.downloadFile(url, fileName)

    // Then
    assertNotNull(uri)
    val file = File(context.cacheDir, "downloads/$fileName")
    assertTrue(file.exists())
    assertEquals(fileContent, file.readText())
  }

  @Test
  fun downloadService_createsDownloadsDirectory() = runBlocking {
    // Given
    val downloadsDir = File(context.cacheDir, "downloads")
    downloadsDir.deleteRecursively()
    assertFalse(downloadsDir.exists())

    mockWebServer.enqueue(MockResponse().setBody("content"))
    val url = mockWebServer.url("/file.txt").toString()

    // When
    downloadService.downloadFile(url, "file.txt")

    // Then
    assertTrue(downloadsDir.exists())
    assertTrue(downloadsDir.isDirectory)
  }

  @Test
  fun downloadService_invalidUrl() = runBlocking {
    // Given
    val invalidUrl = "not-a-valid-url"
    val fileName = "test.txt"

    // When
    val uri = downloadService.downloadFile(invalidUrl, fileName)

    // Then
    assertNull(uri)
  }

  @Test
  fun downloadService_serverError() = runBlocking {
    // Given
    mockWebServer.enqueue(MockResponse().setResponseCode(404))
    val url = mockWebServer.url("/nonexistent.txt").toString()
    val fileName = "test.txt"

    // When
    val uri = downloadService.downloadFile(url, fileName)

    // Then
    assertNull(uri)
  }

  @Test
  fun downloadService_emptyFile() = runBlocking {
    // Given
    mockWebServer.enqueue(MockResponse().setBody(""))
    val url = mockWebServer.url("/empty.txt").toString()
    val fileName = "empty.txt"

    // When
    val uri = downloadService.downloadFile(url, fileName)

    // Then
    assertNotNull(uri)
    val file = File(context.cacheDir, "downloads/$fileName")
    assertTrue(file.exists())
    assertEquals(0, file.length())
  }

  @Test
  fun downloadService_overwriteExistingFile() = runBlocking {
    // Given
    val fileName = "overwrite.txt"
    val file = File(context.cacheDir, "downloads/$fileName")
    file.parentFile?.mkdirs()
    file.writeText("old content")

    mockWebServer.enqueue(MockResponse().setBody("new content"))
    val url = mockWebServer.url("/overwrite.txt").toString()

    // When
    val uri = downloadService.downloadFile(url, fileName)

    // Then
    assertNotNull(uri)
    assertEquals("new content", file.readText())
  }
}
