/* Portions of this code and documentation were generated with the help of AI (ChatGPT 5.1) and Gemini. */
package ch.eureka.eurekapp.ui.home

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Simple configurable implementation of [MeetingRepository] for unit tests.
 *
 * Allows tests to configure project-specific meeting flows without touching Firestore.
 */
class MockMeetingRepository : MeetingRepository {
  private val meetingsByProject = mutableMapOf<String, Flow<List<Meeting>>>()
  private val meetingsById = mutableMapOf<Pair<String, String>, Meeting?>()
  private val userMeetings = MutableStateFlow<List<Meeting>>(emptyList())

  fun setMeetingsForCurrentUser(meetings: List<Meeting>) {
    userMeetings.value = meetings
  }

  override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
    return flowOf(meetingsById[projectId to meetingId])
  }

  override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
    return meetingsByProject[projectId] ?: flowOf(emptyList())
  }

  override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> =
      flowOf(emptyList())

  override fun getMeetingsForCurrentUser(skipCache: Boolean): Flow<List<Meeting>> = userMeetings

  override suspend fun createMeeting(
      meeting: Meeting,
      creatorId: String,
      creatorRole: MeetingRole
  ): Result<String> = Result.success("mock-meeting-id")

  override suspend fun updateMeeting(meeting: Meeting): Result<Unit> = Result.success(Unit)

  override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> =
      Result.success(Unit)

  override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> =
      flowOf(emptyList())

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
