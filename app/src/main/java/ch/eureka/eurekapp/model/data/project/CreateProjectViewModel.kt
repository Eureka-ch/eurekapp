package ch.eureka.eurekapp.model.data.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.authentication.AuthRepositoryProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import kotlinx.coroutines.launch

/**
 * The viewModel responsible for handling project creation in the app
 *
 * @param projectsRepository the project repository to access the projects' database
 * @param authenticationRepository the authentication repository that handles user authentication
 */
class CreateProjectViewModel(
    private val projectsRepository: ProjectRepository = ProjectRepositoryProvider.repository,
    private val authenticationRepository: AuthRepository = AuthRepositoryProvider.repository
) : ViewModel() {

  fun createProject(
      projectToCreate: Project,
      onSuccessCallback: () -> Unit,
      onFailureCallback: () -> Unit
  ) {
    viewModelScope.launch {
      if (projectsRepository.createProject(projectToCreate, projectToCreate.createdBy).isSuccess) {
        onSuccessCallback()
      } else {
        onFailureCallback()
      }
    }
  }

  fun getCurrentUser(): String? {
    return authenticationRepository.getUserId().getOrNull()
  }

  fun getNewProjectId(): String {
    return IdGenerator.generateProjectId()
  }
}
