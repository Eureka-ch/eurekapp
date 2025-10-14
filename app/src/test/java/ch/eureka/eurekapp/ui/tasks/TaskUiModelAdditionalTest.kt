package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Additional tests for TaskUiModel to improve coverage
 *
 * These tests cover edge cases and additional scenarios to ensure comprehensive testing of the
 * TaskUiModel logic.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
class TaskUiModelAdditionalTest {

  @Test
  fun `formatDueDate thisWeek returns correct format`() {
    // Given
    val in5Days = Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in5Days)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.dueDate

    // Then
    assertEquals("Due in 5 days", result)
  }

  @Test
  fun `formatDueDate future returns formatted date`() {
    // Given
    val futureDate = Date(System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(futureDate)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.dueDate

    // Then
    assertTrue(result.startsWith("Due "))
    assertTrue(
        result.contains("Jan") ||
            result.contains("Feb") ||
            result.contains("Mar") ||
            result.contains("Apr") ||
            result.contains("May") ||
            result.contains("Jun") ||
            result.contains("Jul") ||
            result.contains("Aug") ||
            result.contains("Sep") ||
            result.contains("Oct") ||
            result.contains("Nov") ||
            result.contains("Dec"))
  }

  @Test
  fun `determinePriority handles unknown priority`() {
    // Given
    val task = Task(customData = mapOf("priority" to "UNKNOWN"))
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.priority

    // Then
    // Priority is determined by due date, not customData
    assertEquals("Low Priority", result) // No due date = Low Priority
  }

  @Test
  fun `determinePriority based on due date - overdue`() {
    // Given
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.priority

    // Then
    assertEquals("Critical Priority", result)
  }

  @Test
  fun `determinePriority based on due date - today`() {
    // Given
    val today = Date()
    val timestamp = Timestamp(today)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.priority

    // Then
    assertEquals("High Priority", result)
  }

  @Test
  fun `determinePriority based on due date - tomorrow`() {
    // Given
    val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.priority

    // Then
    assertEquals("High Priority", result)
  }

  @Test
  fun `determinePriority based on due date - medium`() {
    // Given
    val in2Days = Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in2Days)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.priority

    // Then
    assertEquals("Medium Priority", result)
  }

  @Test
  fun `determinePriority based on due date - low`() {
    // Given
    val in5Days = Date(System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in5Days)
    val task = Task(dueDate = timestamp)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.priority

    // Then
    assertEquals("Low Priority", result)
  }

  @Test
  fun `progressText handles edge values`() {
    // Given
    val task = Task()
    val uiModel = TaskUiModel(task = task, progress = 0.0f)

    // When
    val result = uiModel.progressText

    // Then
    assertEquals("0%", result)
  }

  @Test
  fun `progressText handles maximum value`() {
    // Given
    val task = Task()
    val uiModel = TaskUiModel(task = task, progress = 1.0f)

    // When
    val result = uiModel.progressText

    // Then
    assertEquals("100%", result)
  }

  @Test
  fun `progressText handles decimal values`() {
    // Given
    val task = Task()
    val uiModel = TaskUiModel(task = task, progress = 0.333f)

    // When
    val result = uiModel.progressText

    // Then
    assertEquals("33%", result)
  }

  @Test
  fun `progressValue returns exact value`() {
    // Given
    val task = Task()
    val uiModel = TaskUiModel(task = task, progress = 0.789f)

    // When
    val result = uiModel.progressValue

    // Then
    assertEquals(0.789f, result, 0.001f)
  }

  @Test
  fun `isCompleted handles different statuses`() {
    // Given
    val todoTask = Task(status = TaskStatus.TODO)
    val inProgressTask = Task(status = TaskStatus.IN_PROGRESS)
    val completedTask = Task(status = TaskStatus.COMPLETED)
    val cancelledTask = Task(status = TaskStatus.CANCELLED)

    // When
    val todoModel =
        TaskUiModel(task = todoTask, isCompleted = TaskBusinessLogic.isTaskCompleted(todoTask))
    val inProgressModel =
        TaskUiModel(
            task = inProgressTask, isCompleted = TaskBusinessLogic.isTaskCompleted(inProgressTask))
    val completedModel =
        TaskUiModel(
            task = completedTask, isCompleted = TaskBusinessLogic.isTaskCompleted(completedTask))
    val cancelledModel =
        TaskUiModel(
            task = cancelledTask, isCompleted = TaskBusinessLogic.isTaskCompleted(cancelledTask))

    // Then
    assertFalse(todoModel.isCompleted)
    assertFalse(inProgressModel.isCompleted)
    assertTrue(completedModel.isCompleted)
    assertFalse(cancelledModel.isCompleted)
  }

  @Test
  fun `tags generation handles different statuses`() {
    // Given
    val todoTask = Task(status = TaskStatus.TODO)
    val inProgressTask = Task(status = TaskStatus.IN_PROGRESS)
    val completedTask = Task(status = TaskStatus.COMPLETED)

    // When
    val todoModel =
        TaskUiModel(task = todoTask, isCompleted = TaskBusinessLogic.isTaskCompleted(todoTask))
    val inProgressModel =
        TaskUiModel(
            task = inProgressTask, isCompleted = TaskBusinessLogic.isTaskCompleted(inProgressTask))
    val completedModel =
        TaskUiModel(
            task = completedTask, isCompleted = TaskBusinessLogic.isTaskCompleted(completedTask))

    // Then
    assertTrue(todoModel.tags.isNotEmpty())
    assertTrue(inProgressModel.tags.isNotEmpty())
    assertTrue(completedModel.tags.isNotEmpty())
  }

  @Test
  fun `id returns correct task ID`() {
    // Given
    val taskId = "test-task-123"
    val task = Task(taskID = taskId)
    val uiModel = TaskUiModel(task = task)

    // When
    val result = uiModel.id

    // Then
    assertEquals(taskId, result)
  }

  @Test
  fun `title with empty template returns untitled`() {
    // Given
    val task = Task(title = "")
    val uiModel = TaskUiModel(task = task, template = null)

    // When
    val result = uiModel.title

    // Then
    assertEquals("Untitled Task", result)
  }
}
