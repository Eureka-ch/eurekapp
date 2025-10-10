/*
The following code comes from the solution of the part 3 of the SwEnt bootcamp made by the SwEnt team:
https://github.com/swent-epfl/bootcamp-25-B3-Solution/blob/main/app/src/main/java/com/github/se/bootcamp/ui/authentication/SignInViewModel.kt
*/

package ch.eureka.eurekapp.ui.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.authentication.AuthRepositoryProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state for authentication.
 *
 * @property isLoading Whether an authentication operation is in progress.
 * @property user The currently signed-in [FirebaseUser], or null if not signed in.
 * @property errorMsg An error message to display, or null if there is no error.
 * @property signedOut True if a sign-out operation has completed.
 */
data class AuthUIState(
    val isLoading: Boolean = false,
    val user: FirebaseUser? = null,
    val errorMsg: String? = null,
    val signedOut: Boolean = false
)

/**
 * ViewModel for the SignIn view.
 *
 * @property repository The repository used to perform authentication operations.
 */
class SignInViewModel(private val repository: AuthRepository = AuthRepositoryProvider.repository) :
    ViewModel() {

  private val _uiState = MutableStateFlow(AuthUIState())
  val uiState: StateFlow<AuthUIState> = _uiState

  /** Clears the error message in the UI state. */
  fun clearErrorMsg() {
    _uiState.update { it.copy(errorMsg = null) }
  }

  /**
   * Builds a request to retrieve the user's Google ID Token.
   *
   * @param context The [Context] instance used to access the application's string resources.
   * @return A [GetSignInWithGoogleOption] instance configured with the server client ID of the
   *   backend.
   */
  private fun getSignInOptions(context: Context) =
      GetSignInWithGoogleOption.Builder(
              serverClientId = context.getString(ch.eureka.eurekapp.R.string.web_client_id))
          .build()

  /**
   * Builds a credential request for the "Sign In with Google" flow.
   *
   * @param signInOptions The Google Sign-In configuration option that defines how the sign-in flow
   *   should request the user's credential.
   * @return A [GetCredentialRequest] containing the specified sign-in option.
   */
  private fun signInRequest(signInOptions: GetSignInWithGoogleOption) =
      GetCredentialRequest.Builder().addCredentialOption(signInOptions).build()

  /**
   * Executes a credential retrieval request using the Android Credential Manager.
   *
   * The suspending function launches the Sign In flow defined by the provided
   * [GetCredentialRequest]. When the user completes the flow, the resulting
   * [androidx.credentials.Credential] is returned.
   *
   * @param context The context used to display any necessary UI and access resources during the
   *   sign-in process.
   * @param request The credential request.
   * @param credentialManager The instance of [CredentialManager] used to execute the credential
   *   retrieval operation.
   * @return the resulting credential of the user.
   */
  private suspend fun getCredential(
      context: Context,
      request: GetCredentialRequest,
      credentialManager: CredentialManager
  ) = credentialManager.getCredential(context, request).credential

  /**
   * Initiates the Google sign-in flow and updates the UI state on success or failure.
   *
   * @param context The current context of the app
   * @param credentialManager A [CredentialManager] instance to manage the user's authentification
   *   flow
   */
  fun signIn(context: Context, credentialManager: CredentialManager) {
    if (_uiState.value.isLoading) return

    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, errorMsg = null) }

      val signInOptions = getSignInOptions(context)
      val signInRequest = signInRequest(signInOptions)

      try {
        // Launch Credential Manager UI
        val credential = getCredential(context, signInRequest, credentialManager)

        // Pass the credential to the repository
        repository.signInWithGoogle(credential).fold({ user ->
          _uiState.update {
            it.copy(isLoading = false, user = user, errorMsg = null, signedOut = false)
          }
        }) { failure ->
          _uiState.update {
            it.copy(
                isLoading = false,
                errorMsg = failure.localizedMessage,
                signedOut = true,
                user = null)
          }
        }
      } catch (_: GetCredentialCancellationException) {
        // User cancelled the sign-in flow
        _uiState.update {
          it.copy(isLoading = false, errorMsg = "Sign-in cancelled", signedOut = true, user = null)
        }
      } catch (e: androidx.credentials.exceptions.GetCredentialException) {
        // Other credential errors
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = "Failed to get credentials: ${e.localizedMessage}",
              signedOut = true,
              user = null)
        }
      } catch (e: Exception) {
        // Unexpected errors
        _uiState.update {
          it.copy(
              isLoading = false,
              errorMsg = "Unexpected error: ${e.localizedMessage}",
              signedOut = true,
              user = null)
        }
      }
    }
  }
}
