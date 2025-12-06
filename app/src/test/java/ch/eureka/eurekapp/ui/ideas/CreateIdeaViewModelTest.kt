/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateIdeaViewModelTest {
  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockIdeasRepository: MockIdeasRepository
  private lateinit var viewModel: CreateIdeaViewModel
  private val currentUserId = "current-user-123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
    mockIdeasRepository = MockIdeasRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockProjectRepository.reset()
    mockUserRepository.reset()
    mockIdeasRepository.reset()
  }

  private fun createViewModel(
      getCurrentUserId: () -> String? = { currentUserId }
  ): CreateIdeaViewModel =
      CreateIdeaViewModel(
          projectRepository = mockProjectRepository,
          userRepository = mockUserRepository,
          ideasRepository = mockIdeasRepository,
          getCurrentUserId = getCurrentUserId,
          dispatcher = testDispatcher)

  @Test
  fun initialState_hasCorrectDefaults() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertNull(state.selectedProject)
    assertEquals(emptySet<String>(), state.selectedParticipantIds)
    assertEquals(emptyList<Project>(), state.availableProjects)
    assertEquals(emptyList<User>(), state.availableUsers)
    assertFalse(state.isCreating)
    assertNull(state.errorMsg)
    assertNull(state.navigateToIdea)
  }

  @Test
  fun updateTitle_updatesStateTitle() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.updateTitle("My Idea")
    advanceUntilIdle()
    assertEquals("My Idea", viewModel.uiState.first().title)
  }

  @Test
  fun selectProject_updatesSelectedProjectAndClearsParticipants() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectProject(project)
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals(project, state.selectedProject)
    assertEquals(emptySet<String>(), state.selectedParticipantIds)
  }

  @Test
  fun toggleParticipant_addsAndRemovesParticipant() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.toggleParticipant("user1")
    advanceUntilIdle()
    assertTrue(viewModel.uiState.first().selectedParticipantIds.contains("user1"))
    viewModel.toggleParticipant("user1")
    advanceUntilIdle()
    assertFalse(viewModel.uiState.first().selectedParticipantIds.contains("user1"))
  }

  @Test
  fun createIdea_withValidData_createsIdeaSuccessfully() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.success("idea-123"))
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.updateTitle("My Idea")
    viewModel.selectProject(project)
    viewModel.toggleParticipant("user1")
    advanceUntilIdle()
    viewModel.createIdea()
    advanceUntilIdle()
    val createdIdea = mockIdeasRepository.createIdeaCalls[0]
    assertEquals("My Idea", createdIdea.title)
    assertEquals("project-123", createdIdea.projectId)
    assertEquals(currentUserId, createdIdea.createdBy)
    assertTrue(createdIdea.participantIds.containsAll(listOf(currentUserId, "user1")))
    assertNotNull(viewModel.uiState.first().navigateToIdea)
  }

  @Test
  fun createIdea_withoutTitle_createsIdeaWithNullTitle() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.success("idea-123"))
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectProject(project)
    advanceUntilIdle()
    viewModel.createIdea()
    advanceUntilIdle()
    val createdIdea = mockIdeasRepository.createIdeaCalls[0]
    assertNull(createdIdea.title)
  }

  @Test
  fun createIdea_withoutProject_showsError() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.createIdea()
    advanceUntilIdle()
    assertEquals("Please select a project", viewModel.uiState.first().errorMsg)
    assertTrue(mockIdeasRepository.createIdeaCalls.isEmpty())
  }

  @Test
  fun createIdea_withoutCurrentUser_showsError() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    viewModel = createViewModel(getCurrentUserId = { null })
    advanceUntilIdle()
    viewModel.selectProject(project)
    advanceUntilIdle()
    viewModel.createIdea()
    advanceUntilIdle()
    assertEquals("User not authenticated", viewModel.uiState.first().errorMsg)
    assertTrue(mockIdeasRepository.createIdeaCalls.isEmpty())
  }

  @Test
  fun selectProject_loadsUsersForProject() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val user1 = User(uid = "user1", displayName = "User 1")
    val user2 = User(uid = "user2", displayName = "User 2")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockProjectRepository.setMembers("project-123", flowOf(members))
    mockUserRepository.setUser("user1", flowOf(user1))
    mockUserRepository.setUser("user2", flowOf(user2))
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectProject(project)
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals(2, state.availableUsers.size)
    assertTrue(state.availableUsers.any { it.uid == "user1" })
    assertTrue(state.availableUsers.any { it.uid == "user2" })
  }

  @Test
  fun reset_clearsAllState() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.updateTitle("My Idea")
    viewModel.selectProject(project)
    viewModel.toggleParticipant("user1")
    advanceUntilIdle()
    viewModel.reset()
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertNull(state.selectedProject)
    assertEquals(emptySet<String>(), state.selectedParticipantIds)
  }
}
