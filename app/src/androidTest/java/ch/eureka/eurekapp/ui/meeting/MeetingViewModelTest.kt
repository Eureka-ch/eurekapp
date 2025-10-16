package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.*
import com.google.firebase.Timestamp
import java.util.*
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
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test suite for MeetingViewModel
 *
 * Note : some tests where generated with Gemini and chatGPT
 */
@ExperimentalCoroutinesApi
class MeetingViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingViewModel
  private lateinit var repositoryMock: MeetingRepositoryMockViewmodel

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MeetingRepositoryMockViewmodel()
    viewModel = MeetingViewModel(repositoryMock)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateIsCorrect() {
    val uiState = viewModel.uiState.value
    assertTrue(uiState.upcomingMeetings.isEmpty())
    assertTrue(uiState.pastMeetings.isEmpty())
    assertFalse(uiState.isLoading)
    assertNull(uiState.errorMsg)
    assertEquals(MeetingTab.UPCOMING, uiState.selectedTab)
  }

  @Test
  fun loadMeetingsCorrectlyFiltersAndSortsMeetings() = runTest {
    val now = Timestamp(Date())
    val older = Timestamp(Date(now.toDate().time - 1000000))
    val newer = Timestamp(Date(now.toDate().time + 1000000))

    val meeting1 =
        Meeting(
            meetingID = "1",
            title = "Future Planning",
            status = MeetingStatus.SCHEDULED,
            datetime = newer)
    val meeting2 =
        Meeting(
            meetingID = "2", title = "Ongoing", status = MeetingStatus.IN_PROGRESS, datetime = now)
    val meeting3 =
        Meeting(
            meetingID = "3",
            title = "Completed",
            status = MeetingStatus.COMPLETED,
            datetime = older)

    repositoryMock.meetingsToReturn = listOf(meeting1, meeting2, meeting3)

    viewModel.loadMeetings("project123")
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value

    // Check loading turned off
    assertFalse(uiState.isLoading)

    // Filtering
    assertEquals(listOf(meeting1, meeting2), uiState.upcomingMeetings)
    assertEquals(listOf(meeting3), uiState.pastMeetings)

    // Check sorting is reversed (newest first)
    val upcomingIds = uiState.upcomingMeetings.map { it.meetingID }
    val pastIds = uiState.pastMeetings.map { it.meetingID }

    assertEquals(listOf("1", "2"), upcomingIds)
    assertEquals(listOf("3"), pastIds)
  }

  @Test
  fun loadMeetingsSortsByTimeSlotWhenDatetimeIsNull() = runTest {
    val baseTime = Date().time

    // One meeting with datetime (newer)
    val meetingWithDatetime =
        Meeting(
            meetingID = "1",
            title = "With Datetime",
            status = MeetingStatus.SCHEDULED,
            datetime = Timestamp(Date(baseTime + 1000000)))

    // One meeting with only timeSlot (older)
    val meetingWithTimeSlot =
        Meeting(
            meetingID = "2",
            title = "With TimeSlot",
            status = MeetingStatus.SCHEDULED,
            datetime = null,
            timeSlot =
                TimeSlot(
                    startTime = Timestamp(Date(baseTime - 1000000)),
                    endTime = Timestamp(Date(baseTime))))

    repositoryMock.meetingsToReturn = listOf(meetingWithDatetime, meetingWithTimeSlot)

    viewModel.loadMeetings("projectABC")
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value

    // Should sort by datetime (or timeSlot.startTime) DESCENDING
    val upcomingIds = uiState.upcomingMeetings.map { it.meetingID }

    // "1" should come before "2" (newer datetime first)
    assertEquals(listOf("1", "2"), upcomingIds)
    assertTrue(uiState.pastMeetings.isEmpty())
  }

  @Test
  fun loadMeetingsHandlesLoadingStateCorrectly() = runTest {
    repositoryMock.meetingsToReturn = listOf(Meeting(title = "Planning"))
    viewModel.loadMeetings("any_project_id")

    // Execute coroutine
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun loadMeetingsHandlesEmptyListFromRepository() = runTest {
    repositoryMock.meetingsToReturn = emptyList()

    viewModel.loadMeetings("any_project_id")
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertTrue(uiState.upcomingMeetings.isEmpty())
    assertTrue(uiState.pastMeetings.isEmpty())
  }

  @Test
  fun loadMeetingsHandlesRepositoryError() = runTest {
    val errorMessage = "Network Error"
    repositoryMock.shouldThrowError = true
    repositoryMock.errorMessage = errorMessage

    viewModel.loadMeetings("any_project_id")
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun selectTabUpdatesTheSelectedTabInUIState() {
    viewModel.selectTab(MeetingTab.PAST)
    assertEquals(MeetingTab.PAST, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(MeetingTab.UPCOMING)
    assertEquals(MeetingTab.UPCOMING, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun clearErrorMsgSetsErrorMsgToNull() = runTest {
    repositoryMock.shouldThrowError = true
    viewModel.loadMeetings("any_project_id")
    testDispatcher.scheduler.advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }
}

/** Mock repository to control meeting data for ViewModel tests. */
class MeetingRepositoryMockViewmodel : MeetingRepository {
  var meetingsToReturn: List<Meeting> = emptyList()
  var shouldThrowError = false
  var errorMessage = "An error occurred"

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
    return if (shouldThrowError) {
      flow { throw Exception(errorMessage) }
    } else {
      flowOf(meetingsToReturn)
    }
  }

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> = flowOf(null)

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> =
      flowOf(emptyList())

  override fun getMeetingsForCurrentUser(
      projectId: String,
      skipCache: Boolean
  ): Flow<List<Meeting>> = flowOf(emptyList())

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> = runCatching { "url" }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = runCatching {}

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> =
      runCatching {}

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> =
      flow {
        emit(emptyList())
      }

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = runCatching {}

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = runCatching {}

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = runCatching {}
}
