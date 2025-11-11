package ch.eureka.eurekapp.model.data.project

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

class ProjectSelectionScreenViewModel(
    private val projectsRepository: ProjectRepository = FirestoreRepositoriesProvider.projectRepository,
    private val usersRepository: UserRepository = FirestoreRepositoriesProvider.userRepository
): ViewModel() {
    init {
    }

    fun getProjectsForUser(): Flow<List<Project>> {
        return projectsRepository.getProjectsForCurrentUser(skipCache = false)
    }

    fun getProjectUsersInformation(projectId: String): Flow<List<User>>{
        return projectsRepository.getMembers(projectId).flatMapLatest{ members ->
            val usersFlow = members.map {member -> usersRepository
                .getUserById(member.userId) }
            combine(usersFlow){ flow ->
                flow.filterNotNull().toList()
            }
        }
    }

}