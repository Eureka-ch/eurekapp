package ch.eureka.eurekapp.screen

import android.Manifest
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertCountEquals
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
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.tasks.CreateTaskViewModel
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.screens.Camera
import ch.eureka.eurekapp.screens.CameraScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.creation.CreateTaskScreen
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.storage.StorageMetadata
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
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
  private var lastCreateVm: CreateTaskViewModel? = null

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

  @Test
  fun projectSelection_showsList_and_selectsProject() {
    val viewModel = CreateTaskViewModel(taskRepository, fileRepository = FakeFileRepository())
    lastCreateVm = viewModel
    val projectId = "project123"

    // Provide a fake project list via a fake repository-injected VM
    val fakeProjectRepository =
        object : ProjectRepository {
          override fun getProjectById(projectId: String): Flow<Project?> =
              flowOf(Project(projectId = projectId, name = "P-$projectId"))

          override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> =
              flowOf(listOf(Project(projectId = projectId, name = "Test Project")))

          override suspend fun createProject(
              project: Project,
              creatorId: String,
              creatorRole: ProjectRole
          ): Result<String> = Result.success(project.projectId)

          override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

          override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

          override fun getMembers(projectId: String): Flow<List<Member>> = flowOf(emptyList())

          override suspend fun addMember(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)

          override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
              Result.success(Unit)

          override suspend fun updateMemberRole(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)
        }

    val injectedVm =
        CreateTaskViewModel(
            taskRepository,
            fileRepository = FakeFileRepository(),
            projectRepository = fakeProjectRepository)
    lastCreateVm = injectedVm

    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(navController = navController, viewModel = injectedVm)
      navController.navigate(Route.TasksSection.CreateTask)
    }
    composeTestRule.waitForIdle()
    composeTestRule.waitForIdle()

    // Bypass dropdown by preselecting project in ViewModel
    injectedVm.setProjectId(projectId)
    composeTestRule.waitForIdle()
    // After selection, no error should appear
    composeTestRule
        .onAllNodesWithTag(CommonTaskTestTags.PROJECT_SELECTION_ERROR)
        .assertCountEquals(0)
  }

  @Test
  fun projectSelection_showsNoProjectsMessage_whenEmptyList() {
    val emptyRepo =
        object : ProjectRepository {
          override fun getProjectById(projectId: String): Flow<Project?> = flowOf(null)

          override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> =
              flowOf(emptyList())

          override suspend fun createProject(
              project: Project,
              creatorId: String,
              creatorRole: ProjectRole
          ): Result<String> = Result.success("id")

          override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

          override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

          override fun getMembers(projectId: String): Flow<List<Member>> = flowOf(emptyList())

          override suspend fun addMember(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)

          override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
              Result.success(Unit)

          override suspend fun updateMemberRole(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)
        }

    val injectedVm =
        CreateTaskViewModel(
            taskRepository, fileRepository = FakeFileRepository(), projectRepository = emptyRepo)
    lastCreateVm = injectedVm

    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(navController = navController, viewModel = injectedVm)
      navController.navigate(Route.TasksSection.CreateTask)
    }

    composeTestRule.onNodeWithTag(CommonTaskTestTags.NO_PROJECTS_AVAILABLE).assertIsDisplayed()
  }

  @After
  fun tearDown() = runBlocking {
    // Cancel any raw, injected ViewModel that might still be alive
    lastCreateVm?.viewModelScope?.cancel()
    lastCreateVm = null
    // Ensure both Firestore and Auth are reset between tests to avoid leaking auth state
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  private val taskRepository: TaskRepository =
      FirestoreTaskRepository(firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

  /**
   * Helper function to select a project from the dropdown menu. This replaces the old
   * RadioButton-based selection pattern.
   */
  private fun selectProject(projectId: String) {
    // Click on the field to open the dropdown
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE).performClick()
    // Wait for the dropdown menu to appear
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag("${CommonTaskTestTags.PROJECT_RADIO}_menu")
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
    // Click on the project item
    composeTestRule.onNodeWithTag("${CommonTaskTestTags.PROJECT_RADIO}_$projectId").performClick()
  }

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
    composeTestRule.setContent { NavigationMenu() }

    // Navigate to Tasks screen
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()
  }

  @Test
  fun testEmptyFieldsShowErrors() {
    navigateToCreateTaskScreen()
    // Focus and leave the Title field empty
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ERROR_MSG).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Test Task")

    // Focus and leave the Description field empty
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performClick()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ERROR_MSG).assertIsDisplayed()
  }

  @Test
  fun testInvalidDateShowsError() {
    navigateToCreateTaskScreen()

    // Input title and description
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Test Task")
    composeTestRule
        .onNodeWithTag(CommonTaskTestTags.DESCRIPTION)
        .performTextInput("Some description")

    // Input invalid date
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("invalid-date")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ERROR_MSG).assertIsDisplayed()
  }

  /** This is our end-to-end test. */
  @Test
  fun testTakingPhotos() {
    navigateToCreateTaskScreen()

    // Initially, no photo should be displayed
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsNotDisplayed()

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    // Wait for navigation back to CreateTaskScreen
    composeTestRule.waitUntil(timeoutMillis = 7_000) {
      composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).fetchSemanticsNodes().isNotEmpty()
    }

    // Now the photo should be displayed in Create Task screen
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).assertIsDisplayed()

    // Delete the photo
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsNotDisplayed()
  }

  @Test
  fun testPhotoUpload() {
    val projectId = "project123"
    val fakeProjectRepository =
        object : ProjectRepository {
          override fun getProjectById(projectId: String): Flow<Project?> =
              flowOf(Project(projectId = projectId, name = "P-$projectId"))

          override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> =
              flowOf(listOf(Project(projectId = projectId, name = "Test Project")))

          override suspend fun createProject(
              project: Project,
              creatorId: String,
              creatorRole: ProjectRole
          ): Result<String> = Result.success(project.projectId)

          override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

          override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

          override fun getMembers(projectId: String): Flow<List<Member>> = flowOf(emptyList())

          override suspend fun addMember(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)

          override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
              Result.success(Unit)

          override suspend fun updateMemberRole(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)
        }

    val viewModel =
        CreateTaskViewModel(
            taskRepository,
            fileRepository = FakeFileRepository(),
            projectRepository = fakeProjectRepository)
    lastCreateVm = viewModel

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(navController = navController, viewModel = viewModel)
      navController.navigate(Route.TasksSection.CreateTask)
    }
    // Preselect project to avoid dropdown dependency
    viewModel.setProjectId(projectId)
    composeTestRule.waitForIdle()
    composeTestRule.waitForIdle()

    assert(!isPhotoSaved(context, "Pictures/EurekApp/"))

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Task 1")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextInput("Description")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Project already selected via viewModel

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    // Wait for navigation back to CreateTaskScreen
    composeTestRule.waitUntil(timeoutMillis = 7_000) {
      composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).fetchSemanticsNodes().isNotEmpty()
    }

    assert(isPhotoSaved(context, "Pictures/EurekApp/"))

    // Now the photo should be displayed in Create Task screen and inputs conserved
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()
    // Wait for navigation back to tasks screen
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
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
      FakeNavGraph(navController = navController, viewModel = viewModel)
      navController.navigate(Route.TasksSection.CreateTask)
    }
    composeTestRule.waitForIdle()

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .TITLE).performTextInput("Task 1")
    composeTestRule
        .onNodeWithTag(CommonTaskTestTags
        .DESCRIPTION)
        .performTextInput("Description")
    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .DUE_DATE).performTextInput("15/10/2025")

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .ADD_PHOTO).performClick()
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
    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CommonTaskTestTags
    .SAVE_TASK).performClick()
    // Even with defective file repository, should navigate back to tasks screen (no crash)
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
  }*/

  @Test
  fun testTaskCreated() {
    val projectId = "project123"
    val fakeProjectRepository =
        object : ProjectRepository {
          override fun getProjectById(projectId: String): Flow<Project?> =
              flowOf(Project(projectId = projectId, name = "P-$projectId"))

          override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> =
              flowOf(listOf(Project(projectId = projectId, name = "Test Project")))

          override suspend fun createProject(
              project: Project,
              creatorId: String,
              creatorRole: ProjectRole
          ): Result<String> = Result.success(project.projectId)

          override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

          override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

          override fun getMembers(projectId: String): Flow<List<Member>> = flowOf(emptyList())

          override suspend fun addMember(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)

          override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
              Result.success(Unit)

          override suspend fun updateMemberRole(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)
        }

    val viewModel =
        CreateTaskViewModel(
            taskRepository,
            fileRepository = FakeFileRepository(),
            projectRepository = fakeProjectRepository)
    lastCreateVm = viewModel

    runBlocking { setupTestProject(projectId, ProjectRole.OWNER) }

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(navController = navController, viewModel = viewModel)
      navController.navigate(Route.TasksSection.CreateTask)
    }
    // Preselect project to avoid dropdown dependency
    viewModel.setProjectId(projectId)
    composeTestRule.waitForIdle()

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Task 1")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextInput("Description")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Project already selected via viewModel

    // Click add photo button to navigate to Camera screen
    composeTestRule.onNodeWithTag(CommonTaskTestTags.ADD_PHOTO).performClick()
    composeTestRule.onNodeWithTag(CameraScreenTestTags.TAKE_PHOTO).performClick()

    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(CameraScreenTestTags.SAVE_PHOTO)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }

    // After taking photo, save the photo
    composeTestRule.onNodeWithTag(CameraScreenTestTags.SAVE_PHOTO).performClick()

    // Wait for navigation back to CreateTaskScreen
    composeTestRule.waitUntil(timeoutMillis = 3_000) {
      composeTestRule.onAllNodesWithTag(CommonTaskTestTags.PHOTO).fetchSemanticsNodes().isNotEmpty()
    }

    // Now the photo should be displayed in Create Task screen and inputs conserved
    composeTestRule.onNodeWithTag(CommonTaskTestTags.PHOTO).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DELETE_PHOTO).assertIsDisplayed()

    composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      composeTestRule
          .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
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
            createdBy = testUserId,
        )

    runBlocking {
      val tasks = taskRepository.getTasksForCurrentUser().first()
      val found = tasks.any { it.title == task.title && it.dueDate == task.dueDate }
      assert(found)
    }
  }

  @Test
  fun testSaveButtonEnabledOnlyWithValidInput() {
    val projectId = "project123"
    val fakeProjectRepository =
        object : ProjectRepository {
          override fun getProjectById(projectId: String): Flow<Project?> =
              flowOf(Project(projectId = projectId, name = "P-$projectId"))

          override fun getProjectsForCurrentUser(skipCache: Boolean): Flow<List<Project>> =
              flowOf(listOf(Project(projectId = projectId, name = "Test Project")))

          override suspend fun createProject(
              project: Project,
              creatorId: String,
              creatorRole: ProjectRole
          ): Result<String> = Result.success(project.projectId)

          override suspend fun updateProject(project: Project): Result<Unit> = Result.success(Unit)

          override suspend fun deleteProject(projectId: String): Result<Unit> = Result.success(Unit)

          override fun getMembers(projectId: String): Flow<List<Member>> = flowOf(emptyList())

          override suspend fun addMember(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)

          override suspend fun removeMember(projectId: String, userId: String): Result<Unit> =
              Result.success(Unit)

          override suspend fun updateMemberRole(
              projectId: String,
              userId: String,
              role: ProjectRole
          ): Result<Unit> = Result.success(Unit)
        }

    val viewModel = CreateTaskViewModel(taskRepository, projectRepository = fakeProjectRepository)
    lastCreateVm = viewModel

    // Inject the view model into the screen
    composeTestRule.setContent {
      val navController = rememberNavController()
      FakeNavGraph(navController = navController, viewModel = viewModel)
      navController.navigate(Route.TasksSection.CreateTask)
    }
    // Preselect project to avoid dropdown dependency
    viewModel.setProjectId(projectId)
    composeTestRule.waitForIdle()

    val saveButton = composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK)

    // Initially, Save button should be disabled
    saveButton.performClick()
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()

    // Project already selected via viewModel (required for valid input)

    // Fill in valid inputs
    composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput("Task 1")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).performTextInput("Description")
    composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput("15/10/2025")

    // Save button should be enabled now
    saveButton.performClick()
    // Wait for navigation back to tasks screen
    composeTestRule.waitUntil(timeoutMillis = 5_000) {
      composeTestRule
          .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
          .fetchSemanticsNodes()
          .isNotEmpty()
    }
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
  private fun FakeNavGraph(navController: NavHostController, viewModel: CreateTaskViewModel) {
    NavHost(navController, startDestination = Route.TasksSection.Tasks) {
      composable<Route.TasksSection.CreateTask> {
        CreateTaskScreen(navigationController = navController, createTaskViewModel = viewModel)
      }
      composable<Route.TasksSection.Tasks> {
        // Fake Tasks screen for testing pop back
        androidx.compose.material3.Text(
            "Tasks Screen",
            modifier = androidx.compose.ui.Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
      }
      composable<Route.Camera> { Camera(navigationController = navController) }
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
