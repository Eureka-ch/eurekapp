package ch.eureka.eurekapp.model.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ProjectTest {

  @Test
  fun project_defaultConstructor_createsEmptyProject() {
    val project = Project()

    assertEquals("", project.projectId)
    assertEquals("", project.groupId)
    assertEquals("", project.workspaceId)
    assertEquals("", project.name)
    assertEquals("", project.description)
    assertEquals("", project.status)
  }

  @Test
  fun project_withParameters_setsCorrectValues() {
    val project =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            description = "A mobile application project",
            status = "in_progress")

    assertEquals("prj123", project.projectId)
    assertEquals("grp123", project.groupId)
    assertEquals("ws123", project.workspaceId)
    assertEquals("Mobile App", project.name)
    assertEquals("A mobile application project", project.description)
    assertEquals("in_progress", project.status)
  }

  @Test
  fun project_copy_createsNewInstance() {
    val project =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            status = "open")
    val copiedProject = project.copy(status = "in_progress")

    assertEquals("prj123", copiedProject.projectId)
    assertEquals("grp123", copiedProject.groupId)
    assertEquals("ws123", copiedProject.workspaceId)
    assertEquals("Mobile App", copiedProject.name)
    assertEquals("in_progress", copiedProject.status)
  }

  @Test
  fun project_equals_comparesCorrectly() {
    val project1 =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            status = "open")
    val project2 =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            status = "open")
    val project3 =
        Project(
            projectId = "prj456",
            groupId = "grp456",
            workspaceId = "ws456",
            name = "Web App",
            status = "completed")

    assertEquals(project1, project2)
    assertNotEquals(project1, project3)
  }

  @Test
  fun project_hashCode_isConsistent() {
    val project1 =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            status = "open")
    val project2 =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            status = "open")

    assertEquals(project1.hashCode(), project2.hashCode())
  }

  @Test
  fun project_toString_containsAllFields() {
    val project =
        Project(
            projectId = "prj123",
            groupId = "grp123",
            workspaceId = "ws123",
            name = "Mobile App",
            description = "A mobile application project",
            status = "open")
    val projectString = project.toString()

    assert(projectString.contains("prj123"))
    assert(projectString.contains("grp123"))
    assert(projectString.contains("ws123"))
    assert(projectString.contains("Mobile App"))
  }
}
