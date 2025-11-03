package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
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
import org.junit.Assert.assertNotEquals
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
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME)
        .assertIsDisplayed()
    // *** ADDED THIS CHECK ***
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION)
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
  fun saveButton_isEnabled_whenStateIsValid() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("Valid Meeting Title")

    composeTestRule.runOnIdle {
      viewModel.setTime(LocalTime.of(10, 0))
      viewModel.setDuration(15) // This sets the duration directly on the VM
    }

    // Button is enabled because state is valid (title + duration >= 5)
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
      viewModel.setTime(LocalTime.of(9, 30))
      viewModel.setDuration(15) // Set valid duration
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
      viewModel.setTime(LocalTime.of(10, 0))
      viewModel.setDuration(30) // Set valid duration
    }

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled() // Ensure button is enabled before click
        .performClick()

    composeTestRule.waitForIdle()

    assertFalse("onDone callback was invoked on failure", onDoneCalled)

    // The LaunchedEffect should show the toast, then immediately set the error to null
    // We check that the VM state is clean
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
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()

    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()

    findCancelButton().performClick()

    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()
  }

  @Test
  fun timeInputField_opensDialogWithIcon_andConfirms() {
    val initialTimeTruncated = viewModel.uiState.value.time.truncatedTo(ChronoUnit.MINUTES)

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()

    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()

    findOkButton().performClick()

    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()

    val actualTimeTruncated = viewModel.uiState.value.time.truncatedTo(ChronoUnit.MINUTES)

    assertEquals(initialTimeTruncated, actualTimeTruncated)
  }

  // --- NEW TESTS FOR DURATION INPUT FIELD ---

  @Test
  fun durationInputField_opensDialog_andCancels() {
    val initialDuration = viewModel.uiState.value.duration
    assertEquals(0, initialDuration) // Check initial state

    // Click the duration field text area
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION).performClick()

    // Dialog appears
    composeTestRule.onNodeWithText("Select Duration").assertIsDisplayed()
    findOkButton().assertIsDisplayed()

    // Click on an option but then cancel
    composeTestRule.onNodeWithText("30 minutes").performClick()
    findCancelButton().performClick()

    // Dialog disappears
    composeTestRule.onNodeWithText("Select Duration").assertDoesNotExist()

    // Value is unchanged in the view model
    assertEquals(initialDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun durationInputField_opensDialogWithIcon_selectsOption_andConfirms() {
    val initialDuration = viewModel.uiState.value.duration
    assertEquals(0, initialDuration)

    // Click the duration field icon (this part is correct)
    composeTestRule.onNodeWithContentDescription("Select duration").performClick()

    // Dialog appears
    composeTestRule.onNodeWithText("Select Duration").assertIsDisplayed()

    // Select "45 minutes"
    composeTestRule.onNodeWithText("45 minutes").performClick()

    // Click OK
    findOkButton().performClick()

    // Dialog disappears
    composeTestRule.onNodeWithText("Select Duration").assertDoesNotExist()

    // Check that the view model and text field are updated
    val newDuration = viewModel.uiState.value.duration
    assertNotEquals(initialDuration, newDuration)
    assertEquals(45, newDuration)

    // Find the node by its label text "Duration" and check its value
    composeTestRule
        .onNodeWithText("Duration")
        .assertTextEquals("Duration", "45 minutes") // Check the OutlinedTextField value
  }

  @Test
  fun saveButton_isEnabled_afterSelectingAllInputsViaUI() {
    // 1. Initially, button is disabled
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    // 2. Enter a title
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("Full UI Test Meeting")

    // 3. Button is still disabled (duration is 0)
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    // 4. Select a duration
    composeTestRule.onNodeWithContentDescription("Select duration").performClick()
    composeTestRule.onNodeWithText("30 minutes").performClick()
    findOkButton().performClick()

    // 5. Button is now enabled
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled()
  }
}
