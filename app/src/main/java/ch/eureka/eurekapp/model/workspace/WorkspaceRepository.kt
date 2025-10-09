package ch.eureka.eurekapp.model.workspace

import kotlinx.coroutines.flow.Flow

interface WorkspaceRepository {
  /** Get workspace by ID with real-time updates */
  fun getWorkspaceById(workspaceId: String): Flow<Workspace?>

  /** Get all workspaces for current user with real-time updates */
  fun getWorkspacesForCurrentUser(): Flow<List<Workspace>>

  /** Create a new workspace */
  suspend fun createWorkspace(workspace: Workspace): Result<String>

  /** Update workspace details */
  suspend fun updateWorkspace(workspace: Workspace): Result<Unit>

  /** Delete workspace */
  suspend fun deleteWorkspace(workspaceId: String): Result<Unit>

  /** Add member to workspace */
  suspend fun addMember(workspaceId: String, userId: String, role: String): Result<Unit>

  /** Remove member from workspace */
  suspend fun removeMember(workspaceId: String, userId: String): Result<Unit>

  /** Update member role */
  suspend fun updateMemberRole(workspaceId: String, userId: String, role: String): Result<Unit>
}
