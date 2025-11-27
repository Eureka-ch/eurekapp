package ch.eureka.eurekapp.navigation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskRepository
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.screens.HomeOverviewTestOverrides
import ch.eureka.eurekapp.screens.HomeOverviewTestTags
import ch.eureka.eurekapp.screens.IdeasScreenTestTags
import ch.eureka.eurekapp.screens.OverviewProjectsScreenTestTags
import ch.eureka.eurekapp.screens.ProjectSelectionScreenTestTags
import ch.eureka.eurekapp.screens.SelfNotesScreenTestTags
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.viewing.ViewTaskScreenTestTags
import ch.eureka.eurekapp.ui.home.HomeOverviewUiState
import ch.eureka.eurekapp.ui.meeting.MeetingDetailScreenTestTags
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.ui.profile.ProfileScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
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

  companion object {
    private const val HOME_OVERVIEW_PROJECT_ID = "home-overview-project-id"
    private const val HOME_OVERVIEW_TASK_ID = "home-overview-task-id"
    private const val HOME_OVERVIEW_MEETING_ID = "home-overview-meeting-id"
  }

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
        .onNodeWithTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON)
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

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.IDEAS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(IdeasScreenTestTags.IDEAS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.MEETINGS_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()

    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.NOTES_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(SelfNotesScreenTestTags.SCREEN).assertIsDisplayed()

    // Verify home button navigates back to HomeOverview
    composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON).performClick()
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()
  }

  @Test
  fun testHomeOverviewComposable() {
    runBlocking { seedHomeOverviewData() }

    val overrideState = createHomeOverviewOverrideState()
    HomeOverviewTestOverrides.uiState = overrideState

    try {
      composeTestRule.setContent { NavigationMenu() }
      composeTestRule.waitForIdle()

      fun waitForTag(tag: String) {
        composeTestRule.waitUntil(timeoutMillis = 5000) {
          try {
            composeTestRule.onNodeWithTag(tag).assertExists()
            true
          } catch (e: AssertionError) {
            false
          }
        }
      }

      fun returnHome() {
        composeTestRule
            .onNodeWithTag(BottomBarNavigationTestTags.OVERVIEW_SCREEN_BUTTON)
            .performClick()
        waitForTag(HomeOverviewTestTags.SCREEN)
      }

      waitForTag(HomeOverviewTestTags.SCREEN)

      waitForTag(HomeOverviewTestTags.CTA_TASKS)
      composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).performClick()
      waitForTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
      returnHome()

      waitForTag(HomeOverviewTestTags.CTA_MEETINGS)
      composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_MEETINGS).performClick()
      waitForTag(MeetingScreenTestTags.MEETING_SCREEN)
      returnHome()

      waitForTag(HomeOverviewTestTags.CTA_PROJECTS)
      composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_PROJECTS).performClick()
      waitForTag(ProjectSelectionScreenTestTags.CREATE_PROJECT_BUTTON)
      returnHome()

      val taskTag = "${HomeOverviewTestTags.TASK_ITEM_PREFIX}$HOME_OVERVIEW_TASK_ID"
      val meetingTag = "${HomeOverviewTestTags.MEETING_ITEM_PREFIX}$HOME_OVERVIEW_MEETING_ID"
      val projectLinkTag = "${HomeOverviewTestTags.PROJECT_LINK_PREFIX}$HOME_OVERVIEW_PROJECT_ID"

      waitForTag(taskTag)
      composeTestRule.onNodeWithTag(taskTag).performClick()
      waitForTag(ViewTaskScreenTestTags.VIEW_DEPENDENCIES)
      composeTestRule.onNodeWithTag(CommonTaskTestTags.BACK_BUTTON).performClick()
      returnHome()

      waitForTag(meetingTag)
      composeTestRule.onNodeWithTag(meetingTag).performClick()
      waitForTag(MeetingDetailScreenTestTags.MEETING_DETAIL_SCREEN)
      returnHome()

      // Scroll to project link in LazyColumn and click it
      // First get the LazyColumn node, then scroll to the project link
      val list = composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN)
      list.performScrollToNode(hasTestTag(projectLinkTag))
      composeTestRule.waitForIdle()
      // Now wait for the tag to be visible and clickable
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(projectLinkTag).assertIsDisplayed()
          true
        } catch (e: AssertionError) {
          false
        }
      }
      composeTestRule.onNodeWithTag(projectLinkTag).performClick()
      waitForTag(OverviewProjectsScreenTestTags.OVERVIEW_PROJECTS_SCREEN_TEXT)
    } finally {
      composeTestRule.runOnIdle { HomeOverviewTestOverrides.uiState = null }
    }
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
>>>>>>> 1d6a0c9d (fix: resolve merge conflicts and fix NavigationMenuTest syntax errors)
        true
      } catch (e: AssertionError) {
        false
      }
    }
