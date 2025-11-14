package ch.eureka.eurekapp.e2e

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
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.ui.meeting.CreateMeetingScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingProposalVoteScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test for the complete meeting workflow.
 *
 * Test coverage: The e2e test includes the user flows:
 * - Setup: Creates test user and project in Firebase Emulator
 * - Create Meeting: Creates meeting with title + duration only (no date/time) → OPEN_TO_VOTES
 *   status
 * - Verify Listing: Meeting appears in list
 * - View Details: Opens meeting, confirms "Voting in progress" badge
 * - Test Voting: Clicks "Vote for meeting proposals", verifies voting screen UI
 * - Navigate Back: Returns to meeting details
 * - Delete Meeting: Scrolls to delete button, confirms deletion
 * - Verify Deletion: Meeting removed from list
 *
 * Key Features:
 * - No Thread.sleep() - uses waitForIdle() and waitUntil()
 * - Tests complete voting workflow for OPEN_TO_VOTES meetings
 * - Isolated testing via Firebase Emulator
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class MeetingE2ETest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var testUserId: String
  private val testUserName = "Test User"
  private val testUserEmail = "testuser@example.com"
  // Use the same project ID as NavigationMenu
  private val testProjectId = "test-project-id"

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @After
  fun tearDown() = runBlocking {
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

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

      // Fill in meeting title
      composeTestRule
          .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
          .performTextInput(meetingTitle)

      // Set duration (using runOnIdle to access the ViewModel)
      // We'll just click the duration field and let it default to a valid value
      composeTestRule
          .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION)
          .performClick()

      // Click OK to set a default duration
      composeTestRule.waitForIdle()
      composeTestRule.onNodeWithText("OK").performClick()

      composeTestRule.waitForIdle()

      // Create the meeting (without date/time to get OPEN_TO_VOTES status)
      composeTestRule
          .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
          .performClick()

      composeTestRule.waitForIdle()

      // Step 2: Verify Listing - Meeting appears in list
      composeTestRule.waitUntilExactlyOneExists(hasText(meetingTitle), timeoutMillis = 10_000)

      // Verify meeting is displayed in the list
      composeTestRule.onNodeWithText(meetingTitle).assertIsDisplayed()

      // Step 3: View Details - Opens meeting, confirms "Voting in progress" badge
      composeTestRule.onNodeWithText(meetingTitle).performClick()

      composeTestRule.waitForIdle()

      // Wait for meeting detail screen to load
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(MeetingDetailScreenTestTags.MEETING_TITLE)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Verify meeting status shows "Voting in progress"
      composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()

      // Step 4: Test Voting - Clicks "Vote for meeting proposals", verifies voting screen UI
      composeTestRule
          .onNodeWithTag(MeetingDetailScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON)
          .performClick()

      composeTestRule.waitForIdle()

      // Verify voting screen is displayed
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_SCREEN)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      composeTestRule
          .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_SCREEN_TITLE)
          .assertIsDisplayed()

      // Step 5: Navigate Back - Returns to meeting details
      composeTestRule.onNodeWithTag("BackButton").performClick()

      composeTestRule.waitForIdle()

      // Verify we're back on meeting detail screen
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(MeetingDetailScreenTestTags.MEETING_TITLE)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Step 6: Delete Meeting - Scrolls to delete button, confirms deletion
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

  private suspend fun setupTestProject(projectId: String, userId: String) {
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    // Create the project document
    val project =
        ch.eureka.eurekapp.model.data.project.Project(
            projectId = projectId,
            name = "E2E Test Project",
            description = "Project for end-to-end meeting testing",
            status = ch.eureka.eurekapp.model.data.project.ProjectStatus.OPEN,
            createdBy = userId,
            memberIds = listOf(userId))
    projectRef.set(project).await()

    // Add the test user as a member
    val member =
        ch.eureka.eurekapp.model.data.project.Member(userId = userId, role = ProjectRole.OWNER)
    val memberRef = projectRef.collection("members").document(userId)
    memberRef.set(member).await()
  }
}
