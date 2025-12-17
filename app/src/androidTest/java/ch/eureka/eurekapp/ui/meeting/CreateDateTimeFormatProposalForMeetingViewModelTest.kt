/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
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
 * Test suite for CreateDateTimeFormatProposalForMeetingViewModel.
 *
 * Note : some tests were written with Gemini.
 */
@ExperimentalCoroutinesApi
class CreateDateTimeFormatProposalForMeetingViewModelTest {

  private lateinit var initialDate: LocalDate
  private lateinit var initialTime: LocalTime

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: CreateDateTimeFormatProposalForMeetingViewModel
  private lateinit var repositoryMock: MockCreateMeetingProposalRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  private val testProjectId = "project-123"
  private val testMeetingId = "meeting-abc"
  private var currentUserId: String? = "test-user-id"

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
    Dispatchers.setMain(testDispatcher)

    val context = InstrumentationRegistry.getInstrumentation().targetContext

    // Initialize mock connectivity observer
    mockConnectivityObserver = MockConnectivityObserver(context)
    mockConnectivityObserver.setConnected(true)

    // Replace ConnectivityObserverProvider's observer with mock
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)

    repositoryMock = MockCreateMeetingProposalRepository()
    viewModel =
        CreateDateTimeFormatProposalForMeetingViewModel(
            projectId = testProjectId,
            meetingId = testMeetingId,
            repository = repositoryMock,
            getCurrentUserId = { currentUserId })

    val initialState = viewModel.uiState.value
    initialDate = initialState.date
    initialTime = initialState.time
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun uiStateIsValidLogicIsCorrect() {
    var state =
        CreateDateTimeFormatProposalForMeetingUIState(
            date = futureDateTime.toLocalDate(), time = futureDateTime.toLocalTime())
    assertTrue(state.isValid)

    state = state.copy(date = pastDateTime.toLocalDate(), time = pastDateTime.toLocalTime())
    assertFalse(state.isValid)

    state = state.copy(date = LocalDate.now(), time = LocalTime.now())
    assertFalse(state.isValid)
  }

  @Test
  fun initialStateIsCorrect() {
    val uiState = viewModel.uiState.value

    assertEquals(initialDate, uiState.date)
    assertEquals(initialTime, uiState.time)

    assertEquals(MeetingFormat.IN_PERSON, uiState.format)
    assertFalse(uiState.saved)
    assertFalse(uiState.hasTouchedDate)
    assertFalse(uiState.hasTouchedTime)
    assertNull(uiState.errorMsg)
    assertFalse(uiState.isLoading)

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
  fun setFormatUpdatesFormat() {
    val newFormat = MeetingFormat.VIRTUAL
    viewModel.setFormat(newFormat)
    assertEquals(newFormat, viewModel.uiState.value.format)
  }

  @Test
  fun setSavedUpdatesSaved() {
    viewModel.setSaved()
    assertTrue(viewModel.uiState.value.saved)
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
  fun createDateTimeFormatProposalWhenStateIsInvalid() {
    viewModel.setDate(pastDateTime.toLocalDate())
    viewModel.setTime(pastDateTime.toLocalTime())
    assertFalse(viewModel.uiState.value.isValid)

    viewModel.createDateTimeFormatProposalForMeeting()

    assertEquals("Meeting should be scheduled in the future.", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.saved)
    assertNull(repositoryMock.lastUpdatedMeeting)
  }

  @Test
  fun createDateTimeFormatProposalWhenUserNotLoggedIn() {
    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    assertTrue(viewModel.uiState.value.isValid)

    currentUserId = null

    viewModel.createDateTimeFormatProposalForMeeting()

    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
    assertFalse(viewModel.uiState.value.saved)
    assertNull(repositoryMock.lastUpdatedMeeting)
  }

  @Test
  fun createDateTimeFormatProposalSuccess() = runTest {
    repositoryMock.meetingToReturn = baseMeeting
    viewModel.loadMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    val newDate = futureDateTime.toLocalDate()
    val newTime = futureDateTime.toLocalTime()
    val newFormat = MeetingFormat.VIRTUAL
    viewModel.setDate(newDate)
    viewModel.setTime(newTime)
    viewModel.setFormat(newFormat)
    assertTrue(viewModel.uiState.value.isValid)

    repositoryMock.shouldUpdateSucceed = true

    viewModel.createDateTimeFormatProposalForMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.saved)
    assertNull(viewModel.uiState.value.errorMsg)

    val updatedMeeting = repositoryMock.lastUpdatedMeeting
    assertNotNull(updatedMeeting)
    assertEquals(2, updatedMeeting!!.meetingProposals.size)

    val newProposal = updatedMeeting.meetingProposals.last()
    val expectedInstant =
        LocalDateTime.of(newDate, newTime).atZone(ZoneId.systemDefault()).toInstant()

    assertEquals(expectedInstant.epochSecond, newProposal.dateTime.seconds)
    assertEquals(1, newProposal.votes.size)
    assertEquals(currentUserId, newProposal.votes[0].userId)
    assertEquals(listOf(newFormat), newProposal.votes[0].formatPreferences)
  }

  @Test
  fun createDateTimeFormatProposalRepositoryFailure() = runTest {
    repositoryMock.meetingToReturn = baseMeeting
    viewModel.loadMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.setDate(futureDateTime.toLocalDate())
    viewModel.setTime(futureDateTime.toLocalTime())
    assertTrue(viewModel.uiState.value.isValid)

    repositoryMock.shouldUpdateSucceed = false

    viewModel.createDateTimeFormatProposalForMeeting()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.saved)
    assertEquals(
        "Datetime/format meeting proposal could not be created.", viewModel.uiState.value.errorMsg)
    assertNotNull(repositoryMock.lastUpdatedMeeting)
  }

  @Test
  fun uiStateCopyWorks() {
    val originalState =
        CreateDateTimeFormatProposalForMeetingUIState(saved = false, hasTouchedDate = false)
    val copiedState = originalState.copy(saved = true, hasTouchedDate = true)

    assertFalse(originalState.saved)
    assertFalse(originalState.hasTouchedDate)
    assertTrue(copiedState.saved)
    assertTrue(copiedState.hasTouchedDate)
  }
}

/** Mock implementation of [MeetingRepository] for testing. */
private class MockCreateMeetingProposalRepository : MeetingRepository {

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

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
    return flowOf(emptyList())
  }

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
    return flowOf(emptyList())
  }

  override fun getMeetingsForCurrentUser(
      projectId: String,
      skipCache: Boolean
  ): Flow<List<Meeting>> {
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
