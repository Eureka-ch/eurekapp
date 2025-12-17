package ch.eureka.eurekapp.model.authentication

import android.os.Bundle
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import junit.framework.TestCase.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test

class DefaultGoogleSignInHelperTest {

  private lateinit var helper: DefaultGoogleSignInHelper

  @Before
  fun setUp() {
    helper = DefaultGoogleSignInHelper()
    // Mock static objects
    mockkObject(GoogleIdTokenCredential)
    mockkStatic(GoogleAuthProvider::class)
  }

  @After
  fun tearDown() {
    unmockkAll()
  }

  @Test
  fun defaultGoogleSignInHelper_extractIdTokenCredentialDelegatesToGoogleIdTokenCredentialCreateFrom() {
    val mockBundle = mockk<Bundle>()
    val expectedCredential = mockk<GoogleIdTokenCredential>()

    every { GoogleIdTokenCredential.createFrom(mockBundle) } returns expectedCredential

    val result = helper.extractIdTokenCredential(mockBundle)

    assertEquals(expectedCredential, result)
  }

  @Test
  fun defaultGoogleSignInHelper_toFirebaseCredentialDelegatesToGoogleAuthProviderGetCredential() {
    val idToken = "fake-id-token"
    val expectedAuthCredential = mockk<AuthCredential>()

    every { GoogleAuthProvider.getCredential(idToken, null) } returns expectedAuthCredential

    val result = helper.toFirebaseCredential(idToken)

    assertEquals(expectedAuthCredential, result)
  }
}
