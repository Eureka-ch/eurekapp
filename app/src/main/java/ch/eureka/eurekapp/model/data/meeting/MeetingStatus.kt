package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible statuses of a meeting.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class MeetingStatus(val description: String) : StringSerializableEnum {
  /** Meeting was proposed and members can vote for it's format and datetime. */
  OPEN_TO_VOTES("Voting in progress"),

  /** Meeting is planned but has not started yet. */
  SCHEDULED("Scheduled"),

  /** Meeting is currently taking place. */
  IN_PROGRESS("In progress"),

  /** Meeting has finished. */
  COMPLETED("Completed")
}
