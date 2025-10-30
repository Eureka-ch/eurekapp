package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible formats of a meeting.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class MeetingFormat(val description: String) : StringSerializableEnum {
  /** The meeting is in-person. */
  IN_PERSON("In person"),

  /** Meeting is virtual (ex: Google Meet, Zoom, etc). */
  VIRTUAL("Virtual")
}
