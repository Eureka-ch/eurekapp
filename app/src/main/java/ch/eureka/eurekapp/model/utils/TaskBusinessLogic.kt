package ch.eureka.eurekapp.model.utils

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import java.text.SimpleDateFormat
import java.util.*

/** Utility class for task-related business logic Separates business logic from UI data models */
object TaskBusinessLogic {

  /**
   * Determines the priority level of a task based on its due date
   *
   * @param task The task to analyze
   * @return Priority string for display
   */
  fun determinePriority(task: Task): String {
    val diffInDays = getDaysUntilDue(task) ?: return "Low Priority"

    return when {
      diffInDays < 0 -> "Critical Priority" // Overdue
      diffInDays <= 1L -> "High Priority" // Due today/tomorrow
      diffInDays <= 3L -> "Medium Priority" // Due in 2-3 days
      else -> "Low Priority" // Due later
    }
  }

  /**
   * Formats the due date for display
   *
   * @param task The task to format due date for
   * @return Formatted date string
   */
  fun formatDueDate(task: Task): String {
    val diffInDays = getDaysUntilDue(task) ?: return "No due date"

    return when {
      diffInDays < 0 -> "Overdue"
      diffInDays == 0L -> "Due today"
      diffInDays == 1L -> "Due tomorrow"
      diffInDays <= 7L -> "Due in $diffInDays days"
      else -> {
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        "Due ${formatter.format(task.dueDate?.toDate() ?: Date())}"
      }
    }
  }

  /**
   * Determines tags for a task based on its status and properties
   *
   * @param task The task to analyze
   * @return List of tag strings
   */
  fun determineTags(task: Task): List<String> {
    val tags = mutableListOf<String>()

    // Add tags based on task status
    when (task.status) {
      TaskStatus.TODO -> tags.add("To Do")
      TaskStatus.IN_PROGRESS -> tags.add("In Progress")
      TaskStatus.COMPLETED -> tags.add("Completed")
      TaskStatus.CANCELLED -> tags.add("Cancelled")
    }

    // Add tags based on assignment status
    if (task.assignedUserIds.isEmpty()) {
      tags.add("Unassigned")
    } else {
      tags.add("Assigned")
    }

    // Add tags based on due date status
    val diffInDays = getDaysUntilDue(task)
    if (diffInDays != null) {
      when {
        diffInDays < 0 -> tags.add("Overdue")
        diffInDays <= 1L -> tags.add("Urgent")
        diffInDays <= 7L -> tags.add("This Week")
      }
    }

    return tags
  }

  /**
   * Validates if a task meets the required business rules
   *
   * @param task The task to validate
   * @return true if the task is valid, false otherwise
   */
  fun isValidTask(task: Task): Boolean {
    // Required fields validation - all must be non-blank
    return !task.taskID.isBlank() && !task.title.isBlank() && !task.projectId.isBlank()
  }

  /**
   * Checks if a task is completed
   *
   * @param task The task to check
   * @return true if completed, false otherwise
   */
  fun isTaskCompleted(task: Task): Boolean {
    return task.status == TaskStatus.COMPLETED
  }
}
