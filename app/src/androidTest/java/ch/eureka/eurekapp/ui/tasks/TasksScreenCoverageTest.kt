package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
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

  @Test
  fun tasksScreen_allFilterOptions() {
    // Given
    composeTestRule.setContent { EurekappTheme { TasksScreen() } }

    // When & Then - Test all filter options to cover filter logic
    TasksScreenRobot(composeTestRule)
        .assertAllFiltersDisplayed()
        .clickMeFilter()
        .clickTeamFilter()
        .clickThisWeekFilter()
        .clickAllFilter()
  }
}
