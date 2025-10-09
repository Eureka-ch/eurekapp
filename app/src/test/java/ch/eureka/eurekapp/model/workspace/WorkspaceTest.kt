package ch.eureka.eurekapp.model.workspace

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class WorkspaceTest {

  @Test
  fun workspace_defaultConstructor_createsEmptyWorkspace() {
    val workspace = Workspace()

    assertEquals("", workspace.workspaceId)
    assertEquals("", workspace.name)
    assertEquals(false, workspace.isPersonal)
    assertEquals(emptyMap<String, String>(), workspace.members)
  }

  @Test
  fun workspace_withParameters_setsCorrectValues() {
    val members = mapOf("user1" to "owner", "user2" to "admin", "user3" to "member")
    val workspace =
        Workspace(
            workspaceId = "ws123", name = "Team Workspace", isPersonal = false, members = members)

    assertEquals("ws123", workspace.workspaceId)
    assertEquals("Team Workspace", workspace.name)
    assertEquals(false, workspace.isPersonal)
    assertEquals(members, workspace.members)
  }

  @Test
  fun workspace_personalWorkspace_setsCorrectValues() {
    val workspace =
        Workspace(workspaceId = "ws456", name = "Personal", isPersonal = true, members = mapOf())

    assertEquals("ws456", workspace.workspaceId)
    assertEquals("Personal", workspace.name)
    assertEquals(true, workspace.isPersonal)
  }

  @Test
  fun workspace_copy_createsNewInstance() {
    val workspace = Workspace(workspaceId = "ws123", name = "Team Workspace", isPersonal = false)
    val copiedWorkspace = workspace.copy(name = "Updated Workspace")

    assertEquals("ws123", copiedWorkspace.workspaceId)
    assertEquals("Updated Workspace", copiedWorkspace.name)
    assertEquals(false, copiedWorkspace.isPersonal)
  }

  @Test
  fun workspace_equals_comparesCorrectly() {
    val workspace1 =
        Workspace(
            workspaceId = "ws123",
            name = "Team Workspace",
            isPersonal = false,
            members = mapOf("user1" to "owner"))
    val workspace2 =
        Workspace(
            workspaceId = "ws123",
            name = "Team Workspace",
            isPersonal = false,
            members = mapOf("user1" to "owner"))
    val workspace3 =
        Workspace(
            workspaceId = "ws456", name = "Other Workspace", isPersonal = true, members = mapOf())

    assertEquals(workspace1, workspace2)
    assertNotEquals(workspace1, workspace3)
  }

  @Test
  fun workspace_hashCode_isConsistent() {
    val workspace1 =
        Workspace(
            workspaceId = "ws123",
            name = "Team Workspace",
            isPersonal = false,
            members = mapOf("user1" to "owner"))
    val workspace2 =
        Workspace(
            workspaceId = "ws123",
            name = "Team Workspace",
            isPersonal = false,
            members = mapOf("user1" to "owner"))

    assertEquals(workspace1.hashCode(), workspace2.hashCode())
  }

  @Test
  fun workspace_toString_containsAllFields() {
    val workspace =
        Workspace(
            workspaceId = "ws123",
            name = "Team Workspace",
            isPersonal = false,
            members = mapOf("user1" to "owner"))
    val workspaceString = workspace.toString()

    assert(workspaceString.contains("ws123"))
    assert(workspaceString.contains("Team Workspace"))
    assert(workspaceString.contains("false"))
  }
}
