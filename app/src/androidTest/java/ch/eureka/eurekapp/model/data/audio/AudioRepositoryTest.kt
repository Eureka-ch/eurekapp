package ch.eureka.eurekapp.model.data.audio

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.audio.RecordingState
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.flowOf
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

    assertTrue(repo.getRecordingStateFlow().value == RecordingState.RUNNING)

    repo.clearRecording()
    repo.deleteRecording()
  }

  @Test
  fun testPauseAndResumeRecordingWorks() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val repo = LocalAudioRecordingRepository()

    val recordingState = repo.getRecordingStateFlow()

    val result = repo.createRecording(context, "mock_recording_2.mp4")

    assertTrue(recordingState.value == RecordingState.RUNNING)
    Thread.sleep(1000)
    repo.pauseRecording()
    assertTrue(recordingState.value == RecordingState.PAUSED)
    Thread.sleep(2000)
    repo.resumeRecording()
    assertTrue(recordingState.value == RecordingState.RUNNING)

    repo.clearRecording()
    repo.deleteRecording()
  }

  @Test
  fun completelyStopAndDeleteRecording() {
    val context: Context = ApplicationProvider.getApplicationContext()
    val repo = LocalAudioRecordingRepository()

    val recordingState = repo.getRecordingStateFlow()

    repo.createRecording(context, "mock_recording_2.mp4")

    assertTrue(recordingState.value == RecordingState.RUNNING)

    Thread.sleep(2000)

    repo.pauseRecording()
    Thread.sleep(1000)
    repo.clearRecording()

    assertTrue(recordingState.value == RecordingState.STOPPED)

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
