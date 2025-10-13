package ch.eureka.eurekapp.model.data.meeting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Test suite for MeetingRole enum.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class MeetingRoleTest {

  @Test
  fun meetingRole_shouldHaveCorrectValues() {
    val roles = MeetingRole.values()
    assertEquals(2, roles.size)
    assertEquals(MeetingRole.HOST, roles[0])
    assertEquals(MeetingRole.PARTICIPANT, roles[1])
  }

  @Test
  fun toString_shouldReturnLowercaseName() {
    assertEquals("host", MeetingRole.HOST.toString())
    assertEquals("participant", MeetingRole.PARTICIPANT.toString())
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForLowercaseString() {
    assertEquals(MeetingRole.HOST, MeetingRole.fromString("host"))
    assertEquals(MeetingRole.PARTICIPANT, MeetingRole.fromString("participant"))
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForUppercaseString() {
    assertEquals(MeetingRole.HOST, MeetingRole.fromString("HOST"))
    assertEquals(MeetingRole.PARTICIPANT, MeetingRole.fromString("PARTICIPANT"))
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForMixedCaseString() {
    assertEquals(MeetingRole.HOST, MeetingRole.fromString("HoSt"))
    assertEquals(MeetingRole.PARTICIPANT, MeetingRole.fromString("PaRtIcIpAnT"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForInvalidString() {
    MeetingRole.fromString("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForEmptyString() {
    MeetingRole.fromString("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForWrongRole() {
    MeetingRole.fromString("admin")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForLeadingSpace() {
    MeetingRole.fromString(" host")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForTrailingSpace() {
    MeetingRole.fromString("host ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForSurroundingSpaces() {
    MeetingRole.fromString(" host ")
  }

  @Test
  fun valueOf_shouldReturnCorrectRole() {
    assertEquals(MeetingRole.HOST, MeetingRole.valueOf("HOST"))
    assertEquals(MeetingRole.PARTICIPANT, MeetingRole.valueOf("PARTICIPANT"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_shouldThrowExceptionForInvalidString() {
    MeetingRole.valueOf("invalid")
  }

  @Test
  fun enumConstant_shouldHaveCorrectName() {
    assertEquals("HOST", MeetingRole.HOST.name)
    assertEquals("PARTICIPANT", MeetingRole.PARTICIPANT.name)
  }

  @Test
  fun enumConstant_shouldHaveCorrectOrdinal() {
    assertEquals(0, MeetingRole.HOST.ordinal)
    assertEquals(1, MeetingRole.PARTICIPANT.ordinal)
  }

  @Test
  fun fromString_shouldReturnNonNullValue() {
    val role = MeetingRole.fromString("host")
    assertNotNull(role)
    assertEquals(MeetingRole.HOST, role)
  }
}
