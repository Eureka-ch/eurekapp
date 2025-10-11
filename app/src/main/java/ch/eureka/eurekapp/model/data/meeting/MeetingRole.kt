package ch.eureka.eurekapp.model.data.meeting

enum class MeetingRole {
  HOST,
  PARTICIPANT;

  companion object {
    fun fromString(role: String): MeetingRole? {
      return values().find { it.name.equals(role, ignoreCase = true) }
    }
  }

  override fun toString(): String {
    return name.lowercase()
  }
}
