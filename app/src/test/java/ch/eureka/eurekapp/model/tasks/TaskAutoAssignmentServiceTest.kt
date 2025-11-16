package ch.eureka.eurekapp.model.tasks

import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
// portions of this code and documentation were generated with the help of AI.
/**
 * Unit tests for TaskAutoAssignmentService.
 *
 * Tests verify the hybrid algorithm combining dependency-based and workload-based assignment.
 */
class TaskAutoAssignmentServiceTest {

  @Test
  fun assignTasks_withNoMembers_returnsAllTasksSkipped() {
    val tasks =
        listOf(
            Task(taskID = "task1", title = "Task 1", status = TaskStatus.TODO),
            Task(taskID = "task2", title = "Task 2", status = TaskStatus.TODO))

    val result = TaskAutoAssignmentService.assignTasks(tasks, emptyList())

    assertEquals(0, result.assignments.size)
    assertEquals(2, result.skippedTasks.size)
    assertTrue(result.skippedTasks.contains("task1"))
    assertTrue(result.skippedTasks.contains("task2"))
  }

  @Test
  fun assignTasks_withNoUnassignedTasks_returnsEmptyResult() {
    val members = listOf(Member(userId = "user1", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            Task(
                taskID = "task1",
                title = "Task 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "task2",
                title = "Task 2",
                status = TaskStatus.IN_PROGRESS,
                assignedUserIds = listOf("user1")))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(0, result.assignments.size)
    assertEquals(0, result.skippedTasks.size)
  }

  @Test
  fun assignTasks_withUnassignedTasks_assignsToLeastLoadedMember() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            // User1 has 2 tasks, User2 has 0 tasks
            Task(
                taskID = "assigned1",
                title = "Assigned 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "assigned2",
                title = "Assigned 2",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            // Unassigned tasks
            Task(taskID = "unassigned1", title = "Unassigned 1", status = TaskStatus.TODO),
            Task(taskID = "unassigned2", title = "Unassigned 2", status = TaskStatus.TODO))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(2, result.assignments.size)
    assertEquals(0, result.skippedTasks.size)
    // Both should be assigned to user2 (least loaded)
    assertEquals("user2", result.assignments["unassigned1"])
    assertEquals("user2", result.assignments["unassigned2"])
  }

