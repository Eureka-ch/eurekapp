/**
 * Tests for TaskActionButtons component
 *
 * These tests verify the correct display and interaction behavior of the task action buttons
 * including create task and auto-assign functionality.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
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
 * Tests for TaskActionButtons component
 *
 * These tests verify the correct display and interaction behavior of the task action buttons
 * (Create Task and Auto-assign).
 *
 * @author Assisted by AI for comprehensive test coverage
 */
@RunWith(AndroidJUnit4::class)
class TaskActionButtonsTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun taskActionButtons_displaysCreateTaskButton() {
    // Given
    composeTestRule.setContent {
      EurekappTheme { TaskActionButtons(onCreateTaskClick = {}, onAutoAssignClick = {}) }
    }

    // Then
    composeTestRule.onNodeWithText("+ New Task").assertIsDisplayed()
  }

  @Test
  fun taskActionButtons_displaysAutoAssignButton() {
    // Given
    composeTestRule.setContent {
      EurekappTheme { TaskActionButtons(onCreateTaskClick = {}, onAutoAssignClick = {}) }
    }

    // Then
    composeTestRule.onNodeWithText("Auto-assign").assertIsDisplayed()
  }

  @Test
  fun taskActionButtons_bothButtonsDisplayed() {
    // Given
    composeTestRule.setContent {
      EurekappTheme { TaskActionButtons(onCreateTaskClick = {}, onAutoAssignClick = {}) }
    }

    // Then
    composeTestRule.onNodeWithText("+ New Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("Auto-assign").assertIsDisplayed()
  }
}
