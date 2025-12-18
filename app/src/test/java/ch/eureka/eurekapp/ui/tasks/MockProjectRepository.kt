package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Configurable mock implementation of ProjectRepository for testing
 *
 * Allows tests to configure project data and flows
 *
 * Note: This file was partially written by GPT-5 Codex and Claude. Co-author: GPT-5 Co-author:
 * Claude
 */
class MockProjectRepository : ProjectRepository {
  private var currentUserProjects: Flow<List<Project>> = flowOf(emptyList())
  private val memberFlows = mutableMapOf<String, Flow<List<Member>>>()

  // Track method calls for verification
  val getProjectsForCurrentUserCalls = mutableListOf<Unit>()
  val getMembersCalls = mutableListOf<String>()

  /** Configure projects returned by getProjectsForCurrentUser() */
  fun setCurrentUserProjects(flow: Flow<List<Project>>) {
    currentUserProjects = flow
  }

  /** Configure members returned by getMembers() for a specific project */
  fun setMembers(projectId: String, flow: Flow<List<Member>>) {
    memberFlows[projectId] = flow
  }

  /** Clear all configuration */
  fun reset() {
    currentUserProjects = flowOf(emptyList())
    memberFlows.clear()
    getProjectsForCurrentUserCalls.clear()
    getMembersCalls.clear()
  }

  override fun getProjectById(projectId: String): Flow<Project?> = flowOf(null)

  override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
    getProjectsForCurrentUserCalls.add(Unit)
    return currentUserProjects
  }

  override suspend fun createProject(
      project: Project,
      creatorId: String,
      creatorRole: ProjectRole
  ): Result<String> = Result.success("mock-project-id")

  override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

  override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

  override fun getMembers(projectId: String): Flow<List<Member>> {
    getMembersCalls.add(projectId)
    return memberFlows[projectId] ?: flowOf(emptyList())
  }

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
