package ch.eureka.eurekapp.ui.meeting

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.map.Location
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Portions of this code were generated with the help of Grok.

@RunWith(AndroidJUnit4::class)
class MeetingDetailScreenTestOffline {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private lateinit var viewModel: MeetingDetailViewModel
  private val testProjectId = "testProject123"
  private val testMeetingId = "testMeeting123"

  @Before
  fun setUp() {
    mockConnectivityObserver =
        MockConnectivityObserver(InstrumentationRegistry.getInstrumentation().targetContext)
    viewModel =
        MeetingDetailViewModel(
            testProjectId, testMeetingId, connectivityObserver = mockConnectivityObserver)
  }

  @After
  fun tearDown() {
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  @Test
  fun meetingDetailScreenOfflineDisplaysMessage() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Offline Detail Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Scroll to the bottom to ensure the offline message is rendered
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CONTENT_COLUMN).performTouchInput {
      repeat(3) { swipeUp(startY = bottom, endY = top) }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreenOfflineStillViewsMeetingDetails() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Offline View Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 45,
            format = MeetingFormat.IN_PERSON,
            location = Location(name = "Test Location"),
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_FORMAT).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreenOfflineDisablesEditButton() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Test Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify edit button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun meetingDetailScreenOfflineDisablesDeleteButton() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Test Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify delete button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun meetingDetailScreenOfflineDisablesJoinMeetingButton() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Virtual Meeting",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify join meeting button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.JOIN_MEETING_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun meetingDetailScreenOfflineDisablesRecordButton() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Recording Meeting",
            status = MeetingStatus.IN_PROGRESS,
            duration = 60,
            format = MeetingFormat.IN_PERSON,
            location = Location(name = "Test Location"),
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify record button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.RECORD_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun meetingDetailScreenOfflineDisablesViewTranscriptButton() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Completed Meeting",
            status = MeetingStatus.COMPLETED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1",
            transcriptId = "testTranscriptId")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify view transcript button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun meetingDetailScreenOnlineThenOfflineShowsMessage() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Online Then Offline",
            status = MeetingStatus.SCHEDULED,
            duration = 60,
            format = MeetingFormat.VIRTUAL,
            link = "https://meet.example.com",
            datetime = Timestamp.now(),
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(true)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify no offline message initially
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.OFFLINE_MESSAGE).assertDoesNotExist()

    // Simulate going offline
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitForIdle()

    // Scroll to the bottom to ensure the offline message is visible
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CONTENT_COLUMN).performTouchInput {
      repeat(3) { swipeUp(startY = bottom, endY = top) }
    }

    composeTestRule.waitForIdle()

    // Verify offline message appears
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreenOfflineDisablesVoteButton() {
    val meeting =
        Meeting(
            meetingID = testMeetingId,
            projectId = testProjectId,
            title = "Open Vote Meeting",
            status = MeetingStatus.OPEN_TO_VOTES,
            duration = 60,
            createdBy = "user1")

    viewModel.setTestMeeting(meeting)
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

    // Verify vote button is displayed but disabled when offline
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  class MockConnectivityObserver(context: Context) : ConnectivityObserver(context) {
    private val _isConnected = MutableStateFlow(true)
    override val isConnected: Flow<Boolean> = _isConnected

    fun setConnected(connected: Boolean) {
      _isConnected.value = connected
    }
  }
}
