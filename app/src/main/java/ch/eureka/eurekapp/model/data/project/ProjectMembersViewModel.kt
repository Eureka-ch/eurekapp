/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.project

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.navigation.HEARTBEAT_DURATION
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * ViewModel for the Project Members screen.
 *
 * This ViewModel is responsible for fetching and managing the list of members for a specific
 * project. It retrieves the member IDs from the project document and then fetches the corresponding
 * user details.
 *
 * @property projectId The unique identifier of the project for which members are being loaded.
 * @property projectsRepository Repository to fetch projects (default:
 *   RepositoriesProvider.projectRepository)
 * @property usersRepository Repository to fetch user information (default:
 *   RepositoriesProvider.userRepository)
 */
class ProjectMembersViewModel(
    private val projectId: String,
    private val projectsRepository: ProjectRepository = RepositoriesProvider.projectRepository,
    private val usersRepository: UserRepository = RepositoriesProvider.userRepository
) : ViewModel() {

  /** Internal mutable state flow for the UI state. */
  private val _uiState = MutableStateFlow<MembersUiState>(MembersUiState.Loading)

  /** Public immutable state flow representing the current UI state of the members screen. */
  val uiState: StateFlow<MembersUiState> = _uiState.asStateFlow()

  init {
    loadMembers()
  }

  /**
   * Factory class for creating instances of [ProjectMembersViewModel].
   *
   * @property projectId The ID of the project.
   */
  class Factory(
      private val projectId: String,
  ) : ViewModelProvider.Factory {
    /**
     * Creates a new instance of the given `Class`.
     *
     * @param modelClass a `Class` whose instance is requested.
     * @return a newly created ViewModel.
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return ProjectMembersViewModel(projectId) as T
    }
  }

  /**
   * Asynchronously loads the members of the project.
   *
   * This function performs the following steps:
   * 1. Fetches the list of member UIDs from the project's "members" subcollection.
   * 2. Fetches the [User] document for each UID.
   * 3. Updates the [_uiState] with the list of loaded users, sorted by display name.
   *
   * In case of an error, it updates the state to [MembersUiState.Error].
   */
  fun loadMembers() {
    viewModelScope.launch {
      _uiState.value = MembersUiState.Loading
      try {

        val project =
            projectsRepository.getProjectById(projectId).first()
                ?: throw IllegalArgumentException("Project is null")

        if (project.memberIds.isEmpty()) {
          _uiState.value = MembersUiState.Success(project.name, emptyList())
          return@launch
        }

        _uiState.value = MembersUiState.Success(project.name, fetchUsers(project.memberIds))
      } catch (e: FirebaseFirestoreException) {
        val message =
            when (e.code) {
              FirebaseFirestoreException.Code.UNAVAILABLE -> "Service Unavailable"
              FirebaseFirestoreException.Code.PERMISSION_DENIED ->
                  "You don't have permission to view members"
              else -> "Failed to load members: ${e.message}"
            }
        _uiState.value = MembersUiState.Error(message)
        Log.e("ProjectMembersViewModel", message, e)
      } catch (e: Exception) {
        _uiState.value = MembersUiState.Error("Failed to load members")
        Log.e(
            "ProjectMembersViewModel",
            "Failed to load members: ${e.message ?: "No error message"}",
            e)
      }
    }
  }

  /**
   * Fetch users from the firestore database.
   *
   * @param memberIds The IDs of the users to fetch.
   * @return the list of the users fetched sorted by their display name.
   */
  private suspend fun fetchUsers(memberIds: List<String>): List<User> {
    val users = mutableListOf<User>()

    memberIds.forEach { uid ->
      try {
        val user = usersRepository.getUserById(uid).first()
        if (user != null) {
          users.add(user)
        } else {
          Log.e("ProjectMembersViewModel", "Fetched user was null.")
        }
      } catch (e: Exception) {
        Log.e("ProjectMembersViewModel", "Error fetching user $uid", e)
      }
    }

    return users.sortedBy { it.displayName }
  }

  /**
   * Logic to determine if a user is online.
   *
   * A user is considered online if their [lastActive] timestamp is within the last
   * [HEARTBEAT_DURATION] milliseconds.
   *
   * @param lastActive The timestamp of the user's last activity.
   * @return True if the user is considered online, false otherwise.
   */
  fun isUserOnline(lastActive: Timestamp): Boolean {
    if (lastActive == Timestamp(0, 0)) return false

    val lastActiveTime = lastActive.toDate().time
    val currentTime = System.currentTimeMillis()

    return (currentTime - lastActiveTime) < HEARTBEAT_DURATION
  }
}

/** Sealed class representing the different states of the Project Members UI. */
sealed class MembersUiState {
  /** State indicating that data is currently being loaded. */
  object Loading : MembersUiState()

  /**
   * State indicating that the members have been successfully loaded.
   *
   * @property members The list of [User] objects representing the project members.
   */
  data class Success(val projectName: String, val members: List<User>) : MembersUiState()

  /**
   * State indicating that an error occurred while loading members.
   *
   * @property message A descriptive error message.
   */
  data class Error(val message: String) : MembersUiState()
}
