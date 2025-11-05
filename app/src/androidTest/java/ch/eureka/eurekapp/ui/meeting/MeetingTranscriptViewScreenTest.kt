package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingTranscriptViewScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.TranscriptScreenTestTags
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import org.junit.Rule
import org.junit.Test

/**
 * Test suite for MeetingTranscriptViewScreen
 *
 * Note: This file was partially written by ChatGPT (GPT-5) Co-author: GPT-5
 */
class MeetingTranscriptViewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val meetingFlow = MutableStateFlow<Meeting?>(null)

  private val repositoryMock =
      object : MeetingRepositoryMock() {
        override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
          return meetingFlow
        }
      }

  @Test
  fun loadingState_displaysLoadingIndicator() {
    // Use a repository that never emits to keep the screen in loading state
    val neverEmittingRepository =
        object : MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
            return flow {} // Never emits
          }
        }

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = neverEmittingRepository,
            speechToTextRepository = io.mockk.mockk(relaxed = true),
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.LOADING_INDICATOR).assertExists()
  }

  @Test
  fun errorState_meetingNotFound_displaysErrorMessage() {
    meetingFlow.value = null

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = io.mockk.mockk(relaxed = true),
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.ERROR_MESSAGE).assertExists()
    composeTestRule.onNodeWithText("Meeting not found").assertExists()
  }

  @Test
  fun errorState_noAudioRecording_displaysErrorMessage() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            attachmentUrls = emptyList() // No audio
            )

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = io.mockk.mockk(relaxed = true),
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.ERROR_MESSAGE).assertExists()
    composeTestRule.onNodeWithText("No audio recording found for this meeting").assertExists()
  }

  @Test
  fun meetingWithAudio_displaysGenerateTranscriptButton() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            attachmentUrls = listOf("https://test.com/audio.mp4"),
            transcriptId = null)

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = io.mockk.mockk(relaxed = true),
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(TranscriptScreenTestTags.GENERATE_TRANSCRIPT_BUTTON)
        .assertExists()
    composeTestRule.onNodeWithText("Generate Transcript").assertExists()
  }

  @Test
  fun screenDisplaysCorrectly() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            attachmentUrls = listOf("https://test.com/audio.mp4"))

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = io.mockk.mockk(relaxed = true),
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.TRANSCRIPT_SCREEN).assertExists()
  }
}
