/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
*/
package ch.eureka.eurekapp.model.data.activity

import android.util.Log
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Utility for logging activities in the background without blocking operations. */
object ActivityLogger {
  private val activityRepository: ActivityRepository = FirestoreActivityRepository()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  /**
   * Logs an activity asynchronously without blocking the calling operation.
   *
   * Activities are logged in the background using a coroutine scope. If logging fails, the
   * exception is caught silently to prevent disrupting the main operation flow.
   *
   * @param projectId ID of the project where the activity occurred.
   * @param activityType Type of activity (CREATED, UPDATED, DELETED, etc.).
   * @param entityType Type of entity the activity is associated with (MEETING, TASK, FILE, etc.).
   * @param entityId ID of the specific entity that was acted upon.
   * @param userId ID of the user who performed the action.
   * @param metadata Optional map containing additional context about the activity (e.g., titles,
   *   old/new values).
   */
  fun logActivity(
      projectId: String,
      activityType: ActivityType,
      entityType: EntityType,
      entityId: String,
      userId: String,
      metadata: Map<String, Any> = emptyMap()
  ) {
    scope.launch {
      try {
        val activity =
            Activity(
                projectId = projectId,
                activityType = activityType,
                entityType = entityType,
                entityId = entityId,
                userId = userId,
                timestamp = Timestamp.now(),
                metadata = metadata)
        activityRepository.createActivity(activity)
      } catch (e: Exception) {
        Log.e("ActivityLogger", "Failed to log activity for $entityType:$entityId", e)
      }
    }
  }
}
