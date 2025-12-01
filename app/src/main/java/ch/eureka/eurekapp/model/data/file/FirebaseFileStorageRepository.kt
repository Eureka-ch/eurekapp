/*
 * This file was co-authored by Claude Code and Gemini.
 */
package ch.eureka.eurekapp.model.data.file

import android.net.Uri
import ch.eureka.eurekapp.model.data.activity.ActivityLogger
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await

/**
 * Firebase implementation of [FileStorageRepository].
 *
 * This class handles all file storage operations using Firebase Storage, including uploading,
 * deleting, and retrieving metadata for files. It integrates with Firebase Auth to ensure only
 * authenticated users can perform operations.
 *
 * Security is enforced through Firebase Storage security rules that verify project membership by
 * querying Firestore. All operations return [Result] for proper error handling.
 *
 * @property storage The Firebase Storage instance for file operations
 * @property auth The Firebase Auth instance for user authentication
 */
class FirebaseFileStorageRepository(
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : FileStorageRepository {

  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> = runCatching {
    val currentUser =
        auth.currentUser ?: throw IllegalStateException("User must be authenticated to upload")

    val ref = storage.reference.child(storagePath)
    val contentType = StorageHelpers.getContentTypeFromPath(storagePath)
    val metadata = StorageMetadata.Builder().setContentType(contentType).build()

    val uploadTask = ref.putFile(fileUri, metadata).await()
    val downloadUrl = ref.downloadUrl.await().toString()

    val projectId = extractProjectIdFromPath(storagePath)
    val fileName = extractFileNameFromPath(storagePath)

    // Only log activity for project-related files
    if (projectId != null && fileName.isNotBlank()) {
      val fileSize = uploadTask.metadata?.sizeBytes ?: 0L
      ActivityLogger.logActivity(
          projectId = projectId,
          activityType = ActivityType.UPLOADED,
          entityType = EntityType.FILE,
          entityId = downloadUrl,
          userId = currentUser.uid,
          metadata = mapOf(
              "title" to fileName,
              "size" to fileSize,
              "type" to contentType
          ))
    } else {
      android.util.Log.d(
          "FirebaseFileStorage", "Skipping activity log for non-project file: $storagePath")
    }

    downloadUrl
  }

  override suspend fun deleteFile(downloadUrl: String): Result<Unit> = runCatching {
    val storagePath = extractStoragePathFromUrl(downloadUrl)
    val ref = storage.reference.child(storagePath)
    ref.delete().await()

    // Log activity after successful deletion
    val currentUserId = auth.currentUser?.uid
    val projectId = extractProjectIdFromPath(storagePath)
    val fileName = extractFileNameFromPath(storagePath)
    if (currentUserId != null && projectId != null) {
      ActivityLogger.logActivity(
          projectId = projectId,
          activityType = ActivityType.DELETED,
          entityType = EntityType.FILE,
          entityId = downloadUrl,
          userId = currentUserId,
          metadata = mapOf("title" to fileName))
    }
  }

  override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> = runCatching {
    val storagePath = extractStoragePathFromUrl(downloadUrl)
    val ref = storage.reference.child(storagePath)
    ref.metadata.await()
  }

  /**
   * Extract the storage path from a Firebase Storage download URL.
   *
   * This helper method parses both emulator and production download URLs to extract the storage
   * path. It handles URL decoding of the path component.
   *
   * @param downloadUrl The download URL to parse
   * @return The storage path (e.g., "projects/proj1/tasks/task1/file.txt")
   * @throws IllegalArgumentException if the URL format is invalid
   */
  private fun extractStoragePathFromUrl(downloadUrl: String): String {
    val pathStart = downloadUrl.indexOf("/o/")
    require(pathStart != -1) { "Invalid storage URL format: missing '/o/' path marker" }

    val pathEnd = downloadUrl.indexOf("?", pathStart)
    require(pathEnd != -1) { "Invalid storage URL format: missing query parameters" }

    val encodedPath = downloadUrl.substring(pathStart + 3, pathEnd)
    return java.net.URLDecoder.decode(encodedPath, "UTF-8")
  }

  /**
   * Extract projectId from storage path.
   *
   * Assumes path format: "projects/{projectId}/..."
   *
   * @return The projectId if found, null otherwise
   */
  private fun extractProjectIdFromPath(storagePath: String): String? {
    val parts = storagePath.split("/")
    val projectsIndex = parts.indexOf("projects")
    return if (projectsIndex != -1 && projectsIndex + 1 < parts.size) {
      parts[projectsIndex + 1]
    } else {
      null
    }
  }

  /**
   * Extract file name from storage path.
   *
   * @return The file name (last component of the path)
   */
  private fun extractFileNameFromPath(storagePath: String): String {
    return storagePath.substringAfterLast("/")
  }
}
