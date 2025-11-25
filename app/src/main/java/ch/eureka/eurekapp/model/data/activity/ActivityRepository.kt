/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.model.data.activity

import kotlinx.coroutines.flow.Flow

/** Repository interface for activity tracking with real-time updates. */
interface ActivityRepository {
  /** Get all activities in a project with real-time updates. */
  fun getActivitiesInProject(projectId: String, limit: Int = 20): Flow<List<Activity>>

  /** Get activities filtered by entity type. */
  fun getActivitiesByEntityType(
      projectId: String,
      entityType: EntityType,
      limit: Int = 20
  ): Flow<List<Activity>>

  /** Get activities performed by a specific user. */
  fun getActivitiesByUser(projectId: String, userId: String, limit: Int = 20): Flow<List<Activity>>

  /** Get activities filtered by activity type. */
  fun getActivitiesByActivityType(
      projectId: String,
      activityType: ActivityType,
      limit: Int = 20
  ): Flow<List<Activity>>

  /** Get activities for a specific entity. */
  fun getActivitiesForEntity(projectId: String, entityId: String): Flow<List<Activity>>

  /** Create a new activity entry. */
  suspend fun createActivity(activity: Activity): Result<String>

  /** Delete an activity entry. */
  suspend fun deleteActivity(projectId: String, activityId: String): Result<Unit>
}
