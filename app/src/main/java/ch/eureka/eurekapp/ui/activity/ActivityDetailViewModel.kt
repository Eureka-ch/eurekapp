/*
 * This file was co-authored by Claude Code.
 */
package ch.eureka.eurekapp.ui.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestorePaths
import ch.eureka.eurekapp.model.data.activity.Activity
import ch.eureka.eurekapp.model.data.activity.ActivityRepository
import ch.eureka.eurekapp.model.data.activity.EntityType
import ch.eureka.eurekapp.model.data.activity.FirestoreActivityRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UI state for the activity detail screen.
 *
 * @property activity The activity to display, or null if loading/not found.
 * @property entityDetails Additional details about the related entity.
 * @property isLoading Whether the data is currently being fetched.
 * @property errorMsg An error message if something went wrong, or null otherwise.
 */
data class ActivityDetailUIState(
    val activity: Activity? = null,
    val entityDetails: Map<String, Any> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null
)

/**
 * ViewModel for the activity detail screen.
 *
 * Loads a specific activity and enriches it with related entity information.
 *
 * @param repository The [ActivityRepository] for fetching activity data.
 * @param firestore The [FirebaseFirestore] instance for fetching entity details.
 */
class ActivityDetailViewModel(
    private val repository: ActivityRepository = FirestoreActivityRepository(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(ActivityDetailUIState())
  val uiState: StateFlow<ActivityDetailUIState> = _uiState

  /**
   * Loads an activity and its related entity details.
   *
   * @param activityId The unique ID of the activity to load.
   */
  fun loadActivity(activityId: String) {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMsg = null) }

      try {
        // Fetch the activity
        val activity = repository.getActivityById(activityId)

        if (activity == null) {
          _uiState.update {
            it.copy(isLoading = false, errorMsg = "Activity not found")
          }
          return@launch
        }

        // Enrich with user name and project name
        val userName = fetchUserName(activity.userId)
        val projectName = fetchProjectName(activity.projectId)
        val enrichedActivity = activity.copy(
            metadata = activity.metadata + ("userName" to userName) + ("projectName" to projectName)
        )

        // Fetch entity details based on type
        val entityDetails = fetchEntityDetails(enrichedActivity)

        _uiState.update {
          it.copy(
              activity = enrichedActivity,
              entityDetails = entityDetails,
              isLoading = false
          )
        }
      } catch (e: Exception) {
        _uiState.update {
          it.copy(isLoading = false, errorMsg = e.message ?: "Unknown error")
        }
      }
    }
  }

  /**
   * Deletes the current activity.
   */
  fun deleteActivity() {
    val activityId = _uiState.value.activity?.activityId ?: return

    viewModelScope.launch {
      repository.deleteActivity(activityId).onFailure { e ->
        _uiState.update { it.copy(errorMsg = e.message) }
      }
    }
  }

  /**
   * Clears the error message.
   */
  fun clearError() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Fetches the display name for a user.
   */
  private suspend fun fetchUserName(userId: String): String {
    return try {
      val userDoc = firestore
          .collection(FirestorePaths.USERS)
          .document(userId)
          .get()
          .await()
      userDoc.getString("displayName") ?: "Unknown User"
    } catch (e: Exception) {
      "Unknown User"
    }
  }

  /**
   * Fetches the project name.
   */
  private suspend fun fetchProjectName(projectId: String): String {
    // If projectId is empty, return "None"
    if (projectId.isEmpty()) {
      return "None"
    }

    return try {
      val projectDoc = firestore
          .collection(FirestorePaths.PROJECTS)
          .document(projectId)
          .get()
          .await()

      // If project doesn't exist or has no name, return "None"
      if (!projectDoc.exists()) {
        "None"
      } else {
        projectDoc.getString("name") ?: "None"
      }
    } catch (e: Exception) {
      "None"
    }
  }

  /**
   * Fetches additional details about the entity related to this activity.
   */
  private suspend fun fetchEntityDetails(activity: Activity): Map<String, Any> {
    return try {
      when (activity.entityType) {
        EntityType.MEETING -> fetchMeetingDetails(activity.projectId, activity.entityId)
        EntityType.TASK -> fetchTaskDetails(activity.projectId, activity.entityId)
        EntityType.FILE -> fetchFileDetails(activity.entityId)
        EntityType.PROJECT -> fetchProjectDetails(activity.projectId)
        EntityType.MEMBER -> fetchMemberDetails(activity.projectId, activity.entityId)
        EntityType.MESSAGE -> fetchMessageDetails(activity.projectId, activity.entityId)
      }
    } catch (e: Exception) {
      emptyMap()
    }
  }

  private suspend fun fetchMeetingDetails(projectId: String, meetingId: String): Map<String, Any> {
    val doc = firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.MEETINGS)
        .document(meetingId)
        .get()
        .await()

    return buildMap {
      doc.getString("title")?.let { put("title", it) }
      doc.getString("description")?.let { put("description", it) }
      doc.getString("status")?.let { put("status", it) }
      doc.getTimestamp("datetime")?.let { put("datetime", it) }
      doc.getLong("duration")?.let { put("duration", it) }
      doc.getString("location")?.let { put("location", it) }
    }
  }

  private suspend fun fetchTaskDetails(projectId: String, taskId: String): Map<String, Any> {
    val doc = firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection(FirestorePaths.TASKS)
        .document(taskId)
        .get()
        .await()

    return buildMap {
      doc.getString("title")?.let { put("title", it) }
      doc.getString("description")?.let { put("description", it) }
      doc.getString("status")?.let { put("status", it) }
      doc.getTimestamp("dueDate")?.let { put("dueDate", it) }
      doc.get("assignedUserIds")?.let { put("assignedUserIds", it) }
    }
  }

  private suspend fun fetchFileDetails(fileUrl: String): Map<String, Any> {
    // For files, the entity ID is the download URL
    // We can extract file name from the URL or use metadata
    return mapOf("downloadUrl" to fileUrl)
  }

  private suspend fun fetchProjectDetails(projectId: String): Map<String, Any> {
    val doc = firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .get()
        .await()

    return buildMap {
      doc.getString("name")?.let { put("name", it) }
      doc.getString("description")?.let { put("description", it) }
      doc.get("memberIds")?.let { put("memberIds", it) }
    }
  }

  private suspend fun fetchMemberDetails(projectId: String, userId: String): Map<String, Any> {
    val memberDoc = firestore
        .collection(FirestorePaths.PROJECTS)
        .document(projectId)
        .collection("members")
        .document(userId)
        .get()
        .await()

    val userDoc = firestore
        .collection(FirestorePaths.USERS)
        .document(userId)
        .get()
        .await()

    return buildMap {
      userDoc.getString("displayName")?.let { put("displayName", it) }
      userDoc.getString("email")?.let { put("email", it) }
      memberDoc.getString("role")?.let { put("role", it) }
    }
  }

  private suspend fun fetchMessageDetails(projectId: String, messageId: String): Map<String, Any> {
    // Note: Messages are in chatChannels/{channelId}/messages subcollection
    // We would need the channelId to fetch the message properly
    // For now, return empty map - future enhancement would store channelId in activity metadata
    return emptyMap()
  }
}
