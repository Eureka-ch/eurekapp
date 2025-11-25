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

/** UI state for activity feed with filtering and pagination support. */
data class ActivityFeedUIState(
    val activities: List<Activity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val filterEntityType: EntityType? = null,
    val filterActivityType: ActivityType? = null,
    val isCompactMode: Boolean = false,
    val limit: Int = 20
)

/** ViewModel managing activity feed with real-time updates and filtering. */
class ActivityFeedViewModel(
    private val repository: ActivityRepository = FirestoreActivityRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ActivityFeedUIState())
  val uiState: StateFlow<ActivityFeedUIState> = _uiState

  private suspend fun enrichActivitiesWithUserNames(activities: List<Activity>): List<Activity> {
    val userIds = activities.map { it.userId }.distinct()
    val userNames = mutableMapOf<String, String>()

    // Fetch user display names from Firestore
    userIds.forEach { userId ->
      try {
        val userDoc = firestore.collection(FirestorePaths.USERS).document(userId).get().await()
        userDoc.getString("displayName")?.let { displayName -> userNames[userId] = displayName }
      } catch (e: Exception) {
        // If fetch fails, skip this user
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
            _uiState.update { it.copy(isLoading = false, activities = enriched) }
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
            _uiState.update { it.copy(isLoading = false, activities = enriched) }
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
            _uiState.update { it.copy(isLoading = false, activities = activities) }
          }
    }
  }

  fun clearFilters(projectId: String) {
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
      _uiState.update {
        it.copy(activities = currentActivities.filter { it.activityId != activityId })
      }

      // Delete from repository
      repository.deleteActivity(projectId, activityId).onFailure { e ->
        // Restore on failure
        _uiState.update { it.copy(activities = currentActivities, errorMsg = e.message) }
      }
    }
  }
}
