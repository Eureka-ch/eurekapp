/**
 * Unit tests for TaskViewModel
 *
 * Tests ViewModel logic including state management, filter handling, and task operations.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Unit tests for TaskViewModel Tests the actual business logic and state management */
@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

  private lateinit var viewModel: TaskViewModel
  private lateinit var mockRepository: MockTaskRepository
  private val testDispatcher = StandardTestDispatcher()

  @Before
  fun setup() {
    Dispatchers.setMain(testDispatcher)
    mockRepository = MockTaskRepository()
    viewModel = TaskViewModel(mockRepository)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun `setFilter updates selectedFilter correctly`() = runTest {
    // Given
    val initialFilter = viewModel.uiState.first().selectedFilter
    assertEquals(TaskFilter.MINE, initialFilter)

    // When
    viewModel.setFilter(TaskFilter.ALL)
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val updatedState = viewModel.uiState.first()
    assertEquals(TaskFilter.ALL, updatedState.selectedFilter)
  }

  @Test
  fun `setFilter updates all filter types correctly`() = runTest {
    val filters =
        listOf(
            TaskFilter.MINE,
            TaskFilter.TEAM,
            TaskFilter.THIS_WEEK,
            TaskFilter.ALL,
            TaskFilter.PROJECT)

    filters.forEach { filter ->
      // When
      viewModel.setFilter(filter)
      testDispatcher.scheduler.advanceUntilIdle()

      // Then
      val state = viewModel.uiState.first()
      assertEquals(filter, state.selectedFilter)
    }
  }

  @Test
  fun `toggleTaskCompletion with empty taskId sets error`() = runTest {
    // When
    viewModel.toggleTaskCompletion("")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Task ID cannot be empty", state.error)
  }

  @Test
  fun `toggleTaskCompletion with blank taskId sets error`() = runTest {
    // When
    viewModel.toggleTaskCompletion("   ")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Task ID cannot be empty", state.error)
  }

  @Test
  fun `toggleTaskCompletion with non-existent taskId sets error`() = runTest {
    // When
    viewModel.toggleTaskCompletion("non-existent-task")
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertEquals("Task not found", state.error)
  }

  @Test
  fun `loadTasks sets loading state correctly`() = runTest {
    // Given
    val initialState = viewModel.uiState.first()
    assertFalse(initialState.isLoading)

    // When
    viewModel.loadTasks()
    testDispatcher.scheduler.advanceUntilIdle()

    // Then
    val state = viewModel.uiState.first()
    assertFalse(state.isLoading) // Should be false after loading completes
    assertTrue(state.rawTasks.isEmpty()) // Mock returns empty list
  }

  @Test
  fun `taskTitles computed property returns correct titles`() = runTest {
    // Given - Mock repository returns empty list by default
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    val titles = viewModel.taskTitles.first()

    // Then
    assertTrue(titles.isEmpty())
  }

  @Test
  fun `isCompletedList computed property returns correct completion status`() = runTest {
    // Given - Mock repository returns empty list by default
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    val completionStatus = viewModel.isCompletedList.first()

    // Then
    assertTrue(completionStatus.isEmpty())
  }

  @Test
  fun `rawTasks computed property returns correct tasks`() = runTest {
    // Given - Mock repository returns empty list by default
    testDispatcher.scheduler.advanceUntilIdle()

    // When
    val tasks = viewModel.rawTasks.first()

    // Then
    assertTrue(tasks.isEmpty())
  }

  @Test
  fun `initial state has correct default values`() = runTest {
    // When
    val state = viewModel.uiState.first()

    // Then
    assertEquals(TaskFilter.MINE, state.selectedFilter)
    assertTrue(state.rawTasks.isEmpty())
    assertFalse(state.isLoading)
    assertEquals(null, state.error)
    assertEquals(null, state.projectId)
  }
}
