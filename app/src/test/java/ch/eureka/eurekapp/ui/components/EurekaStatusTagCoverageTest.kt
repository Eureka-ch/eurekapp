package ch.eureka.eurekapp.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaStatusTagCoverageTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaStatusTag renders SUCCESS type`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Success Status", type = StatusType.SUCCESS)
      }
    }

    composeTestRule.onNodeWithText("Success Status").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders ERROR type`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Error Status", type = StatusType.ERROR)
      }
    }

    composeTestRule.onNodeWithText("Error Status").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders WARNING type`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Warning Status", type = StatusType.WARNING)
      }
    }

    composeTestRule.onNodeWithText("Warning Status").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders INFO type`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(text = "Info Status", type = StatusType.INFO)
      }
    }

    composeTestRule.onNodeWithText("Info Status").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        EurekaStatusTag(text = "Dark Theme Status", type = StatusType.SUCCESS)
      }
    }

    composeTestRule.onNodeWithText("Dark Theme Status").assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders with empty text`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaStatusTag(text = "", type = StatusType.INFO) }
    }

    // Should render without crashing
    composeTestRule.onRoot().assertExists()
  }

  @Test
  fun `EurekaStatusTag renders with long text`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        EurekaStatusTag(
            text = "Very Long Status Text That Should Still Render Correctly",
            type = StatusType.WARNING)
      }
    }

    composeTestRule
        .onNodeWithText("Very Long Status Text That Should Still Render Correctly")
        .assertIsDisplayed()
  }

  @Test
  fun `EurekaStatusTag renders all status types`() {
    val statusTypes =
        listOf(StatusType.SUCCESS, StatusType.ERROR, StatusType.WARNING, StatusType.INFO)

    statusTypes.forEach { type ->
      composeTestRule.setContent {
        EurekaTheme(darkTheme = false) {
          EurekaStatusTag(text = "Status ${type.name}", type = type)
        }
      }
      composeTestRule.onNodeWithText("Status ${type.name}").assertIsDisplayed()
    }
  }
}
