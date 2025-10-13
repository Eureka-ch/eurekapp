package ch.eureka.eurekapp.model.data.project

import ch.eureka.eurekapp.model.data.enumFromString
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
  fun toString_shouldReturnUppercaseName() {
    assertEquals("OWNER", ProjectRole.OWNER.toString())
    assertEquals("ADMIN", ProjectRole.ADMIN.toString())
    assertEquals("MEMBER", ProjectRole.MEMBER.toString())
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForLowercaseString() {
    assertEquals(ProjectRole.OWNER, enumFromString<ProjectRole>("owner"))
    assertEquals(ProjectRole.ADMIN, enumFromString<ProjectRole>("admin"))
    assertEquals(ProjectRole.MEMBER, enumFromString<ProjectRole>("member"))
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForUppercaseString() {
    assertEquals(ProjectRole.OWNER, enumFromString<ProjectRole>("OWNER"))
    assertEquals(ProjectRole.ADMIN, enumFromString<ProjectRole>("ADMIN"))
    assertEquals(ProjectRole.MEMBER, enumFromString<ProjectRole>("MEMBER"))
  }

  @Test
  fun fromString_shouldReturnCorrectRoleForMixedCaseString() {
    assertEquals(ProjectRole.OWNER, enumFromString<ProjectRole>("OwNeR"))
    assertEquals(ProjectRole.ADMIN, enumFromString<ProjectRole>("AdMiN"))
    assertEquals(ProjectRole.MEMBER, enumFromString<ProjectRole>("MeMbEr"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForInvalidString() {
    enumFromString<ProjectRole>("invalid")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForEmptyString() {
    enumFromString<ProjectRole>("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForWrongRole() {
    enumFromString<ProjectRole>("guest")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForLeadingSpace() {
    enumFromString<ProjectRole>(" owner")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForTrailingSpace() {
    enumFromString<ProjectRole>("owner ")
  }

  @Test(expected = IllegalArgumentException::class)
  fun fromString_shouldThrowExceptionForSurroundingSpaces() {
    enumFromString<ProjectRole>(" owner ")
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
    val role = enumFromString<ProjectRole>("owner")
    assertNotNull(role)
    assertEquals(ProjectRole.OWNER, role)
  }
}
