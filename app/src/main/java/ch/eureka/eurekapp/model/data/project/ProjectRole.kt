package ch.eureka.eurekapp.model.data.project

enum class ProjectRole {
  OWNER,
  ADMIN,
  MEMBER;

  companion object {
    fun fromString(role: String): ProjectRole? {
      return values().find { it.name.equals(role, ignoreCase = true) }
    }
  }

  override fun toString(): String {
    return name.lowercase()
  }
}
