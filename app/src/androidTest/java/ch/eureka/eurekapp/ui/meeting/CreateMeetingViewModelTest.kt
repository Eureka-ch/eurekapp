package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for CreateMeetingViewModel. This suite aims for 100% test coverage of the ViewModel
 * and its UIState.
 *
 * Note : some tests were generated with Gemini
 */
@ExperimentalCoroutinesApi
class CreateMeetingViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: CreateMeetingViewModel
  private lateinit var repositoryMock:
      MockCreateMeetingRepository // Assuming this is defined elsewhere

  private var currentUserId: String? = "test-user-id"

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MockCreateMeetingRepository()
    viewModel =
        CreateMeetingViewModel(repository = repositoryMock, getCurrentUserId = { currentUserId })
  }

  @After
  fun tearDown() {
    // Reset the main dispatcher
    Dispatchers.resetMain()
  }

  @Test
  fun uiState_isValid_logicIsCorrect() {
    var state = CreateMeetingUIState()
    assertFalse("Default state (empty title, 0 duration) should be invalid", state.isValid)

    // Test title validation
    state = state.copy(duration = 30)
    assertFalse("State with 0 duration but no title should be invalid", state.isValid)

    state = state.copy(title = " ", duration = 30)
    assertFalse("State with blank title should be invalid", state.isValid)

    // Test duration validation
    state = state.copy(title = "Valid Title", duration = 0)
    assertFalse("State with valid title but 0 duration should be invalid", state.isValid)

    state = state.copy(duration = 4)
    assertFalse("State with duration < 5 should be invalid", state.isValid)

    state = state.copy(duration = 5)
    assertTrue("State with duration = 5 should be valid", state.isValid)

    state = state.copy(duration = 30)
    assertTrue("State with duration > 5 should be valid", state.isValid)
  }

  @Test
  fun initialState_isCorrect() {
    val uiState = viewModel.uiState.value

    assertEquals("", uiState.title)
    assertEquals(LocalDate.now(), uiState.date)
    assertNotNull(uiState.time)
    assertEquals(0, uiState.duration) // Check default duration
    assertFalse(uiState.meetingSaved)
    assertFalse(uiState.hasTouchedTitle)
    assertNull(uiState.errorMsg)
    assertFalse("Initial state should be invalid", uiState.isValid)
  }

  @Test
  fun clearErrorMsg_setsErrorMsgToNull() {
    viewModel.setErrorMsg("An error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setErrorMsg_updatesErrorMsg() {
    val errorMessage = "This is a test error"
    viewModel.setErrorMsg(errorMessage)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setTitle_updatesTitle() {
    val newTitle = "My Meeting Title"
    viewModel.setTitle(newTitle)
    assertEquals(newTitle, viewModel.uiState.value.title)
  }

  @Test
  fun setDate_updatesDate() {
    val newDate = LocalDate.of(2025, 10, 28)
    viewModel.setDate(newDate)
    assertEquals(newDate, viewModel.uiState.value.date)
  }

  @Test
  fun setTime_updatesTime() { // Renamed from setStartTime_updatesStartTime
    val newTime = LocalTime.of(14, 30)
    viewModel.setTime(newTime)
    assertEquals(newTime, viewModel.uiState.value.time)
  }

  @Test
  fun setDuration_updatesDuration() { // New test
    val newDuration = 45
    viewModel.setDuration(newDuration)
    assertEquals(newDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun setMeetingSaved_updatesMeetingSaved() {
    viewModel.setMeetingSaved()
    assertTrue(viewModel.uiState.value.meetingSaved)
  }

  @Test
  fun touchTitle_updatesHasTouchedTitle() {
    viewModel.touchTitle()
    assertTrue(viewModel.uiState.value.hasTouchedTitle)
  }

  @Test
  fun createMeeting_whenStateIsInvalid_setsErrorAndReturns() {
    viewModel.setTitle("") // Ensure state is invalid
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting("project-123")

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated) // Repository should not be called
  }

  @Test
  fun createMeeting_whenUserNotLoggedIn_setsErrorAndReturns() {
    // Setup valid state first
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    assertTrue(viewModel.uiState.value.isValid)

    currentUserId = null // Log out user

    viewModel.createMeeting("project-123")

    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated) // Repository should not be called
  }

  @Test
  fun createMeeting_whenValid_repositorySuccess_setsMeetingSaved() = runTest {
    val title = "Successful Meeting"
    val date = LocalDate.of(2025, 12, 25)
    val time = LocalTime.of(10, 0)
    val duration = 60
    val projectId = "project-success"
    val userId = "test-user-id"

    // Helper to calculate the expected Timestamp
    val expectedInstant = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()

    viewModel.setTitle(title)
    viewModel.setDate(date)
    viewModel.setTime(time)
    viewModel.setDuration(duration)
    assertTrue(viewModel.uiState.value.isValid)

    assertEquals(userId, currentUserId)

    repositoryMock.shouldSucceed = true

    viewModel.createMeeting(projectId)

    testDispatcher.scheduler.advanceUntilIdle()

    // --- Assert UI State ---
    assertTrue(viewModel.uiState.value.meetingSaved)
    assertNull(viewModel.uiState.value.errorMsg)

    // --- Assert Repository Interaction ---
    assertEquals(userId, repositoryMock.lastCreatorId)
    assertEquals(MeetingRole.HOST, repositoryMock.lastCreatorRole)

    // --- Assert Meeting Object Content ---
    val createdMeeting = repositoryMock.lastMeetingCreated
    assertNotNull(createdMeeting)
    assertEquals(projectId, createdMeeting!!.projectId)
    assertEquals(title, createdMeeting.title)
    assertEquals(duration, createdMeeting.duration)
    assertEquals(MeetingStatus.OPEN_TO_VOTES, createdMeeting.status)
    assertEquals(userId, createdMeeting.createdBy)
    assertNotNull(createdMeeting.meetingID) // Check that an ID was generated
    assertFalse(createdMeeting.meetingID.isBlank())

    // --- Assert DateTimeVote Content ---
    assertEquals(1, createdMeeting.dateTimeVotes.size)
    val vote = createdMeeting.dateTimeVotes[0]
    assertEquals(1, vote.voters.size)
    assertEquals(listOf(userId), vote.voters)
    assertEquals(expectedInstant.epochSecond, vote.dateTime.seconds)
  }

  @Test
  fun createMeeting_whenValid_repositoryFailure_setsErrorMsg() = runTest {
    viewModel.setTitle("Failed Meeting")
    viewModel.setDuration(15) // Use new duration setter
    assertTrue(viewModel.uiState.value.isValid)

    assertEquals("test-user-id", currentUserId)

    repositoryMock.shouldSucceed = false
    repositoryMock.failureException = Exception("Database is down")

    viewModel.createMeeting("project-fail")

    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.meetingSaved)
    assertEquals("Meeting could not be created.", viewModel.uiState.value.errorMsg)

    // Check that the repository was still called
    assertNotNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingUiState_copy_worksAsExpected() {
    val originalState = CreateMeetingUIState(title = "Original", duration = 10)
    val copiedState = originalState.copy(title = "Copied")

    assertEquals("Original", originalState.title)
    assertEquals(10, originalState.duration)
    assertEquals("Copied", copiedState.title)
    assertEquals(10, copiedState.duration)
  }
}
