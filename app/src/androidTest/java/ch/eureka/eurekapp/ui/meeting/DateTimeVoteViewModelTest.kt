package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.DateTimeVote
import ch.eureka.eurekapp.model.data.meeting.Meeting
import com.google.firebase.Timestamp // You will need to mock this or have it available
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [DateTimeVoteViewModel].
 *
 * Note : some tests where generated with the help of Gemini
 */
@ExperimentalCoroutinesApi
class DateTimeVoteViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: DateTimeVoteViewModel
  private lateinit var repositoryMock: DateTimeVoteRepositoryMock

  private var currentUserId: String? = "test-user-id"

  companion object {
    private const val TEST_PROJECT_ID = "proj-123"
    private const val TEST_MEETING_ID = "meet-123"
    private const val OTHER_USER_ID = "other-user-id"

    private val mockTimestamp = Timestamp(Date(1730000000L))

    val VOTE_1 = DateTimeVote(dateTime = mockTimestamp, voters = listOf(OTHER_USER_ID))
    val VOTE_2 = DateTimeVote(dateTime = mockTimestamp, voters = emptyList())
    val TEST_VOTES = listOf(VOTE_1, VOTE_2)
    val TEST_MEETING =
        Meeting(
            meetingID = TEST_MEETING_ID, projectId = TEST_PROJECT_ID, dateTimeVotes = TEST_VOTES)
  }

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = DateTimeVoteRepositoryMock()
  }

  /** Helper to create the ViewModel, allows for setting the user ID. */
  private fun createViewModel(userId: String? = currentUserId) {
    viewModel =
        DateTimeVoteViewModel(
            projectId = TEST_PROJECT_ID,
            meetingId = TEST_MEETING_ID,
            repository = repositoryMock,
            getCurrentUserId = { userId })
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun clearErrorMsg_setsErrorMsgToNull() {
    createViewModel()
    viewModel.setErrorMsg("An error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setErrorMsg_updatesErrorMsg() {
    createViewModel()
    val errorMessage = "This is a test error"
    viewModel.setErrorMsg(errorMessage)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setVotesSaved_updatesVotesSavedToTrue() {
    createViewModel()
    assertFalse(viewModel.uiState.value.votesSaved)
    viewModel.setVotesSaved()
    assertTrue(viewModel.uiState.value.votesSaved)
  }

  @Test
  fun voteForDateTime_whenUserLoggedInAndVoteExists_addsVote() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    // Act: Vote for the second option (which is empty)
    val targetVote = viewModel.uiState.value.dateTimeVotes[1]
    viewModel.voteForDateTime(targetVote)

    // Assert
    val updatedVotes = viewModel.uiState.value.dateTimeVotes
    assertEquals(2, updatedVotes.size)
    assertEquals(1, updatedVotes[0].voters.size)
    assertEquals(1, updatedVotes[1].voters.size)
    assertTrue(updatedVotes[1].voters.contains(currentUserId))
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun voteForDateTime_whenVoteDoesNotExist_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidVote = DateTimeVote()
    viewModel.voteForDateTime(invalidVote)

    assertEquals("Datetime to vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun voteForDateTime_whenUserNotLoggedIn_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel(userId = null)
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.voteForDateTime(TEST_VOTES[0])
    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForDateTime_whenUserLoggedInAndVoteExists_removesVote() = runTest {
    val votedVote = VOTE_1.copy(voters = listOf(OTHER_USER_ID, currentUserId!!))
    val meetingWithVote = TEST_MEETING.copy(dateTimeVotes = listOf(votedVote, VOTE_2))
    repositoryMock.setMeetingToReturn(meetingWithVote)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetVote = viewModel.uiState.value.dateTimeVotes[0]
    viewModel.retractVoteForDateTime(targetVote)

    // Assert
    val updatedVotes = viewModel.uiState.value.dateTimeVotes
    assertEquals(1, updatedVotes[0].voters.size)
    assertFalse(updatedVotes[0].voters.contains(currentUserId))
    assertTrue(updatedVotes[0].voters.contains(OTHER_USER_ID))
    assertEquals(0, updatedVotes[1].voters.size)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForDateTime_whenVoteDoesNotExist_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidVote = DateTimeVote()
    viewModel.retractVoteForDateTime(invalidVote)

    assertEquals("Datetime to vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForDateTime_whenUserNotLoggedIn_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel(userId = null)
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.retractVoteForDateTime(TEST_VOTES[0])
    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForDateTime_whenUserDidNotVote_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetVote = viewModel.uiState.value.dateTimeVotes[0]
    viewModel.retractVoteForDateTime(targetVote)

    assertEquals(
        "Cannot retract vote since you did not vote in the first place",
        viewModel.uiState.value.errorMsg)
  }

  @Test
  fun loadDateTimeVotes_onSuccess_updatesState() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()

    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(TEST_VOTES, state.dateTimeVotes)
    assertEquals(TEST_MEETING, state.meeting)
    assertFalse(state.votesSaved)
    assertNull(state.errorMsg)
  }

  @Test
  fun loadDateTimeVotes_onRepositoryException_setsError() = runTest {
    val error = Exception("Database is down")
    repositoryMock.setMeetingLoadToFail(error)
    createViewModel()

    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Database is down", state.errorMsg)
  }

  @Test
  fun loadDateTimeVotes_whenMeetingIsNull_setsError() = runTest {
    repositoryMock.setMeetingToReturn(null)
    createViewModel()

    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Meeting is null.", state.errorMsg)
  }

  @Test
  fun loadDateTimeVotes_whenMeetingHasEmptyVotes_setsError() = runTest {
    val emptyVoteMeeting = TEST_MEETING.copy(dateTimeVotes = emptyList())
    repositoryMock.setMeetingToReturn(emptyVoteMeeting)
    createViewModel()

    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Meeting is null.", state.errorMsg)
  }

  @Test
  fun confirmDateTimeVotes_onSuccess_setsVotesSaved() = runTest {
    // Load the meeting first
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.updateShouldSucceed = true

    viewModel.confirmDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert
    assertTrue(viewModel.uiState.value.votesSaved)
    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(TEST_MEETING, repositoryMock.lastMeetingUpdated)
  }

  @Test
  fun confirmDateTimeVotes_onFailure_setsErrorMsg() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.updateShouldSucceed = false
    repositoryMock.updateFailureException = Exception("Update failed")

    viewModel.confirmDateTimeVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.votesSaved)
    assertEquals("Meeting could not be updated.", viewModel.uiState.value.errorMsg)
    assertEquals(TEST_MEETING, repositoryMock.lastMeetingUpdated)
  }
}
