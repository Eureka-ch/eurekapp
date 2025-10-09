package ch.eureka.eurekapp.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON
import ch.eureka.eurekapp.utils.FakeCredentialManager
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() {
    assumeTrue("Firebase Emulator is not running", FirebaseEmulator.isRunning)
    FirebaseEmulator.clearAuthEmulator()
  }

  @Test
  fun signInScreen_displaysTitleAndButtonCorrectly() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_TITLE).assertIsDisplayed()

    composeTestRule.onNodeWithTag(SIGN_IN_WITH_GOOGLE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun successfulSignIn_triggersOnSignedInCallback() {
    val fakeName = "Ada Lovelace"
    val fakeEmail = "ada.lovelace@example.com"
    val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(fakeName, fakeEmail)

    FirebaseEmulator.createGoogleUser(fakeIdToken)

    val fakeCredentialManager = FakeCredentialManager.create(fakeIdToken)

    val hasNavigated = AtomicBoolean(false)

    composeTestRule.setContent {
      SignInScreen(
          credentialManager = fakeCredentialManager, onSignedIn = { hasNavigated.set(true) })
    }

    composeTestRule.onNodeWithTag(SIGN_IN_WITH_GOOGLE_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) { hasNavigated.get() }

    assertTrue("The onSignedIn callback should have been called.", hasNavigated.get())
  }

  @Test
  fun canSignInWithExistingAccount() {
    val email = "existing@test.com"
    val fakeIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken(name = "Existing User", email = email)
    val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
    runTest {
      val user = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await().user

      assertNotNull(user)
    }

    FirebaseEmulator.auth.signOut()

    composeTestRule.setContent {
      SignInScreen(credentialManager = FakeCredentialManager.create(fakeIdToken))
    }

    composeTestRule.onNodeWithTag(SIGN_IN_WITH_GOOGLE_BUTTON).assertIsDisplayed().performClick()

    composeTestRule.waitUntil(5000) { FirebaseEmulator.auth.currentUser != null }

    assertEquals(email, FirebaseEmulator.auth.currentUser!!.email)
  }
}
