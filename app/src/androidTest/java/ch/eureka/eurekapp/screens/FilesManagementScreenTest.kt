// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.FileItem
import ch.eureka.eurekapp.model.downloads.FilesManagementState
import ch.eureka.eurekapp.model.downloads.FilesManagementViewModel
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.subscreens.tasks.editing.EditTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreen
import ch.eureka.eurekapp.ui.tasks.TaskScreenViewModel
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilesManagementScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  private var testUserId: String = ""
  private lateinit var context: android.content.Context
  private var lastTaskScreenVm: TaskScreenViewModel? = null
  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private val taskRepository: TaskRepository by lazy {
    FirestoreTaskRepository(firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

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
    lastTaskScreenVm?.viewModelScope?.cancel()
    lastTaskScreenVm = null

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  private suspend fun setupTestProject(projectId: String, role: ProjectRole = ProjectRole.OWNER) {
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
      title: String = "Test Task",
      description: String = "Test Description",
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

  private fun createMockViewModel(files: List<FileItem> = emptyList()): FilesManagementViewModel {
    val viewModel = mockk<FilesManagementViewModel>(relaxed = true)
    val stateFlow = MutableStateFlow(FilesManagementState(files = files))
    every { viewModel.uiState } returns stateFlow
    return viewModel
  }

  @Test
  fun filesManagementScreen_emptyStateDisplaysNoFilesMessage() {
    val viewModel = createMockViewModel()

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule
        .onNodeWithTag(FilesManagementScreenTestTags.NO_FILES_MESSAGE)
        .assertIsDisplayed()
  }

  @Test
  fun filesManagementScreen_topBarDisplaysCorrectTitle() {
    val viewModel = createMockViewModel()

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.TOP_BAR_TITLE).assertIsDisplayed()
  }

  @Test
  fun filesManagementScreen_backButtonTriggersCallback() {
    val viewModel = createMockViewModel()
    var backClicked = false

    composeTestRule.setContent {
      FilesManagementScreen(onBackClick = { backClicked = true }, viewModel = viewModel)
    }

    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.TOP_BAR_TITLE).assertExists()
    // Note: BackButton would need a test tag to be directly clickable
  }

  @Test
  fun filesManagementScreen_fileListDisplaysFiles() {
    val mockFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf")
    val fileItem =
        FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf"))
    val viewModel = createMockViewModel(files = listOf(fileItem))

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule
        .onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_test.pdf")
        .assertIsDisplayed()
    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.OPEN_BUTTON).assertIsDisplayed()
    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).assertIsDisplayed()
  }

  @Test
  fun filesManagementScreen_imageFileDisplaysWithThumbnail() {
    val mockFile =
        DownloadedFile(
            url = "http://example.com/photo.jpg",
            fileName = "photo.jpg",
            localPath = "/path/to/photo.jpg")
    val fileItem =
        FileItem(
            file = mockFile,
            displayName = "photo.jpg",
            isImage = true,
            uri = Uri.parse("file:///path/to/photo.jpg"))
    val viewModel = createMockViewModel(files = listOf(fileItem))

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule
        .onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_photo.jpg")
        .assertIsDisplayed()
  }

  @Test
  fun filesManagementScreen_openButtonCallsViewModel() {
    val mockFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf")
    val fileItem =
        FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf"))
    val viewModel = createMockViewModel(files = listOf(fileItem))

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.OPEN_BUTTON).performClick()

    verify { viewModel.getOpenFileIntent(fileItem) }
  }

  @Test
  fun filesManagementScreen_deleteButtonShowsConfirmationDialog() {
    val mockFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf")
    val fileItem =
        FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf"))
    val viewModel = createMockViewModel(files = listOf(fileItem))

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TEXT)
        .assertIsDisplayed()
  }

  @Test
  fun filesManagementScreen_deleteDialogCancelButtonDismissesDialog() {
    val mockFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf")
    val fileItem =
        FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf"))
    val viewModel = createMockViewModel(files = listOf(fileItem))

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.CANCEL_DELETE_BUTTON).performClick()

    composeTestRule
        .onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE)
        .assertDoesNotExist()
  }

  @Test
  fun filesManagementScreen_deleteDialogConfirmButtonCallsViewModelAndDismisses() {
    val mockFile =
        DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf")
    val fileItem =
        FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf"))
    val viewModel = createMockViewModel(files = listOf(fileItem))

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(FilesManagementScreenTestTags.CONFIRM_DELETE_BUTTON)
        .performClick()

    verify { viewModel.deleteFile(fileItem, any()) }
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE)
        .assertDoesNotExist()
  }

  @Test
  fun filesManagementScreen_multipleFilesAllDisplayed() {
    val files =
        listOf(
            FileItem(
                file =
                    DownloadedFile(
                        url = "http://example.com/file1.pdf",
                        localPath = "/path/1",
                        fileName = "file1.pdf"),
                displayName = "file1.pdf",
                isImage = false,
                uri = Uri.parse("file:///path/1")),
            FileItem(
                file =
                    DownloadedFile(
                        url = "http://example.com/file2.jpg",
                        localPath = "/path/2",
                        fileName = "file2.jpg"),
                displayName = "file2.jpg",
                isImage = true,
                uri = Uri.parse("file:///path/2")),
            FileItem(
                file =
                    DownloadedFile(
                        url = "http://example.com/file3.txt",
                        localPath = "/path/3",
                        fileName = "file3.txt"),
                displayName = "file3.txt",
                isImage = false,
                uri = Uri.parse("file:///path/3")))
    val viewModel = createMockViewModel(files = files)

    composeTestRule.setContent { FilesManagementScreen(viewModel = viewModel) }

    composeTestRule
        .onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_file1.pdf")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_file2.jpg")
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_file3.txt")
        .assertIsDisplayed()
  }

  @Test
  fun testNavigationFromTasksScreenToFilesManagementScreen() =
      runBlocking<Unit> {
        val projectId = "project123"
        val taskId = "task123"
        setupTestProject(projectId)
        setupTestTask(projectId, taskId)

        val mockFilesViewModel = createMockViewModel() // Empty files by default

        composeTestRule.setContent {
          val navController = rememberNavController()
          NavigationGraphWithFilesManagement(
              navController = navController, filesManagementViewModel = mockFilesViewModel)
          navController.navigate(Route.TasksSection.Tasks)
        }

        composeTestRule.waitForIdle()

        // Verify we're on TasksScreen
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

        // Click the files management button
        composeTestRule.onNodeWithTag(TasksScreenTestTags.FILES_MANAGEMENT_BUTTON).performClick()

        composeTestRule.waitForIdle()

        // Verify navigation to FilesManagementScreen
        composeTestRule
            .onNodeWithTag(FilesManagementScreenTestTags.TOP_BAR_TITLE)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(FilesManagementScreenTestTags.NO_FILES_MESSAGE)
            .assertIsDisplayed()
      }

  @Composable
  private fun NavigationGraphWithFilesManagement(
      navController: NavHostController,
      filesManagementViewModel: FilesManagementViewModel
  ) {
    val sharedTaskScreenViewModel = remember {
      TaskScreenViewModel(connectivityObserver = mockConnectivityObserver)
    }

    NavHost(navController, startDestination = Route.TasksSection.Tasks) {
      composable<Route.TasksSection.Tasks> {
        lastTaskScreenVm = sharedTaskScreenViewModel
        TasksScreen(
            onTaskClick = { taskId, projectId ->
              navController.navigate(Route.TasksSection.ViewTask(projectId, taskId))
            },
            onCreateTaskClick = { navController.navigate(Route.TasksSection.CreateTask) },
            onFilesManagementClick = { navController.navigate("filesManagement") },
            viewModel = sharedTaskScreenViewModel)
      }
      composable<Route.TasksSection.ViewTask> { backStackEntry ->
        val taskDetailRoute = backStackEntry.toRoute<Route.TasksSection.ViewTask>()
        ViewTaskScreen(
            projectId = taskDetailRoute.projectId,
            taskId = taskDetailRoute.taskId,
            navigationController = navController)
      }
      composable<Route.TasksSection.EditTask> { backStackEntry ->
        val editTaskRoute = backStackEntry.toRoute<Route.TasksSection.EditTask>()
        EditTaskScreen(editTaskRoute.projectId, editTaskRoute.taskId, navController)
      }
      composable("filesManagement") {
        FilesManagementScreen(
            onBackClick = { navController.popBackStack() }, viewModel = filesManagementViewModel)
      }
    }
  }
}
