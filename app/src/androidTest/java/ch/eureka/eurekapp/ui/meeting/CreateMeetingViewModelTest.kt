/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
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
  private lateinit var repositoryMock: MockCreateMeetingRepository

  private var currentUserId: String? = "test-user-id"

  private val futureDateTime: LocalDateTime = LocalDateTime.now().plusDays(1)
  private val pastDateTime: LocalDateTime = LocalDateTime.now().minusDays(1)

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MockCreateMeetingRepository()
    viewModel =
        CreateMeetingViewModel(repository = repositoryMock, getCurrentUserId = { currentUserId })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun uiStateIsValidLogicIsCorrect() {
    var state =
        CreateMeetingUIState(
            title = "Valid Title",
            duration = 10,
            date = futureDateTime.toLocalDate(),
            time = futureDateTime.toLocalTime())
    assertTrue(state.isValid)

    state = state.copy(title = "")
    assertFalse(state.isValid)
    state = state.copy(title = " ")
    assertFalse(state.isValid)

    state = state.copy(title = "Valid Title", duration = 0)
    assertFalse(state.isValid)
    state = state.copy(duration = 4)
    assertFalse(state.isValid)

    state =
        state.copy(
            duration = 10, date = pastDateTime.toLocalDate(), time = pastDateTime.toLocalTime())
    assertFalse(state.isValid)

    state =
        state.copy(
            title = "",
            duration = 0,
            date = pastDateTime.toLocalDate(),
            time = pastDateTime.toLocalTime())
    assertFalse(state.isValid)
  }

  @Test
  fun initialStateIsCorrect() {
    val uiState = viewModel.uiState.value

    assertEquals("", uiState.title)
    assertEquals(LocalDate.now(), uiState.date)
    assertNotNull(uiState.time)
    assertEquals(0, uiState.duration)
    assertEquals(MeetingFormat.IN_PERSON, uiState.format) // <-- Verifies default format
    assertFalse(uiState.meetingSaved)
    assertFalse(uiState.hasTouchedTitle)
    assertFalse(uiState.hasTouchedDate)
    assertFalse(uiState.hasTouchedTime)
    assertNull(uiState.errorMsg)
    assertFalse(uiState.isValid)
  }

  @Test
  fun clearErrorMsgSetsErrorMsgToNull() {
    viewModel.setErrorMsg("An error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setErrorMsgUpdatesErrorMsg() {
    val errorMessage = "This is a test error"
    viewModel.setErrorMsg(errorMessage)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setTitleUpdatesTitle() {
    val newTitle = "My Meeting Title"
    viewModel.setTitle(newTitle)
    assertEquals(newTitle, viewModel.uiState.value.title)
  }

  @Test
  fun setDateUpdatesDate() {
    val newDate = LocalDate.of(2025, 10, 28)
    viewModel.setDate(newDate)
    assertEquals(newDate, viewModel.uiState.value.date)
  }

  @Test
  fun setTimeUpdatesTime() {
    val newTime = LocalTime.of(14, 30)
    viewModel.setTime(newTime)
    assertEquals(newTime, viewModel.uiState.value.time)
  }

  @Test
  fun setDurationUpdatesDuration() {
    val newDuration = 45
    viewModel.setDuration(newDuration)
    assertEquals(newDuration, viewModel.uiState.value.duration)
  }

  @Test
  fun setFormatUpdatesFormat() {
    val newFormat = MeetingFormat.VIRTUAL
    viewModel.setFormat(newFormat)
    assertEquals(newFormat, viewModel.uiState.value.format)
  }

  @Test
  fun setMeetingSavedUpdatesMeetingSaved() {
    viewModel.setMeetingSaved()
    assertTrue(viewModel.uiState.value.meetingSaved)
  }

  @Test
  fun touchTitleUpdatesHasTouchedTitle() {
    viewModel.touchTitle()
    assertTrue(viewModel.uiState.value.hasTouchedTitle)
  }

  @Test
  fun touchDateUpdatesHasTouchedDate() {
    viewModel.touchDate()
    assertTrue(viewModel.uiState.value.hasTouchedDate)
  }

  @Test
  fun touchTimeUpdatesHasTouchedTime() {
    viewModel.touchTime()
    assertTrue(viewModel.uiState.value.hasTouchedTime)
  }

  @Test
  fun createMeetingWhenStateIsInvalidSetsErrorAndReturns() {
    viewModel.setTitle("")
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting("project-123")

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingWhenTimeIsInPastSetsErrorAndReturns() {
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(pastDateTime.toLocalDate())
    viewModel.setTime(pastDateTime.toLocalTime())

    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createMeeting("project-123")

    assertEquals("At least one field is not set", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingWhenUserNotLoggedInSetsErrorAndReturns() {
    viewModel.setTitle("Valid Title")
    viewModel.setDuration(30)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    assertTrue(viewModel.uiState.value.isValid)

    currentUserId = null

    viewModel.createMeeting("project-123")

    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.meetingSaved)
    assertNull(repositoryMock.lastMeetingCreated)
  }

  @Test
  fun createMeetingWhenValidRepositorySuccessSetsMeetingSaved() = runTest {
    val title = "Successful Meeting"
    val date = futureDateTime.toLocalDate()
    val time = futureDateTime.toLocalTime()
    val duration = 60
    val projectId = "project-success"
    val userId = "test-user-id"
    val meetingFormat = MeetingFormat.IN_PERSON

    val expectedInstant = LocalDateTime.of(date, time).atZone(ZoneId.systemDefault()).toInstant()

    viewModel.setTitle(title)
    viewModel.setDate(date)
    viewModel.setTime(time)
    viewModel.setDuration(duration)
    viewModel.setFormat(meetingFormat) // <-- Set non-default format
    assertTrue(viewModel.uiState.value.isValid)

    assertEquals(userId, currentUserId)

    repositoryMock.shouldSucceed = true

    viewModel.createMeeting(projectId)

    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.meetingSaved)
    assertNull(viewModel.uiState.value.errorMsg)

    assertEquals(userId, repositoryMock.lastCreatorId)
    assertEquals(MeetingRole.HOST, repositoryMock.lastCreatorRole)

    val createdMeeting = repositoryMock.lastMeetingCreated
    assertNotNull(createdMeeting)
    assertEquals(projectId, createdMeeting!!.projectId)
    assertEquals(title, createdMeeting.title)
    assertEquals(duration, createdMeeting.duration)
    assertEquals(MeetingStatus.OPEN_TO_VOTES, createdMeeting.status)
    assertEquals(userId, createdMeeting.createdBy)
    assertNotNull(createdMeeting.meetingID)
    assertFalse(createdMeeting.meetingID.isBlank())

    assertEquals(1, createdMeeting.meetingProposals.size)
    val proposal = createdMeeting.meetingProposals[0]

    assertEquals(expectedInstant.epochSecond, proposal.dateTime.seconds)

    assertEquals(1, proposal.votes.size)
    val proposalVote = proposal.votes[0]

    assertEquals(userId, proposalVote.userId)
    assertEquals(listOf(meetingFormat), proposalVote.formatPreferences)
  }

  @Test
  fun createMeetingWhenValidRepositoryFailureSetsErrorMsg() = runTest {
    viewModel.setTitle("Failed Meeting")
    viewModel.setDuration(15)
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
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

  @Test
  fun createMeetingUiStateCopyWorksAsExpected() {
    val originalState =
        CreateMeetingUIState(title = "Original", duration = 10, hasTouchedDate = false)
    val copiedState = originalState.copy(title = "Copied", hasTouchedDate = true)

    assertEquals("Original", originalState.title)
    assertEquals(10, originalState.duration)
    assertFalse(originalState.hasTouchedDate)
    assertEquals("Copied", copiedState.title)
    assertEquals(10, copiedState.duration)
    assertTrue(copiedState.hasTouchedDate)
  }
}
