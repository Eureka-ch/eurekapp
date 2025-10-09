package ch.eureka.eurekapp.model.task

import kotlinx.coroutines.flow.Flow

interface TaskRepository {
  /** Get task by ID with real-time updates */
  fun getTaskById(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String
  ): Flow<Task?>

  /** Get all tasks in project with real-time updates */
  fun getTasksInProject(workspaceId: String, groupId: String, projectId: String): Flow<List<Task>>

  /** Get all tasks assigned to current user across all projects with real-time updates */
  fun getTasksForCurrentUser(): Flow<List<Task>>

  /** Get tasks assigned to current user in specific workspace with real-time updates */
  fun getTasksForCurrentUserInWorkspace(workspaceId: String): Flow<List<Task>>

  /** Create a new task */
  suspend fun createTask(task: Task): Result<String>

  /** Update task details */
  suspend fun updateTask(task: Task): Result<Unit>

  /** Delete task */
  suspend fun deleteTask(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String
  ): Result<Unit>

  /** Assign user to task */
  suspend fun assignUser(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit>

  /** Unassign user from task */
  suspend fun unassignUser(
      workspaceId: String,
      groupId: String,
      projectId: String,
      taskId: String,
      userId: String
  ): Result<Unit>
}
