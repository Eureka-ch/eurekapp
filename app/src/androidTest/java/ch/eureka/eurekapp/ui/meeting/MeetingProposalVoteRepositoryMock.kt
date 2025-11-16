/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * A specific mock repository for the [MeetingProposalVoteViewModelTest].
 *
 * It extends [BaseMockMeetingRepository] and overrides only the methods used by
 * [MeetingProposalVoteViewModel]:
 * - [getMeetingById]
 * - [updateMeeting]
 *
 * Note: this file was written with the help of Gemini
 */
class MeetingProposalVoteRepositoryMock : BaseMockMeetingRepository() {

  // --- State for getMeetingById ---
  private var meetingFlow: Flow<Meeting?> = flowOf(null)
  var meetingLoadException: Exception? = null

  /** Configures the mock [getMeetingById] to return a specific meeting. */
  fun setMeetingToReturn(meeting: Meeting?) {
    meetingLoadException = null
    meetingFlow = flowOf(meeting)
  }

  /** Configures the mock [getMeetingById] to throw an exception. */
  fun setMeetingLoadToFail(exception: Exception) {
    meetingLoadException = exception
    meetingFlow = flow { throw exception }
  }

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    return meetingFlow
  }

  // --- State for updateMeeting ---
  var updateShouldSucceed = true
  var updateFailureException = Exception("Generic update error")
  var lastMeetingUpdated: Meeting? = null

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    lastMeetingUpdated = meeting
    return if (updateShouldSucceed) {
      Result.success(Unit)
    } else {
      Result.failure(updateFailureException)
    }
  }
}

/**
 * A base mock implementation of [MeetingRepository] that provides default empty implementations for
 * all methods. Tests can extend this to override only the methods they need.
 */
open class BaseMockMeetingRepository : MeetingRepository {
  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> = flowOf(null)

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
  ): Result<String> = Result.failure(NotImplementedError("Not mocked"))

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> =
      Result.failure(NotImplementedError("Not mocked"))

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> =
      Result.failure(NotImplementedError("Not mocked"))

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> =
      flowOf(emptyList())

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = Result.failure(NotImplementedError("Not mocked"))

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> = Result.failure(NotImplementedError("Not mocked"))

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> = Result.failure(NotImplementedError("Not mocked"))
}
