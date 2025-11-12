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
    private const val CURRENT_USER_ID = "test-user-id"

    private val mockTimestamp = Timestamp(Date(1730000000L))

    // --- Original Test Data ---
    val OTHER_USER_VOTE = MeetingProposalVote(OTHER_USER_ID, listOf(MeetingFormat.VIRTUAL))
    val CURRENT_USER_VOTE = MeetingProposalVote(CURRENT_USER_ID, listOf(MeetingFormat.IN_PERSON))

    val VOTE_1 = MeetingProposal(dateTime = mockTimestamp, votes = listOf(OTHER_USER_VOTE))
    val VOTE_2 = MeetingProposal(dateTime = mockTimestamp, votes = emptyList())
    val ORIGINAL_TEST_PROPOSALS = listOf(VOTE_1, VOTE_2)
    val ORIGINAL_TEST_MEETING =
        Meeting(
            meetingID = TEST_MEETING_ID,
            projectId = TEST_PROJECT_ID,
            meetingProposals = ORIGINAL_TEST_PROPOSALS)

    val VOTE_IN_PERSON = MeetingProposalVote(CURRENT_USER_ID, listOf(MeetingFormat.IN_PERSON))
    val VOTE_VIRTUAL = MeetingProposalVote(CURRENT_USER_ID, listOf(MeetingFormat.VIRTUAL))
    val VOTE_BOTH =
        MeetingProposalVote(CURRENT_USER_ID, listOf(MeetingFormat.IN_PERSON, MeetingFormat.VIRTUAL))

    val PROPOSAL_NO_VOTE =
        MeetingProposal(dateTime = mockTimestamp, votes = listOf(OTHER_USER_VOTE))
    val PROPOSAL_IN_PERSON_VOTE =
        MeetingProposal(dateTime = mockTimestamp, votes = listOf(OTHER_USER_VOTE, VOTE_IN_PERSON))
    val PROPOSAL_VIRTUAL_VOTE =
        MeetingProposal(dateTime = mockTimestamp, votes = listOf(OTHER_USER_VOTE, VOTE_VIRTUAL))
    val PROPOSAL_BOTH_VOTE =
        MeetingProposal(dateTime = mockTimestamp, votes = listOf(OTHER_USER_VOTE, VOTE_BOTH))
    val PROPOSAL_EMPTY = MeetingProposal(dateTime = mockTimestamp, votes = emptyList())

    val TEST_MEETING_FOR_FORMATS =
        Meeting(
            meetingID = TEST_MEETING_ID,
            projectId = TEST_PROJECT_ID,
            meetingProposals =
                listOf(
                    PROPOSAL_NO_VOTE, // 0
                    PROPOSAL_IN_PERSON_VOTE, // 1
                    PROPOSAL_BOTH_VOTE, // 2
                    PROPOSAL_EMPTY // 3
                    ))
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
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
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
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidProposal = MeetingProposal()
    viewModel.voteForMeetingProposal(invalidProposal, CURRENT_USER_VOTE)

    assertEquals("Meeting proposal to vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  // --- TEST FIXED ---
  @Test
  fun voteForMeetingProposalWhenUserNotLoggedIn_doesNothing() = runTest {
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val originalProposals = viewModel.uiState.value.meetingProposals
    viewModel.voteForMeetingProposal(ORIGINAL_TEST_PROPOSALS[0], CURRENT_USER_VOTE)

    // Assert state did not change
    assertEquals(originalProposals, viewModel.uiState.value.meetingProposals)
    // Assert no error was set
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenUserLoggedInAndVoteExistsRemovesVote() = runTest {
    val votedProposal = VOTE_1.copy(votes = listOf(OTHER_USER_VOTE, CURRENT_USER_VOTE))
    val meetingWithVote =
        ORIGINAL_TEST_MEETING.copy(meetingProposals = listOf(votedProposal, VOTE_2))
    repositoryMock.setMeetingToReturn(meetingWithVote)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetProposal = viewModel.uiState.value.meetingProposals[0]
    viewModel.retractVoteForMeetingProposal(targetProposal)

    val updatedProposals = viewModel.uiState.value.meetingProposals
    assertEquals(1, updatedProposals[0].votes.size)
    assertFalse(updatedProposals[0].votes.contains(CURRENT_USER_VOTE))
    assertTrue(updatedProposals[0].votes.contains(OTHER_USER_VOTE))
    assertEquals(0, updatedProposals[1].votes.size)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenProposalDoesNotExistSetsError() = runTest {
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidProposal = MeetingProposal()
    viewModel.retractVoteForMeetingProposal(invalidProposal)

    assertEquals(
        "Meeting proposal to retract vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  // --- TEST FIXED ---
  @Test
  fun retractVoteForMeetingProposalWhenUserNotLoggedIn_doesNothing() = runTest {
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val originalProposals = viewModel.uiState.value.meetingProposals
    viewModel.retractVoteForMeetingProposal(ORIGINAL_TEST_PROPOSALS[0])

    // Assert state did not change
    assertEquals(originalProposals, viewModel.uiState.value.meetingProposals)
    // Assert no error was set
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractVoteForMeetingProposalWhenUserDidNotVoteSetsError() = runTest {
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val targetProposal = viewModel.uiState.value.meetingProposals[0]
    viewModel.retractVoteForMeetingProposal(targetProposal)

    assertEquals(
        "Cannot retract vote since you did not vote in the first place",
        viewModel.uiState.value.errorMsg)
  }

  @Test
  fun loadMeetingProposalsOnSuccessUpdatesState() = runTest {
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel()

    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val state = viewModel.uiState.value
    assertFalse(state.isLoading)
    assertEquals(ORIGINAL_TEST_PROPOSALS, state.meetingProposals)
    assertEquals(ORIGINAL_TEST_MEETING, state.meeting)
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
    val emptyProposalMeeting = ORIGINAL_TEST_MEETING.copy(meetingProposals = emptyList())
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
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.updateShouldSucceed = true

    viewModel.confirmMeetingProposalsVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.votesSaved)
    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(ORIGINAL_TEST_MEETING, repositoryMock.lastMeetingUpdated)
  }

  @Test
  fun confirmMeetingProposalsVotesOnFailureSetsErrorMsg() = runTest {
    repositoryMock.setMeetingToReturn(ORIGINAL_TEST_MEETING)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.updateShouldSucceed = false
    repositoryMock.updateFailureException = Exception("Update failed")

    viewModel.confirmMeetingProposalsVotes()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.votesSaved)
    assertEquals("Meeting could not be updated.", viewModel.uiState.value.errorMsg)
    assertEquals(ORIGINAL_TEST_MEETING, repositoryMock.lastMeetingUpdated)
  }

  // --- TEST FIXED ---
  @Test
  fun hasVotedForFormat_whenUserNotLoggedIn_returnsFalseAndSetsNoError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val proposal = viewModel.uiState.value.meetingProposals[1]
    val result = viewModel.hasVotedForFormat(proposal, MeetingFormat.IN_PERSON)

    assertFalse(result)
    // Assert no error was set
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun hasVotedForFormat_whenUserVotedForFormat_returnsTrue() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val proposal = viewModel.uiState.value.meetingProposals[1]
    val result = viewModel.hasVotedForFormat(proposal, MeetingFormat.IN_PERSON)

    assertTrue(result)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun hasVotedForFormat_whenUserVotedButNotForFormat_returnsFalse() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val proposal = viewModel.uiState.value.meetingProposals[1]
    val result = viewModel.hasVotedForFormat(proposal, MeetingFormat.VIRTUAL)

    assertFalse(result)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun hasVotedForFormat_whenUserHasNoVote_returnsFalse() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val proposal = viewModel.uiState.value.meetingProposals[0]
    val result = viewModel.hasVotedForFormat(proposal, MeetingFormat.IN_PERSON)

    assertFalse(result)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  // --- Tests for addFormatVote ---

  @Test
  fun addFormatVote_ForAlreadyVotedMeetingProposal_whenProposalNotExists_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidProposal = MeetingProposal()
    viewModel.addFormatVoteForAlreadyVotedMeetingProposal(invalidProposal, MeetingFormat.IN_PERSON)

    assertEquals("Meeting proposal to vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  // --- TEST FIXED (from NPE to correct assertion) ---
  @Test
  fun addFormatVote_ForAlreadyVotedMeetingProposal_whenUserNotLoggedIn_doesNothing() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val originalProposals = viewModel.uiState.value.meetingProposals
    val proposal = viewModel.uiState.value.meetingProposals[0]
    // We call the function (which now has the null guard)
    viewModel.addFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)

    // Assert state did not change
    assertEquals(originalProposals, viewModel.uiState.value.meetingProposals)
    // Assert no error was set
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun addFormatVote_whenUserAlreadyVotedForFormat_setsErrorForAlreadyVotedMeetingProposal() =
      runTest {
        repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
        createViewModel()
        viewModel.loadMeetingProposals()
        testDispatcher.scheduler.advanceUntilIdle()

        val proposal = viewModel.uiState.value.meetingProposals[1]
        viewModel.addFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)

        assertEquals(
            "Cannot add vote since you already vote in the first place",
            viewModel.uiState.value.errorMsg)
      }

  @Test
  fun addFormatVote_whenUserHasVoteAndAddsNewFormat_succeedsForAlreadyVotedMeetingProposal() =
      runTest {
        repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
        createViewModel()
        viewModel.loadMeetingProposals()
        testDispatcher.scheduler.advanceUntilIdle()

        val proposal = viewModel.uiState.value.meetingProposals[1]
        viewModel.addFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.VIRTUAL)

        val updatedProposal = viewModel.uiState.value.meetingProposals[1]
        val usersVote = updatedProposal.votes.find { it.userId == CURRENT_USER_ID }

        assertNull(viewModel.uiState.value.errorMsg)
        assertNotNull(usersVote)
        assertEquals(2, usersVote!!.formatPreferences.size)
        assertTrue(
            usersVote.formatPreferences.containsAll(
                listOf(MeetingFormat.IN_PERSON, MeetingFormat.VIRTUAL)))
      }

  @Test(expected = NullPointerException::class)
  fun addFormatVote_whenUserHasNoVote_ForAlreadyVotedMeetingProposal_throwsNullPointerException() =
      runTest {
        repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
        createViewModel()
        viewModel.loadMeetingProposals()
        testDispatcher.scheduler.advanceUntilIdle()

        val proposal = viewModel.uiState.value.meetingProposals[0]

        viewModel.addFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)
      }

  @Test
  fun retractFormatVote_ForAlreadyVotedMeetingProposal_whenProposalNotExists_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val invalidProposal = MeetingProposal()
    viewModel.retractFormatVoteForAlreadyVotedMeetingProposal(
        invalidProposal, MeetingFormat.IN_PERSON)

    assertEquals(
        "Meeting proposal to retract vote for does not exists.", viewModel.uiState.value.errorMsg)
  }

  // --- TEST FIXED ---
  @Test
  fun retractFormatVote_ForAlreadyVotedMeetingProposal_whenUserNotLoggedIn_doesNothing() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel(userId = null)
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val originalProposals = viewModel.uiState.value.meetingProposals
    val proposal = viewModel.uiState.value.meetingProposals[1]
    viewModel.retractFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)

    // Assert state did not change
    assertEquals(originalProposals, viewModel.uiState.value.meetingProposals)
    // Assert no error was set
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractFormatVote_whenUserHasNotVotedForFormat_setsErrorForAlreadyVotedMeetingProposal() =
      runTest {
        repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
        createViewModel()
        viewModel.loadMeetingProposals()
        testDispatcher.scheduler.advanceUntilIdle()

        val proposal = viewModel.uiState.value.meetingProposals[1]
        viewModel.retractFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.VIRTUAL)

        assertEquals(
            "Cannot retract vote since you did not vote in the first place",
            viewModel.uiState.value.errorMsg)
      }

  @Test
  fun retractFormatVote_whenUserHasNoVoteForAlreadyVotedMeetingProposalAtAll_setsError() = runTest {
    repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
    createViewModel()
    viewModel.loadMeetingProposals()
    testDispatcher.scheduler.advanceUntilIdle()

    val proposal = viewModel.uiState.value.meetingProposals[0]
    viewModel.retractFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)

    assertEquals(
        "Cannot retract vote since you did not vote in the first place",
        viewModel.uiState.value.errorMsg)
  }

  @Test
  fun retractFormatVote_whenUserVotedForMultipleFormats_removesOneFormatForAlreadyVotedMeetingProposal() =
      runTest {
        repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
        createViewModel()
        viewModel.loadMeetingProposals()
        testDispatcher.scheduler.advanceUntilIdle()

        val proposal = viewModel.uiState.value.meetingProposals[2]
        viewModel.retractFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)

        val updatedProposal = viewModel.uiState.value.meetingProposals[2]
        val usersVote = updatedProposal.votes.find { it.userId == CURRENT_USER_ID }

        assertNull(viewModel.uiState.value.errorMsg)
        assertNotNull(usersVote)
        assertEquals(1, usersVote!!.formatPreferences.size)
        assertTrue(usersVote.formatPreferences.contains(MeetingFormat.VIRTUAL))
        assertFalse(usersVote.formatPreferences.contains(MeetingFormat.IN_PERSON))
      }

  @Test
  fun retractFormatVote_whenUserVotedForOnlyOneFormat_removesFormatAndLeavesEmptyListForAlreadyVotedMeetingProposal() =
      runTest {
        repositoryMock.setMeetingToReturn(TEST_MEETING_FOR_FORMATS)
        createViewModel()
        viewModel.loadMeetingProposals()
        testDispatcher.scheduler.advanceUntilIdle()

        val proposal = viewModel.uiState.value.meetingProposals[1]
        viewModel.retractFormatVoteForAlreadyVotedMeetingProposal(proposal, MeetingFormat.IN_PERSON)

        val updatedProposal = viewModel.uiState.value.meetingProposals[1]
        val usersVote = updatedProposal.votes.find { it.userId == CURRENT_USER_ID }

        assertNull(viewModel.uiState.value.errorMsg)
        assertNotNull(usersVote)
        assertTrue(usersVote!!.formatPreferences.isEmpty())
      }
}
