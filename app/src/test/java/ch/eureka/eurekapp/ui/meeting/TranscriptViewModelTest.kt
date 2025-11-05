package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.chatbot.ChatbotRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.transcription.AudioTranscription
import ch.eureka.eurekapp.model.data.transcription.SpeechToTextRepository
import ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus
import io.mockk.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Test suite for TranscriptViewModel
 *
 * Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TranscriptViewModelTest {

  private lateinit var viewModel: TranscriptViewModel
  private lateinit var meetingRepository: MeetingRepository
  private lateinit var speechToTextRepository: SpeechToTextRepository
  private lateinit var chatbotRepository: ChatbotRepository
  private lateinit var testDispatcher: TestDispatcher

  private val projectId = "test-project"
  private val meetingId = "test-meeting"
  private val audioUrl = "https://test.com/audio.mp4"
  private val transcriptId = "test-transcript-id"

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    meetingRepository = mockk(relaxed = true)
    speechToTextRepository = mockk(relaxed = true)
    chatbotRepository = mockk(relaxed = true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    clearAllMocks()
  }

  @Test
  fun `initial state is loading`() = runTest {
    val meeting = createTestMeeting(attachmentUrls = listOf(audioUrl))
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)

    val initialState = viewModel.uiState.first()
    assertTrue(initialState.isLoading)
  }

  @Test
  fun `shows error when meeting not found`() = runTest {
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(null)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.drop(1).first() // Skip loading state
    assertEquals("Meeting not found", state.errorMsg)
    assertNull(state.meeting)
  }

  @Test
  fun `shows error when no audio recording found`() = runTest {
    val meeting = createTestMeeting(attachmentUrls = emptyList())
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.drop(1).first() // Skip loading state
    assertEquals("No audio recording found for this meeting", state.errorMsg)
    assertNull(state.audioUrl)
  }

  @Test
  fun `loads meeting with audio successfully`() = runTest {
    val meeting = createTestMeeting(attachmentUrls = listOf(audioUrl))
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.drop(1).first() // Skip loading state
    assertEquals(meeting, state.meeting)
    assertEquals(audioUrl, state.audioUrl)
    assertNull(state.errorMsg)
    assertFalse(state.hasTranscript)
  }

  @Test
  fun `loads meeting with transcript successfully`() = runTest {
    val meeting = createTestMeeting(attachmentUrls = listOf(audioUrl), transcriptId = transcriptId)
    val transcript =
        AudioTranscription(
            transcriptionId = transcriptId,
            meetingId = meetingId,
            projectId = projectId,
            audioDownloadUrl = audioUrl,
            transcriptionText = "Test transcript text",
            status = TranscriptionStatus.COMPLETED)

    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)
    coEvery {
      speechToTextRepository.getTranscriptionById(projectId, meetingId, transcriptId)
    } returns flowOf(transcript)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    val state = viewModel.uiState.drop(1).first() // Skip loading state
    assertTrue(state.hasTranscript)
    assertEquals("Test transcript text", state.transcriptionText)
    assertEquals(TranscriptionStatus.COMPLETED, state.transcriptionStatus)
  }

  @Test
  fun `clearErrorMsg clears error message`() = runTest {
    val meeting = createTestMeeting(attachmentUrls = emptyList())
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    // Verify error exists
    var state = viewModel.uiState.drop(1).first() // Skip loading state
    assertEquals("No audio recording found for this meeting", state.errorMsg)

    viewModel.clearErrorMsg()
    advanceUntilIdle()

    state = viewModel.uiState.drop(1).first() // Skip loading state
    assertNull(state.errorMsg)
  }

  private fun createTestMeeting(
      attachmentUrls: List<String> = emptyList(),
      transcriptId: String? = null
  ) =
      Meeting(
          meetingID = meetingId,
          projectId = projectId,
          title = "Test Meeting",
          status = MeetingStatus.SCHEDULED,
          createdBy = "test-user",
          participantIds = listOf("test-user"),
          attachmentUrls = attachmentUrls,
          transcriptId = transcriptId)
}
