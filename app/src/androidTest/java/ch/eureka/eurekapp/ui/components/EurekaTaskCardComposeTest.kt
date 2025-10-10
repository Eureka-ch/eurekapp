package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests Compose UI efficaces pour EurekaTaskCard Ces tests testent vraiment le rendu des composants
 */
@RunWith(AndroidJUnit4::class)
class EurekaTaskCardComposeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaTaskCardRendersWithTitle() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTaskCard(title = "Test Task", isCompleted = false) }
    }

    composeTestRule.onNodeWithText("Test Task").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithCompletionCheckmark() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Completed Task", isCompleted = true)
      }
    }

    composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
    composeTestRule.onNodeWithText("✓").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithMetadata() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Metadata", dueDate = "2024-01-15", assignee = "John Doe")
      }
    }

    composeTestRule.onNodeWithText("Task with Metadata").assertIsDisplayed()
    composeTestRule.onNodeWithText("2024-01-15").assertIsDisplayed()
    composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
  }

  @Test
  fun eurekaTaskCardRendersWithProgress() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Task with Progress", progressText = "75%", progressValue = 0.75f)
      }
    }

    composeTestRule.onNodeWithText("Task with Progress").assertIsDisplayed()
    composeTestRule.onNodeWithText("75%").assertIsDisplayed()
  }
}
