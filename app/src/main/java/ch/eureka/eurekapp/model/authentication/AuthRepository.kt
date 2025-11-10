/*
The following code comes from the solution of the part 3 of the SwEnt bootcamp made by the SwEnt team:
https://github.com/swent-epfl/bootcamp-25-B3-Solution/blob/main/app/src/main/java/com/github/se/bootcamp/model/authentication/AuthRepository.kt
Portions of this code were generated with the help of Grok.
*/

package ch.eureka.eurekapp.model.authentication

import androidx.credentials.Credential
import com.google.firebase.auth.FirebaseUser

/** Handles authentication operations such as signing in with Google and signing out. */
interface AuthRepository {

  /**
   * Signs in the user using a Google account through the Credential Manager API.
   *
   * @return A [Result] containing a [FirebaseUser] on success, or an exception on failure.
   */
  suspend fun signInWithGoogle(credential: Credential): Result<FirebaseUser>

  /**
   * Signs out the currently authenticated user and clears the credential state.
   *
   * @return A [Result] indicating success or failure.
   */
  fun signOut(): Result<Unit>

  /**
   * Get user id if the user is logged int
   * *
   */
  fun getUserId(): Result<String?>

  /**
   * Gets the currently signed-in user, or null if no user is signed in.
   *
   * @return The [FirebaseUser] if signed in, or null otherwise.
   */
  fun getCurrentUser(): FirebaseUser?
}
