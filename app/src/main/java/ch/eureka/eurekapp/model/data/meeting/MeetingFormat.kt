package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/** Enum representing the possible formats of a meeting. */
enum class MeetingFormat : StringSerializableEnum {
  /** The meeting is in-person. */
  IN_PERSON,

  /** Meeting is virtual (ex: Google Meet, Zoom, etc). */
  VIRTUAL
}
