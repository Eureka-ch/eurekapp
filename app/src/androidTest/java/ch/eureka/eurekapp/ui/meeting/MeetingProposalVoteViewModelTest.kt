package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import com.google.firebase.Timestamp
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
 * Test suite for [MeetingProposalVoteViewModel].
 *
 * Note : some tests where generated with the help of Gemini
 */
@ExperimentalCoroutinesApi
class MeetingProposalVoteViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingProposalVoteViewModel
  private lateinit var repositoryMock: MeetingProposalVoteRepositoryMock

  private var currentUserId: String? = "test-user-id"

  companion object {
    private const val TEST_PROJECT_ID = "proj-123"
    private const val TEST_MEETING_ID = "meet-123"
    private const val OTHER_USER_ID = "other-user-id"

    private val mockTimestamp = Timestamp(Date(1730000000L))

    // --- Updated Test Data ---
    val OTHER_USER_VOTE = MeetingProposalVote(OTHER_USER_ID, listOf(MeetingFormat.VIRTUAL))
    val CURRENT_USER_VOTE = MeetingProposalVote("test-user-id", listOf(MeetingFormat.IN_PERSON))

    val VOTE_1 = MeetingProposal(dateTime = mockTimestamp, votes = listOf(OTHER_USER_VOTE))
    val VOTE_2 = MeetingProposal(dateTime = mockTimestamp, votes = emptyList())
    val TEST_PROPOSALS = listOf(VOTE_1, VOTE_2)
    val TEST_MEETING =
        Meeting(
            meetingID = TEST_MEETING_ID,
            projectId = TEST_PROJECT_ID,
            meetingProposals = TEST_PROPOSALS)
  }

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MeetingProposalVoteRepositoryMock()
  }

  private fun createViewModel(userId: String? = currentUserId) {
    viewModel =
        MeetingProposalVoteViewModel(
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
  fun clearErrorMsgSetsErrorMsgToNull() {
    createViewModel()
    viewModel.setErrorMsg("An error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setErrorMsgUpdatesErrorMsg() {
    createViewModel()
    val errorMessage = "This is a test error"
    viewModel.setErrorMsg(errorMessage)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)
  }

  @Test
  fun setVotesSavedUpdatesVotesSavedToTrue() {
    createViewModel()
    assertFalse(viewModel.uiState.value.votesSaved)
    viewModel.setVotesSaved()
    assertTrue(viewModel.uiState.value.votesSaved)
  }

  @Test
  fun voteForMeetingProposalWhenUserLoggedInAndProposalExistsAddsVote() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetProposal = viewModel.uiState.value.meetingProposals[1]
    viewModel.voteForMeetingProposal(targetProposal, CURRENT_USER_VOTE)

    val updatedProposals = viewModel.uiState.value.meetingProposals
    assertEquals(2, updatedProposals.size)
    assertEquals(1, updatedProposals[0].votes.size)
    assertEquals(1, updatedProposals[1].votes.size)
    assertTrue(updatedProposals[1].votes.contains(CURRENT_USER_VOTE))
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun voteForMeetingProposalWhenProposalDoesNotExistSetsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidProposal = MeetingProposal()
    viewModel.voteForMeetingProposal(invalidProposal, CURRENT_USER_VOTE)

    assertEquals("Meeting proposal to vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun voteForMeetingProposalWhenUserNotLoggedInSetsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.voteForMeetingProposal(TEST_PROPOSALS[0], CURRENT_USER_VOTE)
    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenUserLoggedInAndVoteExistsRemovesVote() = runTest {
    val votedProposal = VOTE_1.copy(votes = listOf(OTHER_USER_VOTE, CURRENT_USER_VOTE))
    val meetingWithVote = TEST_MEETING.copy(meetingProposals = listOf(votedProposal, VOTE_2))
    repositoryMock.setMeetingToReturn(meetingWithVote)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetProposal = viewModel.uiState.value.meetingProposals[0]
    viewModel.retractVoteForMeetingProposal(targetProposal, CURRENT_USER_VOTE)

    val updatedProposals = viewModel.uiState.value.meetingProposals
    assertEquals(1, updatedProposals[0].votes.size)
    assertFalse(updatedProposals[0].votes.contains(CURRENT_USER_VOTE))
    assertTrue(updatedProposals[0].votes.contains(OTHER_USER_VOTE))
    assertEquals(0, updatedProposals[1].votes.size)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenProposalDoesNotExistSetsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidProposal = MeetingProposal()
    viewModel.retractVoteForMeetingProposal(invalidProposal, CURRENT_USER_VOTE)

    assertEquals(
        "Meeting proposal to retract vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenUserNotLoggedInSetsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.retractVoteForMeetingProposal(TEST_PROPOSALS[0], OTHER_USER_VOTE)
    assertEquals("Not logged in", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenUserDidNotVoteSetsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetProposal = viewModel.uiState.value.meetingProposals[0]
    viewModel.retractVoteForMeetingProposal(targetProposal, CURRENT_USER_VOTE)

    assertEquals(
        "Cannot retract vote since you did not vote in the first place",
        viewModel.uiState.value.errorMsg)
  }

  @Test
  fun loadMeetingProposalsOnSuccessUpdatesState() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()

    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(TEST_PROPOSALS, state.meetingProposals)
    assertEquals(TEST_MEETING, state.meeting)
    assertFalse(state.votesSaved)
    assertNull(state.errorMsg)
  }

  @Test
  fun loadMeetingProposalsOnRepositoryExceptionSetsError() = runTest {
    val error = Exception("Database is down")
    repositoryMock.setMeetingLoadToFail(error)
    createViewModel()

    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Database is down", state.errorMsg)
  }

  @Test
  fun loadMeetingProposalsWhenMeetingIsNullSetsError() = runTest {
    repositoryMock.setMeetingToReturn(null)
    createViewModel()

    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Meeting is null.", state.errorMsg)
  }

  @Test
  fun loadMeetingProposalsWhenMeetingHasEmptyProposalsSetsError() = runTest {
    val emptyProposalMeeting = TEST_MEETING.copy(meetingProposals = emptyList())
    repositoryMock.setMeetingToReturn(emptyProposalMeeting)
    createViewModel()

    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals("Meeting is null.", state.errorMsg)
  }

  @Test
  fun confirmMeetingProposalsVotesOnSuccessSetsVotesSaved() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.updateShouldSucceed = true

    viewModel.confirmMeetingProposalsVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.votesSaved)
    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(TEST_MEETING, repositoryMock.lastMeetingUpdated)
  }

  @Test
  fun confirmMeetingProposalsVotesOnFailureSetsErrorMsg() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.updateShouldSucceed = false
    repositoryMock.updateFailureException = Exception("Update failed")

    viewModel.confirmMeetingProposalsVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.votesSaved)
    assertEquals("Meeting could not be updated.", viewModel.uiState.value.errorMsg)
    assertEquals(TEST_MEETING, repositoryMock.lastMeetingUpdated)
  }
}
