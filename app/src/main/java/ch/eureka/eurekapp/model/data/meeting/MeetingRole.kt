package ch.eureka.eurekapp.model.data.meeting

/**
 * Enum representing the possible roles a user can have in a meeting.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class MeetingRole {
  /** The host who organizes and leads the meeting. */
  HOST,

  /** A regular participant in the meeting. */
  PARTICIPANT;

  companion object {
    /**
     * Converts a string to a MeetingRole enum value (case-insensitive).
     *
     * @param role The string representation of the role.
     * @return The corresponding MeetingRole enum value.
     * @throws IllegalArgumentException if the role string is invalid.
     */
    fun fromString(role: String): MeetingRole {
      return values().find { it.name.equals(role, ignoreCase = true) }
          ?: throw IllegalArgumentException("Invalid MeetingRole: $role")
    }
  }
}
