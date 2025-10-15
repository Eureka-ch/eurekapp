package ch.eureka.eurekapp.model.utils

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskBusinessLogicTest {

  @Test
  fun `determinePriority returns Low Priority for null due date`() {
    val task = Task(taskID = "1", dueDate = null)
    val result = TaskBusinessLogic.determinePriority(task)
    assertEquals("Low Priority", result)
  }

  @Test
  fun `determinePriority returns Critical Priority for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determinePriority(task)
    assertEquals("Critical Priority", result)
  }

  @Test
  fun `determinePriority returns High Priority for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determinePriority(task)
    assertEquals("High Priority", result)
  }

  @Test
  fun `determinePriority returns High Priority for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determinePriority(task)
    assertEquals("High Priority", result)
  }

  @Test
  fun `determinePriority returns Medium Priority for task due in 3 days`() {
    val futureDate = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(futureDate)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determinePriority(task)
    assertEquals("Medium Priority", result)
  }

  @Test
  fun `formatDueDate returns No due date for null`() {
    val result = TaskBusinessLogic.formatDueDate(null)
    assertEquals("No due date", result)
  }

  @Test
  fun `formatDueDate returns Due today for today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val result = TaskBusinessLogic.formatDueDate(timestamp)
    assertEquals("Due today", result)
  }

  @Test
  fun `formatDueDate returns Due tomorrow for tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val result = TaskBusinessLogic.formatDueDate(timestamp)
    assertEquals("Due tomorrow", result)
  }

  @Test
  fun `formatDueDate returns Due in X days for future dates`() {
    val futureDate = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(futureDate)
    val result = TaskBusinessLogic.formatDueDate(timestamp)
    assertTrue(
        "Should contain 'Due in' and 'days'", result.contains("Due in") && result.contains("days"))
  }

  @Test
  fun `formatDueDate returns formatted date for far future`() {
    val farFuture = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000) // 10 days
    val timestamp = Timestamp(farFuture)
    val result = TaskBusinessLogic.formatDueDate(timestamp)
    assertTrue(result.startsWith("Due "))
  }

  @Test
  fun `determineTags returns To Do for TODO status`() {
    val task = Task(taskID = "1", status = TaskStatus.TODO)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("To Do"))
  }

  @Test
  fun `determineTags returns In Progress for IN_PROGRESS status`() {
    val task = Task(taskID = "1", status = TaskStatus.IN_PROGRESS)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("In Progress"))
  }

  @Test
  fun `determineTags returns Completed for COMPLETED status`() {
    val task = Task(taskID = "1", status = TaskStatus.COMPLETED)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Completed"))
  }

  @Test
  fun `determineTags returns Cancelled for CANCELLED status`() {
    val task = Task(taskID = "1", status = TaskStatus.CANCELLED)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Cancelled"))
  }

  @Test
  fun `determineTags returns Unassigned for empty assignedUserIds`() {
    val task = Task(taskID = "1", assignedUserIds = emptyList())
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Unassigned"))
  }

  @Test
  fun `determineTags returns Assigned for non-empty assignedUserIds`() {
    val task = Task(taskID = "1", assignedUserIds = listOf("user1"))
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Assigned"))
  }

  @Test
  fun `isTaskCompleted returns true for completed task`() {
    val task = Task(taskID = "1", status = TaskStatus.COMPLETED)
    val result = TaskBusinessLogic.isTaskCompleted(task)
    assertTrue(result)
  }

  @Test
  fun `isTaskCompleted returns false for incomplete task`() {
    val task = Task(taskID = "1", status = TaskStatus.TODO)
    val result = TaskBusinessLogic.isTaskCompleted(task)
    assertTrue(!result)
  }

  @Test
  fun `isTaskCompleted returns false for in progress task`() {
    val task = Task(taskID = "1", status = TaskStatus.IN_PROGRESS)
    val result = TaskBusinessLogic.isTaskCompleted(task)
    assertTrue(!result)
  }

  @Test
  fun `isTaskCompleted returns false for cancelled task`() {
    val task = Task(taskID = "1", status = TaskStatus.CANCELLED)
    val result = TaskBusinessLogic.isTaskCompleted(task)
    assertTrue(!result)
  }

  @Test
  fun `determineTags returns Overdue tag for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Overdue"))
  }

  @Test
  fun `determineTags returns Urgent tag for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Urgent"))
  }

  @Test
  fun `determineTags returns Urgent tag for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("Urgent"))
  }

  @Test
  fun `determineTags returns This Week tag for task due in 3 days`() {
    val threeDaysFromNow = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(threeDaysFromNow)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("This Week"))
  }

  @Test
  fun `determineTags returns This Week tag for task due in 7 days`() {
    val sevenDaysFromNow = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(sevenDaysFromNow)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(result.contains("This Week"))
  }

  @Test
  fun `determineTags does not return date-based tags for task due far in future`() {
    val farFuture = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(farFuture)
    val task = Task(taskID = "1", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(!result.contains("Overdue"))
    assertTrue(!result.contains("Urgent"))
    assertTrue(!result.contains("This Week"))
  }

  @Test
  fun `determineTags does not return date-based tags for task with null due date`() {
    val task = Task(taskID = "1", dueDate = null)
    val result = TaskBusinessLogic.determineTags(task)
    assertTrue(!result.contains("Overdue"))
    assertTrue(!result.contains("Urgent"))
    assertTrue(!result.contains("This Week"))
  }
}
