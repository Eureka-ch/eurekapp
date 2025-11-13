package ch.eureka.eurekapp.model.data.audio

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.audio.RECORDING_STATE
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.Test

/** Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5 */
class AudioRepositoryTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(android.Manifest.permission.RECORD_AUDIO)

  @Test
  fun testCreateRecordingWorks() {
    val context: Context = ApplicationProvider.getApplicationContext()
    composeTestRule.setContent {
      val context = LocalContext.current
      MeetingAudioRecordingScreen(
          projectId = "test-project-id",
          meetingId = "test-meeting-id",
          meetingRepository =
              object : MeetingRepositoryMock() {
                override fun getMeetingById(projectId: String, meetingId: String) =
                    flowOf(Meeting(projectId = projectId, meetingID = meetingId))
              })
    }
    val repo = LocalAudioRecordingRepository()

    val result = repo.createRecording(context, "mock_recording_2.mp4")

    assertTrue(repo.getRecordingStateFlow().value == RECORDING_STATE.RUNNING)

    repo.clearRecording()
    repo.deleteRecording()
  }

  @Test
  fun testPauseAndResumeRecordingWorks() = runBlocking {
    val context: Context = ApplicationProvider.getApplicationContext()
    val repo = LocalAudioRecordingRepository()

    val recordingState = repo.getRecordingStateFlow()

    val result = repo.createRecording(context, "mock_recording_2.mp4")

    assertTrue(recordingState.value == RECORDING_STATE.RUNNING)
    repo.pauseRecording()
    withTimeout(5000) { recordingState.first { it == RECORDING_STATE.PAUSED } }
    assertTrue(recordingState.value == RECORDING_STATE.PAUSED)
    repo.resumeRecording()
    withTimeout(5000) { recordingState.first { it == RECORDING_STATE.RUNNING } }
    assertTrue(recordingState.value == RECORDING_STATE.RUNNING)

    repo.clearRecording()
    repo.deleteRecording()
  }

  @Test
  fun completelyStopAndDeleteRecording() = runBlocking {
    val context: Context = ApplicationProvider.getApplicationContext()
    val repo = LocalAudioRecordingRepository()

    val recordingState = repo.getRecordingStateFlow()

    repo.createRecording(context, "mock_recording_2.mp4")

    assertTrue(recordingState.value == RECORDING_STATE.RUNNING)

    repo.pauseRecording()
    withTimeout(5000) { recordingState.first { it == RECORDING_STATE.PAUSED } }
    repo.clearRecording()
    withTimeout(5000) { recordingState.first { it == RECORDING_STATE.STOPPED } }

    assertTrue(recordingState.value == RECORDING_STATE.STOPPED)

    repo.deleteRecording()
  }

  @Test
  fun testNotAllowedStatesActuallyWork() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val repo = LocalAudioRecordingRepository()

    repo.createRecording(context, "mock_recording_2.mp4")
    val result = repo.createRecording(context, "mock_recording_2.mp4")
    assertTrue(result.isFailure)

    val result2 = repo.resumeRecording()
    assertTrue(result2.isFailure)

    val result3 = repo.clearRecording()
    assertTrue(result3.isFailure)

    repo.pauseRecording()
    val result4 = repo.pauseRecording()
    assertTrue(result4.isFailure)
  }
}
