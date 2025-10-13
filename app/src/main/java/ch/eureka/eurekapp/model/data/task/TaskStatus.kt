package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.StringSerializableEnum

enum class TaskStatus : StringSerializableEnum {
  TODO,
  IN_PROGRESS,
  COMPLETED,
  CANCELLED;

  override fun toString(): String = toDisplayString()
}
