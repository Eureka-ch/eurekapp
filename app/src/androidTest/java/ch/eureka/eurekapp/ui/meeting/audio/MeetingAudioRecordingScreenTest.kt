package ch.eureka.eurekapp.ui.meeting.audio

import android.Manifest
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.data.audio.MockedStorageRepository
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioRecordingScreen
import ch.eureka.eurekapp.screens.subscreens.meetings.MeetingAudioScreenTestTags
import org.junit.Rule
import org.junit.Test

class MeetingAudioRecordingScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO
    )
    @Test
    fun recordingWorks(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        composeTestRule.setContent {
            MeetingAudioRecordingScreen(
                context = context,
                projectId = "test-project-id",
                meetingId = "meeting-id",
                audioRecordingViewModel = AudioRecordingViewModel(
                    fileStorageRepository = MockedStorageRepository(),
                    recordingRepository = LocalAudioRecordingRepository())
            )
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
            .performClick()

        Thread.sleep(2000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
            .performClick()

        Thread.sleep(2000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON)
            .performClick()

        Thread.sleep(2000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
            .performClick()

        Thread.sleep(2000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
            .performClick()
        Thread.sleep(2000)
        composeTestRule.waitForIdle()


        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag(MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
            .performClick()
        Thread.sleep(2000)
        composeTestRule.waitForIdle()

    }
}