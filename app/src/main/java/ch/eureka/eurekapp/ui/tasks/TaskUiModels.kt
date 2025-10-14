package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.user.User
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/** UI model for displaying task information Pure UI data class - no business logic */
data class TaskUiModel(
    val task: Task,
    val template: TaskTemplate? = null,
    val assignee: User? = null,
    val progress: Float = 0.0f,
    val isCompleted: Boolean = task.status == ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED
) {
  val id: String
    get() = task.taskID

  val title: String
    get() = task.title.ifBlank { template?.title ?: "Untitled Task" }
  // val description: String get() = template?.description ?: ""
  val assigneeName: String
    get() = assignee?.displayName ?: "Unassigned"

  val dueDate: String
    get() = formatDueDate(task.dueDate)

  val priority: String
    get() = determinePriority()

  val tags: List<String>
    get() = determineTags()

  val progressText: String
    get() = "${(progress * 100).toInt()}%"

  val progressValue: Float
    get() = progress

  private fun formatDueDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "No due date"

    val now = Date()
    val dueDate = timestamp.toDate()
    val diffInDays = (dueDate.time - now.time) / (1000 * 60 * 60 * 24)

    return when {
      diffInDays < 0 -> "Overdue"
      diffInDays == 0L -> "Due today"
      diffInDays == 1L -> "Due tomorrow"
      diffInDays <= 7L -> "Due in $diffInDays days"
      else -> {
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        "Due ${formatter.format(dueDate)}"
      }
    }
  }

  private fun determinePriority(): String {
    // Simple priority logic based on due date proximity
    val timestamp = task.dueDate
    if (timestamp == null) return "Low Priority"

    val now = Date()
    val dueDate = timestamp.toDate()
    val diffInDays = (dueDate.time - now.time) / (1000 * 60 * 60 * 24)

    return when {
      diffInDays < 0 -> "Critical Priority" // Overdue
      diffInDays <= 1L -> "High Priority" // Due today/tomorrow
      diffInDays <= 3L -> "Medium Priority" // Due in 2-3 days
      else -> "Low Priority" // Due later
    }
  }

  private fun determineTags(): List<String> {
    val tags = mutableListOf<String>()

    // Add tags based on task status
    when (task.status) {
      ch.eureka.eurekapp.model.data.task.TaskStatus.TODO -> tags.add("To Do")
      ch.eureka.eurekapp.model.data.task.TaskStatus.IN_PROGRESS -> tags.add("In Progress")
      ch.eureka.eurekapp.model.data.task.TaskStatus.COMPLETED -> tags.add("Completed")
      ch.eureka.eurekapp.model.data.task.TaskStatus.CANCELLED -> tags.add("Cancelled")
    }

    // Add tags based on assignment status
    if (task.assignedUserIds.isEmpty()) {
      tags.add("Unassigned")
    } else {
      tags.add("Assigned")
    }

    // Add tags based on due date status
    val timestamp = task.dueDate
    if (timestamp != null) {
      val now = Date()
      val dueDate = timestamp.toDate()
      val diffInDays = (dueDate.time - now.time) / (1000 * 60 * 60 * 24)

      when {
        diffInDays < 0 -> tags.add("Overdue")
        diffInDays <= 1L -> tags.add("Urgent")
        diffInDays <= 7L -> tags.add("This Week")
      }
    }

    return tags
  }
}
