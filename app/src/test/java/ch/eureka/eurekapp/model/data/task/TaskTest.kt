package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.template.field.FieldValue
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
    val customData =
        TaskCustomData(
            mapOf(
                "priority" to FieldValue.TextValue("high"), "hours" to FieldValue.NumberValue(5.0)))
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
            customData = TaskCustomData())

    assertEquals(emptyList<String>(), task.assignedUserIds)
    assertEquals(emptyList<String>(), task.attachmentUrls)
    assertEquals(TaskCustomData(), task.customData)
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
    val customData = TaskCustomData(mapOf("priority" to FieldValue.TextValue("high")))
    val task = Task(taskID = "task123", customData = customData)

    assertEquals(customData, task.customData)
  }

  @Test
  fun task_withMultipleCustomDataFields_setsCorrectValues() {
    val customData =
        TaskCustomData(
            mapOf(
                "priority" to FieldValue.TextValue("high"),
                "hours" to FieldValue.NumberValue(5.0),
                "category" to FieldValue.TextValue("bug")))
    val task = Task(taskID = "task123", customData = customData)

    assertEquals(customData, task.customData)
  }

  @Test
  fun task_withDifferentDataTypesInCustomData_setsCorrectValues() {
    val customData =
        TaskCustomData(
            mapOf(
                "stringField" to FieldValue.TextValue("value"),
                "numberField" to FieldValue.NumberValue(42.0),
                "dateField" to FieldValue.DateValue("2024-01-15")))
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
    assertEquals(TaskCustomData(), task.customData)
    assertEquals("", task.createdBy)
  }

  @Test
  fun task_withMaximalFields_createsValidTask() {
    val assignedUsers = listOf("user1", "user2", "user3")
    val dueDate = Timestamp(1234567890, 0)
    val attachments = listOf("url1", "url2", "url3")
    val customData =
        TaskCustomData(
            mapOf(
                "priority" to FieldValue.TextValue("high"),
                "hours" to FieldValue.NumberValue(8.0),
                "category" to FieldValue.TextValue("feature")))

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

  @Test
  fun getDaysUntilDue_withNullDueDate_returnsNull() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = null)
    assertNull(getDaysUntilDue(task, now))
  }

  @Test
  fun getDaysUntilDue_withPastDueDate_returnsNegativeValue() {
    val now = Timestamp.now()
    val yesterday = java.util.Date(now.toDate().time - 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(yesterday))
    val result = getDaysUntilDue(task, now)
    assertEquals(-1L, result)
  }

  @Test
  fun getDaysUntilDue_withTodayDueDate_returnsZero() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = now)
    val result = getDaysUntilDue(task, now)
    assertEquals(0L, result)
  }

  @Test
  fun getDaysUntilDue_withTomorrowDueDate_returnsOne() {
    val now = Timestamp.now()
    val tomorrow = java.util.Date(now.toDate().time + 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(tomorrow))
    val result = getDaysUntilDue(task, now)
    assertEquals(1L, result)
  }

  @Test
  fun getDaysUntilDue_withFutureDueDate_returnsCorrectDays() {
    val now = Timestamp.now()
    val sevenDaysFromNow = java.util.Date(now.toDate().time + 7 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(sevenDaysFromNow))
    val result = getDaysUntilDue(task, now)
    assertEquals(7L, result)
  }

  @Test
  fun determinePriority_withNullDueDate_returnsLowPriority() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = null)
    val result = determinePriority(task, now)
    assertEquals("Low Priority", result)
  }

  @Test
  fun determinePriority_withOverdueDueDate_returnsCriticalPriority() {
    val now = Timestamp.now()
    val yesterday = java.util.Date(now.toDate().time - 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(yesterday))
    val result = determinePriority(task, now)
    assertEquals("Critical Priority", result)
  }

  @Test
  fun determinePriority_withTodayDueDate_returnsHighPriority() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = now)
    val result = determinePriority(task, now)
    assertEquals("High Priority", result)
  }

  @Test
  fun determinePriority_withTomorrowDueDate_returnsHighPriority() {
    val now = Timestamp.now()
    val tomorrow = java.util.Date(now.toDate().time + 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(tomorrow))
    val result = determinePriority(task, now)
    assertEquals("High Priority", result)
  }

  @Test
  fun determinePriority_withTwoDaysFromNowDueDate_returnsMediumPriority() {
    val now = Timestamp.now()
    val twoDaysFromNow = java.util.Date(now.toDate().time + 2 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(twoDaysFromNow))
    val result = determinePriority(task, now)
    assertEquals("Medium Priority", result)
  }

  @Test
  fun determinePriority_withThreeDaysFromNowDueDate_returnsMediumPriority() {
    val now = Timestamp.now()
    val threeDaysFromNow = java.util.Date(now.toDate().time + 3 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(threeDaysFromNow))
    val result = determinePriority(task, now)
    assertEquals("Medium Priority", result)
  }

  @Test
  fun determinePriority_withFourDaysFromNowDueDate_returnsLowPriority() {
    val now = Timestamp.now()
    val fourDaysFromNow = java.util.Date(now.toDate().time + 4 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(fourDaysFromNow))
    val result = determinePriority(task, now)
    assertEquals("Low Priority", result)
  }

  @Test
  fun determinePriority_withWeekFromNowDueDate_returnsLowPriority() {
    val now = Timestamp.now()
    val sevenDaysFromNow = java.util.Date(now.toDate().time + 7 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(sevenDaysFromNow))
    val result = determinePriority(task, now)
    assertEquals("Low Priority", result)
  }

  @Test
  fun getDaysUntilDue_withExactly23Hours_returnsOne() {
    val now = Timestamp.now()
    val calendar =
        java.util.Calendar.getInstance().apply {
          time = now.toDate()
          set(java.util.Calendar.HOUR_OF_DAY, 1)
          set(java.util.Calendar.MINUTE, 0)
          set(java.util.Calendar.SECOND, 0)
          set(java.util.Calendar.MILLISECOND, 0)
        }
    val nowNormalized = Timestamp(calendar.time)

    val dueCalendar =
        java.util.Calendar.getInstance().apply {
          time = calendar.time
          add(java.util.Calendar.HOUR_OF_DAY, 23)
        }
    val almostOneDay = Timestamp(dueCalendar.time)
    val task = Task(taskID = "1", title = "Test Task", dueDate = almostOneDay)
    val result = getDaysUntilDue(task, nowNormalized)
    assertEquals(1L, result)
  }

  @Test
  fun getDaysUntilDue_withExactly25Hours_returnsOne() {
    val now = Timestamp.now()
    val calendar =
        java.util.Calendar.getInstance().apply {
          time = now.toDate()
          set(java.util.Calendar.HOUR_OF_DAY, 1)
          set(java.util.Calendar.MINUTE, 0)
          set(java.util.Calendar.SECOND, 0)
          set(java.util.Calendar.MILLISECOND, 0)
        }
    val nowNormalized = Timestamp(calendar.time)

    val dueCalendar =
        java.util.Calendar.getInstance().apply {
          time = calendar.time
          add(java.util.Calendar.HOUR_OF_DAY, 25)
        }
    val moreThanOneDay = Timestamp(dueCalendar.time)
    val task = Task(taskID = "1", title = "Test Task", dueDate = moreThanOneDay)
    val result = getDaysUntilDue(task, nowNormalized)
    assertEquals(1L, result)
  }

  @Test
  fun getDaysUntilDue_withTwoWeeksAway_returnsCorrectDays() {
    val now = Timestamp.now()
    val twoWeeksFromNow = java.util.Date(now.toDate().time + 14 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(twoWeeksFromNow))
    val result = getDaysUntilDue(task, now)
    assertEquals(14L, result)
  }

  @Test
  fun determinePriority_withBoundaryOneDay_returnsHighPriority() {
    val now = Timestamp.now()
    val exactlyOneDay = java.util.Date(now.toDate().time + 1 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(exactlyOneDay))
    val result = determinePriority(task, now)
    assertEquals("High Priority", result)
  }

  @Test
  fun determinePriority_withBoundaryThreeDays_returnsMediumPriority() {
    val now = Timestamp.now()
    val exactlyThreeDays = java.util.Date(now.toDate().time + 3 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(exactlyThreeDays))
    val result = determinePriority(task, now)
    assertEquals("Medium Priority", result)
  }

  @Test
  fun determinePriority_withVeryDistantFuture_returnsLowPriority() {
    val now = Timestamp.now()
    val farFuture = java.util.Date(now.toDate().time + 365 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(farFuture))
    val result = determinePriority(task, now)
    assertEquals("Low Priority", result)
  }

  @Test
  fun getDaysUntilDue_withSameTimestamp_returnsZero() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = now)
    val result = getDaysUntilDue(task, now)
    assertEquals(0L, result)
  }

  @Test
  fun getDaysUntilDue_withOneSecondDifference_returnsZero() {
    val now = Timestamp.now()
    val oneSecondLater = java.util.Date(now.toDate().time + 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(oneSecondLater))
    val result = getDaysUntilDue(task, now)
    assertEquals(0L, result)
  }

  @Test
  fun determinePriority_withExactlyNegativeOne_returnsCritical() {
    val now = Timestamp.now()
    val oneDayAgo = java.util.Date(now.toDate().time - 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(oneDayAgo))
    val result = determinePriority(task, now)
    assertEquals("Critical Priority", result)
  }

  @Test
  fun task_withReminderTime_setsCorrectValue() {
    val reminderTime = Timestamp(1234567890, 0)
    val task = Task(taskID = "task123", reminderTime = reminderTime)

    assertEquals(reminderTime, task.reminderTime)
  }

  @Test
  fun task_withoutReminderTime_setsNullReminderTime() {
    val task = Task(taskID = "task123")

    assertEquals(null, task.reminderTime)
  }

  @Test
  fun getHoursUntilDue_withNullDueDate_returnsNull() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = null)
    assertNull(getHoursUntilDue(task, now))
  }

  @Test
  fun getHoursUntilDue_withPastDueDate_returnsNegativeValue() {
    val now = Timestamp.now()
    val twoHoursAgo = java.util.Date(now.toDate().time - 2 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(twoHoursAgo))
    val result = getHoursUntilDue(task, now)
    assertEquals(-2L, result)
  }

  @Test
  fun getHoursUntilDue_withOneHourFromNow_returnsOne() {
    val now = Timestamp.now()
    val oneHourFromNow = java.util.Date(now.toDate().time + 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(oneHourFromNow))
    val result = getHoursUntilDue(task, now)
    assertEquals(1L, result)
  }

  @Test
  fun getDueDateTag_withNullDueDate_returnsNull() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = null)
    assertNull(getDueDateTag(task, now))
  }

  @Test
  fun getDueDateTag_withOverdueTask_returnsOverdue() {
    val now = Timestamp.now()
    val oneHourAgo = java.util.Date(now.toDate().time - 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(oneHourAgo))
    val result = getDueDateTag(task, now)
    assertEquals("Overdue", result)
  }

  @Test
  fun getDueDateTag_withTaskDueNow_returnsDueNow() {
    val now = Timestamp.now()
    val task = Task(taskID = "1", title = "Test Task", dueDate = now)
    val result = getDueDateTag(task, now)
    assertEquals("Due now", result)
  }

  @Test
  fun getDueDateTag_withTaskDueInOneHour_returnsDueIn1Hour() {
    val now = Timestamp.now()
    val oneHourFromNow = java.util.Date(now.toDate().time + 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(oneHourFromNow))
    val result = getDueDateTag(task, now)
    assertEquals("Due in 1 hour", result)
  }

  @Test
  fun getDueDateTag_withTaskDueInThreeHours_returnsDueIn3Hours() {
    val now = Timestamp.now()
    val threeHoursFromNow = java.util.Date(now.toDate().time + 3 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(threeHoursFromNow))
    val result = getDueDateTag(task, now)
    assertEquals("Due in 3 hours", result)
  }

  @Test
  fun getDueDateTag_withTaskDueToday_returnsDueToday() {
    val now = Timestamp.now()
    val twelveHoursFromNow = java.util.Date(now.toDate().time + 12 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(twelveHoursFromNow))
    val result = getDueDateTag(task, now)
    assertEquals("Due today", result)
  }

  @Test
  fun getDueDateTag_withTaskDueMoreThan24Hours_returnsNull() {
    val now = Timestamp.now()
    val twoDaysFromNow = java.util.Date(now.toDate().time + 2 * 24 * 60 * 60 * 1000)
    val task = Task(taskID = "1", title = "Test Task", dueDate = Timestamp(twoDaysFromNow))
    val result = getDueDateTag(task, now)
    assertNull(result)
  }
}
