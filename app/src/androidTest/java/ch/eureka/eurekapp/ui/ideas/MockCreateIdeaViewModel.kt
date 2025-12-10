package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Portions of this file were written with the help of GPT-5 Codex and Gemini.
/** Mock implementation of CreateIdeaViewModel for Android tests. */
class MockCreateIdeaViewModel :
    CreateIdeaViewModel(
        projectRepository = RepositoriesProvider.projectRepository,
        userRepository = RepositoriesProvider.userRepository,
        ideasRepository = MockIdeasRepository()) {
  private val _uiState =
      MutableStateFlow(
          CreateIdeaState(
              title = "",
              selectedProject = null,
              selectedParticipantIds = emptySet(),
              availableProjects = emptyList(),
              availableUsers = emptyList(),
              isCreating = false,
              errorMsg = null,
              navigateToIdea = null))

  override val uiState: StateFlow<CreateIdeaState> = _uiState.asStateFlow()

  var updateTitleCalled = false
  var selectProjectCalled = false
  var toggleParticipantCalled = false
  var createIdeaCalled = false
  var clearErrorCalled = false
  var resetNavigationCalled = false
  var resetCalled = false

  fun setAvailableProjects(projects: List<Project>) {
    _uiState.value = _uiState.value.copy(availableProjects = projects)
  }

  fun setAvailableUsers(users: List<User>) {
    _uiState.value = _uiState.value.copy(availableUsers = users)
  }

  override fun updateTitle(title: String) {
    updateTitleCalled = true
    _uiState.value = _uiState.value.copy(title = title)
  }

  override fun selectProject(project: Project) {
    selectProjectCalled = true
    _uiState.value =
        _uiState.value.copy(selectedProject = project, selectedParticipantIds = emptySet())
  }

  override fun toggleParticipant(userId: String) {
    toggleParticipantCalled = true
    val currentSet = _uiState.value.selectedParticipantIds.toMutableSet()
    if (currentSet.contains(userId)) {
      currentSet.remove(userId)
    } else {
      currentSet.add(userId)
    }
    _uiState.value = _uiState.value.copy(selectedParticipantIds = currentSet)
  }

  override fun createIdea() {
    createIdeaCalled = true
  }

  override fun clearError() {
    clearErrorCalled = true
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }

  override fun resetNavigation() {
    resetNavigationCalled = true
    _uiState.value = _uiState.value.copy(navigateToIdea = null)
  }

  override fun reset() {
    resetCalled = true
    _uiState.value = CreateIdeaState()
  }
}
