// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp
import junit.framework.TestCase.*
import org.junit.Test

/**
 * Test suite for TimeSlot model.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class TimeSlotTest {

  @Test
  fun timeSlot_defaultConstructorSetsValues() {
    val slot = TimeSlot()

    assertNotNull(slot.startTime)
    assertNotNull(slot.endTime)
  }

  @Test
  fun timeSlot_fullConstructorSetsProperties() {
    val start = Timestamp.now()
    val end = Timestamp(start.seconds + 3600, start.nanoseconds) // 1 hour later

    val slot = TimeSlot(start, end)

    assertEquals(start, slot.startTime)
    assertEquals(end, slot.endTime)
  }

  @Test
  fun timeSlot_copyAndEqualityWorkCorrectly() {
    val start = Timestamp.now()
    val end = Timestamp(start.seconds + 1800, start.nanoseconds)
    val slot1 = TimeSlot(start, end)
    val slot2 = slot1.copy(endTime = Timestamp(end.seconds + 1800, end.nanoseconds))

    assertNotSame(slot1, slot2)
    assertFalse(slot1 == slot2)
    assertTrue(slot1.hashCode() != slot2.hashCode())
    assertEquals(start, slot2.startTime)
    assertEquals(end.seconds + 1800, slot2.endTime.seconds)
  }

  @Test
  fun timeSlot_toStringAndComponentsWorkCorrectly() {
    val start = Timestamp.now()
    val end = Timestamp(start.seconds + 3600, start.nanoseconds)
    val slot = TimeSlot(start, end)

    val str = slot.toString()
    assertTrue(str.contains(start.seconds.toString()))
    assertTrue(str.contains(end.seconds.toString()))

    assertEquals(start, slot.component1())
    assertEquals(end, slot.component2())
  }

  @Test
  fun timeSlot_formatTimeSlotReturnsFormattedString() {
    val start = Timestamp.now()
    val end = Timestamp(start.seconds + 3600, start.nanoseconds) // 1 hour later
    val slot = TimeSlot(start, end)

    val formatted = slot.formatTimeSlot()
    assertNotNull(formatted)
    // Optional: verify pattern roughly matches "EEE, MMM d · h:mm a–h:mm a"
    val regex = "\\w{3}, \\w{3} \\d{1,2} · \\d{1,2}:\\d{2} [AP]M–\\d{1,2}:\\d{2} [AP]M".toRegex()
    assertTrue(regex.matches(formatted))
  }
}
