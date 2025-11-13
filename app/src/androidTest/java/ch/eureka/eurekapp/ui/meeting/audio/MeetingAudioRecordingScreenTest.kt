package ch.eureka.eurekapp.ui.meeting.audio

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingRepository
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.RECORDING_STATE
import ch.eureka.eurekapp.model.data.audio.MockedStorageRepository
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioScreenTestTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Note :This file was partially written by ChatGPT (GPT-5) and Grok Co-author : GPT-5 Co-author:
 * Grok
 */
class MeetingAudioRecordingScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

  private class MockAudioRecordingRepository : AudioRecordingRepository {
    private val recordingState = MutableStateFlow(RECORDING_STATE.STOPPED)

    override fun createRecording(context: Context, fileName: String): Result<Uri> {
      recordingState.value = RECORDING_STATE.RUNNING
      return Result.success(Uri.parse("file:///mock/recording.mp4"))
    }

    override fun clearRecording(): Result<Unit> {
      recordingState.value = RECORDING_STATE.STOPPED
      return Result.success(Unit)
    }

    override fun pauseRecording(): Result<Unit> {
      recordingState.value = RECORDING_STATE.PAUSED
      return Result.success(Unit)
    }

    override fun resumeRecording(): Result<Unit> {
      recordingState.value = RECORDING_STATE.RUNNING
      return Result.success(Unit)
    }

    override fun deleteRecording(): Result<Unit> {
      return Result.success(Unit)
    }

    override fun getRecordingStateFlow(): StateFlow<RECORDING_STATE> {
      return recordingState
    }
  }

  @Test
  fun recordingWorks() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    composeTestRule.setContent {
      MeetingAudioRecordingScreen(
          context = context,
          projectId = "test-project-id",
          meetingId = "meeting-id",
          audioRecordingViewModel =
              AudioRecordingViewModel(
                  fileStorageRepository = MockedStorageRepository(),
                  recordingRepository = MockAudioRecordingRepository()))
    }

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule
            .onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule
            .onNodeWithTag(MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON)
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule
            .onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule
            .onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON).performClick()

    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule
            .onNodeWithTag(MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
        .performClick()
  }

  @Test
  fun viewTranscriptButtonAppearsAfterSuccessfulUpload() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val mockMeetingRepository =
        object : ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String) =
              kotlinx.coroutines.flow.flowOf(
                  ch.eureka.eurekapp.model.data.meeting.Meeting(
                      projectId = projectId,
                      meetingID = meetingId,
                      transcriptId = "existing-transcript-id"))
        }

    composeTestRule.setContent {
      MeetingAudioRecordingScreen(
          context = context,
          projectId = "test-project-id",
          meetingId = "meeting-id",
          audioRecordingViewModel =
              AudioRecordingViewModel(
                  fileStorageRepository = MockedStorageRepository(),
                  recordingRepository = MockAudioRecordingRepository(),
                  meetingRepository = mockMeetingRepository),
          meetingRepository = mockMeetingRepository)
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithText("View Transcript").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  @Test
  fun viewTranscriptButtonNavigatesToTranscriptScreen() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val mockMeetingRepository =
        object : ch.eureka.eurekapp.ui.meeting.MeetingRepositoryMock() {
          override fun getMeetingById(projectId: String, meetingId: String) =
              kotlinx.coroutines.flow.flowOf(
                  ch.eureka.eurekapp.model.data.meeting.Meeting(
                      projectId = projectId,
                      meetingID = meetingId,
                      transcriptId = "existing-transcript-id"))
        }

    var navigatedToTranscript = false
    var capturedProjectId = ""
    var capturedMeetingId = ""

    composeTestRule.setContent {
      MeetingAudioRecordingScreen(
          context = context,
          projectId = "test-project-id",
          meetingId = "meeting-id",
          audioRecordingViewModel =
              AudioRecordingViewModel(
                  fileStorageRepository = MockedStorageRepository(),
                  recordingRepository = MockAudioRecordingRepository(),
                  meetingRepository = mockMeetingRepository),
          meetingRepository = mockMeetingRepository,
          onNavigateToTranscript = { projectId, meetingId ->
            navigatedToTranscript = true
            capturedProjectId = projectId
            capturedMeetingId = meetingId
          })
    }

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule
            .onNodeWithTag(MeetingAudioScreenTestTags.GENERATE_AI_TRANSCRIPT_BUTTON)
            .assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.GENERATE_AI_TRANSCRIPT_BUTTON)
        .performClick()

    assert(navigatedToTranscript)
    assert(capturedProjectId == "test-project-id")
    assert(capturedMeetingId == "meeting-id")
  }
}
