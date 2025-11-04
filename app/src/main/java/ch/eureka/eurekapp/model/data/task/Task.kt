package ch.eureka.eurekapp.model.data.task

import com.google.firebase.Timestamp
import java.time.temporal.ChronoUnit
import kotlin.compareTo

/**
 * Data class representing a task within a project.
 *
 * Tasks are work items that can be assigned to users, have due dates, and track progress through
 * status changes. Tasks can be created from templates and support custom data fields.
 *
 * Note: This file was co-authored by Claude Code.
 *
 * @property taskID Unique identifier for the task.
 * @property templateId ID of the template this task was created from (empty if no template).
 * @property projectId ID of the project this task belongs to.
 * @property title The name/title of the task.
 * @property description Detailed description of what the task entails.
 * @property status Current status of the task (TODO, IN_PROGRESS, COMPLETED, CANCELLED).
 * @property assignedUserIds List of user IDs assigned to this task.
 * @property dueDate Optional deadline for task completion.
 * @property attachmentUrls List of file URLs attached to this task (PDFs, images, etc.).
 * @property customData Template-specific data stored as key-value pairs.
 * @property createdBy User ID of the person who created this task.
 * @property dependingOnTasks the taskIds that this task depends on
 */
data class Task(
    val taskID: String = "",
    val templateId: String = "",
    val projectId: String = "",
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val assignedUserIds: List<String> = emptyList(),
    val dueDate: Timestamp? = null,
    val attachmentUrls: List<String> = emptyList(),
    val customData: Map<String, Any> = emptyMap(),
    val createdBy: String = "",
    val dependingOnTasks: List<String> = listOf()
)

fun getDaysUntilDue(task: Task, now: Timestamp): Long? {
  val dueDate = task.dueDate?.toDate() ?: return null
  val currentDate = now.toDate()
  return ChronoUnit.DAYS.between(currentDate.toInstant(), dueDate.toInstant())
}

fun determinePriority(task: Task, now: Timestamp): String {
  val timestamp = task.dueDate
  if (timestamp == null) return "Low Priority"

  val diffInDays = getDaysUntilDue(task, now) ?: return "Low Priority"

  return when {
    diffInDays < 0 -> "Critical Priority"
    diffInDays <= 1L -> "High Priority"
    diffInDays <= 3L -> "Medium Priority"
    else -> "Low Priority"
  }
}
