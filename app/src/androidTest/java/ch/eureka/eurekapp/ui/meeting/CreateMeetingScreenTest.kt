package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * UI test suite for the [CreateMeetingScreen].
 *
 * Note : some tests were generated with Gemini
 */
class CreateMeetingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: CreateMeetingViewModel
  private lateinit var repositoryMock: MockCreateMeetingRepository

  // To track if the onDone callback is invoked
  private var onDoneCalled = false
  private val testProjectId = "project-123"

  @Before
  fun setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    onDoneCalled = false

    repositoryMock = MockCreateMeetingRepository()
    viewModel =
        CreateMeetingViewModel(repository = repositoryMock, getCurrentUserId = { "test-user-id" })

    composeTestRule.setContent {
      CreateMeetingScreen(
          projectId = testProjectId,
          onDone = { onDoneCalled = true },
          createMeetingViewModel = viewModel)
    }
  }

  private fun findOkButton() = composeTestRule.onNodeWithText("OK")

  private fun findCancelButton() = composeTestRule.onNodeWithText("Cancel")

  @Test
  fun screenLoads_displaysStaticContent_andButtonIsDisabled() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_TITLE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_DESCRIPTION)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_START_TIME)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_END_TIME)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.ERROR_MSG).assertDoesNotExist()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun titleInput_onFocusChanged_triggersTouchTitle() {
    assertFalse(viewModel.uiState.value.hasTouchedTitle)

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE).performClick()

    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedTitle)
  }

  @Test
  fun titleInput_whenTouchedAndBlank_showsError() {
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE).performClick()
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()

    composeTestRule.onNodeWithText("Title cannot be empty").assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("My Meeting")

    composeTestRule.onNodeWithText("Title cannot be empty").assertDoesNotExist()
  }

  @Test
  fun startTime_onFocusChanged_triggersTouchStartTime() {
    assertFalse(viewModel.uiState.value.hasTouchedStartTime)

    composeTestRule.onNodeWithText("Start Time").performClick()

    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedStartTime)
  }

  @Test
  fun endTime_onFocusChanged_triggersTouchEndTime() {
    assertFalse(viewModel.uiState.value.hasTouchedEndTime)

    composeTestRule.onNodeWithText("End Time").performClick()

    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedEndTime)
  }

  @Test
  fun timeValidation_whenStartTimeAfterEndTime_showsError() {
    composeTestRule.runOnIdle {
      viewModel.setStartTime(LocalTime.of(14, 0))
      viewModel.setEndTime(LocalTime.of(12, 0))
      viewModel.touchStartTime()
    }

    composeTestRule.onNodeWithText("Start time should be smaller than end time").assertIsDisplayed()

    composeTestRule.runOnIdle { viewModel.touchEndTime() }

    composeTestRule.onNodeWithText("End time should be greater than start time").assertIsDisplayed()

    composeTestRule.runOnIdle { viewModel.setStartTime(LocalTime.of(11, 0)) }

    composeTestRule
        .onNodeWithText("Start time should be smaller than end time")
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithText("End time should be greater than start time")
        .assertDoesNotExist()
  }

  @Test
  fun saveButton_isEnabled_whenStateIsValid() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("Valid Meeting Title")

    composeTestRule.runOnIdle {
      viewModel.setStartTime(LocalTime.of(10, 0))
      viewModel.setEndTime(LocalTime.of(11, 0))
    }

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextClearance()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()
  }

  @Test
  fun createMeeting_whenSuccess_callsOnDone() {
    repositoryMock.shouldSucceed = true

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("My Successful Meeting")
    composeTestRule.runOnIdle {
      viewModel.setStartTime(LocalTime.of(9, 30))
      viewModel.setEndTime(LocalTime.of(10, 30))
    }

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitForIdle()

    assertTrue("onDone callback was not invoked on success", onDoneCalled)

    assertNotNull(repositoryMock.lastMeetingCreated)
    assertEquals("My Successful Meeting", repositoryMock.lastMeetingCreated?.title)
    assertEquals(testProjectId, repositoryMock.lastMeetingCreated?.projectId)
  }

  @Test
  fun createMeeting_whenRepositoryFails_showsErrorAndClearsIt() {
    repositoryMock.shouldSucceed = false

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("My Failed Meeting")
    composeTestRule.runOnIdle {
      viewModel.setStartTime(LocalTime.of(10, 0))
      viewModel.setEndTime(LocalTime.of(11, 0))
    }

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON).performClick()

    composeTestRule.waitForIdle()

    assertFalse("onDone callback was invoked on failure", onDoneCalled)

    assertNull("errorMsg was not cleared by the LaunchedEffect", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun dateInputField_opensDialog_andCancels() {
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()

    composeTestRule.waitForIdle()

    findOkButton().assertIsDisplayed()

    findCancelButton().performClick()

    findOkButton().assertDoesNotExist()
  }

  @Test
  fun dateInputField_opensDialogWithIcon_andConfirms() {
    val initialDate = viewModel.uiState.value.date

    composeTestRule.onNodeWithContentDescription("Select date").performClick()

    findOkButton().assertIsDisplayed()

    findOkButton().performClick()

    findOkButton().assertDoesNotExist()

    assertEquals(initialDate, viewModel.uiState.value.date)
  }

  @Test
  fun timeInputField_opensDialog_andCancels() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_START_TIME)
        .performClick()

    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()

    findCancelButton().performClick()

    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()
  }

  @Test
  fun timeInputField_opensDialogWithIcon_andConfirms() {
    val initialTimeTruncated = viewModel.uiState.value.endTime.truncatedTo(ChronoUnit.MINUTES)

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_END_TIME).performClick()

    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()

    findOkButton().performClick()

    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()

    val actualTimeTruncated = viewModel.uiState.value.endTime.truncatedTo(ChronoUnit.MINUTES)

    assertEquals(initialTimeTruncated, actualTimeTruncated)
  }
}
