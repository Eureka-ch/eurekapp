/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.ui.meeting

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

open class MockProjectRepository : ProjectRepository {
  private val projectsFlow = MutableStateFlow<List<Project>>(emptyList())
  var shouldThrow: Boolean = false

  fun emitProjects(projects: List<Project>) {
    projectsFlow.value = projects
  }

  override fun getProjectById(projectId: String): Flow<Project?> = flow {}

  override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
    if (shouldThrow) {
      return flow { throw Exception("Mock project error") }
    }
    return projectsFlow
  }

  override suspend fun createProject(
      project: Project,
      creatorId: String,
      creatorRole: ProjectRole
  ): Result<String> = Result.success("mock-project-id")

  override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

  override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

  override fun getMembers(projectId: String): Flow<List<Member>> = flow {}

  override suspend fun addMember(
      projectId: String,
      userId: String,
      role: ProjectRole
  ): Result<Unit> = Result.success(Unit)

  override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
      Result.success(Unit)

  override suspend fun updateMemberRole(
      projectId: String,
      userId: String,
      role: ProjectRole
  ): Result<Unit> = Result.success(Unit)
}
