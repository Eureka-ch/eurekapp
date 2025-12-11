package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Portions of this file were written with the help of GPT-5 Codex and Gemini.
/** Mock implementation of IdeasViewModel for Android tests. */
class MockIdeasViewModel(
    projectRepository: MockProjectRepository = MockProjectRepository(),
    ideasRepository: MockIdeasRepository = MockIdeasRepository(),
    private val testUserId: String = TEST_USER_ID
) :
    IdeasViewModel(
        projectRepository = projectRepository,
        ideasRepository = ideasRepository,
        getCurrentUserId = { testUserId }) {

  companion object {
    private const val TEST_USER_ID = "test-user-id"
  }

  private val _uiState =
      MutableStateFlow(
          IdeasUIState(
              selectedProject = null,
              availableProjects = emptyList(),
              ideas = emptyList(),
              selectedIdea = null,
              messages = emptyList(),
              viewMode = IdeasViewMode.LIST,
              currentMessage = "",
              isSending = false,
              isLoading = false,
              errorMsg = null))

  override val uiState: StateFlow<IdeasUIState> = _uiState.asStateFlow()

  var selectProjectCalled = false
  var selectIdeaCalled = false
  var deleteIdeaCalled = false
  var addParticipantToIdeaCalled = false
  var updateMessageCalled = false
  var sendMessageCalled = false
  var clearErrorCalled = false

  fun setAvailableProjects(projects: List<Project>) {
    _uiState.value = _uiState.value.copy(availableProjects = projects)
  }

  fun setSelectedProject(project: Project?) {
    _uiState.value = _uiState.value.copy(selectedProject = project)
  }

  fun setIdeas(ideas: List<Idea>) {
    _uiState.value = _uiState.value.copy(ideas = ideas)
  }

  fun setViewMode(viewMode: IdeasViewMode) {
    _uiState.value = _uiState.value.copy(viewMode = viewMode)
  }

  fun setIsLoading(isLoading: Boolean) {
    _uiState.value = _uiState.value.copy(isLoading = isLoading)
  }

  override fun selectProject(project: Project) {
    selectProjectCalled = true
    _uiState.value = _uiState.value.copy(selectedProject = project)
  }

  override fun selectIdea(idea: Idea) {
    selectIdeaCalled = true
    _uiState.value = _uiState.value.copy(selectedIdea = idea)
  }

  override fun onIdeaCreated(idea: Idea) {
    _uiState.value =
        _uiState.value.copy(
            selectedIdea = idea, viewMode = IdeasViewMode.CONVERSATION, selectedProject = null)
  }

  override fun deleteIdea(ideaId: String) {
    deleteIdeaCalled = true
  }

  override fun clearError() {
    clearErrorCalled = true
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  override fun getCurrentUserId(): String? = testUserId
}
