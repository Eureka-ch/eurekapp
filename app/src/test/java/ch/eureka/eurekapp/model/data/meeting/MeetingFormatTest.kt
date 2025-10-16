package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.StringSerializableEnum
import junit.framework.TestCase.*

/**
 * Test suite for MeetingFormat model.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class MeetingFormatTest {

  @org.junit.Test
  fun testEnumValuesAndValueOf() {
    val values = MeetingFormat.values()
    assertEquals(2, values.size)
    assertTrue(values.contains(MeetingFormat.IN_PERSON))
    assertTrue(values.contains(MeetingFormat.VIRTUAL))

    val inPerson = MeetingFormat.valueOf("IN_PERSON")
    val virtual = MeetingFormat.valueOf("VIRTUAL")

    assertEquals(MeetingFormat.IN_PERSON, inPerson)
    assertEquals(MeetingFormat.VIRTUAL, virtual)
  }

  @org.junit.Test
  fun testImplementsInterface() {
    val format: StringSerializableEnum = MeetingFormat.IN_PERSON
    assertNotNull(format)
  }

  @org.junit.Test
  fun testToString() {
    assertEquals("IN_PERSON", MeetingFormat.IN_PERSON.toString())
    assertEquals("VIRTUAL", MeetingFormat.VIRTUAL.toString())
  }
}
