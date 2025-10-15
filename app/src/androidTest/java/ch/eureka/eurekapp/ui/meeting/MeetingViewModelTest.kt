package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
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
 * Test suite for MeetingViewModel
 *
 * Note : some tests where generated with Gemini
 */
@ExperimentalCoroutinesApi
class MeetingViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingViewModel
  private lateinit var repositoryMock: MeetingRepositoryMockViewmodel

  // This rule sets the main coroutine dispatcher for unit testing.
  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MeetingRepositoryMockViewmodel()
    viewModel = MeetingViewModel(repositoryMock)
  }

  // This rule cleans up the dispatcher after tests are finished.
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
  fun loadMeetingsCorrectlyFiltersUpcomingAndPastMeetings() = runTest {
    // Given a mock repository that returns sample meetings
    val allMeetings = MeetingProvider.sampleMeetings
    repositoryMock.meetingsToReturn = allMeetings

    // When loadMeetings is called
    viewModel.loadMeetings("any_project_id")
    testDispatcher.scheduler.advanceUntilIdle() // Execute the coroutine

    // Then the UI state should be updated with filtered lists
    val uiState = viewModel.uiState.value
    val expectedUpcoming = allMeetings.filter { !it.ended }
    val expectedPast = allMeetings.filter { it.ended }

    assertFalse(uiState.isLoading)
    assertEquals(expectedUpcoming.size, uiState.upcomingMeetings.size)
    assertEquals(expectedPast.size, uiState.pastMeetings.size)
    assertEquals(expectedUpcoming, uiState.upcomingMeetings)
    assertEquals(expectedPast, uiState.pastMeetings)
  }

  @Test
  fun loadMeetingsHandlesLoadingStateCorrectly() = runTest {
    repositoryMock.meetingsToReturn = MeetingProvider.sampleMeetings

    viewModel.loadMeetings("any_project_id")

    // After the coroutine completes, isLoading should be false
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
    // Given the repository will throw an error
    val errorMessage = "Network Error"
    repositoryMock.shouldThrowError = true
    repositoryMock.errorMessage = errorMessage

    // When loadMeetings is called
    viewModel.loadMeetings("any_project_id")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then the error message should be in the UI state
    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun selectTabUpdatesTheSelectedTabInUJiState() {
    // When a new tab is selected
    viewModel.selectTab(MeetingTab.PAST)

    // Then the state should reflect the change
    assertEquals(MeetingTab.PAST, viewModel.uiState.value.selectedTab)

    // When the other tab is selected
    viewModel.selectTab(MeetingTab.UPCOMING)

    // Then the state should change back
    assertEquals(MeetingTab.UPCOMING, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun clearErrorMsgSetsErrorMsgToNull() = runTest {
    // Given the state has an error
    repositoryMock.shouldThrowError = true
    viewModel.loadMeetings("any_project_id")
    testDispatcher.scheduler.advanceUntilIdle()
    assertNotNull(viewModel.uiState.value.errorMsg)

    // When the error message is cleared
    viewModel.clearErrorMsg()

    // Then the error message in the state should be null
    assertNull(viewModel.uiState.value.errorMsg)
  }
}

// A slightly modified mock for more flexible ViewModel testing
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
  ): Result<String> {
    return runCatching { "url" }
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    return runCatching {}
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
    return runCatching {}
  }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
    return flow { emptyList<Participant>() }
  }

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> {
    return runCatching {}
  }

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> {
    return runCatching {}
  }

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> {
    return runCatching {}
  }
}
