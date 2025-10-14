package ch.eureka.eurekapp.model.data.meeting

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
 * @property datetime Date and time of the meeting.
 * @property status Current status of the meeting (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED).
 * @property format The format of the meeting, for example in-person or virtual.
 * @property link Optional link to the meeting on a video communication service.
 * @property attachmentUrls List of file URLs attached to this meeting (notes, recordings, etc.).
 * @property ended True if the meeting was completed and false otherwise.
 * @property createdBy User ID of the person who created this meeting.
 * @property participantIds List of user IDs who are participants in this meeting (for efficient
 *   queries).
 * @property canVote True if the members can still vote for the date/time or format for the meeting
 *   false otherwise.
 * @property timeSlot Optional timeslot in which the meeting should take place, this is defined by
 *   the user that creates the meeting proposal.
 * @property dateTimeVotes Votes of every member of the project for the date and time of the
 *   meeting.
 * @property formatVotes Votes of every member of the project for the format of the meeting.
 */
data class Meeting(
    val meetingID: String = "",
    val projectId: String = "",
    val taskId: String? = null,
    val title: String = "",
    val datetime: Timestamp = Timestamp.now(),
    val status: MeetingStatus = MeetingStatus.SCHEDULED,
    val format: MeetingFormat = MeetingFormat.IN_PERSON,
    val link: String? = null,
    val attachmentUrls: List<String> = emptyList(),
    val ended: Boolean = false,
    val createdBy: String = "",
    val participantIds: List<String> = emptyList(),
    val canVote: Boolean = true,
    val timeSlot: TimeSlot? = null,
    val dateTimeVotes: List<MeetingDateTimeVotes> = emptyList(),
    val formatVotes: List<MeetingFormatVote> = emptyList()
)
