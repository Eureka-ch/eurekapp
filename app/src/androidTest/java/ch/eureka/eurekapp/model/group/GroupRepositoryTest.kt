package ch.eureka.eurekapp.model.group

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

class GroupRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: GroupRepository
  private val testWorkspaceId = "workspace_group_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces/$testWorkspaceId/groups")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreGroupRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun createGroup_shouldCreateGroupInFirestore() = runBlocking {
    val group =
        Group(
            groupID = "group1",
            workspaceId = testWorkspaceId,
            name = "Test Group",
            members = mapOf(testUserId to "admin"))

    val result = repository.createGroup(group)

    assertTrue(result.isSuccess)
    assertEquals("group1", result.getOrNull())

    val savedGroup =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document("group1")
            .get()
            .await()
            .toObject(Group::class.java)

    assertNotNull(savedGroup)
    assertEquals(group.groupID, savedGroup?.groupID)
    assertEquals(group.name, savedGroup?.name)
    assertEquals(group.workspaceId, savedGroup?.workspaceId)
  }

  @Test
  fun getGroupById_shouldReturnGroupWhenExists() = runBlocking {
    val group =
        Group(
            groupID = "group2",
            workspaceId = testWorkspaceId,
            name = "Test Group 2",
            members = mapOf(testUserId to "admin"))
    repository.createGroup(group)

    val flow = repository.getGroupById(testWorkspaceId, "group2")
    val retrievedGroup = flow.first()

    assertNotNull(retrievedGroup)
    assertEquals(group.groupID, retrievedGroup?.groupID)
    assertEquals(group.name, retrievedGroup?.name)
  }

  @Test
  fun getGroupById_shouldReturnNullWhenGroupDoesNotExist() = runBlocking {
    val flow = repository.getGroupById(testWorkspaceId, "non_existent_group")
    val retrievedGroup = flow.first()

    assertNull(retrievedGroup)
  }

  @Test
  fun getGroupsInWorkspace_shouldReturnAllGroupsInWorkspace() = runBlocking {
    val group1 =
        Group(
            groupID = "group3",
            workspaceId = testWorkspaceId,
            name = "Group 3",
            members = mapOf(testUserId to "admin"))
    val group2 =
        Group(
            groupID = "group4",
            workspaceId = testWorkspaceId,
            name = "Group 4",
            members = mapOf(testUserId to "member"))
    repository.createGroup(group1)
    repository.createGroup(group2)

    val flow = repository.getGroupsInWorkspace(testWorkspaceId)
    val groups = flow.first()

    assertEquals(2, groups.size)
    assertTrue(groups.any { it.groupID == "group3" })
    assertTrue(groups.any { it.groupID == "group4" })
  }

  @Test
  fun getGroupsInWorkspace_shouldReturnEmptyListWhenNoGroups() = runBlocking {
    val flow = repository.getGroupsInWorkspace(testWorkspaceId)
    val groups = flow.first()

    assertTrue(groups.isEmpty())
  }

  @Test
  fun getGroupsForCurrentUser_shouldReturnGroupsWhereUserIsMember() = runBlocking {
    val group1 =
        Group(
            groupID = "group5",
            workspaceId = testWorkspaceId,
            name = "Group 5",
            members = mapOf(testUserId to "admin"))
    val group2 =
        Group(
            groupID = "group6",
            workspaceId = testWorkspaceId,
            name = "Group 6",
            members = mapOf("otherUser" to "admin"))
    repository.createGroup(group1)
    repository.createGroup(group2)

    val flow = repository.getGroupsForCurrentUser(testWorkspaceId)
    val groups = flow.first()

    assertEquals(1, groups.size)
    assertEquals("group5", groups[0].groupID)
  }

  @Test
  fun getGroupsForCurrentUser_shouldReturnEmptyListWhenUserNotInAnyGroup() = runBlocking {
    val flow = repository.getGroupsForCurrentUser(testWorkspaceId)
    val groups = flow.first()

    assertTrue(groups.isEmpty())
  }

  @Test
  fun updateGroup_shouldUpdateGroupDetails() = runBlocking {
    val group =
        Group(
            groupID = "group7",
            workspaceId = testWorkspaceId,
            name = "Original Name",
            members = mapOf(testUserId to "admin"))
    repository.createGroup(group)

    val updatedGroup = group.copy(name = "Updated Name")
    val result = repository.updateGroup(updatedGroup)

    assertTrue(result.isSuccess)

    val savedGroup =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document("group7")
            .get()
            .await()
            .toObject(Group::class.java)

    assertNotNull(savedGroup)
    assertEquals("Updated Name", savedGroup?.name)
  }

  @Test
  fun deleteGroup_shouldDeleteGroupFromFirestore() = runBlocking {
    val group =
        Group(
            groupID = "group8",
            workspaceId = testWorkspaceId,
            name = "To Delete",
            members = mapOf(testUserId to "admin"))
    repository.createGroup(group)

    val result = repository.deleteGroup(testWorkspaceId, "group8")

    assertTrue(result.isSuccess)

    val deletedGroup =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document("group8")
            .get()
            .await()
            .toObject(Group::class.java)

    assertNull(deletedGroup)
  }

  @Test
  fun addMember_shouldAddMemberToGroup() = runBlocking {
    val group =
        Group(
            groupID = "group9",
            workspaceId = testWorkspaceId,
            name = "Group 9",
            members = mapOf(testUserId to "admin"))
    repository.createGroup(group)

    val newMemberId = "newMember123"
    val result = repository.addMember(testWorkspaceId, "group9", newMemberId, "member")

    assertTrue(result.isSuccess)

    val updatedGroup =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document("group9")
            .get()
            .await()
            .toObject(Group::class.java)

    assertNotNull(updatedGroup)
    assertTrue(updatedGroup?.members?.containsKey(newMemberId) == true)
    assertEquals("member", updatedGroup?.members?.get(newMemberId))
  }

  @Test
  fun removeMember_shouldRemoveMemberFromGroup() = runBlocking {
    val memberId = "member456"
    val group =
        Group(
            groupID = "group10",
            workspaceId = testWorkspaceId,
            name = "Group 10",
            members = mapOf(testUserId to "admin", memberId to "member"))
    repository.createGroup(group)

    val result = repository.removeMember(testWorkspaceId, "group10", memberId)

    assertTrue(result.isSuccess)

    val updatedGroup =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document("group10")
            .get()
            .await()
            .toObject(Group::class.java)

    assertNotNull(updatedGroup)
    assertTrue(updatedGroup?.members?.containsKey(memberId) == false)
  }

  @Test
  fun updateMemberRole_shouldUpdateMemberRoleInGroup() = runBlocking {
    val memberId = "member789"
    val group =
        Group(
            groupID = "group11",
            workspaceId = testWorkspaceId,
            name = "Group 11",
            members = mapOf(testUserId to "admin", memberId to "member"))
    repository.createGroup(group)

    val result = repository.updateMemberRole(testWorkspaceId, "group11", memberId, "admin")

    assertTrue(result.isSuccess)

    val updatedGroup =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document("group11")
            .get()
            .await()
            .toObject(Group::class.java)

    assertNotNull(updatedGroup)
    assertEquals("admin", updatedGroup?.members?.get(memberId))
  }
}
