// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.model.authentication

import androidx.core.os.bundleOf
import androidx.credentials.CustomCredential
import com.google.android.gms.tasks.Tasks
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test

// Portions of this code were generated with the help of Grok.

@ExperimentalCoroutinesApi
class AuthRepositoryFirebaseTest {

  private val mockAuth = mockk<FirebaseAuth>()
  private val mockHelper = mockk<GoogleSignInHelper>()
  private val repo = AuthRepositoryFirebase(mockAuth, mockHelper)

  @Test
  fun authRepositoryFirebase_signInWithGoogleReturnsSuccessWhenValidCredential() = runTest {
    val mockCredential = mockk<CustomCredential>()
    val mockData = bundleOf("id_token" to "fakeToken")
    val mockFirebaseCred = mockk<AuthCredential>()
    val mockUser = mockk<FirebaseUser>()
    val mockAuthResult = mockk<AuthResult>()

    every { mockCredential.type } returns TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    every { mockCredential.data } returns mockData
    every { mockHelper.extractIdTokenCredential(mockData).idToken } returns "fakeToken"
    every { mockHelper.toFirebaseCredential("fakeToken") } returns mockFirebaseCred
    every { mockAuthResult.user } returns mockUser

    val completedTask = Tasks.forResult(mockAuthResult)
    every { mockAuth.signInWithCredential(mockFirebaseCred) } returns completedTask

    val result = repo.signInWithGoogle(mockCredential)

    assertTrue(result.isSuccess)
    assertEquals(mockUser, result.getOrNull())
  }

  @Test
  fun authRepositoryFirebase_signInWithGoogleFailsWhenWrongCredentialType() = runTest {
    val wrongCredential = mockk<CustomCredential>()
    every { wrongCredential.type } returns "WRONG_TYPE"
    val result = repo.signInWithGoogle(wrongCredential)
    assertTrue(result.isFailure)
  }

  @Test
  fun authRepositoryFirebase_signInWithGoogleFailsWhenUserIsNull() = runTest {
    val mockCredential = mockk<CustomCredential>()
    val mockData = bundleOf("id_token" to "fakeToken")
    val mockFirebaseCred = mockk<AuthCredential>()
    val mockAuthResult = mockk<AuthResult>()

    every { mockCredential.type } returns TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    every { mockCredential.data } returns mockData
    every { mockHelper.extractIdTokenCredential(mockData).idToken } returns "fakeToken"
    every { mockHelper.toFirebaseCredential("fakeToken") } returns mockFirebaseCred
    every { mockAuthResult.user } returns null

    val completedTask = Tasks.forResult(mockAuthResult)
    every { mockAuth.signInWithCredential(mockFirebaseCred) } returns completedTask

    val result = repo.signInWithGoogle(mockCredential)

    assertTrue(result.isFailure)
    val msg = result.exceptionOrNull()?.message ?: ""
    assertTrue(msg.contains("Could not retrieve user"))
  }

  @Test
  fun authRepositoryFirebase_signInWithGoogleHandlesException() = runTest {
    val credential = mockk<CustomCredential>()
    every { credential.type } returns TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    every { credential.data } returns bundleOf()
    every { mockHelper.extractIdTokenCredential(any()) } throws IllegalArgumentException("Bad data")

    val result = repo.signInWithGoogle(credential)
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message!!.contains("Login failed"))
  }

  @Test
  fun authRepositoryFirebase_signOutReturnsSuccess() {
    every { mockAuth.signOut() } returns Unit
    val result = repo.signOut()
    assertTrue(result.isSuccess)
  }

  @Test
  fun authRepositoryFirebase_signOutReturnsFailureOnException() {
    every { mockAuth.signOut() } throws RuntimeException("boom")
    val result = repo.signOut()
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message!!.contains("Logout failed"))
  }

  @Test
  fun authRepositoryFirebase_getCurrentUserLoggedIn() {
    val mockUser = mockk<FirebaseUser>()
    every { mockAuth.currentUser } returns mockUser

    val result = repo.getCurrentUser()

    assertEquals(mockUser, result)
  }

  @Test
  fun authRepositoryFirebase_getCurrentUserNotLoggedIn() {
    every { mockAuth.currentUser } returns null

    val result = repo.getCurrentUser()

    assertEquals(null, result)
  }

  @Test
  fun authRepositoryFirebase_getUserIdLoggedIn() {
    val mockUser = mockk<FirebaseUser>()
    every { mockUser.uid } returns "test-uid-123"
    every { mockAuth.currentUser } returns mockUser

    val result = repo.getUserId()

    assertTrue(result.isSuccess)
    assertEquals("test-uid-123", result.getOrNull())
  }

  @Test
  fun authRepositoryFirebase_getUserIdNotLoggedIn() {
    every { mockAuth.currentUser } returns null

    val result = repo.getUserId()

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message!!.contains("No user logged in"))
  }
}
