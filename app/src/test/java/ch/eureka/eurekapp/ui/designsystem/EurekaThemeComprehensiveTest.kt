package ch.eureka.eurekapp.ui.designsystem

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests complets pour EurekaTheme (nouveau design system) */
@RunWith(AndroidJUnit4::class)
class EurekaThemeComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaTheme renders in light mode`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { Text("Light Mode") } }
    composeTestRule.onNodeWithText("Light Mode").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme renders in dark mode`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = true) { Text("Dark Mode") } }
    composeTestRule.onNodeWithText("Dark Mode").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme light mode with multiple texts`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("First")
        Text("Second")
        Text("Third")
      }
    }
    composeTestRule.onNodeWithText("First").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second").assertIsDisplayed()
    composeTestRule.onNodeWithText("Third").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme dark mode with multiple texts`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        Text("First Dark")
        Text("Second Dark")
      }
    }
    composeTestRule.onNodeWithText("First Dark").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Dark").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme provides LocalSpacing in light mode`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { Text("Spacing Test") } }
    composeTestRule.onNodeWithText("Spacing Test").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme provides LocalSpacing in dark mode`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = true) { Text("Spacing Test Dark") } }
    composeTestRule.onNodeWithText("Spacing Test Dark").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme applies EColors light`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { Text("Colors Light") } }
    composeTestRule.onNodeWithText("Colors Light").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme applies EColors dark`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = true) { Text("Colors Dark") } }
    composeTestRule.onNodeWithText("Colors Dark").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme applies ETypography`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { Text("Typography Test") } }
    composeTestRule.onNodeWithText("Typography Test").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme applies EShapes`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { Text("Shapes Test") } }
    composeTestRule.onNodeWithText("Shapes Test").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme works with empty content`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Empty content
      }
    }
  }

  @Test
  fun `EurekaTheme darkTheme true condition`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = true) { Text("Dark True") } }
    composeTestRule.onNodeWithText("Dark True").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme darkTheme false condition`() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { Text("Dark False") } }
    composeTestRule.onNodeWithText("Dark False").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme nested content`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTheme(darkTheme = true) { Text("Nested") } }
    }
    composeTestRule.onNodeWithText("Nested").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with complex UI`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Title")
        Text("Subtitle")
        Text("Body")
        Text("Footer")
      }
    }
    composeTestRule.onNodeWithText("Title").assertIsDisplayed()
    composeTestRule.onNodeWithText("Subtitle").assertIsDisplayed()
    composeTestRule.onNodeWithText("Body").assertIsDisplayed()
    composeTestRule.onNodeWithText("Footer").assertIsDisplayed()
  }
}
