package ch.eureka.eurekapp.model.group

import kotlinx.coroutines.flow.Flow

interface GroupRepository {
  /** Get group by ID with real-time updates */
  fun getGroupById(workspaceId: String, groupId: String): Flow<Group?>

  /** Get all groups in workspace with real-time updates */
  fun getGroupsInWorkspace(workspaceId: String): Flow<List<Group>>

  /** Get groups where current user is a member with real-time updates */
  fun getGroupsForCurrentUser(workspaceId: String): Flow<List<Group>>

  /** Create a new group */
  suspend fun createGroup(group: Group): Result<String>

  /** Update group details */
  suspend fun updateGroup(group: Group): Result<Unit>

  /** Delete group */
  suspend fun deleteGroup(workspaceId: String, groupId: String): Result<Unit>

  /** Add member to group */
  suspend fun addMember(
      workspaceId: String,
      groupId: String,
      userId: String,
      role: String
  ): Result<Unit>

  /** Remove member from group */
  suspend fun removeMember(workspaceId: String, groupId: String, userId: String): Result<Unit>

  /** Update member role */
  suspend fun updateMemberRole(
      workspaceId: String,
      groupId: String,
      userId: String,
      role: String
  ): Result<Unit>
}
