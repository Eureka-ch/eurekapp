package ch.eureka.eurekapp.model.data.file

import android.net.Uri
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

  /**
   * Upload a file to Firebase Storage.
   *
   * This method uploads a file from a local URI to the specified storage path. The upload operation
   * requires the user to be authenticated and authorized by security rules.
   *
   * @param storagePath The full storage path where the file should be stored
   * @param fileUri The local file URI to upload
   * @return Result containing the download URL on success, or an exception on failure
   * @throws IllegalStateException if the user is not authenticated
   */
  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> = runCatching {
    auth.currentUser ?: throw IllegalStateException("User must be authenticated to upload")

    val ref = storage.reference.child(storagePath)
    val contentType = getContentTypeFromPath(storagePath)
    val metadata = StorageMetadata.Builder().setContentType(contentType).build()

    ref.putFile(fileUri, metadata).await()
    ref.downloadUrl.await().toString()
  }

  /**
   * Delete a file from Firebase Storage using its download URL.
   *
   * This method extracts the storage reference from a download URL and deletes the file. The
   * deletion operation requires the user to be authenticated and authorized by security rules.
   *
   * @param downloadUrl The Firebase Storage download URL of the file to delete
   * @return Result containing Unit on success, or an exception on failure
   * @throws IllegalArgumentException if the download URL is invalid
   */
  override suspend fun deleteFile(downloadUrl: String): Result<Unit> = runCatching {
    val storagePath = extractStoragePathFromUrl(downloadUrl)
    val ref = storage.reference.child(storagePath)
    ref.delete().await()
  }

  /**
   * Get metadata for a file using its download URL.
   *
   * This method retrieves metadata information about a file including name, size, content type, and
   * creation time. The operation requires the user to be authenticated and authorized by security
   * rules.
   *
   * @param downloadUrl The Firebase Storage download URL of the file
   * @return Result containing StorageMetadata on success, or an exception on failure
   * @throws IllegalArgumentException if the download URL is invalid
   */
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
    if (pathStart == -1) {
      throw IllegalArgumentException("Invalid storage URL format: missing '/o/' path marker")
    }

    val pathEnd = downloadUrl.indexOf("?", pathStart)
    if (pathEnd == -1) {
      throw IllegalArgumentException("Invalid storage URL format: missing query parameters")
    }

    val encodedPath = downloadUrl.substring(pathStart + 3, pathEnd)
    return java.net.URLDecoder.decode(encodedPath, "UTF-8")
  }

  /**
   * Determine the MIME content type from a file path based on its extension.
   *
   * This function was co-authored by Claude Code (Anthropic AI assistant).
   *
   * @param path The file path (e.g., "projects/proj1/file.txt")
   * @return The MIME type (e.g., "text/plain")
   */
  private fun getContentTypeFromPath(path: String): String {
    val extension = path.substringAfterLast('.', "").lowercase()
    return when (extension) {
      "txt" -> "text/plain"
      "html",
      "htm" -> "text/html"
      "css" -> "text/css"
      "js" -> "application/javascript"
      "json" -> "application/json"
      "xml" -> "application/xml"
      "jpg",
      "jpeg" -> "image/jpeg"
      "png" -> "image/png"
      "gif" -> "image/gif"
      "svg" -> "image/svg+xml"
      "webp" -> "image/webp"
      "bmp" -> "image/bmp"
      "ico" -> "image/x-icon"
      "pdf" -> "application/pdf"
      "doc" -> "application/msword"
      "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
      "xls" -> "application/vnd.ms-excel"
      "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
      "ppt" -> "application/vnd.ms-powerpoint"
      "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation"
      "zip" -> "application/zip"
      "tar" -> "application/x-tar"
      "gz" -> "application/gzip"
      "7z" -> "application/x-7z-compressed"
      "mp3" -> "audio/mpeg"
      "wav" -> "audio/wav"
      "mp4" -> "video/mp4"
      "avi" -> "video/x-msvideo"
      else -> "application/octet-stream"
    }
  }
}
