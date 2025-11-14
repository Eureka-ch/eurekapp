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

  override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> = runCatching {
    auth.currentUser ?: throw IllegalStateException("User must be authenticated to upload")

    val ref = storage.reference.child(storagePath)
    val contentType = StorageHelpers.getContentTypeFromPath(storagePath)
    val metadata = StorageMetadata.Builder().setContentType(contentType).build()

    ref.putFile(fileUri, metadata).await()
    ref.downloadUrl.await().toString()
  }

  override suspend fun deleteFile(downloadUrl: String): Result<Unit> = runCatching {
    val storagePath = extractStoragePathFromUrl(downloadUrl)
    val ref = storage.reference.child(storagePath)
    ref.delete().await()
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
}
