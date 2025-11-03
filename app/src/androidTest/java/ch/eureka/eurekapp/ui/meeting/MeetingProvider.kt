package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.map.Location
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingFormatVote
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import com.google.firebase.Timestamp
import java.util.Calendar

/**
 * Provides a comprehensive list of fake meetings for testing and preview purposes. This object is
 * updated to conform to the latest Meeting data class structure.
 *
 * Note: this code was generated and updated by Gemini.
 */
object MeetingProvider {

  // Helper function to create Timestamps easily
  private fun createTimestamp(year: Int, month: Int, day: Int, hour: Int, minute: Int): Timestamp {
    val calendar = Calendar.getInstance()
    calendar.set(year, month - 1, day, hour, minute, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return Timestamp(calendar.time)
  }

  // --- Common Test Data ---
  private val userIds =
      listOf("user_anna_1", "user_ben_2", "user_charlie_3", "user_diana_4", "user_ethan_5")
  private val projectIds = listOf("proj_apollo_x", "proj_bravo_y", "proj_charon_z")
  private val taskIds = listOf("task_ui_refresh_11", "task_db_migration_22", "task_api_docs_33")

  private val lausanneOffice =
      Location(latitude = 46.5197, longitude = 6.6323, name = "Lausanne Office")
  private val genevaClientHQ =
      Location(latitude = 46.2044, longitude = 6.1432, name = "Geneva Client HQ")
  private val zurichHub =
      Location(latitude = 47.3769, longitude = 8.5417, name = "Zurich Innovation Hub")

  // --- Generated Meeting List ---
  val sampleMeetings: List<Meeting> =
      listOf(
          // 1. OPEN_TO_VOTES: A typical meeting proposal where members can vote.
          // Note: `datetime`, `format`, `location`, `link` must be null.
          Meeting(
              meetingID = "meet_vote_01",
              projectId = projectIds[0],
              taskId = taskIds[0],
              title = "Apollo UI Refresh Kick-off",
              status = MeetingStatus.OPEN_TO_VOTES,
              duration = 60,
              // `timeSlot` property removed
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 10, 20, 10, 0) to 2, // user 0, 2
                      createTimestamp(2025, 10, 20, 11, 0) to 1, // user 1
                      createTimestamp(2025, 10, 20, 14, 0) to 1 // user 1
                      ),
              formatVotes =
                  listOf(
                      MeetingFormatVote(userIds[0], MeetingFormat.VIRTUAL),
                      MeetingFormatVote(userIds[1], MeetingFormat.IN_PERSON),
                      MeetingFormatVote(userIds[2], MeetingFormat.VIRTUAL)),
              datetime = null,
              format = null,
              location = null,
              link = null,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[1], userIds[2])),

          // 2. SCHEDULED (Virtual): A standard upcoming virtual meeting.
          Meeting(
              meetingID = "meet_scheduled_virtual_02",
              projectId = projectIds[1],
              title = "Bravo Project Weekly Sync",
              status = MeetingStatus.SCHEDULED,
              duration = 30,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 10, 20, 10, 0) to 2, // user 0, 2
                      createTimestamp(2025, 11, 20, 21, 0) to 1, // user 1
                  ),
              datetime = createTimestamp(2025, 10, 17, 15, 0), // This Friday 3 PM
              format = MeetingFormat.VIRTUAL,
              link = "https://meet.google.com/abc-defg-hij",
              createdBy = userIds[1],
              participantIds = listOf(userIds[1], userIds[3], userIds[4])),

          // 3. SCHEDULED (In-Person): A standard upcoming physical meeting.
          Meeting(
              meetingID = "meet_scheduled_inperson_03",
              projectId = projectIds[0],
              taskId = taskIds[1],
              title = "Database Migration Strategy",
              status = MeetingStatus.SCHEDULED,
              duration = 90,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 10, 20, 10, 0) to 2,
                  ),
              datetime = createTimestamp(2025, 10, 22, 10, 30), // Next Wednesday 10:30 AM
              format = MeetingFormat.IN_PERSON,
              location = lausanneOffice,
              createdBy = userIds[2],
              participantIds = listOf(userIds[0], userIds[2], userIds[4])),

          // 4. COMPLETED (In-Person): A past meeting with one attachment.
          Meeting(
              meetingID = "meet_completed_inperson_04",
              projectId = projectIds[2],
              title = "Q4 Marketing Brainstorm",
              status = MeetingStatus.COMPLETED,
              duration = 60,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2026, 1, 1, 9, 0) to 6,
                  ),
              datetime = createTimestamp(2025, 10, 13, 14, 0), // Last Monday 2 PM
              format = MeetingFormat.IN_PERSON,
              location = zurichHub,
              attachmentUrls = listOf("https://storage.example.com/notes_q4_marketing.pdf"),
              createdBy = userIds[3],
              participantIds = listOf(userIds[1], userIds[3])),

          // 5. COMPLETED (Virtual): A past meeting with multiple attachments.
          Meeting(
              meetingID = "meet_completed_virtual_05",
              projectId = projectIds[1],
              taskId = taskIds[2],
              title = "API Documentation Final Review",
              status = MeetingStatus.COMPLETED,
              duration = 60,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 10, 10, 11, 0), // Last Friday 11 AM
              format = MeetingFormat.VIRTUAL,
              link = "https://meet.google.com/api-docs-review",
              attachmentUrls =
                  listOf(
                      "https://storage.example.com/api_docs_recording.mp4",
                      "https://storage.example.com/meeting_chat_log.txt"),
              createdBy = userIds[4],
              participantIds = userIds // All users participated
              ),

          // 6. IN_PROGRESS (Virtual): A virtual meeting happening right now.
          Meeting(
              meetingID = "meet_inprogress_06",
              projectId = projectIds[0],
              title = "Live Demo & Feedback Session",
              status = MeetingStatus.IN_PROGRESS,
              duration = 60,
              datetime = createTimestamp(2025, 10, 16, 17, 0), // Today 5 PM
              format = MeetingFormat.VIRTUAL,
              link = "https://zoom.us/j/1234567890",
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[1], userIds[4])),

          // 7. OPEN_TO_VOTES (No votes yet): A fresh proposal.
          Meeting(
              meetingID = "meet_vote_new_07",
              projectId = projectIds[2],
              title = "Client On-site Visit Planning",
              status = MeetingStatus.OPEN_TO_VOTES,
              duration = 120,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              // `timeSlot` property removed
              // dateTimeVotes uses default emptyMap()
              // formatVotes uses default emptyList()
              datetime = null,
              format = null,
              createdBy = userIds[3],
              participantIds = listOf(userIds[0], userIds[3])),

          // 8. SCHEDULED (Project-Wide): A meeting not tied to a specific task.
          Meeting(
              meetingID = "meet_projectwide_08",
              projectId = projectIds[1],
              taskId = null,
              title = "Bravo Project All-Hands",
              status = MeetingStatus.SCHEDULED,
              duration = 45,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 11, 3, 16, 0),
              format = MeetingFormat.VIRTUAL,
              link = "https://teams.microsoft.com/...",
              createdBy = userIds[1],
              participantIds = userIds),

          // 9. SCHEDULED (Minimal): A meeting with the minimum required fields.
          Meeting(
              meetingID = "meet_minimal_09",
              projectId = projectIds[0],
              title = "Quick Check-in",
              status = MeetingStatus.SCHEDULED,
              duration = 15,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 10, 17, 9, 15),
              format = MeetingFormat.VIRTUAL, // Format is required for scheduled meetings
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[2])),

          // 10. COMPLETED (No Attachments): A past meeting without any follow-up files.
          Meeting(
              meetingID = "meet_completed_no_attachments_10",
              projectId = projectIds[2],
              title = "Budget Preliminary Discussion",
              status = MeetingStatus.COMPLETED,
              duration = 60,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 9, 30, 13, 0),
              format = MeetingFormat.IN_PERSON,
              location = lausanneOffice,
              createdBy = userIds[3],
              participantIds = listOf(userIds[3], userIds[4])),

          // 11. OPEN_TO_VOTES (Complex): Proposal with a multi-day time slot and many participants.
          Meeting(
              meetingID = "meet_vote_complex_11",
              projectId = projectIds[1],
              title = "Service Architecture Planning",
              status = MeetingStatus.OPEN_TO_VOTES,
              duration = 120,
              // `timeSlot` property removed
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 10, 27, 10, 0) to 2, // user 0, 2
                      createTimestamp(2025, 10, 28, 14, 0) to 2, // user 1, 3
                      createTimestamp(2025, 10, 28, 11, 0) to 1, // user 2
                      createTimestamp(2025, 10, 27, 13, 0) to 1 // user 4
                      ),
              formatVotes =
                  listOf(
                      MeetingFormatVote(userIds[0], MeetingFormat.IN_PERSON),
                      MeetingFormatVote(userIds[1], MeetingFormat.VIRTUAL),
                      MeetingFormatVote(userIds[2], MeetingFormat.VIRTUAL),
                      MeetingFormatVote(userIds[3], MeetingFormat.IN_PERSON),
                      MeetingFormatVote(userIds[4], MeetingFormat.IN_PERSON)),
              datetime = null,
              format = null,
              createdBy = userIds[4],
              participantIds = userIds),

          // 12. SCHEDULED (Far Future): A meeting scheduled for a few months from now.
          Meeting(
              meetingID = "meet_future_12",
              projectId = projectIds[0],
              title = "2026 Project Apollo Roadmap",
              status = MeetingStatus.SCHEDULED,
              duration = 180,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 12, 15, 11, 0),
              format = MeetingFormat.IN_PERSON,
              location = genevaClientHQ,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[1], userIds[3])),

          // 13. SCHEDULED (Back-to-Back): A meeting scheduled right after another one.
          Meeting(
              meetingID = "meet_backtoback_13",
              projectId = projectIds[1],
              title = "Bravo Project Weekly Sync (Part 2)",
              status = MeetingStatus.SCHEDULED,
              duration = 30,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 10, 17, 16, 0), // Right after meeting #2
              format = MeetingFormat.VIRTUAL,
              link = "https://meet.google.com/abc-defg-hij",
              createdBy = userIds[1],
              participantIds = listOf(userIds[1], userIds[3], userIds[4])),

          // 14. COMPLETED (Post-Vote): A completed meeting that originated from a vote.
          // It's useful to test if you want to show historical voting data.
          Meeting(
              meetingID = "meet_decided_from_vote_14",
              projectId = projectIds[2],
              title = "Social Media Campaign Launch",
              status = MeetingStatus.COMPLETED,
              duration = 45,
              datetime = createTimestamp(2025, 10, 1, 10, 0),
              format = MeetingFormat.VIRTUAL,
              link = null,
              attachmentUrls = listOf("https://storage.example.com/launch_summary.docx"),
              createdBy = userIds[2],
              participantIds = listOf(userIds[2], userIds[3]),
              // Historical voting data can be preserved:
              dateTimeVotes = mapOf(createTimestamp(2025, 10, 1, 10, 0) to 2),
              formatVotes =
                  listOf(
                      MeetingFormatVote(userIds[2], MeetingFormat.VIRTUAL),
                      MeetingFormatVote(userIds[3], MeetingFormat.VIRTUAL))),

          // 15. IN_PROGRESS (In-Person): An in-person meeting happening right now.
          Meeting(
              meetingID = "meet_inprogress_inperson_15",
              projectId = projectIds[0],
              title = "Urgent Hotfix Discussion",
              status = MeetingStatus.IN_PROGRESS,
              duration = 45,
              dateTimeVotes =
                  mapOf(
                      createTimestamp(2025, 4, 5, 12, 0) to 1,
                  ),
              datetime = createTimestamp(2025, 10, 16, 16, 45), // Today 4:45 PM
              format = MeetingFormat.IN_PERSON,
              location = lausanneOffice,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[4])))
}
