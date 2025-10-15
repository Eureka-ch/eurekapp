package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible statuses of a meeting.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class MeetingStatus : StringSerializableEnum {
  /** Meeting is planned but has not started yet. */
  SCHEDULED,

  /** Meeting is currently taking place. */
  IN_PROGRESS,

  /** Meeting has finished. */
  COMPLETED,

  /** Meeting was cancelled and will not take place. */
  CANCELLED
}
