// Portions of this file were written with the help of Grok.
package ch.eureka.eurekapp.designsystem

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaInfoCard
import ch.eureka.eurekapp.ui.components.EurekaStatusTag
import ch.eureka.eurekapp.ui.components.StatusType
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaThemeIntegrationTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaTheme_rendersComponentsCorrectlyInLightMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column {
          EurekaInfoCard(title = "Test Card", primaryValue = "Test Value")

          EurekaStatusTag(text = "Test Tag", type = StatusType.SUCCESS)
        }
      }
    }

    composeTestRule.onNodeWithText("Test Card").assertIsDisplayed()

    composeTestRule.onNodeWithText("Test Value").assertIsDisplayed()

    composeTestRule.onNodeWithText("Test Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaTheme_rendersComponentsCorrectlyInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        androidx.compose.foundation.layout.Column {
          EurekaInfoCard(title = "Dark Card", primaryValue = "Dark Value")

          EurekaStatusTag(text = "Dark Tag", type = StatusType.ERROR)
        }
      }
    }

    composeTestRule.onNodeWithText("Dark Card").assertIsDisplayed()

    composeTestRule.onNodeWithText("Dark Value").assertIsDisplayed()

    composeTestRule.onNodeWithText("Dark Tag").assertIsDisplayed()
  }

  @Test
  fun eurekaTheme_rendersSimpleText() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { androidx.compose.material3.Text("Simple Text") }
    }

    composeTestRule.onNodeWithText("Simple Text").assertIsDisplayed()
  }
}
