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

@ExperimentalCoroutinesApi
class AuthRepositoryFirebaseTest {

  private val mockAuth = mockk<FirebaseAuth>()
  private val mockHelper = mockk<GoogleSignInHelper>()
  private val repo = AuthRepositoryFirebase(mockAuth, mockHelper)

  @Test
  fun `signInWithGoogle returns success when valid credential`() = runTest {
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
  fun `signInWithGoogle fails when wrong credential type`() = runTest {
    val wrongCredential = mockk<CustomCredential>()
    every { wrongCredential.type } returns "WRONG_TYPE"
    val result = repo.signInWithGoogle(wrongCredential)
    assertTrue(result.isFailure)
  }

  @Test
  fun `signInWithGoogle fails when user is null`() = runTest {
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
  fun `signInWithGoogle handles exception`() = runTest {
    val credential = mockk<CustomCredential>()
    every { credential.type } returns TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    every { credential.data } returns bundleOf()
    every { mockHelper.extractIdTokenCredential(any()) } throws IllegalArgumentException("Bad data")

    val result = repo.signInWithGoogle(credential)
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message!!.contains("Login failed"))
  }

  @Test
  fun `signOut returns success`() {
    every { mockAuth.signOut() } returns Unit
    val result = repo.signOut()
    assertTrue(result.isSuccess)
  }

  @Test
  fun `signOut returns failure on exception`() {
    every { mockAuth.signOut() } throws RuntimeException("boom")
    val result = repo.signOut()
    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message!!.contains("Logout failed"))
  }
}

