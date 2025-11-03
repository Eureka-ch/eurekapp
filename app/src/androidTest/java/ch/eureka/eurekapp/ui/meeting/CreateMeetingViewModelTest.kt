package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import java.time.LocalDate
import java.time.LocalTime
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
  private lateinit var repositoryMock: MockCreateMeetingRepository

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
    val time = LocalTime.of(10, 0)

    var state = CreateMeetingUIState()
    assertFalse("Default state should be invalid", state.isValid)

    state = state.copy(title = "Valid Title", startTime = time, endTime = time)
    assertFalse("State with equal times should be invalid", state.isValid)

    state = state.copy(startTime = time.plusHours(1), endTime = time)
    assertFalse("State with start after end should be invalid", state.isValid)

    state = state.copy(startTime = time, endTime = time.plusHours(1))
    assertTrue("State with title and valid times should be valid", state.isValid)

    state = state.copy(title = " ", startTime = time, endTime = time.plusHours(1))
    assertFalse("State with blank title should be invalid", state.isValid)
  }

  @Test
  fun initialState_isCorrect() {
    val uiState = viewModel.uiState.value

    assertEquals("", uiState.title)
    assertEquals(LocalDate.now(), uiState.date)
    assertNotNull(uiState.startTime)
    assertNotNull(uiState.endTime)
    assertFalse(uiState.meetingSaved)
    assertFalse(uiState.hasTouchedTitle)
    assertFalse(uiState.hasTouchedStartTime)
    assertFalse(uiState.hasTouchedEndTime)
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
  fun setStartTime_updatesStartTime() {
    val newTime = LocalTime.of(14, 30)
    viewModel.setStartTime(newTime)
    assertEquals(newTime, viewModel.uiState.value.startTime)
  }

  @Test
  fun setEndTime_updatesEndTime() {
    val newTime = LocalTime.of(15, 30)
    viewModel.setEndTime(newTime)
    assertEquals(newTime, viewModel.uiState.value.endTime)
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
  fun touchStartTime_updatesHasTouchedStartTime() {
    viewModel.touchStartTime()
    assertTrue(viewModel.uiState.value.hasTouchedStartTime)
  }

  @Test
  fun touchEndTime_updatesHasTouchedEndTime() {
    viewModel.touchEndTime()
    assertTrue(viewModel.uiState.value.hasTouchedEndTime)
  }

  @Test
  fun createMeeting_whenStateIsInvalid_setsErrorAndReturns() {
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting("project-123")

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated) // Repository should not be called
  }

  @Test
  fun createMeeting_whenUserNotLoggedIn_setsErrorAndReturns() {
    viewModel.setTitle("Valid Title")
    viewModel.setStartTime(LocalTime.of(10, 0))
    viewModel.setEndTime(LocalTime.of(11, 0))
    assertTrue(viewModel.uiState.value.isValid)

    currentUserId = null

    viewModel.createMeeting("project-123")

    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated) // Repository should not be called
  }

  @Test
  fun createMeeting_whenValid_repositorySuccess_setsMeetingSaved() = runTest {
    val title = "Successful Meeting"
    val date = LocalDate.of(2025, 12, 25)
    val startTime = LocalTime.of(10, 0)
    val endTime = LocalTime.of(11, 0)
    val projectId = "project-success"

    viewModel.setTitle(title)
    viewModel.setDate(date)
    viewModel.setStartTime(startTime)
    viewModel.setEndTime(endTime)
    assertTrue(viewModel.uiState.value.isValid)

    assertEquals("test-user-id", currentUserId)

    repositoryMock.shouldSucceed = true

    viewModel.createMeeting(projectId)

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.meetingSaved)
    assertNull(viewModel.uiState.value.errorMsg)

    assertNotNull(repositoryMock.lastMeetingCreated)
    assertEquals(title, repositoryMock.lastMeetingCreated?.title)
    assertEquals(projectId, repositoryMock.lastMeetingCreated?.projectId)
    assertEquals(currentUserId, repositoryMock.lastCreatorId)
    assertEquals(MeetingRole.HOST, repositoryMock.lastCreatorRole)
    // assertNotNull(repositoryMock.lastMeetingCreated?.timeSlot) TODO : fix
  }

  @Test
  fun createMeeting_whenValid_repositoryFailure_setsErrorMsg() = runTest {
    viewModel.setTitle("Failed Meeting")
    viewModel.setStartTime(LocalTime.of(9, 0))
    viewModel.setEndTime(LocalTime.of(10, 0))
    assertTrue(viewModel.uiState.value.isValid)

    assertEquals("test-user-id", currentUserId)

    repositoryMock.shouldSucceed = false
    repositoryMock.failureException = Exception("Database is down")

    viewModel.createMeeting("project-fail")

    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.meetingSaved)
    assertEquals("Meeting could not be created.", viewModel.uiState.value.errorMsg)

    assertNotNull(repositoryMock.lastMeetingCreated)
  }
}
