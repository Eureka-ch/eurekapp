package ch.eureka.eurekapp.model.project

import kotlinx.coroutines.flow.Flow

interface ProjectRepository {
  /** Get project by ID with real-time updates */
  fun getProjectById(workspaceId: String, groupId: String, projectId: String): Flow<Project?>

  /** Create a new project */
  suspend fun createProject(project: Project): Result<String>

  /** Update project details */
  suspend fun updateProject(project: Project): Result<Unit>

  /** Delete project */
  suspend fun deleteProject(workspaceId: String, groupId: String, projectId: String): Result<Unit>

  /** Gets a new id for a new project **/
  suspend fun getNewProjectId(): Result<String>
}
