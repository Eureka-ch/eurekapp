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
import com.google.firebase.firestore.FieldValue
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
 * @property activities Currently displayed activities (filtered).
 * @property allActivities Cache of all fetched activities for client-side filtering.
 * @property activitiesByDate Activities grouped by date.
 * @property isLoading Whether data is being fetched.
 * @property errorMsg Error message if something went wrong.
 * @property filterEntityType Active entity type filter.
 * @property filterActivityType Active activity type filter.
 * @property searchQuery Current search query.
 * @property readActivityIds Activity IDs marked as read.
 * @property groupByProject Whether to group by project.
 * @property isRefreshing Whether pull-to-refresh is active.
 * @property isCompactMode Whether to use compact UI mode.
 */
data class ActivityFeedUIState(
    val activities: List<Activity> = emptyList(),
    val allActivities: List<Activity> = emptyList(),
    val activitiesByDate: Map<Long, List<Activity>> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val filterEntityType: EntityType? = null,
    val filterActivityType: ActivityType? = null,
    val searchQuery: String = "",
    val readActivityIds: Set<String> = emptySet(),
    val groupByProject: Boolean = false,
    val isRefreshing: Boolean = false,
    val isCompactMode: Boolean = false
)

/**
 * ViewModel for activity feed with Firestore-persisted read status.
 *
 * @param repository Activity data repository.
 * @param firestore Firestore instance for user data and read status.
 * @param auth Firebase Auth instance.
 */
