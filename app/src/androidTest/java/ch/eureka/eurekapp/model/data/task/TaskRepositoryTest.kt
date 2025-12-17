package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

/**
 * Test suite for TaskRepository implementation.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class TaskRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: TaskRepository
  private val testProjectId = "project_task_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("projects/$testProjectId/tasks")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    val projectRepository: ch.eureka.eurekapp.model.data.project.ProjectRepository =
        ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
    repository =
        FirestoreTaskRepository(
            firestore = FirebaseEmulator.firestore,
            auth = FirebaseEmulator.auth,
            projectRepository = projectRepository)
  }

  @Test
  fun taskRepository_shouldCreateTaskInFirestore() = runBlocking {
    val projectId = "project_task_1"
    setupTestProject(projectId)

    val task =
        Task(
            taskID = "task1",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = listOf(testUserId),
            dueDate = null,
            createdBy = testUserId)

    val result = repository.createTask(task)

    assertTrue(result.isSuccess)
    assertEquals("task1", result.getOrNull())

    val savedTask =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .document("task1")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNotNull(savedTask)
    assertEquals(task.taskID, savedTask?.taskID)
    assertEquals(task.templateId, savedTask?.templateId)
    assertEquals(task.projectId, savedTask?.projectId)
  }

  @Test
  fun taskRepository_shouldReturnTaskWhenExists() = runBlocking {
    val projectId = "project_task_2"
    setupTestProject(projectId)

    val task =
        Task(
            taskID = "task2",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = listOf(testUserId),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task)

    val flow = repository.getTaskById(projectId, "task2")
    val retrievedTask = flow.first()

    assertNotNull(retrievedTask)
    assertEquals(task.taskID, retrievedTask?.taskID)
    assertEquals(task.templateId, retrievedTask?.templateId)
  }

  @Test
  fun taskRepository_shouldReturnNullWhenTaskDoesNotExist() = runBlocking {
    val projectId = "project_task_3"
    setupTestProject(projectId)

    val flow = repository.getTaskById(projectId, "non_existent_task")
    val retrievedTask = flow.first()

    assertNull(retrievedTask)
  }

  @Test
  fun taskRepository_shouldReturnAllTasksInProject() = runBlocking {
    val projectId = "project_task_4"
    setupTestProject(projectId)

    val task1 =
        Task(
            taskID = "task3",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = listOf(testUserId),
            dueDate = null,
            createdBy = testUserId)
    val task2 =
        Task(
            taskID = "task4",
            templateId = "template2",
            projectId = projectId,
            assignedUserIds = emptyList(),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task1)
    repository.createTask(task2)

    val flow = repository.getTasksInProject(projectId)
    val tasks = flow.first()

    assertEquals(2, tasks.size)
    assertTrue(tasks.any { it.taskID == "task3" })
    assertTrue(tasks.any { it.taskID == "task4" })
  }

  @Test
  fun taskRepository_shouldReturnEmptyListWhenNoTasks() = runBlocking {
    val projectId = "project_task_5"
    setupTestProject(projectId)

    val flow = repository.getTasksInProject(projectId)
    val tasks = flow.first()

    assertTrue(tasks.isEmpty())
  }

  @Test
  fun taskRepository_shouldReturnAllTasksInMemberProjects() = runBlocking {
    val projectId = "project_task_6"
    setupTestProject(projectId)

    val task1 =
        Task(
            taskID = "task5",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = listOf(testUserId),
            dueDate = null,
            createdBy = testUserId)
    val task2 =
        Task(
            taskID = "task6",
            templateId = "template2",
            projectId = projectId,
            assignedUserIds = listOf("otherUser"),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task1)
    repository.createTask(task2)

    val flow = repository.getTasksForCurrentUser()
    val tasks = flow.first()

    // getTasksForCurrentUser now returns all tasks from projects where user is a member
    assertEquals(2, tasks.size)
    assertTrue(tasks.any { it.taskID == "task5" && it.assignedUserIds.contains(testUserId) })
    assertTrue(tasks.any { it.taskID == "task6" && !it.assignedUserIds.contains(testUserId) })
  }
  /*
  @Test
  fun getTasksForCurrentUser_shouldReturnEmptyList_WhenNoTasksAssigned() = runBlocking {
    // TODO: Fix test isolation issue - collection group queries see cached data from previous tests
    // This test passes when run alone but fails when run after
    // getTasksForCurrentUser_shouldReturnTasksAssignedToCurrentUser
    // due to eventual consistency in Firestore collection group queries
    val projectId = "project_task_7"
    setupTestProject(projectId)

    // Add delay to ensure collection group query sees cleared state
    delay(1000)

    val flow = repository.getTasksForCurrentUser()
    val tasks = flow.first()
    assertTrue(tasks.isEmpty())
  }
  */

  @Test
  fun taskRepository_shouldUpdateTaskDetails() = runBlocking {
    val projectId = "project_task_8"
    setupTestProject(projectId)

    val task =
        Task(
            taskID = "task9",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = listOf(testUserId),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task)

    val updatedTask =
        task.copy(assignedUserIds = listOf(testUserId, "newUser"), dueDate = Timestamp.now())
    val result = repository.updateTask(updatedTask)

    assertTrue(result.isSuccess)

    val savedTask =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .document("task9")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNotNull(savedTask)
    assertEquals(2, savedTask?.assignedUserIds?.size)
    assertTrue(savedTask?.assignedUserIds?.contains("newUser") == true)
    assertNotNull(savedTask?.dueDate)
  }

  @Test
  fun taskRepository_shouldDeleteTaskFromFirestore() = runBlocking {
    val projectId = "project_task_9"
    setupTestProject(projectId)

    val task =
        Task(
            taskID = "task10",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = emptyList(),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task)

    val result = repository.deleteTask(projectId, "task10")

    assertTrue(result.isSuccess)

    val deletedTask =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .document("task10")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNull(deletedTask)
  }

  @Test
  fun taskRepository_shouldAddUserToTaskAssignedUserIds() = runBlocking {
    val projectId = "project_task_10"
    setupTestProject(projectId)

    val task =
        Task(
            taskID = "task11",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = emptyList(),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task)

    val result = repository.assignUser(projectId, "task11", testUserId)

    assertTrue(result.isSuccess)

    val updatedTask =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .document("task11")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNotNull(updatedTask)
    assertTrue(updatedTask?.assignedUserIds?.contains(testUserId) == true)
  }

  @Test
  fun taskRepository_shouldRemoveUserFromTaskAssignedUserIds() = runBlocking {
    val projectId = "project_task_11"
    setupTestProject(projectId)

    val task =
        Task(
            taskID = "task12",
            templateId = "template1",
            projectId = projectId,
            assignedUserIds = listOf(testUserId, "otherUser"),
            dueDate = null,
            createdBy = testUserId)
    repository.createTask(task)

    val result = repository.unassignUser(projectId, "task12", testUserId)

    assertTrue(result.isSuccess)

    val updatedTask =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("tasks")
            .document("task12")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNotNull(updatedTask)
    assertTrue(updatedTask?.assignedUserIds?.contains(testUserId) == false)
    assertTrue(updatedTask?.assignedUserIds?.contains("otherUser") == true)
  }
}
