package ch.eureka.eurekapp.utils

// Portions of this code were generated with the help of AI.

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TaskDependencyCycleDetector.
 *
 * Tests cycle detection logic using DFS (Depth-First Search) algorithm.
 */
class TaskDependencyCycleDetectorTest {

  private class MockTaskRepository : TaskRepository {
    private val tasks = mutableMapOf<String, Task>()

    fun addTask(task: Task) {
      tasks[task.taskID] = task
    }

    override fun getTaskById(projectId: String, taskId: String): Flow<Task?> {
      return flowOf(tasks[taskId])
    }

    override fun getTasksInProject(projectId: String): Flow<List<Task>> = flowOf(emptyList())

    override fun getTasksForCurrentUser(): Flow<List<Task>> = flowOf(emptyList())

    override suspend fun createTask(task: Task): Result<String> = Result.success(task.taskID)

    override suspend fun updateTask(task: Task): Result<Unit> = Result.success(Unit)

    override suspend fun deleteTask(projectId: String, taskId: String): Result<Unit> =
        Result.success(Unit)

    override suspend fun assignUser(
        projectId: String,
        taskId: String,
        userId: String
    ): Result<Unit> = Result.success(Unit)

    override suspend fun unassignUser(
        projectId: String,
        taskId: String,
        userId: String
    ): Result<Unit> = Result.success(Unit)
  }

  @Test
  fun wouldCreateCycle_taskDependsOnItself_returnsTrue() = runTest {
    val repository = MockTaskRepository()
    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "task1",
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result)
  }

  @Test
  fun wouldCreateCycle_directCycle_returnsTrue() = runTest {
    val repository = MockTaskRepository()
    // task2 depends on task1
    repository.addTask(
        Task(taskID = "task2", projectId = "project1", dependingOnTasks = listOf("task1")))

    // Trying to make task1 depend on task2 creates a cycle
    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "task2",
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result)
  }

  @Test
  fun wouldCreateCycle_indirectCycle_returnsTrue() = runTest {
    val repository = MockTaskRepository()
    // task2 -> task3 -> task1 (indirect cycle)
    repository.addTask(
        Task(taskID = "task2", projectId = "project1", dependingOnTasks = listOf("task3")))
    repository.addTask(
        Task(taskID = "task3", projectId = "project1", dependingOnTasks = listOf("task1")))

    // Trying to make task1 depend on task2 creates a cycle
    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "task2",
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result)
  }

  @Test
  fun wouldCreateCycle_noCycle_returnsFalse() = runTest {
    val repository = MockTaskRepository()
    // task2 depends on task3 (no cycle with task1)
    repository.addTask(
        Task(taskID = "task2", projectId = "project1", dependingOnTasks = listOf("task3")))
    repository.addTask(Task(taskID = "task3", projectId = "project1"))

    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "task2",
            projectId = "project1",
            taskRepository = repository)
    assertFalse(result)
  }

  @Test
  fun wouldCreateCycle_dependencyDoesNotExist_returnsFalse() = runTest {
    val repository = MockTaskRepository()
    // Dependency task doesn't exist
    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "nonexistent",
            projectId = "project1",
            taskRepository = repository)
    assertFalse(result)
  }

  @Test
  fun wouldCreateCycle_multipleDependenciesNoCycle_returnsFalse() = runTest {
    val repository = MockTaskRepository()
    // task2 depends on task3 and task4
    repository.addTask(
        Task(taskID = "task2", projectId = "project1", dependingOnTasks = listOf("task3", "task4")))
    repository.addTask(Task(taskID = "task3", projectId = "project1"))
    repository.addTask(Task(taskID = "task4", projectId = "project1"))

    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "task2",
            projectId = "project1",
            taskRepository = repository)
    assertFalse(result)
  }

  @Test
  fun validateNoCycles_validDependencies_returnsSuccess() = runTest {
    val repository = MockTaskRepository()
    repository.addTask(Task(taskID = "task2", projectId = "project1"))
    repository.addTask(Task(taskID = "task3", projectId = "project1"))

    val result =
        TaskDependencyCycleDetector.validateNoCycles(
            taskId = "task1",
            dependencyTaskIds = listOf("task2", "task3"),
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result.isSuccess)
  }

  @Test
  fun validateNoCycles_cycleDetected_returnsFailure() = runTest {
    val repository = MockTaskRepository()
    // task2 depends on task1
    repository.addTask(
        Task(taskID = "task2", projectId = "project1", dependingOnTasks = listOf("task1")))

    val result =
        TaskDependencyCycleDetector.validateNoCycles(
            taskId = "task1",
            dependencyTaskIds = listOf("task2"),
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result.isFailure)
    assertTrue(
        result.exceptionOrNull()?.message?.contains("circular dependency") == true ||
            result.exceptionOrNull()?.message?.contains("task2") == true)
  }

  @Test
  fun validateNoCycles_emptyDependencies_returnsSuccess() = runTest {
    val repository = MockTaskRepository()
    val result =
        TaskDependencyCycleDetector.validateNoCycles(
            taskId = "task1",
            dependencyTaskIds = emptyList(),
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result.isSuccess)
  }

  @Test
  fun validateNoCycles_selfDependency_returnsFailure() = runTest {
    val repository = MockTaskRepository()
    val result =
        TaskDependencyCycleDetector.validateNoCycles(
            taskId = "task1",
            dependencyTaskIds = listOf("task1"),
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result.isFailure)
  }

  @Test
  fun wouldCreateCycle_complexCycle_returnsTrue() = runTest {
    val repository = MockTaskRepository()
    // Complex cycle: task2 -> task3 -> task4 -> task2
    // But we need task4 to depend on task1 to create a cycle when task1 depends on task2
    repository.addTask(
        Task(taskID = "task2", projectId = "project1", dependingOnTasks = listOf("task3")))
    repository.addTask(
        Task(taskID = "task3", projectId = "project1", dependingOnTasks = listOf("task4")))
    repository.addTask(
        Task(taskID = "task4", projectId = "project1", dependingOnTasks = listOf("task1")))

    // Trying to make task1 depend on task2 creates a cycle: task1 -> task2 -> task3 -> task4 ->
    // task1
    val result =
        TaskDependencyCycleDetector.wouldCreateCycle(
            taskId = "task1",
            dependencyTaskId = "task2",
            projectId = "project1",
            taskRepository = repository)
    assertTrue(result)
  }
}
