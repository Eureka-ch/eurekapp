/* Portions of this file were written with the help of Gemini and Grok.*/
package ch.eureka.eurekapp.ui.meeting

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.map.Location
import ch.eureka.eurekapp.utils.MockConnectivityObserver
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
  private lateinit var locationRepositoryMock: MockLocationRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  private var onDoneCalled = false
  private var onBackClickCalled = false
  private val testProjectId = "project-123"

  private val futureDate: LocalDate = LocalDate.now().plusDays(1)
  private val pastDate: LocalDate = LocalDate.now().minusDays(1)

  @Before
  fun setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    onDoneCalled = false
    onBackClickCalled = false

    repositoryMock = MockCreateMeetingRepository()
    locationRepositoryMock = MockLocationRepository()

    viewModel =
        CreateMeetingViewModel(
            repository = repositoryMock,
            locationRepository = locationRepositoryMock,
            getCurrentUserId = { "test-user-id" })

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer for all tests
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

    composeTestRule.setContent {
      CreateMeetingScreen(
          projectId = testProjectId,
          onDone = { onDoneCalled = true },
          onBackClick = { onBackClickCalled = true },
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
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Duration").assertTextEquals("Duration", "0 minutes")

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_FORMAT).assertIsDisplayed()
    composeTestRule.onNodeWithText("Format").assertTextEquals("Format", "In person")

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.ERROR_MSG).assertDoesNotExist()
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
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE).performClick()
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
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
      viewModel.setDate(futureDate)
      viewModel.setFormat(MeetingFormat.VIRTUAL)
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
      viewModel.setDuration(15)
      viewModel.setDate(futureDate)
      viewModel.setTime(LocalTime.of(9, 30))
      viewModel.setFormat(MeetingFormat.VIRTUAL)
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
    assertEquals(
        MeetingFormat.VIRTUAL,
        repositoryMock.lastMeetingCreated
            ?.meetingProposals
            ?.first()
            ?.votes
            ?.first()
            ?.formatPreferences
            ?.first())
  }

  @Test
  fun createMeeting_whenRepositoryFails_showsErrorAndClearsIt() {
    repositoryMock.shouldSucceed = false

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
        .performTextInput("My Failed Meeting")
    composeTestRule.runOnIdle {
      viewModel.setDuration(30)
      viewModel.setDate(futureDate)
      viewModel.setTime(LocalTime.of(10, 0))
      viewModel.setFormat(MeetingFormat.VIRTUAL)
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

  @Test
  fun durationInputField_opensDialog_andCancels() {
    val initialDuration = viewModel.uiState.value.duration
    assertEquals(0, initialDuration)
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DURATION).performClick()
    composeTestRule.onNodeWithText("Select a duration").assertIsDisplayed()
    findOkButton().assertIsDisplayed()
    composeTestRule.onNodeWithText("30 minutes").performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText("Select a duration").assertDoesNotExist()
    assertEquals(initialDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun durationInputField_opensDialogWithIcon_selectsOption_andConfirms() {
    val initialDuration = viewModel.uiState.value.duration
    assertEquals(0, initialDuration)
    composeTestRule.onNodeWithContentDescription("Select duration").performClick()
    composeTestRule.onNodeWithText("Select a duration").assertIsDisplayed()
    composeTestRule.onNodeWithText("45 minutes").performClick()
    findOkButton().performClick()
    composeTestRule.onNodeWithText("Select a duration").assertDoesNotExist()
    val newDuration = viewModel.uiState.value.duration
    assertNotEquals(initialDuration, newDuration)
    assertEquals(45, newDuration)
    composeTestRule.onNodeWithText("Duration").assertTextEquals("Duration", "45 minutes")
  }

  @Test
  fun formatInputField_opensDialog_andCancels() {
    val initialFormat = viewModel.uiState.value.format
    assertEquals(MeetingFormat.IN_PERSON, initialFormat)

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_FORMAT).performClick()

    composeTestRule.onNodeWithText("Select a format").assertIsDisplayed()
    findOkButton().assertIsDisplayed()

    composeTestRule.onNodeWithText("Virtual").performClick()

    findCancelButton().performClick()

    composeTestRule.onNodeWithText("Select a format").assertDoesNotExist()

    assertEquals(initialFormat, viewModel.uiState.value.format)
    composeTestRule.onNodeWithText("Format").assertTextEquals("Format", "In person")
  }

  @Test
  fun formatInputField_opensDialogWithIcon_selectsOption_andConfirms() {
    val initialFormat = viewModel.uiState.value.format
    assertEquals(MeetingFormat.IN_PERSON, initialFormat)

    composeTestRule.onNodeWithContentDescription("Select format").performClick()

    composeTestRule.onNodeWithText("Select a format").assertIsDisplayed()

    composeTestRule.onNodeWithText("Virtual").performClick()

    findOkButton().performClick()

    composeTestRule.onNodeWithText("Select a format").assertDoesNotExist()

    val newFormat = viewModel.uiState.value.format
    assertNotEquals(initialFormat, newFormat)
    assertEquals(MeetingFormat.VIRTUAL, newFormat)

    composeTestRule.onNodeWithText("Format").assertTextEquals("Format", "Virtual")
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

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsNotEnabled()

    composeTestRule.runOnIdle {
      viewModel.setDate(futureDate)
      viewModel.setFormat(MeetingFormat.VIRTUAL)
    }

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON)
        .assertIsEnabled()
  }

  @Test
  fun dateInput_iconClick_triggersTouchDate() {
    assertFalse(viewModel.uiState.value.hasTouchedDate)
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedDate)
    findCancelButton().performClick()
  }

  @Test
  fun timeInput_iconClick_triggersTouchTime() {
    assertFalse(viewModel.uiState.value.hasTouchedTime)
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedTime)
    findCancelButton().performClick()
  }

  @Test
  fun pastTimeError_appears_whenDateAndTimeTouchedAndInPast() {
    val errorText = "Meeting should be scheduled in the future."
    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()

    composeTestRule.runOnIdle { viewModel.setDate(pastDate) }

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    findCancelButton().performClick()

    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()

    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    findCancelButton().performClick()

    composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
  }

  @Test
  fun pastTimeError_disappears_whenFutureDateIsSelected() {
    val errorText = "Meeting should be scheduled in the future."
    composeTestRule.runOnIdle { viewModel.setDate(pastDate) }
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_DATE).performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_TIME).performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText(errorText).assertIsDisplayed()

    composeTestRule.runOnIdle { viewModel.setDate(futureDate) }

    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()
  }

  @Test
  fun locationInput_isDisplayed_onlyWhenFormatIsInPerson() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION)
        .assertIsDisplayed()

    composeTestRule.runOnIdle { viewModel.setFormat(MeetingFormat.VIRTUAL) }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION)
        .assertDoesNotExist()

    composeTestRule.runOnIdle { viewModel.setFormat(MeetingFormat.IN_PERSON) }
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION)
        .assertIsDisplayed()
  }

  @Test
  fun locationInput_showsSuggestions_andUpdatesOnSelect() {
    val expectedLocation = Location(46.5, 6.6, "EPFL Lausanne")
    locationRepositoryMock.searchResults = listOf(expectedLocation)

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION)
        .performTextInput("EPFL")

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CreateMeetingScreenTestTags.LOCATION_SUGGESTION)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule
        .onAllNodesWithTag(CreateMeetingScreenTestTags.LOCATION_SUGGESTION)
        .onFirst()
        .assertIsDisplayed()
        .assertTextEquals("EPFL Lausanne")

    composeTestRule
        .onAllNodesWithTag(CreateMeetingScreenTestTags.LOCATION_SUGGESTION)
        .onFirst()
        .performClick()

    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION)
        .assertTextContains("EPFL Lausanne")

    assertEquals(expectedLocation, viewModel.uiState.value.selectedLocation)
  }

  @Test
  fun locationInput_showsMoreOption_whenManySuggestions() {
    locationRepositoryMock.searchResults =
        listOf(
            Location(0.0, 0.0, "Loc1"),
            Location(0.0, 0.0, "Loc2"),
            Location(0.0, 0.0, "Loc3"),
            Location(0.0, 0.0, "Loc4"))

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION)
        .performTextInput("Loc")

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CreateMeetingScreenTestTags.LOCATION_SUGGESTION)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    composeTestRule.onNodeWithText("More...").assertIsDisplayed()

    val suggestionCount =
        composeTestRule
            .onAllNodesWithTag(CreateMeetingScreenTestTags.LOCATION_SUGGESTION)
            .fetchSemanticsNodes()
            .size

    assertEquals(3, suggestionCount)
  }

  @Test
  fun locationInput_mapIcon_isClickable() {
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.PICK_LOCATION)
        .assertIsDisplayed()
        .assertIsEnabled()
        .performClick()
  }

  @Test
  fun test_backButton_isDisplayed_andCallsOnBackClick() {
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.BACK_BUTTON)
        .assertIsDisplayed()
        .performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) { onBackClickCalled }

    assertTrue("onBackClick should be called", onBackClickCalled)
  }

  @Test
  fun createMeetingScreen_navigatesBackWhenConnectionLost() {
    // Verify we're on CreateMeetingScreen
    composeTestRule
        .onNodeWithTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_TITLE)
        .assertIsDisplayed()

    // Simulate connection loss
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitForIdle()

    // Verify onBackClick was called
    composeTestRule.waitUntil(timeoutMillis = 5000) { onBackClickCalled }

    assertTrue("onBackClick should be called", onBackClickCalled)
  }
}
