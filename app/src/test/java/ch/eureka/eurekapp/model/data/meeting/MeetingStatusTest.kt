package ch.eureka.eurekapp.model.data.meeting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class MeetingStatusTest {

  @Test
  fun meetingStatus_shouldHaveCorrectValues() {
    val statuses = MeetingStatus.values()
    assertEquals(4, statuses.size)
    assertEquals(MeetingStatus.SCHEDULED, statuses[0])
    assertEquals(MeetingStatus.IN_PROGRESS, statuses[1])
    assertEquals(MeetingStatus.COMPLETED, statuses[2])
    assertEquals(MeetingStatus.CANCELLED, statuses[3])
  }

  @Test
  fun toString_shouldReturnLowercaseName() {
    assertEquals("scheduled", MeetingStatus.SCHEDULED.toString())
    assertEquals("in_progress", MeetingStatus.IN_PROGRESS.toString())
    assertEquals("completed", MeetingStatus.COMPLETED.toString())
    assertEquals("cancelled", MeetingStatus.CANCELLED.toString())
  }

  @Test
  fun fromString_shouldReturnCorrectStatusForLowercaseString() {
    assertEquals(MeetingStatus.SCHEDULED, MeetingStatus.fromString("scheduled"))
    assertEquals(MeetingStatus.IN_PROGRESS, MeetingStatus.fromString("in_progress"))
    assertEquals(MeetingStatus.COMPLETED, MeetingStatus.fromString("completed"))
    assertEquals(MeetingStatus.CANCELLED, MeetingStatus.fromString("cancelled"))
  }

  @Test
  fun fromString_shouldReturnCorrectStatusForUppercaseString() {
    assertEquals(MeetingStatus.SCHEDULED, MeetingStatus.fromString("SCHEDULED"))
    assertEquals(MeetingStatus.IN_PROGRESS, MeetingStatus.fromString("IN_PROGRESS"))
    assertEquals(MeetingStatus.COMPLETED, MeetingStatus.fromString("COMPLETED"))
    assertEquals(MeetingStatus.CANCELLED, MeetingStatus.fromString("CANCELLED"))
  }

  @Test
  fun fromString_shouldReturnCorrectStatusForMixedCaseString() {
    assertEquals(MeetingStatus.SCHEDULED, MeetingStatus.fromString("ScHeDuLeD"))
    assertEquals(MeetingStatus.IN_PROGRESS, MeetingStatus.fromString("In_PrOgReSs"))
    assertEquals(MeetingStatus.COMPLETED, MeetingStatus.fromString("CoMpLeTeD"))
    assertEquals(MeetingStatus.CANCELLED, MeetingStatus.fromString("CaNcElLeD"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForInvalidString() {
    MeetingStatus.fromString("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForEmptyString() {
    MeetingStatus.fromString("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForWrongStatus() {
    MeetingStatus.fromString("pending")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForLeadingSpace() {
    MeetingStatus.fromString(" scheduled")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForTrailingSpace() {
    MeetingStatus.fromString("scheduled ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForSurroundingSpaces() {
    MeetingStatus.fromString(" scheduled ")
  }

  @Test
  fun valueOf_shouldReturnCorrectStatus() {
    assertEquals(MeetingStatus.SCHEDULED, MeetingStatus.valueOf("SCHEDULED"))
    assertEquals(MeetingStatus.IN_PROGRESS, MeetingStatus.valueOf("IN_PROGRESS"))
    assertEquals(MeetingStatus.COMPLETED, MeetingStatus.valueOf("COMPLETED"))
    assertEquals(MeetingStatus.CANCELLED, MeetingStatus.valueOf("CANCELLED"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_shouldThrowExceptionForInvalidString() {
    MeetingStatus.valueOf("invalid")
  }

  @Test
  fun enumConstant_shouldHaveCorrectName() {
    assertEquals("SCHEDULED", MeetingStatus.SCHEDULED.name)
    assertEquals("IN_PROGRESS", MeetingStatus.IN_PROGRESS.name)
    assertEquals("COMPLETED", MeetingStatus.COMPLETED.name)
    assertEquals("CANCELLED", MeetingStatus.CANCELLED.name)
  }

  @Test
  fun enumConstant_shouldHaveCorrectOrdinal() {
    assertEquals(0, MeetingStatus.SCHEDULED.ordinal)
    assertEquals(1, MeetingStatus.IN_PROGRESS.ordinal)
    assertEquals(2, MeetingStatus.COMPLETED.ordinal)
    assertEquals(3, MeetingStatus.CANCELLED.ordinal)
  }

  @Test
  fun fromString_shouldReturnNonNullValue() {
    val status = MeetingStatus.fromString("scheduled")
    assertNotNull(status)
    assertEquals(MeetingStatus.SCHEDULED, status)
  }
}
