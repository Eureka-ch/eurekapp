package ch.eureka.eurekapp.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaTaskCard
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaTaskCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaTaskCardRendersBasicContent() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTaskCard(title = "Test Task", onToggleComplete = {}) }
    }

    // Check that basic content is displayed
    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithAllOptionalFields() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Complete Task",
            dueDate = "2024-01-15",
            assignee = "John Doe",
            priority = "High",
            progressText = "75%",
            progressValue = 0.75f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Check that all content is displayed
    composeTestRule.onNodeWithText("Complete Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("2024-01-15").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()
    composeTestRule.onNodeWithText("Work").assertIsDisplayed()
    composeTestRule.onNodeWithText("75%").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardShowsCompletedState() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Completed Task", isCompleted = true, onToggleComplete = {})
      }
    }

    // Check that completed task shows checkmark
    composeTestRule.onNodeWithText("✓").assertIsDisplayed()
    composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardShowsIncompleteState() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Incomplete Task", isCompleted = false, onToggleComplete = {})
      }
    }

    // Check that incomplete task shows checkbox
    composeTestRule.onNodeWithText("Incomplete Task").assertIsDisplayed()
    // Checkbox should be present but not checked
    composeTestRule.onNodeWithText("Incomplete Task").assertIsNotSelected()
  }

  @Test
  fun eurekaTaskCardHandlesToggleComplete() {
    var toggleCalled = false

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Toggle Task", isCompleted = false, onToggleComplete = { toggleCalled = true })
      }
    }

    // Click on the task (this should trigger the checkbox)
    composeTestRule.onNodeWithText("Toggle Task").performClick()

    // Verify the callback was called
    assert(toggleCalled)
  }

  @Test
  fun eurekaTaskCardRendersWithOnlyDueDate() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Due Date", dueDate = "Tomorrow", onToggleComplete = {})
      }
    }

    // Check that due date is displayed
    composeTestRule.onNodeWithText("Task with Due Date").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tomorrow").assertIsDisplayed()
    composeTestRule.onNodeWithText("⏰").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithOnlyAssignee() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Assignee", assignee = "Alice", onToggleComplete = {})
      }
    }

    // Check that assignee is displayed
    composeTestRule.onNodeWithText("Task with Assignee").assertIsDisplayed()
    composeTestRule.onNodeWithText("Alice").assertIsDisplayed()
    composeTestRule.onNodeWithText("👤").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithOnlyPriorityLow() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Priority", priority = "Low", onToggleComplete = {})
      }
    }

    // Check that priority tag is displayed
    composeTestRule.onNodeWithText("Task with Priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("Low").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithOnlyPriorityHigh() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Priority", priority = "High", onToggleComplete = {})
      }
    }

    // Check that priority tag is displayed
    composeTestRule.onNodeWithText("Task with Priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("High").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithProgressOnly() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Progress", progressValue = 0.5f, onToggleComplete = {})
      }
    }

    // Check that progress is displayed
    composeTestRule.onNodeWithText("Task with Progress").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithProgressTextOnly() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Task with Progress Text", progressText = "50%", onToggleComplete = {})
      }
    }

    // Check that progress text is displayed
    composeTestRule.onNodeWithText("Task with Progress Text").assertIsDisplayed()
    composeTestRule.onNodeWithText("50%").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardWorksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaTaskCard(
            title = "Dark Mode Task",
            dueDate = "2024-01-20",
            assignee = "Bob",
            priority = "Medium",
            onToggleComplete = {})
      }
    }

    // Check that task content is displayed in dark mode
    composeTestRule.onNodeWithText("Dark Mode Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("2024-01-20").assertIsDisplayed()
    composeTestRule.onNodeWithText("Bob").assertIsDisplayed()
    composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
    composeTestRule.onNodeWithText("Home").assertIsDisplayed()
  }
}
