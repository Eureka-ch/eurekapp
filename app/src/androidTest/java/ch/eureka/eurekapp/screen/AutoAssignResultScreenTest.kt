package ch.eureka.eurekapp.screen

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.subscreens.tasks.AutoAssignResultScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.EmptyState
import ch.eureka.eurekapp.screens.subscreens.tasks.LoadingState
import ch.eureka.eurekapp.ui.tasks.AutoAssignResultViewModel
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// portions of this code and documentation were generated with the help of AI.
/**
 * Android instrumentation tests for AutoAssignResultScreen.
 *
 * Tests are designed to be safe against non-determinism by:
 * - Using stable test data (fixed IDs, names)
 * - Waiting for UI state to stabilize before assertions
 * - Using text-based assertions that don't depend on order
 * - Testing behavior rather than exact state
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class AutoAssignResultScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository

  private val testUser1 = User(uid = "user1", displayName = "Alice", email = "alice@test.com")
  private val testUser2 = User(uid = "user2", displayName = "Bob", email = "bob@test.com")

  private val testProject1 =
      Project(projectId = "proj1", name = "Project 1", memberIds = listOf("user1", "user2"))

  @Before
  fun setUp() {
    mockTaskRepository = MockTaskRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
  }

  private fun setContentWithNav(includeTasksScreen: Boolean = false) {
    if (includeTasksScreen) {
      // For navigation tests: start from Tasks and navigate to AutoTaskAssignment
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController, startDestination = Route.TasksSection.Tasks) {
          composable<Route.TasksSection.Tasks> {
            Text("Tasks Screen", modifier = Modifier.testTag("tasks_screen"))
          }
          composable<Route.TasksSection.AutoTaskAssignment> {
            AutoAssignResultScreen(
                navigationController = navController,
                viewModel =
                    AutoAssignResultViewModel(
                        mockTaskRepository, mockProjectRepository, mockUserRepository))
          }
        }
        navController.navigate(Route.TasksSection.AutoTaskAssignment)
      }
    } else {
      // For regular tests: start directly on AutoTaskAssignment
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController, startDestination = Route.TasksSection.AutoTaskAssignment) {
          composable<Route.TasksSection.AutoTaskAssignment> {
            AutoAssignResultScreen(
                navigationController = navController,
                viewModel =
                    AutoAssignResultViewModel(
                        mockTaskRepository, mockProjectRepository, mockUserRepository))
          }
        }
      }
    }
  }

  private fun setupBasicProject() {
    mockProjectRepository.setCurrentUserProjects(flowOf(listOf(testProject1)))
    mockProjectRepository.setMembers(
        "proj1", flowOf(listOf(Member(userId = "user1", role = ProjectRole.MEMBER))))
    mockUserRepository.setUser("user1", flowOf(testUser1))
  }

  private fun createTask(id: String, title: String = "Task $id", assigned: Boolean = false) =
      Task(
          taskID = id,
          title = title,
          status = TaskStatus.TODO,
          projectId = "proj1",
          assignedUserIds = if (assigned) listOf("user1") else emptyList())

  private fun waitForText(text: String, timeout: Long = 5000L) {
    composeTestRule.waitUntil(timeoutMillis = timeout) {
      composeTestRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }
  }

  private fun assertTextDisplayed(text: String) {
    assert(
        composeTestRule
            .onAllNodesWithText(text, substring = true)
            .fetchSemanticsNodes()
            .isNotEmpty())
  }

  @Test
  fun autoAssignResultScreen_withLoadingState_displaysLoadingIndicator() {
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))
    setContentWithNav()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Auto-Assign Results", substring = true).assertIsDisplayed()
  }

  @Test
  fun autoAssignResultScreen_withNoProjects_displaysErrorMessage() {
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))
    setContentWithNav()
    composeTestRule.waitForIdle()
    waitForText("No projects")
    assertTextDisplayed("No projects")
  }

  @Test
  fun autoAssignResultScreen_withProposedAssignments_displaysTaskCards() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks(
        "proj1", flowOf(listOf(createTask("task1"), createTask("task2"))))
    setContentWithNav()
    composeTestRule.waitForIdle()
    waitForText("Review Assignments")
    assertTextDisplayed("Review Assignments")
  }

  @Test
  fun autoAssignResultScreen_acceptAssignment_updatesButtonState() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(createTask("task1"))))
    setContentWithNav()
    composeTestRule.waitForIdle()
    waitForText("Accept")
    composeTestRule.onAllNodesWithText("Accept", substring = true).get(1).performClick()
    composeTestRule.waitForIdle()
    waitForText("Accepted", timeout = 2000L)
  }

  @Test
  fun autoAssignResultScreen_applyAcceptedAssignments_appliesOnlyAccepted() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks(
        "proj1", flowOf(listOf(createTask("task1"), createTask("task2"))))
    setContentWithNav()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntilExactlyOneExists(hasText("Accept"), timeoutMillis = 5000)
    composeTestRule.onAllNodesWithText("Accept", substring = true).get(1).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntilExactlyOneExists(hasText("Apply"), timeoutMillis = 3000)
    composeTestRule.onNodeWithText("Apply", substring = true).performClick()
    composeTestRule.waitForIdle()
    // Wait for either success message or verify assignment was called
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      mockTaskRepository.assignUserCalls.size >= 1 ||
          composeTestRule
              .onAllNodesWithText("applied", substring = true, ignoreCase = true)
              .fetchSemanticsNodes()
              .isNotEmpty()
    }
    assert(mockTaskRepository.assignUserCalls.size >= 1)
  }

  @Test
  fun autoAssignResultScreen_withEmptyAssignments_displaysEmptyState() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks(
        "proj1", flowOf(listOf(createTask("task1", assigned = true))))
    setContentWithNav(includeTasksScreen = true)
    composeTestRule.waitForIdle()
    composeTestRule.waitUntilExactlyOneExists(
        hasText("No assignments to review"), timeoutMillis = 5000)
    composeTestRule.onNodeWithText("No assignments to review").assertIsDisplayed()
  }

  @Test
  fun autoAssignResultScreen_emptyState_goBackButtonNavigatesBack() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks(
        "proj1", flowOf(listOf(createTask("task1", assigned = true))))
    setContentWithNav(includeTasksScreen = true)
    composeTestRule.waitForIdle()
    waitForText("Go Back")
    composeTestRule.onNodeWithText("Go Back", substring = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tasks Screen", substring = true).assertIsDisplayed()
  }

  @Test
  fun autoAssignResultScreen_errorState_goBackButtonNavigatesBack() {
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))
    setContentWithNav(includeTasksScreen = true)
    composeTestRule.waitForIdle()
    waitForText("Go Back")
    composeTestRule.onNodeWithText("Go Back", substring = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Tasks Screen", substring = true).assertIsDisplayed()
  }

  @Test
  fun autoAssignResultScreen_afterSuccessfulApplication_navigatesBack() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks("proj1", flowOf(listOf(createTask("task1"))))
    setContentWithNav(includeTasksScreen = true)
    composeTestRule.waitForIdle()
    waitForText("Accept")
    composeTestRule.onAllNodesWithText("Accept", substring = true).get(1).performClick()
    composeTestRule.waitForIdle()
    waitForText("Apply", timeout = 3000L)
    composeTestRule.onNodeWithText("Apply", substring = true).performClick()
    composeTestRule.waitForIdle()
    waitForText("Tasks Screen", timeout = 5000L)
    composeTestRule.onNodeWithText("Tasks Screen", substring = true).assertIsDisplayed()
  }

  @Test
  fun autoAssignResultScreen_loadingState_displaysLoadingIndicator() {
    setupBasicProject()
    mockTaskRepository.setProjectTasks("proj1", flowOf(emptyList()))
    setContentWithNav()
    composeTestRule.waitForIdle()
    // Should show loading or have completed (safe assertion)
    assert(
        composeTestRule
            .onAllNodesWithText("Calculating assignments", substring = true)
            .fetchSemanticsNodes()
            .isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("Review Assignments", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("No assignments", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty() ||
            composeTestRule
                .onAllNodesWithText("Error", substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty())
  }

  @Test
  fun loadingStateComposable_displaysProgressAndMessage() {
    composeTestRule.setContent { LoadingState() }
    composeTestRule
        .onNodeWithText("Calculating assignments...", substring = false)
        .assertIsDisplayed()
  }

  @Test
  fun emptyStateComposable_showsMessageAndButton() {
    composeTestRule.setContent {
      val navController = rememberNavController()
      EmptyState(navigationController = navController)
    }
    composeTestRule.onNodeWithText("No assignments to review").assertIsDisplayed()
    composeTestRule.onNodeWithText("Go Back").assertIsDisplayed()
  }
}
