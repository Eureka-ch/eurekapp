/*
 * This test file was co-authored by Claude Code (Anthropic AI assistant).
 */
package ch.eureka.eurekapp.model.data.file

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import java.io.File
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test suite for FileStorageRepository implementation.
 *
 * This test class verifies the Firebase Storage repository operations including uploading,
 * deleting, and retrieving metadata for files. Tests use temporary files created in the app's cache
 * directory and require the Firebase emulator to be running.
 */
class FirebaseFileStorageRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: FirebaseFileStorageRepository
  private val tempFiles = mutableListOf<File>()

  override fun getCollectionPaths(): List<String> {
    return listOf("projects")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirebaseFileStorageRepository(
            storage = FirebaseEmulator.storage, auth = FirebaseEmulator.auth)
  }

  @After
  override fun tearDown() = runBlocking {
    // Clean up temporary files
    tempFiles.forEach { it.delete() }
    tempFiles.clear()

    // Clear storage emulator data
    // Note: We call this directly here rather than in base class because
    // storage emulator doesn't have a simple clear endpoint like Firestore
    FirebaseEmulator.clearStorageEmulator()

    super.tearDown()
  }

  private fun stripDownloadUrl(downloadUrl: String) =
      downloadUrl.substringAfter("http://10.0.2.2:9199/v0/b/eureka-app-ch.firebasestorage.app/o/")

  private fun getStoragePath(downloadUrl: String) =
      stripDownloadUrl(downloadUrl).substringBefore("?alt=media").replace("%2F", "/")

  private fun getToken(downloadUrl: String) =
      stripDownloadUrl(downloadUrl).substringAfter("?alt=media&token=")

  private fun downloadFile(downloadUrl: String): String {
    val client = okhttp3.OkHttpClient()
    val request = okhttp3.Request.Builder().url(downloadUrl).build()
    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) throw Exception("Failed to download file: $response")
      return response.body?.string() ?: throw Exception("Empty response body")
    }
  }

  private fun compareFileContent(original: String, downloadUrl: String) {
    val downloadedContent = downloadFile(downloadUrl)
    assertEquals(original, downloadedContent)
  }
  /**
   * Create a temporary test file with specified content.
   *
   * @param filename The name of the file
   * @param extension The file extension (e.g., "txt", "jpg")
   * @param content The content to write to the file
   * @return Uri pointing to the created file
   */
  private fun createTempFile(filename: String, extension: String, content: String): Uri {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    val tempFile = File.createTempFile(filename, ".$extension", context.cacheDir)
    tempFile.writeText(content)
    tempFiles.add(tempFile)
    return Uri.fromFile(tempFile)
  }

  @Test
  fun uploadFile_shouldUploadAndReturnDownloadUrl() = runBlocking {
    val projectId = "test_project_1"
    setupTestProject(projectId)

    val testContent = "Test file content for upload"
    val fileUri = createTempFile("test_upload", "txt", testContent)
    val expectedPath = StoragePaths.taskAttachmentPath(projectId, "task1", "test.txt")

    val result = repository.uploadFile(storagePath = expectedPath, fileUri = fileUri)

    val downloadUrl = result.getOrNull()
    assertNotNull(downloadUrl)

    // Verify URL format
    assertTrue(
        "Expected storage URL but got: $downloadUrl",
        downloadUrl!!.startsWith("http://10.0.2.2:9199/v0/b/eureka-app-ch.firebasestorage.app/o/"))

    // Verify storage path is correct
    val storagePath = getStoragePath(downloadUrl)
    assertEquals("Storage path mismatch", expectedPath, storagePath)

    // Verify token is present
    val token = getToken(downloadUrl)
    assertTrue("Download URL should contain token", token.isNotEmpty())

    // Verify file content by downloading
    compareFileContent(testContent, downloadUrl)
  }

  @Test
  fun uploadFile_shouldFailWhenNotAuthenticated() = runBlocking {
    // Sign out to test unauthenticated access
    FirebaseEmulator.auth.signOut()

    val fileUri = createTempFile("test_unauth", "txt", "Test content")

    val result =
        repository.uploadFile(
            storagePath = StoragePaths.userFilePath("user1", "test.txt"), fileUri = fileUri)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun deleteFile_shouldDeleteFileSuccessfully() = runBlocking {
    val projectId = "test_project_2"
    setupTestProject(projectId)

    val testContent = "File to be deleted"
    val fileUri = createTempFile("test_delete", "txt", testContent)

    val uploadResult =
        repository.uploadFile(
            storagePath = StoragePaths.projectFilePath(projectId, "delete_test.txt"),
            fileUri = fileUri)
    assertTrue(uploadResult.isSuccess)
    val downloadUrl = uploadResult.getOrNull()!!

    compareFileContent(testContent, downloadUrl)

    val storagePath = getStoragePath(downloadUrl)
    assertEquals("projects/$projectId/delete_test.txt", storagePath)

    val deleteResult = repository.deleteFile(downloadUrl)
    assertTrue(
        "Delete should succeed: ${deleteResult.exceptionOrNull()?.message}", deleteResult.isSuccess)
    val metadataResult = repository.getFileMetadata(downloadUrl)
    assertTrue("Metadata should fail after deletion", metadataResult.isFailure)

    // Verify file is no longer downloadable
    try {
      downloadFile(downloadUrl)
      throw AssertionError("File should not be downloadable after deletion")
    } catch (e: Exception) {
      // Expected - file should not be accessible
      assertTrue(e.message?.contains("Failed to download") == true)
    }
  }

  @Test
  fun deleteFile_shouldFailForInvalidUrl() = runBlocking {
    val invalidUrl = "https://invalid-url.com/file.txt"

    val result = repository.deleteFile(invalidUrl)

    assertTrue(result.isFailure)
  }

  @Test
  fun getFileMetadata_shouldReturnMetadataForExistingFile() = runBlocking {
    val projectId = "test_project_3"
    setupTestProject(projectId)

    val testContent = "Test metadata content"
    val fileUri = createTempFile("test_metadata", "txt", testContent)
    val expectedPath = StoragePaths.meetingAttachmentPath(projectId, "meeting1", "notes.txt")

    // Upload file first
    val uploadResult = repository.uploadFile(storagePath = expectedPath, fileUri = fileUri)
    assertTrue(uploadResult.isSuccess)
    val downloadUrl = uploadResult.getOrNull()!!

    // Verify file is accessible
    compareFileContent(testContent, downloadUrl)

    // Get metadata
    val metadataResult = repository.getFileMetadata(downloadUrl)
    assertTrue(
        "Get metadata failed: ${metadataResult.exceptionOrNull()?.message}",
        metadataResult.isSuccess)

    val metadata = metadataResult.getOrNull()!!
    assertNotNull("Metadata name should not be null", metadata.name)
    assertTrue("File size should be greater than 0", metadata.sizeBytes > 0)
    assertEquals("File size should match content", testContent.length.toLong(), metadata.sizeBytes)
    assertEquals("Content type should be text/plain", "text/plain", metadata.contentType)

    // Verify metadata path matches upload path
    val storagePath = getStoragePath(downloadUrl)
    assertEquals("Storage path should match", expectedPath, storagePath)
  }

  @Test
  fun getFileMetadata_shouldFailForNonExistentFile() = runBlocking {
    // Use a valid-looking but non-existent URL
    val fakeUrl =
        "https://firebasestorage.googleapis.com/v0/b/test.appspot.com/o/nonexistent.txt?alt=media"

    val result = repository.getFileMetadata(fakeUrl)

    assertTrue(result.isFailure)
  }

  @Test
  fun uploadFile_shouldHandleDifferentFileTypes() = runBlocking {
    val projectId = "test_project_7"
    setupTestProject(projectId)

    // Test text file
    val txtUri = createTempFile("test", "txt", "Text content")
    val txtResult =
        repository.uploadFile(
            storagePath = StoragePaths.projectFilePath(projectId, "test.txt"), fileUri = txtUri)
    assertTrue(txtResult.isSuccess)

    // Test PDF file (simulated)
    val pdfUri = createTempFile("document", "pdf", "PDF content")
    val pdfResult =
        repository.uploadFile(
            storagePath = StoragePaths.projectFilePath(projectId, "doc.pdf"), fileUri = pdfUri)
    assertTrue(pdfResult.isSuccess)

    // Test image file (simulated)
    val imgUri = createTempFile("image", "jpg", "Image content")
    val imgResult =
        repository.uploadFile(
            storagePath = StoragePaths.projectFilePath(projectId, "img.jpg"), fileUri = imgUri)
    assertTrue(imgResult.isSuccess)
  }

  @Test
  fun uploadFile_shouldHandleSpecialCharactersInFilename() = runBlocking {
    val projectId = "test_project_8"
    setupTestProject(projectId)

    val fileUri = createTempFile("special", "txt", "Content with special chars")
    val filename = "my document (v2).txt"

    val result =
        repository.uploadFile(
            storagePath = StoragePaths.projectFilePath(projectId, filename), fileUri = fileUri)

    assertTrue(result.isSuccess)
    val downloadUrl = result.getOrNull()!!
    assertNotNull(downloadUrl)
  }

  @Test
  fun uploadAndDelete_multipleFiles_shouldWork() = runBlocking {
    val projectId = "test_project_9"
    setupTestProject(projectId)

    val downloadUrls = mutableListOf<String>()

    // Upload multiple files
    for (i in 1..3) {
      val fileUri = createTempFile("file$i", "txt", "Content $i")
      val result =
          repository.uploadFile(
              storagePath = StoragePaths.projectFilePath(projectId, "file$i.txt"),
              fileUri = fileUri)
      assertTrue(result.isSuccess)
      downloadUrls.add(result.getOrNull()!!)
    }

    // Verify all files exist
    downloadUrls.forEach { url ->
      val metadataResult = repository.getFileMetadata(url)
      assertTrue(metadataResult.isSuccess)
    }

    // Delete all files
    downloadUrls.forEach { url ->
      val deleteResult = repository.deleteFile(url)
      assertTrue(deleteResult.isSuccess)
    }

    // Verify all files are deleted
    downloadUrls.forEach { url ->
      val metadataResult = repository.getFileMetadata(url)
      assertTrue(metadataResult.isFailure)
    }
  }
}
