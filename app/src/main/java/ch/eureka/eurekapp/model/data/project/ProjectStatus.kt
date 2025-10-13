package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible statuses of a project.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class ProjectStatus : StringSerializableEnum {
  /** Project is open and accepting work. */
  OPEN,

  /** Project is actively being worked on. */
  IN_PROGRESS,

  /** Project has been finished successfully. */
  COMPLETED,

  /** Project has been archived and is no longer active. */
  ARCHIVED
}
