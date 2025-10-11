package ch.eureka.eurekapp.model.workspace

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

class WorkspaceRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: WorkspaceRepository

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreWorkspaceRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun createWorkspace_shouldCreateWorkspaceInFirestore() = runBlocking {
    val workspace =
        Workspace(
            workspaceId = "workspace1",
            name = "Test Workspace",
            isPersonal = false,
            members = mapOf(testUserId to "owner"))

    val result = repository.createWorkspace(workspace)

    assertTrue(result.isSuccess)
    assertEquals("workspace1", result.getOrNull())

    val savedWorkspace =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document("workspace1")
            .get()
            .await()
            .toObject(Workspace::class.java)

    assertNotNull(savedWorkspace)
    assertEquals(workspace.workspaceId, savedWorkspace?.workspaceId)
    assertEquals(workspace.name, savedWorkspace?.name)
    assertEquals(workspace.isPersonal, savedWorkspace?.isPersonal)
  }

  @Test
  fun getWorkspaceById_shouldReturnWorkspaceWhenExists() = runBlocking {
    val workspace =
        Workspace(
            workspaceId = "workspace2",
            name = "Test Workspace 2",
            isPersonal = false,
            members = mapOf(testUserId to "owner"))
    repository.createWorkspace(workspace)

    val flow = repository.getWorkspaceById("workspace2")
    val retrievedWorkspace = flow.first()

    assertNotNull(retrievedWorkspace)
    assertEquals(workspace.workspaceId, retrievedWorkspace?.workspaceId)
    assertEquals(workspace.name, retrievedWorkspace?.name)
  }

  @Test
  fun getWorkspaceById_shouldReturnNullWhenWorkspaceDoesNotExist() = runBlocking {
    val flow = repository.getWorkspaceById("non_existent_workspace")
    val retrievedWorkspace = flow.first()

    assertNull(retrievedWorkspace)
  }

  @Test
  fun getWorkspacesForCurrentUser_shouldReturnWorkspacesWhereUserIsMember() = runBlocking {
    val workspace1 =
        Workspace(
            workspaceId = "workspace3",
            name = "Workspace 3",
            isPersonal = false,
            members = mapOf(testUserId to "owner"))
    val workspace2 =
        Workspace(
            workspaceId = "workspace4",
            name = "Workspace 4",
            isPersonal = true,
            members = mapOf(testUserId to "owner"))
    repository.createWorkspace(workspace1)
    repository.createWorkspace(workspace2)

    val flow = repository.getWorkspacesForCurrentUser()
    val workspaces = flow.first()

    assertEquals(2, workspaces.size)
    assertTrue(workspaces.any { it.workspaceId == "workspace3" })
    assertTrue(workspaces.any { it.workspaceId == "workspace4" })
  }

  @Test
  fun getWorkspacesForCurrentUser_shouldReturnEmptyListWhenUserHasNoWorkspaces() = runBlocking {
    val flow = repository.getWorkspacesForCurrentUser()
    val workspaces = flow.first()

    assertTrue(workspaces.isEmpty())
  }

  @Test
  fun updateWorkspace_shouldUpdateWorkspaceDetails() = runBlocking {
    val workspace =
        Workspace(
            workspaceId = "workspace5",
            name = "Original Name",
            isPersonal = false,
            members = mapOf(testUserId to "owner"))
    repository.createWorkspace(workspace)

    val updatedWorkspace = workspace.copy(name = "Updated Name")
    val result = repository.updateWorkspace(updatedWorkspace)

    assertTrue(result.isSuccess)

    val savedWorkspace =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document("workspace5")
            .get()
            .await()
            .toObject(Workspace::class.java)

    assertNotNull(savedWorkspace)
    assertEquals("Updated Name", savedWorkspace?.name)
  }

  @Test
  fun deleteWorkspace_shouldDeleteWorkspaceFromFirestore() = runBlocking {
    val workspace =
        Workspace(
            workspaceId = "workspace6",
            name = "To Delete",
            isPersonal = false,
            members = mapOf(testUserId to "owner"))
    repository.createWorkspace(workspace)

    val result = repository.deleteWorkspace("workspace6")

    assertTrue(result.isSuccess)

    val deletedWorkspace =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document("workspace6")
            .get()
            .await()
            .toObject(Workspace::class.java)

    assertNull(deletedWorkspace)
  }

  @Test
  fun addMember_shouldAddMemberToWorkspace() = runBlocking {
    val workspace =
        Workspace(
            workspaceId = "workspace7",
            name = "Workspace 7",
            isPersonal = false,
            members = mapOf(testUserId to "owner"))
    repository.createWorkspace(workspace)

    val newMemberId = "newMember123"
    val result = repository.addMember("workspace7", newMemberId, "member")

    assertTrue(result.isSuccess)

    val updatedWorkspace =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document("workspace7")
            .get()
            .await()
            .toObject(Workspace::class.java)

    assertNotNull(updatedWorkspace)
    assertTrue(updatedWorkspace?.members?.containsKey(newMemberId) == true)
    assertEquals("member", updatedWorkspace?.members?.get(newMemberId))
  }

  @Test
  fun removeMember_shouldRemoveMemberFromWorkspace() = runBlocking {
    val memberId = "member456"
    val workspace =
        Workspace(
            workspaceId = "workspace8",
            name = "Workspace 8",
            isPersonal = false,
            members = mapOf(testUserId to "owner", memberId to "member"))
    repository.createWorkspace(workspace)

    val result = repository.removeMember("workspace8", memberId)

    assertTrue(result.isSuccess)

    val updatedWorkspace =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document("workspace8")
            .get()
            .await()
            .toObject(Workspace::class.java)

    assertNotNull(updatedWorkspace)
    assertTrue(updatedWorkspace?.members?.containsKey(memberId) == false)
  }

  @Test
  fun updateMemberRole_shouldUpdateMemberRoleInWorkspace() = runBlocking {
    val memberId = "member789"
    val workspace =
        Workspace(
            workspaceId = "workspace9",
            name = "Workspace 9",
            isPersonal = false,
            members = mapOf(testUserId to "owner", memberId to "member"))
    repository.createWorkspace(workspace)

    val result = repository.updateMemberRole("workspace9", memberId, "admin")

    assertTrue(result.isSuccess)

    val updatedWorkspace =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document("workspace9")
            .get()
            .await()
            .toObject(Workspace::class.java)

    assertNotNull(updatedWorkspace)
    assertEquals("admin", updatedWorkspace?.members?.get(memberId))
  }
}
