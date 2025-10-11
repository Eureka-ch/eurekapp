package ch.eureka.eurekapp.model.data.task

import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Test suite for Task model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class TaskTest {

  @Test
  fun task_defaultConstructor_createsEmptyTask() {
    val task = Task()

    assertEquals("", task.taskID)
    assertEquals("", task.templateId)
    assertEquals("", task.projectId)
    assertEquals(emptyList<String>(), task.assignedUserIds)
    assertNull(task.dueDate)
  }

  @Test
  fun task_withParameters_setsCorrectValues() {
    val assignedUsers = listOf("user1", "user2")
    val dueDate = Timestamp(1000, 0)
    val task =
        Task(
            taskID = "task123",
            templateId = "tmpl123",
            projectId = "prj123",
            assignedUserIds = assignedUsers,
            dueDate = dueDate)

    assertEquals("task123", task.taskID)
    assertEquals("tmpl123", task.templateId)
    assertEquals("prj123", task.projectId)
    assertEquals(assignedUsers, task.assignedUserIds)
    assertEquals(dueDate, task.dueDate)
  }

  @Test
  fun task_withoutDueDate_setsNullDueDate() {
    val task = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")

    assertNull(task.dueDate)
  }

  @Test
  fun task_copy_createsNewInstance() {
    val task = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")
    val copiedTask = task.copy(assignedUserIds = listOf("user1", "user2"))

    assertEquals("task123", copiedTask.taskID)
    assertEquals("tmpl123", copiedTask.templateId)
    assertEquals("prj123", copiedTask.projectId)
    assertEquals(listOf("user1", "user2"), copiedTask.assignedUserIds)
  }

  @Test
  fun task_equals_comparesCorrectly() {
    val task1 = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")
    val task2 = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")
    val task3 = Task(taskID = "task456", templateId = "tmpl456", projectId = "prj456")

    assertEquals(task1, task2)
    assertNotEquals(task1, task3)
  }

  @Test
  fun task_hashCode_isConsistent() {
    val task1 = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")
    val task2 = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")

    assertEquals(task1.hashCode(), task2.hashCode())
  }

  @Test
  fun task_toString_containsAllFields() {
    val task = Task(taskID = "task123", templateId = "tmpl123", projectId = "prj123")
    val taskString = task.toString()

    assert(taskString.contains("task123"))
    assert(taskString.contains("tmpl123"))
    assert(taskString.contains("prj123"))
  }
}
