package ch.eureka.eurekapp.model.tasks

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus

// Part of this code and documentation were generated with the help of AI.
/**
 * Service for automatically assigning tasks to team members.
 *
 * Uses a hybrid algorithm that combines:
 * 1. Dependency-based assignment: Tasks with dependencies are assigned to the same member as their
 *    parent task when possible
 * 2. Workload-based assignment: When no dependency preference exists, assigns to the member with
 *    the least current workload
 *
 * Note: This file was co-authored by Claude Code.
 */
object TaskAutoAssignmentService {

  /**
   * Result of the auto-assignment operation.
   *
   * @property assignments Map of task IDs to assigned user IDs
   * @property skippedTasks List of task IDs that couldn't be assigned (e.g., no available members)
   */
  data class AssignmentResult(
      val assignments: Map<String, String>, // taskId -> userId
      val skippedTasks: List<String> = emptyList()
  )

  /**
   * Automatically assigns unassigned tasks to team members using a hybrid algorithm.
   *
   * Algorithm:
   * 1. Calculate workload for each member
   * 2. For each unassigned task: a. If task has dependencies, try to assign to the same member as
   *    the parent task b. If no dependency preference or parent not assigned, assign to least
   *    loaded member
   * 3. Skip tasks that cannot be assigned (e.g., no members available)
   *
   * @param tasks All tasks in the projects (assigned and unassigned)
   * @param members List of project members available for assignment
   * @return AssignmentResult containing the assignments and any skipped tasks
   */
  fun assignTasks(tasks: List<Task>, members: List<Member>): AssignmentResult {
    if (members.isEmpty()) {
      return AssignmentResult(emptyMap(), tasks.map { it.taskID })
    }

    // Filter to only unassigned tasks
    val unassignedTasks =
        tasks.filter { task ->
          task.assignedUserIds.isEmpty() &&
              (task.status == TaskStatus.TODO || task.status == TaskStatus.IN_PROGRESS)
        }

    if (unassignedTasks.isEmpty()) {
      return AssignmentResult(emptyMap())
    }

    // Calculate workload for each member
    val workloadMap = calculateWorkload(tasks, members)

    // Create a map of task ID to assigned user ID for quick lookup
    val taskToAssigneeMap = mutableMapOf<String, String>()

    // Sort tasks by priority: tasks WITHOUT dependencies first (so parents are assigned before
    // dependents)
    val sortedTasks = unassignedTasks.sortedBy { task -> task.dependingOnTasks.isNotEmpty() }

    val skippedTasks = mutableListOf<String>()

    for (task in sortedTasks) {
      val assignedUserId = selectAssignee(task, tasks, workloadMap, members, taskToAssigneeMap)

      if (assignedUserId != null) {
        taskToAssigneeMap[task.taskID] = assignedUserId
        // Update workload for the assigned member
        workloadMap[assignedUserId] = (workloadMap[assignedUserId] ?: 0) + 1
      } else {
        skippedTasks.add(task.taskID)
      }
    }

    return AssignmentResult(taskToAssigneeMap.toMap(), skippedTasks)
  }

  /**
   * Calculates the current workload for each member.
   *
   * Workload is defined as the count of tasks with "TODO_" or "IN_PROGRESS" status assigned to the
   * member.
   *
   * @param tasks All tasks in the project
   * @param members List of project members
   * @return Map of userId to workload count
   */
  private fun calculateWorkload(tasks: List<Task>, members: List<Member>): MutableMap<String, Int> {
    val workloadMap = mutableMapOf<String, Int>()

    // Initialize all members with 0 workload
    members.forEach { member -> workloadMap[member.userId] = 0 }

    // Count active tasks for each member
    tasks
        .filter { it.status == TaskStatus.TODO || it.status == TaskStatus.IN_PROGRESS }
        .forEach { task ->
          task.assignedUserIds.forEach { userId ->
            if (workloadMap.containsKey(userId)) {
              workloadMap[userId] = (workloadMap[userId] ?: 0) + 1
            }
          }
        }

    return workloadMap
  }

  /**
   * Selects the best assignee for a task using the hybrid algorithm.
   *
   * Priority:
   * 1. If task has dependencies, try to assign to the same member as the parent task
   * 2. Otherwise, assign to the member with the least workload
   *
   * @param task The task to assign
   * @param allTasks All tasks in the project (for looking up parent tasks)
   * @param workloadMap Current workload for each member
   * @param members Available members for assignment
   * @param taskToAssigneeMap Map of already assigned tasks (to check parent assignments)
   * @return User ID of the selected assignee, or null if no suitable assignee found
   */
  private fun selectAssignee(
      task: Task,
      allTasks: List<Task>,
      workloadMap: Map<String, Int>,
      members: List<Member>,
      taskToAssigneeMap: Map<String, String>
  ): String? {
    // Strategy 1: If task has dependencies, try to assign to same member as parent
    if (task.dependingOnTasks.isNotEmpty()) {
      val parentTaskId = task.dependingOnTasks.first()

      // Check if parent was already assigned in this run
      val parentAssigneeFromMap = taskToAssigneeMap[parentTaskId]
      if (parentAssigneeFromMap != null && members.any { it.userId == parentAssigneeFromMap }) {
        return parentAssigneeFromMap
      }

      // Fallback: check if parent was already assigned before (in original task data)
      val parentTask = allTasks.find { it.taskID == parentTaskId }
      if (parentTask != null &&
          parentTask.assignedUserIds.isNotEmpty() &&
          members.any { it.userId == parentTask.assignedUserIds.first() }) {
        return parentTask.assignedUserIds.first()
      }
    }

    // Strategy 2: Assign to member with least workload
    val availableMembers = members.map { it.userId }.filter { workloadMap.containsKey(it) }
    if (availableMembers.isEmpty()) {
      return null
    }

    // Find member with minimum workload
    val minWorkload = availableMembers.minOfOrNull { workloadMap[it] ?: 0 }
    if (minWorkload == null) {
      return availableMembers.firstOrNull()
    }

    // If multiple members have the same minimum workload, pick the first one
    return availableMembers.firstOrNull { (workloadMap[it] ?: 0) == minWorkload }
  }
}
