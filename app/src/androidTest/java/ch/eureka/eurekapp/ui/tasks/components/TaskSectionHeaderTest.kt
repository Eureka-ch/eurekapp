package ch.eureka.eurekapp.ui.tasks.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for TaskSectionHeader component
 *
 * These tests verify the correct display of section headers with proper task counts and formatting.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
@RunWith(AndroidJUnit4::class)
class TaskSectionHeaderTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun taskSectionHeader_displaysTitle() {
    // Given
    val title = "Current Tasks"
    val taskCount = 5

    composeTestRule.setContent {
      EurekappTheme { TaskSectionHeader(title = title, taskCount = taskCount) }
    }

    // Then
    composeTestRule.onNodeWithText(title).assertIsDisplayed()
  }

  @Test
  fun taskSectionHeader_displaysTaskCount() {
    // Given
    val title = "Current Tasks"
    val taskCount = 3

    composeTestRule.setContent {
      EurekappTheme { TaskSectionHeader(title = title, taskCount = taskCount) }
    }

    // Then
    composeTestRule.onNodeWithText("$taskCount Tasks").assertIsDisplayed()
  }

  @Test
  fun taskSectionHeader_displaysSingleTask() {
    // Given
    val title = "My Tasks"
    val taskCount = 1

    composeTestRule.setContent {
      EurekappTheme { TaskSectionHeader(title = title, taskCount = taskCount) }
    }

    // Then
    composeTestRule.onNodeWithText("1 Task").assertIsDisplayed()
  }

  @Test
  fun taskSectionHeader_displaysZeroTasks() {
    // Given
    val title = "Completed Tasks"
    val taskCount = 0

    composeTestRule.setContent {
      EurekappTheme { TaskSectionHeader(title = title, taskCount = taskCount) }
    }

    // Then
    composeTestRule.onNodeWithText("0 No task").assertIsDisplayed()
  }

  @Test
  fun taskSectionHeader_displaysMultipleTasks() {
    // Given
    val title = "All Tasks"
    val taskCount = 15

    composeTestRule.setContent {
      EurekappTheme { TaskSectionHeader(title = title, taskCount = taskCount) }
    }

    // Then
    composeTestRule.onNodeWithText("$taskCount Tasks").assertIsDisplayed()
  }
}
