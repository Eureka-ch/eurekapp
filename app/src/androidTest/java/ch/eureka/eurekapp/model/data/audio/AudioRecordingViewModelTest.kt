package ch.eureka.eurekapp.model.data.audio

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.audio.RECORDING_STATE
import ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AudioRecordingViewModelTest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

  @Test
  fun startRecordingWorksAsExpected() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())

    viewModel.startRecording(context, "test_recording")
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.RUNNING } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
    viewModel.startRecording(context, "test_recording")
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.RUNNING } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
  }

  @Test
  fun pauseRecordingWorksAsExpected() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()

    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.PAUSED } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
    viewModel.pauseRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.PAUSED } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
  }

  @Test
  fun stopRecordingWorksAsExpected() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording_5")
    viewModel.stopRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.RUNNING } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
    viewModel.pauseRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.PAUSED } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
    viewModel.stopRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.STOPPED } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.STOPPED)
  }

  @Test
  fun resumeRecordingWorksAsExpected() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.resumeRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.RUNNING } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
    viewModel.pauseRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.PAUSED } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
    viewModel.resumeRecording()
    withTimeout(5000) { viewModel.isRecording.first { it == RECORDING_STATE.RUNNING } }
    assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
  }

  @Test
  fun deleteRecordingWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    viewModel.stopRecording()
    viewModel.deleteLocalRecording()
  }

  @Test
  fun onClearedWorksAsExpected() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(recordingRepository = LocalAudioRecordingRepository())
    viewModel.startRecording(context, "test_recording")
    viewModel.testOnCleared()
  }

  @Test
  fun saveRecordingToDatabaseWorksAsExpected() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(
            fileStorageRepository = MockedStorageRepository(),
            recordingRepository = LocalAudioRecordingRepository(),
            meetingRepository = object : MeetingRepositoryMock() {})

    var runned = false

    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    viewModel.saveRecordingToDatabase("", "", { runned = true }, {})
    withTimeout(5000) {
      while (!runned) {
        kotlinx.coroutines.delay(100)
      }
    }
    assertTrue(runned)
  }

  @Test
  fun saveRecordingToDatabaseWorksAsExpectedOnFailure() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val viewModel: AudioRecordingViewModel =
        AudioRecordingViewModel(
            fileStorageRepository = ErrorMockedStorageRepository(),
            recordingRepository = LocalAudioRecordingRepository(),
            meetingRepository = object : MeetingRepositoryMock() {})

    var runned = false

    viewModel.startRecording(context, "test_recording")
    viewModel.pauseRecording()
    viewModel.saveRecordingToDatabase("", "", {}, { runned = true })
    withTimeout(5000) {
      while (!runned) {
        kotlinx.coroutines.delay(100)
      }
    }
    assertTrue(runned)
  }
}
