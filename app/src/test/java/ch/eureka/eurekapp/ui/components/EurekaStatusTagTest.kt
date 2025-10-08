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
class EurekaStatusTagTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaStatusTag displays success tag`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Success", type = StatusType.SUCCESS)
      }
    }

    composeTestRule.onNodeWithText("Success").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag displays warning tag`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Warning", type = StatusType.WARNING)
      }
    }

    composeTestRule.onNodeWithText("Warning").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag displays error tag`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "Error", type = StatusType.ERROR) }
    }

    composeTestRule.onNodeWithText("Error").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag displays info tag`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "Info", type = StatusType.INFO) }
    }

    composeTestRule.onNodeWithText("Info").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag displays default info tag`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "Default") }
    }

    composeTestRule.onNodeWithText("Default").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaStatusTag(text = "Dark Theme", type = StatusType.SUCCESS)
      }
    }

    composeTestRule.onNodeWithText("Dark Theme").assertIsDisplayed()
  }
}
