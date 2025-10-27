package ch.eureka.eurekapp.model.data.audio

import android.net.Uri
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import com.google.firebase.storage.StorageMetadata

class MockedStorageRepository : FileStorageRepository {
  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
    return Result.success("test")
  }

  override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
    return Result.success(Unit)
  }

  override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
    return Result.failure(RuntimeException(""))
  }
}

class ErrorMockedStorageRepository : FileStorageRepository {
  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
    return Result.failure(RuntimeException(""))
  }

  override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
    return Result.success(Unit)
  }

  override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
    return Result.failure(RuntimeException(""))
  }
}
