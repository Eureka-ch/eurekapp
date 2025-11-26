/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.model.data.activity.FirestoreActivityRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI state for activity feed with filtering and pagination support.
 *
 * @property activities List of all activities (enriched with user names)
 * @property activitiesByDate Activities grouped by date (day) for efficient rendering
 * @property isLoading True when loading activities from repository
 * @property errorMsg Error message to display, null if no error
 * @property filterEntityType Current entity type filter (MEETING, FILE, etc.), null if no filter
 * @property filterActivityType Current activity type filter (CREATED, UPDATED, etc.), null if no
 *   filter
 * @property isCompactMode True for compact display mode with fewer items
 * @property limit Maximum number of activities to fetch and display
 */
data class ActivityFeedUIState(
    val activities: List<Activity> = emptyList(),
    val activitiesByDate: Map<Long, List<Activity>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val filterEntityType: EntityType? = null,
    val filterActivityType: ActivityType? = null,
    val isCompactMode: Boolean = false,
    val limit: Int = 20
)

/**
 * ViewModel managing activity feed with real-time updates and filtering.
 *
 * @property repository Repository for fetching activities from Firestore
 * @property firestore Firestore instance for fetching user display names
 */
class ActivityFeedViewModel(
    private val repository: ActivityRepository = FirestoreActivityRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ActivityFeedUIState())
  val uiState: StateFlow<ActivityFeedUIState> = _uiState

  /**
   * Group activities by date (day).
   *
   * @param activities List of activities to group
   * @return Map of date (as timestamp in millis) to list of activities for that date
   */
  private fun groupActivitiesByDate(activities: List<Activity>): Map<Long, List<Activity>> {
    return activities.groupBy { activity ->
      val calendar = java.util.Calendar.getInstance()
      calendar.time = activity.timestamp.toDate()
      calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
      calendar.set(java.util.Calendar.MINUTE, 0)
      calendar.set(java.util.Calendar.SECOND, 0)
      calendar.set(java.util.Calendar.MILLISECOND, 0)
      calendar.timeInMillis
    }
  }

  private suspend fun enrichActivitiesWithUserNames(activities: List<Activity>): List<Activity> {
    val userIds = activities.map { it.userId }.distinct()
    val userNames = mutableMapOf<String, String>()

    // Fetch user display names from Firestore
    userIds.forEach { userId ->
      try {
        val userDoc = firestore.collection(FirestorePaths.USERS).document(userId).get().await()
        userDoc.getString("displayName")?.let { displayName -> userNames[userId] = displayName }
      } catch (e: Exception) {
        android.util.Log.e(
            "ActivityFeedViewModel", "Failed to fetch display name for user: $userId", e)
      }
    }

    // Add userName to each activity's metadata
    return activities.map { activity ->
      val userName = userNames[activity.userId] ?: "Someone"
      activity.copy(metadata = activity.metadata + ("userName" to userName))
    }
  }

  fun clearErrorMsg() = _uiState.update { it.copy(errorMsg = null) }

  fun setErrorMsg(msg: String) = _uiState.update { it.copy(errorMsg = msg) }

  fun setCompactMode(isCompact: Boolean) {
    _uiState.update { it.copy(isCompactMode = isCompact, limit = if (isCompact) 10 else 20) }
  }

  fun loadActivities(projectId: String) {
    viewModelScope.launch {
      repository
          .getActivitiesInProject(projectId, limit = _uiState.value.limit)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { activities ->
            val enriched = enrichActivitiesWithUserNames(activities)
            val grouped = groupActivitiesByDate(enriched)
            _uiState.update {
              it.copy(isLoading = false, activities = enriched, activitiesByDate = grouped)
            }
          }
    }
  }

  fun loadActivitiesByEntityType(projectId: String, entityType: EntityType) {
    _uiState.update { it.copy(filterEntityType = entityType) }
    viewModelScope.launch {
      repository
          .getActivitiesInProject(
              projectId, limit = 100) // Fetch more to have enough after filtering
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { activities ->
            // Filter by entity type in app code
            val filtered =
                activities
                    .filter { activity ->
                      when (entityType) {
                        EntityType.PROJECT ->
                            activity.entityType == EntityType.PROJECT ||
                                activity.entityType == EntityType.MEMBER
                        EntityType.MEETING -> activity.entityType == EntityType.MEETING
                        else -> activity.entityType == entityType
                      }
                    }
                    .take(_uiState.value.limit)
            val enriched = enrichActivitiesWithUserNames(filtered)
            val grouped = groupActivitiesByDate(enriched)
            _uiState.update {
              it.copy(isLoading = false, activities = enriched, activitiesByDate = grouped)
            }
          }
    }
  }

  fun loadActivitiesByActivityType(projectId: String, activityType: ActivityType) {
    _uiState.update { it.copy(filterActivityType = activityType) }
    viewModelScope.launch {
      repository
          .getActivitiesByActivityType(projectId, activityType, limit = _uiState.value.limit)
          .onStart { _uiState.update { it.copy(isLoading = true) } }
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { activities ->
            val grouped = groupActivitiesByDate(activities)
            _uiState.update {
              it.copy(isLoading = false, activities = activities, activitiesByDate = grouped)
            }
          }
    }
  }

  fun clearFilters() {
    _uiState.update {
      it.copy(filterEntityType = null, filterActivityType = null, activities = emptyList())
    }
  }

  fun refresh(projectId: String) {
    when {
      _uiState.value.filterEntityType != null ->
          loadActivitiesByEntityType(projectId, _uiState.value.filterEntityType!!)
      _uiState.value.filterActivityType != null ->
          loadActivitiesByActivityType(projectId, _uiState.value.filterActivityType!!)
      else -> loadActivities(projectId)
    }
  }

  fun deleteActivity(projectId: String, activityId: String) {
    viewModelScope.launch {
      // Optimistically remove from UI
      val currentActivities = _uiState.value.activities
      val currentActivitiesByDate = _uiState.value.activitiesByDate
      val filteredActivities =
          currentActivities.filter { activity -> activity.activityId != activityId }
      val updatedActivitiesByDate = groupActivitiesByDate(filteredActivities)

      _uiState.update {
        it.copy(activities = filteredActivities, activitiesByDate = updatedActivitiesByDate)
      }

      // Delete from repository
      repository.deleteActivity(projectId, activityId).onFailure { e ->
        // Restore on failure
        _uiState.update {
          it.copy(
              activities = currentActivities,
              activitiesByDate = currentActivitiesByDate,
              errorMsg = e.message)
        }
      }
    }
  }
}
