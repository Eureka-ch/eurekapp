/**
 * Advanced unit tests for TaskDateUtils utility class
 *
 * Tests comprehensive date calculation scenarios including overdue tasks, due dates, and various
 * time-based business logic.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.getDaysUntilDue
import ch.eureka.eurekapp.model.utils.TaskBusinessLogic
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests avancés pour TaskDateUtils Teste la logique de calcul des dates avec différents scénarios
 */
class TaskDateUtilsAdvancedTest {

  @Test
  fun `isDueThisWeek returns true for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertTrue(TaskBusinessLogic.determineTags(task).contains("Urgent"))
  }

  @Test
  fun `isDueThisWeek returns true for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertTrue(TaskBusinessLogic.determineTags(task).contains("Urgent"))
  }

  @Test
  fun `isDueThisWeek returns true for task due in 7 days`() {
    val in7Days = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in7Days)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertTrue(TaskBusinessLogic.determineTags(task).contains("This Week"))
  }

  @Test
  fun `isDueThisWeek returns false for task due in 8 days`() {
    val in8Days = Date(System.currentTimeMillis() + 8 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in8Days)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertFalse(TaskBusinessLogic.determineTags(task).contains("This Week"))
  }

  @Test
  fun `isDueThisWeek returns false for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertFalse(TaskBusinessLogic.determineTags(task).contains("This Week"))
  }

  @Test
  fun `isDueThisWeek returns false for null due date`() {
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = null)
    assertFalse(TaskBusinessLogic.determineTags(task).contains("This Week"))
  }

  @Test
  fun `isOverdue returns true for task due yesterday`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertTrue(TaskBusinessLogic.determineTags(task).contains("Overdue"))
  }

  @Test
  fun `isOverdue returns true for task due 5 days ago`() {
    val fiveDaysAgo = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(fiveDaysAgo)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertTrue(TaskBusinessLogic.determineTags(task).contains("Overdue"))
  }

  @Test
  fun `isOverdue returns false for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertFalse(TaskBusinessLogic.determineTags(task).contains("Overdue"))
  }

  @Test
  fun `isOverdue returns false for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    assertFalse(TaskBusinessLogic.determineTags(task).contains("Overdue"))
  }

  @Test
  fun `isOverdue returns false for null due date`() {
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = null)
    assertFalse(TaskBusinessLogic.determineTags(task).contains("Overdue"))
  }

  @Test
  fun `getDaysUntilDue returns correct positive days for future task`() {
    val in3Days = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in3Days)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val days = getDaysUntilDue(task)
    assertEquals(3L, days)
  }

  @Test
  fun `getDaysUntilDue returns negative days for overdue task`() {
    val twoDaysAgo = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(twoDaysAgo)
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = timestamp)
    val days = getDaysUntilDue(task)
    assertEquals(-2L, days)
  }

  @Test
  fun `getDaysUntilDue returns null for null due date`() {
    val task = Task(taskID = "test", projectId = "proj", title = "Test", dueDate = null)
    val days = getDaysUntilDue(task)
    assertEquals(null, days)
  }
}
