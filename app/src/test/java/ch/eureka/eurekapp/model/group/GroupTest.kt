package ch.eureka.eurekapp.model.group

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class GroupTest {

  @Test
  fun group_defaultConstructor_createsEmptyGroup() {
    val group = Group()

    assertEquals("", group.groupID)
    assertEquals("", group.workspaceId)
    assertEquals("", group.name)
    assertEquals(emptyMap<String, String>(), group.members)
  }

  @Test
  fun group_withParameters_setsCorrectValues() {
    val members = mapOf("user1" to "admin", "user2" to "member")
    val group =
        Group(
            groupID = "grp123", workspaceId = "ws123", name = "Development Team", members = members)

    assertEquals("grp123", group.groupID)
    assertEquals("ws123", group.workspaceId)
    assertEquals("Development Team", group.name)
    assertEquals(members, group.members)
  }

  @Test
  fun group_copy_createsNewInstance() {
    val group = Group(groupID = "grp123", workspaceId = "ws123", name = "Development Team")
    val copiedGroup = group.copy(name = "QA Team")

    assertEquals("grp123", copiedGroup.groupID)
    assertEquals("ws123", copiedGroup.workspaceId)
    assertEquals("QA Team", copiedGroup.name)
  }

  @Test
  fun group_equals_comparesCorrectly() {
    val group1 =
        Group(
            groupID = "grp123",
            workspaceId = "ws123",
            name = "Development Team",
            members = mapOf("user1" to "admin"))
    val group2 =
        Group(
            groupID = "grp123",
            workspaceId = "ws123",
            name = "Development Team",
            members = mapOf("user1" to "admin"))
    val group3 =
        Group(
            groupID = "grp456",
            workspaceId = "ws456",
            name = "QA Team",
            members = mapOf("user2" to "member"))

    assertEquals(group1, group2)
    assertNotEquals(group1, group3)
  }

  @Test
  fun group_hashCode_isConsistent() {
    val group1 =
        Group(
            groupID = "grp123",
            workspaceId = "ws123",
            name = "Development Team",
            members = mapOf("user1" to "admin"))
    val group2 =
        Group(
            groupID = "grp123",
            workspaceId = "ws123",
            name = "Development Team",
            members = mapOf("user1" to "admin"))

    assertEquals(group1.hashCode(), group2.hashCode())
  }

  @Test
  fun group_toString_containsAllFields() {
    val group =
        Group(
            groupID = "grp123",
            workspaceId = "ws123",
            name = "Development Team",
            members = mapOf("user1" to "admin"))
    val groupString = group.toString()

    assert(groupString.contains("grp123"))
    assert(groupString.contains("ws123"))
    assert(groupString.contains("Development Team"))
  }
}
