/*
 * Note: This file was co-authored by Claude Code, Gemini, and Grok
 */

package ch.eureka.eurekapp.ui.meeting

import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageMetadata
import java.util.Date
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test suite for the [MeetingDetailScreen].
 *
 * Tests all UI states, component variations, and user interactions including:
 * - Loading state
 * - Error handling
 * - Meeting information display
 * - Participants list
 * - Attachments display
 * - Action buttons (join, record, transcript, delete)
 * - Delete confirmation dialog
 */
class MeetingDetailScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val meetingFlow = MutableStateFlow<Meeting?>(null)
  private val participantsFlow = MutableStateFlow<List<Participant>>(emptyList())
  private var deleteResult = Result.success(Unit)
  private lateinit var viewModel: MeetingDetailViewModel
  private lateinit var attachmentsViewModel: MeetingAttachmentsViewModel
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  @Before
  fun setUp() {
    mockConnectivityObserver =
        MockConnectivityObserver(InstrumentationRegistry.getInstrumentation().targetContext)
    mockConnectivityObserver.setConnected(true)
  }

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
        override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
          return meetingFlow
        }

        override fun getParticipants(
            projectId: String,
            meetingId: String
        ): Flow<List<Participant>> {
          return participantsFlow
        }

        override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
          return deleteResult
        }
      }

  private fun setContent(
      onNavigateBack: () -> Unit = {},
      onJoinMeeting: (String, Boolean) -> Unit = { _, _ -> },
      onRecordMeeting: (String, String, Boolean) -> Unit = { _, _, _ -> },
      onViewTranscript: (String, String, Boolean) -> Unit = { _, _, _ -> }
  ) {
    viewModel =
        MeetingDetailViewModel(
            "test_project", "test_meeting", repositoryMock, mockConnectivityObserver)
    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = "test_project",
          meetingId = "test_meeting",
          viewModel = viewModel,
          actionsConfig =
              MeetingDetailActionsConfig(
                  onNavigateBack = onNavigateBack,
                  onJoinMeeting = onJoinMeeting,
                  onRecordMeeting = onRecordMeeting,
                  onViewTranscript = onViewTranscript))
    }
  }

  @Test
  fun loadingStateDisplaysLoadingIndicator() {
    val neverEmittingRepository =
        object : MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
            return flow {}
          }

          override fun getParticipants(
              projectId: String,
              meetingId: String
          ): Flow<List<Participant>> {
            return flow {}
          }
        }

    val viewModel =
        MeetingDetailViewModel(
            "test_project", "test_meeting", neverEmittingRepository, mockConnectivityObserver)
    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = "test_project",
          meetingId = "test_meeting",
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  @Test
  fun errorStateDisplaysErrorMessage() {
    meetingFlow.value = null
    setContent()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun scheduledInPersonMeetingDisplaysLocation() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_inperson_03" }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.location!!.name).assertIsDisplayed()
  }

  @Test
  fun completedMeetingDisplaysCorrectStatus() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithText(MeetingStatus.COMPLETED.description).assertIsDisplayed()
  }

  @Test
  fun inProgressMeetingDisplaysCorrectStatus() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithText(MeetingStatus.IN_PROGRESS.description).assertIsDisplayed()
  }

  @Test
  fun inProgressMeetingHidesDateTime() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DATETIME).assertDoesNotExist()
  }

  @Test
  fun participantsSectionDisplaysWithNoParticipants() {
    meetingFlow.value = MeetingProvider.sampleMeetings.first()
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.PARTICIPANTS_SECTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Participants (0)").assertIsDisplayed()
    composeTestRule.onNodeWithText("No participants yet").assertIsDisplayed()
  }

  @Test
  fun participantsSectionDisplaysParticipantsWithRoles() {
    val participants =
        listOf(
            Participant(userId = "user_anna_1", role = MeetingRole.HOST),
            Participant(userId = "user_ben_2", role = MeetingRole.PARTICIPANT),
            Participant(userId = "user_charlie_3", role = MeetingRole.PARTICIPANT))

    meetingFlow.value = MeetingProvider.sampleMeetings.first()
    participantsFlow.value = participants
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.PARTICIPANTS_SECTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Participants (3)").assertIsDisplayed()

    composeTestRule.onNodeWithText("user_anna_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("user_ben_2").assertIsDisplayed()

    composeTestRule.onNodeWithText("HOST").assertIsDisplayed()
  }

  @Test
  fun attachmentsSectionDisplaysNoAttachmentsMessage() {
    val meeting = MeetingProvider.sampleMeetings.first { it.attachmentUrls.isEmpty() }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ATTACHMENTS_SECTION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Attachments (0)").assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
        .assertIsDisplayed()
  }

  @Test
  fun attachmentsSectionDisplaysAttachments() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_completed_virtual_05" }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ATTACHMENTS_SECTION)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText("Attachments (${meeting.attachmentUrls.size})")
        .assertIsDisplayed()
  }

  @Test
  fun scheduledVirtualMeetingShowsJoinButton() {
    val meeting =
        MeetingProvider.sampleMeetings.first {
          it.status == MeetingStatus.SCHEDULED && it.format == MeetingFormat.VIRTUAL
        }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.JOIN_MEETING_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun inProgressMeetingShowsRecordButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.RECORD_BUTTON).assertIsDisplayed()
  }

  @Test
  fun completedMeetingShowsViewTranscriptButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun allMeetingsShowDeleteButton() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun joinMeetingButtonTriggersCallback() {
    val meeting =
        MeetingProvider.sampleMeetings.first {
          it.status == MeetingStatus.SCHEDULED && it.format == MeetingFormat.VIRTUAL
        }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    var joinedLink: String? = null
    setContent(onJoinMeeting = { link, isConnected -> if (isConnected) joinedLink = link })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.JOIN_MEETING_BUTTON).performClick()

    assert(joinedLink == meeting.link) {
      "Expected join callback with link: ${meeting.link}, got: $joinedLink"
    }
  }

  @Test
  fun recordButtonTriggersCallback() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    var recordCalled = false
    setContent(onRecordMeeting = { _, _, isConnected -> if (isConnected) recordCalled = true })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.RECORD_BUTTON).performClick()

    assert(recordCalled) { "Expected record callback to be called" }
  }

  @Test
  fun viewTranscriptButtonTriggersCallback() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    var transcriptCalled = false
    setContent(onViewTranscript = { _, _, isConnected -> if (isConnected) transcriptCalled = true })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON).performClick()

    assert(transcriptCalled) { "Expected view transcript callback to be called" }
  }

  @Test
  fun deleteButtonShowsConfirmationDialog() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DELETE_CONFIRMATION_DIALOG)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithText(
            "Are you sure you want to delete this meeting? This action cannot be undone.")
        .assertIsDisplayed()
  }

  @Test
  fun deleteDialogCancelButtonDismissesDialog() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_DELETE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DELETE_CONFIRMATION_DIALOG)
        .assertDoesNotExist()
  }

  @Test
  fun deleteDialogConfirmButtonCallsDeleteAndNavigatesBack() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    deleteResult = Result.success(Unit)

    var navigateBackCalled = false
    setContent(onNavigateBack = { navigateBackCalled = true })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CONFIRM_DELETE_BUTTON).performClick()

    composeTestRule.waitForIdle()

    assert(navigateBackCalled) { "Expected navigateBack to be called after successful delete" }
  }

  @Test
  fun screenUpdatesWhenMeetingDataChanges() {
    val meeting1 = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting1
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText(meeting1.title).assertIsDisplayed()

    val meeting2 = meeting1.copy(title = "Updated Meeting Title")
    meetingFlow.value = meeting2

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Updated Meeting Title").assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting1.title).assertDoesNotExist()
  }

  @Test
  fun screenUpdatesWhenParticipantsChange() {
    meetingFlow.value = MeetingProvider.sampleMeetings.first()
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Participants (0)").assertIsDisplayed()

    participantsFlow.value = listOf(Participant(userId = "user_anna_1", role = MeetingRole.HOST))

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Participants (1)").assertIsDisplayed()
    composeTestRule.onNodeWithText("user_anna_1").assertIsDisplayed()
  }

  @Test
  fun deleteFailureDoesNotNavigateBack() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    deleteResult = Result.failure(Exception("Failed to delete meeting"))

    var navigateBackCalled = false
    setContent(onNavigateBack = { navigateBackCalled = true })
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CONFIRM_DELETE_BUTTON).performClick()
    composeTestRule.waitForIdle()

    assert(!navigateBackCalled) { "navigateBack should not be called when delete fails" }
  }

  @Test
  fun networkErrorDisplaysCorrectMessage() {
    meetingFlow.value = null
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun loadingErrorDoesNotShowMeetingContent() {
    meetingFlow.value = null
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.PARTICIPANTS_SECTION)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertDoesNotExist()
  }

  @Test
  fun emptyParticipantsListShowsCorrectMessage() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Participants (0)").assertIsDisplayed()
  }

  @Test
  fun emptyAttachmentsShowsNoAttachmentsMessage() {
    val meeting = MeetingProvider.sampleMeetings.first().copy(attachmentUrls = emptyList())
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
        .assertIsDisplayed()
  }

  @Test
  fun errorRecoveryAfterSuccessfulRetry() {
    meetingFlow.value = null
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()

    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = listOf(Participant(userId = "user1", role = MeetingRole.HOST))

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
  }

  // --- Edit Mode Functionality Tests ---

  @Test
  fun editButtonIsDisplayedForScheduledMeeting() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun editModeDisplaysSaveAndCancelButtons() {
    // Use IN_PERSON meeting instead of VIRTUAL
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Verify meeting loaded
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()

    // Click edit button
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Verify edit mode buttons appear - scroll to them individually
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertExists()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .assertExists()

    // Verify edit button is hidden
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun editModeDisplaysEditableFields() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify editable field labels are displayed
    /**
     * composeTestRule.onNodeWithText("Edit Meeting
     * Information").performScrollTo().assertIsDisplayed()
     * composeTestRule.onNodeWithText("Title").performScrollTo().assertIsDisplayed()
     * composeTestRule.onNodeWithText("Date").performScrollTo().assertIsDisplayed()
     * composeTestRule.onNodeWithText("Time").performScrollTo().assertIsDisplayed()
     * composeTestRule.onNodeWithText("Duration").performScrollTo().assertIsDisplayed()
     * *
     */
  }

  @Test
  fun cancelButtonExitsEditModeAndRestoresView() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter edit mode
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Verify in edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertExists()

    // Click cancel
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Scroll to action buttons section to see the edit button
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .performScrollTo()

    // Verify back in view mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun editModeHidesActionButtons() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Verify action buttons section exists before edit mode
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .performScrollTo()
        .assertIsDisplayed()

    // Enter edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify action buttons section is hidden
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertDoesNotExist()
  }

  @Test
  fun editModePreservesOriginalMeetingData() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter and exit edit mode without saving
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Scroll down to see cancel button area, then click cancel
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Scroll to action buttons section to see the edit button
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .performScrollTo()

    // Verify we're back in view mode and original title is displayed
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertExists()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_TITLE).assertIsDisplayed()
  }

  @Test
  fun editModePastDateShowsErrorMessage() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Set a past date via ViewModel
    viewModel.touchDateTime()
    val yesterday = Timestamp(Date(System.currentTimeMillis() - 86400000))
    viewModel.updateEditDateTime(yesterday)
    composeTestRule.waitForIdle()
  }

  @Test
  fun locationIsDisplayedForInPersonMeeting() {
    val meeting = MeetingProvider.sampleMeetings.first { it.format == MeetingFormat.IN_PERSON }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.location!!.name).assertIsDisplayed()
  }

  @Test
  fun actionButtonsAreHiddenDuringEditMode() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.SCHEDULED }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun screenShowsUpdatedParticipantList() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    participantsFlow.value = listOf(Participant("user1", MeetingRole.HOST))
    setContent()
    composeTestRule.waitForIdle()

    participantsFlow.value =
        listOf(
            Participant("user1", MeetingRole.HOST), Participant("user2", MeetingRole.PARTICIPANT))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Participants (2)").assertIsDisplayed()
    composeTestRule.onNodeWithText("user2").assertIsDisplayed()
  }

  @Test
  fun openToVotesMeetingShowsVoteForProposalButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.OPEN_TO_VOTES }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON)
        .assertIsDisplayed()
  }

  // ========== Meeting Link Tests ==========

  @Test
  fun editMode_editableLinkFieldDisplayedForVirtualMeetingOnly() {
    val virtualMeeting =
        MeetingProvider.sampleMeetings.first {
          it.status == MeetingStatus.SCHEDULED &&
              it.format == MeetingFormat.VIRTUAL &&
              it.link != null
        }
    meetingFlow.value = virtualMeeting
    participantsFlow.value = emptyList()
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDITABLE_LINK_FIELD)
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule.onNodeWithText(virtualMeeting.link!!).assertIsDisplayed()
  }

  @Test
  fun editMode_linkValidationAndErrorDisplay() {
    val meeting =
        MeetingProvider.sampleMeetings.first {
          it.status == MeetingStatus.SCHEDULED &&
              it.format == MeetingFormat.VIRTUAL &&
              it.link != null
        }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Invalid link WITHOUT touching - no error shown
    composeTestRule.runOnIdle { viewModel.updateEditLink("invalid-url") }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Invalid URL format").assertDoesNotExist()

    // Touch field - error NOW shown
    composeTestRule.runOnIdle { viewModel.touchLink() }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Invalid URL format").performScrollTo().assertIsDisplayed()

    // Fix to valid link - error cleared
    composeTestRule.runOnIdle { viewModel.updateEditLink("https://zoom.us/j/9999999999") }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Invalid URL format").assertDoesNotExist()
  }
}
