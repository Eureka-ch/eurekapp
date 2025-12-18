package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.chatbot.ChatbotRepository
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
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

  private val projectId = "test-project-id"
  private val meetingId = "test-meeting-id"
  private val audioUrl = "https://test.com/audio.mp4"
  private val transcriptId = "test-transcript-id"

  @Before
  fun setup() {
    testDispatcher = StandardTestDispatcher()
    Dispatchers.setMain(testDispatcher)

    meetingRepository = mockk(relaxed = true)
    speechToTextRepository = mockk(relaxed = true)
    chatbotRepository = mockk(relaxed = true)

    // Mock ConnectivityObserverProvider
    mockkObject(ConnectivityObserverProvider)
    val mockConnectivityObserver = mockk<ConnectivityObserver>(relaxed = true)
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
    every { ConnectivityObserverProvider.connectivityObserver } returns mockConnectivityObserver
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    unmockkAll()
  }

  @Test
  fun transcriptViewModel_initialStateIsLoading() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl)
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
  fun transcriptViewModel_showsErrorWhenMeetingNotFound() = runTest {
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
  fun transcriptViewModel_showsErrorWhenNoAudioRecordingFound() = runTest {
    val meeting = createTestMeeting(audioUrl = null)
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
  fun transcriptViewModel_loadsMeetingWithAudioSuccessfully() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl)
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
  fun transcriptViewModel_loadsMeetingWithTranscriptSuccessfully() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl, transcriptId = transcriptId)
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
  fun transcriptViewModel_clearErrorMsgClearsErrorMessage() = runTest {
    val meeting = createTestMeeting(audioUrl = null)
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

  @Test
  fun transcriptViewModel_generateTranscriptFailsWhenNoAudioUrlAvailable() = runTest {
    val meeting = createTestMeeting(audioUrl = null)
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    viewModel.generateTranscript("en-US")
    advanceUntilIdle()

    // Should not call transcribeAudio since no audio URL
    coVerify(exactly = 0) { speechToTextRepository.transcribeAudio(any(), any(), any(), any()) }
  }

  @Test
  fun transcriptViewModel_generateTranscriptWithValidAudioUrlDoesNotThrow() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl)
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)
    coEvery { speechToTextRepository.transcribeAudio(any(), any(), any(), any()) } returns
        Result.success(transcriptId)
    coEvery { meetingRepository.updateMeeting(any()) } returns Result.success(Unit)

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    // Should not throw when called with valid audio
    viewModel.generateTranscript("en-US")
    advanceUntilIdle()
  }

  @Test
  fun transcriptViewModel_generateTranscriptWithFailureDoesNotUpdateMeeting() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl)
    var updateCalled = false
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)
    coEvery { speechToTextRepository.transcribeAudio(any(), any(), any(), any()) } returns
        Result.failure(Exception("Network error"))
    coEvery { meetingRepository.updateMeeting(any()) } answers
        {
          updateCalled = true
          Result.success(Unit)
        }

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    viewModel.generateTranscript("en-US")
    advanceUntilIdle()

    // Meeting should NOT be updated on failure
    assertFalse(updateCalled)
  }

  @Test
  fun transcriptViewModel_generateSummaryWithNoTranscriptDoesNotCallChatbot() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl)
    var chatbotCalled = false
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)
    coEvery { chatbotRepository.sendMessage(any(), any()) } answers
        {
          chatbotCalled = true
          "summary"
        }

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    viewModel.generateSummary()
    advanceUntilIdle()

    // Chatbot should NOT be called when there's no transcript
    assertFalse(chatbotCalled)
  }

  @Test
  fun transcriptViewModel_generateSummaryWithTranscriptTextDoesNotThrow() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl, transcriptId = transcriptId)
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
    coEvery { chatbotRepository.sendMessage(any(), any()) } returns "Test summary"

    viewModel =
        TranscriptViewModel(
            projectId = projectId,
            meetingId = meetingId,
            meetingRepository = meetingRepository,
            speechToTextRepository = speechToTextRepository,
            chatbotRepository = chatbotRepository)
    advanceUntilIdle()

    // Should not throw when called with valid transcript
    viewModel.generateSummary()
    advanceUntilIdle()
  }

  @Test
  fun transcriptViewModel_transcriptWithPendingStatusDoesNotShowText() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl, transcriptId = transcriptId)
    val transcript =
        AudioTranscription(
            transcriptionId = transcriptId,
            meetingId = meetingId,
            projectId = projectId,
            audioDownloadUrl = audioUrl,
            transcriptionText = "",
            status = TranscriptionStatus.PENDING)

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

    val state = viewModel.uiState.drop(1).first()
    assertNull(state.transcriptionText)
    assertEquals(TranscriptionStatus.PENDING, state.transcriptionStatus)
  }

  @Test
  fun transcriptViewModel_transcriptWithFailedStatusShowsError() = runTest {
    val meeting = createTestMeeting(audioUrl = audioUrl, transcriptId = transcriptId)
    val transcript =
        AudioTranscription(
            transcriptionId = transcriptId,
            meetingId = meetingId,
            projectId = projectId,
            audioDownloadUrl = audioUrl,
            transcriptionText = "",
            status = TranscriptionStatus.FAILED,
            errorMessage = "Audio quality too low")

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

    val state = viewModel.uiState.drop(1).first()
    assertEquals("Audio quality too low", state.errorMsg)
    assertEquals(TranscriptionStatus.FAILED, state.transcriptionStatus)
  }

  private fun createTestMeeting(audioUrl: String? = null, transcriptId: String? = null) =
      Meeting(
          meetingID = meetingId,
          projectId = projectId,
          title = "Test Meeting",
          status = MeetingStatus.SCHEDULED,
          createdBy = "test-user",
          participantIds = listOf("test-user"),
          audioUrl = audioUrl,
          transcriptId = transcriptId)
}
