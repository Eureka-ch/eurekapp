/*
 * Note: This file was co-authored by Claude Code.
 */

package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
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
      onJoinMeeting: (String) -> Unit = {},
      onRecordMeeting: () -> Unit = {},
      onViewTranscript: () -> Unit = {}
  ) {
    val viewModel = MeetingDetailViewModel("test_project", "test_meeting", repositoryMock)
    composeTestRule.setContent {
      MeetingDetailScreen(
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

    val viewModel = MeetingDetailViewModel("test_project", "test_meeting", neverEmittingRepository)
    composeTestRule.setContent {
      MeetingDetailScreen(
          projectId = "test_project", meetingId = "test_meeting", viewModel = viewModel)
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
  fun scheduledVirtualMeetingDisplaysAllInformation() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_virtual_02" }
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DATETIME).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_FORMAT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LINK).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.link!!).assertIsDisplayed()
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

    meeting.attachmentUrls.forEach { url ->
      composeTestRule.onNodeWithText(url).assertIsDisplayed()
    }
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
    setContent(onJoinMeeting = { link -> joinedLink = link })

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
    setContent(onRecordMeeting = { recordCalled = true })

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
    setContent(onViewTranscript = { transcriptCalled = true })

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
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
  }

  @Test
  fun editModeDisplaysSaveAndCancelButtons() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Click edit button
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify edit mode buttons appear
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()

    // Verify edit button is hidden
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun editModeDisplaysEditableFields() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify editable field labels are displayed
    composeTestRule.onNodeWithText("Edit Meeting Information").assertIsDisplayed()
    composeTestRule.onNodeWithText("Title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Date").assertIsDisplayed()
    composeTestRule.onNodeWithText("Time").assertIsDisplayed()
    composeTestRule.onNodeWithText("Duration").assertIsDisplayed()
  }

  @Test
  fun cancelButtonExitsEditModeAndRestoresView() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify in edit mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertExists()

    // Click cancel (scroll to it first as it's at the end of the screen)
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Verify back in view mode
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun editModeHidesActionButtons() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Verify action buttons section exists before edit mode
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
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
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    participantsFlow.value = emptyList()

    setContent()
    composeTestRule.waitForIdle()

    // Enter and exit edit mode without saving
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    // Verify original meeting title is still displayed
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
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

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertDoesNotExist()
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
}
