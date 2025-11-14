package ch.eureka.eurekapp.e2e

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.navigation.BottomBarNavigationTestTags
import ch.eureka.eurekapp.navigation.NavigationMenu
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.screens.subscreens.tasks.CommonTaskTestTags
import ch.eureka.eurekapp.utils.FakeJwtGenerator
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.auth.GoogleAuthProvider
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * End-to-end test for the task creation workflow.
 *
 * Test coverage: The e2e test includes the user flows:
 * - Authentication: user signs in with Google
 * - Navigation: user navigates to the Tasks tab
 * - Task Creation: user creates a new task with title, description, and due date
 * - Verification: confirms the task appears in the task list
 */
@OptIn(ExperimentalTestApi::class)
@RunWith(AndroidJUnit4::class)
class TaskCreationE2ETest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  private lateinit var testUserId: String
  private val testUserName = "Test User"
  private val testUserEmail = "testuser@example.com"

  @Before
  fun setup() = runBlocking {
    if (!FirebaseEmulator.isRunning) {
      throw IllegalStateException("Firebase Emulator must be running for tests")
    }

    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()

    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @After
  fun tearDown() = runBlocking {
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  @Test
  fun taskCreationE2E_userSignsInNavigatesToTasksCreatesTaskAndVerifiesIt() {
    runBlocking {
      // Step 1: Authentication - User signs in with Google
      val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(testUserName, testUserEmail)
      val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
      val authResult = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await()
      testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")

      // Create a test project for the user
      val projectId = "test-project-e2e"
      setupTestProject(projectId, testUserId)

      // Set up the navigation menu after authentication
      composeTestRule.setContent { NavigationMenu() }

      // Step 2: Navigation - User navigates to the Tasks tab
      composeTestRule.waitForIdle()

      // Wait for navigation to complete and Tasks button to be available
      composeTestRule.waitUntilExactlyOneExists(hasText("Tasks"), timeoutMillis = 10_000)

      composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()

      // Verify we're on the Tasks screen
      composeTestRule.waitUntilExactlyOneExists(hasText("+ New Task"), timeoutMillis = 10_000)
      composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()

      // Step 3: Task Creation - User creates a new task
      composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()

      // Wait for Create Task screen to load
      composeTestRule.waitUntilExactlyOneExists(hasText("Create Task"), timeoutMillis = 10_000)

      // Fill in task details
      val taskTitle = "E2E Test Task"
      val taskDescription = "This is an end-to-end test task"
      val taskDueDate = "31/12/2025"

      composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput(taskTitle)
      composeTestRule
          .onNodeWithTag(CommonTaskTestTags.DESCRIPTION)
          .performTextInput(taskDescription)
      composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput(taskDueDate)

      // Select project - open dropdown and select the test project
      composeTestRule.onNodeWithTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE).performClick()
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule
            .onAllNodesWithTag("${CommonTaskTestTags.PROJECT_RADIO}_menu")
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      composeTestRule.onNodeWithTag("${CommonTaskTestTags.PROJECT_RADIO}_$projectId").performClick()

      // Save the task
      composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()

      // Wait for navigation back to Tasks screen
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Step 4: Verification - Confirm the task appears in the task list
      composeTestRule.waitForIdle()

      // Wait for the task to appear in the list
      composeTestRule.waitUntilExactlyOneExists(hasText(taskTitle), timeoutMillis = 10_000)

      // Scroll to the task if needed
      val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)
      taskList.performScrollToNode(hasText(taskTitle))

      // Verify the task is displayed with correct information
      composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()

      // Verify task count has increased
      composeTestRule.onNodeWithText("1 tasks", substring = true).assertIsDisplayed()
    }
  }

  @Test
  fun taskCreationE2E_alreadySignedInUserCreatesTask() {
    runBlocking {
      // Pre-authenticate user
      val fakeIdToken = FakeJwtGenerator.createFakeGoogleIdToken(testUserName, testUserEmail)
      val firebaseCred = GoogleAuthProvider.getCredential(fakeIdToken, null)
      val authResult = FirebaseEmulator.auth.signInWithCredential(firebaseCred).await()
      testUserId = authResult.user?.uid ?: throw IllegalStateException("Failed to sign in")

      // Create a test project for the user
      val projectId = "test-project-e2e-2"
      setupTestProject(projectId, testUserId)

      composeTestRule.setContent { NavigationMenu() }

      // Should automatically navigate past sign-in screen since user is already authenticated
      composeTestRule.waitForIdle()

      // Navigate to Tasks tab
      composeTestRule.waitUntilExactlyOneExists(hasText("Tasks"), timeoutMillis = 10_000)
      composeTestRule.onNodeWithTag(BottomBarNavigationTestTags.TASKS_SCREEN_BUTTON).performClick()

      // Wait for Tasks screen to load
      composeTestRule.waitUntilExactlyOneExists(hasText("+ New Task"), timeoutMillis = 10_000)

      // Create a new task
      composeTestRule.onNodeWithTag(TasksScreenTestTags.CREATE_TASK_BUTTON).performClick()

      // Wait for Create Task screen
      composeTestRule.waitUntilExactlyOneExists(hasText("Create Task"), timeoutMillis = 10_000)

      // Fill in task details
      val taskTitle = "Second E2E Task"
      val taskDescription = "Another test task"
      val taskDueDate = "15/06/2026"

      composeTestRule.onNodeWithTag(CommonTaskTestTags.TITLE).performTextInput(taskTitle)
      composeTestRule
          .onNodeWithTag(CommonTaskTestTags.DESCRIPTION)
          .performTextInput(taskDescription)
      composeTestRule.onNodeWithTag(CommonTaskTestTags.DUE_DATE).performTextInput(taskDueDate)

      // Select project
      composeTestRule.onNodeWithTag(CommonTaskTestTags.PROJECT_SELECTION_TITLE).performClick()
      composeTestRule.waitUntil(timeoutMillis = 5_000) {
        composeTestRule
            .onAllNodesWithTag("${CommonTaskTestTags.PROJECT_RADIO}_menu")
            .fetchSemanticsNodes()
            .isNotEmpty()
      }
      composeTestRule.onNodeWithTag("${CommonTaskTestTags.PROJECT_RADIO}_$projectId").performClick()

      // Save the task
      composeTestRule.onNodeWithTag(CommonTaskTestTags.SAVE_TASK).performClick()

      // Wait for navigation back
      composeTestRule.waitUntil(timeoutMillis = 10_000) {
        composeTestRule
            .onAllNodesWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT)
            .fetchSemanticsNodes()
            .isNotEmpty()
      }

      // Verify the task appears
      composeTestRule.waitUntilExactlyOneExists(hasText(taskTitle), timeoutMillis = 10_000)

      val taskList = composeTestRule.onNodeWithTag(TasksScreenTestTags.TASK_LIST)
      taskList.performScrollToNode(hasText(taskTitle))
      composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
    }
  }

  private suspend fun setupTestProject(projectId: String, userId: String) {
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    // Create the project document
    val project =
        ch.eureka.eurekapp.model.data.project.Project(
            projectId = projectId,
            name = "E2E Test Project",
            description = "Project for end-to-end testing",
            status = ch.eureka.eurekapp.model.data.project.ProjectStatus.OPEN,
            createdBy = userId,
            memberIds = listOf(userId))
    projectRef.set(project).await()

    // Add the test user as a member
    val member =
        ch.eureka.eurekapp.model.data.project.Member(userId = userId, role = ProjectRole.OWNER)
    val memberRef = projectRef.collection("members").document(userId)
    memberRef.set(member).await()
  }
}
