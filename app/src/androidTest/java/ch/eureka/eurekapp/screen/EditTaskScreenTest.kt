package ch.eureka.eurekapp.screen

import android.Manifest
import android.content.Context
import android.provider.MediaStore
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import ch.eureka.eurekapp.model.tasks.EditTaskViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CameraScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreen
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreenTestTags
import ch.eureka.eurekapp.ui.tasks.TaskScreenViewModel
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.Timestamp
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of Grok.

open class EditTaskScreenTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  var testUserId: String = ""
  private lateinit var context: Context
  private var lastEditVm: EditTaskViewModel? = null

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    // Clear first before signing in
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
    // Sign in anonymously first to ensure auth is established before clearing data
    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")

    // Verify auth state is properly set
    if (FirebaseEmulator.auth.currentUser == null) {
      throw IllegalStateException("Auth state not properly established after sign-in")
    }

    context = InstrumentationRegistry.getInstrumentation().targetContext
    clearTestPhotos()
  }

  @After
  fun tearDown() = runBlocking {
    // Cancel any raw, injected ViewModel that might still be alive
    lastEditVm?.viewModelScope?.cancel()
    lastEditVm = null
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  private val taskRepository: TaskRepository =
      FirestoreTaskRepository(firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

  protected suspend fun setupTestProject(projectId: String, role: ProjectRole = ProjectRole.OWNER) {
    // Create project and member sequentially (security rules require project to exist first)
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    // First create the project document with createdBy field and memberIds
    val project =
        Project(
            projectId = projectId,
            name = "Test Project",
            description = "Test project for integration tests",
            status = ProjectStatus.OPEN,
            createdBy = testUserId,
            memberIds = listOf(testUserId))
    projectRef.set(project).await()

    // Then add the test user as a member
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
      attachmentUrls: List<String> = emptyList(),
      dependingOnTasks: List<String> = emptyList()
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
            status = status,
            dependingOnTasks = dependingOnTasks)
    taskRepository.updateTask(task).getOrThrow()
  }

  @Test
  fun testEmptyFieldsShowErrors() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        // Wait for task to load
        composeTestRule.waitForIdle()

        // Clear title and focus
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextClearance()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performClick()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.ERROR_MSG).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Valid Title")

        // Clear description and focus
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextClearance()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performClick()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.ERROR_MSG).assertIsDisplayed()
      }

  @Test
  fun testInvalidDateShowsError() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Input invalid date
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("invalid-date")
        composeTestRule.onNodeWithTag(CommonTaskTestTags.ERROR_MSG).assertIsDisplayed()
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

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Verify fields are loaded
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.STATUS_BUTTON).assertIsDisplayed()
      }

  @Test
  fun testTakingPhotos() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Ensure no initial photo previews are present (clean up if any residuals)
        val existingDeletes =
            composeTestRule.onAllNodesWithTag(CommonTaskTestTags.DELETE_PHOTO).fetchSemanticsNodes()
        repeat(existingDeletes.size) {
          composeTestRule.onAllNodesWithTag(CommonTaskTestTags.DELETE_PHOTO)[0].performClick()
        }
        composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).assertCountEquals(0)

        // Add photo
        composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_PHOTO).performClick()
        composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

        composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

        composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsDisplayed()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).assertIsDisplayed()

        // Delete photo
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).performClick()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsNotDisplayed()
      }

  @Test
  fun testTaskEdited() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextClearance()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextClearance()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextClearance()

        // Edit fields
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Edited Task")
        composeTestRule
            .onNodeWithTag(CommonTaskTestTags.DESCRIPTION)
            .performTextInput("Edited Desc")
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("25/12/2025")

        composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

        // Verify task updated without leaving background coroutines running
        val task = taskRepository.getTaskById(projectId, taskId).first()
        assert(task?.title == "Edited Task")
      }

  @Test
  fun testTaskDeleted() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val remoteUrl1 = "https://fake.com/photo1.jpg"
        val remoteUrl2 = "https://fake.com/photo2.jpg"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId, attachmentUrls = listOf(remoteUrl1, remoteUrl2))

        val fileRepository = FakeFileRepository()
        val mockProjectRepository = FakeProjectRepository()
        mockProjectRepository.setCurrentUserProjects(
            flowOf(listOf(Project(projectId = projectId, name = "Test Project"))))
        val viewModel = EditTaskViewModel(taskRepository, fileRepository = fileRepository)
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // First, click delete to show dialog
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.DELETE_TASK).performClick()
        // Verify dialog is shown
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CONFIRM_DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CANCEL_DELETE).assertIsDisplayed()
        // Click cancel to dismiss dialog
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CANCEL_DELETE).performClick()
        // Verify dialog is dismissed
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CONFIRM_DELETE).assertIsNotDisplayed()
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CANCEL_DELETE).assertIsNotDisplayed()
        // Click delete again to show dialog
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.DELETE_TASK).performClick()
        // Verify dialog is shown again
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CONFIRM_DELETE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CANCEL_DELETE).assertIsDisplayed()
        // Confirm deletion
        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.CONFIRM_DELETE).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

        // Verify task deleted
        viewModel.viewModelScope.launch(Dispatchers.IO) {
          val task = taskRepository.getTaskById(projectId, taskId).first()
          assert(task == null)
        }

        // Verify all remote attachments were deleted
        assert(fileRepository.deletedFiles.contains(remoteUrl1))
        assert(fileRepository.deletedFiles.contains(remoteUrl2))
      }

  @Test
  fun testStatusChange() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId, status = TaskStatus.TODO)

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(EditTaskScreenTestTags.STATUS_BUTTON).performClick()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()
        composeTestRule.waitForIdle()

        val task = taskRepository.getTaskById(projectId, taskId).first()
        assert(task?.status == TaskStatus.IN_PROGRESS)
      }

  @Test
  fun testSaveButtonEnabledOnlyWithValidInput() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextClearance()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextClearance()
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextClearance()

        val saveButton = composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK)
        saveButton.performClick() // Should not save with invalid input

        // Make valid
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Valid Title")
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextInput("Valid Desc")
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("15/10/2025")

        saveButton.performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
      }

  @Test
  fun testNavigateFromTasksToEditTask() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(
            projectId,
            taskId,
            title = "Test Task",
            description = "Test Desc",
            dueDate = "15/10/2025")

        val taskViewModel =
            TestableTaskScreenViewModel(
                taskRepository,
                FakeProjectRepository(),
                FakeUserRepository(),
                currentUserId = testUserId)

        composeTestRule.setContent {
          val navController = rememberNavController()
          NavHost(navController, startDestination = Route.TasksSection.Tasks) {
            composable<Route.TasksSection.Tasks> {
              TasksScreen(
                  onTaskClick = { tId, pId ->
                    navController.navigate(
                        Route.TasksSection.EditTask(projectId = pId, taskId = tId))
                  },
                  viewModel = taskViewModel)
            }
            composable<Route.TasksSection.EditTask> { backStackEntry ->
              val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.EditTask>()

              val fakeProjectRepository = FakeProjectRepository()
              fakeProjectRepository.setCurrentUserProjects(
                  flowOf(listOf(Project(projectId = projectId, name = "Test Project"))))
              val editTaskViewModel: EditTaskViewModel =
                  viewModel(
                      factory = EditTaskViewModelFactory(taskRepository, FakeFileRepository()))

              EditTaskScreen(
                  projectId = editTaskRoute.projectId,
                  taskId = editTaskRoute.taskId,
                  navigationController = navController,
                  editTaskViewModel = editTaskViewModel)
            }
            composable<Route.Camera> { Camera(navigationController = navController) }
          }
        }

        composeTestRule.waitForIdle()

        // Wait for task to load
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onAllNodesWithTag(TasksScreenTestTags.TASK_CARD)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

        // Click on the first task card (which should be our test task)
        composeTestRule.onAllNodesWithTag(TasksScreenTestTags.TASK_CARD)[0].performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify navigation to EditTaskScreen
        composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()

        taskViewModel.cleanupForTest()
      }

  // Helper functions for dependency tests only
  private fun setupEditTaskScreenForDependencies(
      projectId: String,
      taskId: String
  ): EditTaskViewModel {
    val viewModel = EditTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
    lastEditVm = viewModel
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(
          projectId = projectId,
          taskId = taskId,
          navController = navController,
          viewModel = viewModel)
      navController.navigate(Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
    }
    composeTestRule.waitForIdle()
    return viewModel
  }

  private fun waitForDependenciesReady(viewModel: EditTaskViewModel, taskId: String) {
    composeTestRule.waitUntil(timeoutMillis = 10000) { viewModel.uiState.value.taskId == taskId }
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 10000) { viewModel.availableTasks.value.isNotEmpty() }
    composeTestRule.waitForIdle()
  }

  private fun scrollToDependencyButton() {
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule
          .onAllNodesWithTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    try {
      composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON).assertIsDisplayed()
    } catch (e: Exception) {
      composeTestRule
          .onRoot()
          .performScrollToNode(hasTestTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON))
      composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON).assertIsDisplayed()
    }
  }

  private fun openDependencyMenu() {
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_DEPENDENCY_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      composeTestRule
          .onAllNodesWithTag("${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_menu")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
  }

  private fun scrollToDependencyItem(dependencyId: String) {
    try {
      composeTestRule
          .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId")
          .assertIsDisplayed()
    } catch (e: Exception) {
      composeTestRule
          .onRoot()
          .performScrollToNode(
              hasTestTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId"))
      composeTestRule
          .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId")
          .assertIsDisplayed()
    }
  }

  @Test
  fun testDependenciesLoadedFromTask() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val dependencyId = "task1"

        setupTestProject(projectId)
        setupTestTask(projectId, dependencyId, title = "Dependency Task")
        setupTestTask(
            projectId, taskId, title = "Main Task", dependingOnTasks = listOf(dependencyId))

        val viewModel = setupEditTaskScreenForDependencies(projectId, taskId)
        waitForDependenciesReady(viewModel, taskId)

        scrollToDependencyItem(dependencyId)
        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId")
            .assertIsDisplayed()
      }

  @Test
  fun testCurrentTaskIsexcludedfromavailabletasks() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val taskId1 = "task1"

        setupTestProject(projectId)
        setupTestTask(projectId, taskId, title = "Current Task")
        setupTestTask(projectId, taskId1, title = "Other Task")

        val viewModel = setupEditTaskScreenForDependencies(projectId, taskId)
        waitForDependenciesReady(viewModel, taskId)
        scrollToDependencyButton()
        openDependencyMenu()

        composeTestRule
            .onAllNodesWithTag("${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_$taskId")
            .assertCountEquals(0)
        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_$taskId1")
            .assertIsDisplayed()
      }

  @Test
  fun testEditTaskWithdependenciesSavesdependencies() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val dependencyId = "task1"

        setupTestProject(projectId)
        setupTestTask(projectId, dependencyId, title = "Dependency Task")
        setupTestTask(projectId, taskId, title = "Main Task")

        val viewModel = setupEditTaskScreenForDependencies(projectId, taskId)
        waitForDependenciesReady(viewModel, taskId)
        scrollToDependencyButton()
        openDependencyMenu()

        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_$dependencyId")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          composeTestRule
              .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
              .fetchSemanticsNodes()
              .isNotEmpty()
        }

        val task = taskRepository.getTaskById(projectId, taskId).first()
        assert(task?.dependingOnTasks?.contains(dependencyId) == true)
      }

  @Test
  fun testRemoveDependencyRemovesfromlist() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val dependencyId = "task1"

        setupTestProject(projectId)
        setupTestTask(projectId, dependencyId, title = "Dependency Task")
        setupTestTask(
            projectId, taskId, title = "Main Task", dependingOnTasks = listOf(dependencyId))

        val viewModel = setupEditTaskScreenForDependencies(projectId, taskId)
        waitForDependenciesReady(viewModel, taskId)

        scrollToDependencyItem(dependencyId)
        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId")
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.REMOVE_DEPENDENCY}_$dependencyId")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule
            .onAllNodesWithTag("${CommonTaskTestTags.TASK_DEPENDENCY_ITEM}_$dependencyId")
            .assertCountEquals(0)
      }

  @Test
  fun testCycleErrorIsdisplayedWhencycledetected() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val taskId1 = "task1"
        val taskId2 = "task2"

        setupTestProject(projectId)
        setupTestTask(projectId, taskId1, title = "Task 1", dependingOnTasks = listOf(taskId2))
        setupTestTask(projectId, taskId2, title = "Task 2", dependingOnTasks = listOf(taskId))
        setupTestTask(projectId, taskId, title = "Main Task")

        val viewModel = setupEditTaskScreenForDependencies(projectId, taskId)
        waitForDependenciesReady(viewModel, taskId)
        scrollToDependencyButton()
        openDependencyMenu()

        composeTestRule
            .onNodeWithTag("${CommonTaskTestTags.TASK_DEPENDENCIES_FIELD}_$taskId1")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 15000) { viewModel.cycleError.value != null }
        composeTestRule.waitForIdle()

        composeTestRule.waitUntil(timeoutMillis = 15000) {
          try {
            composeTestRule
                .onNodeWithTag(CommonTaskTestTags.DEPENDENCY_CYCLE_ERROR)
                .assertIsDisplayed()
            true
          } catch (e: Exception) {
            try {
              composeTestRule
                  .onRoot()
                  .performScrollToNode(hasTestTag(CommonTaskTestTags.DEPENDENCY_CYCLE_ERROR))
              composeTestRule
                  .onNodeWithTag(CommonTaskTestTags.DEPENDENCY_CYCLE_ERROR)
                  .assertIsDisplayed()
              true
            } catch (e2: Exception) {
              false
            }
          }
        }

        composeTestRule.onNodeWithTag(CommonTaskTestTags.DEPENDENCY_CYCLE_ERROR).assertIsDisplayed()
      }

  private fun clearTestPhotos() {
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
    val selectionArgs = arrayOf("Pictures/EurekApp/")
    val cursor =
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null)
    cursor?.use {
      val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
      while (cursor.moveToNext()) {
        val id = cursor.getLong(idColumn)
        val uri =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI.buildUpon()
                .appendPath(id.toString())
                .build()
        try {
          context.contentResolver.delete(uri, null, null)
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }
  }

  @Composable
  private fun FakeNavGraph(
      projectId: String,
      taskId: String,
      navController: NavHostController,
      viewModel: EditTaskViewModel
  ) {
    NavHost(navController, startDestination = Route.TasksSection.Tasks) {
      composable<Route.TasksSection.EditTask> {
        EditTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            editTaskViewModel = viewModel)
      }
      composable<Route.TasksSection.Tasks> {
        Text("Tasks Screen", modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
      }
      composable<Route.Camera> { Camera(navigationController = navController) }
    }
  }

  class FakeUserRepository : UserRepository {
    private val currentUserFlow = MutableStateFlow<User?>(null)

    override fun getUserById(userId: String): Flow<User?> {
      // Return a simple fake user flow
      return flowOf(User(uid = userId, displayName = "Test User", email = "test@example.com"))
    }

    override fun getCurrentUser(): Flow<User?> {
      // Return whatever currentUserFlow contains (can be null)
      return currentUserFlow
    }

    override suspend fun saveUser(user: User): Result<Unit> {
      // Update the current user and return success
      currentUserFlow.value = user
      return Result.success(Unit)
    }

    override suspend fun updateLastActive(userId: String): Result<Unit> {
      // No-op, just return success
      return Result.success(Unit)
    }
  }

  class FakeProjectRepository : ProjectRepository {
    private var currentUserProjects: Flow<List<Project>> = flowOf(emptyList())

    fun setCurrentUserProjects(flow: Flow<List<Project>>) {
      currentUserProjects = flow
    }

    override fun getProjectById(projectId: String): Flow<Project?> {
      // Return a dummy project or null as needed
      return flowOf(
          Project(projectId = projectId, name = "Fake Project", description = "Test project"))
    }

    override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> {
      return currentUserProjects
    }

    override suspend fun createProject(
        project: Project,
        creatorId: String,
        creatorRole: ProjectRole
    ): Result<String> {
      // Return fake project ID
      return Result.success("fake_project_id")
    }

    override suspend fun updateProject(project: Project): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun deleteProject(projectId: String): Result<Unit> {
      return Result.success(Unit)
    }

    override fun getMembers(projectId: String): Flow<List<Member>> {
      // Return empty member list
      return flowOf(emptyList())
    }

    override suspend fun addMember(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun removeMember(projectId: String, userId: String): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun updateMemberRole(
        projectId: String,
        userId: String,
        role: ProjectRole
    ): Result<Unit> {
      return Result.success(Unit)
    }
  }

  class EditTaskViewModelFactory(
      private val taskRepository: TaskRepository,
      private val fileRepository: FileStorageRepository
  ) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(EditTaskViewModel::class.java)) {
        return EditTaskViewModel(taskRepository, fileRepository) as T
      }
      throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
  }

  @Test
  fun testDeleteRemotePhotos() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        val remoteUrl = "https://fake.com/photo1.jpg"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId, attachmentUrls = listOf(remoteUrl))

        val fileRepository = FakeFileRepository()
        val viewModel = EditTaskViewModel(taskRepository, fileRepository = fileRepository)
        lastEditVm = viewModel
        composeTestRule.setContent {
          val navController = rememberNavController()
          FakeNavGraph(
              projectId = projectId,
              taskId = taskId,
              navController = navController,
              viewModel = viewModel)
          navController.navigate(
              Route.TasksSection.EditTask(projectId = projectId, taskId = taskId))
        }

        composeTestRule.waitForIdle()

        // Wait for photo to be displayed
        composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsDisplayed()

        // Click delete photo
        composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).performClick()

        composeTestRule.waitForIdle()

        // Verify deleteFile was called for the remote URL
        assert(fileRepository.deletedFiles.contains(remoteUrl))
      }
}

class TestableTaskScreenViewModel(
    taskRepository: TaskRepository,
    projectRepository: ProjectRepository,
    userRepository: UserRepository,
    currentUserId: String?
) : TaskScreenViewModel(taskRepository, projectRepository, userRepository, currentUserId) {
  fun cleanupForTest() {
    viewModelScope.cancel()
  }
}
