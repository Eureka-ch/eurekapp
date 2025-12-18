// Portions of this file were generated with the help of Claude (Sonnet 4.5).
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
  fun member_hasCorrectDefaultValues() {
    val member = Member()
    assertEquals("", member.userId)
    assertEquals(ProjectRole.MEMBER, member.role)
  }

  @Test
  fun member_initializesWithProvidedValues() {
    val member = Member(userId = "user123", role = ProjectRole.OWNER)
    assertEquals("user123", member.userId)
    assertEquals(ProjectRole.OWNER, member.role)
  }

  @Test
  fun member_supportsCopy() {
    val member = Member(userId = "user123", role = ProjectRole.OWNER)
    val copiedMember = member.copy(role = ProjectRole.ADMIN)
    assertEquals("user123", copiedMember.userId)
    assertEquals(ProjectRole.ADMIN, copiedMember.role)
    assertEquals(ProjectRole.OWNER, member.role)
  }

  @Test
  fun member_supportsEquality() {
    val member1 = Member(userId = "user123", role = ProjectRole.OWNER)
    val member2 = Member(userId = "user123", role = ProjectRole.OWNER)
    assertEquals(member1, member2)
  }

  @Test
  fun member_supportsHashCode() {
    val member1 = Member(userId = "user123", role = ProjectRole.OWNER)
    val member2 = Member(userId = "user123", role = ProjectRole.OWNER)
    assertEquals(member1.hashCode(), member2.hashCode())
  }

  @Test
  fun member_supportsToString() {
    val member = Member(userId = "user123", role = ProjectRole.OWNER)
    val string = member.toString()
    assert(string.contains("user123"))
    assert(string.contains("OWNER"))
  }

  @Test
  fun member_differentInstancesAreNotSame() {
    val member1 = Member(userId = "user123", role = ProjectRole.OWNER)
    val member2 = Member(userId = "user123", role = ProjectRole.OWNER)
    assertNotSame(member1, member2)
  }

  @Test
  fun member_supportsComponentFunctions() {
    val member = Member(userId = "user123", role = ProjectRole.OWNER)
    val (userId, role) = member
    assertEquals("user123", userId)
    assertEquals(ProjectRole.OWNER, role)
  }
}
