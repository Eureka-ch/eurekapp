// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.screens.TaskAndUsers
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.getFilterTag
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Error management and edge case tests for TasksScreen
 *
 * Tests error states, loading states, and error recovery scenarios using MockTaskScreenViewModel
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class TasksScreenErrorManagementTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockViewModel: MockTaskScreenViewModel

  private val testUser1 = User(uid = "user1", displayName = "Alice Smith", email = "alice@test.com")

  @Before
  fun setUp() {
    mockTaskRepository = MockTaskRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()

    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    mockViewModel =
        MockTaskScreenViewModel(
            projectRepository = mockProjectRepository,
            taskRepository = mockTaskRepository,
            userRepository = mockUserRepository,
            currentUserId = "user1")
  }

  @Test
  fun tasksScreen_withLoadingStateDisplaysLoadingIndicator() {
    // Set loading state
    mockViewModel.setLoading(true)

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    // Verify loading indicator is displayed
    composeTestRule.onNodeWithTag(TasksScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
    composeTestRule.onNodeWithText("Loading tasks...").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_withErrorStateDisplaysErrorMessage() {
    // Set error state
    mockViewModel.setError("Failed to load tasks from server")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    // Verify error message is displayed
    composeTestRule.onNodeWithTag(TasksScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Error: Failed to load tasks from server").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_errorStateHidesTaskList() {
    // Set error state with some tasks
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))
    mockViewModel.setError("Network error")
    mockViewModel.setTasksAndUsers(listOf(TaskAndUsers(task, listOf(testUser1))))

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    // Verify error is shown and tasks are not displayed
    composeTestRule.onNodeWithText("Error: Network error").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Task").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_loadingStateHidesTaskList() {
    // Set loading state with some tasks in state
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))
    mockViewModel.setLoading(true)
    mockViewModel.setTasksAndUsers(listOf(TaskAndUsers(task, listOf(testUser1))))

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    // Verify loading is shown and tasks are not displayed
    composeTestRule.onNodeWithText("Loading tasks...").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Task").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_transitionFromLoadingToSuccessDisplaysTasksCorrectly() {
    // Start with loading
    mockViewModel.setLoading(true)

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule.waitUntilExactlyOneExists(hasText("Loading tasks..."), 3000)

    // Transition to success with tasks
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Loaded Task",
            assignedUserIds = listOf("user1"))
    mockViewModel.setLoading(false)
    mockViewModel.setTasksAndUsers(listOf(TaskAndUsers(task, listOf(testUser1))))

    // Verify tasks are now displayed
    composeTestRule.waitUntilExactlyOneExists(hasText("Loaded Task"), 3000)
    composeTestRule.onNodeWithText("Loaded Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("Loading tasks...").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_transitionFromErrorToSuccessDisplaysTasksCorrectly() {
    // Start with error
    mockViewModel.setError("Initial error")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule.waitUntilExactlyOneExists(hasText("Error: Initial error"), 3000)

    // Recover from error
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Recovered Task",
            assignedUserIds = listOf("user1"))
    mockViewModel.setError(null)
    mockViewModel.setTasksAndUsers(listOf(TaskAndUsers(task, listOf(testUser1))))

    // Verify tasks are now displayed
    composeTestRule.waitUntilExactlyOneExists(hasText("Recovered Task"), 3000)
    composeTestRule.onNodeWithText("Recovered Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("Error: Initial error").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_withNetworkErrorDisplaysSpecificErrorMessage() {
    mockViewModel.setError("Network connection failed. Please check your internet connection.")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithText("Error: Network connection failed. Please check your internet connection.")
        .assertIsDisplayed()
  }

  @Test
  fun tasksScreen_withDatabaseErrorDisplaysSpecificErrorMessage() {
    mockViewModel.setError("Database error: Failed to sync tasks")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithText("Error: Database error: Failed to sync tasks")
        .assertIsDisplayed()
  }

  @Test
  fun tasksScreen_withAuthenticationErrorDisplaysSpecificErrorMessage() {
    mockViewModel.setError("Authentication failed. Please sign in again.")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule
        .onNodeWithText("Error: Authentication failed. Please sign in again.")
        .assertIsDisplayed()
  }

  @Test
  fun tasksScreen_errorStateFiltersStillInteractive() {
    mockViewModel.setError("Test error")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule.waitUntilExactlyOneExists(hasText("Error: Test error"), 3000)

    // Verify filters are still clickable (even though they don't affect error state)
    composeTestRule.onNodeWithTag(getFilterTag(TaskScreenFilter.Team)).assertIsDisplayed()
    composeTestRule.onNodeWithTag(getFilterTag(TaskScreenFilter.Team)).performClick()

    // Error should still be displayed after filter click
    composeTestRule.onNodeWithText("Error: Test error").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_errorStateActionButtonsStillClickable() {
    mockViewModel.setError("Test error")

    var createTaskClicked = false
    var autoAssignClicked = false

    composeTestRule.setContent {
      TasksScreen(
          viewModel = mockViewModel,
          onCreateTaskClick = { createTaskClicked = true },
          onAutoAssignClick = { autoAssignClicked = true })
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Error: Test error"), 3000)

    // Verify action buttons are still clickable
    composeTestRule.onNodeWithText("+ New Task").performClick()
    assert(createTaskClicked) { "Create task button should work even in error state" }

    composeTestRule.onNodeWithText("Auto-assign").performClick()
    assert(autoAssignClicked) { "Auto-assign button should work even in error state" }
  }

  @Test
  fun tasksScreen_toggleCompletionWithMockViewModelCallsToggleMethod() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Toggle Test",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.TODO)

    mockViewModel.setTasksAndUsers(listOf(TaskAndUsers(task, listOf(testUser1))))

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule.waitUntilExactlyOneExists(hasText("Toggle Test"), 3000)

    // Find and click the checkbox
    composeTestRule.onNodeWithTag("checkbox").performClick()

    // Verify the mock view model tracked the call
    assert(mockViewModel.toggleCompletionCalls.isNotEmpty()) {
      "toggleTaskCompletion should be called"
    }
    assert(mockViewModel.toggleCompletionCalls[0].taskID == "task1") {
      "Correct task should be toggled"
    }
  }

  @Test
  fun tasksScreen_multipleErrorsDisplaysLatestError() {
    mockViewModel.setError("First error")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    composeTestRule.waitUntilExactlyOneExists(hasText("Error: First error"), 3000)

    // Update to new error
    mockViewModel.setError("Second error - more recent")

    composeTestRule.waitUntilExactlyOneExists(hasText("Error: Second error - more recent"), 3000)

    // First error should not be displayed
    composeTestRule.onNodeWithText("Error: First error").assertDoesNotExist()
  }

  @Test
  fun tasksScreen_emptyErrorStringShowsGenericError() {
    mockViewModel.setError("")

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    // Empty error string should still trigger error state
    composeTestRule.onNodeWithTag(TasksScreenTestTags.ERROR_MESSAGE).assertIsDisplayed()
    composeTestRule.onNodeWithText("Error: ").assertIsDisplayed()
  }

  @Test
  fun tasksScreen_longErrorMessageDisplaysCompletely() {
    val longError =
        "This is a very long error message that describes in detail what went wrong with the task loading process. " +
            "It includes multiple sentences and provides comprehensive information about the failure. " +
            "The UI should be able to handle this without truncation or layout issues."

    mockViewModel.setError(longError)

    composeTestRule.setContent { TasksScreen(viewModel = mockViewModel) }

    // Verify the full error message is present
    composeTestRule.onNodeWithText("Error: $longError", substring = true).assertIsDisplayed()
  }
}
