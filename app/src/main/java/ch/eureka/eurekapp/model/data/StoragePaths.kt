/*
 * Portions of this file were co-authored by Claude Code (Anthropic AI assistant).
 */
package ch.eureka.eurekapp.model.data

/**
 * Object containing constants and helper functions for Firebase Storage paths.
 *
 * This centralizes all storage path logic to ensure consistency across the application and make
 * path structure changes easier to manage. The architecture mirrors the Firestore structure with
 * user files, project-level files, and nested task/meeting attachments.
 *
 * Storage Structure:
 * - profilePhotos/{userId}.{ext} - User profile photos (public read, owner write only)
 * - users/{userId}/{filename} - User personal files
 * - projects/{projectId}/{filename} - Project-level files
 * - projects/{projectId}/tasks/{taskId}/attachments/{filename} - Task attachments
 * - projects/{projectId}/meetings/{meetingId}/attachments/{filename} - Meeting attachments
 * - projects/{projectId}/meetings/{meetingId}/transcriptions/{filename} - Meeting audio transcriptions
 */
object StoragePaths {
  private const val PROFILE_PHOTOS = "profilePhotos"
  private const val USERS = "users"
  private const val PROJECTS = "projects"
  private const val TASKS = "tasks"
  private const val MEETINGS = "meetings"
  private const val ATTACHMENTS = "attachments"
  private const val TRANSCRIPTIONS = "transcriptions"

  /**
   * Generate storage path for user profile photo.
   *
   * Profile photos are stored with the user ID as the filename to enforce the security rule that
   * only the owner can write to their profile photo.
   *
   * @param userId The user ID (this will be the filename)
   * @param extension The file extension (e.g., "jpg", "png")
   * @return Storage path: profilePhotos/{userId}.{extension}
   */
  fun profilePhotoPath(userId: String, extension: String) = "$PROFILE_PHOTOS/$userId.$extension"

  /**
   * Generate storage path for user personal files.
   *
   * @param userId The user ID
   * @param filename The filename to store
   * @return Storage path: users/{userId}/{filename}
   */
  fun userFilePath(userId: String, filename: String) = "$USERS/$userId/$filename"

  /**
   * Generate storage path for project-level files.
   *
   * @param projectId The project ID
   * @param filename The filename to store
   * @return Storage path: projects/{projectId}/{filename}
   */
  fun projectFilePath(projectId: String, filename: String) = "$PROJECTS/$projectId/$filename"

  /**
   * Generate storage path for task attachment files.
   *
   * @param projectId The project ID
   * @param taskId The task ID
   * @param filename The filename to store
   * @return Storage path: projects/{projectId}/tasks/{taskId}/attachments/{filename}
   */
  fun taskAttachmentPath(projectId: String, taskId: String, filename: String) =
      "$PROJECTS/$projectId/$TASKS/$taskId/$ATTACHMENTS/$filename"

  /**
   * Generate storage path for meeting attachment files.
   *
   * @param projectId The project ID
   * @param meetingId The meeting ID
   * @param filename The filename to store
   * @return Storage path: projects/{projectId}/meetings/{meetingId}/attachments/{filename}
   */
  fun meetingAttachmentPath(projectId: String, meetingId: String, filename: String) =
      "$PROJECTS/$projectId/$MEETINGS/$meetingId/$ATTACHMENTS/$filename"

  /**
   * Generate storage path for meeting transcription audio files.
   *
   * @param projectId The project ID
   * @param meetingId The meeting ID
   * @param filename The filename to store
   * @return Storage path: projects/{projectId}/meetings/{meetingId}/transcriptions/{filename}
   */
  fun meetingTranscriptionAudioPath(projectId: String, meetingId: String, filename: String) =
      "$PROJECTS/$projectId/$MEETINGS/$meetingId/$TRANSCRIPTIONS/$filename"
}
