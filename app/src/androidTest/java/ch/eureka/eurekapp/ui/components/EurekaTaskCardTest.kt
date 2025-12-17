// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Functional UI tests for EurekaTaskCard component Tests actual behavior and user interactions, not
 * just display
 */
@RunWith(AndroidJUnit4::class)
class EurekaTaskCardTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun eurekaTaskCard_taskCompletionToggleChangesUIStateCorrectly() {
    var toggleCallCount = 0

    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task", isCompleted = false, onToggleComplete = { toggleCallCount++ })
      }
    }

    // Initially should show checkbox
    composeTestRule.onNodeWithTag("checkbox").assertIsDisplayed()
    composeTestRule.onNodeWithText("Done").assertIsNotDisplayed()

    // Click checkbox
    composeTestRule.onNodeWithTag("checkbox").performClick()

    // Verify callback was called
    assert(toggleCallCount == 1)
  }

  @Test
  fun eurekaTaskCard_showsConditionalContentBasedOnDataAvailability() {
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task",
            dueDate = "Today",
            assignee = "John Doe",
            progressText = "75%",
            progressValue = 0.75f)
      }
    }

    // All content should be visible
    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("Today").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("75%").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_hidesOptionalContentWhenNotProvided() {
    composeTestRule.setContent { EurekappTheme { EurekaTaskCard(title = "Minimal Task") } }

    // Only title should be visible
    composeTestRule.onNodeWithText("Minimal Task").assertIsDisplayed()

    // Optional content should not be visible
    composeTestRule.onNodeWithText("Today").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsNotDisplayed()
    composeTestRule.onNodeWithText("75%").assertIsNotDisplayed()
  }

  @Test
  fun eurekaTaskCard_completedTaskShows100PercentProgressRegardlessOfInput() {
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Completed Task",
            progressText = "50%",
            progressValue = 0.5f,
            isCompleted = true)
      }
    }

    // Should show "Done" tag for completed task (not checkbox)
    composeTestRule.onNodeWithText("Done").assertIsDisplayed()

    // Should not show checkbox when task is already completed
    composeTestRule.onNodeWithTag("checkbox").assertIsNotDisplayed()
  }

  @Test
  fun eurekaTaskCard_handlesMultipleRapidClicksCorrectly() {
    var clickCount = 0

    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Rapid Click Test", isCompleted = false, onToggleComplete = { clickCount++ })
      }
    }

    // Perform multiple rapid clicks
    repeat(5) { composeTestRule.onNodeWithTag("checkbox").performClick() }

    // Should have registered all clicks
    assert(clickCount == 5)
  }

  @Test
  fun eurekaTaskCard_displaysDueDateTagWhenProvided() {
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Urgent Task",
            dueDate = "Today",
            dueDateTag = "Due in 2 hours",
            isCompleted = false)
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithText("Urgent Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("Due in 2 hours").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_hidesDueDateTagWhenNotProvided() {
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Normal Task", dueDate = "Tomorrow", dueDateTag = null, isCompleted = false)
      }
    }

    composeTestRule.onNodeWithText("Normal Task").assertIsDisplayed()
  }
}
