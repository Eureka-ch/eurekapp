package ch.eureka.eurekapp.model.data.project

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.authentication.AuthRepository
import ch.eureka.eurekapp.model.authentication.AuthRepositoryProvider

class CreateProjectViewModel(
    private val projectsRepository: ProjectRepository = ProjectRepositoryProvider.repository,
    private val authenticationRepository: AuthRepository = AuthRepositoryProvider.repository
): ViewModel() {

    suspend fun createProject(projectToCreate: Project, onSuccessCallback: () -> Unit,
                              onFailureCallback: () -> Unit){
        if(projectsRepository.createProject(projectToCreate,
                projectToCreate.createdBy).isSuccess){
            onSuccessCallback()
        }else{
            onFailureCallback()
        }
    }

    fun getCurrentUser(): String?{
        return authenticationRepository.getUserId().getOrNull()
    }

    suspend fun getNewProjectId(): String?{
        return projectsRepository.getNewProjectId().getOrNull()
    }
}