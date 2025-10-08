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
class EurekaInfoCardTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaInfoCard displays all content`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(
            title = "Tasks in Progress", primaryValue = "3 open", secondaryValue = "1 due today")
      }
    }

    composeTestRule.onNodeWithText("Tasks in Progress").assertIsDisplayed()

    composeTestRule.onNodeWithText("3 open").assertIsDisplayed()

    composeTestRule.onNodeWithText("1 due today").assertIsDisplayed()
  }

  @Test
  fun `EurekaInfoCard displays without secondary value`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaInfoCard(title = "Simple Card", primaryValue = "5 items")
      }
    }

    composeTestRule.onNodeWithText("Simple Card").assertIsDisplayed()

    composeTestRule.onNodeWithText("5 items").assertIsDisplayed()
  }

  @Test
  fun `EurekaInfoCard renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaInfoCard(title = "Dark Theme Card", primaryValue = "2 items")
      }
    }

    composeTestRule.onNodeWithText("Dark Theme Card").assertIsDisplayed()
  }
}
