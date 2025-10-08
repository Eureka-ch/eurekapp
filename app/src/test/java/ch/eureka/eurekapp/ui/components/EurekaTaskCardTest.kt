package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaTaskCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaTaskCard displays task information`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(
            title = "Implement Overview Screen",
            dueDate = "Due: Today - 23:59",
            assignee = "Assigned: Ismail",
            priority = "High",
            category = "UI",
            progress = 0.65f)
      }
    }

    composeTestRule.onNodeWithText("Implement Overview Screen").assertIsDisplayed()

    composeTestRule.onNodeWithText("Due: Today - 23:59").assertIsDisplayed()

    composeTestRule.onNodeWithText("Assigned: Ismail").assertIsDisplayed()

    composeTestRule.onNodeWithText("High").assertIsDisplayed()

    composeTestRule.onNodeWithText("UI").assertIsDisplayed()

    composeTestRule.onNodeWithText("65%").assertIsDisplayed()
  }

  @Test
  fun `EurekaTaskCard displays completed task`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaTaskCard(title = "Completed Task", isCompleted = true)
      }
    }

    composeTestRule.onNodeWithText("Completed Task").assertIsDisplayed()
  }

  @Test
  fun `EurekaTaskCard renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaTaskCard(title = "Dark Theme Task", priority = "Medium")
      }
    }

    composeTestRule.onNodeWithText("Dark Theme Task").assertIsDisplayed()
  }
}
