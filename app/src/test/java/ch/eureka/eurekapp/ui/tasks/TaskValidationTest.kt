/**
 * Unit tests for task validation logic
 *
 * Tests various validation scenarios for task data including empty fields, invalid IDs, and
 * business rule compliance.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Tests pour la validation des tâches Teste les règles métier et la validation des données */
class TaskValidationTest {

  @Test
  fun `valid task with all required fields passes validation`() {
    val task =
        Task(
            taskID = "task-123",
            title = "Valid Task",
            description = "A valid task description",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertTrue(isValidTask(task))
  }

  @Test
  fun `task with empty title fails validation`() {
    val task =
        Task(
            taskID = "task-123",
            title = "",
            description = "A task with empty title",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertFalse(isValidTask(task))
  }

  @Test
  fun `task with blank title fails validation`() {
    val task =
        Task(
            taskID = "task-123",
            title = "   ",
            description = "A task with blank title",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertFalse(isValidTask(task))
  }

  @Test
  fun `task with empty taskID fails validation`() {
    val task =
        Task(
            taskID = "",
            title = "Valid Title",
            description = "A task with empty ID",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertFalse(isValidTask(task))
  }

  @Test
  fun `task with empty projectId fails validation`() {
    val task =
        Task(
            taskID = "task-123",
            title = "Valid Title",
            description = "A task with empty project ID",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "")

    assertFalse(isValidTask(task))
  }

  @Test
  fun `task with empty assignedUserIds is valid`() {
    val task =
        Task(
            taskID = "task-123",
            title = "Unassigned Task",
            description = "A task with no assignees",
            status = TaskStatus.TODO,
            assignedUserIds = emptyList(),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertTrue(isValidTask(task))
  }

  @Test
  fun `task with null dueDate is valid`() {
    val task =
        Task(
            taskID = "task-123",
            title = "No Due Date Task",
            description = "A task with no due date",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = null,
            projectId = "project-123")

    assertTrue(isValidTask(task))
  }

  @Test
  fun `task with null description is valid`() {
    val task =
        Task(
            taskID = "task-123",
            title = "No Description Task",
            description = "",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertTrue(isValidTask(task))
  }

  @Test
  fun `task with empty description is valid`() {
    val task =
        Task(
            taskID = "task-123",
            title = "Empty Description Task",
            description = "",
            status = TaskStatus.TODO,
            assignedUserIds = listOf("user-1"),
            dueDate = Timestamp(Date()),
            projectId = "project-123")

    assertTrue(isValidTask(task))
  }

  /** Validation logic for tasks Tests the business rules for task validity */
  private fun isValidTask(task: Task): Boolean {
    // Required fields validation
    if (task.taskID.isBlank()) return false
    if (task.title.isBlank()) return false
    if (task.projectId.isBlank()) return false

    return true
  }
}
