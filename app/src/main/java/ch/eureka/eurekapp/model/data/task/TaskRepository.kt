package ch.eureka.eurekapp.model.data.task

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  /** Get task by ID with real-time updates */
  fun getTaskById(projectId: String, taskId: String): Flow<Task?>

  /** Get all tasks in project with real-time updates */
  fun getTasksInProject(projectId: String): Flow<List<Task>>

  /** Get all tasks assigned to current user across all projects with real-time updates */
  fun getTasksForCurrentUser(): Flow<List<Task>>

  /** Create a new task */
  suspend fun createTask(task: Task): Result<String>

  /** Update task details */
  suspend fun updateTask(task: Task): Result<Unit>

  /** Delete task */
  suspend fun deleteTask(projectId: String, taskId: String): Result<Unit>

  /** Assign user to task */
  suspend fun assignUser(projectId: String, taskId: String, userId: String): Result<Unit>

  /** Unassign user from task */
  suspend fun unassignUser(projectId: String, taskId: String, userId: String): Result<Unit>
}
