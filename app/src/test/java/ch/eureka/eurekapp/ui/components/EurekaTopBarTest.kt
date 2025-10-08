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
class EurekaTopBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaTopBar displays default title`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { EurekaTopBar() } }

    composeTestRule.onNodeWithText("EUREKA").assertIsDisplayed()
  }

  @Test
  fun `EurekaTopBar displays custom title`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = "Custom Title") }
    }

    composeTestRule.onNodeWithText("Custom Title").assertIsDisplayed()
  }

  @Test
  fun `EurekaTopBar renders in dark theme`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = true) { EurekaTopBar() } }

    composeTestRule.onNodeWithText("EUREKA").assertIsDisplayed()
  }
}
