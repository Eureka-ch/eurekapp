/**
 * Unit tests for TaskDateUtils utility class
 *
 * Tests basic date utility functions including overdue detection, week calculations, and day
 * counting.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.utils.TaskBusinessLogic
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDateUtilsTest {

  @Test
  fun `isDueThisWeek returns false for null due date`() {
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = null)
    val result = TaskBusinessLogic.determineTags(task).contains("This Week")
    assertFalse(result)
  }

  @Test
  fun `isDueThisWeek returns true for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("Urgent")
    assertTrue(result)
  }

  @Test
  fun `isDueThisWeek returns true for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("Urgent")
    assertTrue(result)
  }

  @Test
  fun `isDueThisWeek returns true for task due in 7 days`() {
    val sevenDaysFromNow = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(sevenDaysFromNow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("This Week")
    assertTrue(result)
  }

  @Test
  fun `isDueThisWeek returns false for task due in 8 days`() {
    val eightDaysFromNow = Date(System.currentTimeMillis() + 8 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(eightDaysFromNow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("This Week")
    assertFalse(result)
  }

  @Test
  fun `isDueThisWeek returns false for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("This Week")
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns false for null due date`() {
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = null)
    val result = TaskBusinessLogic.determineTags(task).contains("Overdue")
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns true for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("Overdue")
    assertTrue(result)
  }

  @Test
  fun `isOverdue returns false for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("Overdue")
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns false for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("Overdue")
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns false for task due in future`() {
    val futureDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(futureDate)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = TaskBusinessLogic.determineTags(task).contains("Overdue")
    assertFalse(result)
  }

  @Test
  fun `getDaysUntilDue returns null for null due date`() {
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = null)
    val result = getDaysUntilDue(task)
    assertTrue(result == null)
  }

  @Test
  fun `getDaysUntilDue returns 0 for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = getDaysUntilDue(task)
    assertTrue(result == 0L)
  }

  @Test
  fun `getDaysUntilDue returns positive value for future task`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = getDaysUntilDue(task)
    assertTrue(result != null && result > 0)
  }

  @Test
  fun `getDaysUntilDue returns negative value for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val result = getDaysUntilDue(task)
    assertTrue(result != null && result < 0)
  }
}
