package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import ch.eureka.eurekapp.ui.tasks.TaskScreenViewModel
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import kotlinx.coroutines.flow.flowOf
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Portions of this code were generated with the help of Grok.

@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class TasksScreenOfflineTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  private val testUser = User(uid = "user1", displayName = "Test User", email = "test@test.com")

  @Before
  fun setUp() {
    mockTaskRepository = MockTaskRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
    mockConnectivityObserver =
        MockConnectivityObserver(InstrumentationRegistry.getInstrumentation().targetContext)
    mockUserRepository.setUsers(testUser)
  }

  @After
  fun tearDown() {
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  @Test
  fun tasksScreenOfflineDisplaysMessage() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.TODO)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  projectRepository = mockProjectRepository,
                  taskRepository = mockTaskRepository,
                  mockUserRepository,
                  "user1",
                  mockConnectivityObserver))
    }

    composeTestRule.waitUntilExactlyOneExists(
        hasText("You are offline. Some features may be unavailable."), 3000)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun tasksScreenOfflineDisablesButtons() {
    mockTaskRepository.setCurrentUserTasks(flowOf(emptyList()))
    mockConnectivityObserver.setConnected(false)

    var createTaskClicked = false
    var autoAssignClicked = false

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  projectRepository = mockProjectRepository,
                  taskRepository = mockTaskRepository,
                  mockUserRepository,
                  "user1",
                  mockConnectivityObserver),
          onCreateTaskClick = { createTaskClicked = true },
          onAutoAssignClick = { autoAssignClicked = true })
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("+ New Task"), 3000)

    // Try to click action buttons - they should not trigger callbacks
    composeTestRule.onNodeWithText("+ New Task").performClick()
    composeTestRule.onNodeWithText("Auto-assign").performClick()

    assert(!createTaskClicked) { "Create task should not be triggered when offline" }
    assert(!autoAssignClicked) { "Auto-assign should not be triggered when offline" }
  }

  @Test
  fun tasksScreenOfflineViewsExistingTasks() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Offline Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.IN_PROGRESS)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  projectRepository = mockProjectRepository,
                  taskRepository = mockTaskRepository,
                  mockUserRepository,
                  "user1",
                  mockConnectivityObserver))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Offline Task"), 3000)
    composeTestRule.onNodeWithText("Offline Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("👤 Test User").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()
  }

  @Test
  fun tasksScreenGoesOfflineUpdatesUI() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockConnectivityObserver.setConnected(true)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  projectRepository = mockProjectRepository,
                  taskRepository = mockTaskRepository,
                  mockUserRepository,
                  "user1",
                  mockConnectivityObserver))
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Test Task"), 3000)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.OFFLINE_MESSAGE).assertDoesNotExist()

    // Simulate going offline
    mockConnectivityObserver.setConnected(false)

    composeTestRule.waitUntilExactlyOneExists(
        hasText("You are offline. Some features may be unavailable."), 3000)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
  }

  @Test
  fun tasksScreenComesBackOnlineUpdatesUI() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Test Task",
            assignedUserIds = listOf("user1"))

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockConnectivityObserver.setConnected(false)

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  projectRepository = mockProjectRepository,
                  taskRepository = mockTaskRepository,
                  mockUserRepository,
                  "user1",
                  mockConnectivityObserver))
    }

    composeTestRule.waitUntilExactlyOneExists(
        hasText("You are offline. Some features may be unavailable."), 3000)

    // Simulate coming back online
    mockConnectivityObserver.setConnected(true)

    composeTestRule.waitUntil(3000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.OFFLINE_MESSAGE).assertDoesNotExist()
        true
      } catch (e: AssertionError) {
        false
      }
    }
  }

  @Test
  fun tasksScreenOfflineAllowsNavigationToViewTask() {
    val task =
        Task(
            taskID = "task1",
            projectId = "proj1",
            title = "Navigable Offline Task",
            assignedUserIds = listOf("user1"),
            status = TaskStatus.TODO)

    mockTaskRepository.setCurrentUserTasks(flowOf(listOf(task)))
    mockConnectivityObserver.setConnected(false)

    var navigatedTaskId: String? = null
    var navigatedProjectId: String? = null

    composeTestRule.setContent {
      TasksScreen(
          viewModel =
              TaskScreenViewModel(
                  projectRepository = mockProjectRepository,
                  taskRepository = mockTaskRepository,
                  mockUserRepository,
                  "user1",
                  mockConnectivityObserver),
          onTaskClick = { taskId, projectId ->
            navigatedTaskId = taskId
            navigatedProjectId = projectId
          })
    }

    composeTestRule.waitUntilExactlyOneExists(hasText("Navigable Offline Task"), 3000)

    // Click on the task card
    composeTestRule.onNodeWithText("Navigable Offline Task").performClick()

    // Assert that navigation callback was triggered with correct IDs
    assert(navigatedTaskId == "task1") { "Task ID should be captured on click" }
    assert(navigatedProjectId == "proj1") { "Project ID should be captured on click" }
  }
}
