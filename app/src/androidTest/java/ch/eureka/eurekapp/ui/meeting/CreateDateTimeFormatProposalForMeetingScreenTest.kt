/* Portions of this file were written with the help of Gemini and Grok.*/
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
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.TimeZone
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
 * UI test suite for the [CreateDateTimeFormatProposalForMeetingScreen].
 *
 * Note: some tests where written with the help of Gemini.
 */
class CreateDateTimeFormatProposalForMeetingScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var viewModel: CreateDateTimeFormatProposalForMeetingViewModel
  private lateinit var repositoryMock: MockCreateMeetingProposalScreenRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  private var onDoneCalled = false
  private var onBackClickCalled = false
  private val testProjectId = "project-123"
  private val testMeetingId = "meeting-abc"
  private val testUserId = "test-user-id"

  private val futureDateTime: LocalDateTime = LocalDateTime.now().plusDays(1)
  private val pastDateTime: LocalDateTime = LocalDateTime.now().minusDays(1)

  private val baseProposal =
      MeetingProposal(
          Timestamp(Date.from(Instant.now().plusSeconds(1000))),
          listOf(MeetingProposalVote("user-a", listOf(MeetingFormat.IN_PERSON))))
  private val baseMeeting =
      Meeting(
          meetingID = testMeetingId,
          projectId = testProjectId,
          meetingProposals = listOf(baseProposal))

  @Before
  fun setup() {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    onDoneCalled = false
    onBackClickCalled = false

    repositoryMock = MockCreateMeetingProposalScreenRepository()

    repositoryMock.meetingToReturn = baseMeeting

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer for all tests
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

    viewModel =
        CreateDateTimeFormatProposalForMeetingViewModel(
            projectId = testProjectId,
            meetingId = testMeetingId,
            repository = repositoryMock,
            getCurrentUserId = { testUserId })

    composeTestRule.setContent {
      CreateDateTimeFormatProposalForMeetingScreen(
          projectId = testProjectId,
          meetingId = testMeetingId,
          onDone = { onDoneCalled = true },
          onBackClick = { onBackClickCalled = true },
          createMeetingProposalViewModel = viewModel)
    }

    composeTestRule.waitForIdle()
  }

  private fun findOkButton() = composeTestRule.onNodeWithText("OK")

  private fun findCancelButton() = composeTestRule.onNodeWithText("Cancel")

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_screenLoadsDisplaysStaticContentAndButtonIsDisabled() {
    composeTestRule
        .onNodeWithTag(
            CreateDateTimeFormatMeetingProposalScreenTestTags.CREATE_MEETING_PROPOSAL_SCREEN_TITLE)
        .assertIsDisplayed()
        .assertTextEquals("Create Meeting Proposal")

    composeTestRule
        .onNodeWithTag(
            CreateDateTimeFormatMeetingProposalScreenTestTags
                .CREATE_MEETING_PROPOSAL_SCREEN_DESCRIPTION)
        .assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_FORMAT)
        .assertIsDisplayed()

    composeTestRule.onNodeWithText("Format").assertTextEquals("Format", "In person")

    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.ERROR_MSG)
        .assertDoesNotExist()
    composeTestRule
        .onNodeWithText("Meeting should be scheduled in the future.")
        .assertDoesNotExist()

    composeTestRule
        .onNodeWithTag(
            CreateDateTimeFormatMeetingProposalScreenTestTags.CREATE_MEETING_PROPOSAL_BUTTON)
        .assertIsDisplayed()
        .assertIsNotEnabled()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_saveButtonIsEnabledWhenStateIsValid() {
    val buttonTag = CreateDateTimeFormatMeetingProposalScreenTestTags.CREATE_MEETING_PROPOSAL_BUTTON

    composeTestRule.onNodeWithTag(buttonTag).assertIsNotEnabled()

    composeTestRule.runOnIdle {
      viewModel.setDate(futureDateTime.toLocalDate())
      viewModel.setTime(futureDateTime.toLocalTime())
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(buttonTag).assertIsEnabled()

    composeTestRule.runOnIdle {
      viewModel.setDate(pastDateTime.toLocalDate())
      viewModel.setTime(pastDateTime.toLocalTime())
    }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag(buttonTag).assertIsNotEnabled()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_createProposalWhenSuccessCallsOnDone() {
    repositoryMock.shouldUpdateSucceed = true

    composeTestRule.runOnIdle {
      viewModel.setDate(futureDateTime.toLocalDate())
      viewModel.setTime(futureDateTime.toLocalTime())
    }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(
            CreateDateTimeFormatMeetingProposalScreenTestTags.CREATE_MEETING_PROPOSAL_BUTTON)
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitForIdle()

    assertTrue("onDone callback was not invoked on success", onDoneCalled)
    assertNotNull(repositoryMock.lastUpdatedMeeting)
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_createProposalWhenRepositoryFailsShowsErrorAndClearsIt() {
    repositoryMock.shouldUpdateSucceed = false

    composeTestRule.runOnIdle {
      viewModel.setDate(futureDateTime.toLocalDate())
      viewModel.setTime(futureDateTime.toLocalTime())
    }
    composeTestRule.waitForIdle()

    composeTestRule
        .onNodeWithTag(
            CreateDateTimeFormatMeetingProposalScreenTestTags.CREATE_MEETING_PROPOSAL_BUTTON)
        .assertIsEnabled()
        .performClick()

    composeTestRule.waitForIdle()

    assertFalse("onDone callback was invoked on failure", onDoneCalled)
    assertNull("errorMsg was not cleared by the LaunchedEffect", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_dateInputFieldOpensDialogAndCancels() {
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE)
        .performClick()
    composeTestRule.waitForIdle()
    findOkButton().assertIsDisplayed()
    findCancelButton().performClick()
    findOkButton().assertDoesNotExist()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_dateInputFieldOpensDialogWithIconAndConfirms() {
    val initialDate = viewModel.uiState.value.date
    composeTestRule.onNodeWithContentDescription("Select date").performClick()
    findOkButton().assertIsDisplayed()
    findOkButton().performClick()
    findOkButton().assertDoesNotExist()
    assertEquals(initialDate, viewModel.uiState.value.date)
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_timeInputFieldOpensDialogAndCancels() {
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME)
        .performClick()
    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_timeInputFieldOpensDialogWithIconAndConfirms() {
    val initialTimeTruncated = viewModel.uiState.value.time.truncatedTo(ChronoUnit.MINUTES)
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME)
        .performClick()
    composeTestRule.onNodeWithText("Select time").assertIsDisplayed()
    findOkButton().performClick()
    composeTestRule.onNodeWithText("Select time").assertDoesNotExist()
    val actualTimeTruncated = viewModel.uiState.value.time.truncatedTo(ChronoUnit.MINUTES)
    assertEquals(initialTimeTruncated, actualTimeTruncated)
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_formatInputFieldOpensDialogAndCancels() {
    val initialFormat = viewModel.uiState.value.format
    assertEquals(MeetingFormat.IN_PERSON, initialFormat)

    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_FORMAT)
        .performClick()

    composeTestRule.onNodeWithText("Select a format").assertIsDisplayed()
    findOkButton().assertIsDisplayed()

    composeTestRule.onNodeWithText("Virtual").performClick()

    findCancelButton().performClick()

    composeTestRule.onNodeWithText("Select a format").assertDoesNotExist()

    assertEquals(initialFormat, viewModel.uiState.value.format)
    composeTestRule.onNodeWithText("Format").assertTextEquals("Format", "In person")
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_formatInputFieldOpensDialogWithIconSelectsOptionAndConfirms() {
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
  fun createDateTimeFormatProposalForMeetingScreen_dateInputIconClickTriggersTouchDate() {
    assertFalse(viewModel.uiState.value.hasTouchedDate)
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE)
        .performClick()
    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedDate)
    findCancelButton().performClick()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_timeInputIconClickTriggersTouchTime() {
    assertFalse(viewModel.uiState.value.hasTouchedTime)
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME)
        .performClick()
    composeTestRule.waitForIdle()
    assertTrue(viewModel.uiState.value.hasTouchedTime)
    findCancelButton().performClick()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_pastTimeErrorAppearsWhenDateAndTimeTouchedAndInPast() {
    val errorText = "Meeting should be scheduled in the future."
    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()

    composeTestRule.runOnIdle { viewModel.setDate(pastDateTime.toLocalDate()) }

    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE)
        .performClick()
    findCancelButton().performClick()

    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()

    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME)
        .performClick()
    findCancelButton().performClick()

    composeTestRule.onNodeWithText(errorText).assertIsDisplayed()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_pastTimeErrorDisappearsWhenFutureDateIsSelected() {
    val errorText = "Meeting should be scheduled in the future."
    composeTestRule.runOnIdle { viewModel.setDate(pastDateTime.toLocalDate()) }
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE)
        .performClick()
    findCancelButton().performClick()
    composeTestRule
        .onNodeWithTag(CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME)
        .performClick()
    findCancelButton().performClick()
    composeTestRule.onNodeWithText(errorText).assertIsDisplayed()

    composeTestRule.runOnIdle { viewModel.setDate(futureDateTime.toLocalDate()) }
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText(errorText).assertDoesNotExist()
  }

  @Test
  fun createDateTimeFormatProposalForMeetingScreen_navigatesBackWhenConnectionLost() {
    // Verify we're on CreateDateTimeFormatProposalForMeetingScreen
    composeTestRule
        .onNodeWithTag(
            CreateDateTimeFormatMeetingProposalScreenTestTags.CREATE_MEETING_PROPOSAL_SCREEN_TITLE)
        .assertIsDisplayed()

    // Simulate connection loss
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitForIdle()

    // Verify onBackClick was called
    composeTestRule.waitUntil(timeoutMillis = 5000) { onBackClickCalled }

    assertTrue("onBackClick should be called", onBackClickCalled)
  }
}

/** Mock implementation of [MeetingRepository] for testing. */
private class MockCreateMeetingProposalScreenRepository : MeetingRepository {

  var meetingToReturn: Meeting? = null
  var getMeetingException: Exception? = null
  var shouldUpdateSucceed = true
  var updateException: Exception = Exception("Update failed")
  var lastUpdatedMeeting: Meeting? = null

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    return flow {
      getMeetingException?.let { throw it }
      emit(meetingToReturn)
    }
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    lastUpdatedMeeting = meeting
    return if (shouldUpdateSucceed) {
      Result.success(Unit)
    } else {
      Result.failure(updateException)
    }
  }

  // --- Unused methods by this ViewModel ---

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
    return flowOf(emptyList())
  }

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
    return flowOf(emptyList())
  }

  override fun getMeetingsForCurrentUser(skipCache: Boolean): Flow<List<Meeting>> {
    return flowOf(emptyList())
  }

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> {
    return Result.success("new-meeting-id")
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
    return Result.success(Unit)
  }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
    return flowOf(emptyList())
  }

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> {
    return Result.success(Unit)
  }

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> {
    return Result.success(Unit)
  }

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> {
    return Result.success(Unit)
  }
}