<<<<<<< HEAD
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertIsDisplayed()

    // Test one callback to ensure callbacks are covered (onOpenTasks)
    // This covers line 178: onOpenTasks = { navigationController.navigate(Route.TasksSection.Tasks)
    // }
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).performClick()
    composeTestRule.waitForIdle()

    // Verify navigation happened (confirms callback executed)
    composeTestRule.waitUntil(timeoutMillis = 5000) {
      try {
        composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertExists()
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

  private suspend fun seedHomeOverviewData() {
    val testUserId =
        FirebaseEmulator.auth.currentUser?.uid ?: throw IllegalStateException("No user")

    val projectRef =
        FirebaseEmulator.firestore.collection("projects").document(HOME_OVERVIEW_PROJECT_ID)
    projectRef
        .set(
            Project(
                projectId = HOME_OVERVIEW_PROJECT_ID,
                name = "Home Overview Project",
                description = "Project for coverage",
                status = ProjectStatus.OPEN,
                createdBy = testUserId,
                memberIds = listOf(testUserId),
                lastUpdated = Timestamp.now()))
        .await()
    projectRef
        .collection("members")
        .document(testUserId)
        .set(Member(userId = testUserId, role = ProjectRole.OWNER))
        .await()

    val taskRepository: TaskRepository =
        FirestoreTaskRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
    val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse("15/10/2025")!!
    taskRepository
        .updateTask(
            Task(
                taskID = HOME_OVERVIEW_TASK_ID,
                projectId = HOME_OVERVIEW_PROJECT_ID,
                title = "Home Overview Task",
                description = "Task for coverage",
                assignedUserIds = listOf(testUserId),
                dueDate = Timestamp(date),
                attachmentUrls = emptyList(),
                createdBy = testUserId,
                status = TaskStatus.TODO))
        .getOrThrow()

    val meeting =
        Meeting(
            meetingID = HOME_OVERVIEW_MEETING_ID,
            projectId = HOME_OVERVIEW_PROJECT_ID,
            title = "Coverage Meeting",
            status = MeetingStatus.SCHEDULED,
            datetime = Timestamp.now(),
            createdBy = testUserId,
            participantIds = listOf(testUserId))

    FirebaseEmulator.firestore
        .collection("projects")
        .document(HOME_OVERVIEW_PROJECT_ID)
        .collection("meetings")
        .document(HOME_OVERVIEW_MEETING_ID)
        .set(meeting)
        .await()
  }

  private fun createHomeOverviewOverrideState(): HomeOverviewUiState {
    return HomeOverviewUiState(
        currentUserName = "Test User",
        upcomingTasks =
            listOf(
                Task(
                    taskID = HOME_OVERVIEW_TASK_ID,
                    projectId = HOME_OVERVIEW_PROJECT_ID,
                    title = "Home Overview Task",
                    description = "Task for coverage",
                    status = TaskStatus.TODO)),
        upcomingMeetings =
            listOf(
                Meeting(
                    meetingID = HOME_OVERVIEW_MEETING_ID,
                    projectId = HOME_OVERVIEW_PROJECT_ID,
                    title = "Coverage Meeting",
                    status = MeetingStatus.SCHEDULED,
                    createdBy = "test-user")),
        recentProjects =
            listOf(
                Project(
                    projectId = HOME_OVERVIEW_PROJECT_ID,
                    name = "Home Overview Project",
                    description = "Project for home overview coverage",
                    status = ProjectStatus.OPEN)),
        isLoading = false,
        isConnected = true)
  }
}
