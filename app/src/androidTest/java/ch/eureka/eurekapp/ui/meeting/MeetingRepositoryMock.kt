/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

open class MeetingRepositoryMock : MeetingRepository {
  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    val meeting = Meeting(projectId = projectId, meetingID = meetingId)
    return flow { emit(meeting) }
  }

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
    return flow { emit(MeetingProvider.sampleMeetings) }
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
    return runCatching { "url" }
  }

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
    return runCatching { Unit }
  }

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
    return runCatching { Unit }
  }

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
    val meeting = Meeting(projectId = projectId, meetingID = meetingId)
    return flow { emit(emptyList()) }
  }

  override suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> {
    return runCatching { Unit }
  }

  override suspend fun removeParticipant(
      projectId: String,
      meetingId: String,
      userId: String
  ): Result<Unit> {
    return runCatching { Unit }
  }

  override suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit> {
    return runCatching { Unit }
  }
}
