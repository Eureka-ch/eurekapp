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
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
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

/** UI test suite for the [MeetingDetailScreen]. */
class MeetingDetailScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val meetingFlow = MutableStateFlow<Meeting?>(null)
  private val userFlow = MutableStateFlow<User?>(null)
  private val projectFlow = MutableStateFlow<Project?>(null)
  private var deleteResult = Result.success(Unit)
  private lateinit var viewModel: MeetingDetailViewModel
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

  private val projectRepositoryMock =
      object : MockProjectRepository() {
        override fun getProjectById(projectId: String): Flow<Project?> {
          return projectFlow
        }
      }

  private fun setContent(
      onNavigateBack: () -> Unit = {},
      onJoinMeeting: (String, Boolean) -> Unit = { _, _ -> },
      onRecordMeeting: (String, String, Boolean) -> Unit = { _, _, _ -> },
      onViewTranscript: (String, String, Boolean) -> Unit = { _, _, _ -> },
      currentUserId: String? = "testUser"
  ) {
    viewModel =
        MeetingDetailViewModel(
            projectId = "test_project",
            meetingId = "test_meeting",
            repository = repositoryMock,
            projectRepository = projectRepositoryMock,
            userRepository = userRepositoryMock,
            connectivityObserver = mockConnectivityObserver,
            getCurrentUserId = { currentUserId })
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

    val neverEmittingProjectRepository =
        object : MockProjectRepository() {
          override fun getProjectById(projectId: String): Flow<Project?> {
            return flow {}
          }
        }

    val viewModel =
        MeetingDetailViewModel(
            "test_project",
            "test_meeting",
            neverEmittingRepository,
            neverEmittingProjectRepository,
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
    projectFlow.value = Project(name = "Test Project")
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
    projectFlow.value = Project(name = "My Test Project")

    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_STATUS).assertIsDisplayed()

    // VERIFY PROJECT NAME (From My Version)
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.MEETING_PROJECT_NAME)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("My Test Project").assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DATETIME).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_FORMAT).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_LINK).assertIsDisplayed()
    composeTestRule.onNodeWithText(meeting.link!!).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_scheduledInPersonMeetingDisplaysLocation() {
    val meeting =
        MeetingProvider.sampleMeetings.first { it.meetingID == "meet_scheduled_inperson_03" }
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.MEETING_DATETIME).assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_creatorSectionDisplaysWhenUserFound() {
    val meeting = MeetingProvider.sampleMeetings.first()
    val creator =
        User(
            uid = meeting.createdBy,
            displayName = "John Doe",
            photoUrl = "https://example.com/photo.jpg")

    meetingFlow.value = meeting
    userFlow.value = creator
    projectFlow.value = Project(name = "My Test Project")
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_SECTION).assertIsDisplayed()
    composeTestRule.onNodeWithText("Creator").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("Meeting Creator").assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_creatorSectionDisplaysFallbackWhenUserNotFound() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null // User not found
    projectFlow.value = Project(name = "My Test Project")
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_SECTION).assertIsDisplayed()
    // Should fallback to ID
    composeTestRule.onNodeWithText(meeting.createdBy).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_AVATAR).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_creatorSectionDisplaysFallbackIconWhenNoPhotoUrl() {
    val meeting = MeetingProvider.sampleMeetings.first()
    val creator = User(uid = meeting.createdBy, displayName = "Jane Smith", photoUrl = "")
    meetingFlow.value = meeting
    userFlow.value = creator
    projectFlow.value = Project(name = "My Test Project")
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Jane Smith").assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_AVATAR).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_attachmentsSectionDisplaysNoAttachmentsMessage() {
    val meeting = MeetingProvider.sampleMeetings.first { it.attachmentUrls.isEmpty() }
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.RECORD_BUTTON).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_completedMeetingShowsViewTranscriptButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.COMPLETED }
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")

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
    projectFlow.value = Project(name = "My Test Project")

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
    projectFlow.value = Project(name = "My Test Project")

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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")
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
  fun meetingDetailScreen_deleteFailureDoesNotNavigateBack() {
    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")
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
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_loadingErrorDoesNotShowMeetingContent() {
    meetingFlow.value = null
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.CREATOR_SECTION).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_emptyAttachmentsShowsNoAttachmentsMessage() {
    val meeting = MeetingProvider.sampleMeetings.first().copy(attachmentUrls = emptyList())
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

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
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()

    val meeting = MeetingProvider.sampleMeetings.first()
    meetingFlow.value = meeting

    composeTestRule.waitForIdle()
  }

  @Test
  fun meetingDetailScreen_editButtonIsDisplayedForScheduledMeeting() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_editModeDisplaysSaveAndCancelButtons() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_editModeDisplaysEditableFields() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun meetingDetailScreen_cancelButtonExitsEditModeAndRestoresView() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertExists()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.SAVE_BUTTON).assertDoesNotExist()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_editModeHidesActionButtons() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .performScrollTo()
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_editModePreservesOriginalMeetingData() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting with datetime
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performScrollTo()
        .performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(meeting.title).assertIsDisplayed()
  }

  @Test
  fun meetingDetailScreen_editModePastDateShowsErrorMessage() {
    val meeting = MeetingProvider.sampleMeetings[1] // SCHEDULED meeting
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()

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
    projectFlow.value = Project(name = "My Test Project")

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
    projectFlow.value = Project(name = "My Test Project")

    setContent()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(MeetingDetailScreenTestTags.EDIT_BUTTON).performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun meetingDetailScreen_openToVotesMeetingShowsVoteForProposalButton() {
    val meeting = MeetingProvider.sampleMeetings.first { it.status == MeetingStatus.OPEN_TO_VOTES }
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")
    setContent()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ACTION_BUTTONS_SECTION)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.VOTE_FOR_MEETING_PROPOSAL_BUTTON)
        .assertIsDisplayed()
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
    projectFlow.value = Project(name = "My Test Project")

    setContent(currentUserId = creatorId)

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
    projectFlow.value = Project(name = "My Test Project")

    setContent(currentUserId = otherUserId)

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
    projectFlow.value = Project(name = "My Test Project")

    setContent(currentUserId = creatorId)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_BUTTON)
        .performScrollTo()
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
    projectFlow.value = Project(name = "My Test Project")

    setContent(currentUserId = otherUserId)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun meetingDetailScreen_scheduledMeetingPastStartTimeShowsStartReminder() {
    val creatorId = "user_creator"
    val pastDateTime = Timestamp(Date(System.currentTimeMillis() - 3600000))
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.SCHEDULED }
            .copy(createdBy = creatorId, datetime = pastDateTime)
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent(currentUserId = creatorId)

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
    val startDateTime = Timestamp(Date(System.currentTimeMillis() - 7200000))
    val meeting =
        MeetingProvider.sampleMeetings
            .first { it.status == MeetingStatus.IN_PROGRESS }
            .copy(createdBy = creatorId, datetime = startDateTime, duration = 60)
    meetingFlow.value = meeting
    userFlow.value = null
    projectFlow.value = Project(name = "My Test Project")

    setContent(currentUserId = creatorId)

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.END_MEETING_REMINDER)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithText("The scheduled end time has passed. Consider ending the meeting.")
        .assertIsDisplayed()
  }
}
