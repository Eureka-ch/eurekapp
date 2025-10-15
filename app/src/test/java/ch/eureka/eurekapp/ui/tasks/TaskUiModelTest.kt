package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.user.User
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskUiModelTest {

  @Test
  fun `TaskUiModel returns correct id`() {
    val task = Task(taskID = "test-id")
    val uiModel = TaskUiModel(task = task)

    assertEquals("test-id", uiModel.id)
  }

  @Test
  fun `TaskUiModel returns task title when not blank`() {
    val task = Task(taskID = "1", title = "Test Task")
    val uiModel = TaskUiModel(task = task)

    assertEquals("Test Task", uiModel.title)
  }

  @Test
  fun `TaskUiModel returns template title when task title is blank`() {
    val task = Task(taskID = "1", title = "")
    val template = TaskTemplate(templateID = "t1", title = "Template Title")
    val uiModel = TaskUiModel(task = task, template = template)

    assertEquals("Template Title", uiModel.title)
  }

  @Test
  fun `TaskUiModel returns Untitled Task when both task and template titles are blank`() {
    val task = Task(taskID = "1", title = "")
    val uiModel = TaskUiModel(task = task)

    assertEquals("Untitled Task", uiModel.title)
  }

  @Test
  fun `TaskUiModel returns template title when task title is null`() {
    val task = Task(taskID = "1", title = "")
    val template = TaskTemplate(templateID = "t1", title = "Template Title")
    val uiModel = TaskUiModel(task = task, template = template)

    assertEquals("Template Title", uiModel.title)
  }

  @Test
  fun `TaskUiModel returns assignee name when available`() {
    val task = Task(taskID = "1")
    val assignee = User(uid = "u1", displayName = "John Doe")
    val uiModel = TaskUiModel(task = task, assignee = assignee)

    assertEquals("John Doe", uiModel.assigneeName)
  }

  @Test
  fun `TaskUiModel returns Unassigned when no assignee`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task)

    assertEquals("Unassigned", uiModel.assigneeName)
  }

  @Test
  fun `TaskUiModel returns assignee name when assignee has empty display name`() {
    val task = Task(taskID = "1")
    val assignee = User(uid = "u1", displayName = "")
    val uiModel = TaskUiModel(task = task, assignee = assignee)

    assertEquals("", uiModel.assigneeName)
  }

  @Test
  fun `TaskUiModel returns correct progress text`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = 0.5f)

    assertEquals("50%", uiModel.progressText)
  }

  @Test
  fun `TaskUiModel returns correct progress value`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = 0.75f)

    assertEquals(0.75f, uiModel.progressValue, 0.01f)
  }

  @Test
  fun `TaskUiModel returns correct isCompleted value`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, isCompleted = true)

    assertEquals(true, uiModel.isCompleted)
  }

  @Test
  fun `TaskUiModel handles zero progress`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = 0.0f)

    assertEquals("0%", uiModel.progressText)
    assertEquals(0.0f, uiModel.progressValue, 0.01f)
  }

  @Test
  fun `TaskUiModel handles full progress`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = 1.0f)

    assertEquals("100%", uiModel.progressText)
    assertEquals(1.0f, uiModel.progressValue, 0.01f)
  }

  @Test
  fun `TaskUiModel handles negative progress`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = -0.1f)

    assertEquals("-10%", uiModel.progressText)
    assertEquals(-0.1f, uiModel.progressValue, 0.01f)
  }

  @Test
  fun `TaskUiModel handles progress over 100 percent`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = 1.5f)

    assertEquals("150%", uiModel.progressText)
    assertEquals(1.5f, uiModel.progressValue, 0.01f)
  }

  @Test
  fun `TaskUiModel handles decimal progress`() {
    val task = Task(taskID = "1")
    val uiModel = TaskUiModel(task = task, progress = 0.333f)

    assertEquals("33%", uiModel.progressText)
    assertEquals(0.333f, uiModel.progressValue, 0.01f)
  }

  @Test
  fun `TaskUiModel with all parameters`() {
    val task = Task(taskID = "1", title = "Test Task")
    val template = TaskTemplate(templateID = "t1", title = "Template")
    val assignee = User(uid = "u1", displayName = "John Doe")
    val uiModel =
        TaskUiModel(
            task = task,
            template = template,
            assignee = assignee,
            progress = 0.5f,
            isCompleted = true)

    assertEquals("1", uiModel.id)
    assertEquals("Test Task", uiModel.title)
    assertEquals("John Doe", uiModel.assigneeName)
    assertEquals("50%", uiModel.progressText)
    assertEquals(0.5f, uiModel.progressValue, 0.01f)
    assertEquals(true, uiModel.isCompleted)
  }
}
