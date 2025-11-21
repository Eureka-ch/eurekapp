package ch.eureka.eurekapp.screen

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
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
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import com.google.firebase.storage.StorageMetadata
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of Grok.
/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

open class ViewTaskScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  var testUserId: String = ""
  private lateinit var context: android.content.Context
  private var lastViewVm: ViewTaskViewModel? = null
  private var lastTaskScreenVm: TaskScreenViewModel? = null
  private val dependenciesScreenTag = "task_dependencies_screen"
  private lateinit var mockConnectivityObserver: MockConnectivityObserver

  @Before
  fun setup() {
    runBlocking {
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
      mockConnectivityObserver = MockConnectivityObserver(context)
      mockConnectivityObserver.setConnected(true)
    }
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

  private suspend fun setupTestUser(userId: String, displayName: String = "", email: String = "") {
    val user =
        ch.eureka.eurekapp.model.data.user.User(
            uid = userId,
            displayName = displayName,
            email = email,
            photoUrl = "",
            lastActive = Timestamp.now())
    FirebaseEmulator.firestore.collection("users").document(userId).set(user).await()
  }

  private suspend fun setupTestTask(
      projectId: String,
      taskId: String,
      title: String = "Original Task",
      description: String = "Original Description",
      dueDate: String = "15/10/2025",
      status: TaskStatus = TaskStatus.TODO,
      attachmentUrls: List<String> = emptyList(),
      assignedUserIds: List<String> = listOf()
  ) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val date = dateFormat.parse(dueDate)!!
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = title,
            description = description,
            assignedUserIds = assignedUserIds.ifEmpty { listOf(testUserId) },
            dueDate = Timestamp(date),
            attachmentUrls = attachmentUrls,
            createdBy = testUserId,
            status = status)
    taskRepository.updateTask(task).getOrThrow()
  }

  /**
   * Helper function to setup a test with common boilerplate code.
   *
   * @param projectId The project ID to use
   * @param taskId The task ID to use
   * @param taskSetup Optional lambda to setup the task with custom parameters
   */
  private suspend fun setupViewTaskTest(
      projectId: String = "project123",
      taskId: String = "task123",
      taskSetup: (suspend () -> Unit)? = null
  ) {
    setupTestProject(projectId)
    taskSetup?.invoke()

    val viewModel =
        ViewTaskViewModel(
            projectId, taskId, taskRepository, Dispatchers.IO, mockConnectivityObserver)
    lastViewVm = viewModel
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(
          projectId = projectId,
          taskId = taskId,
          navController = navController,
          viewModel = viewModel)
      navController.navigate(Route.TasksSection.ViewTask(projectId = projectId, taskId = taskId))
    }

    composeTestRule.waitForIdle()
  }

  @Test
  fun testTaskLoadedCorrectly() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupViewTaskTest(projectId, taskId) {
          setupTestTask(
              projectId,
              taskId,
              title = "Loaded Task",
              description = "Loaded Desc",
              dueDate = "20/12/2024",
              status = TaskStatus.IN_PROGRESS)
        }

        // Verify fields are loaded and displayed
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()

        // Wait for data to load from Firestore
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule.onNodeWithText("Loaded Task").assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }

        composeTestRule.onNodeWithText("Loaded Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Loaded Desc").assertIsDisplayed()
        composeTestRule.onNodeWithText("20/12/2024").assertIsDisplayed()
        composeTestRule.onNodeWithText("Status: IN PROGRESS").assertIsDisplayed()
      }

  @Test
  fun testNavigateToTaskDependencies() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupViewTaskTest(projectId, taskId) { setupTestTask(projectId, taskId) }

        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.VIEW_DEPENDENCIES).assertIsDisplayed()
        composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.VIEW_DEPENDENCIES).performClick()

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(dependenciesScreenTag).assertIsDisplayed()
      }

  @Test
  fun testTaskNotFound() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "nonexistent"
        setupViewTaskTest(projectId, taskId)

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
        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, attachmentUrls = listOf(attachmentUrl1, attachmentUrl2))
        }

        // Verify attachments are displayed
        composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).assertCountEquals(2)
      }

  @Test
  fun testNavigateToEditTask() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupViewTaskTest(projectId, taskId) { setupTestTask(projectId, taskId) }

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
        setupViewTaskTest(projectId, taskId) {
          setupTestTask(
              projectId,
              taskId,
              title = "Read Only Task",
              description = "Read Only Description",
              dueDate = "25/11/2025")
        }

        // Verify fields are displayed with original values
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).assertIsDisplayed()

        // Custom matcher to check if a node is read-only (doesn't have SetText action)
        val isReadOnly =
            SemanticsMatcher("Field is read-only (no SetText action)") {
              val hasSetText = it.config.contains(SemanticsActions.SetText)
              !hasSetText
            }

        // Verify that all fields are read-only by checking they don't have SetText action
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assert(isReadOnly)
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assert(isReadOnly)
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).assert(isReadOnly)

        // Verify the original values remain displayed
        composeTestRule.onNodeWithText("Read Only Task").assertIsDisplayed()
        composeTestRule.onNodeWithText("Read Only Description").assertIsDisplayed()
        composeTestRule.onNodeWithText("25/11/2025").assertIsDisplayed()
      }

  @Test
  fun testStatusDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, status = TaskStatus.COMPLETED)
        }

        // Verify status is displayed
        composeTestRule.onNodeWithText("Status: COMPLETED").assertIsDisplayed()
      }

  @Test
  fun testNoAttachmentsDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, attachmentUrls = emptyList())
        }

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

        // Wait for task card to load from Firestore
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_CARD).assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }

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
              Route.TasksSection.ViewTask(projectId = projectId, taskId = taskId))
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

  @Test
  fun testAssignedUsersDisplayedWithDisplayName() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val userId1 = "user1"
        val userId2 = "user2"

        setupTestUser(userId1, displayName = "Alice Johnson", email = "alice@test.com")
        setupTestUser(userId2, displayName = "Bob Smith", email = "bob@test.com")

        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, assignedUserIds = listOf(userId1, userId2))
        }

        // Wait for assigned users to load from Firestore
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule
                .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
                .assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }

        // Verify assigned users section is displayed
        composeTestRule
            .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
            .assertIsDisplayed()

        // Verify the header text
        composeTestRule.onNodeWithText("Assigned Users:").assertIsDisplayed()

        // Verify individual users are displayed with their display names
        composeTestRule.onNodeWithText("• Alice Johnson").assertIsDisplayed()
        composeTestRule.onNodeWithText("• Bob Smith").assertIsDisplayed()

        // Verify the users have proper test tags
        composeTestRule
            .onNodeWithTag("${ViewTaskScreenTestTags.ASSIGNED_USER_ITEM}_0")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("${ViewTaskScreenTestTags.ASSIGNED_USER_ITEM}_1")
            .assertIsDisplayed()
      }

  @Test
  fun testAssignedUsersDisplayedWithEmailWhenNoDisplayName() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val userId1 = "user1"

        setupTestUser(userId1, displayName = "", email = "testuser@example.com")

        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, assignedUserIds = listOf(userId1))
        }

        // Wait for assigned users to be loaded and displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule
                .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
                .assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }

        // Verify assigned users section is displayed
        composeTestRule
            .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
            .assertIsDisplayed()

        // Verify user is displayed with email when display name is blank
        composeTestRule.onNodeWithText("• testuser@example.com").assertIsDisplayed()
      }

  @Test
  fun testNoAssignedUsersNotDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"

        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, assignedUserIds = emptyList())
        }

        // Verify assigned users section is NOT displayed when there are no assigned users
        composeTestRule
            .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
            .assertDoesNotExist()
        composeTestRule.onNodeWithText("Assigned Users:").assertDoesNotExist()
      }

  @Test
  fun testMultipleAssignedUsersDisplayed() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val userId1 = "user1"
        val userId2 = "user2"
        val userId3 = "user3"

        setupTestUser(userId1, displayName = "Charlie Brown", email = "charlie@test.com")
        setupTestUser(userId2, displayName = "Diana Prince", email = "diana@test.com")
        setupTestUser(userId3, displayName = "", email = "eve@test.com")

        setupViewTaskTest(projectId, taskId) {
          setupTestTask(projectId, taskId, assignedUserIds = listOf(userId1, userId2, userId3))
        }

        // Wait for assigned users to be loaded and displayed
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule
                .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
                .assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }

        // Verify assigned users section is displayed
        composeTestRule
            .onNodeWithTag(ViewTaskScreenTestTags.ASSIGNED_USERS_SECTION)
            .assertIsDisplayed()

        // Verify all three users are displayed
        composeTestRule.onNodeWithText("• Charlie Brown").assertIsDisplayed()
        composeTestRule.onNodeWithText("• Diana Prince").assertIsDisplayed()
        composeTestRule.onNodeWithText("• eve@test.com").assertIsDisplayed()

        // Verify all three user items have test tags
        composeTestRule
            .onNodeWithTag("${ViewTaskScreenTestTags.ASSIGNED_USER_ITEM}_0")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("${ViewTaskScreenTestTags.ASSIGNED_USER_ITEM}_1")
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("${ViewTaskScreenTestTags.ASSIGNED_USER_ITEM}_2")
            .assertIsDisplayed()
      }

  @Test
  fun testBackButtonNavigatesBack() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"
      setupTestProject(projectId)
      setupTestTask(projectId, taskId)

      val viewModel =
          ViewTaskViewModel(
              projectId, taskId, taskRepository, Dispatchers.IO, mockConnectivityObserver)
      lastViewVm = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        FakeNavGraph(
            navController = navController,
            viewModel = viewModel,
            projectId = projectId,
            taskId = taskId)
        navController.navigate(Route.TasksSection.ViewTask(projectId = projectId, taskId = taskId))
      }

      composeTestRule.waitForIdle()

      // Verify we're on ViewTaskScreen
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()

      // Click the back button
      composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).performClick()

      composeTestRule.waitForIdle()

      // Verify navigation back to TasksScreen
      composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
    }
  }

  @Composable
  private fun FullNavigationGraph(navController: NavHostController) {
    val sharedViewModel = remember {
      ViewTaskViewModel(
          "project123", "task123", taskRepository, Dispatchers.IO, mockConnectivityObserver)
    }
    val sharedTaskScreenViewModel = remember { TaskScreenViewModel() }

    NavHost(navController, startDestination = Route.TasksSection.Tasks) {
      composable<Route.TasksSection.Tasks> {
        // expose the TaskScreenViewModel for test teardown / inspection
        lastTaskScreenVm = sharedTaskScreenViewModel
        TasksScreen(
            onTaskClick = { taskId, projectId ->
              navController.navigate(Route.TasksSection.ViewTask(projectId, taskId))
            },
            onCreateTaskClick = { navController.navigate(Route.TasksSection.CreateTask) },
            viewModel = sharedTaskScreenViewModel)
      }
      composable<Route.TasksSection.ViewTask> { backStackEntry ->
        val taskDetailRoute = backStackEntry.toRoute<Route.TasksSection.ViewTask>()
        // expose the same instance for test teardown / inspection
        lastViewVm = sharedViewModel
        ViewTaskScreen(
            projectId = taskDetailRoute.projectId,
            taskId = taskDetailRoute.taskId,
            navigationController = navController,
            viewTaskViewModel = sharedViewModel)
      }
      composable<Route.TasksSection.EditTask> { backStackEntry ->
        val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.EditTask>()
        EditTaskScreen(editTaskRoute.projectId, editTaskRoute.taskId, navController)
      }
      composable<Route.TasksSection.TaskDependence> {
        Text("Task Dependencies Screen", modifier = Modifier.testTag(dependenciesScreenTag))
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
    val vm =
        viewModel
            ?: remember {
              ViewTaskViewModel(
                  projectId, taskId, taskRepository, Dispatchers.IO, mockConnectivityObserver)
            }
    NavHost(navController, startDestination = Route.TasksSection.Tasks) {
      composable<Route.TasksSection.Tasks> {
        Text("Tasks Screen", modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
      }
      composable<Route.TasksSection.ViewTask> {
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = vm)
      }
      composable<Route.TasksSection.EditTask> {
        // Dummy edit screen for navigation test
        Text("Edit Task Screen", modifier = Modifier.testTag(EditTaskScreenTestTags.STATUS_BUTTON))
      }
      composable<Route.TasksSection.TaskDependence> {
        Text("Task Dependencies Screen", modifier = Modifier.testTag(dependenciesScreenTag))
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
