package ch.eureka.eurekapp.model.data.project

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Test suite for Project model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class ProjectTest {

  @Test
  fun project_defaultConstructor_createsEmptyProject() {
    val project = Project()

    assertEquals("", project.projectId)
    assertEquals("", project.name)
    assertEquals("", project.description)
    assertEquals(ProjectStatus.OPEN, project.status)
  }

  @Test
  fun project_withParameters_setsCorrectValues() {
    val project =
        Project(
            projectId = "prj123",
            name = "Mobile App",
            description = "A mobile application project",
            status = ProjectStatus.IN_PROGRESS,
        )

    assertEquals("prj123", project.projectId)
    assertEquals("Mobile App", project.name)
    assertEquals("A mobile application project", project.description)
    assertEquals(ProjectStatus.IN_PROGRESS, project.status)
  }

  @Test
  fun project_copy_createsNewInstance() {
    val project = Project(projectId = "prj123", name = "Mobile App", status = ProjectStatus.OPEN)
    val copiedProject = project.copy(status = ProjectStatus.IN_PROGRESS)

    assertEquals("prj123", copiedProject.projectId)
    assertEquals("Mobile App", copiedProject.name)
    assertEquals(ProjectStatus.IN_PROGRESS, copiedProject.status)
  }

  @Test
  fun project_equals_comparesCorrectly() {
    val project1 = Project(projectId = "prj123", name = "Mobile App", status = ProjectStatus.OPEN)
    val project2 = Project(projectId = "prj123", name = "Mobile App", status = ProjectStatus.OPEN)
    val project3 = Project(projectId = "prj456", name = "Web App", status = ProjectStatus.COMPLETED)

    assertEquals(project1, project2)
    assertNotEquals(project1, project3)
  }

  @Test
  fun project_hashCode_isConsistent() {
    val project1 = Project(projectId = "prj123", name = "Mobile App", status = ProjectStatus.OPEN)
    val project2 = Project(projectId = "prj123", name = "Mobile App", status = ProjectStatus.OPEN)

    assertEquals(project1.hashCode(), project2.hashCode())
  }

  @Test
  fun project_toString_containsAllFields() {
    val project =
        Project(
            projectId = "prj123",
            name = "Mobile App",
            description = "A mobile application project",
            status = ProjectStatus.OPEN,
        )
    val projectString = project.toString()

    assert(projectString.contains("prj123"))
    assert(projectString.contains("Mobile App"))
    assert(projectString.contains("OPEN"))
  }
}
