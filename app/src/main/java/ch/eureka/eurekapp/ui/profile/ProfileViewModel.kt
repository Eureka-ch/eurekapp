package ch.eureka.eurekapp.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.data.user.UserRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the profile screen.
 *
 * @property user The current user's data, or null if not available.
 * @property isEditing Whether the profile is currently in editing mode.
 */
data class ProfileUIState(val user: User? = null, val isEditing: Boolean = false)

/**
 * ViewModel for the profile screen.
 *
 * @property userRepository Repository for user data operations.
 * @property userId The ID of the currently authenticated user.
 */
class ProfileViewModel(
    private val userRepository: UserRepository = UserRepositoryProvider.repository,
    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
) : ViewModel() {

  private val _uiState = MutableStateFlow(ProfileUIState())
  val uiState: StateFlow<ProfileUIState> = _uiState

  init {
    loadUserProfile()
  }

  /** Loads the current user's profile data. */
  private fun loadUserProfile() {
    val uid = userId ?: throw IllegalStateException("User ID is null - user must be authenticated")

    viewModelScope.launch {
      userRepository.getUserById(uid).collect { user -> _uiState.update { it.copy(user = user) } }
    }
  }

  /** Sets the editing mode. */
  fun setEditing(isEditing: Boolean) {
    _uiState.update { it.copy(isEditing = isEditing) }
  }

  /** Updates the user's display name. */
  fun updateDisplayName(newDisplayName: String) {
    val currentUser = _uiState.value.user ?: throw IllegalStateException("Cannot update display name - user data is not available")

    viewModelScope.launch {
      val updatedUser = currentUser.copy(displayName = newDisplayName)
      userRepository.saveUser(updatedUser)
      _uiState.update { it.copy(isEditing = false) }
    }
  }
}
