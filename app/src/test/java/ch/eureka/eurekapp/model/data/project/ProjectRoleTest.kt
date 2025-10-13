package ch.eureka.eurekapp.model.data.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Test suite for ProjectRole enum.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class ProjectRoleTest {

  @Test
  fun projectRole_shouldHaveCorrectValues() {
    val roles = ProjectRole.values()
    assertEquals(3, roles.size)
    assertEquals(ProjectRole.OWNER, roles[0])
    assertEquals(ProjectRole.ADMIN, roles[1])
    assertEquals(ProjectRole.MEMBER, roles[2])
  }

  @Test
  fun toString_shouldReturnLowercaseName() {
    assertEquals("owner", ProjectRole.OWNER.toString())
    assertEquals("admin", ProjectRole.ADMIN.toString())
    assertEquals("member", ProjectRole.MEMBER.toString())
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForLowercaseString() {
    assertEquals(ProjectRole.OWNER, ProjectRole.fromString("owner"))
    assertEquals(ProjectRole.ADMIN, ProjectRole.fromString("admin"))
    assertEquals(ProjectRole.MEMBER, ProjectRole.fromString("member"))
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForUppercaseString() {
    assertEquals(ProjectRole.OWNER, ProjectRole.fromString("OWNER"))
    assertEquals(ProjectRole.ADMIN, ProjectRole.fromString("ADMIN"))
    assertEquals(ProjectRole.MEMBER, ProjectRole.fromString("MEMBER"))
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForMixedCaseString() {
    assertEquals(ProjectRole.OWNER, ProjectRole.fromString("OwNeR"))
    assertEquals(ProjectRole.ADMIN, ProjectRole.fromString("AdMiN"))
    assertEquals(ProjectRole.MEMBER, ProjectRole.fromString("MeMbEr"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForInvalidString() {
    ProjectRole.fromString("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForEmptyString() {
    ProjectRole.fromString("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForWrongRole() {
    ProjectRole.fromString("guest")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForLeadingSpace() {
    ProjectRole.fromString(" owner")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForTrailingSpace() {
    ProjectRole.fromString("owner ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForSurroundingSpaces() {
    ProjectRole.fromString(" owner ")
  }

  @Test
  fun valueOf_shouldReturnCorrectRole() {
    assertEquals(ProjectRole.OWNER, ProjectRole.valueOf("OWNER"))
    assertEquals(ProjectRole.ADMIN, ProjectRole.valueOf("ADMIN"))
    assertEquals(ProjectRole.MEMBER, ProjectRole.valueOf("MEMBER"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun valueOf_shouldThrowExceptionForInvalidString() {
    ProjectRole.valueOf("invalid")
  }

  @Test
  fun enumConstant_shouldHaveCorrectName() {
    assertEquals("OWNER", ProjectRole.OWNER.name)
    assertEquals("ADMIN", ProjectRole.ADMIN.name)
    assertEquals("MEMBER", ProjectRole.MEMBER.name)
  }

  @Test
  fun enumConstant_shouldHaveCorrectOrdinal() {
    assertEquals(0, ProjectRole.OWNER.ordinal)
    assertEquals(1, ProjectRole.ADMIN.ordinal)
    assertEquals(2, ProjectRole.MEMBER.ordinal)
  }

  @Test
  fun fromString_shouldReturnNonNullValue() {
    val role = ProjectRole.fromString("owner")
    assertNotNull(role)
    assertEquals(ProjectRole.OWNER, role)
  }
}
