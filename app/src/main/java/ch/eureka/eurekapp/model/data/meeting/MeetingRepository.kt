package ch.eureka.eurekapp.model.data.meeting

import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
  /** Get meeting by ID with real-time updates */
  fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?>

  /** Get all meetings in project with real-time updates */
  fun getMeetingsInProject(projectId: String): Flow<List<Meeting>>

  /** Get meetings for specific task with real-time updates */
  fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>>

  /** Get meetings where current user is a participant with real-time updates */
  fun getMeetingsForCurrentUser(projectId: String, skipCache: Boolean = true): Flow<List<Meeting>>

  /** Create a new meeting */
  suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole = MeetingRole.HOST
  ): Result<String>

  /** Update meeting */
  suspend fun updateMeeting(meeting: Meeting): Result<Unit>

  /** Delete meeting */
  suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit>

  /** Get participants of a meeting with real-time updates */
  fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>>

  /** Add participant to meeting */
  suspend fun addParticipant(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit>

  /** Remove participant from meeting */
  suspend fun removeParticipant(projectId: String, meetingId: String, userId: String): Result<Unit>

  /** Update participant role */
  suspend fun updateParticipantRole(
      projectId: String,
      meetingId: String,
      userId: String,
      role: MeetingRole
  ): Result<Unit>
}
