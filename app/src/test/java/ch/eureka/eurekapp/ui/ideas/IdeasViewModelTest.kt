/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.chat.Message
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
  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockIdeasRepository: MockIdeasRepository
  private lateinit var viewModel: IdeasViewModel
  private val currentUserId = "current-user-123"

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

  private fun createViewModel(getCurrentUserId: () -> String? = { currentUserId }): IdeasViewModel =
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
    val idea = Idea(ideaId = "idea-123", projectId = "project-123", createdBy = currentUserId)
    viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.selectIdea(idea)
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertEquals(idea, state.selectedIdea)
    // Conversation mode will be tested in frontend PR
    assertEquals(IdeasViewMode.LIST, state.viewMode)
  }
}
