package ch.eureka.eurekapp.model.data.file

import android.net.Uri
import ch.eureka.eurekapp.model.data.StoragePaths
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

  /**
   * Upload a user personal file.
   *
   * Convenience method for uploading files to a user's personal storage space. Uses the path
   * pattern: users/{userId}/{filename}
   *
   * @param userId The user ID
   * @param fileUri The local file URI to upload
   * @param filename The desired filename
   * @return Result containing the download URL on success, or an exception on failure
   */
  suspend fun uploadUserFile(userId: String, fileUri: Uri, filename: String): Result<String> =
      uploadFile(storagePath = StoragePaths.userFilePath(userId, filename), fileUri = fileUri)

  /**
   * Upload a project-level file.
   *
   * Convenience method for uploading files to a project's storage space. Uses the path pattern:
   * projects/{projectId}/{filename}
   *
   * @param projectId The project ID
   * @param fileUri The local file URI to upload
   * @param filename The desired filename
   * @return Result containing the download URL on success, or an exception on failure
   */
  suspend fun uploadProjectFile(projectId: String, fileUri: Uri, filename: String): Result<String> =
      uploadFile(storagePath = StoragePaths.projectFilePath(projectId, filename), fileUri = fileUri)

  /**
   * Upload a task attachment file.
   *
   * Convenience method for uploading attachments to a task. Uses the path pattern:
   * projects/{projectId}/tasks/{taskId}/{filename}
   *
   * @param projectId The project ID
   * @param taskId The task ID
   * @param fileUri The local file URI to upload
   * @param filename The desired filename
   * @return Result containing the download URL on success, or an exception on failure
   */
  suspend fun uploadTaskFile(
      projectId: String,
      taskId: String,
      fileUri: Uri,
      filename: String
  ): Result<String> =
      uploadFile(
          storagePath = StoragePaths.taskAttachmentPath(projectId, taskId, filename),
          fileUri = fileUri)

  /**
   * Upload a meeting attachment file.
   *
   * Convenience method for uploading attachments to a meeting. Uses the path pattern:
   * projects/{projectId}/meetings/{meetingId}/{filename}
   *
   * @param projectId The project ID
   * @param meetingId The meeting ID
   * @param fileUri The local file URI to upload
   * @param filename The desired filename
   * @return Result containing the download URL on success, or an exception on failure
   */
  suspend fun uploadMeetingFile(
      projectId: String,
      meetingId: String,
      fileUri: Uri,
      filename: String
  ): Result<String> =
      uploadFile(
          storagePath = StoragePaths.meetingAttachmentPath(projectId, meetingId, filename),
          fileUri = fileUri)
}
