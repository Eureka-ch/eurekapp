package ch.eureka.eurekapp.model.data.meeting

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
 * @property attachmentUrls List of file URLs attached to this meeting (notes, recordings, etc.).
 * @property createdBy User ID of the person who created this meeting.
 * @property participantIds List of user IDs who are participants in this meeting (for efficient
 *   queries).
 */
data class Meeting(
    val meetingID: String = "",
    val projectId: String = "",
    val taskId: String? = null,
    val title: String = "",
    val status: MeetingStatus = MeetingStatus.SCHEDULED,
    val attachmentUrls: List<String> = emptyList(),
    val createdBy: String = "",
    val participantIds: List<String> = emptyList()
)
