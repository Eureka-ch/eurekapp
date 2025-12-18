package ch.eureka.eurekapp.ui.meeting.audio

import android.Manifest
import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.audio.MockedStorageRepository
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioScreenTestTags
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Note :This file was partially written by ChatGPT (GPT-5) and Grok Co-author : GPT-5 Co-author :
 * Grok
 */
class MeetingAudioRecordingScreenTest {
  @get:Rule val composeTestRule = createComposeRule()

  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO)

  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()

    // Initialize mock connectivity observer for all tests
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)
  }

  @Test
  fun meetingAudioRecordingScreen_recordingWorks() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    composeTestRule.setContent {
      MeetingAudioRecordingScreen(
          context = context,
          projectId = "test-project-id",
          meetingId = "meeting-id",
          audioRecordingViewModel =
              AudioRecordingViewModel(
                  fileStorageRepository = MockedStorageRepository(),
                  recordingRepository = LocalAudioRecordingRepository()))
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON).performClick()

    Thread.sleep(2000)
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON).performClick()

    Thread.sleep(2000)
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON).performClick()

    Thread.sleep(2000)
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON).performClick()

    Thread.sleep(2000)
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON).performClick()
    Thread.sleep(2000)
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
        .performClick()
    Thread.sleep(2000)
    composeTestRule.waitForIdle()
  }

  @Test
  fun meetingAudioRecordingScreen_viewTranscriptButtonAppearsAfterSuccessfulUpload() {
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
                  recordingRepository = LocalAudioRecordingRepository(),
                  meetingRepository = mockMeetingRepository),
          meetingRepository = mockMeetingRepository)
    }

    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.GENERATE_AI_TRANSCRIPT_BUTTON)
        .assertExists()
    composeTestRule.onNodeWithText("View Transcript").assertExists()
  }

  @Test
  fun meetingAudioRecordingScreen_viewTranscriptButtonNavigatesToTranscriptScreen() {
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
                  recordingRepository = LocalAudioRecordingRepository(),
                  meetingRepository = mockMeetingRepository),
          meetingRepository = mockMeetingRepository,
          onNavigateToTranscript = { projectId, meetingId ->
            navigatedToTranscript = true
            capturedProjectId = projectId
            capturedMeetingId = meetingId
          })
    }

    composeTestRule.waitForIdle()
    Thread.sleep(1000)

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.GENERATE_AI_TRANSCRIPT_BUTTON)
        .performClick()

    assert(navigatedToTranscript)
    assert(capturedProjectId == "test-project-id")
    assert(capturedMeetingId == "meeting-id")
  }

  @Test
  fun meetingAudioRecordingScreen_testBackButtonIsDisplayedAndCallsOnBackClick() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val onBackClickCalled = mutableStateOf(false)
    composeTestRule.setContent {
      MeetingAudioRecordingScreen(
          context = context,
          projectId = "test-project-id",
          meetingId = "meeting-id",
          audioRecordingViewModel =
              AudioRecordingViewModel(
                  fileStorageRepository = MockedStorageRepository(),
                  recordingRepository = LocalAudioRecordingRepository()),
          onBackClick = { onBackClickCalled.value = true })
    }

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) { onBackClickCalled.value }

    assert(onBackClickCalled.value) { "onBackClick should be called" }
  }

  @Test
  fun meetingAudioRecordingScreen_navigatesBackWhenConnectionLost() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val onBackClickCalled = mutableStateOf(false)
    composeTestRule.setContent {
      MeetingAudioRecordingScreen(
          context = context,
          projectId = "test-project-id",
          meetingId = "meeting-id",
          audioRecordingViewModel =
              AudioRecordingViewModel(
                  fileStorageRepository = MockedStorageRepository(),
                  recordingRepository = LocalAudioRecordingRepository()),
          onBackClick = { onBackClickCalled.value = true })
    }

    composeTestRule.waitForIdle()

    // Verify we're on MeetingAudioRecordingScreen
    composeTestRule
        .onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
        .assertIsDisplayed()

    // Simulate connection loss
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitForIdle()

    // Verify onBackClick was called
    composeTestRule.waitUntil(timeoutMillis = 5000) { onBackClickCalled.value }

    assert(onBackClickCalled.value) { "onBackClick should be called" }
  }
}
