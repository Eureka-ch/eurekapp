/* Portions of this file were written with the help of Gemini and Grok.*/
package ch.eureka.eurekapp.ui.meeting

import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.*
import ch.eureka.eurekapp.model.map.Location
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
 * Note : some tests where generated with Gemini, chatGPT, and Grok
 */
@ExperimentalCoroutinesApi
class MeetingViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingViewModel
  private lateinit var repositoryMock: MeetingRepositoryMockViewmodel

  private val testCreatorId = "test-creator-id"
  private var currentUserId: String? = testCreatorId

  private val date1 = Timestamp(Date(1000L))
  private val date2 = Timestamp(Date(2000L))
  private val testLocation = Location(name = "Test Office")

  private val voteInPerson = MeetingProposalVote("u1", listOf(MeetingFormat.IN_PERSON))
  private val voteVirtual = MeetingProposalVote("u2", listOf(MeetingFormat.VIRTUAL))
  private val voteInPerson2 = MeetingProposalVote("u3", listOf(MeetingFormat.IN_PERSON))
  private val voteVirtual2 = MeetingProposalVote("u4", listOf(MeetingFormat.VIRTUAL))
  private val voteNoFormat = MeetingProposalVote("u5", emptyList())

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
    repositoryMock = MeetingRepositoryMockViewmodel()
    viewModel = MeetingViewModel(repositoryMock, { currentUserId })
  }

  private fun createDummyMeeting(
      createdBy: String = testCreatorId,
      proposals: List<MeetingProposal> = emptyList(),
      location: Location? = testLocation,
      projectId: String = "p1"
  ): Meeting {
    return Meeting(
        meetingID = "m1",
        projectId = projectId,
        createdBy = createdBy,
        meetingProposals = proposals,
        location = location)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun meetingViewModel_initialStateIsCorrect() {
    val uiState = viewModel.uiState.value
    assertTrue(uiState.upcomingMeetings.isEmpty())
    assertTrue(uiState.pastMeetings.isEmpty())
    assertFalse(uiState.isLoading)
    assertNull(uiState.errorMsg)
    assertEquals(MeetingTab.UPCOMING, uiState.selectedTab)
  }

  @Test
  fun meetingViewModel_loadMeetingsCorrectlyFiltersAndSortsMeetings() = runTest {
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

    viewModel.loadMeetings()
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value

    assertFalse(uiState.isLoading)

    assertEquals(listOf(meeting1, meeting2), uiState.upcomingMeetings)
    assertEquals(listOf(meeting3), uiState.pastMeetings)

    val upcomingIds = uiState.upcomingMeetings.map { it.meetingID }
    val pastIds = uiState.pastMeetings.map { it.meetingID }

    assertEquals(listOf("1", "2"), upcomingIds)
    assertEquals(listOf("3"), pastIds)
  }

  @Test
  fun meetingViewModel_loadMeetingsSortsByTimeSlotWhenDatetimeIsNull() = runTest {
    val baseTime = Date().time

    val meetingWithDatetime =
        Meeting(
            meetingID = "1",
            title = "With Datetime",
            status = MeetingStatus.SCHEDULED,
            datetime = Timestamp(Date(baseTime + 1000000)))

    val dummyVote = MeetingProposalVote(userId = "u1")
    val meetingWithTime =
        Meeting(
            meetingID = "2",
            title = "With TimeSlot",
            status = MeetingStatus.SCHEDULED,
            datetime = null,
            meetingProposals =
                listOf(
                    MeetingProposal(Timestamp(Date(baseTime - 1000000)), listOf(dummyVote)),
                    MeetingProposal(Timestamp(Date(baseTime + 1000001)), listOf(dummyVote)),
                    MeetingProposal(
                        Timestamp(Date(baseTime - 2000000)), emptyList()) // This one is ignored
                    ))

    repositoryMock.meetingsToReturn = listOf(meetingWithDatetime, meetingWithTime)

    viewModel.loadMeetings()
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value

    val upcomingIds = uiState.upcomingMeetings.map { it.meetingID }

    assertEquals(listOf("1", "2"), upcomingIds)
    assertTrue(uiState.pastMeetings.isEmpty())
  }

  @Test
  fun meetingViewModel_loadMeetingsHandlesLoadingStateCorrectly() = runTest {
    repositoryMock.meetingsToReturn = listOf(Meeting(title = "Planning"))
    viewModel.loadMeetings()

    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isLoading)
  }

  @Test
  fun meetingViewModel_loadMeetingsHandlesEmptyListFromRepository() = runTest {
    repositoryMock.meetingsToReturn = emptyList()

    viewModel.loadMeetings()
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertTrue(uiState.upcomingMeetings.isEmpty())
    assertTrue(uiState.pastMeetings.isEmpty())
  }

  @Test
  fun meetingViewModel_loadMeetingsHandlesRepositoryError() = runTest {
    val errorMessage = "Network Error"
    repositoryMock.shouldThrowError = true
    repositoryMock.errorMessage = errorMessage

    viewModel.loadMeetings()
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun meetingViewModel_loadMeetingsLoadsMeetingsFromDifferentProjects() = runTest {
    val meetingProjA =
        Meeting(
            meetingID = "1",
            projectId = "projectA",
            title = "Project A Meeting",
            status = MeetingStatus.SCHEDULED,
            datetime = Timestamp(Date()))
    val meetingProjB =
        Meeting(
            meetingID = "2",
            projectId = "projectB",
            title = "Project B Meeting",
            status = MeetingStatus.SCHEDULED,
            datetime = Timestamp(Date()))

    repositoryMock.meetingsToReturn = listOf(meetingProjA, meetingProjB)

    viewModel.loadMeetings()
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertEquals(2, uiState.upcomingMeetings.size)
    assertTrue(uiState.upcomingMeetings.any { it.projectId == "projectA" })
    assertTrue(uiState.upcomingMeetings.any { it.projectId == "projectB" })
  }

  @Test
  fun meetingViewModel_selectTabUpdatesTheSelectedTabInUIState() {
    viewModel.selectTab(MeetingTab.PAST)
    assertEquals(MeetingTab.PAST, viewModel.uiState.value.selectedTab)

    viewModel.selectTab(MeetingTab.UPCOMING)
    assertEquals(MeetingTab.UPCOMING, viewModel.uiState.value.selectedTab)
  }

  @Test
  fun meetingViewModel_clearErrorMsgSetsErrorMsgToNull() = runTest {
    viewModel.setErrorMsg("Test Error")
    assertNotNull(viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingWhenUserIsNotCreatorAndNoVotes() = runTest {
    // We create a meeting where createdBy is NOT the currentUserId ("test-creator-id")
    val meeting = createDummyMeeting(createdBy = "other-user", proposals = emptyList())
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(0, repositoryMock.updateMeetingCallCount)
    assertEquals(
        "Cannot close votes since you are not the creator of the meeting.",
        viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingWhenNoVotesCast() = runTest {
    val meeting = createDummyMeeting(proposals = emptyList())
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(0, repositoryMock.updateMeetingCallCount)
    assertEquals("No votes have been cast for any proposal.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingWhenWinningProposalHasNoFormatVotes() = runTest {
    val proposals = listOf(MeetingProposal(date1, listOf(voteNoFormat)))
    val meeting = createDummyMeeting(proposals = proposals)
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(0, repositoryMock.updateMeetingCallCount)
    assertEquals(
        "Could not determine a winning format from the votes", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingWhenWinnerIsInPersonButNoLocation() = runTest {
    val proposals = listOf(MeetingProposal(date1, listOf(voteInPerson)))
    val meeting = createDummyMeeting(proposals = proposals, location = null)
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(0, repositoryMock.updateMeetingCallCount)
    assertEquals(
        "Cannot close votes, in-person meeting has no location.", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingSuccessVirtual() = runTest {
    val proposals = listOf(MeetingProposal(date1, listOf(voteVirtual)))
    val meeting = createDummyMeeting(proposals = proposals, location = null)
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(1, repositoryMock.updateMeetingCallCount)
    assertEquals(1, repositoryMock.loadMeetingsCallCount)

    val updatedMeeting = repositoryMock.updatedMeeting
    assertNotNull(updatedMeeting)
    assertEquals(MeetingStatus.SCHEDULED, updatedMeeting!!.status)
    assertEquals(MeetingFormat.VIRTUAL, updatedMeeting.format)
    assertEquals(date1, updatedMeeting.datetime)
    assertNotNull(updatedMeeting.link)
    assertTrue(updatedMeeting.meetingProposals.isEmpty())
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingSuccessInPerson() = runTest {
    val proposals = listOf(MeetingProposal(date1, listOf(voteInPerson)))
    val meeting = createDummyMeeting(proposals = proposals)
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(1, repositoryMock.updateMeetingCallCount)
    assertEquals(1, repositoryMock.loadMeetingsCallCount)

    val updatedMeeting = repositoryMock.updatedMeeting
    assertNotNull(updatedMeeting)
    assertEquals(MeetingStatus.SCHEDULED, updatedMeeting!!.status)
    assertEquals(MeetingFormat.IN_PERSON, updatedMeeting.format)
    assertEquals(date1, updatedMeeting.datetime)
    assertNull(updatedMeeting.link)
    assertTrue(updatedMeeting.meetingProposals.isEmpty())
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingSuccessPicksProposalWithMostVotes() = runTest {
    val p1 = MeetingProposal(date1, listOf(voteInPerson))
    val p2 = MeetingProposal(date2, listOf(voteVirtual, voteVirtual2))
    val meeting = createDummyMeeting(proposals = listOf(p1, p2))
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(1, repositoryMock.updateMeetingCallCount)

    val updatedMeeting = repositoryMock.updatedMeeting
    assertNotNull(updatedMeeting)
    assertEquals(date2, updatedMeeting!!.datetime)
    assertEquals(MeetingFormat.VIRTUAL, updatedMeeting.format)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingSuccessUsesFormatVoteAsDatetimeTieBreaker() = runTest {
    val p1 = MeetingProposal(date1, listOf(voteInPerson, voteVirtual))
    val p2 = MeetingProposal(date2, listOf(voteInPerson, voteInPerson2))
    val meeting = createDummyMeeting(proposals = listOf(p1, p2))
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(1, repositoryMock.updateMeetingCallCount)

    val updatedMeeting = repositoryMock.updatedMeeting
    assertNotNull(updatedMeeting)
    assertEquals(date2, updatedMeeting!!.datetime)
    assertEquals(MeetingFormat.IN_PERSON, updatedMeeting.format)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingSuccessPicksFirstOnCompleteTie() = runTest {
    val p1 = MeetingProposal(date1, listOf(voteInPerson, voteInPerson2))
    val p2 = MeetingProposal(date2, listOf(voteVirtual, voteVirtual2))
    val meeting = createDummyMeeting(proposals = listOf(p1, p2))
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
    assertEquals(1, repositoryMock.updateMeetingCallCount)

    val updatedMeeting = repositoryMock.updatedMeeting
    assertNotNull(updatedMeeting)
    assertEquals(date1, updatedMeeting!!.datetime)
    assertEquals(MeetingFormat.IN_PERSON, updatedMeeting.format)
  }

  @Test
  fun meetingViewModel_closeVotesForMeetingWhenRepositoryUpdateFails() = runTest {
    repositoryMock.updateShouldFail = true
    val proposals = listOf(MeetingProposal(date1, listOf(voteInPerson)))
    val meeting = createDummyMeeting(proposals = proposals)
    viewModel.closeVotesForMeeting(meeting)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("Meeting votes could not be closed.", viewModel.uiState.value.errorMsg)
    assertEquals(1, repositoryMock.updateMeetingCallCount)
    assertEquals(0, repositoryMock.loadMeetingsCallCount)
  }
}

class MeetingRepositoryMockViewmodel : MeetingRepository {
  var meetingsToReturn: List<Meeting> = emptyList()
  var shouldThrowError = false
  var errorMessage = "An error occurred"
  var updateShouldFail = false

  var updatedMeeting: Meeting? = null
  var updateMeetingCallCount = 0
  var loadMeetingsCallCount = 0

  override fun getMeetingsForCurrentUser(skipCache: Boolean): Flow<List<Meeting>> {
    loadMeetingsCallCount++
    return if (shouldThrowError) {
      flow { throw Exception(errorMessage) }
    } else {
      flowOf(meetingsToReturn)
    }
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    updateMeetingCallCount++
    updatedMeeting = meeting
    return if (updateShouldFail) {
      Result.failure(Exception("Update failed"))
    } else {
      Result.success(Unit)
    }
  }

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> = flowOf(null)

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> = flowOf(emptyList())

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> =
      flowOf(emptyList())

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> = runCatching { "url" }

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
