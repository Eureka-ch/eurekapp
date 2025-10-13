package ch.eureka.eurekapp.model.data.meeting

/**
 * Enum representing the possible statuses of a meeting.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class MeetingStatus {
  /** Meeting is planned but has not started yet. */
  SCHEDULED,

  /** Meeting is currently taking place. */
  IN_PROGRESS,

  /** Meeting has finished. */
  COMPLETED,

  /** Meeting was cancelled and will not take place. */
  CANCELLED;

  companion object {
    /**
     * Converts a string to a MeetingStatus enum value (case-insensitive).
     *
     * @param status The string representation of the status.
     * @return The corresponding MeetingStatus enum value.
     * @throws IllegalArgumentException if the status string is invalid.
     */
    fun fromString(status: String): MeetingStatus {
      return values().find { it.name.equals(status, ignoreCase = true) }
          ?: throw IllegalArgumentException("Invalid MeetingStatus: $status")
    }
  }
}
