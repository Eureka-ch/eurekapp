/*
 * Co-Authored-By: Claude Sonnet 4.5
 */
package ch.eureka.eurekapp.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Data class to represent the UI state of the activity detail screen.
 *
 * @property activity The detailed activity information, or null if loading or not found.
 * @property relatedActivities List of activities related to the same entity.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property isLoading Whether a data loading operation is in progress.
 * @property shareSuccess Whether the activity was successfully shared.
 * @property isConnected Whether the device is connected to the internet.
 */
data class ActivityDetailUIState(
    val activity: Activity? = null,
    val relatedActivities: List<Activity> = emptyList(),
    val errorMsg: String? = null,
    val isLoading: Boolean = false,
    val shareSuccess: Boolean = false,
    val isConnected: Boolean = true
)

/**
 * ViewModel for the activity detail screen.
 *
 * Manages the state and business logic for displaying detailed activity information, including
 * enriched user data and related activities for the same entity.
 *
 * @property activityId The ID of the activity to display.
 * @property repository The repository for activity data operations.
 * @property connectivityObserver The connectivity observer.
 */
class ActivityDetailViewModel(
    private val activityId: String,
    private val repository: ActivityRepository = RepositoriesProvider.activityRepository,
    private val connectivityObserver: ConnectivityObserver =
        ConnectivityObserverProvider.connectivityObserver,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore = Firebase.firestore,
    private val auth: com.google.firebase.auth.FirebaseAuth = Firebase.auth
) : ViewModel() {

  private val _activity = MutableStateFlow<Activity?>(null)
  private val _relatedActivities = MutableStateFlow<List<Activity>>(emptyList())
  private val _shareSuccess = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  private val currentUserId = auth.currentUser?.uid ?: ""

  init {
    loadActivityDetails()
  }

  /**
   * UI state combining activity data, related activities, and operation states.
   *
   * Follows the project's Flow pattern with stateIn() and WhileSubscribed strategy for automatic
   * lifecycle management.
   */
  val uiState: StateFlow<ActivityDetailUIState> =
      combine(_activity, _relatedActivities, _shareSuccess, _errorMsg, _isConnected) {
              flows: Array<Any?> ->
            @Suppress("UNCHECKED_CAST") val activity = flows[0] as Activity?
            @Suppress("UNCHECKED_CAST") val relatedActivities = flows[1] as List<Activity>
            val shareSuccess = flows[2] as Boolean
            val errorMsg = flows[3] as String?
            val isConnected = flows[4] as Boolean

            ActivityDetailUIState(
                activity = activity,
                relatedActivities = relatedActivities,
                isLoading = false,
                shareSuccess = shareSuccess,
                errorMsg = errorMsg,
                isConnected = isConnected)
          }
          .onStart { emit(ActivityDetailUIState(isLoading = true)) }
          .catch { e -> emit(ActivityDetailUIState(errorMsg = e.message, isLoading = false)) }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = ActivityDetailUIState(isLoading = true))

  /**
   * Loads activity details and enriches with user name from Firestore.
   *
   * Also triggers loading of related activities for the same entity.
   */
  private fun loadActivityDetails() {
    viewModelScope.launch {
      try {
        repository.getActivities(currentUserId).collect { activities ->
          val activity = activities.firstOrNull { it.activityId == activityId }

          if (activity != null) {
            val enrichedActivity = enrichActivityWithUserName(activity)
            _activity.value = enrichedActivity

            loadRelatedActivities(activity.entityId, activities)
          } else {
            _errorMsg.value = "Activity not found"
          }
        }
      } catch (e: Exception) {
        _errorMsg.value = e.message ?: "An error occurred"
      }
    }
  }

  /**
   * Enriches an activity with the user's display name from Firestore.
   *
   * Fetches the display name for the user who performed the activity and adds it to metadata.
   * Throws an exception if the user document or display name cannot be found.
   *
   * @param activity The activity to enrich.
   * @return The enriched activity with userName in metadata.
   * @throws IllegalStateException if the user document or displayName is not found.
   */
  private suspend fun enrichActivityWithUserName(activity: Activity): Activity {
    return try {
      val userDoc = firestore.collection("users").document(activity.userId).get().await()
      val displayName =
          userDoc.getString("displayName")
              ?: run {
                android.util.Log.e(
                    "ActivityDetailViewModel",
                    "Display name not found for user: ${activity.userId}")
                throw IllegalStateException("Display name not found for user: ${activity.userId}")
              }
      activity.copy(metadata = activity.metadata + ("userName" to displayName))
    } catch (e: Exception) {
      android.util.Log.e("ActivityDetailViewModel", "Failed to enrich activity with user name", e)
      throw e
    }
  }

  /**
   * Loads related activities for the same entity.
   *
   * Filters activities by matching entityId, excludes the current activity, sorts by timestamp
   * (most recent first), and limits to 10 activities. Enriches all activities with user names in a
   * single batch request for efficiency.
   *
   * @param entityId The entity ID to filter by.
   * @param allActivities All activities in the project.
   */
  private suspend fun loadRelatedActivities(entityId: String, allActivities: List<Activity>) {
    val related =
        allActivities
            .filter { it.entityId == entityId && it.activityId != activityId }
            .sortedByDescending { it.timestamp }
            .take(10)

    if (related.isEmpty()) {
      _relatedActivities.value = emptyList()
      return
    }

    val userIds = related.map { it.userId }.toSet()
    val userNames = batchFetchUserNames(userIds)

    val enriched =
        related.map { activity ->
          val userName =
              userNames[activity.userId]
                  ?: throw IllegalStateException("User name not found for user: ${activity.userId}")
          activity.copy(metadata = activity.metadata + ("userName" to userName))
        }

    _relatedActivities.value = enriched
  }

  /**
   * Batch fetches display names for multiple users in a single Firestore query.
   *
   * Uses Firestore's whereIn query to fetch all user documents at once, which is much more
   * efficient than sequential requests (e.g., 1 request vs 10 sequential requests for 10 users).
   *
   * @param userIds Set of user IDs to fetch display names for.
   * @return Map of userId to displayName.
   * @throws IllegalStateException if any user document or displayName is not found.
   */
  private suspend fun batchFetchUserNames(userIds: Set<String>): Map<String, String> {
    if (userIds.isEmpty()) return emptyMap()

    return try {
      val userDocs =
          firestore
              .collection("users")
              .whereIn(com.google.firebase.firestore.FieldPath.documentId(), userIds.toList())
              .get()
              .await()

      userDocs.documents.associate { doc ->
        val displayName =
            doc.getString("displayName")
                ?: run {
                  android.util.Log.e(
                      "ActivityDetailViewModel", "Display name not found for user: ${doc.id}")
                  throw IllegalStateException("Display name not found for user: ${doc.id}")
                }
        doc.id to displayName
      }
    } catch (e: Exception) {
      android.util.Log.e("ActivityDetailViewModel", "Failed to batch fetch user names", e)
      throw e
    }
  }

  /**
   * Builds a formatted string with activity details for sharing.
   *
   * Returns null if no activity is loaded yet.
   *
   * @return Formatted activity details string, or null if activity is not available.
   */
  fun getShareText(): String? {
    val activity = _activity.value ?: return null

    return buildString {
      appendLine("Activity Details")
      appendLine("================")
      appendLine("Type: ${activity.activityType.name}")
      appendLine("Entity: ${activity.entityType.name}")
      appendLine("User: ${activity.metadata["userName"]?.toString() ?: "Unknown User"}")
      appendLine(
          "Time: ${ch.eureka.eurekapp.utils.Formatters.formatFullTimestamp(activity.timestamp.toDate())}")

      if (activity.metadata.containsKey("title")) {
        appendLine("Entity: ${activity.metadata["title"]}")
      }
    }
  }

  fun markShareSuccess() {
    _shareSuccess.value = true
  }

  fun clearError() {
    _errorMsg.value = null
  }
}
