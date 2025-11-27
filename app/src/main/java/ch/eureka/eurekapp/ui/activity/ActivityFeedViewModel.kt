/*
 * This file was co-authored by Claude Code and Gemini.
 */
package ch.eureka.eurekapp.ui.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.ActivityType
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.model.data.activity.FirestoreActivityRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI state for the activity feed screen.
 *
 * @property activities The list of activities currently displayed (filtered).
 * @property allActivities Cache of all fetched activities to enable fast client-side filtering.
 * @property activitiesByDate Activities grouped by date (timestamp in millis as key).
 * @property isLoading Whether the data is currently being fetched.
 * @property errorMsg An error message if something went wrong, or null otherwise.
 * @property filterEntityType The currently active entity type filter (e.g., MEETING, PROJECT), or
 *   null if showing all.
 * @property filterActivityType The currently active activity type filter (e.g. CREATED, UPDATED),
 *   or null if showing all.
 * @property isCompactMode True if UI should use compact mode, false otherwise.
 */
data class ActivityFeedUIState(
    val activities: List<Activity> = emptyList(),
    val allActivities: List<Activity> = emptyList(), // Cache for client-side filtering
    val activitiesByDate: Map<Long, List<Activity>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val filterEntityType: EntityType? = null,
    val filterActivityType: ActivityType? = null,
    val isCompactMode: Boolean = false
)

/**
 * ViewModel managing the activity feed logic.
 *
 * It uses a simplified repository pattern where it fetches all allowed activities for the user and
 * then handles filtering (by Projects or Meetings) in memory.
 *
 * @param repository The [ActivityRepository] for data operations.
 * @param firestore The [FirebaseFirestore] instance (used mostly for enriching data with user
 *   names).
 * @param auth The [FirebaseAuth] instance.
 */
class ActivityFeedViewModel(
    private val repository: ActivityRepository = FirestoreActivityRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ActivityFeedUIState())
  val uiState: StateFlow<ActivityFeedUIState> = _uiState

  /**
   * Groups activities by date (normalized to start of day).
   *
   * @param activities List of activities to group.
   * @return Map with date in millis as key and list of activities as value.
   */
  private fun groupActivitiesByDate(activities: List<Activity>): Map<Long, List<Activity>> {
    return activities.groupBy { activity ->
      val calendar = Calendar.getInstance()
      calendar.time = activity.timestamp.toDate()
      calendar[Calendar.HOUR_OF_DAY] = 0
      calendar[Calendar.MINUTE] = 0
      calendar[Calendar.SECOND] = 0
      calendar[Calendar.MILLISECOND] = 0
      calendar.timeInMillis
    }
  }

  private suspend fun enrichActivitiesWithUserNames(activities: List<Activity>): List<Activity> {
    val userIds = activities.map { it.userId }.distinct()
    val userNames = mutableMapOf<String, String>()

    userIds.forEach { userId ->
      try {
        val userDoc = firestore.collection(FirestorePaths.USERS).document(userId).get().await()
        userDoc.getString("displayName")?.let { displayName -> userNames[userId] = displayName }
      } catch (e: Exception) {
        Log.e("ActivityFeedViewModel", e.message ?: "No message.")
      }
    }

    return activities.map { activity ->
      val userName = userNames[activity.userId] ?: "Someone"
      activity.copy(metadata = activity.metadata + ("userName" to userName))
    }
  }

  /** Clears the error messages. */
  fun clearErrorMsg() = _uiState.update { it.copy(errorMsg = null) }

  /**
   * Set compact mode flag.
   *
   * @param isCompact The new value for the compact mode flag.
   */
  fun setCompactMode(isCompact: Boolean) {
    _uiState.update { it.copy(isCompactMode = isCompact) }
  }

  /**
   * Loads the single unified feed for the current user. Fetches EVERYTHING allowed, then applies
   * local filters if active.
   */
  fun loadActivities() {
    val currentUserId = auth.currentUser?.uid ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      repository
          .getActivities(currentUserId)
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { rawActivities ->
            try {
              val enriched = enrichActivitiesWithUserNames(rawActivities)
              _uiState.update { state ->
                // Apply current filter to new data
                val filtered =
                    if (state.filterEntityType != null) {
                      filterList(enriched, state.filterEntityType)
                    } else {
                      enriched
                    }
                val groupedByDate = groupActivitiesByDate(filtered)
                state.copy(
                    isLoading = false,
                    allActivities = enriched,
                    activities = filtered,
                    activitiesByDate = groupedByDate)
              }
            } catch (e: Exception) {
              _uiState.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
          }
    }
  }

  /**
   * Applies a filter to the activity feed.
   *
   * @param entityType The type of entity to filter by (e.g., [EntityType.MEETING] or
   *   [EntityType.PROJECT]).
   */
  fun applyFilter(entityType: EntityType) {
    _uiState.update { state ->
      val filtered = filterList(state.allActivities, entityType)
      val groupedByDate = groupActivitiesByDate(filtered)
      state.copy(
          filterEntityType = entityType, activities = filtered, activitiesByDate = groupedByDate)
    }
    if (_uiState.value.allActivities.isEmpty()) loadActivities()
  }

  /** Clears filters to show everything. */
  fun clearFilters() {
    _uiState.update { state ->
      val groupedByDate = groupActivitiesByDate(state.allActivities)
      state.copy(
          filterEntityType = null,
          activities = state.allActivities,
          activitiesByDate = groupedByDate)
    }
    if (_uiState.value.allActivities.isEmpty()) loadActivities()
  }

  private fun filterList(list: List<Activity>, type: EntityType): List<Activity> {
    return list.filter { activity ->
      when (type) {
        EntityType.PROJECT ->
            activity.entityType == EntityType.PROJECT || activity.entityType == EntityType.MEMBER
        EntityType.MEETING -> activity.entityType == EntityType.MEETING
        else -> activity.entityType == type
      }
    }
  }

  /**
   * Deletes an activity.
   *
   * @param activityId The unique ID of the activity to delete.
   */
  fun deleteActivity(activityId: String) {
    viewModelScope.launch {
      val prevList = _uiState.value.activities
      val prevAll = _uiState.value.allActivities
      val prevGrouped = _uiState.value.activitiesByDate

      _uiState.update { state ->
        val filtered = state.activities.filter { it.activityId != activityId }
        val groupedByDate = groupActivitiesByDate(filtered)
        state.copy(
            activities = filtered,
            allActivities = state.allActivities.filter { it.activityId != activityId },
            activitiesByDate = groupedByDate)
      }

      repository.deleteActivity(activityId).onFailure { e ->
        // Revert on failure
        _uiState.update { state ->
          state.copy(
              activities = prevList,
              allActivities = prevAll,
              activitiesByDate = prevGrouped,
              errorMsg = e.message)
        }
      }
    }
  }
}
