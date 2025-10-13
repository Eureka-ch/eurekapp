package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.enumFromString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TaskStatusTest {

  @Test
  fun taskStatus_shouldHaveCorrectValues() {
    val statuses = TaskStatus.values()
    assertEquals(4, statuses.size)
    assertEquals(TaskStatus.TODO, statuses[0])
    assertEquals(TaskStatus.IN_PROGRESS, statuses[1])
    assertEquals(TaskStatus.COMPLETED, statuses[2])
    assertEquals(TaskStatus.CANCELLED, statuses[3])
  }

  @Test
  fun toString_shouldReturnUppercaseName() {
    assertEquals("TODO", TaskStatus.TODO.toString())
    assertEquals("IN_PROGRESS", TaskStatus.IN_PROGRESS.toString())
    assertEquals("COMPLETED", TaskStatus.COMPLETED.toString())
    assertEquals("CANCELLED", TaskStatus.CANCELLED.toString())
  }

  @Test
  fun enumFromString_shouldReturnCorrectStatusForLowercaseString() {
    assertEquals(TaskStatus.TODO, enumFromString<TaskStatus>("todo"))
    assertEquals(TaskStatus.IN_PROGRESS, enumFromString<TaskStatus>("in_progress"))
    assertEquals(TaskStatus.COMPLETED, enumFromString<TaskStatus>("completed"))
    assertEquals(TaskStatus.CANCELLED, enumFromString<TaskStatus>("cancelled"))
  }

  @Test
  fun enumFromString_shouldReturnCorrectStatusForUppercaseString() {
    assertEquals(TaskStatus.TODO, enumFromString<TaskStatus>("TODO"))
    assertEquals(TaskStatus.IN_PROGRESS, enumFromString<TaskStatus>("IN_PROGRESS"))
    assertEquals(TaskStatus.COMPLETED, enumFromString<TaskStatus>("COMPLETED"))
    assertEquals(TaskStatus.CANCELLED, enumFromString<TaskStatus>("CANCELLED"))
  }

  @Test
  fun enumFromString_shouldReturnCorrectStatusForMixedCaseString() {
    assertEquals(TaskStatus.TODO, enumFromString<TaskStatus>("ToDo"))
    assertEquals(TaskStatus.IN_PROGRESS, enumFromString<TaskStatus>("In_PrOgReSs"))
    assertEquals(TaskStatus.COMPLETED, enumFromString<TaskStatus>("CoMpLeTeD"))
    assertEquals(TaskStatus.CANCELLED, enumFromString<TaskStatus>("CaNcElLeD"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForInvalidString() {
    enumFromString<TaskStatus>("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForEmptyString() {
    enumFromString<TaskStatus>("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForWrongStatus() {
    enumFromString<TaskStatus>("pending")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForLeadingSpace() {
    enumFromString<TaskStatus>(" todo")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForTrailingSpace() {
    enumFromString<TaskStatus>("todo ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForSurroundingSpaces() {
    enumFromString<TaskStatus>(" todo ")
  }

  @Test
  fun valueOf_shouldReturnCorrectStatus() {
    assertEquals(TaskStatus.TODO, TaskStatus.valueOf("TODO"))
    assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.valueOf("IN_PROGRESS"))
    assertEquals(TaskStatus.COMPLETED, TaskStatus.valueOf("COMPLETED"))
    assertEquals(TaskStatus.CANCELLED, TaskStatus.valueOf("CANCELLED"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_shouldThrowExceptionForInvalidString() {
    TaskStatus.valueOf("invalid")
  }

  @Test
  fun enumConstant_shouldHaveCorrectName() {
    assertEquals("TODO", TaskStatus.TODO.name)
    assertEquals("IN_PROGRESS", TaskStatus.IN_PROGRESS.name)
    assertEquals("COMPLETED", TaskStatus.COMPLETED.name)
    assertEquals("CANCELLED", TaskStatus.CANCELLED.name)
  }

  @Test
  fun enumConstant_shouldHaveCorrectOrdinal() {
    assertEquals(0, TaskStatus.TODO.ordinal)
    assertEquals(1, TaskStatus.IN_PROGRESS.ordinal)
    assertEquals(2, TaskStatus.COMPLETED.ordinal)
    assertEquals(3, TaskStatus.CANCELLED.ordinal)
  }

  @Test
  fun enumFromString_shouldReturnNonNullValue() {
    val status = enumFromString<TaskStatus>("todo")
    assertNotNull(status)
    assertEquals(TaskStatus.TODO, status)
  }
}
