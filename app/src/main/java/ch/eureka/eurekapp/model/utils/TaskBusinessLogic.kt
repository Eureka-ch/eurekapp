package ch.eureka.eurekapp.model.utils

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
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
    val timestamp = task.dueDate
    if (timestamp == null) return "Low Priority"

    val diffInDays = TaskDateUtils.getDaysUntilDue(timestamp)

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
   * @param timestamp The due date timestamp
   * @return Formatted date string
   */
  fun formatDueDate(timestamp: Timestamp?): String {
    if (timestamp == null) return "No due date"

    val diffInDays = TaskDateUtils.getDaysUntilDue(timestamp)

    return when {
      diffInDays < 0 -> "Overdue"
      diffInDays == 0L -> "Due today"
      diffInDays == 1L -> "Due tomorrow"
      diffInDays <= 7L -> "Due in $diffInDays days"
      else -> {
        val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())
        "Due ${formatter.format(timestamp.toDate())}"
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

    // Add tags based on due date status using TaskDateUtils
    val timestamp = task.dueDate
    if (timestamp != null) {
      val diffInDays = TaskDateUtils.getDaysUntilDue(timestamp)

      when {
        diffInDays < 0 -> tags.add("Overdue")
        diffInDays <= 1L -> tags.add("Urgent")
        diffInDays <= 7L -> tags.add("This Week")
      }
    }

    return tags
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
