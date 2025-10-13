package ch.eureka.eurekapp.model.data.meeting

/**
 * Data class representing a participant in a meeting.
 *
 * Participants are stored as a subcollection under each meeting document, allowing for role-based
 * access and permissions.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property userId The ID of the user participating in the meeting.
 * @property role The role of the participant (HOST or PARTICIPANT).
 */
data class Participant(val userId: String = "", val role: MeetingRole = MeetingRole.PARTICIPANT)
