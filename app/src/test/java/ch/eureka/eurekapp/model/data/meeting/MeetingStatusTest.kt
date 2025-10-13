package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.enumFromString
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
  fun toString_shouldReturnUppercaseName() {
    assertEquals("SCHEDULED", MeetingStatus.SCHEDULED.toString())
    assertEquals("IN_PROGRESS", MeetingStatus.IN_PROGRESS.toString())
    assertEquals("COMPLETED", MeetingStatus.COMPLETED.toString())
    assertEquals("CANCELLED", MeetingStatus.CANCELLED.toString())
  }

  @Test
  fun fromString_shouldReturnCorrectStatusForLowercaseString() {
    assertEquals(MeetingStatus.SCHEDULED, enumFromString<MeetingStatus>("scheduled"))
    assertEquals(MeetingStatus.IN_PROGRESS, enumFromString<MeetingStatus>("in_progress"))
    assertEquals(MeetingStatus.COMPLETED, enumFromString<MeetingStatus>("completed"))
    assertEquals(MeetingStatus.CANCELLED, enumFromString<MeetingStatus>("cancelled"))
  }

  @Test
  fun fromString_shouldReturnCorrectStatusForUppercaseString() {
    assertEquals(MeetingStatus.SCHEDULED, enumFromString<MeetingStatus>("SCHEDULED"))
    assertEquals(MeetingStatus.IN_PROGRESS, enumFromString<MeetingStatus>("IN_PROGRESS"))
    assertEquals(MeetingStatus.COMPLETED, enumFromString<MeetingStatus>("COMPLETED"))
    assertEquals(MeetingStatus.CANCELLED, enumFromString<MeetingStatus>("CANCELLED"))
  }

  @Test
  fun fromString_shouldReturnCorrectStatusForMixedCaseString() {
    assertEquals(MeetingStatus.SCHEDULED, enumFromString<MeetingStatus>("ScHeDuLeD"))
    assertEquals(MeetingStatus.IN_PROGRESS, enumFromString<MeetingStatus>("In_PrOgReSs"))
    assertEquals(MeetingStatus.COMPLETED, enumFromString<MeetingStatus>("CoMpLeTeD"))
    assertEquals(MeetingStatus.CANCELLED, enumFromString<MeetingStatus>("CaNcElLeD"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForInvalidString() {
    enumFromString<MeetingStatus>("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForEmptyString() {
    enumFromString<MeetingStatus>("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForWrongStatus() {
    enumFromString<MeetingStatus>("pending")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForLeadingSpace() {
    enumFromString<MeetingStatus>(" scheduled")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForTrailingSpace() {
    enumFromString<MeetingStatus>("scheduled ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForSurroundingSpaces() {
    enumFromString<MeetingStatus>(" scheduled ")
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
    val status = enumFromString<MeetingStatus>("scheduled")
    assertNotNull(status)
    assertEquals(MeetingStatus.SCHEDULED, status)
  }
}
