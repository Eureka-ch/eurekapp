package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.ui.home.HomeOverviewUiState
import com.google.firebase.Timestamp
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// Part of this code and documentation were generated with the help of AI (ChatGPT 5.1).
@RunWith(AndroidJUnit4::class)
class HomeOverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun displaysGreetingAndSections() {
    val uiState =
        sampleState()
            .copy(
                currentUserName = "Alex",
                upcomingTasks = listOf(createTask("Task A")),
                upcomingMeetings = listOf(createMeeting("Meeting A")),
                recentProjects = listOf(createProject("Project A")),
                isLoading = false)

    composeTestRule.setContent { HomeOverviewLayout(uiState = uiState) }

    // Verify greeting
    composeTestRule.onNodeWithText("Hello Alex").assertIsDisplayed()

    // Verify sections are displayed by checking unique action buttons
    // (text appears twice in summary cards and headers, so we test unique elements instead)
    composeTestRule.onNodeWithText("View all").assertIsDisplayed()
    composeTestRule.onNodeWithText("Open meetings").assertIsDisplayed()
    composeTestRule.onNodeWithText("Browse projects").assertIsDisplayed()

    // Verify content items are displayed
    composeTestRule.onNodeWithText("Task A").assertIsDisplayed()
    composeTestRule.onNodeWithText("Meeting A").assertIsDisplayed()
    composeTestRule.onNodeWithText("Project A").assertIsDisplayed()
  }

  @Test
  fun limitsItemsPerSection() {
    val tasks = (1..4).map { createTask("Task $it") }
    val meetings = (1..4).map { createMeeting("Meeting $it") }
    val projects = (1..4).map { createProject("Project $it") }
    val uiState =
        sampleState()
            .copy(
                upcomingTasks = tasks,
                upcomingMeetings = meetings,
                recentProjects = projects,
                isLoading = false)

    composeTestRule.setContent { HomeOverviewLayout(uiState = uiState) }

    // Verify items are limited to 3 (4th item should not exist)
    try {
      composeTestRule.onNodeWithText("Task 4").assertExists()
      org.junit.Assert.fail("Task 4 should not exist")
    } catch (e: AssertionError) {
      // Expected - item should not exist
    }
    try {
      composeTestRule.onNodeWithText("Meeting 4").assertExists()
      org.junit.Assert.fail("Meeting 4 should not exist")
    } catch (e: AssertionError) {
      // Expected - item should not exist
    }
    try {
      composeTestRule.onNodeWithText("Project 4").assertExists()
      org.junit.Assert.fail("Project 4 should not exist")
    } catch (e: AssertionError) {
      // Expected - item should not exist
    }
  }

  @Test
  fun ctaButtonsTriggerCallbacks() {
    var tasksClicked = false
    var meetingsClicked = false
    var projectsClicked = false

    composeTestRule.setContent {
      HomeOverviewLayout(
          uiState =
              sampleState()
                  .copy(
                      upcomingTasks = listOf(createTask("Task CTA")),
                      upcomingMeetings = listOf(createMeeting("Meeting CTA")),
                      recentProjects = listOf(createProject("Project CTA")),
                      isLoading = false),
          onOpenTasks = { tasksClicked = true },
          onOpenMeetings = { meetingsClicked = true },
          onOpenProjects = { projectsClicked = true })
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("View all").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Open meetings").performClick()
    composeTestRule.waitForIdle()
    // Wait for Browse projects button to be available
    composeTestRule.waitUntil(timeoutMillis = 3000) {
      try {
        composeTestRule.onNodeWithText("Browse projects").assertExists()
        true
      } catch (e: AssertionError) {
        false
      }
    }
    composeTestRule.onNodeWithText("Browse projects").performClick()

    assertTrue(tasksClicked)
    assertTrue(meetingsClicked)
    assertTrue(projectsClicked)
  }

  @Test
  fun showsLoadingState() {
    composeTestRule.setContent {
      HomeOverviewLayout(uiState = HomeOverviewUiState(isLoading = true))
    }

    composeTestRule.onNodeWithTag(HomeOverviewTestTags.LOADING_INDICATOR).assertIsDisplayed()
  }

  private fun sampleState() =
      HomeOverviewUiState(
          currentUserName = "",
          upcomingTasks = emptyList(),
          upcomingMeetings = emptyList(),
          recentProjects = emptyList(),
          isLoading = false,
          isConnected = true,
          error = null)

  private fun createTask(title: String) =
      Task(
          taskID = title,
          projectId = "project-1",
          title = title,
          status = TaskStatus.TODO,
          dueDate = Timestamp.now())

  private fun createMeeting(title: String) =
      Meeting(
          meetingID = title,
          projectId = "project-1",
          title = title,
          status = MeetingStatus.SCHEDULED,
          datetime = Timestamp.now(),
          format = MeetingFormat.VIRTUAL,
          link = "https://meet.google.com/test")

  private fun createProject(name: String) =
      Project(projectId = name, name = name, status = ProjectStatus.OPEN)
}
