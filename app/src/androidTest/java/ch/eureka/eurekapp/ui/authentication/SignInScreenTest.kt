package ch.eureka.eurekapp.ui.authentication

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON
import ch.eureka.eurekapp.utils.FakeCredentialManager
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Portions of this code were generated with the help of Grok.

@RunWith(AndroidJUnit4::class)
class SignInScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val uiDevice: UiDevice =
      UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

  @Before
  fun setup() {
    assumeTrue("Firebase Emulator is not running", FirebaseEmulator.isRunning)
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  @After
  fun tearDown() {
    // Reset emulators to avoid cross-test leakage
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
    // Re-enable network radios
    uiDevice.executeShellCommand("svc wifi enable")
    uiDevice.executeShellCommand("svc data enable")
  }

  @Test
  fun signInScreen_displaysTitleAndButtonCorrectly() {
    composeTestRule.setContent { SignInScreen() }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_TITLE).assertIsDisplayed()

    composeTestRule.onNodeWithTag(SIGN_IN_WITH_GOOGLE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun signInScreen_successfulSignInTriggersOnSignedInCallback() {
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
  fun signInScreen_canSignInWithExistingAccount() {
    val email = "existing@test.com"
    val fakeIdToken =
        FakeJwtGenerator.createFakeGoogleIdToken(name = "Existing User", email = email)
    val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
    runTest {
      val user = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await().user

      assertNotNull(user)
    }

    FirebaseEmulator.auth.signOut()
    composeTestRule.waitUntil(3000) { FirebaseEmulator.auth.currentUser == null }

    composeTestRule.setContent {
      SignInScreen(credentialManager = FakeCredentialManager.create(fakeIdToken))
    }

    composeTestRule.onNodeWithTag(SIGN_IN_WITH_GOOGLE_BUTTON).assertIsDisplayed().performClick()

    composeTestRule.waitUntil(5000) { FirebaseEmulator.auth.currentUser != null }

    assertEquals(email, FirebaseEmulator.auth.currentUser!!.email)
  }

  @Test
  fun signInScreen_autoSignInOnline() {
    val fakeEmail = "auto.login@example.com"
    val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken("Auto User", fakeEmail)

    // Sign in the user
    FirebaseEmulator.createGoogleUser(fakeIdToken)
    val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
    runTest {
      val user = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await().user
      assertNotNull("User should be signed in programmatically", user)
    }

    // Now test that SignInScreen auto-navigates when opened with an already logged-in user
    val hasNavigated = AtomicBoolean(false)
    composeTestRule.setContent {
      SignInScreen(
          credentialManager = FakeCredentialManager.create(fakeIdToken),
          onSignedIn = { hasNavigated.set(true) })
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) { hasNavigated.get() }

    assertTrue("Auto sign-in should trigger onSignedIn callback immediately", hasNavigated.get())
  }

  @Test
  fun signInScreen_autoSignInOffline() {
    val fakeEmail = "offline.auto@example.com"
    val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken("Offline User", fakeEmail)

    // Sign in the user
    FirebaseEmulator.createGoogleUser(fakeIdToken)
    val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
    runTest {
      val user = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await().user
      assertNotNull("User should be signed in programmatically", user)
    }

    // Simulate offline: Disable WiFi and mobile data
    uiDevice.executeShellCommand("svc wifi disable")
    uiDevice.executeShellCommand("svc data disable")

    // Test auto-login in offline scenario - Firebase Auth maintains local state
    val hasNavigated = AtomicBoolean(false)
    composeTestRule.setContent {
      SignInScreen(
          credentialManager = FakeCredentialManager.create(fakeIdToken),
          onSignedIn = { hasNavigated.set(true) })
    }
    composeTestRule.waitUntil(timeoutMillis = 5000) { hasNavigated.get() }

    assertTrue("Auto sign-in should work offline with cached user", hasNavigated.get())
  }
}
