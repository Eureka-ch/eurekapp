/*
 * Note: This file was co-authored by Claude Code.
 */

package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.*
import com.google.firebase.Timestamp
import java.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Test suite for MeetingDetailViewModel
 *
 * Tests all ViewModel functionality including:
 * - Initial state validation
 * - Meeting detail loading with real-time Flow updates
 * - Error handling for loading operations
 * - Delete meeting functionality (success and failure)
 * - Error message clearing
 *
 * Note: This file was co-authored by Claude Code.
 */
@ExperimentalCoroutinesApi
class MeetingDetailViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingDetailViewModel
  private lateinit var repositoryMock: MeetingDetailRepositoryMock
  private val testProjectId = "project123"
  private val testMeetingId = "meeting456"

  private val testMeeting =
      Meeting(
          meetingID = testMeetingId,
          title = "Test Meeting",
          status = MeetingStatus.SCHEDULED,
          format = MeetingFormat.VIRTUAL,
          datetime = Timestamp(Date()),
          link = "https://meet.test.com",
          location = null)

  private val testParticipants =
      listOf(
          Participant(userId = "user1", role = MeetingRole.HOST),
          Participant(userId = "user2", role = MeetingRole.PARTICIPANT))

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MeetingDetailRepositoryMock()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun initialStateIsCorrect() {
    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)

    val uiState = viewModel.uiState.value
    assertNull(uiState.meeting)
    assertTrue(uiState.participants.isEmpty())
    assertNull(uiState.errorMsg)
    assertTrue(uiState.isLoading)
    assertFalse(uiState.deleteSuccess)
  }

  @Test
  fun loadMeetingDetailsSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.meeting)
    assertEquals(testMeetingId, uiState.meeting?.meetingID)
    assertEquals("Test Meeting", uiState.meeting?.title)
    assertEquals(2, uiState.participants.size)
    assertEquals("user1", uiState.participants[0].userId)
    assertEquals(MeetingRole.HOST, uiState.participants[0].role)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun loadMeetingDetailsHandlesNullMeeting() = runTest {
    repositoryMock.meetingToReturn.value = null
    repositoryMock.participantsToReturn.value = emptyList()

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertTrue(uiState.participants.isEmpty())
    assertEquals("Meeting not found", uiState.errorMsg)
  }

  @Test
  fun loadMeetingDetailsHandlesError() = runTest {
    val errorMessage = "Network error loading meeting"
    repositoryMock.shouldThrowMeetingError = true
    repositoryMock.meetingErrorMessage = errorMessage

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertTrue(uiState.errorMsg!!.contains(errorMessage))
  }

  @Test
  fun loadMeetingDetailsHandlesParticipantsError() = runTest {
    val errorMessage = "Error loading participants"
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.shouldThrowParticipantsError = true
    repositoryMock.participantsErrorMessage = errorMessage

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertTrue(uiState.errorMsg!!.contains(errorMessage))
  }

  @Test
  fun loadMeetingDetailsUpdatesWhenDataChanges() = runTest {
    // Initial data
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertEquals("Test Meeting", initialState.meeting?.title)
    assertEquals(2, initialState.participants.size)

    // Update data (simulating real-time Firestore updates)
    val updatedMeeting = testMeeting.copy(title = "Updated Meeting Title")
    val updatedParticipants =
        listOf(
            Participant(userId = "user1", role = MeetingRole.HOST),
            Participant(userId = "user2", role = MeetingRole.PARTICIPANT),
            Participant(userId = "user3", role = MeetingRole.PARTICIPANT))

    repositoryMock.meetingToReturn.value = updatedMeeting
    repositoryMock.participantsToReturn.value = updatedParticipants
    testDispatcher.scheduler.advanceUntilIdle()

    val updatedState = viewModel.uiState.value
    assertEquals("Updated Meeting Title", updatedState.meeting?.title)
    assertEquals(3, updatedState.participants.size)
  }

  @Test
  fun deleteMeetingSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants
    repositoryMock.deleteResult = Result.success(Unit)

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.deleteMeeting(testProjectId, testMeetingId)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertTrue(uiState.deleteSuccess)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun deleteMeetingHandlesFailure() = runTest {
    val errorMessage = "Failed to delete meeting"
    repositoryMock.deleteResult = Result.failure(Exception(errorMessage))

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    viewModel.deleteMeeting(testProjectId, testMeetingId)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertFalse(uiState.deleteSuccess)
    assertNotNull(uiState.errorMsg)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun deleteMeetingResetsLoadingStateAfterCompletion() = runTest {
    repositoryMock.deleteResult = Result.success(Unit)

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    viewModel.deleteMeeting(testProjectId, testMeetingId)
    testDispatcher.scheduler.advanceUntilIdle()

    // After completion, loading should be false
    val finalState = viewModel.uiState.value
    assertFalse(finalState.isLoading)
    assertTrue(finalState.deleteSuccess)
  }

  @Test
  fun clearErrorMsgSetsErrorMsgToNull() = runTest {
    // Set up meeting data
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger an error via delete failure
    val errorMessage = "Delete failed"
    repositoryMock.deleteResult = Result.failure(Exception(errorMessage))
    viewModel.deleteMeeting(testProjectId, testMeetingId)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.errorMsg)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)

    // Clear the error
    viewModel.clearErrorMsg()
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun clearErrorMsgDoesNotAffectOtherStateProperties() = runTest {
    // Load meeting data first
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    // Trigger an error via delete failure
    val errorMessage = "Delete operation failed"
    repositoryMock.deleteResult = Result.failure(Exception(errorMessage))
    viewModel.deleteMeeting(testProjectId, testMeetingId)
    testDispatcher.scheduler.advanceUntilIdle()

    val stateBeforeClear = viewModel.uiState.value
    assertNotNull(stateBeforeClear.errorMsg)
    assertEquals(errorMessage, stateBeforeClear.errorMsg)

    // Clear error
    viewModel.clearErrorMsg()
    testDispatcher.scheduler.advanceUntilIdle()

    val stateAfterClear = viewModel.uiState.value
    assertNull(stateAfterClear.errorMsg)
    // Other properties should remain unchanged
    assertEquals(stateBeforeClear.isLoading, stateAfterClear.isLoading)
    assertEquals(stateBeforeClear.deleteSuccess, stateAfterClear.deleteSuccess)
  }

  @Test
  fun deleteSuccessPreservedAcrossFlowUpdates() = runTest {
    // Load meeting
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel = MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock)
    backgroundScope.launch { viewModel.uiState.collect { } }
    testDispatcher.scheduler.advanceUntilIdle()

    // Delete meeting
    repositoryMock.deleteResult = Result.success(Unit)
    viewModel.deleteMeeting(testProjectId, testMeetingId)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.deleteSuccess)

    // Simulate Flow update
    repositoryMock.meetingToReturn.value = testMeeting.copy(title = "Updated")
    testDispatcher.scheduler.advanceUntilIdle()

    // deleteSuccess should still be true
    assertTrue(viewModel.uiState.value.deleteSuccess)
  }
}

/** Mock repository for MeetingDetailViewModel tests with controllable flows. */
class MeetingDetailRepositoryMock : MeetingRepository {
  val meetingToReturn = MutableStateFlow<Meeting?>(null)
  val participantsToReturn = MutableStateFlow<List<Participant>>(emptyList())
  var shouldThrowMeetingError = false
  var meetingErrorMessage = "Meeting error"
  var shouldThrowParticipantsError = false
  var participantsErrorMessage = "Participants error"
  var deleteResult: Result<Unit> = Result.success(Unit)

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    return if (shouldThrowMeetingError) {
      flow { throw Exception(meetingErrorMessage) }
    } else {
      meetingToReturn
    }
  }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
    return if (shouldThrowParticipantsError) {
      flow { throw Exception(participantsErrorMessage) }
    } else {
      participantsToReturn
    }
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
    return deleteResult
  }

  // Unused methods for this test suite
  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> = flowOf(emptyList())

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
  ): Result<String> = Result.success("test-url")

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = Result.success(Unit)

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = Result.success(Unit)

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = Result.success(Unit)

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = Result.success(Unit)
}
