/*
The following code comes from the solution of the part 3 of the SwEnt bootcamp made by the SwEnt team:
https://github.com/swent-epfl/bootcamp-25-B3-Solution/blob/main/app/src/main/java/com/github/se/bootcamp/ui/authentication/SignInViewModel.kt
Portions of this code were generated with the help of Grok.
*/

package ch.eureka.eurekapp.ui.authentication

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.authentication.AuthRepositoryProvider
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.data.user.UserRepositoryProvider
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.FirebaseMessaging
import kotlin.math.exp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

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
 * @property userRepository The repository used to save user profile data.
 */
class SignInViewModel(
    private val repository: AuthRepository = AuthRepositoryProvider.repository,
    private val userRepository: UserRepository = UserRepositoryProvider.repository
) : ViewModel() {

  private val _uiState = MutableStateFlow(AuthUIState())
  val uiState: StateFlow<AuthUIState> = _uiState

  init {
    // Check if user is already signed in for offline support
    val currentUser = repository.getCurrentUser()
    if (currentUser != null) {
      updateUserFcmToken()
      _uiState.update { it.copy(user = currentUser, signedOut = false) }
    }
  }

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
  ) =
      try {
        credentialManager.getCredential(context, request).credential
      } catch (_: Exception) {
        null
      }

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
        if (credential == null) {
          throw NoCredentialException("No credentials")
        }

        // Pass the credential to the repository
        repository.signInWithGoogle(credential).fold({ firebaseUser ->
          viewModelScope.launch {
            // Check if user already exists in database
            val existingUser = userRepository.getUserById(firebaseUser.uid).firstOrNull()

            val userToSave =
                existingUser?.copy(
                    photoUrl = firebaseUser.photoUrl?.toString() ?: existingUser.photoUrl,
                    lastActive = Timestamp.now())
                    ?: User(
                        uid = firebaseUser.uid,
                        displayName = firebaseUser.displayName ?: "",
                        email = firebaseUser.email ?: "",
                        photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                        lastActive = Timestamp.now())

            userRepository
                .saveUser(userToSave)
                .fold(
                    onSuccess = {
                      _uiState.update {
                        it.copy(
                            isLoading = false,
                            user = firebaseUser,
                            errorMsg = null,
                            signedOut = false)
                      }
                    },
                    onFailure = { error ->
                      _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMsg = "Failed to save user data: ${error.localizedMessage}",
                            signedOut = true,
                            user = null)
                      }
                    })
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
  /** Update users fcm Token in order to send notifications to the user */
  fun updateUserFcmToken() {
    viewModelScope.launch {
      val newToken = FirebaseMessaging.getInstance().token.await()
      val currentUser = userRepository.getCurrentUser().first()
      var retries = 0.0
      var result: Result<Unit>
      if (currentUser != null && currentUser.fcmToken != newToken) {
        do {
          result = userRepository.updateFcmToken(currentUser.uid, newToken)
          if (result.isFailure) {
            delay(1000L * exp(retries).toLong())
            retries += 1
            if (retries >= 4) {
              break
            }
          }
        } while (result.isFailure)
      }
    }
  }
}
