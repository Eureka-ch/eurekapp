/* Portions of this file were written with the help of Claude. */
package ch.eureka.eurekapp.ui.tasks

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import ch.eureka.eurekapp.screens.TasksScreenTestTags

/**
 * Robot pattern for TasksScreen UI tests Provides a fluent API for interacting with the TasksScreen
 */
class TasksScreenRobot(private val composeTestRule: ComposeTestRule) {

  // Navigation actions
  fun clickCreateTask(): TasksScreenRobot {
    composeTestRule.onNodeWithText("+ New Task").performClick()
    return this
  }

  fun clickAutoAssign(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Auto-assign").performClick()
    return this
  }

  // Filter actions
  fun clickFilter(filterName: String): TasksScreenRobot {
    composeTestRule.waitForIdle() // Attendre le rendu avant interaction
    composeTestRule
        .onNodeWithTag("filter_${filterName.lowercase().replace(" ", "_")}")
        .performClick()
    return this
  }

  fun clickMeFilter(): TasksScreenRobot = clickFilter("My tasks")

  fun clickTeamFilter(): TasksScreenRobot = clickFilter("Team")

  fun clickThisWeekFilter(): TasksScreenRobot = clickFilter("This week")

  fun clickAllFilter(): TasksScreenRobot = clickFilter("All")

  fun clickProjectFilter(): TasksScreenRobot {
    composeTestRule.waitForIdle() // Attendre le rendu
    return clickFilter("Project")
  }

  // Task actions
  fun clickTask(taskTitle: String): TasksScreenRobot {
    composeTestRule.onNodeWithText(taskTitle).performClick()
    return this
  }

  fun toggleTaskCompletion(taskTitle: String): TasksScreenRobot {
    // Find the checkbox for the task and click it
    composeTestRule.onNodeWithText(taskTitle).performScrollTo().performClick()
    return this
  }

  // Assertions - Display
  fun assertScreenTitleDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithTag("tasksTopBar").assertIsDisplayed()
    return this
  }

  fun assertDescriptionDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Manage and track your project tasks").assertIsDisplayed()
    return this
  }

  fun assertCreateTaskButtonDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("+ New Task").assertIsDisplayed()
    return this
  }

  fun assertAutoAssignButtonDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Auto-assign").assertIsDisplayed()
    return this
  }

  fun assertFilterDisplayed(filterName: String): TasksScreenRobot {
    composeTestRule.waitForIdle() // Attendre le rendu avant assertion
    composeTestRule
        .onNodeWithTag("filter_${filterName.lowercase().replace(" ", "_")}")
        .assertIsDisplayed()
    return this
  }

  fun assertAllFiltersDisplayed(): TasksScreenRobot {
    composeTestRule.waitForIdle() // Attendre le rendu complet
    assertFilterDisplayed("My tasks")
    assertFilterDisplayed("Team")
    assertFilterDisplayed("This week")
    assertFilterDisplayed("All")
    assertFilterDisplayed("Project")
    return this
  }

  // Assertions - Task content
  fun assertTaskDisplayed(taskTitle: String): TasksScreenRobot {
    composeTestRule.onNodeWithText(taskTitle).assertIsDisplayed()
    return this
  }

  fun assertTaskNotDisplayed(taskTitle: String): TasksScreenRobot {
    composeTestRule.onNodeWithText(taskTitle).assertIsNotDisplayed()
    return this
  }

  fun assertTaskCountDisplayed(count: Int): TasksScreenRobot {
    composeTestRule.onNodeWithText("$count tasks").assertIsDisplayed()
    return this
  }

  fun assertTaskCountDisplayed(count: Int, singular: Boolean = false): TasksScreenRobot {
    val text = if (singular && count == 1) "1 task" else "$count tasks"
    composeTestRule.onNodeWithText(text).assertIsDisplayed()
    return this
  }

  // Assertions - States
  fun assertLoadingDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithTag(TasksScreenTestTags.LOADING_INDICATOR).assertIsDisplayed()
    return this
  }

  fun assertLoadingNotDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithTag(TasksScreenTestTags.LOADING_INDICATOR).assertIsNotDisplayed()
    return this
  }

  fun assertEmptyStateDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("No tasks found").assertIsDisplayed()
    return this
  }

  fun assertErrorStateDisplayed(errorMessage: String): TasksScreenRobot {
    composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    return this
  }

  fun assertErrorStateNotDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithTag(TasksScreenTestTags.ERROR_MESSAGE).assertIsNotDisplayed()
    return this
  }

  // Assertions - Filter states
  fun assertFilterSelected(filterName: String): TasksScreenRobot {
    composeTestRule.onNodeWithText(filterName).assertIsDisplayed()
    return this
  }

  fun assertFilterNotSelected(filterName: String): TasksScreenRobot {
    composeTestRule.onNodeWithText(filterName).assertIsDisplayed()
    return this
  }

  // Assertions - Sections
  fun assertCurrentTasksSectionDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Current Tasks").assertIsDisplayed()
    return this
  }

  fun assertCompletedTasksSectionDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Recently Completed").assertIsDisplayed()
    return this
  }

  fun assertCurrentTasksSectionNotDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Current Tasks").assertIsNotDisplayed()
    return this
  }

  fun assertCompletedTasksSectionNotDisplayed(): TasksScreenRobot {
    composeTestRule.onNodeWithText("Recently Completed").assertIsNotDisplayed()
    return this
  }

  // Helper methods
  fun verifyScreenLoaded(): TasksScreenRobot {
    assertScreenTitleDisplayed()
    return this
  }
}
