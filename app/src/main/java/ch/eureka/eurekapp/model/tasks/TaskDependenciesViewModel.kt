package ch.eureka.eurekapp.model.tasks

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import kotlin.collections.emptyList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

/**
 * ViewModel responsible for providing task dependency and user data within a project.
 *
 * @property tasksRepository Reference to the TaskRepository for fetching task data.
 * @property usersRepository Reference to the UserRepository for fetching user data.
 * @property projectsRepository Reference to the ProjectRepository for accessing project details.
 *
 * Disclaimer: This documentation was written by AI (ChatGPT - GPT-5).
 */
class TaskDependenciesViewModel(
    private val tasksRepository: TaskRepository = FirestoreRepositoriesProvider.taskRepository,
    private val usersRepository: UserRepository = FirestoreRepositoriesProvider.userRepository,
    private val projectsRepository: ProjectRepository =
        FirestoreRepositoriesProvider.projectRepository
) : ViewModel() {
  /**
   * Retrieves the list of dependent tasks for a given task.
   *
   * @param projectId The ID of the project containing the task.
   * @param task The task whose dependencies are to be fetched.
   * @return A list of Flows representing each dependent task.
   *
   * Disclaimer: This description was written by AI (ChatGPT - GPT-5).
   */
  fun getDependentTasksForTask(projectId: String, task: Task): Flow<List<Task?>> {
    return combine(
        task.dependingOnTasks.map { taskId -> tasksRepository.getTaskById(projectId, taskId) }) {
            tasksArray ->
          tasksArray.toList()
        }
  }

  /**
   * Retrieves all users associated with a given project.
   *
   * @param projectId The ID of the project.
   * @return A Flow emitting a list of Flows, each representing a user in the project.
   *
   * Disclaimer: This description was written by AI (ChatGPT - GPT-5).
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  fun getProjectUsers(projectId: String): Flow<List<User?>> {
    return projectsRepository.getProjectById(projectId).flatMapLatest { project ->
      val ids = project?.memberIds.orEmpty()
      if (ids.isEmpty()) return@flatMapLatest flowOf(emptyList())

      combine(ids.map { userId -> usersRepository.getUserById(userId) }) { array -> array.toList() }
    }
  }

  /**
   * Retrieves a specific task from the repository.
   *
   * @param projectId The ID of the project containing the task.
   * @param taskId The ID of the task to be retrieved.
   * @return A Flow emitting the task object, or null if not found.
   *
   * Disclaimer: This description was written by AI (ChatGPT - GPT-5).
   */
  fun getTaskFromRepository(projectId: String, taskId: String): Flow<Task?> {
    return tasksRepository.getTaskById(projectId, taskId)
  }
}
