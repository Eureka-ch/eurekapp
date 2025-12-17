/*
 * Note: This file was co-authored by Claude Code, Gemini and Grok.
 */

package ch.eureka.eurekapp.ui.meeting

import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import java.util.Date
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for MeetingDetailViewModel
 *
 * Note: This file was co-authored by Claude Code, Gemini and Grok.
 */
@ExperimentalCoroutinesApi
class MeetingDetailViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingDetailViewModel
  private lateinit var repositoryMock: MeetingDetailRepositoryMock
  private lateinit var projectRepositoryMock: MockProjectRepository
  private lateinit var userRepositoryMock: UserRepositoryMock
  private lateinit var connectivityObserverMock: MockConnectivityObserver

  private val testProjectId = "project123"
  private val testProjectName = "Test Project Name"
  private val testMeetingId = "meeting456"
  private val testCreatorId = "user1"

  private val projectFlow = MutableStateFlow<Project?>(null)

  private val testMeeting =
      Meeting(
          meetingID = testMeetingId,
          title = "Test Meeting",
          status = MeetingStatus.SCHEDULED,
          format = MeetingFormat.VIRTUAL,
          datetime = Timestamp(Date(System.currentTimeMillis() + 86400000)),
          link = "https://meet.test.com",
          location = null,
          createdBy = testCreatorId)

  private val testUser =
      User(
          uid = testCreatorId,
          displayName = "Test User",
          photoUrl = "https://example.com/photo.jpg")

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    repositoryMock = MeetingDetailRepositoryMock()
    userRepositoryMock = UserRepositoryMock()

    projectRepositoryMock =
        object : MockProjectRepository() {
          override fun getProjectById(projectId: String): Flow<Project?> = projectFlow
        }
    projectFlow.value = Project(projectId = testProjectId, name = testProjectName)

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    connectivityObserverMock = MockConnectivityObserver(context)
    connectivityObserverMock.setConnected(true)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel(currentUserId: String? = testCreatorId): MeetingDetailViewModel {
    return MeetingDetailViewModel(
        projectId = testProjectId,
        meetingId = testMeetingId,
        repository = repositoryMock,
        projectRepository = projectRepositoryMock,
        userRepository = userRepositoryMock,
        connectivityObserver = connectivityObserverMock,
        getCurrentUserId = { currentUserId })
  }

  @Test
  fun meetingDetailViewModel_initialStateIsCorrect() {
    viewModel = createViewModel()

    val uiState = viewModel.uiState.value
    assertNull(uiState.meeting)
    assertNull(uiState.errorMsg)
    assertTrue(uiState.isLoading)
    assertFalse(uiState.deleteSuccess)
    assertNull(uiState.meetingProjectName)
    assertNull(uiState.creatorUser)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    userRepositoryMock.userToReturn.value = testUser
    projectFlow.value = Project(projectId = testProjectId, name = testProjectName)

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.meeting)
    assertEquals(testMeetingId, uiState.meeting?.meetingID)
    assertEquals("Test Meeting", uiState.meeting?.title)

    assertEquals(testProjectName, uiState.meetingProjectName)

    assertNotNull(uiState.creatorUser)
    assertEquals(testCreatorId, uiState.creatorUser?.uid)
    assertEquals("Test User", uiState.creatorUser?.displayName)

    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsHandlesNullMeeting() = runTest {
    repositoryMock.meetingToReturn.value = null
    userRepositoryMock.userToReturn.value = null

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertNull(uiState.creatorUser)
    assertEquals("Meeting not found", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsHandlesError() = runTest {
    val errorMessage = "Network error loading meeting"
    repositoryMock.shouldThrowMeetingError = true
    repositoryMock.meetingErrorMessage = errorMessage

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertTrue(uiState.errorMsg!!.contains(errorMessage))
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsRejectsInvalidTitle() = runTest {
    val invalidMeeting = testMeeting.copy(title = "")
    repositoryMock.meetingToReturn.value = invalidMeeting
    userRepositoryMock.userToReturn.value = testUser

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Meeting has invalid title", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsRejectsInPersonMeetingWithoutLocation() = runTest {
    val invalidMeeting = testMeeting.copy(format = MeetingFormat.IN_PERSON, location = null)
    repositoryMock.meetingToReturn.value = invalidMeeting
    userRepositoryMock.userToReturn.value = testUser

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("In-person meeting must have a location", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsRejectsVirtualMeetingWithoutLink() = runTest {
    val invalidMeeting = testMeeting.copy(format = MeetingFormat.VIRTUAL, link = null)
    repositoryMock.meetingToReturn.value = invalidMeeting
    userRepositoryMock.userToReturn.value = testUser

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Virtual meeting must have a link", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsUpdatesWhenDataChanges() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    userRepositoryMock.userToReturn.value = testUser

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertEquals("Test Meeting", initialState.meeting?.title)
    assertEquals("Test User", initialState.creatorUser?.displayName)

    val updatedMeeting = testMeeting.copy(title = "Updated Meeting Title")
    val updatedUser = testUser.copy(displayName = "Updated User")

    repositoryMock.meetingToReturn.value = updatedMeeting
    userRepositoryMock.userToReturn.value = updatedUser
    testDispatcher.scheduler.advanceUntilIdle()

    val updatedState = viewModel.uiState.value
    assertEquals("Updated Meeting Title", updatedState.meeting?.title)
    assertEquals("Updated User", updatedState.creatorUser?.displayName)
  }

  @Test
  fun meetingDetailViewModel_deleteMeetingSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.deleteResult = Result.success(Unit)

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.deleteMeeting(testProjectId, testMeetingId, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertTrue(uiState.deleteSuccess)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_deleteMeetingHandlesFailure() = runTest {
    val errorMessage = "Failed to delete meeting"
    repositoryMock.deleteResult = Result.failure(Exception(errorMessage))

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    viewModel.deleteMeeting(testProjectId, testMeetingId, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertFalse(uiState.deleteSuccess)
    assertNotNull(uiState.errorMsg)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_deleteMeetingResetsLoadingStateAfterCompletion() = runTest {
    repositoryMock.deleteResult = Result.success(Unit)

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    viewModel.deleteMeeting(testProjectId, testMeetingId, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val finalState = viewModel.uiState.value
    assertFalse(finalState.isLoading)
    assertTrue(finalState.deleteSuccess)
  }

  @Test
  fun meetingDetailViewModel_clearErrorMsgSetsErrorMsgToNull() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val errorMessage = "Delete failed"
    repositoryMock.deleteResult = Result.failure(Exception(errorMessage))
    viewModel.deleteMeeting(testProjectId, testMeetingId, true)
    testDispatcher.scheduler.advanceUntilIdle()

    assertNotNull(viewModel.uiState.value.errorMsg)
    assertEquals(errorMessage, viewModel.uiState.value.errorMsg)

    viewModel.clearErrorMsg()
    testDispatcher.scheduler.advanceUntilIdle()

    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_clearErrorMsgDoesNotAffectOtherStateProperties() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val errorMessage = "Delete operation failed"
    repositoryMock.deleteResult = Result.failure(Exception(errorMessage))
    viewModel.deleteMeeting(testProjectId, testMeetingId, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val stateBeforeClear = viewModel.uiState.value
    assertNotNull(stateBeforeClear.errorMsg)
    assertEquals(errorMessage, stateBeforeClear.errorMsg)

    viewModel.clearErrorMsg()
    testDispatcher.scheduler.advanceUntilIdle()

    val stateAfterClear = viewModel.uiState.value
    assertNull(stateAfterClear.errorMsg)
    assertEquals(stateBeforeClear.isLoading, stateAfterClear.isLoading)
    assertEquals(stateBeforeClear.deleteSuccess, stateAfterClear.deleteSuccess)
  }

  @Test
  fun meetingDetailViewModel_deleteSuccessPreservedAcrossFlowUpdates() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    repositoryMock.deleteResult = Result.success(Unit)
    viewModel.deleteMeeting(testProjectId, testMeetingId, true)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.deleteSuccess)

    repositoryMock.meetingToReturn.value = testMeeting.copy(title = "Updated")
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.deleteSuccess)
  }

  @Test
  fun meetingDetailViewModel_toggleEditModeEntersEditMode() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isEditMode)

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertTrue(uiState.isEditMode)
    assertEquals(testMeeting.title, uiState.editTitle)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.updateResult = Result.success(Unit)

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.updateEditTitle("Updated Title")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertTrue(uiState.updateSuccess)
    assertFalse(uiState.isEditMode)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesHandlesFailure() = runTest {
    val errorMessage = "Failed to update meeting"
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.updateResult = Result.failure(Exception(errorMessage))

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.updateEditTitle("Updated Title")

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.updateSuccess)
    assertTrue(uiState.isEditMode)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_startMeetingSuccessfully() = runTest {
    val meeting = testMeeting.copy(status = MeetingStatus.SCHEDULED)
    repositoryMock.meetingToReturn.value = meeting
    repositoryMock.updateResult = Result.success(Unit)

    viewModel = createViewModel(testCreatorId)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.startMeeting(meeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    // Assert the flow updated via the mock logic (see Mock below)
    assertEquals(MeetingStatus.IN_PROGRESS, repositoryMock.meetingToReturn.value?.status)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_startMeetingFailsWhenOffline() = runTest {
    val meeting = testMeeting.copy(status = MeetingStatus.SCHEDULED)
    repositoryMock.meetingToReturn.value = meeting

    viewModel = createViewModel()
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.startMeeting(meeting, false)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("Cannot start meeting while offline", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_startMeetingFailsWhenNotCreator() = runTest {
    val meeting = testMeeting.copy(status = MeetingStatus.SCHEDULED, createdBy = "otherUser")
    repositoryMock.meetingToReturn.value = meeting

    viewModel = createViewModel(testCreatorId)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.startMeeting(meeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("Only the meeting creator can start the meeting", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_endMeetingSuccessfully() = runTest {
    val meeting = testMeeting.copy(status = MeetingStatus.IN_PROGRESS)
    repositoryMock.meetingToReturn.value = meeting
    repositoryMock.updateResult = Result.success(Unit)

    viewModel = createViewModel(testCreatorId)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.endMeeting(meeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(MeetingStatus.COMPLETED, repositoryMock.meetingToReturn.value?.status)
    assertNull(viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_endMeetingFailsWhenNotInProgress() = runTest {
    val meeting = testMeeting.copy(status = MeetingStatus.SCHEDULED)
    repositoryMock.meetingToReturn.value = meeting

    viewModel = createViewModel(testCreatorId)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.endMeeting(meeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals("Meeting can only be ended if it is in progress", viewModel.uiState.value.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_shouldMeetingBeStartedReturnsTrueWhenScheduledAndPastStartTime() =
      runTest {
        val pastDateTime = Timestamp(Date(System.currentTimeMillis() - 3600000)) // 1 hour ago
        val meeting = testMeeting.copy(status = MeetingStatus.SCHEDULED, datetime = pastDateTime)
        repositoryMock.meetingToReturn.value = meeting

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.shouldMeetingBeStarted(meeting))
      }

  @Test
  fun meetingDetailViewModel_shouldMeetingBeEndedReturnsTrueWhenInProgressAndPastEndTime() =
      runTest {
        val startDateTime = Timestamp(Date(System.currentTimeMillis() - 7200000)) // 2 hours ago
        val meeting =
            testMeeting.copy(
                status = MeetingStatus.IN_PROGRESS, datetime = startDateTime, duration = 60)
        repositoryMock.meetingToReturn.value = meeting

        viewModel = createViewModel()
        backgroundScope.launch { viewModel.uiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.shouldMeetingBeEnded(meeting))
      }
}

/** Mock repository for MeetingDetailViewModel tests with controllable flows. */
class MeetingDetailRepositoryMock : MeetingRepository {
  val meetingToReturn = MutableStateFlow<Meeting?>(null)
  var shouldThrowMeetingError = false
  var meetingErrorMessage = "Meeting error"
  var deleteResult: Result<Unit> = Result.success(Unit)
  var updateResult: Result<Unit> = Result.success(Unit)

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    return if (shouldThrowMeetingError) {
      flow { throw Exception(meetingErrorMessage) }
    } else {
      meetingToReturn
    }
  }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
    return flowOf(emptyList())
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
    return deleteResult
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    if (updateResult.isSuccess) {
      meetingToReturn.value = meeting
    }
    return updateResult
  }

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> = flowOf(emptyList())

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> =
      flowOf(emptyList())

  override fun getMeetingsForCurrentUser(skipCache: Boolean): Flow<List<Meeting>> =
      flowOf(emptyList())

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> = Result.success("test-url")

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

/** Mock user repository for MeetingDetailViewModel tests with controllable flows. */
class UserRepositoryMock : UserRepository {
  val userToReturn = MutableStateFlow<User?>(null)
  var shouldThrowError = false
  var errorMessage = "User error"

  override fun getUserById(userId: String): Flow<User?> {
    return if (shouldThrowError) {
      flow { throw Exception(errorMessage) }
    } else {
      userToReturn
    }
  }

  override fun getCurrentUser(): Flow<User?> = flow { emit(null) }

  override suspend fun saveUser(user: User): Result<Unit> = Result.success(Unit)

  override suspend fun updateLastActive(userId: String): Result<Unit> = Result.success(Unit)

  override suspend fun updateFcmToken(userId: String, fcmToken: String): Result<Unit> =
      Result.success(Unit)
}
