// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.ui.meeting

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests for MeetingDetailScreen behavior when the device is offline. */
@RunWith(AndroidJUnit4::class)
class MeetingDetailScreenOfflineTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private lateinit var viewModel: MeetingDetailViewModel
  private lateinit var attachmentsViewModel: MeetingAttachmentsViewModel
  private val testProjectId = "testProject123"
  private val testMeetingId = "testMeeting123"

  private val meetingFlow = MutableStateFlow<Meeting?>(null)
  private val participantsFlow = MutableStateFlow<List<Participant>>(emptyList())

  private class FileStorageRepositoryMock : FileStorageRepository {
    override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
      return Result.failure(Exception(""))
    }

    override suspend fun uploadFile(
        storagePath: String,
        fileDescriptor: ParcelFileDescriptor
    ): Result<String> {
      return Result.failure(Exception(""))
    }

    override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
      return Result.failure(Exception(""))
    }

    override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
      return Result.failure(Exception(""))
    }
  }

  private val repositoryMock =
      object : MeetingRepositoryMock() {
        override fun getMeetingById(
            projectId: String,
            meetingId: String
        ): kotlinx.coroutines.flow.Flow<Meeting?> {
          return meetingFlow
        }

        override fun getParticipants(
            projectId: String,
            meetingId: String
        ): kotlinx.coroutines.flow.Flow<List<Participant>> {
          return participantsFlow
        }
      }

  @Before
  fun setUp() {
    mockConnectivityObserver =
        MockConnectivityObserver(InstrumentationRegistry.getInstrumentation().targetContext)
    viewModel =
        MeetingDetailViewModel(
            testProjectId, testMeetingId, repositoryMock, mockConnectivityObserver)
    attachmentsViewModel =
        MeetingAttachmentsViewModel(
            fileStorageRepository = FileStorageRepositoryMock(),
            meetingsRepository = repositoryMock,
            connectivityObserver = mockConnectivityObserver)
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
          actionsConfig = MeetingDetailActionsConfig())
    }

    composeTestRule.waitForIdle()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR)
          .fetchSemanticsNodes()
          .isEmpty()
    }

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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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
        .performScrollTo()
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(true)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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

    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel,
          attachmentsViewModel = attachmentsViewModel,
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
}
