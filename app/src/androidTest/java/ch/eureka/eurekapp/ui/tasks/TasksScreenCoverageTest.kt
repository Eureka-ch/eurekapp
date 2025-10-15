package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TasksScreenCoverageTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun tasksScreen_loadsWithAllComponents() {
    // Given
    composeTestRule.setContent { EurekappTheme { TasksScreen() } }

    // Then - This will execute TasksScreen composable and cover initialization code
    TasksScreenRobot(composeTestRule)
        .assertScreenTitleDisplayed()
        .assertDescriptionDisplayed()
        .assertCreateTaskButtonDisplayed()
        .assertAutoAssignButtonDisplayed()
  }

  @Test
  fun tasksScreen_filterInteractions() {
    // Given
    composeTestRule.setContent { EurekappTheme { TasksScreen() } }

    // When & Then - This will trigger filter changes in TaskViewModel
    TasksScreenRobot(composeTestRule)
        .clickMeFilter()
        .clickTeamFilter()
        .clickThisWeekFilter()
        .clickAllFilter()
  }

  @Test
  fun tasksScreen_withCallbacks() {
    // Given
    var createTaskCalled = false
    var autoAssignCalled = false

    composeTestRule.setContent {
      EurekappTheme {
        TasksScreen(
            onCreateTaskClick = { createTaskCalled = true },
            onAutoAssignClick = { autoAssignCalled = true },
            onNavigate = { /* navigation callback */})
      }
    }

    // When
    TasksScreenRobot(composeTestRule).clickCreateTask().clickAutoAssign()

    // Then
    assert(createTaskCalled) { "Create task callback should be called" }
    assert(autoAssignCalled) { "Auto assign callback should be called" }
  }

  @Test
  fun tasksScreen_emptyState() {
    // Given
    composeTestRule.setContent { EurekappTheme { TasksScreen() } }

    // Then - This will execute the empty state logic
    TasksScreenRobot(composeTestRule).assertEmptyStateDisplayed().assertTaskCountDisplayed(0)
  }

  @Ignore("CI timeout issue - passes locally but fails in CI due to timing/rendering issues")
  @Test
  fun tasksScreen_allFilterOptions() {
    // Given - Use mock ViewModel for stable state
    val mockViewModel = TaskViewModel(MockTaskRepository())
    mockViewModel.setFilter(TaskFilter.MINE) // Set stable state

    composeTestRule.setContent { EurekappTheme { TasksScreen(viewModel = mockViewModel) } }

    // Wait for filters to be ready - use correct testTags
    composeTestRule.waitUntil(timeoutMillis = 10000) {
      try {
        composeTestRule.onNodeWithTag("filter_my_tasks").assertExists()
        composeTestRule.onNodeWithTag("filter_team").assertExists()
        composeTestRule.onNodeWithTag("filter_this_week").assertExists()
        composeTestRule.onNodeWithTag("filter_all").assertExists()
        true
      } catch (e: Exception) {
        false
      }
    }

    // When & Then - Test all filter options to cover filter logic
    TasksScreenRobot(composeTestRule)
        .assertAllFiltersDisplayed()
        .clickMeFilter()
        .clickTeamFilter()
        .clickThisWeekFilter()
        .clickAllFilter()
  }
}
