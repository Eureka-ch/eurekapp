package ch.eureka.eurekapp.model.data.file

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FileStorageRepositoryDummyTest {

  private class DummyFileStorageRepository : FileStorageRepository {
    override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
      return Result.success("https://mock-download-url.com/$storagePath")
    }

    override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
      val metadata = StorageMetadata.Builder().build()
      return Result.success(metadata)
    }
  }

  private val repository = DummyFileStorageRepository()
  private val mockUri = Uri.parse("content://mock/file")

  @Test
  fun uploadUserFile_delegatesToUploadFile_returnsSuccess() = runTest {
    val userId = "user123"
    val filename = "profile.jpg"

    val result = repository.uploadUserFile(userId, mockUri, filename)

    assertTrue(result.isSuccess)
    assertEquals("https://mock-download-url.com/users/user123/profile.jpg", result.getOrNull())
  }

  @Test
  fun uploadProjectFile_delegatesToUploadFile_returnsSuccess() = runTest {
    val projectId = "project456"
    val filename = "document.pdf"

    val result = repository.uploadProjectFile(projectId, mockUri, filename)

    assertTrue(result.isSuccess)
    assertEquals(
        "https://mock-download-url.com/projects/project456/document.pdf", result.getOrNull())
  }

  @Test
  fun uploadTaskFile_delegatesToUploadFile_returnsSuccess() = runTest {
    val projectId = "project789"
    val taskId = "task123"
    val filename = "screenshot.png"

    val result = repository.uploadTaskFile(projectId, taskId, mockUri, filename)

    assertTrue(result.isSuccess)
    assertEquals(
        "https://mock-download-url.com/projects/project789/tasks/task123/screenshot.png",
        result.getOrNull())
  }

  @Test
  fun uploadMeetingFile_delegatesToUploadFile_returnsSuccess() = runTest {
    val projectId = "projectABC"
    val meetingId = "meetingXYZ"
    val filename = "notes.txt"

    val result = repository.uploadMeetingFile(projectId, meetingId, mockUri, filename)

    assertTrue(result.isSuccess)
    assertEquals(
        "https://mock-download-url.com/projects/projectABC/meetings/meetingXYZ/notes.txt",
        result.getOrNull())
  }

  @Test
  fun deleteFile_returnsSuccess() = runTest {
    val downloadUrl = "https://mock-url.com/file.txt"

    val result = repository.deleteFile(downloadUrl)

    assertTrue(result.isSuccess)
  }

  @Test
  fun getFileMetadata_returnsMetadata() = runTest {
    val downloadUrl = "https://mock-url.com/file.txt"

    val result = repository.getFileMetadata(downloadUrl)

    assertTrue(result.isSuccess)
    // Metadata exists but may have null/empty fields in mock
    assertTrue(result.getOrNull() != null)
  }
}
