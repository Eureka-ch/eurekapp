package ch.eureka.eurekapp.model.data.project

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
  /** Get project by ID with real-time updates */
  fun getProjectById(projectId: String): Flow<Project?>

  /** Get all projects for current user with real-time updates */
  fun getProjectsForCurrentUser(skipCache: Boolean = true): Flow<List<Project>>

  /** Create a new project */
  suspend fun createProject(
      project: Project,
      creatorId: String,
      creatorRole: ProjectRole = ProjectRole.OWNER
  ): Result<String>

  /** Update project details */
  suspend fun updateProject(project: Project): Result<Unit>

  /** Delete project */
  suspend fun deleteProject(projectId: String): Result<Unit>

  /** Get members of a project with real-time updates */
  fun getMembers(projectId: String): Flow<List<Member>>

  /** Add member to project */
  suspend fun addMember(projectId: String, userId: String, role: ProjectRole): Result<Unit>

  /** Remove member from project */
  suspend fun removeMember(projectId: String, userId: String): Result<Unit>

  /** Update member role */
  suspend fun updateMemberRole(projectId: String, userId: String, role: ProjectRole): Result<Unit>
}
