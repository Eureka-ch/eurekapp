package ch.eureka.eurekapp.model.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
Portions of this code were generated with the help of Grok.
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

@OptIn(ExperimentalCoroutinesApi::class)
class ViewTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: ch.eureka.eurekapp.model.data.task.TaskRepository
  private lateinit var mockUserRepository: ch.eureka.eurekapp.model.data.user.UserRepository
  private lateinit var viewModel: ViewTaskViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = mockk()
    mockUserRepository = mockk()
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun viewModelWithValidTaskEmitsCorrectState() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("url1", "url2"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Test Task", state.title)
    assertEquals("Test Description", state.description)
    assertEquals(projectId, state.projectId)
    assertEquals(taskId, state.taskId)
    assertEquals(listOf("url1", "url2"), state.attachmentUrls)
    assertEquals(TaskStatus.TODO, state.status)
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
    assertEquals(emptyList<ch.eureka.eurekapp.model.data.user.User>(), state.assignedUsers)
  }

  @Test
  fun viewModelWithNullTaskEmitsErrorState() = runTest {
    val projectId = "project123"
    val taskId = "task123"

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(null)

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.dueDate)
    assertEquals("", state.projectId)
    assertEquals("", state.taskId)
    assertTrue(state.attachmentUrls.isEmpty())
    assertFalse(state.isLoading)
    assertEquals("Task not found.", state.errorMsg)
  }

  @Test
  fun viewModelWithExceptionEmitsErrorState() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val exception = Exception("Network error")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flow { throw exception }

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.dueDate)
    assertEquals("", state.projectId)
    assertEquals("", state.taskId)
    assertTrue(state.attachmentUrls.isEmpty())
    assertFalse(state.isLoading)
    assertEquals("Failed to load Task: Network error", state.errorMsg)
  }

  // This test checks that when a task has no due date, the ViewModel emits an empty string for
  // dueDate.
  // It seems to me like the cleanest solution for this case.
  // The main goal of this test is to ensure that the ViewModel won't crash or misbehave.
  @Test
  fun viewModelWithTaskWithoutDueDateEmitsEmptyDueDate() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = null,
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.dueDate)
  }

  @Test
  fun viewModelWithSingleAssignedUserLoadsUserCorrectly() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val userId = "user1"
    val user =
        ch.eureka.eurekapp.model.data.user.User(
            uid = userId, email = "user1@example.com", displayName = "User One")

    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = listOf(userId),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockUserRepository.getUserById(userId) } returns flowOf(user)

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.assignedUsers.size)
    assertEquals(userId, state.assignedUsers[0].uid)
    assertEquals("User One", state.assignedUsers[0].displayName)
    assertEquals("user1@example.com", state.assignedUsers[0].email)
  }

  @Test
  fun viewModelWithMultipleAssignedUsersLoadsAllUsersCorrectly() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val user1Id = "user1"
    val user2Id = "user2"
    val user3Id = "user3"

    val user1 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user1Id, email = "user1@example.com", displayName = "User One")
    val user2 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user2Id, email = "user2@example.com", displayName = "User Two")
    val user3 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user3Id, email = "user3@example.com", displayName = "User Three")

    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = listOf(user1Id, user2Id, user3Id),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockUserRepository.getUserById(user1Id) } returns flowOf(user1)
    every { mockUserRepository.getUserById(user2Id) } returns flowOf(user2)
    every { mockUserRepository.getUserById(user3Id) } returns flowOf(user3)

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(3, state.assignedUsers.size)
    assertEquals("User One", state.assignedUsers[0].displayName)
    assertEquals("User Two", state.assignedUsers[1].displayName)
    assertEquals("User Three", state.assignedUsers[2].displayName)
  }

  @Test
  fun viewModelWithNullUserFiltersOutNulls() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val user1Id = "user1"
    val user2Id = "user2"

    val user1 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user1Id, email = "user1@example.com", displayName = "User One")

    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = listOf(user1Id, user2Id),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockUserRepository.getUserById(user1Id) } returns flowOf(user1)
    every { mockUserRepository.getUserById(user2Id) } returns flowOf(null)

    viewModel =
        ViewTaskViewModel(projectId, taskId, mockTaskRepository, mockUserRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.assignedUsers.size)
    assertEquals("User One", state.assignedUsers[0].displayName)
  }
}
