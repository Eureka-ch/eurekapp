package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.meeting.Meeting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Test suite for MeetingScreen
 *
 * Note : some tests where generated with Gemini
 */
class MeetingsScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  // Use a more flexible mock that allows changing the emitted data
  private val meetingsFlow = MutableStateFlow<List<Meeting>>(emptyList())
  private val repositoryMock =
      object : MeetingRepositoryMock() {
        override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
          return meetingsFlow
        }
      }

  private fun setContent() {
    val viewModel = MeetingViewModel(repositoryMock)
    composeTestRule.setContent { MeetingScreen("test_project", viewModel) }
  }

  @Test
  fun screenLoadsAndDisplaysTitleAndDescription() {
    setContent()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN_TITLE).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN_DESCRIPTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_TABS).assertIsDisplayed()
  }

  @Test
  fun upcomingMeetingsAreDisplayedByDefault() {
    val allMeetings = MeetingProvider.sampleMeetings
    meetingsFlow.value = allMeetings
    setContent()

    val expectedUpcoming = allMeetings.filter { !it.ended }

    val firstUpcomingMeetingTitle = expectedUpcoming.first().title
    composeTestRule.onNodeWithText(firstUpcomingMeetingTitle).assertIsDisplayed()

    val firstPastMeetingTitle = allMeetings.first { it.ended }.title
    composeTestRule.onNodeWithText(firstPastMeetingTitle).assertDoesNotExist()
  }

  @Test
  fun clickingPastTabDisplaysPastMeetings() {
    meetingsFlow.value = MeetingProvider.sampleMeetings
    setContent()

    // Click on the 'PAST' tab
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_TAB_PAST).performClick()

    // Verify the past meetings are shown
    val pastCount = MeetingProvider.sampleMeetings.count { it.ended }
    composeTestRule
        .onAllNodesWithTag(MeetingScreenTestTags.MEETING_CARD)
        .assertCountEquals(pastCount)
  }

  @Test
  fun displaysNoMeetingsMessageForUpcomingTab() {
    meetingsFlow.value = emptyList()
    setContent()

    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.NO_UPCOMING_MEETINGS_MESSAGE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.NO_PAST_MEETINGS_MESSAGE)
        .assertDoesNotExist()
  }

  @Test
  fun displaysNoMeetingsMessageForPastTab() {
    meetingsFlow.value = emptyList()
    setContent()

    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_TAB_PAST).performClick()

    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.NO_PAST_MEETINGS_MESSAGE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.NO_UPCOMING_MEETINGS_MESSAGE)
        .assertDoesNotExist()
  }

  @Test
  fun meetingCardForVotingShowsCorrectElements() {
    // Use only the meeting that is in the voting phase
    val votingMeeting = MeetingProvider.sampleMeetings.first { it.canVote }
    meetingsFlow.value = listOf(votingMeeting)
    setContent()

    // Check for voting-specific UI elements
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_STATUS_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_TIMESLOT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.MEETING_VOTE_FOR_DATETIME_MESSAGE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.MEETING_VOTE_FOR_FORMAT_MESSAGE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.VOTE_FOR_DATETIME_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.VOTE_FOR_FORMAT_BUTTON).assertIsDisplayed()

    // Check that other elements are not displayed
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON).assertDoesNotExist()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.DIRECTIONS_BUTTON).assertDoesNotExist()
  }

  @Test
  fun meetingCardForScheduledVirtualMeetingShowsCorrectElements() {
    val virtualMeeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_virtual_02" }
    meetingsFlow.value = listOf(virtualMeeting)
    setContent()

    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_STATUS_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_DATETIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_LINK).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON).assertIsDisplayed()

    // Ensure voting buttons are not present
    composeTestRule
        .onNodeWithTag(MeetingScreenTestTags.VOTE_FOR_DATETIME_BUTTON)
        .assertDoesNotExist()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.VOTE_FOR_FORMAT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun meetingCardForScheduledInpersonMeetingShowsCorrectElements() {
    val inPersonMeeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_inperson_03" }
    meetingsFlow.value = listOf(inPersonMeeting)
    setContent()

    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_STATUS_TEXT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_DATETIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.DIRECTIONS_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.RECORD_BUTTON).assertIsDisplayed()

    // Ensure virtual and voting elements are not present
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.JOIN_MEETING_BUTTON).assertDoesNotExist()
  }
}
