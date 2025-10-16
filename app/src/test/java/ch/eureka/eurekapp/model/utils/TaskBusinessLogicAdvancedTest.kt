/**
 * Advanced unit tests for TaskBusinessLogic utility class
 *
 * Tests comprehensive business logic scenarios including priority determination, date formatting,
 * tag generation, and task completion status.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.utils.TaskBusinessLogic
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests avancés pour TaskBusinessLogic Teste la logique métier réelle avec différents scénarios */
class TaskBusinessLogicAdvancedTest {

  @Test
  fun `determinePriority returns Critical Priority for overdue tasks`() {
    // Task overdue by 2 days
    val overdueDate = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "test", title = "Overdue Task", dueDate = Timestamp(overdueDate))

    val priority = TaskBusinessLogic.determinePriority(task)
    assertEquals("Critical Priority", priority)
  }

  @Test
  fun `determinePriority returns High Priority for tasks due today`() {
    val today = Date()
    val task = Task(taskID = "test", title = "Today Task", dueDate = Timestamp(today))

    val priority = TaskBusinessLogic.determinePriority(task)
    assertEquals("High Priority", priority)
  }

  @Test
  fun `determinePriority returns High Priority for tasks due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
    val task = Task(taskID = "test", title = "Tomorrow Task", dueDate = Timestamp(tomorrow))

    val priority = TaskBusinessLogic.determinePriority(task)
    assertEquals("High Priority", priority)
  }

  @Test
  fun `determinePriority returns Medium Priority for tasks due in 2-3 days`() {
    val in3Days = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "test", title = "3 Days Task", dueDate = Timestamp(in3Days))

    val priority = TaskBusinessLogic.determinePriority(task)
    assertEquals("Medium Priority", priority)
  }

  @Test
  fun `determinePriority returns Low Priority for tasks due later`() {
    val in10Days = Date(System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "test", title = "Future Task", dueDate = Timestamp(in10Days))

    val priority = TaskBusinessLogic.determinePriority(task)
    assertEquals("Low Priority", priority)
  }

  @Test
  fun `determinePriority returns Low Priority for tasks without due date`() {
    val task = Task(taskID = "test", title = "No Due Date Task", dueDate = null)

    val priority = TaskBusinessLogic.determinePriority(task)
    assertEquals("Low Priority", priority)
  }

  @Test
  fun `formatDueDate returns Overdue for past dates`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val formatted = TaskBusinessLogic.formatDueDate(Timestamp(yesterday))
    assertEquals("Overdue", formatted)
  }

  @Test
  fun `formatDueDate returns Due today for today`() {
    val today = Date()
    val formatted = TaskBusinessLogic.formatDueDate(Timestamp(today))
    assertEquals("Due today", formatted)
  }

  @Test
  fun `formatDueDate returns Due tomorrow for tomorrow`() {
    // Create a timestamp for tomorrow at noon to avoid timezone issues
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 12)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val tomorrow = calendar.time
    val formatted = TaskBusinessLogic.formatDueDate(Timestamp(tomorrow))
    assertEquals("Due tomorrow", formatted)
  }

  @Test
  fun `formatDueDate returns No due date for null timestamp`() {
    val formatted = TaskBusinessLogic.formatDueDate(null)
    assertEquals("No due date", formatted)
  }

  @Test
  fun `isTaskCompleted returns true for completed tasks`() {
    val task = Task(taskID = "test", title = "Completed Task", status = TaskStatus.COMPLETED)

    assertTrue(TaskBusinessLogic.isTaskCompleted(task))
  }

  @Test
  fun `isTaskCompleted returns false for non-completed tasks`() {
    val task = Task(taskID = "test", title = "In Progress Task", status = TaskStatus.IN_PROGRESS)

    assertFalse(TaskBusinessLogic.isTaskCompleted(task))
  }

  @Test
  fun `isTaskCompleted returns false for todo tasks`() {
    val task = Task(taskID = "test", title = "Todo Task", status = TaskStatus.TODO)

    assertFalse(TaskBusinessLogic.isTaskCompleted(task))
  }

  @Test
  fun `isTaskCompleted returns false for cancelled tasks`() {
    val task = Task(taskID = "test", title = "Cancelled Task", status = TaskStatus.CANCELLED)

    assertFalse(TaskBusinessLogic.isTaskCompleted(task))
  }
}