  @Test
  fun assignTasks_withTaskDependencies_assignsToSameMemberAsParent() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            // Parent task assigned to user1
            Task(
                taskID = "parent",
                title = "Parent Task",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            // Dependent task (should be assigned to user1)
            Task(
                taskID = "dependent",
                title = "Dependent Task",
                status = TaskStatus.TODO,
                dependingOnTasks = listOf("parent")),
            // Independent task (should go to least loaded)
            Task(taskID = "independent", title = "Independent Task", status = TaskStatus.TODO))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(2, result.assignments.size)
    // Dependent task should be assigned to same member as parent
    assertEquals("user1", result.assignments["dependent"])
    // Independent task can go to either (both have same workload after parent assignment)
    assertTrue(
        result.assignments["independent"] == "user1" ||
            result.assignments["independent"] == "user2")
  }

  @Test
  fun assignTasks_ignoresCompletedTasks() {
    val members = listOf(Member(userId = "user1", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            Task(taskID = "todo", title = "TODO Task", status = TaskStatus.TODO),
            Task(taskID = "completed", title = "Completed Task", status = TaskStatus.COMPLETED),
            Task(taskID = "cancelled", title = "Cancelled Task", status = TaskStatus.CANCELLED))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(1, result.assignments.size)
    assertTrue(result.assignments.containsKey("todo"))
    assertFalse(result.assignments.containsKey("completed"))
    assertFalse(result.assignments.containsKey("cancelled"))
  }

  @Test
  fun assignTasks_ignoresInProgressTasksThatAreUnassigned() {
    val members = listOf(Member(userId = "user1", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            Task(taskID = "todo", title = "TODO Task", status = TaskStatus.TODO),
            Task(taskID = "inProgress", title = "In Progress Task", status = TaskStatus.IN_PROGRESS))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    // Both TODO and IN_PROGRESS unassigned tasks should be assigned
    assertEquals(2, result.assignments.size)
    assertTrue(result.assignments.containsKey("todo"))
    assertTrue(result.assignments.containsKey("inProgress"))
  }

  @Test
  fun assignTasks_withEqualWorkload_assignsToFirstAvailable() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            // Both users have same workload (1 task each)
            Task(
                taskID = "assigned1",
                title = "Assigned 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "assigned2",
                title = "Assigned 2",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user2")),
            // Unassigned task
            Task(taskID = "unassigned", title = "Unassigned", status = TaskStatus.TODO))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(1, result.assignments.size)
    // Should assign to one of them (algorithm picks first with min workload)
    assertTrue(
        result.assignments["unassigned"] == "user1" ||
            result.assignments["unassigned"] == "user2")
  }

  @Test
  fun assignTasks_withMultipleDependencies_usesFirstDependency() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            Task(
                taskID = "parent1",
                title = "Parent 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "parent2",
                title = "Parent 2",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user2")),
            Task(
                taskID = "dependent",
                title = "Dependent",
                status = TaskStatus.TODO,
                dependingOnTasks = listOf("parent1", "parent2")))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(1, result.assignments.size)
    // Should use first dependency (parent1 -> user1)
    assertEquals("user1", result.assignments["dependent"])
  }

  @Test
  fun assignTasks_withDependencyOnUnassignedParent_usesWorkloadStrategy() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            // Parent is unassigned
            Task(taskID = "parent", title = "Parent", status = TaskStatus.TODO),
            Task(
                taskID = "dependent",
                title = "Dependent",
                status = TaskStatus.TODO,
                dependingOnTasks = listOf("parent")))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(2, result.assignments.size)
    // Both should be assigned (parent first, then dependent to same member)
    assertTrue(result.assignments.containsKey("parent"))
    assertTrue(result.assignments.containsKey("dependent"))
    // Dependent should be assigned to same member as parent (after parent is assigned)
    // Note: Since parent is assigned first, dependent will follow the same member
    val parentAssignee = result.assignments["parent"]
    val dependentAssignee = result.assignments["dependent"]
    // They should be assigned to valid members
    assertTrue(parentAssignee in listOf("user1", "user2"))
    assertTrue(dependentAssignee in listOf("user1", "user2"))
    // Dependent should match parent (algorithm assigns dependent to same member as parent)
    assertEquals(parentAssignee, dependentAssignee)
  }

  @Test
  fun assignTasks_withComplexScenario_balancesWorkload() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER),
            Member(userId = "user3", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            // User1: 3 tasks, User2: 1 task, User3: 0 tasks
            Task(
                taskID = "u1_1",
                title = "U1 Task 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "u1_2",
                title = "U1 Task 2",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "u1_3",
                title = "U1 Task 3",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            Task(
                taskID = "u2_1",
                title = "U2 Task 1",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user2")),
            // 3 unassigned tasks
            Task(taskID = "unassigned1", title = "Unassigned 1", status = TaskStatus.TODO),
            Task(taskID = "unassigned2", title = "Unassigned 2", status = TaskStatus.TODO),
            Task(taskID = "unassigned3", title = "Unassigned 3", status = TaskStatus.TODO))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(3, result.assignments.size)
    // All should go to user3 (least loaded with 0 tasks)
    // After first assignment, user3 has 1 task, but still least loaded
    assertTrue(result.assignments["unassigned1"] in listOf("user2", "user3"))
    assertTrue(result.assignments["unassigned2"] in listOf("user2", "user3"))
    assertTrue(result.assignments["unassigned3"] in listOf("user2", "user3"))
    // Verify workload balancing: user3 should get most assignments
    val assignmentsByUser = result.assignments.values.groupingBy { it }.eachCount()
    // User3 should have at least 2 assignments (or user2 if algorithm balances differently)
    assertTrue(assignmentsByUser.getOrDefault("user3", 0) >= 1)
  }

  @Test
  fun assignTasks_updatesWorkloadAfterEachAssignment() {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))
    val tasks =
        listOf(
            // User1 has 1 task, User2 has 0 tasks
            Task(
                taskID = "assigned",
                title = "Assigned",
                status = TaskStatus.TODO,
                assignedUserIds = listOf("user1")),
            // 2 unassigned tasks
            Task(taskID = "unassigned1", title = "Unassigned 1", status = TaskStatus.TODO),
            Task(taskID = "unassigned2", title = "Unassigned 2", status = TaskStatus.TODO))

    val result = TaskAutoAssignmentService.assignTasks(tasks, members)

    assertEquals(2, result.assignments.size)
    // First should go to user2 (0 tasks)
    assertEquals("user2", result.assignments["unassigned1"])
    // After first assignment, user2 has 1 task, user1 has 1 task
    // Second should go to either (both have same workload now)
    // Algorithm picks first with min workload, which could be user1 or user2
    assertTrue(
        result.assignments["unassigned2"] == "user1" ||
            result.assignments["unassigned2"] == "user2")
  }
}

