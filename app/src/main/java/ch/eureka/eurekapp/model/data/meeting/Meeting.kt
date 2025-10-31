package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.map.Location
import com.google.firebase.Timestamp

/**
 * Data class representing a meeting within a project.
 *
 * Meetings can be associated with specific tasks or be project-wide. They track participants
 * through a separate subcollection and support file attachments.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property meetingID Unique identifier for the meeting.
 * @property projectId ID of the project this meeting belongs to.
 * @property taskId Optional ID of the task this meeting is associated with.
 * @property title The name/title of the meeting.
 * @property status Current status of the meeting (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED).
 * @property duration Duration of the meeting in minutes.
 * @property dateTimeVotes Votes of every member of the project for the date and time of the
 *   meeting.
 * @property formatVotes Votes of every member of the project for the format of the meeting.
 * @property datetime Date and time of the meeting.
 * @property format The format of the meeting, for example in-person or virtual.
 * @property location Optional location of the meeting.
 * @property link Optional link to the meeting on a video communication service.
 * @property attachmentUrls List of file URLs attached to this meeting (notes, recordings, etc.).
 * @property transcriptId Optional ID linking to the transcription document of this meeting
 * @property createdBy User ID of the person who created this meeting.
 * @property participantIds List of user IDs who are participants in this meeting (for efficient
 *   queries).
 */
data class Meeting(
    val meetingID: String = "",
    val projectId: String = "",
    val taskId: String? = null,
    val title: String = "",
    val status: MeetingStatus = MeetingStatus.OPEN_TO_VOTES,
    val duration: Int = 30,
    val dateTimeVotes: List<DateTimeVote> = emptyList(),
    val formatVotes: List<MeetingFormatVote> = emptyList(),
    val datetime: Timestamp? = Timestamp.now(),
    val format: MeetingFormat? = null,
    val location: Location? = null,
    val link: String? = null,
    val attachmentUrls: List<String> = emptyList(),
    val transcriptId: String? = null,
    val createdBy: String = "",
    val participantIds: List<String> = emptyList(),
)
