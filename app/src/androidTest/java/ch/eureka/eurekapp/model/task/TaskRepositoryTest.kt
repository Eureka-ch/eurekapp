package ch.eureka.eurekapp.model.task

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

class TaskRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: TaskRepository
  private val testWorkspaceId = "workspace_task_test"
  private val testGroupId = "group_task_test"
  private val testProjectId = "project_task_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces/$testWorkspaceId/groups/$testGroupId/projects/$testProjectId/tasks")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreTaskRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun createTask_shouldCreateTaskInFirestore() = runBlocking {
    val task =
        Task(
            taskID = "task1",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId),
            dueDate = null)

    val result = repository.createTask(task)

    assertTrue(result.isSuccess)
    assertEquals("task1", result.getOrNull())

    val savedTask =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document(testProjectId)
            .collection("tasks")
            .document("task1")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNotNull(savedTask)
    assertEquals(task.taskID, savedTask?.taskID)
    assertEquals(task.templateId, savedTask?.templateId)
    assertEquals(task.projectId, savedTask?.projectId)
    assertEquals(task.groupId, savedTask?.groupId)
    assertEquals(task.workspaceId, savedTask?.workspaceId)
  }

  @Test
  fun getTaskById_shouldReturnTaskWhenExists() = runBlocking {
    val task =
        Task(
            taskID = "task2",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId),
            dueDate = null)
    repository.createTask(task)

    val flow = repository.getTaskById(testWorkspaceId, testGroupId, testProjectId, "task2")
    val retrievedTask = flow.first()

    assertNotNull(retrievedTask)
    assertEquals(task.taskID, retrievedTask?.taskID)
    assertEquals(task.templateId, retrievedTask?.templateId)
  }

  @Test
  fun getTaskById_shouldReturnNullWhenTaskDoesNotExist() = runBlocking {
    val flow =
        repository.getTaskById(testWorkspaceId, testGroupId, testProjectId, "non_existent_task")
    val retrievedTask = flow.first()

    assertNull(retrievedTask)
  }

  @Test
  fun getTasksInProject_shouldReturnAllTasksInProject() = runBlocking {
    val task1 =
        Task(
            taskID = "task3",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId),
            dueDate = null)
    val task2 =
        Task(
            taskID = "task4",
            templateId = "template2",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = emptyList(),
            dueDate = null)
    repository.createTask(task1)
    repository.createTask(task2)

    val flow = repository.getTasksInProject(testWorkspaceId, testGroupId, testProjectId)
    val tasks = flow.first()

    assertEquals(2, tasks.size)
    assertTrue(tasks.any { it.taskID == "task3" })
    assertTrue(tasks.any { it.taskID == "task4" })
  }

  @Test
  fun getTasksInProject_shouldReturnEmptyListWhenNoTasks() = runBlocking {
    val flow = repository.getTasksInProject(testWorkspaceId, testGroupId, testProjectId)
    val tasks = flow.first()

    assertTrue(tasks.isEmpty())
  }

  @Test
  fun getTasksForCurrentUser_shouldReturnTasksAssignedToCurrentUser() = runBlocking {
    val task1 =
        Task(
            taskID = "task5",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId),
            dueDate = null)
    val task2 =
        Task(
            taskID = "task6",
            templateId = "template2",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf("otherUser"),
            dueDate = null)
    repository.createTask(task1)
    repository.createTask(task2)

    val flow = repository.getTasksForCurrentUser()
    val tasks = flow.first()

    assertEquals(1, tasks.size)
    assertEquals("task5", tasks[0].taskID)
    assertTrue(tasks[0].assignedUserIds.contains(testUserId))
  }

  @Test
  fun getTasksForCurrentUser_shouldReturnEmptyListWhenNoTasksAssigned() = runBlocking {
    val flow = repository.getTasksForCurrentUser()
    val tasks = flow.first()

    assertTrue(tasks.isEmpty())
  }

  @Test
  fun getTasksForCurrentUserInWorkspace_shouldReturnTasksInSpecificWorkspace() = runBlocking {
    val task1 =
        Task(
            taskID = "task7",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId),
            dueDate = null)
    val task2 =
        Task(
            taskID = "task8",
            templateId = "template2",
            projectId = "project2",
            groupId = "group2",
            workspaceId = "workspace2",
            assignedUserIds = listOf(testUserId),
            dueDate = null)
    repository.createTask(task1)
    repository.createTask(task2)

    val flow = repository.getTasksForCurrentUserInWorkspace(testWorkspaceId)
    val tasks = flow.first()

    assertEquals(1, tasks.size)
    assertEquals("task7", tasks[0].taskID)
    assertEquals(testWorkspaceId, tasks[0].workspaceId)
  }

  @Test
  fun updateTask_shouldUpdateTaskDetails() = runBlocking {
    val task =
        Task(
            taskID = "task9",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId),
            dueDate = null)
    repository.createTask(task)

    val updatedTask =
        task.copy(assignedUserIds = listOf(testUserId, "newUser"), dueDate = Timestamp.now())
    val result = repository.updateTask(updatedTask)

    assertTrue(result.isSuccess)

    val savedTask =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document(testProjectId)
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
  fun deleteTask_shouldDeleteTaskFromFirestore() = runBlocking {
    val task =
        Task(
            taskID = "task10",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = emptyList(),
            dueDate = null)
    repository.createTask(task)

    val result = repository.deleteTask(testWorkspaceId, testGroupId, testProjectId, "task10")

    assertTrue(result.isSuccess)

    val deletedTask =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document(testProjectId)
            .collection("tasks")
            .document("task10")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNull(deletedTask)
  }

  @Test
  fun assignUser_shouldAddUserToTaskAssignedUserIds() = runBlocking {
    val task =
        Task(
            taskID = "task11",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = emptyList(),
            dueDate = null)
    repository.createTask(task)

    val result =
        repository.assignUser(testWorkspaceId, testGroupId, testProjectId, "task11", testUserId)

    assertTrue(result.isSuccess)

    val updatedTask =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document(testProjectId)
            .collection("tasks")
            .document("task11")
            .get()
            .await()
            .toObject(Task::class.java)

    assertNotNull(updatedTask)
    assertTrue(updatedTask?.assignedUserIds?.contains(testUserId) == true)
  }

  @Test
  fun unassignUser_shouldRemoveUserFromTaskAssignedUserIds() = runBlocking {
    val task =
        Task(
            taskID = "task12",
            templateId = "template1",
            projectId = testProjectId,
            groupId = testGroupId,
            workspaceId = testWorkspaceId,
            assignedUserIds = listOf(testUserId, "otherUser"),
            dueDate = null)
    repository.createTask(task)

    val result =
        repository.unassignUser(testWorkspaceId, testGroupId, testProjectId, "task12", testUserId)

    assertTrue(result.isSuccess)

    val updatedTask =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("groups")
            .document(testGroupId)
            .collection("projects")
            .document(testProjectId)
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
