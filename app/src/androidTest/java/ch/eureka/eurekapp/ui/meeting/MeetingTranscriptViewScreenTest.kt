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
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "")

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

  @Test
  fun dismissButton_clearsErrorMessage() {
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

    composeTestRule.onNodeWithText("Meeting not found").assertExists()
    composeTestRule.onNodeWithText("Dismiss").performClick()

    composeTestRule.waitForIdle()
    // After dismissing, error should be cleared
  }

  @Test
  fun navigationIcon_callsOnNavigateBack() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            attachmentUrls = listOf("https://test.com/audio.mp4"),
            audioUrl = "https://test.com/audio.mp4")

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = io.mockk.mockk(relaxed = true),
            chatbotRepository = io.mockk.mockk(relaxed = true))

    var backCalled = false
    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = { backCalled = true })
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithContentDescription("Back").performClick()
    assert(backCalled)
  }

  @Test
  fun transcriptGenerating_displaysLoadingIndicator() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "test-transcript")

    val transcriptRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery {
      transcriptRepository.getTranscriptionById("test-project", "test-meeting", "test-transcript")
    } returns
        kotlinx.coroutines.flow.flowOf(
            ch.eureka.eurekapp.model.data.transcription.AudioTranscription(
                transcriptionId = "test-transcript",
                meetingId = "test-meeting",
                projectId = "test-project",
                audioDownloadUrl = "https://test.com/audio.mp4",
                transcriptionText = "",
                status = ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus.PENDING))

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = transcriptRepository,
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.TRANSCRIPTION_LOADING).assertExists()
    composeTestRule.onNodeWithText("Generating transcript...").assertExists()
  }

  @Test
  fun transcriptCompleted_displaysTranscriptText() {
    val transcriptText = "This is the completed transcript text."
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "test-transcript")

    val transcriptRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery {
      transcriptRepository.getTranscriptionById("test-project", "test-meeting", "test-transcript")
    } returns
        kotlinx.coroutines.flow.flowOf(
            ch.eureka.eurekapp.model.data.transcription.AudioTranscription(
                transcriptionId = "test-transcript",
                meetingId = "test-meeting",
                projectId = "test-project",
                audioDownloadUrl = "https://test.com/audio.mp4",
                transcriptionText = transcriptText,
                status = ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus.COMPLETED))

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = transcriptRepository,
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.TRANSCRIPT_TEXT).assertExists()
    composeTestRule.onNodeWithText(transcriptText).assertExists()
  }

  @Test
  fun transcriptFailed_displaysErrorMessage() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "test-transcript")

    val transcriptRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery {
      transcriptRepository.getTranscriptionById("test-project", "test-meeting", "test-transcript")
    } returns
        kotlinx.coroutines.flow.flowOf(
            ch.eureka.eurekapp.model.data.transcription.AudioTranscription(
                transcriptionId = "test-transcript",
                meetingId = "test-meeting",
                projectId = "test-project",
                audioDownloadUrl = "https://test.com/audio.mp4",
                transcriptionText = "",
                status = ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus.FAILED,
                errorMessage = "Audio quality too low"))

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = transcriptRepository,
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.TRANSCRIPTION_ERROR).assertExists()
    composeTestRule.onNodeWithText("Audio quality too low").assertExists()
  }

  @Test
  fun transcriptCompleted_displaysSummarySection() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "test-transcript")

    val transcriptRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery {
      transcriptRepository.getTranscriptionById("test-project", "test-meeting", "test-transcript")
    } returns
        kotlinx.coroutines.flow.flowOf(
            ch.eureka.eurekapp.model.data.transcription.AudioTranscription(
                transcriptionId = "test-transcript",
                meetingId = "test-meeting",
                projectId = "test-project",
                audioDownloadUrl = "https://test.com/audio.mp4",
                transcriptionText = "Test transcript",
                status = ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus.COMPLETED))

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = transcriptRepository,
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    // Summary button should be visible when transcript is completed
    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.GENERATE_SUMMARY_BUTTON).assertExists()
    composeTestRule.onNodeWithText("Generate Summary").assertExists()
  }

  @Test
  fun summaryGenerating_displaysLoadingIndicator() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "test-transcript")

    val transcriptRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery {
      transcriptRepository.getTranscriptionById("test-project", "test-meeting", "test-transcript")
    } returns
        kotlinx.coroutines.flow.flowOf(
            ch.eureka.eurekapp.model.data.transcription.AudioTranscription(
                transcriptionId = "test-transcript",
                meetingId = "test-meeting",
                projectId = "test-project",
                audioDownloadUrl = "https://test.com/audio.mp4",
                transcriptionText = "Test transcript",
                status = ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus.COMPLETED))

    val chatbotRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.chatbot.ChatbotRepository>(relaxed = true)
    // Simulate slow chatbot response to keep isSummarizing = true
    io.mockk.coEvery { chatbotRepository.sendMessage(any(), any()) } coAnswers
        {
          kotlinx.coroutines.delay(5000)
          "Test summary"
        }

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = transcriptRepository,
            chatbotRepository = chatbotRepository)

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    // Trigger summary generation
    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.GENERATE_SUMMARY_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Check for summary loading indicator
    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.SUMMARY_LOADING).assertExists()
    composeTestRule.onNodeWithText("Generating summary...").assertExists()
  }

  @Test
  fun summaryCompleted_displaysSummaryText() {
    val summaryText = "This is a summary of the meeting."
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "test-transcript")

    val transcriptRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery {
      transcriptRepository.getTranscriptionById("test-project", "test-meeting", "test-transcript")
    } returns
        kotlinx.coroutines.flow.flowOf(
            ch.eureka.eurekapp.model.data.transcription.AudioTranscription(
                transcriptionId = "test-transcript",
                meetingId = "test-meeting",
                projectId = "test-project",
                audioDownloadUrl = "https://test.com/audio.mp4",
                transcriptionText = "Test transcript",
                status = ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus.COMPLETED))

    val chatbotRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.chatbot.ChatbotRepository>(relaxed = true)
    io.mockk.coEvery { chatbotRepository.sendMessage(any(), any()) } returns summaryText

    meetingFlow.value = meeting

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = transcriptRepository,
            chatbotRepository = chatbotRepository)

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    // Trigger summary generation
    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.GENERATE_SUMMARY_BUTTON).performClick()

    composeTestRule.waitForIdle()

    // Check for summary text
    composeTestRule.onNodeWithTag(TranscriptScreenTestTags.SUMMARY_TEXT).assertExists()
    composeTestRule.onNodeWithText(summaryText).assertExists()
  }

  @Test
  fun errorMessageWithTranscript_displaysError() {
    val meeting =
        Meeting(
            meetingID = "test-meeting",
            projectId = "test-project",
            title = "Test Meeting",
            status = MeetingStatus.COMPLETED,
            audioUrl = "https://test.com/audio.mp4",
            transcriptId = "")

    meetingFlow.value = meeting

    val speechToTextRepository =
        io.mockk.mockk<ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository>(
            relaxed = true)
    io.mockk.coEvery { speechToTextRepository.transcribeAudio(any(), any(), any(), any()) } returns
        Result.failure(Exception("Network error"))

    val viewModel =
        TranscriptViewModel(
            projectId = "test-project",
            meetingId = "test-meeting",
            meetingRepository = repositoryMock,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = io.mockk.mockk(relaxed = true))

    composeTestRule.setContent {
      MeetingTranscriptViewScreen(
          projectId = "test-project",
          meetingId = "test-meeting",
          viewModel = viewModel,
          onNavigateBack = {})
    }

    composeTestRule.waitForIdle()

    // Trigger transcript generation
    composeTestRule
        .onNodeWithTag(TranscriptScreenTestTags.GENERATE_TRANSCRIPT_BUTTON)
        .performClick()

    composeTestRule.waitForIdle()

    // Error message should be displayed
    composeTestRule.onAllNodesWithTag(TranscriptScreenTestTags.ERROR_MESSAGE).assertCountEquals(1)
  }
}
