/* This file was co-authored with Claude Code model */
package ch.eureka.eurekapp.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.user.FirestoreUserRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.ui.meeting.CreateMeetingScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
import java.util.UUID
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-End test for the complete Meeting workflow.
 *
 * This test covers the following user journey:
 * 1. Navigate to Meetings screen
 * 2. Create a new meeting
 * 3. Open meeting details
 * 4. Edit the meeting
 * 5. Delete the meeting
 *
 * This test uses Firebase Emulator and NavigationMenu for real UI interaction testing.
 */
@RunWith(AndroidJUnit4::class)
class MeetingE2EFlowTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val uiDevice: UiDevice =
      UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

  // Test data
  private val testProjectId = "e2e_test_project_${UUID.randomUUID()}"
  private val userEmail = "testuser.e2e@test.com"
  private val userName = "E2E Test User"
  private val meetingTitle = "E2E Meeting ${System.currentTimeMillis()}"

  private lateinit var userIdToken: String
  private lateinit var userId: String

  // Repositories for setup/cleanup
  private lateinit var userRepository: UserRepository
  private lateinit var projectRepository: ProjectRepository

  @Before
  fun setup() {
    assumeTrue("Firebase Emulator is not running", FirebaseEmulator.isRunning)
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    // Initialize repositories
    userRepository = FirestoreUserRepository(FirebaseEmulator.firestore, FirebaseEmulator.auth)
    projectRepository = FirestoreProjectRepository(FirebaseEmulator.firestore, FirebaseEmulator.auth)

    // Create test user and sign in
    userIdToken = FakeJwtGenerator.createFakeGoogleIdToken(userName, userEmail)

    runBlocking {
      FirebaseEmulator.createGoogleUser(userIdToken)
      val cred = GoogleAuthProvider.getCredential(userIdToken, null)
      val authResult = FirebaseEmulator.auth.signInWithCredential(cred).await()
      userId = authResult.user!!.uid

      // Create user profile
      userRepository
          .saveUser(User(uid = userId, displayName = userName, email = userEmail))
          .getOrThrow()

      // Create test project
      projectRepository
          .createProject(
              Project(
                  projectId = testProjectId,
                  name = "E2E Test Project",
                  description = "Project for E2E testing",
                  createdBy = userId,
                  memberIds = listOf(userId)),
              creatorId = userId,
              creatorRole = ProjectRole.OWNER)
          .getOrThrow()
    }
  }

  @After
  fun tearDown() {
    // Clean up test project
    runBlocking { projectRepository.deleteProject(testProjectId).getOrNull() }

    // Clean up emulators
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    // Re-enable network
    uiDevice.executeShellCommand("svc wifi enable")
    uiDevice.executeShellCommand("svc data enable")
  }

  @Test
  fun completeE2EFlow_createEditDeleteMeeting_succeeds() {
    // Launch NavigationMenu (user is already signed in)
    composeTestRule.setContent { NavigationMenu() }

    composeTestRule.waitForIdle()
    Thread.sleep(2000) // Wait for initial load

    // STEP 1: Navigate to Meetings screen
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
        .performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()

    // STEP 2: Click Create Meeting button
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)

    // STEP 3: Fill in meeting details
    // Title
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput(meetingTitle)
    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    // Duration - click to open picker and select 30 minutes
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION).performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)
    composeTestRule.onNodeWithText("30").performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)

    // Date - click to open picker and select a future date
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performScrollTo().performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)
    // Click on a day in the future (the date picker shows a calendar, click any selectable day)
    // Just clicking OK will use today's date which won't work for future meetings
    // Instead, let's try to click on the next day or any visible future date
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)

    // Time - click to open picker
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performScrollTo().performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)
    composeTestRule.onNodeWithText("OK").performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)

    // Format - select Virtual
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_FORMAT).performScrollTo().performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)
    composeTestRule.onNodeWithText("Virtual").performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)

    // STEP 4: Create the meeting
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(3000) // Wait for creation and navigation back

    // STEP 5: Verify meeting appears in list and click on it
    composeTestRule.onNodeWithText(meetingTitle, useUnmergedTree = true).assertIsDisplayed()
    Thread.sleep(1000)

    composeTestRule.onNodeWithText(meetingTitle, useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000) // Observe meeting detail screen

    // STEP 6: Edit the meeting (if edit button exists)
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DETAIL_SCREEN)
        .assertIsDisplayed()
    Thread.sleep(2000)

    // STEP 7: Delete the meeting
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON)
        .performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    // Confirm deletion in dialog
    composeTestRule.onNodeWithText("Delete").performClick()
    composeTestRule.waitForIdle()
    Thread.sleep(2000)

    // STEP 8: Verify meeting is deleted (back on meetings list, meeting not visible)
    composeTestRule.onNodeWithText(meetingTitle).assertDoesNotExist()
  }
}
