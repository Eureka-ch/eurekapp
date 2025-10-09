package ch.eureka.eurekapp.model.meeting

import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
  /** Get meeting by ID with real-time updates */
  fun getMeetingById(workspaceId: String, meetingId: String): Flow<Meeting?>

  /** Get all meetings in workspace with real-time updates */
  fun getMeetingsInWorkspace(workspaceId: String): Flow<List<Meeting>>

  /**
   * Get meetings for specific context (workspace, group, project, or task) with real-time updates
   */
  fun getMeetingsForContext(
      workspaceId: String,
      contextId: String,
      contextType: ContextType
  ): Flow<List<Meeting>>

  /** Get meetings where current user is a participant with real-time updates */
  fun getMeetingsForCurrentUser(workspaceId: String): Flow<List<Meeting>>

  /** Create a new meeting */
  suspend fun createMeeting(meeting: Meeting): Result<String>

  /** Update meeting */
  suspend fun updateMeeting(meeting: Meeting): Result<Unit>

  /** Delete meeting */
  suspend fun deleteMeeting(workspaceId: String, meetingId: String): Result<Unit>

  /** Add participant to meeting */
  suspend fun addParticipant(
      workspaceId: String,
      meetingId: String,
      userId: String,
      role: String
  ): Result<Unit>

  /** Remove participant from meeting */
  suspend fun removeParticipant(
      workspaceId: String,
      meetingId: String,
      userId: String
  ): Result<Unit>
}
