package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.enumFromString
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
  fun toString_shouldReturnUppercaseName() {
    assertEquals("HOST", MeetingRole.HOST.toString())
    assertEquals("PARTICIPANT", MeetingRole.PARTICIPANT.toString())
  }

  @Test
  fun enumFromString_shouldReturnCorrectRoleForLowercaseString() {
    assertEquals(MeetingRole.HOST, enumFromString<MeetingRole>("host"))
    assertEquals(MeetingRole.PARTICIPANT, enumFromString<MeetingRole>("participant"))
  }

  @Test
  fun enumFromString_shouldReturnCorrectRoleForUppercaseString() {
    assertEquals(MeetingRole.HOST, enumFromString<MeetingRole>("HOST"))
    assertEquals(MeetingRole.PARTICIPANT, enumFromString<MeetingRole>("PARTICIPANT"))
  }

  @Test
  fun enumFromString_shouldReturnCorrectRoleForMixedCaseString() {
    assertEquals(MeetingRole.HOST, enumFromString<MeetingRole>("HoSt"))
    assertEquals(MeetingRole.PARTICIPANT, enumFromString<MeetingRole>("PaRtIcIpAnT"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForInvalidString() {
    enumFromString<MeetingRole>("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForEmptyString() {
    enumFromString<MeetingRole>("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForWrongRole() {
    enumFromString<MeetingRole>("admin")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForLeadingSpace() {
    enumFromString<MeetingRole>(" host")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForTrailingSpace() {
    enumFromString<MeetingRole>("host ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun enumFromString_shouldThrowExceptionForSurroundingSpaces() {
    enumFromString<MeetingRole>(" host ")
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
    val role = enumFromString<MeetingRole>("host")
    assertNotNull(role)
    assertEquals(MeetingRole.HOST, role)
  }
}
