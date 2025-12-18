// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.model.data.audio

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.audio.RecordingState
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class AudioRecordingViewModelTest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Initialize mock connectivity observer
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)
  }

  @Test
  fun audioRecordingViewModel_startRecordingWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())

    viewModel.startRecording(context, "test_recording")
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.RUNNING)
    viewModel.startRecording(context, "test_recording")
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.RUNNING)
  }

  @Test
  fun audioRecordingViewModel_pauseRecordingWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.PAUSED)
    viewModel.pauseRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.PAUSED)
  }

  @Test
  fun audioRecordingViewModel_stopRecordingWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording_5")
    viewModel.stopRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.RUNNING)
    viewModel.pauseRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.PAUSED)
    viewModel.stopRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.STOPPED)
  }

  @Test
  fun audioRecordingViewModel_resumeRecordingWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.resumeRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.RUNNING)
    viewModel.pauseRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.PAUSED)
    viewModel.resumeRecording()
    Thread.sleep(2000)
    assertTrue(viewModel.isRecording.value == RecordingState.RUNNING)
  }

  @Test
  fun audioRecordingViewModel_deleteRecordingWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    viewModel.stopRecording()
    viewModel.deleteLocalRecording()
  }

  @Test
  fun audioRecordingViewModel_onClearedWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.testOnCleared()
  }

  @Test
  fun audioRecordingViewModel_saveRecordingToDatabaseWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(
            fileStorageRepository = MockedStorageRepository(),
            recordingRepository = LocalAudioRecordingRepository(),
            meetingRepository = object : MeetingRepositoryMock() {})

    var runned = false

    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    runBlocking {
      viewModel.saveRecordingToDatabase("", "", { runned = true }, {}, onCompletion = {})
    }
    Thread.sleep(2000)
    assertTrue(runned)
  }

  @Test
  fun audioRecordingViewModel_saveRecordingToDatabaseWorksAsExpectedOnFailure() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(
            fileStorageRepository = ErrorMockedStorageRepository(),
            recordingRepository = LocalAudioRecordingRepository(),
            meetingRepository = object : MeetingRepositoryMock() {})

    var runned = false

    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    runBlocking {
      viewModel.saveRecordingToDatabase("", "", {}, { runned = true }, onCompletion = {})
    }
    Thread.sleep(2000)
    assertTrue(runned)
  }
}
