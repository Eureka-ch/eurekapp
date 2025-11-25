/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.model.data.activity

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Utility for logging activities in the background without blocking operations. */
object ActivityLogger {
  private val activityRepository: ActivityRepository = FirestoreActivityRepository()
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        // Silent failure - activity logging should not block operations
      }
    }
  }

  private fun log(
      type: ActivityType,
      projectId: String,
      entityType: EntityType,
      entityId: String,
      userId: String,
      title: String? = null
  ) {
    val metadata = buildMap {
      title?.let { put("title", it) }
      FirebaseAuth.getInstance().currentUser?.displayName?.let { put("userName", it) }
    }
    logActivity(projectId, type, entityType, entityId, userId, metadata)
  }

  fun logCreated(
      projectId: String,
      entityType: EntityType,
      entityId: String,
      userId: String,
      title: String? = null
  ) = log(ActivityType.CREATED, projectId, entityType, entityId, userId, title)

  fun logUpdated(
      projectId: String,
      entityType: EntityType,
      entityId: String,
      userId: String,
      title: String? = null
  ) = log(ActivityType.UPDATED, projectId, entityType, entityId, userId, title)

  fun logDeleted(
      projectId: String,
      entityType: EntityType,
      entityId: String,
      userId: String,
      title: String? = null
  ) = log(ActivityType.DELETED, projectId, entityType, entityId, userId, title)

  fun logFileUploaded(projectId: String, fileId: String, userId: String, fileName: String) =
      logActivity(
          projectId,
          ActivityType.UPLOADED,
          EntityType.FILE,
          fileId,
          userId,
          mapOf("fileName" to fileName))

  fun logStatusChanged(
      projectId: String,
      entityType: EntityType,
      entityId: String,
      userId: String,
      oldStatus: String,
      newStatus: String
  ) =
      logActivity(
          projectId,
          ActivityType.STATUS_CHANGED,
          entityType,
          entityId,
          userId,
          mapOf("oldStatus" to oldStatus, "newStatus" to newStatus))
}
