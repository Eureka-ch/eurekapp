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
*/

@OptIn(ExperimentalCoroutinesApi::class)
class ViewTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: ch.eureka.eurekapp.model.data.task.TaskRepository
  private lateinit var viewModel: ViewTaskViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = mockk()
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
            assignedUserIds = listOf("user1"),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("url1", "url2"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)

    viewModel = ViewTaskViewModel(projectId, taskId, mockTaskRepository, testDispatcher)
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
  }

  @Test
  fun viewModelWithNullTaskEmitsErrorState() = runTest {
    val projectId = "project123"
    val taskId = "task123"

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(null)

    viewModel = ViewTaskViewModel(projectId, taskId, mockTaskRepository, testDispatcher)
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

    viewModel = ViewTaskViewModel(projectId, taskId, mockTaskRepository, testDispatcher)
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
            assignedUserIds = listOf("user1"),
            dueDate = null,
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)

    viewModel = ViewTaskViewModel(projectId, taskId, mockTaskRepository, testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.dueDate)
  }
}
