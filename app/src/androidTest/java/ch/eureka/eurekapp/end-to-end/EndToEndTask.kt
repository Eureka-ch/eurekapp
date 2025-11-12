package ch.eureka.eurekapp.end_to_end

import android.Manifest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.Eurekapp
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags
import ch.eureka.eurekapp.utils.FakeCredentialManager
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
*/

/**
 * End-to-end test for the task creation flow.
 *
 * This test validates the complete user journey:
 * 1. Sign in with Google auth
 * 2. Navigate to the Tasks tab
 * 3. Create a new task
 *
 * This ensures that the entire flow works together seamlessly.
 */
@RunWith(AndroidJUnit4::class)
class TaskEndToEndTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  private var testUserId: String = ""

  @Before
  fun setup() {
    runBlocking {
      assumeTrue("Firebase Emulator must be running for tests", FirebaseEmulator.isRunning)

      // Clear emulators before test
      FirebaseEmulator.clearFirestoreEmulator()
      FirebaseEmulator.clearAuthEmulator()
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      // Clean up after test
      FirebaseEmulator.clearFirestoreEmulator()
      FirebaseEmulator.clearAuthEmulator()
    }
  }

  @Test
  fun endToEnd_signInNavigateToTasksAndCreateTask() {
    val fakeName = "Test User"
    val fakeEmail = "testuser@eureka.com"
    val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(fakeName, fakeEmail)
    val fakeCredentialManager = FakeCredentialManager.create(fakeIdToken)

    // Create the Google user in Firebase emulator (creates account but doesn't sign in)
    FirebaseEmulator.createGoogleUser(fakeIdToken)

    composeTestRule.setContent { Eurekapp(credentialManager = fakeCredentialManager) }

    // Wait for sign-in screen to appear
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule
            .onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON).performClick()

    // Wait a moment for the sign-in to process
    Thread.sleep(3000)

    // Get the signed-in user ID and create user profile + test project
    runBlocking {
      testUserId =
          FirebaseEmulator.auth.currentUser?.uid
              ?: throw IllegalStateException("User not signed in after clicking sign-in button")

      // Create user profile in Firestore
      val userRef = FirebaseEmulator.firestore.collection("users").document(testUserId)
      val userProfile =
          mapOf(
              "uid" to testUserId,
              "displayName" to "Test User",
              "email" to "testuser@eureka.com",
              "photoUrl" to "")
      userRef.set(userProfile).await()

      // Create a test project for the user
      val projectId = "test-project-e2e"
      val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)
      val project =
          ch.eureka.eurekapp.model.data.project.Project(
              projectId = projectId,
              name = "End-to-End Test Project",
              description = "Test project for E2E testing",
              status = ch.eureka.eurekapp.model.data.project.ProjectStatus.OPEN,
              createdBy = testUserId,
              memberIds = listOf(testUserId))
      projectRef.set(project).await()

      // Add the test user as a project member
      val member =
          ch.eureka.eurekapp.model.data.project.Member(
              userId = testUserId, role = ch.eureka.eurekapp.model.data.project.ProjectRole.OWNER)
      val memberRef = projectRef.collection("members").document(testUserId)
      memberRef.set(member).await()
    }

    // Wait for sign-in to complete and navigation to happen
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      try {
        // Check if we've navigated past sign-in by looking for bottom navigation
        composeTestRule
            .onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // Wait for UI to settle
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()

    // Wait for Tasks screen to load
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()

    // Wait for Create Task screen to load
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      try {
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("End-to-End Test Task")

    composeTestRule
        .onNodeWithTag(CommonTaskTestTags.DESCRIPTION)
        .performTextInput("This task was created by the end-to-end test")

    composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("31/12/2025")

    composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()

    // Wait for navigation back to Tasks screen and verify task was created
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule.onNodeWithText("End-to-End Test Task").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("End-to-End Test Task").assertIsDisplayed()
  }
}
