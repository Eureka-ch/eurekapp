/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
*/
package ch.eureka.eurekapp.model.data.activity

import kotlinx.coroutines.flow.Flow

/** Repository interface for activity tracking with real-time updates. */
interface ActivityRepository {
  /**
   * Get all activities in a project with real-time updates.
   *
   * @param projectId ID of the project to retrieve activities from.
   * @param limit Maximum number of activities to return (default: 20).
   * @return Flow emitting a list of activities ordered by timestamp.
   */
  fun getActivitiesInProject(projectId: String, limit: Int = 20): Flow<List<Activity>>

  /**
   * Get activities filtered by entity type.
   *
   * @param projectId ID of the project to retrieve activities from.
   * @param entityType Type of entity to filter by (MEETING, TASK, FILE, etc.).
   * @param limit Maximum number of activities to return (default: 20).
   * @return Flow emitting a list of activities for the specified entity type.
   */
  fun getActivitiesByEntityType(
      projectId: String,
      entityType: EntityType,
      limit: Int = 20
  ): Flow<List<Activity>>

  /**
   * Get activities performed by a specific user.
   *
   * @param projectId ID of the project to retrieve activities from.
   * @param userId ID of the user whose activities to retrieve.
   * @param limit Maximum number of activities to return (default: 20).
   * @return Flow emitting a list of activities performed by the specified user.
   */
  fun getActivitiesByUser(projectId: String, userId: String, limit: Int = 20): Flow<List<Activity>>

  /**
   * Get activities filtered by activity type.
   *
   * @param projectId ID of the project to retrieve activities from.
   * @param activityType Type of activity to filter by (CREATED, UPDATED, DELETED, etc.).
   * @param limit Maximum number of activities to return (default: 20).
   * @return Flow emitting a list of activities of the specified type.
   */
  fun getActivitiesByActivityType(
      projectId: String,
      activityType: ActivityType,
      limit: Int = 20
  ): Flow<List<Activity>>

  /**
   * Get activities for a specific entity.
   *
   * @param projectId ID of the project to retrieve activities from.
   * @param entityId ID of the specific entity to retrieve activities for.
   * @return Flow emitting a list of activities related to the specified entity.
   */
  fun getActivitiesForEntity(projectId: String, entityId: String): Flow<List<Activity>>

  /**
   * Create a new activity entry.
   *
   * @param activity The activity object to create.
   * @return Result containing the created activity ID on success, or an error on failure.
   */
  suspend fun createActivity(activity: Activity): Result<String>

  /**
   * Delete an activity entry.
   *
   * @param projectId ID of the project containing the activity.
   * @param activityId ID of the activity to delete.
   * @return Result containing Unit on success, or an error on failure.
   */
  suspend fun deleteActivity(projectId: String, activityId: String): Result<Unit>
}
