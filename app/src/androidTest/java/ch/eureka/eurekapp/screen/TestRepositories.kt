package ch.eureka.eurekapp.screen

import android.net.Uri
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import com.google.firebase.storage.StorageMetadata

/**
 * Shared fake repositories for Create/Edit Task screen tests. Extracted to avoid duplication across
 * test files.
 */

/** Fake file repository for testing file operations */
class FakeFileRepository : FileStorageRepository {
  val deletedFiles = mutableListOf<String>()

  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
    return Result.success("https://fakeurl.com/file.jpg")
  }

  override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
    deletedFiles.add(downloadUrl)
    return Result.success(Unit)
  }

  override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
    return Result.success(StorageMetadata.Builder().setContentType("image/jpeg").build())
  }
}
