package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum
import junit.framework.TestCase.*

/**
 * Test suite for MeetingStatus model.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class MeetingStatusTest {

  @org.junit.Test
  fun testEnumValuesAndValueOf() {
    val values = MeetingStatus.values()
    assertEquals(4, values.size)
    assertTrue(values.contains(MeetingStatus.SCHEDULED))
    assertTrue(values.contains(MeetingStatus.IN_PROGRESS))
    assertTrue(values.contains(MeetingStatus.COMPLETED))
    assertTrue(values.contains(MeetingStatus.CANCELLED))

    val scheduled = MeetingStatus.valueOf("SCHEDULED")
    val inProgress = MeetingStatus.valueOf("IN_PROGRESS")
    val completed = MeetingStatus.valueOf("COMPLETED")
    val cancelled = MeetingStatus.valueOf("CANCELLED")

    assertEquals(MeetingStatus.SCHEDULED, scheduled)
    assertEquals(MeetingStatus.IN_PROGRESS, inProgress)
    assertEquals(MeetingStatus.COMPLETED, completed)
    assertEquals(MeetingStatus.CANCELLED, cancelled)
  }

  @org.junit.Test
  fun testImplementsInterface() {
    val status: StringSerializableEnum = MeetingStatus.SCHEDULED
    assertNotNull(status)
  }

  @org.junit.Test
  fun testToString() {
    assertEquals("SCHEDULED", MeetingStatus.SCHEDULED.toString())
    assertEquals("IN_PROGRESS", MeetingStatus.IN_PROGRESS.toString())
    assertEquals("COMPLETED", MeetingStatus.COMPLETED.toString())
    assertEquals("CANCELLED", MeetingStatus.CANCELLED.toString())
  }
}
