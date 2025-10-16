package ch.eureka.eurekapp.model.utils

import com.google.firebase.Timestamp
import java.util.*

/**
 * Utility class for task-related date calculations
 *
 * Provides business logic for date calculations related to tasks. Moved from ui.tasks to
 * model.utils for better separation of concerns.
 */
object TaskDateUtils {

  /**
   * Checks if a task is due within the next week
   *
   * @param dueDate The task's due date timestamp
   * @return true if the task is due within 7 days, false otherwise
   */
  fun isDueThisWeek(dueDate: Timestamp?): Boolean {
    if (dueDate == null) return false

    val now = Date()
    val dueDateAsDate = dueDate.toDate()
    val diffInDays = (dueDateAsDate.time - now.time) / (1000 * 60 * 60 * 24)

    return diffInDays >= 0 && diffInDays <= 7
  }

  /**
   * Checks if a task is overdue
   *
   * @param dueDate The task's due date timestamp
   * @return true if the task is overdue, false otherwise
   */
  fun isOverdue(dueDate: Timestamp?): Boolean {
    if (dueDate == null) return false

    val now = Date()
    val dueDateAsDate = dueDate.toDate()
    val diffInDays = (dueDateAsDate.time - now.time) / (1000 * 60 * 60 * 24)

    return diffInDays < 0
  }

  /**
   * Gets the number of days until due date
   *
   * @param dueDate The task's due date timestamp
   * @return Number of days until due (negative if overdue)
   */
  fun getDaysUntilDue(dueDate: Timestamp?): Long {
    if (dueDate == null) return Long.MAX_VALUE

    val now = Date()
    val dueDateAsDate = dueDate.toDate()
    return (dueDateAsDate.time - now.time) / (1000 * 60 * 60 * 24)
  }
}