class ActivityFeedViewModel(
    private val repository: ActivityRepository = FirestoreActivityRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ActivityFeedUIState())
  val uiState: StateFlow<ActivityFeedUIState> = _uiState

  /** Loads read activity IDs from Firestore. */
  private suspend fun loadReadActivityIds(userId: String): Set<String> {
    return try {
      val userDoc = firestore.collection(FirestorePaths.USERS).document(userId).get().await()
      @Suppress("UNCHECKED_CAST")
      (userDoc["readActivityIds"] as? List<String>)?.toSet() ?: emptySet()
    } catch (e: Exception) {
      Log.e("ActivityFeedViewModel", "Failed to load read IDs: ${e.message}")
      emptySet()
    }
  }

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

  /** Clears error message. */
  fun clearErrorMsg() = _uiState.update { it.copy(errorMsg = null) }

  /** Sets compact mode. */
  fun setCompactMode(isCompact: Boolean) {
    _uiState.update { it.copy(isCompactMode = isCompact) }
  }

  /** Loads activities for current user. */
  fun loadActivities() {
    val currentUserId = auth.currentUser?.uid ?: return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }

      // Load read activity IDs from Firestore
      val readIds = loadReadActivityIds(currentUserId)

      repository
          .getActivities(currentUserId)
          .catch { e -> _uiState.update { it.copy(isLoading = false, errorMsg = e.message) } }
          .collect { rawActivities ->
            try {
              val enriched = enrichActivitiesWithUserNames(rawActivities)
              _uiState.update { state ->
                val filtered =
                    applyAllFilters(
                        enriched,
                        state.filterEntityType,
                        state.filterActivityType,
                        state.searchQuery)
                val groupedByDate = groupActivitiesByDate(filtered)
                state.copy(
                    isLoading = false,
                    allActivities = enriched,
                    activities = filtered,
                    activitiesByDate = groupedByDate,
                    readActivityIds = readIds)
              }
            } catch (e: Exception) {
              _uiState.update { it.copy(isLoading = false, errorMsg = e.message) }
            }
          }
    }
  }

  /** Applies entity type filter (PROJECT, MEETING, etc). */
  fun applyEntityTypeFilter(entityType: EntityType) {
    _uiState.update { state ->
      val filtered =
          applyAllFilters(
              state.allActivities, entityType, state.filterActivityType, state.searchQuery)
      val groupedByDate = groupActivitiesByDate(filtered)
      state.copy(
          filterEntityType = entityType, activities = filtered, activitiesByDate = groupedByDate)
    }
    if (_uiState.value.allActivities.isEmpty()) loadActivities()
  }

  /** Applies activity type filter (CREATED, UPDATED, etc). */
  fun applyActivityTypeFilter(activityType: ActivityType?) {
    _uiState.update { state ->
      val filtered =
          applyAllFilters(
              state.allActivities, state.filterEntityType, activityType, state.searchQuery)
      val groupedByDate = groupActivitiesByDate(filtered)
      state.copy(
          filterActivityType = activityType,
          activities = filtered,
          activitiesByDate = groupedByDate)
    }
  }

  /** Applies search query filter. */
  fun applySearch(query: String) {
    _uiState.update { state ->
      val filtered =
          applyAllFilters(
              state.allActivities, state.filterEntityType, state.filterActivityType, query)
      val groupedByDate = groupActivitiesByDate(filtered)
      state.copy(searchQuery = query, activities = filtered, activitiesByDate = groupedByDate)
    }
  }

  /** Clears all filters. */
  fun clearFilters() {
    _uiState.update { state ->
      state.copy(
          filterEntityType = null,
          filterActivityType = null,
          searchQuery = "",
          activities = emptyList(),
          activitiesByDate = emptyMap())
    }
  }

  /** Marks activity as read. */
  fun markAsRead(activityId: String) {
    val userId = auth.currentUser?.uid ?: return
    viewModelScope.launch {
      try {
        firestore
            .collection(FirestorePaths.USERS)
            .document(userId)
            .update("readActivityIds", FieldValue.arrayUnion(activityId))
            .await()
        _uiState.update { state ->
          state.copy(readActivityIds = state.readActivityIds + activityId)
        }
      } catch (e: Exception) {
        Log.e("ActivityFeedViewModel", "Failed to mark as read: ${e.message}")
      }
    }
  }

  /** Marks all visible activities as read. */
  fun markAllAsRead() {
    val userId = auth.currentUser?.uid ?: return
    val allActivityIds = _uiState.value.activities.map { it.activityId }
    viewModelScope.launch {
      try {
        firestore
            .collection(FirestorePaths.USERS)
            .document(userId)
            .update("readActivityIds", FieldValue.arrayUnion(*allActivityIds.toTypedArray()))
            .await()
        _uiState.update { state ->
          state.copy(readActivityIds = state.readActivityIds + allActivityIds)
        }
      } catch (e: Exception) {
        Log.e("ActivityFeedViewModel", "Failed to mark all as read: ${e.message}")
      }
    }
  }

  /** Toggles project grouping mode. */
  fun toggleGroupByProject() {
    _uiState.update { state -> state.copy(groupByProject = !state.groupByProject) }
  }

  /** Refreshes activity feed. */
  fun refresh() {
    _uiState.update { it.copy(isRefreshing = true) }
    loadActivities()
    _uiState.update { it.copy(isRefreshing = false) }
  }

  private fun applyAllFilters(
      list: List<Activity>,
      entityType: EntityType?,
      activityType: ActivityType?,
      query: String
  ): List<Activity> {
    var filtered = list

    // Apply entity type filter
    if (entityType != null) {
      filtered =
          filtered.filter { activity ->
            when (entityType) {
              EntityType.PROJECT ->
                  activity.entityType == EntityType.PROJECT ||
                      activity.entityType == EntityType.MEMBER
              EntityType.MEETING -> activity.entityType == EntityType.MEETING
              else -> activity.entityType == entityType
            }
          }
    }

    // Apply activity type filter
    if (activityType != null) {
      filtered = filtered.filter { it.activityType == activityType }
    }

    // Apply search query
    if (query.isNotBlank()) {
      filtered =
          filtered.filter { activity ->
            val title = activity.metadata["title"]?.toString() ?: ""
            val userName = activity.metadata["userName"]?.toString() ?: ""
            title.contains(query, ignoreCase = true) || userName.contains(query, ignoreCase = true)
          }
    }

    return filtered
  }

  /** Deletes an activity. */
  fun deleteActivity(activityId: String) {
    val userId = auth.currentUser?.uid ?: return
    viewModelScope.launch {
      val prevList = _uiState.value.activities
      val prevGrouped = _uiState.value.activitiesByDate
      val prevReadIds = _uiState.value.readActivityIds

      // Optimistically update UI (only filtered view, let Flow handle allActivities)
      _uiState.update { state ->
        val filtered = state.activities.filter { it.activityId != activityId }
        val groupedByDate = groupActivitiesByDate(filtered)
        state.copy(
            activities = filtered,
            activitiesByDate = groupedByDate,
            readActivityIds = state.readActivityIds - activityId)
      }

      val deleteResult = repository.deleteActivity(activityId)

      if (deleteResult.isSuccess) {
        try {
          firestore
              .collection(FirestorePaths.USERS)
              .document(userId)
              .update("readActivityIds", FieldValue.arrayRemove(activityId))
              .await()
        } catch (e: Exception) {
          Log.e("ActivityFeedViewModel", "Failed to remove read status: ${e.message}")
        }
      } else {
        val exception = deleteResult.exceptionOrNull()
        _uiState.update { state ->
          state.copy(
              activities = prevList,
              activitiesByDate = prevGrouped,
              readActivityIds = prevReadIds,
              errorMsg = exception?.message)
        }
      }
    }
  }
}
