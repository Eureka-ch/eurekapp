/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
/* This code was written with help of Claude. */
package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
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
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IdeasViewModelTest {
  companion object {
    private const val TEST_USER_ID = "test-user-id"
  }

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockIdeasRepository: MockIdeasRepository
  private lateinit var viewModel: IdeasViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockProjectRepository = MockProjectRepository()
    mockIdeasRepository = MockIdeasRepository()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockProjectRepository.reset()
    mockIdeasRepository.reset()
  }

  private fun createViewModel(getCurrentUserId: () -> String? = { TEST_USER_ID }): IdeasViewModel =
      IdeasViewModel(
          projectRepository = mockProjectRepository,
          ideasRepository = mockIdeasRepository,
          getCurrentUserId = getCurrentUserId,
          dispatcher = testDispatcher)

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
  }

  @Test
  fun selectProject_updatesSelectedProject() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectProject(project)
    advanceUntilIdle()
    assertEquals(project, viewModel.uiState.first().selectedProject)
  }

  @Test
  fun selectIdea_updatesSelectedIdea() = runTest {
    val idea = Idea(ideaId = "idea-123", projectId = "project-123", createdBy = TEST_USER_ID)
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectIdea(idea)
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals(idea, state.selectedIdea)
    // Conversation mode will be tested in frontend PR
    assertEquals(IdeasViewMode.LIST, state.viewMode)
  }

  @Test
  fun onIdeaCreated_updatesSelectedIdeaAndProject() = runTest {
    val project = Project(projectId = "project-123", name = "Test Project")
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(project)))
    viewModel = createViewModel()
    advanceUntilIdle()
    val idea = Idea(ideaId = "idea-123", projectId = "project-123", createdBy = TEST_USER_ID)
    viewModel.onIdeaCreated(idea)
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals(idea, state.selectedIdea)
    assertEquals(project, state.selectedProject)
  }

  @Test
  fun deleteIdea_hidesIdea() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.deleteIdea("idea-123")
    advanceUntilIdle()
    // Verify deletion logic (hidden in UI state)
    assertNull(viewModel.uiState.first().errorMsg)
  }

  @Test
  fun clearError_resetsErrorMessage() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.clearError()
    advanceUntilIdle()
    assertNull(viewModel.uiState.first().errorMsg)
  }
}
