package ch.eureka.eurekapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.theme.EurekappTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests complets pour MainActivity et ses composables */
@RunWith(AndroidJUnit4::class)
class MainActivityComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `Greeting displays correct text`() {
    composeTestRule.setContent { Greeting("World") }
    composeTestRule.onNodeWithText("Hello World!").assertIsDisplayed()
  }

  @Test
  fun `Greeting displays with Android`() {
    composeTestRule.setContent { Greeting("Android") }
    composeTestRule.onNodeWithText("Hello Android!").assertIsDisplayed()
  }

  @Test
  fun `Greeting displays with custom name`() {
    composeTestRule.setContent { Greeting("Test User") }
    composeTestRule.onNodeWithText("Hello Test User!").assertIsDisplayed()
  }

  @Test
  fun `Greeting displays with empty name`() {
    composeTestRule.setContent { Greeting("") }
    composeTestRule.onNodeWithText("Hello !").assertIsDisplayed()
  }

  @Test
  fun `Greeting displays with special characters`() {
    composeTestRule.setContent { Greeting("John@123") }
    composeTestRule.onNodeWithText("Hello John@123!").assertIsDisplayed()
  }

  @Test
  fun `Greeting displays in light theme`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = false) { Greeting("Light") } }
    composeTestRule.onNodeWithText("Hello Light!").assertIsDisplayed()
  }

  @Test
  fun `Greeting displays in dark theme`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = true) { Greeting("Dark") } }
    composeTestRule.onNodeWithText("Hello Dark!").assertIsDisplayed()
  }

  @Test
  fun `GreetingPreview renders without crash`() {
    composeTestRule.setContent { GreetingPreview() }
    composeTestRule.onNodeWithText("Hello Android!").assertIsDisplayed()
  }

  @Test
  fun `Greeting with very long name`() {
    composeTestRule.setContent { Greeting("VeryLongNameThatShouldStillWork") }
    composeTestRule.onNodeWithText("Hello VeryLongNameThatShouldStillWork!").assertIsDisplayed()
  }

  @Test
  fun `Greeting with numbers`() {
    composeTestRule.setContent { Greeting("12345") }
    composeTestRule.onNodeWithText("Hello 12345!").assertIsDisplayed()
  }

  @Test
  fun `Greeting with spaces`() {
    composeTestRule.setContent { Greeting("John Doe") }
    composeTestRule.onNodeWithText("Hello John Doe!").assertIsDisplayed()
  }

  @Test
  fun `Greeting with unicode characters`() {
    composeTestRule.setContent { Greeting("世界") }
    composeTestRule.onNodeWithText("Hello 世界!").assertIsDisplayed()
  }

  @Test
  fun `Multiple greetings render correctly`() {
    composeTestRule.setContent {
      Greeting("First")
      Greeting("Second")
    }
    composeTestRule.onNodeWithText("Hello First!").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello Second!").assertIsDisplayed()
  }
}
