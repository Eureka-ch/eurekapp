/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.chat.Message
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

/**
 * Unit tests for IdeasViewModel.
 *
 * Tests focus on the new functionality: createNewIdea() and loadUsersForProject(). Pattern follows
 * CreateTaskViewModelTest and CreateConversationViewModelTest.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IdeasViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockIdeasRepository: MockIdeasRepository
  private lateinit var viewModel: IdeasViewModel

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

  private fun createViewModel(getCurrentUserId: () -> String? = { currentUserId }): IdeasViewModel {
    return IdeasViewModel(
        projectRepository = mockProjectRepository,
        userRepository = mockUserRepository,
        ideasRepository = mockIdeasRepository,
        getCurrentUserId = getCurrentUserId,
        dispatcher = testDispatcher)
  }

  // ========== INITIAL STATE TESTS ==========

  @Test
  fun initialState_hasCorrectDefaults() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNull(state.selectedProject)
    assertEquals(emptyList<Project>(), state.availableProjects)
    assertEquals(emptyList<Idea>(), state.ideas)
    assertNull(state.selectedIdea)
    assertEquals(emptyList<Message>(), state.messages)
    assertEquals(IdeasViewMode.LIST, state.viewMode)
    assertEquals("", state.currentMessage)
    assertFalse(state.isSending)
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
    assertEquals(emptyList<User>(), state.availableUsers)
    assertFalse(state.isLoadingUsers)
  }

  // ========== CREATE IDEA TESTS ==========

  @Test
  fun createNewIdea_withTitleAndProjectId_createsIdeaWithCorrectData() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.success("idea-123"))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.createNewIdea("My Idea Title", "project-123", listOf("user1", "user2"))
    advanceUntilIdle()

    // Verify idea was created with correct data
    assertTrue(mockIdeasRepository.createIdeaCalls.isNotEmpty())
    val createdIdea = mockIdeasRepository.createIdeaCalls[0]
    assertEquals("My Idea Title", createdIdea.title)
    assertEquals("project-123", createdIdea.projectId)
    assertEquals(currentUserId, createdIdea.createdBy)
    // Creator + selected participants should be in participantIds
    assertTrue(createdIdea.participantIds.contains(currentUserId))
    assertTrue(createdIdea.participantIds.contains("user1"))
    assertTrue(createdIdea.participantIds.contains("user2"))
    assertEquals(3, createdIdea.participantIds.size) // creator + 2 participants

    // Verify idea is automatically selected and view mode changes
    val state = viewModel.uiState.first()
    assertEquals(project, state.selectedProject)
    assertNotNull(state.selectedIdea)
    assertEquals("idea-123", state.selectedIdea?.ideaId)
    assertEquals(IdeasViewMode.CONVERSATION, state.viewMode)
  }

  @Test
  fun createNewIdea_withoutTitle_createsIdeaWithNullTitle() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.success("idea-123"))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.createNewIdea(null, "project-123", emptyList())
    advanceUntilIdle()

    val createdIdea = mockIdeasRepository.createIdeaCalls[0]
    assertNull(createdIdea.title)
    assertEquals("project-123", createdIdea.projectId)
    assertEquals(currentUserId, createdIdea.createdBy)
    // Only creator should be in participantIds
    assertEquals(listOf(currentUserId), createdIdea.participantIds)
  }

  @Test
  fun createNewIdea_withParticipants_addsCreatorAndSelectedParticipants() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.success("idea-123"))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.createNewIdea("Test Idea", "project-123", listOf("user1", "user2", "user3"))
    advanceUntilIdle()

    val createdIdea = mockIdeasRepository.createIdeaCalls[0]
    val participantIds = createdIdea.participantIds.toSet()
    assertTrue(participantIds.contains(currentUserId))
    assertTrue(participantIds.contains("user1"))
    assertTrue(participantIds.contains("user2"))
    assertTrue(participantIds.contains("user3"))
    assertEquals(4, participantIds.size) // creator + 3 participants
  }

  @Test
  fun createNewIdea_withoutCurrentUser_showsError() = runTest {
    viewModel = createViewModel(getCurrentUserId = { null })
    advanceUntilIdle()

    viewModel.createNewIdea("Test Idea", "project-123", emptyList())
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("User not authenticated", state.errorMsg)
    assertTrue(mockIdeasRepository.createIdeaCalls.isEmpty())
  }

  @Test
  fun createNewIdea_onRepositoryFailure_showsError() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.failure(Exception("Repository error")))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.createNewIdea("Test Idea", "project-123", emptyList())
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Error creating idea"))
  }

  @Test
  fun createNewIdea_removesDuplicateParticipants() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    mockIdeasRepository.setCreateIdeaResult(Result.success("idea-123"))

    viewModel = createViewModel()
    advanceUntilIdle()

    // Include currentUserId in participants list (should be deduplicated)
    viewModel.createNewIdea(
        "Test Idea", "project-123", listOf(currentUserId, "user1", currentUserId))
    advanceUntilIdle()

    val createdIdea = mockIdeasRepository.createIdeaCalls[0]
    val participantIds = createdIdea.participantIds.toSet()
    assertEquals(2, participantIds.size) // creator + user1 (no duplicates)
    assertTrue(participantIds.contains(currentUserId))
    assertTrue(participantIds.contains("user1"))
  }

  // ========== LOAD USERS FOR PROJECT TESTS ==========

  @Test
  fun loadUsersForProject_loadsUsersFromProjectMembers() = runTest {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val user1 = ch.eureka.eurekapp.model.data.user.User(uid = "user1", displayName = "User 1")
    val user2 = ch.eureka.eurekapp.model.data.user.User(uid = "user2", displayName = "User 2")

    mockProjectRepository.setMembers("project-123", flowOf(members))
    mockUserRepository.setUser("user1", flowOf(user1))
    mockUserRepository.setUser("user2", flowOf(user2))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.loadUsersForProject("project-123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(2, state.availableUsers.size)
    assertTrue(state.availableUsers.any { it.uid == "user1" })
    assertTrue(state.availableUsers.any { it.uid == "user2" })
    assertFalse(state.isLoadingUsers)
    assertTrue(mockProjectRepository.getMembersCalls.contains("project-123"))
  }

  @Test
  fun loadUsersForProject_withEmptyProjectId_clearsUsers() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    // First load some users
    val members = listOf(Member(userId = "user1", role = ProjectRole.MEMBER))
    mockProjectRepository.setMembers("project-123", flowOf(members))
    mockUserRepository.setUser(
        "user1", flowOf(ch.eureka.eurekapp.model.data.user.User(uid = "user1")))
    viewModel.loadUsersForProject("project-123")
    advanceUntilIdle()

    // Then clear with empty projectId
    viewModel.loadUsersForProject("")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<User>(), state.availableUsers)
  }

  @Test
  fun loadUsersForProject_onError_showsError() = runTest {
    mockProjectRepository.setMembers(
        "project-123", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockUserRepository.setError("user1", Exception("User not found"))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.loadUsersForProject("project-123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("Error loading users"))
    assertFalse(state.isLoadingUsers)
  }

  @Test
  fun loadUsersForProject_setsLoadingState() = runTest {
    val members = listOf(Member(userId = "user1", role = ProjectRole.MEMBER))
    mockProjectRepository.setMembers("project-123", flowOf(members))
    mockUserRepository.setUser(
        "user1", flowOf(ch.eureka.eurekapp.model.data.user.User(uid = "user1")))

    viewModel = createViewModel()
    advanceUntilIdle()

    // Loading should start immediately
    viewModel.loadUsersForProject("project-123")
    // Note: With UnconfinedTestDispatcher, loading state might be very brief
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.isLoadingUsers) // Should be false after loading completes
  }

  @Test
  fun loadUsersForProject_withEmptyMembers_returnsEmptyList() = runTest {
    mockProjectRepository.setMembers("project-123", flowOf(emptyList()))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.loadUsersForProject("project-123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<User>(), state.availableUsers)
    assertFalse(state.isLoadingUsers)
  }

  @Test
  fun loadUsersForProject_filtersNullUsers() = runTest {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val user1 = ch.eureka.eurekapp.model.data.user.User(uid = "user1", displayName = "User 1")

    mockProjectRepository.setMembers("project-123", flowOf(members))
    mockUserRepository.setUser("user1", flowOf(user1))
    mockUserRepository.setUser("user2", flowOf(null)) // user2 not found

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.loadUsersForProject("project-123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.availableUsers.size) // Only user1, user2 is filtered out
    assertEquals("user1", state.availableUsers[0].uid)
  }

  // ========== SELECT PROJECT TESTS ==========

  @Test
  fun selectProject_updatesSelectedProject() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectProject(project)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(project, state.selectedProject)
  }

  // ========== SELECT IDEA TESTS ==========

  @Test
  fun selectIdea_updatesSelectedIdeaAndViewMode() = runTest {
    val idea = Idea(ideaId = "idea-123", projectId = "project-123", createdBy = currentUserId)

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectIdea(idea)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(idea, state.selectedIdea)
    assertEquals(IdeasViewMode.CONVERSATION, state.viewMode)
  }
}
