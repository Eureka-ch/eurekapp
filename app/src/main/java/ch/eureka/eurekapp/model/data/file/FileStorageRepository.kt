package ch.eureka.eurekapp.model.data.file

import android.net.Uri
import com.google.firebase.storage.StorageMetadata

/**
 * Repository interface for managing file storage operations.
 *
 * This interface provides methods for uploading, deleting, and retrieving metadata for files stored
 * in Firebase Storage. It includes both low-level operations and high-level convenience methods for
 * common use cases (user files, project files, task attachments, meeting attachments).
 *
 * Implementations should handle Firebase Storage interactions and return Results for error
 * handling.
 */
interface FileStorageRepository {

  /**
   * Upload a file to storage at the specified path.
   *
   * This is the core upload method that all convenience methods delegate to. Implementations must
   * handle the actual Firebase Storage upload operation.
   *
   * @param storagePath The full storage path where the file should be stored
   * @param fileUri The local file URI to upload
   * @return Result containing the download URL on success, or an exception on failure
   */
  suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String>

  /**
   * Delete a file from storage using its download URL.
   *
   * @param downloadUrl The Firebase Storage download URL of the file to delete
   * @return Result containing Unit on success, or an exception on failure
   */
  suspend fun deleteFile(downloadUrl: String): Result<Unit>

  /**
   * Get metadata for a file using its download URL.
   *
   * @param downloadUrl The Firebase Storage download URL of the file
   * @return Result containing StorageMetadata on success, or an exception on failure
   */
  suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata>
}
