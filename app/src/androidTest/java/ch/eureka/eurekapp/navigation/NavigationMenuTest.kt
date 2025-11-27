package ch.eureka.eurekapp.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.screens.IdeasScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreenTestTags
import ch.eureka.eurekapp.ui.conversation.ConversationListScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.ui.notes.SelfNotesScreenTestTags
import ch.eureka.eurekapp.ui.profile.ProfileScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.Timestamp
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Portions of this code were generated with the help of Grok.

/*
Co-author: GPT-5 Codex
*/

class NavigationMenuTest : TestCase() {
  @get:Rule
  val permissionRule: GrantPermissionRule =
      GrantPermissionRule.grant(
          android.Manifest.permission.READ_CALENDAR, android.Manifest.permission.WRITE_CALENDAR)
  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    val authResult = FirebaseEmulator.auth.signInAnonymously().await()
    if (authResult.user == null) {
      throw IllegalStateException("Failed to sign in")
    }

    if (FirebaseEmulator.auth.currentUser == null) {
      throw IllegalStateException("Auth state not properly established after sign-in")
    }

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
    RepositoriesProvider.initialize(context)
  }

  @Test
  fun testNavigationBottomBarComponent() {
    composeTestRule.setContent {
      val navigationController = rememberNavController()
      BottomBarNavigationComponent(navigationController)
    }

    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.CONVERSATIONS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun testAllPages() {
    composeTestRule.setContent { NavigationMenu() }

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.IDEAS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.SCREEN).assertIsDisplayed()

    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.CONVERSATIONS_SCREEN_BUTTON)
        .performClick()
    composeTestRule.onNodeWithTag(ConversationListScreenTestTags.TITLE).assertIsDisplayed()
  }

  @Test
  fun testNavigateToTaskDependenciesScreen() {
    runBlocking {
      val testUserId =
          FirebaseEmulator.auth.currentUser?.uid ?: throw IllegalStateException("No user")
      val projectId = "test-project-id"
      val taskId = "test-task-id"

      // Setup minimal test project
      val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)
      projectRef
          .set(
              Project(
                  projectId = projectId,
                  name = "Test Project",
                  description = "Test",
                  status = ProjectStatus.OPEN,
                  createdBy = testUserId,
                  memberIds = listOf(testUserId)))
          .await()
      projectRef
          .collection("members")
          .document(testUserId)
          .set(Member(userId = testUserId, role = ProjectRole.OWNER))
          .await()

      // Setup minimal test task
      val taskRepository: TaskRepository =
          FirestoreTaskRepository(
              firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
      val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("15/10/2025")!!
      taskRepository
          .updateTask(
              Task(
                  taskID = taskId,
                  projectId = projectId,
                  title = "Test Task",
                  description = "Test",
                  assignedUserIds = listOf(testUserId),
                  dueDate = Timestamp(date),
                  attachmentUrls = emptyList(),
                  createdBy = testUserId,
                  status = TaskStatus.TODO))
          .getOrThrow()
    }

    // Use actual NavigationMenu to cover the real Navigation.kt lines 233-239
    composeTestRule.setContent { NavigationMenu() }
    composeTestRule.waitForIdle()

    // Navigate through UI to reach TaskDependenciesScreen
    // This will execute the actual composable in Navigation.kt
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Wait for task card to appear and click it
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_CARD).assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_CARD).performClick()
    composeTestRule.waitForIdle()

    // Click the dependencies button to navigate to TaskDependenciesScreen
    // This triggers the actual composable in Navigation.kt lines 233-239
    composeTestRule.onNodeWithTag(ViewTaskScreenTestTags.VIEW_DEPENDENCIES).performClick()
    composeTestRule.waitForIdle()

    // Verify the screen is displayed (this confirms the composable executed)
    composeTestRule.onNodeWithTag("back_button_dependencies").assertIsDisplayed()
  }
}
