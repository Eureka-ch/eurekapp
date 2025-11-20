package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.tasks.TaskAutoAssignmentService
import kotlinx.coroutines.flow.first

// portions of this code and documentation were generated with the help of AI.
/**
 * Responsible for the business logic of preparing the auto-assignment proposal: loading projects,
 * members, tasks and building the [ProposedAssignment] list.
 */
class AutoAssignResultLoader(
    private val taskRepository: TaskRepository,
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository
) {

  sealed class Result {
    data class Success(val proposedAssignments: List<ProposedAssignment>) : Result()

    sealed class Error : Result() {
      object NoProjects : Error()

      object NoMembers : Error()

      data class NoAssignableTasks(val skippedTasks: Int) : Error()

      data class Generic(val throwable: Throwable) : Error()
    }
  }

  suspend fun load(): Result {
    return try {
      val projects = projectRepository.getProjectsForCurrentUser().first()
      if (projects.isEmpty()) return Result.Error.NoProjects

      val (allTasks, projectTasksMap) = collectTasksFromProjects(projects)
      val uniqueMembers = collectUniqueMembers(projects)
      if (uniqueMembers.isEmpty()) return Result.Error.NoMembers

      val assignmentResult = TaskAutoAssignmentService.assignTasks(allTasks, uniqueMembers)
      if (assignmentResult.assignments.isEmpty()) {
        return Result.Error.NoAssignableTasks(assignmentResult.skippedTasks.size)
      }

      val proposedAssignments =
          createProposedAssignments(
              assignments = assignmentResult.assignments,
              projects = projects,
              projectTasksMap = projectTasksMap,
              allTasks = allTasks)

      Result.Success(proposedAssignments)
    } catch (t: Throwable) {
      Result.Error.Generic(t)
    }
  }

  private suspend fun collectTasksFromProjects(
      projects: List<Project>
  ): Pair<List<Task>, Map<String, List<Task>>> {
    val allTasks = mutableListOf<Task>()
    val projectTasksMap = mutableMapOf<String, List<Task>>()

    for (project in projects) {
      val tasks = taskRepository.getTasksInProject(project.projectId).first()
      allTasks.addAll(tasks)
      projectTasksMap[project.projectId] = tasks
    }

    return allTasks to projectTasksMap
  }

  private suspend fun collectUniqueMembers(projects: List<Project>): List<Member> {
    val allMembers = mutableListOf<Member>()
    for (project in projects) {
      val members = projectRepository.getMembers(project.projectId).first()
      allMembers.addAll(members)
    }
    return allMembers.distinctBy { it.userId }
  }

  private suspend fun createProposedAssignments(
      assignments: Map<String, String>,
      projects: List<Project>,
      projectTasksMap: Map<String, List<Task>>,
      allTasks: List<Task>
  ): List<ProposedAssignment> {
    val proposedAssignments = mutableListOf<ProposedAssignment>()

    for ((taskId, userId) in assignments) {
      val taskProject =
          projects.find { project ->
            projectTasksMap[project.projectId]?.any { it.taskID == taskId } == true
          }

      if (taskProject != null) {
        val task = allTasks.find { it.taskID == taskId }
        val user = userRepository.getUserById(userId).first()

        if (task != null && user != null) {
          proposedAssignments.add(
              ProposedAssignment(
                  task = task, proposedAssignee = user, projectId = taskProject.projectId))
        }
      }
    }

    return proposedAssignments
  }
}
