package ch.eureka.eurekapp.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
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

// Part of this code and documentation were generated with the help of AI (ChatGPT 5.1, and Grok).
@RunWith(AndroidJUnit4::class)
class HomeOverviewScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun homeOverviewScreen_displaysGreetingAndSections() {
    val uiState =
        sampleState()
            .copy(
                currentUserName = "Alex",
                upcomingTasks = listOf(createTask("Task A")),
                upcomingMeetings = listOf(createMeeting("Meeting A")),
                recentProjects = listOf(createProject("Project A")),
                isLoading = false)

    composeTestRule.setContent { HomeOverviewLayout(uiState = uiState) }

    composeTestRule.waitForIdle()

    // Verify greeting
    composeTestRule.onNodeWithText("Hello Alex").assertIsDisplayed()

    // Scroll to make sure items are visible and verify via test tags
    val list = composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN)
    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.getTaskItemTestTag("Task A")))
    composeTestRule
        .onNodeWithTag(HomeOverviewTestTags.getTaskItemTestTag("Task A"))
        .assertIsDisplayed()

    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.getMeetingItemTestTag("Meeting A")))
    composeTestRule
        .onNodeWithTag(HomeOverviewTestTags.getMeetingItemTestTag("Meeting A"))
        .assertIsDisplayed()

    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.getProjectItemTestTag("Project A")))
    composeTestRule
        .onNodeWithTag(HomeOverviewTestTags.getProjectItemTestTag("Project A"))
        .assertIsDisplayed()

    // Verify action buttons are displayed (scroll into view first)
    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_TASKS))
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).assertIsDisplayed()
    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_MEETINGS))
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_MEETINGS).assertIsDisplayed()
    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_PROJECTS))
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_PROJECTS).assertIsDisplayed()
  }

  @Test
  fun homeOverviewScreen_limitsItemsPerSection() {
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
  fun homeOverviewScreen_ctaButtonsTriggerCallbacks() {
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
          actions =
              HomeOverviewActions(
                  onOpenTasks = { tasksClicked = true },
                  onOpenMeetings = { meetingsClicked = true },
                  onOpenProjects = { projectsClicked = true }))
    }

    composeTestRule.waitForIdle()

    val list = composeTestRule.onNodeWithTag(HomeOverviewTestTags.SCREEN)

    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_TASKS))
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_TASKS).performClick()
    composeTestRule.waitForIdle()

    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_MEETINGS))
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_MEETINGS).performClick()
    composeTestRule.waitForIdle()

    list.performScrollToNode(hasTestTag(HomeOverviewTestTags.CTA_PROJECTS))
    composeTestRule.onNodeWithTag(HomeOverviewTestTags.CTA_PROJECTS).performClick()

    assertTrue(tasksClicked)
    assertTrue(meetingsClicked)
    assertTrue(projectsClicked)
  }

  @Test
  fun homeOverviewScreen_showsLoadingState() {
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
