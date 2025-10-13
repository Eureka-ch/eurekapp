package ch.eureka.eurekapp.model.data.project

/**
 * Enum representing the possible roles a user can have in a project.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class ProjectRole {
  /** The owner who created the project and has full control. */
  OWNER,

  /** An administrator with elevated permissions. */
  ADMIN,

  /** A regular member of the project. */
  MEMBER;

  companion object {
    /**
     * Converts a string to a ProjectRole enum value (case-insensitive).
     *
     * @param role The string representation of the role.
     * @return The corresponding ProjectRole enum value.
     * @throws IllegalArgumentException if the role string is invalid.
     */
    fun fromString(role: String): ProjectRole {
      return values().find { it.name.equals(role, ignoreCase = true) }
          ?: throw IllegalArgumentException("Invalid ProjectRole: $role")
    }
  }
}
