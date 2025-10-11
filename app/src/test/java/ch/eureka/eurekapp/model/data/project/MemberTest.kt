package ch.eureka.eurekapp.model.data.project

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

/**
 * Test suite for Member model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class MemberTest {

  @Test
  fun member_shouldHaveCorrectDefaultValues() {
    val member = Member()
    assertEquals("", member.userId)
    assertEquals("", member.role)
  }

  @Test
  fun member_shouldInitializeWithProvidedValues() {
    val member = Member(userId = "user123", role = "owner")
    assertEquals("user123", member.userId)
    assertEquals("owner", member.role)
  }

  @Test
  fun member_shouldSupportCopy() {
    val member = Member(userId = "user123", role = "owner")
    val copiedMember = member.copy(role = "admin")
    assertEquals("user123", copiedMember.userId)
    assertEquals("admin", copiedMember.role)
    assertEquals("owner", member.role)
  }

  @Test
  fun member_shouldSupportEquality() {
    val member1 = Member(userId = "user123", role = "owner")
    val member2 = Member(userId = "user123", role = "owner")
    assertEquals(member1, member2)
  }

  @Test
  fun member_shouldSupportHashCode() {
    val member1 = Member(userId = "user123", role = "owner")
    val member2 = Member(userId = "user123", role = "owner")
    assertEquals(member1.hashCode(), member2.hashCode())
  }

  @Test
  fun member_shouldSupportToString() {
    val member = Member(userId = "user123", role = "owner")
    val string = member.toString()
    assert(string.contains("user123"))
    assert(string.contains("owner"))
  }

  @Test
  fun member_differentInstances_shouldNotBeSame() {
    val member1 = Member(userId = "user123", role = "owner")
    val member2 = Member(userId = "user123", role = "owner")
    assertNotSame(member1, member2)
  }

  @Test
  fun member_shouldSupportComponentFunctions() {
    val member = Member(userId = "user123", role = "owner")
    val (userId, role) = member
    assertEquals("user123", userId)
    assertEquals("owner", role)
  }
}
