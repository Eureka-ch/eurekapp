package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.project.Project
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Mock implementation of IdeasViewModel for Android tests. */
class MockIdeasViewModel :
    IdeasViewModel(
        projectRepository = RepositoriesProvider.projectRepository,
        ideasRepository = IdeasRepositoryPlaceholder(),
        getCurrentUserId = { "test-user-id" }) {

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

  override fun addParticipantToIdea(ideaId: String, userId: String) {
    addParticipantToIdeaCalled = true
  }

  override fun updateMessage(message: String) {
    updateMessageCalled = true
    _uiState.value = _uiState.value.copy(currentMessage = message)
  }

  override fun sendMessage() {
    sendMessageCalled = true
  }

  override fun clearError() {
    clearErrorCalled = true
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  override fun getCurrentUserId(): String? = "test-user-id"
}
