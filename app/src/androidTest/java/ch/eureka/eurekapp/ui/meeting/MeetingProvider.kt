/* Portions of this file were written with the help of Gemini.*/
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.map.Location
import com.google.firebase.Timestamp
import java.util.Calendar

/**
 * Provides a comprehensive list of fake meetings for testing and preview purposes. This object is
 * updated to conform to the latest Meeting data class structure.
 *
 * Note: this code was generated and updated by Gemini.
 */
object MeetingProvider {

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

  // --- Reusable Vote Definitions ---
  private val voteAnnaVirtual = MeetingProposalVote(userIds[0], listOf(MeetingFormat.VIRTUAL))
  private val voteAnnaInPerson = MeetingProposalVote(userIds[0], listOf(MeetingFormat.IN_PERSON))
  private val voteBenVirtual = MeetingProposalVote(userIds[1], listOf(MeetingFormat.VIRTUAL))
  private val voteBenInPerson = MeetingProposalVote(userIds[1], listOf(MeetingFormat.IN_PERSON))
  private val voteCharlieVirtual = MeetingProposalVote(userIds[2], listOf(MeetingFormat.VIRTUAL))
  private val voteCharlieInPerson = MeetingProposalVote(userIds[2], listOf(MeetingFormat.IN_PERSON))
  private val voteDianaVirtual = MeetingProposalVote(userIds[3], listOf(MeetingFormat.VIRTUAL))
  private val voteDianaInPerson = MeetingProposalVote(userIds[3], listOf(MeetingFormat.IN_PERSON))
  private val voteEthanVirtual = MeetingProposalVote(userIds[4], listOf(MeetingFormat.VIRTUAL))
  private val voteEthanInPerson = MeetingProposalVote(userIds[4], listOf(MeetingFormat.IN_PERSON))

  private val allVotesInPerson =
      listOf(
          voteAnnaInPerson,
          voteBenInPerson,
          voteCharlieInPerson,
          voteDianaInPerson,
          voteEthanInPerson)

