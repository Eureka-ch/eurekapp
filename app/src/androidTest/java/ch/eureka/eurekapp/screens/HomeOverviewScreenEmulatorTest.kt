package ch.eureka.eurekapp.screens

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.FirestoreTaskRepository
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.navigation.Route
import ch.eureka.eurekapp.ui.meeting.MeetingScreenTestTags
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.MockConnectivityObserver
import com.google.firebase.Timestamp
import com.kaspersky.kaspresso.testcases.api.testcase.TestCase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// Part of this code and documentation were generated with the help of AI (ChatGPT 5.1).

/**
 * Emulator-based integration tests for HomeOverviewScreen.
 *
 * These tests verify that the HomeOverview screen correctly displays real data from Firebase
 * Emulator and handles navigation properly.
 */
class HomeOverviewScreenEmulatorTest : TestCase() {

  @get:Rule val composeTestRule = createComposeRule()

  private var testUserId: String = ""
  private lateinit var context: android.content.Context
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
      ConnectivityObserverProvider.initialize(context)
    }
  }

  @After
  fun tearDown() = runBlocking {
    FirebaseEmulator.clearFirestoreEmulator()
    FirebaseEmulator.clearAuthEmulator()
  }

  private suspend fun setupTestUser(userId: String, displayName: String = "Test User") {
    val user =
        User(
            uid = userId,
            displayName = displayName,
            email = "test@example.com",
            photoUrl = "",
            lastActive = Timestamp.now())
    FirebaseEmulator.firestore.collection("users").document(userId).set(user).await()
  }

  private suspend fun setupTestProject(
      projectId: String,
      name: String = "Test Project",
      role: ProjectRole = ProjectRole.OWNER
  ) {
    val projectRef = FirebaseEmulator.firestore.collection("projects").document(projectId)

    val project =
        Project(
            projectId = projectId,
            name = name,
            description = "Test project description",
            status = ProjectStatus.OPEN,
            createdBy = testUserId,
            memberIds = listOf(testUserId),
            lastUpdated = Timestamp.now())
    projectRef.set(project).await()

    val member = Member(userId = testUserId, role = role)
    val memberRef = projectRef.collection("members").document(testUserId)
    memberRef.set(member).await()
  }

  private suspend fun setupTestTask(
      projectId: String,
      taskId: String,
      title: String = "Test Task",
      dueDate: Timestamp = Timestamp.now(),
      status: TaskStatus = TaskStatus.TODO
  ) {
    val projectRepository: ch.eureka.eurekapp.model.data.project.ProjectRepository =
        ch.eureka.eurekapp.model.data.project.FirestoreProjectRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
    val taskRepository =
        FirestoreTaskRepository(
            firestore = FirebaseEmulator.firestore,
            auth = FirebaseEmulator.auth,
            projectRepository = projectRepository)
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = title,
            description = "Test task description",
            assignedUserIds = listOf(testUserId),
            dueDate = dueDate,
            createdBy = testUserId,
            status = status)
    taskRepository.updateTask(task).getOrThrow()
  }

  private suspend fun setupTestMeeting(
      projectId: String,
      meetingId: String,
      title: String = "Test Meeting",
      datetime: Timestamp = Timestamp.now(),
      status: MeetingStatus = MeetingStatus.SCHEDULED
  ) {
    val meetingRef =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("meetings")
            .document(meetingId)

    // Format must be null for OPEN_TO_VOTES, and set for other statuses
    val format = if (status == MeetingStatus.OPEN_TO_VOTES) null else MeetingFormat.VIRTUAL
    val link =
        if (status == MeetingStatus.OPEN_TO_VOTES || format == null) null
        else "https://meet.google.com/test"

    val meeting =
        Meeting(
            meetingID = meetingId,
            projectId = projectId,
            title = title,
            status = status,
            datetime = datetime,
            format = format,
            link = link,
            duration = 60,
            participantIds = listOf(testUserId),
            createdBy = testUserId)
    meetingRef.set(meeting).await()

    // Add participant
    val participantRef = meetingRef.collection("participants").document(testUserId)
    val participant =
        ch.eureka.eurekapp.model.data.meeting.Participant(
            userId = testUserId, role = MeetingRole.HOST)
    participantRef.set(participant).await()
  }

  @Test
  fun homeOverview_displaysRealDataFromFirebase() {
    runBlocking {
      // Setup test data
      setupTestUser(testUserId, displayName = "Eureka User")
      val projectId1 = "project-1"
      val projectId2 = "project-2"
      setupTestProject(projectId1, name = "Project Alpha")
      setupTestProject(projectId2, name = "Project Beta")

      // Create tasks with different due dates
      val tomorrow = Timestamp(java.util.Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
      val dayAfterTomorrow =
          Timestamp(java.util.Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000))
      setupTestTask(projectId1, "task-1", "Upcoming Task 1", dueDate = tomorrow)
      setupTestTask(projectId1, "task-2", "Upcoming Task 2", dueDate = dayAfterTomorrow)
      setupTestTask(
          projectId1,
          "task-3",
          "Upcoming Task 3",
          dueDate = Timestamp(java.util.Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)))

      // Create meetings
      val meetingTime1 = Timestamp(java.util.Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
      val meetingTime2 =
          Timestamp(java.util.Date(System.currentTimeMillis() + 2 * 24 * 60 * 60 * 1000))
      setupTestMeeting(projectId1, "meeting-1", "Upcoming Meeting 1", datetime = meetingTime1)
      setupTestMeeting(projectId2, "meeting-2", "Upcoming Meeting 2", datetime = meetingTime2)

      // Compose the screen
      composeTestRule.setContent {
        val navController = rememberNavController()
        NavHost(navController, startDestination = Route.HomeOverview) {
          composable<Route.HomeOverview> {
            HomeOverviewScreen(
                actions =
                    HomeOverviewActions(
                        onOpenProjects = { navController.navigate(Route.ProjectSelection) },
                        onOpenTasks = { navController.navigate(Route.TasksSection.Tasks) },
                        onOpenMeetings = {
                          navController.navigate(Route.MeetingsSection.Meetings)
                        }))
          }
          composable<Route.ProjectSelection> {
            androidx.compose.material3.Text(
                "Project Selection", modifier = Modifier.testTag("projectSelectionScreen"))
          }
          composable<Route.TasksSection.Tasks> {
            androidx.compose.material3.Text(
                "Tasks Screen", modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
          }
          composable<Route.MeetingsSection.Meetings> {
            androidx.compose.material3.Text(
                "Meetings Screen",
                modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_SCREEN))
          }
        }
      }

      composeTestRule.waitForIdle()

      // Wait for data to load from Firestore
      composeTestRule.waitUntil(timeoutMillis = 10000) {
        try {
          composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertExists()
          composeTestRule.onNodeWithText("Hello Eureka User").assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      // Verify greeting displays user name
      composeTestRule.onNodeWithText("Hello Eureka User").assertIsDisplayed()

      val list = composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN)

      // Scroll to CTA buttons and ensure they exist (static layout check)
      list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_TASKS))
      composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).assertExists()
      list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_MEETINGS))
      composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_MEETINGS).assertExists()
      list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_PROJECTS))
      composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_PROJECTS).assertExists()
    }
  }

  @Test
  fun homeOverview_navigationButtonsWorkCorrectly() {
    runBlocking {
      // Setup minimal test data
      setupTestUser(testUserId, displayName = "Test User")
      val projectId = "project-1"
      setupTestProject(projectId, name = "Test Project")

      var tasksNavigated = false
      var meetingsNavigated = false
      var projectsNavigated = false

      // Compose the screen with navigation (single setContent call)
      lateinit var navController: NavHostController
      composeTestRule.setContent {
        navController = rememberNavController()
        NavHost(navController, startDestination = Route.HomeOverview) {
          composable<Route.HomeOverview> {
            HomeOverviewScreen(
                actions =
                    HomeOverviewActions(
                        onOpenProjects = {
                          navController.navigate(Route.ProjectSelection)
                          projectsNavigated = true
                        },
                        onOpenTasks = {
                          navController.navigate(Route.TasksSection.Tasks)
                          tasksNavigated = true
                        },
                        onOpenMeetings = {
                          navController.navigate(Route.MeetingsSection.Meetings)
                          meetingsNavigated = true
                        }))
          }
          composable<Route.ProjectSelection> {
            androidx.compose.material3.Text(
                "Project Selection", modifier = Modifier.testTag("projectSelectionScreen"))
          }
          composable<Route.TasksSection.Tasks> {
            androidx.compose.material3.Text(
                "Tasks Screen", modifier = Modifier.testTag(TasksScreenTestTags.TASKS_SCREEN_TEXT))
          }
          composable<Route.MeetingsSection.Meetings> {
            androidx.compose.material3.Text(
                "Meetings Screen",
                modifier = Modifier.testTag(MeetingScreenTestTags.MEETING_SCREEN))
          }
        }
      }

      composeTestRule.waitForIdle()

      // Wait for home screen to load
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      // Test navigation to Tasks screen
      composeTestRule.onNodeWithText("View all").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.waitUntil(timeoutMillis = 3000) {
        try {
          composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }
      composeTestRule.onNodeWithTag(TasksScreenTestTags.TASKS_SCREEN_TEXT).assertIsDisplayed()
      org.junit.Assert.assertTrue("Tasks navigation callback should be triggered", tasksNavigated)

      // Navigate back to home using popBackStack on UI thread
      composeTestRule.runOnUiThread { navController.popBackStack() }
      composeTestRule.waitForIdle()
      composeTestRule.waitUntil(timeoutMillis = 3000) {
        try {
          composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      // Test navigation to Meetings screen
      composeTestRule.onNodeWithText("Open meetings").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.waitUntil(timeoutMillis = 3000) {
        try {
          composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }
      composeTestRule.onNodeWithTag(MeetingScreenTestTags.MEETING_SCREEN).assertIsDisplayed()
      org.junit.Assert.assertTrue(
          "Meetings navigation callback should be triggered", meetingsNavigated)

      // Navigate back to home again
      composeTestRule.runOnUiThread { navController.popBackStack() }
      composeTestRule.waitForIdle()
      composeTestRule.waitUntil(timeoutMillis = 3000) {
        try {
          composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN).assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }

      // Test navigation to Projects screen - wait for button to be available
      composeTestRule.waitUntil(timeoutMillis = 5000) {
        try {
          composeTestRule.onNodeWithText("Browse projects").assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }
      composeTestRule.onNodeWithText("Browse projects").performClick()
      composeTestRule.waitForIdle()
      composeTestRule.waitUntil(timeoutMillis = 3000) {
        try {
          composeTestRule.onNodeWithTag("projectSelectionScreen").assertExists()
          true
        } catch (e: AssertionError) {
          false
        }
      }
      composeTestRule.onNodeWithTag("projectSelectionScreen").assertIsDisplayed()
      org.junit.Assert.assertTrue(
          "Projects navigation callback should be triggered", projectsNavigated)
    }
  }
}
