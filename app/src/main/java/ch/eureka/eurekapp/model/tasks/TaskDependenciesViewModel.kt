package ch.eureka.eurekapp.model.tasks

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRepositoryProvider
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskRepositoryProvider
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.data.user.UserRepositoryProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.collections.emptyList

class TaskDependenciesViewModel(
    private val tasksRepository: TaskRepository = TaskRepositoryProvider._repository,
    private val usersRepository: UserRepository = UserRepositoryProvider.repository,
    private val projectsRepository: ProjectRepository = ProjectRepositoryProvider.repository
): ViewModel()  {

    fun getDependentTasksForTask(projectId: String, task: Task): List<Flow<Task?>>{
        return task.dependingOnTasks.map { taskId -> tasksRepository
            .getTaskById(projectId, task.taskID) }
    }

    fun getProjectUsers(projectId: String): Flow<List<Flow<User?>>>{
        return projectsRepository.getProjectById(projectId).map { project ->
            val ids = project?.memberIds.orEmpty()
            if(ids.isEmpty()) return@map emptyList()

            ids.map { userId ->
                usersRepository.getUserById(userId)
            }
        }
    }

    fun getTaskFromRepository(projectId: String, taskId: String): Flow<Task?>{
        return tasksRepository.getTaskById(projectId,taskId)
    }
}