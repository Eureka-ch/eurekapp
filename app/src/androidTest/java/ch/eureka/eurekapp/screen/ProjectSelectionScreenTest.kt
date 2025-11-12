package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectSelectionScreenViewModel
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.screens.ProjectSelectionScreen
import ch.eureka.eurekapp.screens.ProjectSelectionScreenTestTags
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

val fakeProject1 =
    Project().copy(projectId = "test-project-1", memberIds = listOf("user1", "user2"))
val fakeProject2 = Project().copy(projectId = "test-project-2", memberIds = listOf("user1"))

class ProjectSelectionScreenTest : TestCase() {
  @get:Rule val composeRule = createComposeRule()

  private class MockedProjectsRepository : ProjectRepository {
    override fun getProjectById(projectId: String): Flow<Project?> {
      if (projectId == fakeProject1.projectId) {
        return flowOf(fakeProject1)
      } else {
        return flowOf(fakeProject2)
      }
    }

    override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
      return flowOf(listOf(fakeProject1, fakeProject2))
    }

    override suspend fun createProject(
        project: Project,
        creatorId: String,
        creatorRole: ProjectRole
    ): Result<String> {
      TODO("Not yet implemented")
    }

    override suspend fun updateProject(project: Project): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
      TODO("Not yet implemented")
    }

    override fun getMembers(projectId: String): Flow<List<Member>> {
      if (projectId == fakeProject1.projectId) {
        return flowOf(listOf(Member().copy(userId = "user1"), Member().copy(userId = "user2")))
      } else {
        return flowOf(listOf(Member().copy(userId = "user1")))
      }
    }

    override suspend fun addMember(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun removeMember(projectId: String, userId: String): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun updateMemberRole(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }
  }

  private class MockedUserRepository : UserRepository {
    override fun getUserById(userId: String): Flow<User?> {
      return flowOf(User().copy(uid = userId))
    }

    override fun getCurrentUser(): Flow<User?> {
      TODO("Not yet implemented")
    }

    override suspend fun saveUser(user: User): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun updateLastActive(userId: String): Result<Unit> {
      TODO("Not yet implemented")
    }
  }

  @Test
  fun getProjectsForUserWorks() = runBlocking {
    val fakeViewModel =
        ProjectSelectionScreenViewModel(
            projectsRepository = MockedProjectsRepository(),
            usersRepository = MockedUserRepository())

    val projectsForUser = fakeViewModel.getProjectsForUser().first()

    assertEquals(listOf(fakeProject1, fakeProject2), projectsForUser)
  }

  @Test
  fun getProjectUsersInformationWorks() = runBlocking {
    val fakeViewModel =
        ProjectSelectionScreenViewModel(
            projectsRepository = MockedProjectsRepository(),
            usersRepository = MockedUserRepository())

    val users = fakeViewModel.getProjectUsersInformation("test-project-1").first()
    assertEquals(listOf(User().copy(uid = "user1"), User().copy(uid = "user2")), users)
  }

  @Test
  fun testProjectScreenCorrectlyShowsProjectsAndNavigatesToThem() {
    val fakeViewModel =
        ProjectSelectionScreenViewModel(
            projectsRepository = MockedProjectsRepository(),
            usersRepository = MockedUserRepository())

    var project1HasBeenNavigatedTo = false
    var project2HasBeenNavigatedTo = false

    composeRule.setContent {
      ProjectSelectionScreen(
          projectSelectionScreenViewModel = fakeViewModel,
          onCreateProjectRequest = {},
          onProjectSelectRequest = { project ->
            if (project.projectId == "test-project-1") {
              project1HasBeenNavigatedTo = true
            }
            if (project.projectId == "test-project-2") {
              project2HasBeenNavigatedTo = true
            }
          })
    }

    composeRule.waitForIdle()

    composeRule
        .onNodeWithTag(
            ProjectSelectionScreenTestTags.getNavigateButtonTestTagForButton("test-project-1"))
        .performClick()
    composeRule
        .onNodeWithTag(
            ProjectSelectionScreenTestTags.getNavigateButtonTestTagForButton("test-project-2"))
        .performClick()

    assertEquals(true, project1HasBeenNavigatedTo)
    assertEquals(true, project2HasBeenNavigatedTo)
  }

  @Test
  fun testProjectScreenCorrectlyCallsCreateProjectCallback() {
    var createProjectCalled = false
    composeRule.setContent {
      ProjectSelectionScreen(
          onProjectSelectRequest = {}, onCreateProjectRequest = { createProjectCalled = true })
    }
    composeRule.waitForIdle()
    composeRule.onNodeWithTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON).performClick()
    assertEquals(true, createProjectCalled)
  }
}
