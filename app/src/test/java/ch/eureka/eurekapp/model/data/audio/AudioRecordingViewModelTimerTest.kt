package ch.eureka.eurekapp.model.audio
// Parts of this code were generated using the help of Gemini 3 Pro and Grok
import android.content.Context
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
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

  private val mockConnectivityObserver: ConnectivityObserver = mockk(relaxed = true)

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    every { recordingRepository.getRecordingStateFlow() } returns recordingStateFlow

    // Initialize mock connectivity observer
    every { mockConnectivityObserver.isConnected } returns MutableStateFlow<Boolean>(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

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
  fun audioRecordingViewModelTimer_startRecordingInitiatesTimerAndIncrementsSeconds() =
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
  fun audioRecordingViewModelTimer_resumeRecordingStartsTimerIfStateIsPausedAndResumeSucceeds() =
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
  fun audioRecordingViewModelTimer_stopRecordingResetsTimerToZero() =
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
  fun audioRecordingViewModelTimer_timerLoopStopsWhenRecordingStateIsNoLongerRunning() =
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
