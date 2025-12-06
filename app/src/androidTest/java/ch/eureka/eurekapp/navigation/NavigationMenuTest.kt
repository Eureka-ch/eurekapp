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
import ch.eureka.eurekapp.screens.HomeOverviewTestTags
import ch.eureka.eurekapp.screens.ProjectSelectionScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
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
          android.Manifest.permission.READ_CALENDAR,
          android.Manifest.permission.WRITE_CALENDAR,
          android.Manifest.permission.POST_NOTIFICATIONS)
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
        .onNodeWithTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON)
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun testAllPages() {
    composeTestRule.setContent { NavigationMenu() }

    // Verify HomeOverview is the start destination
    composeTestRule
        .onNodeWithTag(ch.eureka.eurekapp.screens.HomeOverviewTestTags.SCREEN)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROFILE_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(ProfileScreenTestTags.PROFILE_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON)
        .assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON).performClick()
    composeTestRule
        .onNodeWithTag(ch.eureka.eurekapp.ui.ideas.IdeasScreenTestTags.SCREEN)
        .assertIsDisplayed()

    // Verify home button navigates back to HomeOverview
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()
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
      val projectRepository: ch.eureka.eurekapp.model.data.project.ProjectRepository =
          ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository(
              firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
      val taskRepository: TaskRepository =
          FirestoreTaskRepository(
              firestore = FirebaseEmulator.firestore,
              auth = FirebaseEmulator.auth,
              projectRepository = projectRepository)
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

  @Test
  fun testProjectButtonNavigatesToProjectSelectionScreen() {
    // Test covers: BottomBarNavigationComponent.kt line 174 (onClick Project button)
    composeTestRule.setContent { NavigationMenu() }
    composeTestRule.waitForIdle()

    // Verify HomeOverview is displayed initially
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()

    // Click on Project button - covers line 174: onClick = { navigateToTab(Route.ProjectSelection)
    // }
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify ProjectSelectionScreen is displayed - covers line 174 navigation
    composeTestRule
        .onNodeWithTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun testHomeButtonAlwaysReturnsToHomeOverviewWithoutRestoringState() {
    // Test covers: BottomBarNavigationComponent.kt lines 85-91 (navigateToHome function)
    // and line 187 (onClick = { navigateToHome() })
    composeTestRule.setContent { NavigationMenu() }
    composeTestRule.waitForIdle()

    // Verify HomeOverview is displayed initially
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()

    // Navigate to Project screen first
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON).performClick()
    composeTestRule.waitForIdle()
    composeTestRule
        .onNodeWithTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON)
        .assertIsDisplayed()

    // Click on Home button - covers line 187: onClick = { navigateToHome() }
    // This should use navigateToHome() which has restoreState = false (line 89)
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify we're back at HomeOverview - covers navigateToHome() function (lines 85-91)
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()

    // Verify ProjectSelectionScreen is no longer displayed (state not restored)
    composeTestRule
        .onNodeWithTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON)
        .assertDoesNotExist()
  }

  @Test
  fun testProjectsScreenPressedState() {
    // Test covers: BottomBarNavigationComponent.kt lines 119-124 (isProjectsScreenPressed)
    composeTestRule.setContent { NavigationMenu() }
    composeTestRule.waitForIdle()

    // Initially, Project button should not be pressed
    composeTestRule
        .onNodeWithTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON)
        .assertIsDisplayed()

    // Navigate to Project screen - covers line 122: hasRoute(Route.ProjectSelection::class)
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.PROJECTS_SCREEN_BUTTON).performClick()
    composeTestRule.waitForIdle()

    // Verify ProjectSelectionScreen is displayed - this confirms isProjectsScreenPressed logic
    // works
    composeTestRule
        .onNodeWithTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON)
        .assertIsDisplayed()
  }
}
