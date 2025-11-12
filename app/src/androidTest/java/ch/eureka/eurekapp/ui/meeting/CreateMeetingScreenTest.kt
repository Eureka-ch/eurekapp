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
import java.time.LocalDate
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

  // Helpers for future/past dates
  private val futureDate: LocalDate = LocalDate.now().plusDays(1)
  private val pastDate: LocalDate = LocalDate.now().minusDays(1)

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

    // Find fields by their icons/tags
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION)
        .assertIsDisplayed()

    // Check that neither error message is displayed by their tag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.ERROR_MSG).assertDoesNotExist()
    // Check by text as well for robustness
    composeTestRule.onNodeWithText("Title cannot be empty").assertDoesNotExist()
    composeTestRule
        .onNodeWithText("Meeting should be scheduled in the future.")
        .assertDoesNotExist()

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
    // Click title
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE).performClick()
    // Click date icon to remove focus from title
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    // *** ADDED: Close the dialog that opens ***
    findCancelButton().performClick()

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
      viewModel.setDuration(15)
      // *** ADDED: Set a future date for the state to be valid ***
      viewModel.setDate(futureDate)
    }

    // Button is enabled because state is valid
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
      viewModel.setDuration(15)
      // *** ADDED: Set a future date for the state to be valid ***
      viewModel.setDate(futureDate)
      viewModel.setTime(LocalTime.of(9, 30))
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
      viewModel.setDuration(30)
      // *** ADDED: Set a future date for the state to be valid ***
      viewModel.setDate(futureDate)
      viewModel.setTime(LocalTime.of(10, 0))
    }

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitForIdle()

    assertFalse("onDone callback was invoked on failure", onDoneCalled)
    assertNull("errorMsg was not cleared by the LaunchedEffect", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun dateInputField_opensDialog_andCancels() {
    // Clicks the icon via testTag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    composeTestRule.waitForIdle()
    findOkButton().assertIsDisplayed()
    findCancelButton().performClick()
    findOkButton().assertDoesNotExist()
  }

  @Test
  fun dateInputField_opensDialogWithIcon_andConfirms() {
    val initialDate = viewModel.uiState.value.date
    // Clicks the icon via contentDescription
    composeTestRule.onNodeWithContentDescription("Select date").performClick()
    findOkButton().assertIsDisplayed()
    findOkButton().performClick()
    findOkButton().assertDoesNotExist()
    assertEquals(initialDate, viewModel.uiState.value.date)
  }

  @Test
  fun timeInputField_opensDialog_andCancels() {
    // Clicks the icon via testTag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()
  }

  @Test
  fun timeInputField_opensDialogWithIcon_andConfirms() {
    val initialTimeTruncated = viewModel.uiState.value.time.truncatedTo(ChronoUnit.MINUTES)
    // Clicks the icon via testTag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()
    findOkButton().performClick()
    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()
    val actualTimeTruncated = viewModel.uiState.value.time.truncatedTo(ChronoUnit.MINUTES)
    assertEquals(initialTimeTruncated, actualTimeTruncated)
  }

  @Test
  fun durationInputField_opensDialog_andCancels() {
    val initialDuration = viewModel.uiState.value.duration
    assertEquals(0, initialDuration)
    // Clicks the icon via testTag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION).performClick()
    composeTestRule.onNodeWithText("Select Duration").assertIsDisplayed()
    findOkButton().assertIsDisplayed()
    composeTestRule.onNodeWithText("30 minutes").performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText("Select Duration").assertDoesNotExist()
    assertEquals(initialDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun durationInputField_opensDialogWithIcon_selectsOption_andConfirms() {
    val initialDuration = viewModel.uiState.value.duration
    assertEquals(0, initialDuration)
    // Clicks the icon via contentDescription
    composeTestRule.onNodeWithContentDescription("Select duration").performClick()
    composeTestRule.onNodeWithText("Select Duration").assertIsDisplayed()
    composeTestRule.onNodeWithText("45 minutes").performClick()
    findOkButton().performClick()
    composeTestRule.onNodeWithText("Select Duration").assertDoesNotExist()
    val newDuration = viewModel.uiState.value.duration
    assertNotEquals(initialDuration, newDuration)
    assertEquals(45, newDuration)
    // Finds the text field by its label and checks its value
    composeTestRule.onNodeWithText("Duration").assertTextEquals("Duration", "45 minutes")
  }

  @Test
  fun saveButton_isEnabled_afterSelectingAllInputsViaUI() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("Full UI Test Meeting")

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    composeTestRule.onNodeWithContentDescription("Select duration").performClick()
    composeTestRule.onNodeWithText("30 minutes").performClick()
    findOkButton().performClick()

    // *** UPDATED: Button is still disabled, must set future date ***
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    // Set a future date
    composeTestRule.runOnIdle { viewModel.setDate(futureDate) }

    // Button is now enabled
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled()
  }

  // --- NEW TESTS FOR TOUCH HANDLERS AND PAST TIME ERROR ---

  @Test
  fun dateInput_iconClick_triggersTouchDate() {
    assertFalse(viewModel.uiState.value.hasTouchedDate)
    // Click the date icon using its testTag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    composeTestRule.waitForIdle()
    // Check that the viewmodel state was updated
    assertTrue(viewModel.uiState.value.hasTouchedDate)
    // Close the dialog
    findCancelButton().performClick()
  }

  @Test
  fun timeInput_iconClick_triggersTouchTime() {
    assertFalse(viewModel.uiState.value.hasTouchedTime)
    // Click the time icon using its testTag
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    composeTestRule.waitForIdle()
    // Check that the viewmodel state was updated
    assertTrue(viewModel.uiState.value.hasTouchedTime)
    // Close the dialog
    findCancelButton().performClick()
  }

  @Test
  fun pastTimeError_appears_whenDateAndTimeTouchedAndInPast() {
    val errorText = "Meeting should be scheduled in the future."
    // Error does not exist initially
    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()

    // 1. Set a past date
    composeTestRule.runOnIdle { viewModel.setDate(pastDate) }

    // 2. "Touch" the date field (by clicking its icon)
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    findCancelButton().performClick() // Close dialog

    // Error still does not exist (time not touched)
    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()

    // 3. "Touch" the time field (by clicking its icon)
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    findCancelButton().performClick() // Close dialog

    // 4. NOW the error should appear
    composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
  }

  @Test
  fun pastTimeError_disappears_whenFutureDateIsSelected() {
    val errorText = "Meeting should be scheduled in the future."
    // 1. First, make the error appear
    composeTestRule.runOnIdle { viewModel.setDate(pastDate) }
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText(errorText).assertIsDisplayed()

    // 2. Now, fix the date by selecting a future one
    composeTestRule.runOnIdle { viewModel.setDate(futureDate) }

    // 3. The error should disappear
    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()
  }
}
