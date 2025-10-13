package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible roles a user can have in a meeting.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class MeetingRole : StringSerializableEnum {
  /** The host who organizes and leads the meeting. */
  HOST,

  /** A regular participant in the meeting. */
  PARTICIPANT
}
