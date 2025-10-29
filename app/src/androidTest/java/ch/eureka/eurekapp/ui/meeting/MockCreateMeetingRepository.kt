package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Mock repository for [CreateMeetingViewModelTest].
 *
 * This mock inherits from the user-provided [CreateMeetingRepositoryMock] to get default
 * implementations for unused methods and overrides the one we need: [createMeeting].
 */
class MockCreateMeetingRepository : CreateMeetingRepositoryMock() {

  var shouldSucceed = true
  var failureException = Exception("Generic repository error")

  // Properties to inspect what was passed to the mock
  var lastMeetingCreated: Meeting? = null
  var lastCreatorId: String? = null
  var lastCreatorRole: MeetingRole? = null

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> {
    lastMeetingCreated = meeting
    lastCreatorId = creatorId
    lastCreatorRole = creatorRole

    return if (shouldSucceed) {
      Result.success(meeting.meetingID)
    } else {
      Result.failure(failureException)
    }
  }
}

/**
 * Base mock repository provided in the user's prompt. [MockCreateMeetingRepository] extends this.
 */
open class CreateMeetingRepositoryMock : MeetingRepository {
  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    val meeting = Meeting(projectId = projectId, meetingID = meetingId)
    return flow { emit(meeting) }
  }

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
    return flow { emit(emptyList()) }
  }

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
    val meeting = Meeting(projectId = projectId, taskId = taskId)
    return flow { emit(listOf(meeting)) }
  }

  override fun getMeetingsForCurrentUser(
      projectId: String,
      skipCache: Boolean
  ): Flow<List<Meeting>> {
    val meeting =
        Meeting(
            projectId = projectId,
        )
    return flow { emit(listOf(meeting)) }
  }

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> {
    return runCatching { "default-mock-url" }
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    return runCatching {}
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
    return runCatching {}
  }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
    return flow { emit(emptyList()) }
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
