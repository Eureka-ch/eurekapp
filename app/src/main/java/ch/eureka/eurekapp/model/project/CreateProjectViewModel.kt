package ch.eureka.eurekapp.model.project

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.authentication.AuthRepositoryProvider

class CreateProjectViewModel(
    private val projectsRepository: ProjectRepository = ProjectRepositoryProvider.repository,
    private val authenticationRepository: AuthRepository = AuthRepositoryProvider.repository
): ViewModel() {

}