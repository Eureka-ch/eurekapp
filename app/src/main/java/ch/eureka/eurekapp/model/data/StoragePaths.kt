package ch.eureka.eurekapp.model.data

/**
 * Object containing constants and helper functions for Firebase Storage paths.
 *
 * This centralizes all storage path logic to ensure consistency across the application and make
 * path structure changes easier to manage. The architecture mirrors the Firestore structure with
 * user files, project-level files, and nested task/meeting attachments.
 *
 * Storage Structure:
 * - users/{userId}/{filename} - User personal files
 * - projects/{projectId}/{filename} - Project-level files
 * - projects/{projectId}/tasks/{taskId}/{filename} - Task attachments
 * - projects/{projectId}/meetings/{meetingId}/{filename} - Meeting attachments
 */
object StoragePaths {
  private const val USERS = "users"
  private const val PROJECTS = "projects"
  private const val TASKS = "tasks"
  private const val MEETINGS = "meetings"

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
   * @return Storage path: projects/{projectId}/tasks/{taskId}/{filename}
   */
  fun taskAttachmentPath(projectId: String, taskId: String, filename: String) =
      "$PROJECTS/$projectId/$TASKS/$taskId/$filename"

  /**
   * Generate storage path for meeting attachment files.
   *
   * @param projectId The project ID
   * @param meetingId The meeting ID
   * @param filename The filename to store
   * @return Storage path: projects/{projectId}/meetings/{meetingId}/{filename}
   */
  fun meetingAttachmentPath(projectId: String, meetingId: String, filename: String) =
      "$PROJECTS/$projectId/$MEETINGS/$meetingId/$filename"
}
