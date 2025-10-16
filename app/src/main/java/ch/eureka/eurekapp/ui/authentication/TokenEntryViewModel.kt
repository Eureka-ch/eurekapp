package ch.eureka.eurekapp.ui.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.invitation.Invitation
import ch.eureka.eurekapp.model.data.invitation.InvitationRepository
import ch.eureka.eurekapp.model.data.invitation.InvitationRepositoryProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI state for the token entry screen.
 *
 * @property token The current token input by the user.
 * @property isLoading Whether a validation operation is in progress.
 * @property errorMessage Error message to display, or null if no error.
 * @property validationSuccess True if token validation and marking as used succeeded.
 * @property userName The display name of the currently signed-in user.
 */
data class TokenEntryUIState(
    val token: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val validationSuccess: Boolean = false,
    val userName: String = ""
)

/**
 * ViewModel for the token entry screen.
 *
 * Handles token validation and marking invitations as used after successful authentication.
 *
 * @property repository The invitation repository for validation operations.
 * @property auth Firebase Auth instance to get the current user.
 */
class TokenEntryViewModel(
    private val repository: InvitationRepository = InvitationRepositoryProvider.repository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

  private val _uiState = MutableStateFlow(TokenEntryUIState())
  val uiState: StateFlow<TokenEntryUIState> = _uiState

  init {
    loadUserName()
  }

  /**
   * Loads the current user's display name from Firebase Auth.
   */
  private fun loadUserName() {
    val currentUser = auth.currentUser
    val displayName = currentUser?.displayName ?: "Guest"
    _uiState.update { it.copy(userName = displayName) }
  }

  /**
   * Updates the token input value.
   *
   * @param newToken The new token string entered by the user.
   */
  fun updateToken(newToken: String) {
    _uiState.update { it.copy(token = newToken.trim(), errorMessage = null) }
  }

  /**
   * Clears any error message.
   */
  fun clearError() {
    _uiState.update { it.copy(errorMessage = null) }
  }

  /**
   * Validates the entered token and marks it as used if valid.
   *
   * This performs the following steps:
   * 1. Validates token format (non-empty)
   * 2. Fetches invitation from repository
   * 3. Validates invitation exists and is not already used
   * 4. Marks invitation as used with current user ID
   */
  fun validateToken() {
    val currentToken = _uiState.value.token

    // Basic validation
    if (currentToken.isBlank()) {
      _uiState.update { it.copy(errorMessage = "Please enter a token") }
      return
    }

    val currentUser = auth.currentUser
    if (currentUser == null) {
      _uiState.update {
        it.copy(errorMessage = "You must be signed in to use an invitation token")
      }
      return
    }

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMessage = null) }

      try {
        // Fetch invitation by token
        val invitation = repository.getInvitationByToken(currentToken).firstOrNull()

        if (invitation == null) {
          _uiState.update {
            it.copy(
                isLoading = false,
                errorMessage = "Invalid token. Please check and try again.")
          }
          return@launch
        }

        // Check if already used
        if (invitation.isUsed) {
          _uiState.update {
            it.copy(isLoading = false, errorMessage = "This token has already been used.")
          }
          return@launch
        }

        // Mark as used
        val result = repository.markInvitationAsUsed(currentToken, currentUser.uid)

        result.fold(
            onSuccess = {
              _uiState.update { it.copy(isLoading = false, validationSuccess = true) }
            },
            onFailure = { error ->
              _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage =
                        error.message ?: "Failed to validate token. Please try again.")
              }
            })
      } catch (e: Exception) {
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMessage = "An unexpected error occurred: ${e.localizedMessage}")
        }
      }
    }
  }
}
