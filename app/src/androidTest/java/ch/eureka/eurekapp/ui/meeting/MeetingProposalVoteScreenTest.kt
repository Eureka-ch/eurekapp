/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.utils.Formatters
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Refactored test suite for [MeetingProposalVoteScreen].
 *
 * This suite addresses flakiness and timeouts by:
 * 1. Using `waitForIdle()` to sync the UI after actions.
 * 2. Using `waitUntil()` to wait for async state changes from the ViewModel.
 * 3. Using `useUnmergedTree = true` on text matchers for reliability.
 *
 * Note : some tests were generated with the help of Gemini
 */
@RunWith(AndroidJUnit4::class)
class MeetingProposalVoteScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  private lateinit var viewModel: MeetingProposalVoteViewModel
  private lateinit var repositoryMock: MeetingProposalVoteRepositoryMock
  private val onDoneCalled = mutableStateOf(false)

  private val PROJECT_ID = "test-project"
  private val MEETING_ID = "test-meeting"
  private val USER_ID = "test-user-id"
  private val OTHER_USER_ID = "other-user-id"

  private val USER_VOTE_NO_FORMATS =
      MeetingProposalVote(
          userId = USER_ID, formatPreferences = emptyList()) // User vote with no format
  private val OTHER_USER_VOTE = MeetingProposalVote(userId = OTHER_USER_ID)

  private val PROPOSAL_VOTED =
      MeetingProposal(
          dateTime = Timestamp(1730000000, 0),
          votes = listOf(USER_VOTE_NO_FORMATS, OTHER_USER_VOTE))
  private val PROPOSAL_NOT_VOTED =
      MeetingProposal(dateTime = Timestamp(1730008000, 0), votes = listOf(OTHER_USER_VOTE))

  private val MOCK_MEETING =
      Meeting(
          meetingID = MEETING_ID,
          title = "Test Meeting",
          meetingProposals = listOf(PROPOSAL_VOTED, PROPOSAL_NOT_VOTED))

  private val DATE_1_STR = Formatters.formatDateTime(PROPOSAL_VOTED.dateTime.toDate())
  private val DATE_2_STR = Formatters.formatDateTime(PROPOSAL_NOT_VOTED.dateTime.toDate())

  private val VOTE_USER_IN_PERSON_ONLY =
      MeetingProposalVote(userId = USER_ID, formatPreferences = listOf(MeetingFormat.IN_PERSON))
  private val VOTE_USER_BOTH_FORMATS =
      MeetingProposalVote(
          userId = USER_ID,
          formatPreferences = listOf(MeetingFormat.IN_PERSON, MeetingFormat.VIRTUAL))

  private val PROPOSAL_VOTED_IN_PERSON =
      MeetingProposal(
          dateTime = Timestamp(1730016000, 0),
          votes = listOf(VOTE_USER_IN_PERSON_ONLY, OTHER_USER_VOTE))
  private val PROPOSAL_VOTED_BOTH =
      MeetingProposal(
          dateTime = Timestamp(1730024000, 0),
          votes = listOf(VOTE_USER_BOTH_FORMATS, OTHER_USER_VOTE))

  private val MEETING_FOR_FORMAT_TESTS =
      Meeting(
          meetingID = MEETING_ID,
          title = "Format Test Meeting",
          meetingProposals = listOf(PROPOSAL_VOTED_IN_PERSON, PROPOSAL_VOTED_BOTH))

  @Before
  fun setUp() {
    onDoneCalled.value = false
    repositoryMock = MeetingProposalVoteRepositoryMock()
  }

  /** Helper function to set the Composable content for a test. */
  private fun setContent(currentUserId: String? = USER_ID) {
    viewModel =
        MeetingProposalVoteViewModel(
            projectId = PROJECT_ID,
            meetingId = MEETING_ID,
            repository = repositoryMock,
            getCurrentUserId = { currentUserId })

    composeTestRule.setContent {
      MeetingProposalVoteScreen(
          projectId = PROJECT_ID,
          meetingId = MEETING_ID,
          onDone = { onDoneCalled.value = true },
          meetingProposalVoteViewModel = viewModel,
          onCreateDateTimeFormatProposalForMeeting = {})
    }
  }

  @Test
  fun test_screenLoadsAndDisplaysVotes_whenDataIsAvailable() {

    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_SCREEN)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_SCREEN_TITLE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_SCREEN_DESCRIPTION)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.ADD_MEETING_PROPOSALS)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.CONFIRM_MEETING_PROPOSALS_VOTES)
        .assertIsDisplayed()

    composeTestRule.onNodeWithText(DATE_1_STR).assertIsDisplayed()
    composeTestRule.onNodeWithText("2", useUnmergedTree = true).assertIsDisplayed()

    composeTestRule.onNodeWithText(DATE_2_STR).assertIsDisplayed()
    composeTestRule.onNodeWithText("1", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun test_confirmVotes_succeeds_andCallsOnDone() {
    // Arrange
    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    repositoryMock.updateShouldSucceed = true
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.CONFIRM_MEETING_PROPOSALS_VOTES)
        .performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) { onDoneCalled.value }

    assertTrue("onDone should be called", onDoneCalled.value)
    assertNotNull(
        "Repository should have received the updated meeting", repositoryMock.lastMeetingUpdated)
    assertEquals(
        MOCK_MEETING.copy(meetingProposals = viewModel.uiState.value.meetingProposals),
        repositoryMock.lastMeetingUpdated)
  }

  @Test
  fun test_confirmVotes_fails_andSetsError() {
    val error = Exception("Update Failed")
    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    repositoryMock.updateShouldSucceed = false
    repositoryMock.updateFailureException = error
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.CONFIRM_MEETING_PROPOSALS_VOTES)
        .performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) { viewModel.uiState.value.errorMsg != null }

    assertFalse("onDone should not be called", onDoneCalled.value)
    assertEquals("Meeting could not be updated.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun test_loadingMeetingFails_clearsErrorAndShowsNoVotes() {
    val error = Exception("Load Failed")
    repositoryMock.setMeetingLoadToFail(error)

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_CARD)
        .assertCountEquals(0)

    composeTestRule.runOnIdle {
      assertNull(
          "Error message should be null after being consumed by the UI",
          viewModel.uiState.value.errorMsg)
      assertTrue(
          "MeetingProposals list should be empty",
          viewModel.uiState.value.meetingProposals.isEmpty())
    }
  }

  @Test
  fun test_peopleButton_whenVoted_retractsEntireVote() {
    // Arrange
    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    assertTrue(viewModel.uiState.value.meetingProposals[0].votes.any { it.userId == USER_ID })

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_BUTTON)[0]
        .performClick()
    composeTestRule.waitForIdle()

    assertFalse(viewModel.uiState.value.meetingProposals[0].votes.any { it.userId == USER_ID })

    composeTestRule.onAllNodesWithText("1", useUnmergedTree = true).assertCountEquals(2)
  }

  @Test
  fun test_peopleButton_whenNotVoted_opensFormatDialog() {

    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_VALIDATE)
        .assertDoesNotExist()

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_BUTTON)[1]
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Select format(s)").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_VALIDATE)
        .assertIsDisplayed()
  }

  @Test
  fun test_dialog_okButton_isInitiallyDisabled_andEnablesOnSelection() {

    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_BUTTON)[1]
        .performClick()
    composeTestRule.waitForIdle()

    val okButton =
        composeTestRule.onNodeWithTag(
            MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_VALIDATE)
    okButton.assertIsDisplayed().assertIsNotEnabled()

    composeTestRule.onNodeWithTag(MeetingProposalVoteScreenTestTags.IN_PERSON_OPTION).performClick()
    composeTestRule.waitForIdle()

    okButton.assertIsEnabled()

    composeTestRule.onNodeWithTag(MeetingProposalVoteScreenTestTags.IN_PERSON_OPTION).performClick()
    composeTestRule.waitForIdle()

    okButton.assertIsNotEnabled()
  }

  @Test
  fun test_dialog_cancelButton_dismissesDialog() {
    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_BUTTON)[1]
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Select format(s)").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_CANCEL)
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Select format(s)").assertDoesNotExist()
  }

  @Test
  fun test_dialog_okButton_addsVoteAndDismissesDialog() {
    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    assertFalse(viewModel.uiState.value.meetingProposals[1].votes.any { it.userId == USER_ID })

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_BUTTON)[1]
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingProposalVoteScreenTestTags.IN_PERSON_OPTION).performClick()
    composeTestRule.onNodeWithTag(MeetingProposalVoteScreenTestTags.VIRTUAL_OPTION).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_VALIDATE)
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Select format(s)").assertDoesNotExist()

    val newVote = viewModel.uiState.value.meetingProposals[1].votes.find { it.userId == USER_ID }
    assertNotNull(newVote)
    assertTrue(newVote!!.formatPreferences.contains(MeetingFormat.IN_PERSON))
    assertTrue(newVote.formatPreferences.contains(MeetingFormat.VIRTUAL))
  }

  @Test
  fun test_formatButtons_areDisplayed_whenVoted_andNot_whenNotVoted() {
    repositoryMock.setMeetingToReturn(MOCK_MEETING)
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.IN_PERSON_BUTTON)
        .assertCountEquals(1)
    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.VIRTUAL_BUTTON)
        .assertCountEquals(1)
  }

  @Test
  fun test_formatButton_clickToAddFormatVote() {
    repositoryMock.setMeetingToReturn(MEETING_FOR_FORMAT_TESTS)
    setContent()
    composeTestRule.waitForIdle()

    var vote = viewModel.uiState.value.meetingProposals[0].votes.find { it.userId == USER_ID }
    assertNotNull(vote)
    assertTrue(vote!!.formatPreferences.contains(MeetingFormat.IN_PERSON))
    assertFalse(vote.formatPreferences.contains(MeetingFormat.VIRTUAL))

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.VIRTUAL_BUTTON)[0]
        .performClick()
    composeTestRule.waitForIdle()

    vote = viewModel.uiState.value.meetingProposals[0].votes.find { it.userId == USER_ID }
    assertNotNull(vote)
    assertTrue(vote!!.formatPreferences.contains(MeetingFormat.IN_PERSON))
    assertTrue(vote.formatPreferences.contains(MeetingFormat.VIRTUAL))
  }

  @Test
  fun test_formatButton_clickToRetractFormatVote() {
    repositoryMock.setMeetingToReturn(MEETING_FOR_FORMAT_TESTS)
    setContent()
    composeTestRule.waitForIdle()

    var vote = viewModel.uiState.value.meetingProposals[1].votes.find { it.userId == USER_ID }
    assertNotNull(vote)
    assertTrue(vote!!.formatPreferences.contains(MeetingFormat.IN_PERSON))
    assertTrue(vote.formatPreferences.contains(MeetingFormat.VIRTUAL))

    composeTestRule
        .onAllNodesWithTag(MeetingProposalVoteScreenTestTags.IN_PERSON_BUTTON)[1]
        .performClick()
    composeTestRule.waitForIdle()

    vote = viewModel.uiState.value.meetingProposals[1].votes.find { it.userId == USER_ID }
    assertNotNull(vote)
    assertFalse(vote!!.formatPreferences.contains(MeetingFormat.IN_PERSON))
    assertTrue(vote.formatPreferences.contains(MeetingFormat.VIRTUAL))
  }
}
