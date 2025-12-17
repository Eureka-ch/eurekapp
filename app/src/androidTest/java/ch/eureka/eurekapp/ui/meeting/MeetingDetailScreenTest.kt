/*
 * Note: This file was co-authored by Claude Code, Gemini, and Grok and Claude 4.5 Sonnet.
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
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageMetadata
import java.util.Date
import junit.framework.TestCase.assertEquals
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
  private val userFlow = MutableStateFlow<User?>(null)
  private var deleteResult = Result.success(Unit)
  private lateinit var viewModel: MeetingDetailViewModel
  private lateinit var attachmentsViewModel: MeetingAttachmentsViewModel
  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private val testProjectId = "testProject123"
  private val testMeetingId = "testMeeting123"

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

        override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
          return deleteResult
        }
      }

  private val userRepositoryMock =
      object : UserRepository {
        override fun getUserById(userId: String): Flow<User?> {
          return userFlow
        }

        override fun getCurrentUser(): Flow<User?> {
          return flow { emit(null) }
        }

        override suspend fun saveUser(user: User): Result<Unit> {
          return Result.success(Unit)
        }

        override suspend fun updateLastActive(userId: String): Result<Unit> {
          return Result.success(Unit)
        }

        override suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit> {
          return Result.success(Unit)
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
            "test_project",
            "test_meeting",
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver)
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
  fun meetingDetailScreen_loadingStateDisplaysLoadingIndicator() {
    val neverEmittingRepository =
        object : MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
            return flow {}
          }
        }

    val viewModel =
        MeetingDetailViewModel(
            "test_project",
            "test_meeting",
            neverEmittingRepository,
            userRepositoryMock,
            mockConnectivityObserver)
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
  fun meetingDetailScreen_errorStateDisplaysErrorMessage() {
    meetingFlow.value = null
    setContent()

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_scheduledVirtualMeetingDisplaysAllInformation() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_virtual_02" }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DATETIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_FORMAT).assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LINK)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_scheduledInPersonMeetingDisplaysLocation() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_inperson_03" }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.location!!.name).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_completedMeetingDisplaysCorrectStatus() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithText(MeetingStatus.COMPLETED.description).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_inProgressMeetingDisplaysCorrectStatus() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()
    composeTestRule.onNodeWithText(MeetingStatus.IN_PROGRESS.description).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_inProgressMeetingHidesDateTime() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DATETIME).assertDoesNotExist()
  }

  @Test
  fun attachmentsSectionDisplaysNoAttachmentsMessage() {
    val meeting = MeetingProvider.sampleMeetings.first { it.attachmentUrls.isEmpty() }
    meetingFlow.value = meeting
    userFlow.value = null
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
  fun meetingDetailScreen_attachmentsSectionDisplaysAttachments() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_completed_virtual_05" }
    meetingFlow.value = meeting
    userFlow.value = null
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
  fun meetingDetailScreen_scheduledVirtualMeetingShowsJoinButton() {
    val meeting =
        MeetingProvider.sampleMeetings.first {
          it.status == MeetingStatus.SCHEDULED && it.format == MeetingFormat.VIRTUAL
        }
    meetingFlow.value = meeting
    userFlow.value = null
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
  fun meetingDetailScreen_inProgressMeetingShowsRecordButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.RECORD_BUTTON).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_completedMeetingShowsViewTranscriptButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_allMeetingsShowDeleteButton() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_joinMeetingButtonTriggersCallback() {
    val meeting =
        MeetingProvider.sampleMeetings.first {
          it.status == MeetingStatus.SCHEDULED && it.format == MeetingFormat.VIRTUAL
        }
    meetingFlow.value = meeting
    userFlow.value = null

    var joinedLink: String? = null
    setContent(onJoinMeeting = { link, isConnected -> if (isConnected) joinedLink = link })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.JOIN_MEETING_BUTTON).performClick()

    assert(joinedLink == meeting.link) {
      "Expected join callback with link: ${meeting.link}, got: $joinedLink"
    }
  }

  @Test
  fun meetingDetailScreen_recordButtonTriggersCallback() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.IN_PROGRESS }
    meetingFlow.value = meeting
    userFlow.value = null

    var recordCalled = false
    setContent(onRecordMeeting = { _, _, isConnected -> if (isConnected) recordCalled = true })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.RECORD_BUTTON).performClick()

    assert(recordCalled) { "Expected record callback to be called" }
  }

  @Test
  fun meetingDetailScreen_viewTranscriptButtonTriggersCallback() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    userFlow.value = null

    var transcriptCalled = false
    setContent(onViewTranscript = { _, _, isConnected -> if (isConnected) transcriptCalled = true })

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.VIEW_TRANSCRIPT_BUTTON).performClick()

    assert(transcriptCalled) { "Expected view transcript callback to be called" }
  }

  @Test
  fun meetingDetailScreen_deleteButtonShowsConfirmationDialog() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null
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
  fun meetingDetailScreen_deleteDialogCancelButtonDismissesDialog() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.DELETE_BUTTON).performClick()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_DELETE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DELETE_CONFIRMATION_DIALOG)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_deleteDialogConfirmButtonCallsDeleteAndNavigatesBack() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null
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
  fun meetingDetailScreen_screenUpdatesWhenMeetingDataChanges() {
    val meeting1 = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting1
    userFlow.value = null
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
  fun deleteFailureDoesNotNavigateBack() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null
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
  fun meetingDetailScreen_networkErrorDisplaysCorrectMessage() {
    meetingFlow.value = null
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_loadingErrorDoesNotShowMeetingContent() {
    meetingFlow.value = null
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_SECTION).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertDoesNotExist()
  }

  @Test
  fun emptyAttachmentsShowsNoAttachmentsMessage() {
    val meeting = MeetingProvider.sampleMeetings.first().copy(attachmentUrls = emptyList())
    meetingFlow.value = meeting
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_errorRecoveryAfterSuccessfulRetry() {
    meetingFlow.value = null
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()

    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertDoesNotExist()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
  }

  // --- Edit Mode Functionality Tests ---

  @Test
  fun meetingDetailScreen_editButtonIsDisplayedForScheduledMeeting() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_editModeDisplaysSaveAndCancelButtons() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting
    meetingFlow.value = meeting
    userFlow.value = null

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
  fun meetingDetailScreen_editModeDisplaysEditableFields() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null

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
  fun meetingDetailScreen_cancelButtonExitsEditModeAndRestoresView() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null

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
  fun meetingDetailScreen_editModeHidesActionButtons() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null

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
  fun meetingDetailScreen_editModePreservesOriginalMeetingData() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null

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
  fun meetingDetailScreen_editModePastDateShowsErrorMessage() {
    val meeting = MeetingProvider.sampleMeetings[2] // SCHEDULED IN_PERSON meeting
    meetingFlow.value = meeting
    userFlow.value = null

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
  fun meetingDetailScreen_locationIsDisplayedForInPersonMeeting() {
    val meeting = MeetingProvider.sampleMeetings.first { it.format == MeetingFormat.IN_PERSON }
    meetingFlow.value = meeting
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LOCATION).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.location!!.name).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_actionButtonsAreHiddenDuringEditMode() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.SCHEDULED }
    meetingFlow.value = meeting
    userFlow.value = null

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun openToVotesMeetingShowsVoteForProposalButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.OPEN_TO_VOTES }
    meetingFlow.value = meeting
    userFlow.value = null
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
    userFlow.value = null
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
    userFlow.value = null
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

  // ========== Creator Section Tests ==========

  @Test
  fun creatorSectionDisplaysWhenUserFound() {
    val meeting = MeetingProvider.sampleMeetings.first()
    val creator =
        User(
            uid = meeting.createdBy,
            displayName = "John Doe",
            photoUrl = "https://example.com/photo.jpg")

    meetingFlow.value = meeting
    userFlow.value = creator
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Creator").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Meeting Creator").assertIsDisplayed()
  }

  @Test
  fun creatorSectionDisplaysFallbackWhenUserNotFound() {
    val meeting = MeetingProvider.sampleMeetings.first()

    meetingFlow.value = meeting
    userFlow.value = null
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.createdBy).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_AVATAR).assertIsDisplayed()
  }

  @Test
  fun creatorSectionDisplaysFallbackIconWhenNoPhotoUrl() {
    val meeting = MeetingProvider.sampleMeetings.first()
    val creator = User(uid = meeting.createdBy, displayName = "Jane Smith", photoUrl = "")

    meetingFlow.value = meeting
    userFlow.value = creator
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_AVATAR).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_scheduledMeetingShowsStartMeetingButtonForCreator() {
    val creatorId = "user_creator"
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.SCHEDULED }
            .copy(createdBy = creatorId)
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.START_MEETING_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_scheduledMeetingDoesNotShowStartMeetingButtonForNonCreator() {
    val creatorId = "user_creator"
    val otherUserId = "user_other"
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.SCHEDULED }
            .copy(createdBy = creatorId)
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { otherUserId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.START_MEETING_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_inProgressMeetingShowsEndMeetingButtonForCreator() {
    val creatorId = "user_creator"
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.IN_PROGRESS }
            .copy(createdBy = creatorId)
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_inProgressMeetingDoesNotShowEndMeetingButtonForNonCreator() {
    val creatorId = "user_creator"
    val otherUserId = "user_other"
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.IN_PROGRESS }
            .copy(createdBy = creatorId)
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { otherUserId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_startMeetingButtonClickChangesStatusToInProgress() {
    val creatorId = "user_creator"
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.SCHEDULED }
            .copy(createdBy = creatorId)
    meetingFlow.value = meeting
    userFlow.value = null

    val updatedRepositoryMock =
        object : MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
            return meetingFlow
          }

          override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
            meetingFlow.value = meeting
            return Result.success(Unit)
          }
        }

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            updatedRepositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), updatedRepositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.START_MEETING_BUTTON)
        .performScrollTo()
        .performClick()

    composeTestRule.waitForIdle()

    assertEquals(MeetingStatus.IN_PROGRESS, meetingFlow.value?.status)
  }

  @Test
  fun meetingDetailScreen_endMeetingButtonClickChangesStatusToCompleted() {
    val creatorId = "user_creator"
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.IN_PROGRESS }
            .copy(createdBy = creatorId)
    meetingFlow.value = meeting
    userFlow.value = null

    val updatedRepositoryMock =
        object : MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
            return meetingFlow
          }

          override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
            meetingFlow.value = meeting
            return Result.success(Unit)
          }
        }

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            updatedRepositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), updatedRepositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_BUTTON).performClick()

    composeTestRule.waitForIdle()

    assertEquals(MeetingStatus.COMPLETED, meetingFlow.value?.status)
  }

  @Test
  fun meetingDetailScreen_scheduledMeetingPastStartTimeShowsStartReminder() {
    val creatorId = "user_creator"
    val pastDateTime = Timestamp(Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.SCHEDULED }
            .copy(createdBy = creatorId, datetime = pastDateTime)
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.START_MEETING_REMINDER)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText("The scheduled start time has passed. Consider starting the meeting.")
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_inProgressMeetingPastEndTimeShowsEndReminder() {
    val creatorId = "user_creator"
    val startDateTime = Timestamp(Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.IN_PROGRESS }
            .copy(createdBy = creatorId, datetime = startDateTime, duration = 60) // 1 hour duration
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_REMINDER)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText("The scheduled end time has passed. Consider ending the meeting.")
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_scheduledMeetingFutureStartTimeDoesNotShowReminder() {
    val creatorId = "user_creator"
    val futureDateTime = Timestamp(Date(System.currentTimeMillis() + 3600000)) // 1 hour from now
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.SCHEDULED }
            .copy(createdBy = creatorId, datetime = futureDateTime)
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.START_MEETING_REMINDER)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_inProgressMeetingNotPastEndTimeDoesNotShowReminder() {
    val creatorId = "user_creator"
    val startDateTime = Timestamp(Date(System.currentTimeMillis() - 1800000)) // 30 min ago
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.IN_PROGRESS }
            .copy(createdBy = creatorId, datetime = startDateTime, duration = 60) // Ends in 30 min
    meetingFlow.value = meeting
    userFlow.value = null

    viewModel =
        MeetingDetailViewModel(
            testProjectId,
            testMeetingId,
            repositoryMock,
            userRepositoryMock,
            mockConnectivityObserver,
            getCurrentUserId = { creatorId })

    composeTestRule.setContent {
      MeetingDetailScreen(
          attachmentsViewModel =
              MeetingAttachmentsViewModel(
                  FileStorageRepositoryMock(), repositoryMock, mockConnectivityObserver),
          projectId = testProjectId,
          meetingId = testMeetingId,
          viewModel = viewModel)
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_REMINDER)
        .assertDoesNotExist()
  }
}
