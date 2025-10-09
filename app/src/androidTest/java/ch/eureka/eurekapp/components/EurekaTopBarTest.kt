package ch.eureka.eurekapp.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EurekaTopBarTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun eurekaTopBarRendersDefaultTitle() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { EurekaTopBar() } }

    // Check that default title is displayed
    composeTestRule.onNodeWithText("EUREKA").assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersCustomTitle() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = "Custom Title") }
    }

    // Check that custom title is displayed
    composeTestRule.onNodeWithText("Custom Title").assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersEmptyTitle() {
    composeTestRule.setContent { EurekaTheme(darkTheme = false) { EurekaTopBar(title = "") } }

    // Check that empty title is handled gracefully
    composeTestRule.onNodeWithText("").assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersLongTitle() {
    val longTitle = "This is a very long title that should still be displayed correctly"

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = longTitle) }
    }

    // Check that long title is displayed
    composeTestRule.onNodeWithText(longTitle).assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersSpecialCharacters() {
    val specialTitle = "Title with Special Chars: @#$%^&*()"

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = specialTitle) }
    }

    // Check that special characters are displayed
    composeTestRule.onNodeWithText(specialTitle).assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersUnicodeCharacters() {
    val unicodeTitle = "Título con Caracteres Especiales: ñáéíóú"

    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = unicodeTitle) }
    }

    // Check that unicode characters are displayed
    composeTestRule.onNodeWithText(unicodeTitle).assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarWorksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { EurekaTopBar(title = "Dark Mode Title") }
    }

    // Check that title is displayed in dark mode
    composeTestRule.onNodeWithText("Dark Mode Title").assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarWorksInLightMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = "Light Mode Title") }
    }

    // Check that title is displayed in light mode
    composeTestRule.onNodeWithText("Light Mode Title").assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersMultipleInstances() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Column {
          EurekaTopBar(title = "First Bar")
          EurekaTopBar(title = "Second Bar")
        }
      }
    }

    // Check that both titles are displayed
    composeTestRule.onNodeWithText("First Bar").assertIsDisplayed()
    composeTestRule.onNodeWithText("Second Bar").assertIsDisplayed()
  }

  @Test
  fun eurekaTopBarRendersWithNumbers() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { EurekaTopBar(title = "Title 123") }
    }

    // Check that title with numbers is displayed
    composeTestRule.onNodeWithText("Title 123").assertIsDisplayed()
  }
}
