package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.StringSerializableEnum

/**
 * Enum representing the possible statuses of a task.
 *
 * Note: This file was co-authored by Claude Code.
 */
enum class TaskStatus : StringSerializableEnum {
  /** Task has not been started yet. */
  TODO,

  /** Task is currently being worked on. */
  IN_PROGRESS,

  /** Task has been finished successfully. */
  COMPLETED,

  /** Task was cancelled and will not be completed. */
  CANCELLED
}
