package ch.eureka.eurekapp.model.data.meeting

import junit.framework.TestCase.*
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Test suite for MeetingStatus model.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class MeetingStatusTest {

  @Test
  fun `each status should have correct description`() {
    assertEquals("Voting in progress", MeetingStatus.OPEN_TO_VOTES.description)
    assertEquals("Scheduled", MeetingStatus.SCHEDULED.description)
    assertEquals("In progress", MeetingStatus.IN_PROGRESS.description)
    assertEquals("Completed", MeetingStatus.COMPLETED.description)
  }

  @Test
  fun `values should contain all statuses`() {
    val values = MeetingStatus.values()
    assertEquals(4, values.size)
    assertTrue(values.contains(MeetingStatus.OPEN_TO_VOTES))
    assertTrue(values.contains(MeetingStatus.SCHEDULED))
    assertTrue(values.contains(MeetingStatus.IN_PROGRESS))
    assertTrue(values.contains(MeetingStatus.COMPLETED))
  }

  @Test
  fun `valueOf should return correct enum`() {
    assertEquals(MeetingStatus.OPEN_TO_VOTES, MeetingStatus.valueOf("OPEN_TO_VOTES"))
    assertEquals(MeetingStatus.SCHEDULED, MeetingStatus.valueOf("SCHEDULED"))
    assertEquals(MeetingStatus.IN_PROGRESS, MeetingStatus.valueOf("IN_PROGRESS"))
    assertEquals(MeetingStatus.COMPLETED, MeetingStatus.valueOf("COMPLETED"))
  }

  @Test
  fun `valueOf should throw IllegalArgumentException for invalid name`() {
    assertThrows(IllegalArgumentException::class.java) { MeetingStatus.valueOf("INVALID") }
  }
}
