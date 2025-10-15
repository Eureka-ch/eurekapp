package ch.eureka.eurekapp.screen

import android.Manifest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.authentication.CurrentUserProvider
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.MainScreens
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.navigation.Screen
import ch.eureka.eurekapp.navigation.TaskSpecificScreens
import ch.eureka.eurekapp.screens.CameraScreenTestTags
import ch.eureka.eurekapp.screens.CreateTaskScreen
import ch.eureka.eurekapp.screens.CreateTaskScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of ChatGPT.

class NavigationButtonsTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  private val mockRepository = FakeTaskRepository()

  private fun navigateToCreateTaskScreen() {
    val currentScreen = mutableStateOf<Screen>(MainScreens.ProfileScreen)
    composeTestRule.setContent { NavigationMenu() }

    // Navigate to Tasks screen
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    // Click Create Task button
    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()
  }

  @Test
  fun testEmptyFieldsShowErrors() {
    navigateToCreateTaskScreen()

    // Focus and leave the Title field empty
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performClick()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ERROR_MSG).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Test Task")

    // Focus and leave the Description field empty
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION).performClick()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ERROR_MSG).assertIsDisplayed()
  }

  @Test
  fun testInvalidDateShowsError() {
    navigateToCreateTaskScreen()

    // Input title and description
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Test Task")
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION)
        .performTextInput("Some description")

    // Input invalid date
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DUE_DATE)
        .performTextInput("invalid-date")
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ERROR_MSG).assertIsDisplayed()
  }

  @Test
  fun testPhotoUpload() {
    navigateToCreateTaskScreen()

    // Initially, no photo should be displayed
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsNotDisplayed()

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    Thread.sleep(5000)

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    // Now the photo should be displayed in Create Task screen
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    // Delete the photo
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DELETE_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsNotDisplayed()
  }

  @Test
  fun testSaveButtonEnabledOnlyWithValidInput() {
    val viewModel =
        CreateTaskViewModel(mockRepository, currentUserProvider = FakeCurrentUserProvider())
    val projectId = "project123"

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(projectId = projectId, navController = navController, viewModel = viewModel)
      navController.navigate(TaskSpecificScreens.CreateTaskScreen.title)
    }

    val saveButton = composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.SAVE_TASK)

    // Initially, Save button should be disabled
    saveButton.performClick()
    Thread.sleep(1000) // Wait to ensure no navigation occurs
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).assertIsDisplayed()

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Task 1")
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION)
        .performTextInput("Description")
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Save button should be enabled now
    saveButton.performClick()
    Thread.sleep(1000) // Wait for recomposition/navigation

    // Ensure navigation back to tasks screen (pop back)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
  }

  @Composable
  private fun FakeNavGraph(
      projectId: String,
      navController: NavHostController,
      viewModel: CreateTaskViewModel
  ) {
    NavHost(navController, startDestination = MainScreens.TasksScreen.title) {
      composable(TaskSpecificScreens.CreateTaskScreen.title) {
        CreateTaskScreen(
            projectId = projectId,
            navigationController = navController,
            createTaskViewModel = viewModel)
      }
      composable(MainScreens.TasksScreen.title) {
        // Fake Tasks screen for testing pop back
        androidx.compose.material3.Text(
            "Tasks Screen",
            modifier = androidx.compose.ui.Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
      }
    }
  }

  class FakeTaskRepository : TaskRepository {
    override fun getTaskById(projectId: String, taskId: String) = flowOf(null)

    override fun getTasksInProject(projectId: String): Flow<List<Task>> = flowOf(emptyList())

    override fun getTasksForCurrentUser(): Flow<List<Task>> = flowOf(emptyList())

    override suspend fun createTask(task: Task) = Result.success("fakeTaskId")

    override suspend fun updateTask(task: Task) = Result.success(Unit)

    override suspend fun deleteTask(projectId: String, taskId: String) = Result.success(Unit)

    override suspend fun assignUser(projectId: String, taskId: String, userId: String) =
        Result.success(Unit)

    override suspend fun unassignUser(projectId: String, taskId: String, userId: String) =
        Result.success(Unit)
  }

  class FakeCurrentUserProvider : CurrentUserProvider {
    override val currentUserId: String? = "user123"
  }
}
