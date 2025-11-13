package ch.eureka.eurekapp.model.data.audio

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.audio.RecordingState
import ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AudioRecordingViewModelTest {

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

  @Test
  fun startRecordingWorksAsExpected() {
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
  fun pauseRecordingWorksAsExpected() {
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
  fun stopRecordingWorksAsExpected() {
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
  fun resumeRecordingWorksAsExpected() {
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
  fun saveRecordingToDatabaseWorksAsExpected() {
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
  fun saveRecordingToDatabaseWorksAsExpectedOnFailure() {
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
