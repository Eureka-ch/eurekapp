package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Mock implementation of TaskRepository for previews only This should only be used in @Preview
 * composables
 */
class MockTaskRepository : TaskRepository {
  override fun getTaskById(projectId: String, taskId: String): Flow<Task?> = flowOf(null)

  override fun getTasksInProject(projectId: String): Flow<List<Task>> = flowOf(emptyList())

  override fun getTasksForCurrentUser(): Flow<List<Task>> = flowOf(emptyList())

  override suspend fun createTask(task: Task): Result<String> = Result.success("mock-task-id")

  override suspend fun updateTask(task: Task): Result<Unit> = Result.success(Unit)

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
