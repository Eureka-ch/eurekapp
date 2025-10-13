package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible roles a user can have in a project.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class ProjectRole : StringSerializableEnum {
  /** The owner who created the project and has full control. */
  OWNER,

  /** An administrator with elevated permissions. */
  ADMIN,

  /** A regular member of the project. */
  MEMBER
}
