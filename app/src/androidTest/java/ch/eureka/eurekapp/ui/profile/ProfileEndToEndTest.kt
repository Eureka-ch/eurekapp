package ch.eureka.eurekapp.ui.profile

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.Eurekapp
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepositoryProvider
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags
import ch.eureka.eurekapp.utils.FakeCredentialManager
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test for the complete user flow: Sign in → Navigate to Profile → Change Name → Save
 *
 * This test validates the entire user journey from authentication through profile editing.
 */
@RunWith(AndroidJUnit4::class)
class ProfileEndToEndTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val testUserName = "Ada Lovelace"
  private val testUserEmail = "ada.lovelace@example.com"
  private val updatedName = "Grace Hopper"

  @Before
  fun setup() {
    assumeTrue("Firebase Emulator is not running", FirebaseEmulator.isRunning)
    FirebaseEmulator.clearAuthEmulator()
    FirebaseEmulator.clearFirestoreEmulator()
  }

  @After
  fun teardown() {
    FirebaseEmulator.auth.signOut()
  }

  @Test
  fun endToEnd_signInNavigateToProfileChangeNameAndSave() {
    // Step 1: Create fake user and sign in
    val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(testUserName, testUserEmail)
    FirebaseEmulator.createGoogleUser(fakeIdToken)
    val fakeCredentialManager = FakeCredentialManager.create(fakeIdToken)

    composeTestRule.setContent { Eurekapp(credentialManager = fakeCredentialManager) }

    composeTestRule
        .onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) { FirebaseEmulator.auth.currentUser != null }

    val userId = FirebaseEmulator.auth.currentUser?.uid!!

    val initialUser =
        User(
            uid = userId,
            displayName = testUserName,
            email = testUserEmail,
            photoUrl = "",
            lastActive = Timestamp.now())

    val saveResult = runBlocking { UserRepositoryProvider.repository.saveUser(initialUser) }
    assert(saveResult.isSuccess) { "Failed to save user: ${saveResult.exceptionOrNull()}" }

    Thread.sleep(2000)

    // Step 2: Navigate to Profile Screen
    composeTestRule.waitUntil(timeoutMillis = 15000) {
      composeTestRule
          .onAllNodesWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule
          .onAllNodesWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals(testUserName)

    // Step 3: Enter edit mode
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.EDIT_BUTTON).performClick()

    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).assertIsDisplayed()

    // Step 4: Change the name
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD).performTextClearance()
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .performTextInput(updatedName)

    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD)
        .assertTextEquals("Display Name", updatedName)

    // Step 5: Save the changes
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.SAVE_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule
          .onAllNodesWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // Step 6: Verify the name was updated and saved in UI
    composeTestRule
        .onNodeWithTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT)
        .assertTextEquals(updatedName)
  }
}
