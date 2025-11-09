package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/

/**
 * Configurable mock implementation of TaskRepository for testing
 *
 * Allows tests to configure task data, flows, and error scenarios
 */
class MockTaskRepository : TaskRepository {
  private var currentUserTasks: Flow<List<Task>> = flowOf(emptyList())
  private val projectTasks = mutableMapOf<String, Flow<List<Task>>>()
  private val tasksById = mutableMapOf<String, Task>()
  private var updateTaskResult: Result<Unit> = Result.success(Unit)
  private var createTaskResult: Result<String> = Result.success("mock-task-id")

  // Track method calls for verification
  val updateTaskCalls = mutableListOf<Task>()
  val createTaskCalls = mutableListOf<Task>()
  val getTasksForCurrentUserCalls = mutableListOf<Unit>()
  val getTasksInProjectCalls = mutableListOf<String>()

  /** Add a task that can be retrieved by getTaskById */
  fun addTask(task: Task) {
    tasksById[task.taskID] = task
  }

  /** Configure tasks returned by getTasksForCurrentUser() */
  fun setCurrentUserTasks(flow: Flow<List<Task>>) {
    currentUserTasks = flow
  }

  /** Configure tasks returned by getTasksInProject() */
  fun setProjectTasks(projectId: String, flow: Flow<List<Task>>) {
    projectTasks[projectId] = flow
  }

  /** Configure the result returned by updateTask() */
  fun setUpdateTaskResult(result: Result<Unit>) {
    updateTaskResult = result
  }

  /** Configure the result returned by createTask() */
  fun setCreateTaskResult(result: Result<String>) {
    createTaskResult = result
  }

  /** Clear all configuration */
  fun reset() {
    currentUserTasks = flowOf(emptyList())
    projectTasks.clear()
    tasksById.clear()
    updateTaskResult = Result.success(Unit)
    createTaskResult = Result.success("mock-task-id")
    updateTaskCalls.clear()
    createTaskCalls.clear()
    getTasksForCurrentUserCalls.clear()
    getTasksInProjectCalls.clear()
  }

  override fun getTaskById(projectId: String, taskId: String): Flow<Task?> {
    return flowOf(tasksById[taskId])
  }

  override fun getTasksInProject(projectId: String): Flow<List<Task>> {
    getTasksInProjectCalls.add(projectId)
    return projectTasks[projectId] ?: flowOf(emptyList())
  }

  override fun getTasksForCurrentUser(): Flow<List<Task>> {
    getTasksForCurrentUserCalls.add(Unit)
    return currentUserTasks
  }

  override suspend fun createTask(task: Task): Result<String> {
    createTaskCalls.add(task)
    tasksById[task.taskID] = task
    return createTaskResult
  }

  override suspend fun updateTask(task: Task): Result<Unit> {
    updateTaskCalls.add(task)
    tasksById[task.taskID] = task
    return updateTaskResult
  }

  override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> =
      Result.success(Unit)

  override suspend fun assignUser(projectId: String, taskId: String, userId: String): Result<Unit> =
      Result.success(Unit)

  override suspend fun unassignUser(
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit> = Result.success(Unit)
}
