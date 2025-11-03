package ch.eureka.eurekapp.screen

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.tasks.ViewTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreenTestTags
import ch.eureka.eurekapp.ui.tasks.TaskScreenViewModel
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageMetadata
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of Grok.

open class ViewTaskScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  var testUserId: String = ""
  private lateinit var context: android.content.Context
  private var lastViewVm: ViewTaskViewModel? = null
  private var lastTaskScreenVm: TaskScreenViewModel? = null

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")

    if (FirebaseEmulator.auth.currentUser == null) {
      throw IllegalStateException("Auth state not properly established after sign-in")
    }

    context = InstrumentationRegistry.getInstrumentation().targetContext
  }

  @After
  fun tearDown() = runBlocking {
    lastViewVm?.viewModelScope?.cancel()
    lastViewVm = null

    lastTaskScreenVm?.viewModelScope?.cancel()
    lastTaskScreenVm = null

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  private val taskRepository: TaskRepository =
      FirestoreTaskRepository(firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

  protected suspend fun setupTestProject(projectId: String, role: ProjectRole = ProjectRole.OWNER) {
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    val project =
        Project(
            projectId = projectId,
            name = "Test Project",
            description = "Test project for integration tests",
            status = ProjectStatus.OPEN,
            createdBy = testUserId,
            memberIds = listOf(testUserId))
    projectRef.set(project).await()

    val member = Member(userId = testUserId, role = role)
    val memberRef = projectRef.collection("members").document(testUserId)
    memberRef.set(member).await()
  }

  private suspend fun setupTestTask(
      projectId: String,
      taskId: String,
      title: String = "Original Task",
      description: String = "Original Description",
      dueDate: String = "15/10/2025",
      status: TaskStatus = TaskStatus.TODO,
      attachmentUrls: List<String> = emptyList()
  ) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = dateFormat.parse(dueDate)!!
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = title,
            description = description,
            assignedUserIds = listOf(testUserId),
            dueDate = Timestamp(date),
            attachmentUrls = attachmentUrls,
            createdBy = testUserId,
            status = status)
    taskRepository.updateTask(task).getOrThrow()
  }

  @Test
  fun testTaskLoadedCorrectly() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(
            projectId,
            taskId,
            title = "Loaded Task",
            description = "Loaded Desc",
            dueDate = "20/12/2024",
            status = TaskStatus.IN_PROGRESS)

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify fields are loaded and displayed
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()

        composeTestRule.onNodeWithText("Loaded Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loaded Desc").assertIsDisplayed()
        composeTestRule.onNodeWithText("20/12/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status: IN PROGRESS").assertIsDisplayed()
      }

  @Test
  fun testTaskNotFound() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "nonexistent"
        setupTestProject(projectId)
        // Do not setup task

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify the app does not crash and shows a reasonable UI state
        // Edit button should still be displayed even if task not found
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()
      }

  @Test
  fun testAttachmentsDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val attachmentUrl1 = "https://fake.com/photo1.jpg"
        val attachmentUrl2 = "https://fake.com/photo2.jpg"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId, attachmentUrls = listOf(attachmentUrl1, attachmentUrl2))

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify attachments are displayed
        composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).assertCountEquals(2)
      }

  @Test
  fun testNavigateToEditTask() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Click edit button
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify navigation to EditTaskScreen
        composeTestRule.onNodeWithText("Edit Task Screen").assertIsDisplayed()
      }

  @Test
  fun testFieldsAreReadOnly() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(
            projectId,
            taskId,
            title = "Read Only Task",
            description = "Read Only Description",
            dueDate = "25/11/2025")

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify fields are not enabled (read-only)
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assertIsNotEnabled()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).assertIsNotEnabled()
      }

  @Test
  fun testStatusDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId, status = TaskStatus.COMPLETED)

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify status is displayed
        composeTestRule.onNodeWithText("Status: COMPLETED").assertIsDisplayed()
      }

  @Test
  fun testNoAttachmentsDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId, attachmentUrls = emptyList())

        val viewModel = ViewTaskViewModel(taskRepository)
        lastViewVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify no attachments are displayed
        composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).assertCountEquals(0)
      }

  @Test
  fun testNavigationFromTasksScreenToViewTask() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(
            projectId,
            taskId,
            title = "Navigation Test Task",
            description = "Test Description",
            dueDate = "01/01/2025")

        composeTestRule.setContent {
          val navController = rememberNavController()
          FullNavigationGraph(navController = navController)
          // Start on TasksScreen
          navController.navigate(Route.TasksSection.Tasks)
        }

        composeTestRule.waitForIdle()

        // Verify we're on TasksScreen
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

        // Click on the task card
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_CARD).performClick()

        composeTestRule.waitForIdle()

        // Verify navigation to ViewTaskScreen
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
      }

  @Test
  fun testNavigationToEditTaskAndSaveModifications() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(
            projectId,
            taskId,
            title = "Original Title",
            description = "Original Description",
            dueDate = "15/10/2025",
            status = TaskStatus.TODO)

        composeTestRule.setContent {
          val navController = rememberNavController()
          FullNavigationGraph(navController = navController)
          navController.navigate(
              Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Click edit button
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).performClick()

        composeTestRule.waitForIdle()

        // Verify we're on EditTaskScreen
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.STATUS_BUTTON).assertIsDisplayed()

        // Change status
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.STATUS_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithText("TODO").assertIsDisplayed()

        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.STATUS_BUTTON).performClick()
        composeTestRule.waitForIdle()

        // Verify status changed to IN_PROGRESS
        composeTestRule.onNodeWithText("IN PROGRESS").assertIsDisplayed()

        // Save the task
        composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()

        composeTestRule.waitForIdle()

        // Wait for navigation back to ViewTaskScreen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule.onNodeWithTag(EditTaskScreenTestTags.STATUS_BUTTON).assertDoesNotExist()
            true
          } catch (e: AssertionError) {
            false
          }
        }

        // Wait for data to reload on ViewTaskScreen
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule.onNodeWithText("Status: IN PROGRESS").assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }

        // Verify we're back on ViewTaskScreen and status was updated
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()
        composeTestRule.onNodeWithText("Status: IN PROGRESS").assertIsDisplayed()

        // Verify the original title and description are still there
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assertIsDisplayed()
      }

  @Composable
  private fun FullNavigationGraph(navController: NavHostController) {
    // Create a single, remembered ViewTaskViewModel instance so it is stable across navigation.
    // This ensures the ViewModel keeps collecting updates (and ViewTaskScreen reloads data)
    // when navigating to EditTaskScreen and back.
    val sharedViewModel = remember { ViewTaskViewModel(taskRepository) }
    // Also remember TaskScreenViewModel to prevent orphaned listeners
    val sharedTaskScreenViewModel = remember { TaskScreenViewModel() }

    NavHost(navController, startDestination = Route.TasksSection.Tasks) {
      composable<Route.TasksSection.Tasks> {
        // expose the TaskScreenViewModel for test teardown / inspection
        lastTaskScreenVm = sharedTaskScreenViewModel
        TasksScreen(
            onTaskClick = { taskId, projectId ->
              navController.navigate(Route.TasksSection.TaskDetail(projectId, taskId))
            },
            onCreateTaskClick = { navController.navigate(Route.TasksSection.CreateTask) },
            viewModel = sharedTaskScreenViewModel)
      }
      composable<Route.TasksSection.TaskDetail> { backStackEntry ->
        val taskDetailRoute = backStackEntry.toRoute<Route.TasksSection.TaskDetail>()
        // expose the same instance for test teardown / inspection
        lastViewVm = sharedViewModel
        ViewTaskScreen(
            projectId = taskDetailRoute.projectId,
            taskId = taskDetailRoute.taskId,
            navigationController = navController,
            viewTaskViewModel = sharedViewModel)
      }
      composable<Route.TasksSection.TaskEdit> { backStackEntry ->
        val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.TaskEdit>()
        EditTaskScreen(editTaskRoute.projectId, editTaskRoute.taskId, navController)
      }
    }
  }

  @Composable
  private fun FakeNavGraph(
      projectId: String,
      taskId: String,
      navController: NavHostController,
      viewModel: ViewTaskViewModel? = null
  ) {
    // Use the provided ViewModel if given, otherwise create a remembered instance so the VM is
    // stable.
    val vm = viewModel ?: remember { ViewTaskViewModel(taskRepository) }
    NavHost(
        navController,
        startDestination = Route.TasksSection.TaskDetail(projectId = projectId, taskId = taskId)) {
          composable<Route.TasksSection.TaskDetail> {
            ViewTaskScreen(
                projectId = projectId,
                taskId = taskId,
                navigationController = navController,
                viewTaskViewModel = vm)
          }
          composable<Route.TasksSection.TaskEdit> {
            // Dummy edit screen for navigation test
            Text(
                "Edit Task Screen",
                modifier = Modifier.testTag(EditTaskScreenTestTags.STATUS_BUTTON))
          }
          composable<Route.TasksSection.Tasks> {
            Text("Tasks Screen", modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
          }
        }
  }

  class FakeFileRepository : FileStorageRepository {
    override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
      return Result.success("https://fakeurl.com/file.jpg")
    }

    override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
      return Result.success(StorageMetadata.Builder().setContentType("image/jpeg").build())
    }
  }
}
