// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.screen

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.downloads.AppDatabase
import ch.eureka.eurekapp.model.tasks.ViewTaskViewModel
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreen
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ViewTaskScreenOfflineTest {

  @get:Rule val composeTestRule = createComposeRule()

  private var lastViewModel: ViewTaskViewModel? = null
  private var testUserId: String = ""
  private lateinit var context: android.content.Context
  private lateinit var mockConnectivityObserver: MockConnectivityObserver
  private lateinit var database: AppDatabase

  private val projectRepository: ch.eureka.eurekapp.model.data.project.ProjectRepository =
      ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository(
          firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

  private val taskRepository: TaskRepository =
      FirestoreTaskRepository(
          firestore = FirebaseEmulator.firestore,
          auth = FirebaseEmulator.auth,
          projectRepository = projectRepository)

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
    mockConnectivityObserver = MockConnectivityObserver(context)

    // Clear database before each test
    database = AppDatabase.getDatabase(context)
    database.clearAllTables()
  }

  @After
  fun tearDown() = runBlocking {
    lastViewModel = null
    database.clearAllTables()
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

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
  fun viewTaskScreenOffline_displaysMessage() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"

      setupTestProject(projectId)
      setupTestTask(projectId, taskId, title = "Offline Task")

      mockConnectivityObserver.setConnected(false)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              AppDatabase.getDatabase(context).downloadedFileDao(),
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
    }
  }

  @Test
  fun viewTaskScreenOffline_stillViewsTaskDetails() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"

      setupTestProject(projectId)
      setupTestTask(
          projectId,
          taskId,
          title = "Offline View Task",
          description = "Can view offline",
          dueDate = "15/12/2024",
          status = TaskStatus.IN_PROGRESS)

      mockConnectivityObserver.setConnected(false)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              AppDatabase.getDatabase(context).downloadedFileDao(),
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Verify task details are still visible
      composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
      composeTestRule.onNodeWithTag(CommonTaskTestTags.DESCRIPTION).assertIsDisplayed()
      composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).assertIsDisplayed()
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.TASK_STATUS).assertIsDisplayed()
    }
  }

  @Test
  fun viewTaskScreenOffline_buttonDoesNotNavigate() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"

      setupTestProject(projectId)
      setupTestTask(projectId, taskId)

      mockConnectivityObserver.setConnected(false)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              AppDatabase.getDatabase(context).downloadedFileDao(),
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Try to click edit button
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).performClick()

      composeTestRule.waitForIdle()

      // Verify we're still on ViewTaskScreen (no navigation occurred)
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.EDIT_TASK).assertIsDisplayed()
      composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).assertIsDisplayed()
    }
  }

  @Test
  fun viewTaskScreenOffline_attachmentsShowMessage() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"
      val attachmentUrl = "https://example.com/photo.jpg"

      setupTestProject(projectId)
      setupTestTask(projectId, taskId, attachmentUrls = listOf(attachmentUrl))

      mockConnectivityObserver.setConnected(false)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              AppDatabase.getDatabase(context).downloadedFileDao(),
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Verify offline message for attachments
      composeTestRule
          .onNodeWithTag(CommonTaskTestTags.ATTACHMENT_OFFLINE_MESSAGE)
          .assertIsDisplayed()
    }
  }

  @Test
  fun viewTaskScreenOffline_onlineThenOfflineShowsMessage() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"

      setupTestProject(projectId)
      setupTestTask(projectId, taskId, title = "Online Then Offline")

      mockConnectivityObserver.setConnected(true)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              AppDatabase.getDatabase(context).downloadedFileDao(),
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Verify no offline message initially
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.OFFLINE_MESSAGE).assertDoesNotExist()

      mockConnectivityObserver.setConnected(false)

      composeTestRule.waitForIdle()

      // Verify offline message appears
      composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.OFFLINE_MESSAGE).assertIsDisplayed()
    }
  }

  @Test
  fun viewTaskScreenOffline_noDownloadButton() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"
      val attachmentUrl = "https://example.com/document.pdf|document.pdf|application/pdf"

      setupTestProject(projectId)
      setupTestTask(projectId, taskId, attachmentUrls = listOf(attachmentUrl))

      mockConnectivityObserver.setConnected(false)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              AppDatabase.getDatabase(context).downloadedFileDao(),
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Verify download button is not displayed when offline
      composeTestRule
          .onNodeWithTag(ViewTaskScreenTestTags.DOWNLOAD_ALL_ATTACHMENTS)
          .assertDoesNotExist()
    }
  }

  @Test
  fun viewTaskScreenOffline_downloadedAttachmentDisplayedAsLocal() {
    runBlocking {
      val projectId = "project123"
      val taskId = "task123"
      val attachmentUrl = "https://example.com/photo.jpg|photo.jpg|image/jpeg"

      setupTestProject(projectId)
      setupTestTask(projectId, taskId, attachmentUrls = listOf(attachmentUrl))

      // Manually mark the attachment as downloaded in the database
      val dao = AppDatabase.getDatabase(context).downloadedFileDao()
      dao.insert(
          ch.eureka.eurekapp.model.downloads.DownloadedFile(
              url = attachmentUrl.substringBefore("|"),
              localPath = "file:///fake/path/photo.jpg",
              fileName = "photo.jpg"))

      mockConnectivityObserver.setConnected(false)

      val viewModel =
          ViewTaskViewModel(
              projectId,
              taskId,
              dao,
              taskRepository,
              connectivityObserver = mockConnectivityObserver)
      lastViewModel = viewModel

      composeTestRule.setContent {
        val navController = rememberNavController()
        ViewTaskScreen(
            projectId = projectId,
            taskId = taskId,
            navigationController = navController,
            viewTaskViewModel = viewModel)
      }

      composeTestRule.waitForIdle()

      // Verify attachment is displayed (as Local when downloaded and offline)
      composeTestRule.onAllNodesWithTag(CommonTaskTestTags.ATTACHMENT).assertCountEquals(1)
    }
  }
}
