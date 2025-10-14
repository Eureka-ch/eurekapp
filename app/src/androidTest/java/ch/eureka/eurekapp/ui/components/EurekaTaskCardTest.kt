package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for EurekaTaskCard component
 *
 * These tests verify the correct display and interaction behavior of the task card component used
 * throughout the app.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
@RunWith(AndroidJUnit4::class)
class EurekaTaskCardTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

  @Test
  fun eurekaTaskCard_displaysTitle() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task",
            dueDate = "Due: Today",
            assignee = "Assigned: John",
            priority = "High Priority",
            progressText = "50%",
            progressValue = 0.5f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_displaysDueDate() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task",
            dueDate = "Due: Tomorrow",
            assignee = "Assigned: John",
            priority = "High Priority",
            progressText = "50%",
            progressValue = 0.5f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("⏰ Due: Tomorrow").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_displaysAssignee() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task",
            dueDate = "Due: Today",
            assignee = "Assigned: Sarah",
            priority = "High Priority",
            progressText = "50%",
            progressValue = 0.5f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("👤 Assigned: Sarah").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_displaysPriority() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task",
            dueDate = "Due: Today",
            assignee = "Assigned: John",
            priority = "Low Priority",
            progressText = "50%",
            progressValue = 0.5f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("Low Priority").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_displaysProgress() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Test Task",
            dueDate = "Due: Today",
            assignee = "Assigned: John",
            priority = "High Priority",
            progressText = "75%",
            progressValue = 0.75f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("75%").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_displaysCompletedState() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Completed Task",
            dueDate = "Due: Yesterday",
            assignee = "Assigned: John",
            priority = "High Priority",
            progressText = "100%",
            progressValue = 1.0f,
            isCompleted = true,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCard_handlesClick() {
    // Given
    var clicked = false
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Clickable Task",
            dueDate = "Due: Today",
            assignee = "Assigned: John",
            priority = "High Priority",
            progressText = "50%",
            progressValue = 0.5f,
            isCompleted = false,
            onToggleComplete = { clicked = true })
      }
    }

    // When - Click on the checkbox using a different selector
    composeTestRule.onNode(hasTestTag("checkbox")).performClick()

    // Then
    assertTrue("Task should be clickable", clicked)
  }

  @Test
  fun eurekaTaskCard_displaysAllElements() {
    // Given
    composeTestRule.setContent {
      EurekappTheme {
        EurekaTaskCard(
            title = "Complete Task",
            dueDate = "Due: Next Week",
            assignee = "Assigned: Team Lead",
            priority = "Medium Priority",
            progressText = "25%",
            progressValue = 0.25f,
            isCompleted = false,
            onToggleComplete = {})
      }
    }

    // Then
    composeTestRule.onNodeWithText("Complete Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("⏰ Due: Next Week").assertIsDisplayed()
    composeTestRule.onNodeWithText("👤 Assigned: Team Lead").assertIsDisplayed()
    composeTestRule.onNodeWithText("Medium Priority").assertIsDisplayed()
    composeTestRule.onNodeWithText("25%").assertIsDisplayed()
  }
}
