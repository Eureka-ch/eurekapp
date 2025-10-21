package ch.eureka.eurekapp.screen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.MainScreens
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.navigation.Screen
import ch.eureka.eurekapp.navigation.SharedScreens
import ch.eureka.eurekapp.navigation.TaskSpecificScreens
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CameraScreenTestTags
import ch.eureka.eurekapp.screens.CreateTaskScreen
import ch.eureka.eurekapp.screens.CreateTaskScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.StorageMetadata
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of ChatGPT.

class CreateTaskScreenTests : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()
  @get:Rule
  var permissionRule: GrantPermissionRule? = GrantPermissionRule.grant(Manifest.permission.CAMERA)

  var testUserId: String = ""
  private lateinit var context: Context

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    // Clear first before signing in
    FirebaseEmulator.clearFirestoreEmulator()
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

  @After fun tearDown() = runBlocking { FirebaseEmulator.clearFirestoreEmulator() }

  private val taskRepository: TaskRepository =
      FirestoreTaskRepository(firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

  protected suspend fun setupTestProject(projectId: String, role: ProjectRole = ProjectRole.OWNER) {
    // Create project and member sequentially (security rules require project to exist first)
    // Note: Data is already cleared in setup() via clearFirestoreEmulator()
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    // First create the project document with createdBy field and memberIds
    val project =
        ch.eureka.eurekapp.model.data.project.Project(
            projectId = projectId,
            name = "Test Project",
            description = "Test project for integration tests",
            status = ch.eureka.eurekapp.model.data.project.ProjectStatus.OPEN,
            createdBy = testUserId,
            memberIds = listOf(testUserId))
    projectRef.set(project).await()

    // Then add the test user as a member
    val member = ch.eureka.eurekapp.model.data.project.Member(userId = testUserId, role = role)
    val memberRef = projectRef.collection("members").document(testUserId)
    memberRef.set(member).await()
  }

  private fun navigateToCreateTaskScreen() {
    val currentScreen = mutableStateOf<Screen>(MainScreens.ProfileScreen)
    composeTestRule.setContent { NavigationMenu() }

    // Navigate to Tasks screen
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()
  }

  @Test
  fun testEmptyFieldsShowErrors() {
    navigateToCreateTaskScreen()
    // Focus and leave the Title field empty
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performClick()
    composeTestRule.waitForIdle()
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

  /** This is our end-to-end test. */
  @Test
  fun testTakingPhotos() {
    navigateToCreateTaskScreen()

    // Initially, no photo should be displayed
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsNotDisplayed()

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

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
  fun testPhotoUpload() {
    val viewModel = CreateTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
    val projectId = "project123"

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(projectId = projectId, navController = navController, viewModel = viewModel)
      navController.navigate(TaskSpecificScreens.CreateTaskScreen.title)
    }

    assert(!isPhotoSaved(context, "Pictures/EurekApp/"))

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Task 1")
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION)
        .performTextInput("Description")
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    assert(isPhotoSaved(context, "Pictures/EurekApp/"))

    // Now the photo should be displayed in Create Task screen and inputs conserved
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.SAVE_TASK).performClick()
    // Ensure navigation back to tasks screen (pop back)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
    assert(!isPhotoSaved(context, "Pictures/EurekApp/"))
  }

  /* @Test
  fun testDefectiveFileRepository() {
    val viewModel =
        CreateTaskViewModel(taskRepository, fileRepository = DefectiveFakeFileRepository())
    val projectId = "project123"

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(projectId = projectId, navController = navController, viewModel = viewModel)
      navController.navigate(TaskSpecificScreens.CreateTaskScreen.title)
    }

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Task 1")
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION)
        .performTextInput("Description")
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    // Now the photo should be displayed in Create Task screen and inputs conserved
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.SAVE_TASK).performClick()
    // Even with defective file repository, should navigate back to tasks screen (no crash)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
  }*/

  @Test
  fun testTaskCreated() {
    val viewModel = CreateTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
    val projectId = "project123"

    runBlocking { setupTestProject(projectId, ProjectRole.OWNER) }

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(projectId = projectId, navController = navController, viewModel = viewModel)
      navController.navigate(TaskSpecificScreens.CreateTaskScreen.title)
    }

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Task 1")
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION)
        .performTextInput("Description")
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    // Now the photo should be displayed in Create Task screen and inputs conserved
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.SAVE_TASK).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    val dateText = "15/10/2025"
    val simpleDateFormat = java.text.SimpleDateFormat("dd/MM/yyyy")
    val date = simpleDateFormat.parse(dateText)!!

    val task =
        Task(
            projectId = projectId,
            title = "Task 1",
            description = "Description",
            dueDate = com.google.firebase.Timestamp(date), // 15/10/2025
            attachmentUrls = listOf(),
            createdBy = FirebaseAuth.getInstance().currentUser!!.uid)

    viewModel.viewModelScope.launch(Dispatchers.IO) {
      val tasks = taskRepository.getTasksForCurrentUser().first()
      val found = tasks.any { it.title == task.title && it.dueDate == task.dueDate }
      assert(found)
    }
  }

  @Test
  fun testSaveButtonEnabledOnlyWithValidInput() {
    val viewModel = CreateTaskViewModel(taskRepository)
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
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).assertIsDisplayed()

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.TITLE).performTextInput("Task 1")
    composeTestRule
        .onNodeWithTag(CreateTaskScreenTestTags.DESCRIPTION)
        .performTextInput("Description")
    composeTestRule.onNodeWithTag(CreateTaskScreenTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Save button should be enabled now
    saveButton.performClick()
    composeTestRule.waitForIdle()
    // Ensure navigation back to tasks screen (pop back)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
  }

  fun isPhotoSaved(context: Context, relativePath: String): Boolean {
    val projection = arrayOf(MediaStore.Images.Media.DISPLAY_NAME)
    val selection = "${MediaStore.Images.Media.RELATIVE_PATH} = ?"
    val selectionArgs = arrayOf(relativePath)
    val cursor =
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null)
    val exists = cursor?.moveToFirst() ?: false
    cursor?.close()
    return exists
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
      composable(SharedScreens.CameraScreen.title) { Camera(navigationController = navController) }
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

  class DefectiveFakeFileRepository : FileStorageRepository {
    override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
      return Result.failure(Exception("Upload failed"))
    }

    override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
      return Result.failure(Exception("Delete failed"))
    }

    override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
      return Result.failure(Exception("No metadata"))
    }
  }
}
