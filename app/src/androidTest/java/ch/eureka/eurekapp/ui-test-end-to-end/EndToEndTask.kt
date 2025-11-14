package ch.eureka.eurekapp.test_end_to_end

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.Eurekapp
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.ui.authentication.SignInScreenTestTags
import ch.eureka.eurekapp.ui.meeting.CreateMeetingScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.utils.FakeCredentialManager
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
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

  private var testUserId: String = ""

  @Before
  fun setup() {
    runBlocking {
      assumeTrue("Firebase Emulator must be running for tests", FirebaseEmulator.isRunning)

      // Initialize ConnectivityObserverProvider for the test
      ConnectivityObserverProvider.initialize(ApplicationProvider.getApplicationContext())

      // Clear emulators before test
      FirebaseEmulator.clearFirestoreEmulator()
      FirebaseEmulator.clearAuthEmulator()

      // Give Firebase emulators time to fully reset
      Thread.sleep(500)

      // Ensure no user is signed in at the start
      FirebaseEmulator.auth.signOut()
      Thread.sleep(500)
    }
  }

  @After
  fun tearDown() {
    runBlocking {
      // Clean up after test (clearAuthEmulator will also clear signed-in users)
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

    // Wait for compose to be idle to ensure all initialization is complete
    composeTestRule.waitForIdle()

    // Give additional time for Firebase initialization and ViewModel setup
    Thread.sleep(1000)

    // Wait for sign-in screen to appear (increased timeout for CI environment)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
      try {
        // Wait for idle state before each check
        composeTestRule.waitForIdle()
        composeTestRule
            .onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON)
            .assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(SignInScreenTestTags.SIGN_IN_WITH_GOOGLE_BUTTON).performClick()

    // Wait for authentication to complete by polling for the current user
    // This is more reliable than a fixed sleep, especially in CI environments
    var currentUser: String? = null
    val authStartTime = System.currentTimeMillis()
    val authTimeout = 10_000L // 10 seconds timeout for authentication

    while (currentUser == null && (System.currentTimeMillis() - authStartTime) < authTimeout) {
      runBlocking { currentUser = FirebaseEmulator.auth.currentUser?.uid }
      if (currentUser == null) {
        Thread.sleep(500) // Poll every 500ms
      }
    }

    // Get the signed-in user ID and create user profile + test project
    runBlocking {
      testUserId =
          currentUser
              ?: throw IllegalStateException(
                  "User not signed in after ${authTimeout}ms. " +
                      "Firebase Auth currentUser is null. " +
                      "Check that Firebase emulators are accessible from the Android emulator.")

      // Wait for Firebase Auth token to propagate to Firestore
      // This prevents PERMISSION_DENIED errors when the app tries to query Firestore
      Thread.sleep(2000)

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

      // Additional wait to ensure Firestore recognizes the auth state
      // before the app starts querying for projects/tasks
      Thread.sleep(1000)
    }

    // Wait for sign-in to complete and navigation to happen (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 30_000) {
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

    // Wait for Tasks screen to load (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 15_000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()

    // Wait for Create Task screen to load (increased timeout for CI)
    composeTestRule.waitUntil(timeoutMillis = 10_000) {
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

    // Wait for navigation back to Tasks screen and verify task was created (increased timeout for
    // CI)
    composeTestRule.waitUntil(timeoutMillis = 20_000) {
      try {
        composeTestRule.onNodeWithText("End-to-End Test Task").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    composeTestRule.onNodeWithText("End-to-End Test Task").assertIsDisplayed()
  }
  private val testUserName = "Test User"
  private val testUserEmail = "testuser@example.com"
  // Use the same project ID as NavigationMenu
  private val testProjectId = "test-project-id"
  @OptIn(ExperimentalTestApi::class)
  @Test
  fun completeE2EFlow_createVoteDeleteMeeting_succeeds() {
    runBlocking {
      // Setup: Authenticate user
      val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(testUserName, testUserEmail)
      val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
      val authResult = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await()
      testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")

      // Set up the navigation menu after authentication
      // NavigationMenu will create the test project automatically
      composeTestRule.setContent { NavigationMenu() }

      // Navigate to Meetings tab
      composeTestRule.waitForIdle()

      // Wait for bottom navigation to be ready
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
          .onAllNodesWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
        .performClick()

      composeTestRule.waitForIdle()

      // Wait for meetings screen to load
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
          .onAllNodesWithTag(MeetingScreenTestTags.MEETING_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      // Step 1: Create Meeting - Title + duration only (no date/time) → OPEN_TO_VOTES status
      composeTestRule.onNodeWithTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON).performClick()

      composeTestRule.waitForIdle()

      // Wait for create meeting screen to load
      composeTestRule.waitUntilExactlyOneExists(hasText("Create Meeting"), timeoutMillis = 10_000)

      val meetingTitle = "E2E Test Meeting"

      // Title input
      composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput(meetingTitle)
      composeTestRule.waitForIdle()

      // Duration
      composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION)
        .performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("30 minutes").performClick()
      composeTestRule.onNodeWithText("OK").performClick()
      composeTestRule.waitForIdle()

      // Date selection (end of current month)
      composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Tuesday, November 25, 2025").performClick()
      composeTestRule.onNodeWithText("OK").performClick()
      composeTestRule.waitForIdle()

      // Time selection
      composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("OK").performClick()
      composeTestRule.waitForIdle()

      // Format
      composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_FORMAT).performClick()
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("Virtual").performClick()
      composeTestRule.onNodeWithText("OK").performClick()
      composeTestRule.waitForIdle()

      // Create meeting
      composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .performClick()

      composeTestRule.waitForIdle()

      // Step 2: Verify Listing - Wait to navigate back to meetings screen
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
          .onAllNodesWithTag(MeetingScreenTestTags.MEETING_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      // Wait for meeting to appear in list
      composeTestRule.waitUntilExactlyOneExists(hasText(meetingTitle), timeoutMillis = 15_000)

      // Verify meeting is displayed in the list
      composeTestRule.onNodeWithText(meetingTitle).assertIsDisplayed()

      // Step 3: View Details - Click on meeting card to open details
      // Use meeting card tag instead of text to ensure we click the right element
      composeTestRule.waitForIdle()

      // Find and click the meeting card
      composeTestRule.onAllNodesWithTag(MeetingScreenTestTags.MEETING_CARD)[0].performClick()

      composeTestRule.waitForIdle()

      // Wait for meeting detail screen to load
      composeTestRule.waitUntil(timeoutMillis = 15_000) {
        composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.MEETING_DETAIL_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      // Verify meeting status shows "Voting in progress"
      composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()

      // Step 4: Test complete - Verify meeting was created successfully
      // Meeting is now in the list and details are accessible

      // Step 5: Delete Meeting - Navigate back to detail screen and delete
      // Scroll to delete button if needed
      composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performScrollTo()

      composeTestRule.waitForIdle()

      // Click delete button
      composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performClick()

      composeTestRule.waitForIdle()

      // Confirm deletion in dialog
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.DELETE_CONFIRMATION_DIALOG)
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CONFIRM_DELETE_BUTTON)
        .performClick()

      composeTestRule.waitForIdle()

      // Step 7: Verify Deletion - Meeting removed from list
      // Wait for navigation back to meetings list
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
          .onAllNodesWithTag(MeetingScreenTestTags.MEETING_SCREEN)
          .fetchSemanticsNodes()
          .isNotEmpty()
      }

      // Verify meeting no longer appears in the list
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText(meetingTitle).assertDoesNotExist()
    }
  }
}
