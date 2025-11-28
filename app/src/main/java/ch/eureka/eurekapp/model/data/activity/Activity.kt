/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.model.data.activity

import com.google.firebase.Timestamp

/**
 * Data class representing an activity/action performed within a project.
 *
 * Activities track all significant actions users take within a project, such as creating meetings,
 * sending messages, uploading files, updating tasks, etc. This enables building activity feeds,
 * notifications, and audit trails.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property activityId Unique identifier for this activity.
 * @property projectId ID of the project this activity belongs to.
 * @property activityType Type of action performed (CREATED, UPDATED, DELETED, etc.).
 * @property entityType Type of entity the action was performed on (MEETING, MESSAGE, FILE, TASK).
 * @property entityId ID of the specific entity that was acted upon.
 * @property userId ID of the user who performed the action.
 * @property timestamp When the activity occurred.
 * @property metadata Flexible map containing entity-specific details (e.g., meeting title, file
 *   name, old/new status). Common keys: "title", "description", "oldValue", "newValue", "fileName"
 */
data class Activity(
    val activityId: String = "",
    val projectId: String = "",
    val activityType: ActivityType = ActivityType.CREATED,
    val entityType: EntityType = EntityType.MEETING,
    val entityId: String = "",
    val userId: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Enum representing the type of entity an activity is associated with.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class EntityType {
  /** Meeting entity */
  MEETING,

  /** Chat message entity */
  MESSAGE,

  /** File/document entity */
  FILE,

  /** Task entity */
  TASK,

  /** Project entity (for project-level activities) */
  PROJECT,

  /** User/member entity (for membership changes) */
  MEMBER
}
