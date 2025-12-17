package ch.eureka.eurekapp.model.audio
// Parts of this code were generated using the help of Gemini 3 Pro
import android.content.Context
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AudioRecordingViewModelTimerTest {

  private lateinit var viewModel: AudioRecordingViewModel
  private val recordingRepository: LocalAudioRecordingRepository = mockk(relaxed = true)
  private val fileStorageRepository: FileStorageRepository = mockk(relaxed = true)
  private val meetingRepository: MeetingRepository = mockk(relaxed = true)
  private val context: Context = mockk(relaxed = true)

  private val recordingStateFlow = MutableStateFlow(RecordingState.STOPPED)

  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    every { recordingRepository.getRecordingStateFlow() } returns recordingStateFlow

    viewModel =
        AudioRecordingViewModel(
            fileStorageRepository = fileStorageRepository,
            recordingRepository = recordingRepository,
            meetingRepository = meetingRepository,
        )
  }

  @After
  fun tearDown() {
    recordingStateFlow.value = RecordingState.STOPPED

    testDispatcher.scheduler.advanceUntilIdle()

    Dispatchers.resetMain()
  }

  @Test
  fun `startRecording initiates timer and increments seconds`() =
      runTest(testDispatcher) {
        recordingStateFlow.value = RecordingState.RUNNING
        every { recordingRepository.createRecording(any(), any()) } returns Result.success(mockk())

        viewModel.startRecording(context, "test.mp4")

        advanceTimeBy(1100)

        assertEquals(
            "Timer should increment to 1 after 1 second",
            1L,
            viewModel.recordingTimeInSeconds.value)
        recordingStateFlow.value = RecordingState.PAUSED
      }

  @Test
  fun `resumeRecording starts timer if state is PAUSED and resume succeeds`() =
      runTest(testDispatcher) {
        recordingStateFlow.value = RecordingState.PAUSED
        every { recordingRepository.resumeRecording() } returns Result.success(Unit)

        viewModel.resumeRecording()

        recordingStateFlow.value = RecordingState.RUNNING

        advanceTimeBy(1100)

        assertEquals(
            "Timer should increment after resuming", 1L, viewModel.recordingTimeInSeconds.value)

        recordingStateFlow.value = RecordingState.PAUSED
      }

  @Test
  fun `stopRecording resets timer to zero`() =
      runTest(testDispatcher) {
        recordingStateFlow.value = RecordingState.RUNNING
        every { recordingRepository.createRecording(any(), any()) } returns Result.success(mockk())
        viewModel.startRecording(context, "test")
        advanceTimeBy(1100)

        assertEquals(1L, viewModel.recordingTimeInSeconds.value)

        recordingStateFlow.value = RecordingState.PAUSED
        every { recordingRepository.clearRecording() } returns Result.success(Unit)

        viewModel.stopRecording()

        assertEquals(
            "Timer should be reset to 0 after stopping", 0L, viewModel.recordingTimeInSeconds.value)
      }

  @Test
  fun `timer loop stops when RecordingState is no longer RUNNING`() =
      runTest(testDispatcher) {
        recordingStateFlow.value = RecordingState.RUNNING
        every { recordingRepository.createRecording(any(), any()) } returns Result.success(mockk())
        viewModel.startRecording(context, "test")

        advanceTimeBy(1100)
        assertEquals(1L, viewModel.recordingTimeInSeconds.value)

        recordingStateFlow.value = RecordingState.PAUSED

        advanceTimeBy(5000)

        assertEquals(
            "Timer should stop incrementing when state is not RUNNING",
            1L,
            viewModel.recordingTimeInSeconds.value)
      }
}
