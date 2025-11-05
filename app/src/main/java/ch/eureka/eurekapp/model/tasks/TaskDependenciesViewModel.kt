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
import kotlin.collections.emptyList
import kotlinx.coroutines.flow.Flow
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
    private val tasksRepository: TaskRepository = TaskRepositoryProvider._repository,
    private val usersRepository: UserRepository = UserRepositoryProvider.repository,
    private val projectsRepository: ProjectRepository = ProjectRepositoryProvider.repository
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
  fun getDependentTasksForTask(projectId: String, task: Task): List<Flow<Task?>> {
    return task.dependingOnTasks.map { taskId -> tasksRepository.getTaskById(projectId, taskId) }
  }

  /**
   * Retrieves all users associated with a given project.
   *
   * @param projectId The ID of the project.
   * @return A Flow emitting a list of Flows, each representing a user in the project.
   *
   * Disclaimer: This description was written by AI (ChatGPT - GPT-5).
   */
  fun getProjectUsers(projectId: String): Flow<List<Flow<User?>>> {
    return projectsRepository.getProjectById(projectId).map { project ->
      val ids = project?.memberIds.orEmpty()
      if (ids.isEmpty()) return@map emptyList()

      ids.map { userId -> usersRepository.getUserById(userId) }
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
