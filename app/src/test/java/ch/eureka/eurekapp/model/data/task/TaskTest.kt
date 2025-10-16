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
  fun task_withParameters_setsCorrectValues() {
    val assignedUsers = listOf("user1", "user2")
    val dueDate = Timestamp(1000, 0)
    val attachments = listOf("uri1", "uri2")
    val customData = mapOf("priority" to "high", "hours" to 5)
    val task =
        Task(
            taskID = "task123",
            templateId = "tmpl123",
            projectId = "prj123",
            title = "Test Task",
            description = "Test Description",
            status = TaskStatus.IN_PROGRESS,
            assignedUserIds = assignedUsers,
            dueDate = dueDate,
            attachmentUrls = attachments,
            customData = customData)

    assertEquals("task123", task.taskID)
    assertEquals("tmpl123", task.templateId)
    assertEquals("prj123", task.projectId)
    assertEquals("Test Task", task.title)
    assertEquals("Test Description", task.description)
    assertEquals(TaskStatus.IN_PROGRESS, task.status)
    assertEquals(assignedUsers, task.assignedUserIds)
    assertEquals(dueDate, task.dueDate)
    assertEquals(attachments, task.attachmentUrls)
    assertEquals(customData, task.customData)
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
    val task =
        Task(
            taskID = "task123",
            templateId = "tmpl123",
            projectId = "prj123",
            title = "Test Task",
            description = "Test Description")
    val taskString = task.toString()

    assert(taskString.contains("task123"))
    assert(taskString.contains("tmpl123"))
    assert(taskString.contains("prj123"))
    assert(taskString.contains("Test Task"))
    assert(taskString.contains("Test Description"))
  }

  @Test
  fun task_withCreatedBy_setsCorrectValue() {
    val task = Task(taskID = "task123", createdBy = "user123")

    assertEquals("user123", task.createdBy)
  }

  @Test
  fun task_defaultConstructor_setsEmptyCreatedBy() {
    val task = Task()

    assertEquals("", task.createdBy)
  }

  @Test
  fun task_withAllTaskStatusValues_setsCorrectStatus() {
    val todoTask = Task(taskID = "1", status = TaskStatus.TODO)
    val inProgressTask = Task(taskID = "2", status = TaskStatus.IN_PROGRESS)
    val completedTask = Task(taskID = "3", status = TaskStatus.COMPLETED)
    val cancelledTask = Task(taskID = "4", status = TaskStatus.CANCELLED)

    assertEquals(TaskStatus.TODO, todoTask.status)
    assertEquals(TaskStatus.IN_PROGRESS, inProgressTask.status)
    assertEquals(TaskStatus.COMPLETED, completedTask.status)
    assertEquals(TaskStatus.CANCELLED, cancelledTask.status)
  }

  @Test
  fun task_withEmptyLists_setsCorrectValues() {
    val task =
        Task(
            taskID = "task123",
            assignedUserIds = emptyList(),
            attachmentUrls = emptyList(),
            customData = emptyMap())

    assertEquals(emptyList<String>(), task.assignedUserIds)
    assertEquals(emptyList<String>(), task.attachmentUrls)
    assertEquals(emptyMap<String, Any>(), task.customData)
  }

  @Test
  fun task_withSingleAssignedUser_setsCorrectValue() {
    val task = Task(taskID = "task123", assignedUserIds = listOf("user1"))

    assertEquals(listOf("user1"), task.assignedUserIds)
  }

  @Test
  fun task_withMultipleAssignedUsers_setsCorrectValues() {
    val users = listOf("user1", "user2", "user3")
    val task = Task(taskID = "task123", assignedUserIds = users)

    assertEquals(users, task.assignedUserIds)
  }

  @Test
  fun task_withSingleAttachment_setsCorrectValue() {
    val task = Task(taskID = "task123", attachmentUrls = listOf("url1"))

    assertEquals(listOf("url1"), task.attachmentUrls)
  }

  @Test
  fun task_withMultipleAttachments_setsCorrectValues() {
    val attachments = listOf("url1", "url2", "url3")
    val task = Task(taskID = "task123", attachmentUrls = attachments)

    assertEquals(attachments, task.attachmentUrls)
  }

  @Test
  fun task_withSingleCustomDataField_setsCorrectValue() {
    val customData = mapOf("priority" to "high")
    val task = Task(taskID = "task123", customData = customData)

    assertEquals(customData, task.customData)
  }

  @Test
  fun task_withMultipleCustomDataFields_setsCorrectValues() {
    val customData =
        mapOf("priority" to "high", "hours" to 5, "category" to "bug", "estimated" to true)
    val task = Task(taskID = "task123", customData = customData)

    assertEquals(customData, task.customData)
  }

  @Test
  fun task_withDifferentDataTypesInCustomData_setsCorrectValues() {
    val customData =
        mapOf(
            "stringField" to "value", "intField" to 42, "boolField" to true, "doubleField" to 3.14)
    val task = Task(taskID = "task123", customData = customData)

    assertEquals(customData, task.customData)
  }

  @Test
  fun task_withDueDate_setsCorrectValue() {
    val dueDate = Timestamp(1234567890, 0)
    val task = Task(taskID = "task123", dueDate = dueDate)

    assertEquals(dueDate, task.dueDate)
  }

  @Test
  fun task_withDifferentDueDates_setsCorrectValues() {
    val dueDate1 = Timestamp(1000, 0)
    val dueDate2 = Timestamp(2000, 0)
    val task1 = Task(taskID = "task1", dueDate = dueDate1)
    val task2 = Task(taskID = "task2", dueDate = dueDate2)

    assertEquals(dueDate1, task1.dueDate)
    assertEquals(dueDate2, task2.dueDate)
    assertNotEquals(task1.dueDate, task2.dueDate)
  }

  @Test
  fun task_copyWithDifferentStatus_createsCorrectInstance() {
    val originalTask = Task(taskID = "task123", status = TaskStatus.TODO)
    val updatedTask = originalTask.copy(status = TaskStatus.COMPLETED)

    assertEquals(TaskStatus.TODO, originalTask.status)
    assertEquals(TaskStatus.COMPLETED, updatedTask.status)
    assertEquals("task123", updatedTask.taskID)
  }

  @Test
  fun task_copyWithDifferentDueDate_createsCorrectInstance() {
    val originalTask = Task(taskID = "task123", dueDate = null)
    val newDueDate = Timestamp(1234567890, 0)
    val updatedTask = originalTask.copy(dueDate = newDueDate)

    assertNull(originalTask.dueDate)
    assertEquals(newDueDate, updatedTask.dueDate)
    assertEquals("task123", updatedTask.taskID)
  }

  @Test
  fun task_copyWithDifferentAssignedUsers_createsCorrectInstance() {
    val originalTask = Task(taskID = "task123", assignedUserIds = emptyList())
    val newUsers = listOf("user1", "user2")
    val updatedTask = originalTask.copy(assignedUserIds = newUsers)

    assertEquals(emptyList<String>(), originalTask.assignedUserIds)
    assertEquals(newUsers, updatedTask.assignedUserIds)
    assertEquals("task123", updatedTask.taskID)
  }

  @Test
  fun task_withMinimalRequiredFields_createsValidTask() {
    val task = Task(taskID = "task123")

    assertEquals("task123", task.taskID)
    assertEquals("", task.templateId)
    assertEquals("", task.projectId)
    assertEquals("", task.title)
    assertEquals("", task.description)
    assertEquals(TaskStatus.TODO, task.status)
    assertEquals(emptyList<String>(), task.assignedUserIds)
    assertNull(task.dueDate)
    assertEquals(emptyList<String>(), task.attachmentUrls)
    assertEquals(emptyMap<String, Any>(), task.customData)
    assertEquals("", task.createdBy)
  }

  @Test
  fun task_withMaximalFields_createsValidTask() {
    val assignedUsers = listOf("user1", "user2", "user3")
    val dueDate = Timestamp(1234567890, 0)
    val attachments = listOf("url1", "url2", "url3")
    val customData =
        mapOf("priority" to "high", "hours" to 8, "category" to "feature", "estimated" to false)

    val task =
        Task(
            taskID = "task123",
            templateId = "tmpl123",
            projectId = "prj123",
            title = "Complete Task",
            description = "Full description",
            status = TaskStatus.IN_PROGRESS,
            assignedUserIds = assignedUsers,
            dueDate = dueDate,
            attachmentUrls = attachments,
            customData = customData,
            createdBy = "creator123")

    assertEquals("task123", task.taskID)
    assertEquals("tmpl123", task.templateId)
    assertEquals("prj123", task.projectId)
    assertEquals("Complete Task", task.title)
    assertEquals("Full description", task.description)
    assertEquals(TaskStatus.IN_PROGRESS, task.status)
    assertEquals(assignedUsers, task.assignedUserIds)
    assertEquals(dueDate, task.dueDate)
    assertEquals(attachments, task.attachmentUrls)
    assertEquals(customData, task.customData)
    assertEquals("creator123", task.createdBy)
  }
}
