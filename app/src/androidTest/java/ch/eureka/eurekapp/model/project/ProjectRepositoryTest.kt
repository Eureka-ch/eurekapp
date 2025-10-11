package ch.eureka.eurekapp.model.project

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

class ProjectRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: ProjectRepository
  private val testWorkspaceId = "workspace_project_test"
  private val testGroupId = "group_project_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces/$testWorkspaceId/groups/$testGroupId/projects")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreProjectRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun createProject_shouldCreateProjectInFirestore() = runBlocking {
    val project =
        Project(
            workspaceId = testWorkspaceId,
            projectId = "project1",
            groupId = testGroupId,
            name = "Test Project",
            description = "Test project description",
            status = "open")

    val result = repository.createProject(project)

    assertTrue(result.isSuccess)
    assertEquals("project1", result.getOrNull())

    val savedProject =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document("project1")
            .get()
            .await()
            .toObject(Project::class.java)

    assertNotNull(savedProject)
    assertEquals(project.projectId, savedProject?.projectId)
    assertEquals(project.name, savedProject?.name)
    assertEquals(project.description, savedProject?.description)
    assertEquals(project.status, savedProject?.status)
  }

  @Test
  fun getProjectById_shouldReturnProjectWhenExists() = runBlocking {
    val project =
        Project(
            workspaceId = testWorkspaceId,
            projectId = "project2",
            groupId = testGroupId,
            name = "Test Project 2",
            description = "Description 2",
            status = "in_progress")
    repository.createProject(project)

    val flow = repository.getProjectById(testWorkspaceId, testGroupId, "project2")
    val retrievedProject = flow.first()

    assertNotNull(retrievedProject)
    assertEquals(project.projectId, retrievedProject?.projectId)
    assertEquals(project.name, retrievedProject?.name)
    assertEquals(project.status, retrievedProject?.status)
  }

  @Test
  fun getProjectById_shouldReturnNullWhenProjectDoesNotExist() = runBlocking {
    val flow = repository.getProjectById(testWorkspaceId, testGroupId, "non_existent_project")
    val retrievedProject = flow.first()

    assertNull(retrievedProject)
  }

  @Test
  fun getProjectsInGroup_shouldReturnAllProjectsInGroup() = runBlocking {
    val project1 =
        Project(
            workspaceId = testWorkspaceId,
            projectId = "project3",
            groupId = testGroupId,
            name = "Project 3",
            description = "",
            status = "open")
    val project2 =
        Project(
            workspaceId = testWorkspaceId,
            projectId = "project4",
            groupId = testGroupId,
            name = "Project 4",
            description = "",
            status = "completed")
    repository.createProject(project1)
    repository.createProject(project2)

    val flow = repository.getProjectsInGroup(testWorkspaceId, testGroupId)
    val projects = flow.first()

    assertEquals(2, projects.size)
    assertTrue(projects.any { it.projectId == "project3" })
    assertTrue(projects.any { it.projectId == "project4" })
  }

  @Test
  fun getProjectsInGroup_shouldReturnEmptyListWhenNoProjects() = runBlocking {
    val flow = repository.getProjectsInGroup(testWorkspaceId, testGroupId)
    val projects = flow.first()

    assertTrue(projects.isEmpty())
  }

  @Test
  fun updateProject_shouldUpdateProjectDetails() = runBlocking {
    val project =
        Project(
            workspaceId = testWorkspaceId,
            projectId = "project5",
            groupId = testGroupId,
            name = "Original Name",
            description = "Original Description",
            status = "open")
    repository.createProject(project)

    val updatedProject =
        project.copy(
            name = "Updated Name", description = "Updated Description", status = "in_progress")
    val result = repository.updateProject(updatedProject)

    assertTrue(result.isSuccess)

    val savedProject =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document("project5")
            .get()
            .await()
            .toObject(Project::class.java)

    assertNotNull(savedProject)
    assertEquals("Updated Name", savedProject?.name)
    assertEquals("Updated Description", savedProject?.description)
    assertEquals("in_progress", savedProject?.status)
  }

  @Test
  fun deleteProject_shouldDeleteProjectFromFirestore() = runBlocking {
    val project =
        Project(
            workspaceId = testWorkspaceId,
            projectId = "project6",
            groupId = testGroupId,
            name = "To Delete",
            description = "",
            status = "archived")
    repository.createProject(project)

    val result = repository.deleteProject(testWorkspaceId, testGroupId, "project6")

    assertTrue(result.isSuccess)

    val deletedProject =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document("project6")
            .get()
            .await()
            .toObject(Project::class.java)

    assertNull(deletedProject)
  }
}
