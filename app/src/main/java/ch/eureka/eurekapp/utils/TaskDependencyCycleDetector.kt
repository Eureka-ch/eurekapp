package ch.eureka.eurekapp.utils

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import kotlinx.coroutines.flow.first

// Portions of this code were generated with the help of AI.
/**
 * Utility object for detecting cycles in task dependency graphs.
 *
 * Uses Depth-First Search (DFS) to detect cycles. A cycle exists if adding a dependency
 * would create a circular reference where task A depends on task B, and task B (directly
 * or indirectly) depends on task A.
 */
object TaskDependencyCycleDetector {
  /**
   * Checks if adding a dependency would create a cycle.
   *
   * @param taskId The ID of the task that would have the new dependency
   * @param dependencyTaskId The ID of the task that would be added as a dependency
   * @param projectId The project ID containing the tasks
   * @param taskRepository Repository to fetch task data
   * @return true if adding the dependency would create a cycle, false otherwise
   */
  suspend fun wouldCreateCycle(
      taskId: String,
      dependencyTaskId: String,
      projectId: String,
      taskRepository: TaskRepository
  ): Boolean {
    // A task cannot depend on itself
    if (taskId == dependencyTaskId) {
      return true
    }

    // Build the dependency graph starting from the dependency task
    // If we can reach the original task, there's a cycle
    val visited = mutableSetOf<String>()
    return hasPathToTask(dependencyTaskId, taskId, projectId, taskRepository, visited)
  }

  /**
   * Checks if there's a path from startTaskId to targetTaskId in the dependency graph.
   *
   * @param startTaskId The task to start the search from
   * @param targetTaskId The task we're trying to reach
   * @param projectId The project ID containing the tasks
   * @param taskRepository Repository to fetch task data
   * @param visited Set of visited task IDs to prevent infinite loops
   * @return true if there's a path from startTaskId to targetTaskId, false otherwise
   */
  private suspend fun hasPathToTask(
      startTaskId: String,
      targetTaskId: String,
      projectId: String,
      taskRepository: TaskRepository,
      visited: MutableSet<String>
  ): Boolean {
    // If we've already visited this task, we're in a loop (but not necessarily the cycle we're
    // looking for)
    if (visited.contains(startTaskId)) {
      return false
    }

    // If we've reached the target, there's a path (cycle detected)
    if (startTaskId == targetTaskId) {
      return true
    }

    visited.add(startTaskId)

    // Get the task and check all its dependencies
    val task = taskRepository.getTaskById(projectId, startTaskId).first() ?: return false

    // Recursively check all dependencies
    for (dependencyId in task.dependingOnTasks) {
      if (hasPathToTask(dependencyId, targetTaskId, projectId, taskRepository, visited)) {
        return true
      }
    }

    return false
  }

  /**
   * Validates that a list of dependencies for a task doesn't create any cycles.
   *
   * @param taskId The ID of the task that would have these dependencies
   * @param dependencyTaskIds The list of task IDs that would be dependencies
   * @param projectId The project ID containing the tasks
   * @param taskRepository Repository to fetch task data
   * @return Result with error message if cycle detected, or success if no cycles
   */
  suspend fun validateNoCycles(
      taskId: String,
      dependencyTaskIds: List<String>,
      projectId: String,
      taskRepository: TaskRepository
  ): Result<Unit> {
    for (dependencyId in dependencyTaskIds) {
      if (wouldCreateCycle(taskId, dependencyId, projectId, taskRepository)) {
        return Result.failure(
            IllegalArgumentException(
                "Adding dependency '$dependencyId' would create a circular dependency"))
      }
    }
    return Result.success(Unit)
  }
}

