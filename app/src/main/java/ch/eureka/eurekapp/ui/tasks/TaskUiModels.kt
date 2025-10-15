package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.user.User

/**
 * Pure UI data model for displaying task information Contains only data needed for UI rendering, no
 * business logic
 */
data class TaskUiModel(
    val task: Task,
    val template: TaskTemplate? = null,
    val assignee: User? = null,
    val progress: Float = 0.0f,
    val isCompleted: Boolean = false // Passed as parameter, no business logic
) {
  val id: String
    get() = task.taskID

  val title: String
    get() = task.title.ifBlank { template?.title ?: "Untitled Task" }

  val assigneeName: String
    get() = assignee?.displayName ?: "Unassigned"

  val progressText: String
    get() = "${(progress * 100).toInt()}%"

  val progressValue: Float
    get() = progress
}
