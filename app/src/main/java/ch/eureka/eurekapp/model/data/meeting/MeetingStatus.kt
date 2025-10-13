package ch.eureka.eurekapp.model.data.meeting

enum class MeetingStatus {
  SCHEDULED,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED;

  companion object {
    fun fromString(status: String): MeetingStatus {
      return values().find { it.name.equals(status, ignoreCase = true) }
          ?: throw IllegalArgumentException("Invalid MeetingStatus: $status")
    }
  }

  override fun toString(): String {
    return name.lowercase()
  }
}
