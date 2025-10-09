package ch.eureka.eurekapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests complets pour SecondActivity et GreetingRobo */
@RunWith(AndroidJUnit4::class)
class SecondActivityComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `GreetingRobo displays correct text`() {
    composeTestRule.setContent { GreetingRobo("Robolectric") }
    composeTestRule.onNodeWithText("Hello Robolectric!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo displays with custom name`() {
    composeTestRule.setContent { GreetingRobo("Test") }
    composeTestRule.onNodeWithText("Hello Test!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo displays with empty name`() {
    composeTestRule.setContent { GreetingRobo("") }
    composeTestRule.onNodeWithText("Hello !").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo displays in light theme`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = false) { GreetingRobo("Light") } }
    composeTestRule.onNodeWithText("Hello Light!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo displays in dark theme`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = true) { GreetingRobo("Dark") } }
    composeTestRule.onNodeWithText("Hello Dark!").assertIsDisplayed()
  }

  @Test
  fun `GreetingPreview2 renders without crash`() {
    composeTestRule.setContent { GreetingPreview2() }
    composeTestRule.onNodeWithText("Hello Robolectric!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo with numbers`() {
    composeTestRule.setContent { GreetingRobo("123") }
    composeTestRule.onNodeWithText("Hello 123!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo with special characters`() {
    composeTestRule.setContent { GreetingRobo("Test@123") }
    composeTestRule.onNodeWithText("Hello Test@123!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo with spaces`() {
    composeTestRule.setContent { GreetingRobo("John Doe") }
    composeTestRule.onNodeWithText("Hello John Doe!").assertIsDisplayed()
  }

  @Test
  fun `GreetingRobo with unicode`() {
    composeTestRule.setContent { GreetingRobo("世界") }
    composeTestRule.onNodeWithText("Hello 世界!").assertIsDisplayed()
  }
}
