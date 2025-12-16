/*
 * Note: This file was co-authored by Claude Code, Gemini and Grok.
 */

package ch.eureka.eurekapp.ui.meeting

import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.meeting.Participant
import ch.eureka.eurekapp.model.data.project.Project
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
 * Tests all ViewModel functionality including:
 * - Initial state validation
 * - Meeting detail loading with real-time Flow updates
 * - Error handling for loading operations
 * - Delete meeting functionality (success and failure)
 * - Error message clearing
 *
 * Note: This file was co-authored by Claude Code, Gemini and Grok.
 */
@ExperimentalCoroutinesApi
class MeetingDetailViewModelTest {

  private val testDispatcher = StandardTestDispatcher()
  private lateinit var viewModel: MeetingDetailViewModel
  private lateinit var repositoryMock: MeetingDetailRepositoryMock
  private lateinit var projectRepositoryMock: MockProjectRepository
  private val testProjectId = "project123"
  private val testProjectName = "Test Project Name"
  private val testMeetingId = "meeting456"

  private val projectFlow = MutableStateFlow<Project?>(null)

  private val testMeeting =
      Meeting(
          meetingID = testMeetingId,
          title = "Test Meeting",
          status = MeetingStatus.SCHEDULED,
          format = MeetingFormat.VIRTUAL,
          datetime = Timestamp(Date(System.currentTimeMillis() + 86400000)), // Tomorrow
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

    projectRepositoryMock =
        object : MockProjectRepository() {
          override fun getProjectById(projectId: String): Flow<Project?> = projectFlow
        }

    projectFlow.value = Project(projectId = testProjectId, name = testProjectName)

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun meetingDetailViewModel_initialStateIsCorrect() {
    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)

    val uiState = viewModel.uiState.value
    assertNull(uiState.meeting)
    assertTrue(uiState.participants.isEmpty())
    assertNull(uiState.errorMsg)
    assertTrue(uiState.isLoading)
    assertFalse(uiState.deleteSuccess)
    assertEquals(null, uiState.meetingProjectName)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants
    projectFlow.value = Project(projectId = testProjectId, name = testProjectName)

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
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

    assertEquals(testProjectName, uiState.meetingProjectName)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsHandlesNullMeeting() = runTest {
    repositoryMock.meetingToReturn.value = null
    repositoryMock.participantsToReturn.value = emptyList()

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertTrue(uiState.participants.isEmpty())
    assertEquals("Meeting not found", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsHandlesError() = runTest {
    val errorMessage = "Network error loading meeting"
    repositoryMock.shouldThrowMeetingError = true
    repositoryMock.meetingErrorMessage = errorMessage

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNull(uiState.meeting)
    assertEquals("Virtual meeting must have a link", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsHandlesParticipantsError() = runTest {
    val errorMessage = "Error loading participants"
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.shouldThrowParticipantsError = true
    repositoryMock.participantsErrorMessage = errorMessage

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isLoading)
    assertNotNull(uiState.errorMsg)
    assertTrue(uiState.errorMsg!!.contains(errorMessage))
  }

  @Test
  fun meetingDetailViewModel_loadMeetingDetailsUpdatesWhenDataChanges() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    val initialState = viewModel.uiState.value
    assertEquals("Test Meeting", initialState.meeting?.title)
    assertEquals(2, initialState.participants.size)

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
  fun meetingDetailViewModel_deleteMeetingSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants
    repositoryMock.deleteResult = Result.success(Unit)

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
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
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isEditMode)

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertTrue(uiState.isEditMode)
    assertEquals(testMeeting.title, uiState.editTitle)
    assertEquals(testMeeting.datetime, uiState.editDateTime)
    assertEquals(testMeeting.duration, uiState.editDuration)
  }

  @Test
  fun meetingDetailViewModel_toggleEditModeExitsEditModeAndResetsFields() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()
    assertTrue(viewModel.uiState.value.isEditMode)

    viewModel.toggleEditMode(null, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.isEditMode)
    assertEquals("", uiState.editTitle)
    assertNull(uiState.editDateTime)
    assertEquals(30, uiState.editDuration)
  }

  @Test
  fun meetingDetailViewModel_updateEditTitleUpdatesState() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val newTitle = "Updated Meeting Title"
    viewModel.updateEditTitle(newTitle)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(newTitle, viewModel.uiState.value.editTitle)
  }

  @Test
  fun meetingDetailViewModel_updateEditDateTimeUpdatesState() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val newDateTime = Timestamp(Date(System.currentTimeMillis() + 86400000))
    viewModel.updateEditDateTime(newDateTime)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(newDateTime, viewModel.uiState.value.editDateTime)
  }

  @Test
  fun meetingDetailViewModel_updateEditDurationUpdatesState() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val newDuration = 60
    viewModel.updateEditDuration(newDuration)
    testDispatcher.scheduler.advanceUntilIdle()

    assertEquals(newDuration, viewModel.uiState.value.editDuration)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesSuccessfully() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants
    repositoryMock.updateResult = Result.success(Unit)

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.updateEditTitle("Updated Title")
    viewModel.updateEditDuration(60)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertTrue(uiState.updateSuccess)
    assertFalse(uiState.isEditMode)
    assertFalse(uiState.isSaving)
    assertNull(uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesHandlesFailure() = runTest {
    val errorMessage = "Failed to update meeting"
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants
    repositoryMock.updateResult = Result.failure(Exception(errorMessage))

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.updateEditTitle("Updated Title")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.updateSuccess)
    assertTrue(uiState.isEditMode)
    assertFalse(uiState.isSaving)
    assertEquals(errorMessage, uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesRejectsBlankTitle() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.updateEditTitle("")
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.updateSuccess)
    assertEquals("Title cannot be empty", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesRejectsNullDateTime() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.updateEditDateTime(null)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.updateSuccess)
    assertEquals("Date and time must be set", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesRejectsNegativeDuration() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.updateEditDuration(0)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.updateSuccess)
    assertEquals("Duration must be greater than 0", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_saveMeetingChangesRejectsPastDateTime() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    val yesterday = Timestamp(Date(System.currentTimeMillis() - 86400000))
    viewModel.updateEditDateTime(yesterday)
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    val uiState = viewModel.uiState.value
    assertFalse(uiState.updateSuccess)
    assertEquals("Meeting should be scheduled in the future.", uiState.errorMsg)
  }

  @Test
  fun meetingDetailViewModel_clearUpdateSuccessSetsUpdateSuccessToFalse() = runTest {
    repositoryMock.meetingToReturn.value = testMeeting
    repositoryMock.participantsToReturn.value = testParticipants
    repositoryMock.updateResult = Result.success(Unit)

    viewModel =
        MeetingDetailViewModel(testProjectId, testMeetingId, repositoryMock, projectRepositoryMock)
    backgroundScope.launch { viewModel.uiState.collect {} }
    testDispatcher.scheduler.advanceUntilIdle()

    viewModel.toggleEditMode(testMeeting, true)
    viewModel.saveMeetingChanges(testMeeting, true)
    testDispatcher.scheduler.advanceUntilIdle()

    assertTrue(viewModel.uiState.value.updateSuccess)

    viewModel.clearUpdateSuccess()
    testDispatcher.scheduler.advanceUntilIdle()

    assertFalse(viewModel.uiState.value.updateSuccess)
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
  var updateResult: Result<Unit> = Result.success(Unit)

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

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = updateResult

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
