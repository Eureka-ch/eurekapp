package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import android.os.ParcelFileDescriptor
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import com.google.firebase.storage.StorageMetadata

/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/

class MockFileStorageRepository : FileStorageRepository {
  private var uploadFileResult: Result<String> = Result.success("https://mock-url.com/file.jpg")
  private var deleteFileResult: Result<Unit> = Result.success(Unit)

  val uploadFileCalls = mutableListOf<Pair<String, Uri>>()
  val uploadFileDescriptorCalls = mutableListOf<Pair<String, ParcelFileDescriptor>>()
  val deleteFileCalls = mutableListOf<String>()

  fun setUploadFileResult(result: Result<String>) {
    uploadFileResult = result
  }

  fun setDeleteFileResult(result: Result<Unit>) {
    deleteFileResult = result
  }

  fun reset() {
    uploadFileResult = Result.success("https://mock-url.com/file.jpg")
    deleteFileResult = Result.success(Unit)
    uploadFileCalls.clear()
    uploadFileDescriptorCalls.clear()
    deleteFileCalls.clear()
  }

  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
    uploadFileCalls.add(Pair(storagePath, fileUri))
    return uploadFileResult
  }

  override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
    deleteFileCalls.add(downloadUrl)
    return deleteFileResult
  }

  override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
    throw NotImplementedError("getFileMetadata not implemented in mock")
  }

  override suspend fun uploadFile(
      storagePath: String,
      fileDescriptor: ParcelFileDescriptor
  ): Result<String> {
    uploadFileDescriptorCalls.add(Pair(storagePath, fileDescriptor))
    return uploadFileResult
  }
}