  // --- Generated Meeting List ---
  val sampleMeetings: List<Meeting> =
      listOf(
          Meeting(
              meetingID = "meet_vote_01",
              projectId = projectIds[0],
              taskId = taskIds[0],
              title = "Apollo UI Refresh Kick-off",
              status = MeetingStatus.OPEN_TO_VOTES,
              duration = 60,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 20, 10, 0),
                          votes = listOf(voteAnnaVirtual, voteCharlieVirtual)),
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 20, 11, 0),
                          votes = listOf(voteBenInPerson)),
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 20, 14, 0),
                          votes = listOf(voteBenInPerson))),
              datetime = null,
              format = null,
              location = null,
              link = null,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[1], userIds[2])),
          Meeting(
              meetingID = "meet_scheduled_virtual_02",
              projectId = projectIds[1],
              title = "Bravo Project Weekly Sync",
              status = MeetingStatus.SCHEDULED,
              duration = 30,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 20, 10, 0),
                          votes = listOf(voteAnnaVirtual, voteCharlieVirtual)),
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 11, 20, 21, 0),
                          votes = listOf(voteBenVirtual))),
              datetime = createTimestamp(2025, 10, 17, 15, 0),
              format = MeetingFormat.VIRTUAL,
              link = "https://meet.google.com/abc-defg-hij",
              createdBy = userIds[1],
              participantIds = listOf(userIds[1], userIds[3], userIds[4])),
          Meeting(
              meetingID = "meet_scheduled_inperson_03",
              projectId = projectIds[0],
              taskId = taskIds[1],
              title = "Database Migration Strategy",
              status = MeetingStatus.SCHEDULED,
              duration = 90,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 20, 10, 0),
                          votes = listOf(voteAnnaInPerson, voteCharlieInPerson))),
              datetime = createTimestamp(2025, 10, 22, 10, 30),
              format = MeetingFormat.IN_PERSON,
              location = lausanneOffice,
              createdBy = userIds[2],
              participantIds = listOf(userIds[0], userIds[2], userIds[4])),
          Meeting(
              meetingID = "meet_completed_inperson_04",
              projectId = projectIds[2],
              title = "Q4 Marketing Brainstorm",
              status = MeetingStatus.COMPLETED,
              duration = 60,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2026, 1, 1, 9, 0), votes = allVotesInPerson)),
              datetime = createTimestamp(2025, 10, 13, 14, 0),
              format = MeetingFormat.IN_PERSON,
              location = zurichHub,
              attachmentUrls = listOf("https://storage.example.com/notes_q4_marketing.pdf"),
              audioUrl = "https://storage.example.com/audio_q4_marketing.mp3",
              transcriptId = "transcript_q4_marketing_abc",
              createdBy = userIds[3],
              participantIds = listOf(userIds[1], userIds[3])),
          Meeting(
              meetingID = "meet_completed_virtual_05",
              projectId = projectIds[1],
              taskId = taskIds[2],
              title = "API Documentation Final Review",
              status = MeetingStatus.COMPLETED,
              duration = 60,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteEthanVirtual))),
              datetime = createTimestamp(2025, 10, 10, 11, 0),
              format = MeetingFormat.VIRTUAL,
              link = "https://meet.google.com/api-docs-review",
              attachmentUrls =
                  listOf(
                      "https://storage.example.com/api_docs_recording.mp4",
                      "https://storage.example.com/meeting_chat_log.txt"),
              audioUrl = "https://storage.example.com/api_docs_recording.mp3",
              transcriptId = "transcript_api_docs_xyz",
              createdBy = userIds[4],
              participantIds = userIds),
          Meeting(
              meetingID = "meet_inprogress_06",
              projectId = projectIds[0],
              title = "Live Demo & Feedback Session",
              status = MeetingStatus.IN_PROGRESS,
              duration = 60,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 16, 17, 0),
                          votes = listOf(voteAnnaVirtual, voteBenVirtual, voteEthanVirtual))),
              datetime = createTimestamp(2025, 10, 16, 17, 0),
              format = MeetingFormat.VIRTUAL,
              link = "https://zoom.us/j/1234567890",
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[1], userIds[4])),
          Meeting(
              meetingID = "meet_vote_new_07",
              projectId = projectIds[2],
              title = "Client On-site Visit Planning",
              status = MeetingStatus.OPEN_TO_VOTES,
              duration = 120,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteDianaInPerson))),
              datetime = null,
              format = null,
              createdBy = userIds[3],
              participantIds = listOf(userIds[0], userIds[3])),
          Meeting(
              meetingID = "meet_projectwide_08",
              projectId = projectIds[1],
              taskId = null,
              title = "Bravo Project All-Hands",
              status = MeetingStatus.SCHEDULED,
              duration = 45,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteBenVirtual))),
              datetime = createTimestamp(2025, 11, 3, 16, 0),
              format = MeetingFormat.VIRTUAL,
              link = "https://teams.microsoft.com/...",
              createdBy = userIds[1],
              participantIds = userIds),
          Meeting(
              meetingID = "meet_minimal_09",
              projectId = projectIds[0],
              title = "Quick Check-in",
              status = MeetingStatus.SCHEDULED,
              duration = 15,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteAnnaVirtual))),
              datetime = createTimestamp(2025, 10, 17, 9, 15),
              format = MeetingFormat.VIRTUAL,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[2])),
          Meeting(
              meetingID = "meet_completed_no_attachments_10",
              projectId = projectIds[2],
              title = "Budget Preliminary Discussion",
              status = MeetingStatus.COMPLETED,
              duration = 60,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteDianaInPerson))),
              datetime = createTimestamp(2025, 9, 30, 13, 0),
              format = MeetingFormat.IN_PERSON,
              location = lausanneOffice,
              createdBy = userIds[3],
              participantIds = listOf(userIds[3], userIds[4])),
          Meeting(
              meetingID = "meet_vote_complex_11",
              projectId = projectIds[1],
              title = "Service Architecture Planning",
              status = MeetingStatus.OPEN_TO_VOTES,
              duration = 120,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 27, 10, 0),
                          votes = listOf(voteAnnaInPerson, voteCharlieVirtual)),
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 28, 14, 0),
                          votes = listOf(voteBenVirtual, voteDianaInPerson)),
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 28, 11, 0),
                          votes = listOf(voteCharlieVirtual)),
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 27, 13, 0),
                          votes = listOf(voteEthanInPerson))),
              datetime = null,
              format = null,
              createdBy = userIds[4],
              participantIds = userIds),
          Meeting(
              meetingID = "meet_future_12",
              projectId = projectIds[0],
              title = "2026 Project Apollo Roadmap",
              status = MeetingStatus.SCHEDULED,
              duration = 180,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteAnnaInPerson))),
              datetime = createTimestamp(2025, 12, 15, 11, 0),
              format = MeetingFormat.IN_PERSON,
              location = genevaClientHQ,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[1], userIds[3])),
          Meeting(
              meetingID = "meet_backtoback_13",
              projectId = projectIds[1],
              title = "Bravo Project Weekly Sync (Part 2)",
              status = MeetingStatus.SCHEDULED,
              duration = 30,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteBenVirtual))),
              datetime = createTimestamp(2025, 10, 17, 16, 0),
              format = MeetingFormat.VIRTUAL,
              link = "https://meet.google.com/abc-defg-hij",
              createdBy = userIds[1],
              participantIds = listOf(userIds[1], userIds[3], userIds[4])),
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
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 10, 1, 10, 0),
                          votes = listOf(voteCharlieVirtual, voteDianaVirtual)))),
          Meeting(
              meetingID = "meet_inprogress_inperson_15",
              projectId = projectIds[0],
              title = "Urgent Hotfix Discussion",
              status = MeetingStatus.IN_PROGRESS,
              duration = 45,
              meetingProposals =
                  listOf(
                      MeetingProposal(
                          dateTime = createTimestamp(2025, 4, 5, 12, 0),
                          votes = listOf(voteAnnaInPerson))),
              datetime = createTimestamp(2025, 10, 16, 16, 45),
              format = MeetingFormat.IN_PERSON,
              location = lausanneOffice,
              createdBy = userIds[0],
              participantIds = listOf(userIds[0], userIds[4])))
}
