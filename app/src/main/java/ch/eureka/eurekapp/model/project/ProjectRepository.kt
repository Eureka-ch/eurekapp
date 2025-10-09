package ch.eureka.eurekapp.model.project

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
  /** Get project by ID with real-time updates */
  fun getProjectById(workspaceId: String, groupId: String, projectId: String): Flow<Project?>

  /** Get all projects in group with real-time updates */
  fun getProjectsInGroup(workspaceId: String, groupId: String): Flow<List<Project>>

  /** Create a new project */
  suspend fun createProject(project: Project): Result<String>

  /** Update project details */
  suspend fun updateProject(project: Project): Result<Unit>

  /** Delete project */
  suspend fun deleteProject(workspaceId: String, groupId: String, projectId: String): Result<Unit>
}
