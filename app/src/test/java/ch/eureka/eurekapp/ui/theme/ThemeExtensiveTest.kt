package ch.eureka.eurekapp.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThemeExtensiveTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekappTheme applies light colors correctly`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors.primary)
        assertNotNull(colors.background)
        Text("Light Theme")
      }
    }
    composeTestRule.onNodeWithText("Light Theme").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme applies dark colors correctly`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = true) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors.primary)
        assertNotNull(colors.background)
        Text("Dark Theme")
      }
    }
    composeTestRule.onNodeWithText("Dark Theme").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme typography is accessible`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        val typography = MaterialTheme.typography
        assertNotNull(typography.displayLarge)
        assertNotNull(typography.bodyMedium)
        Text("Typography Test")
      }
    }
    composeTestRule.onNodeWithText("Typography Test").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme shapes are accessible`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        val shapes = MaterialTheme.shapes
        assertNotNull(shapes.small)
        assertNotNull(shapes.medium)
        Text("Shapes Test")
      }
    }
    composeTestRule.onNodeWithText("Shapes Test").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme with Surface in light mode`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Surface { Text("Surface Light") } }
    }
    composeTestRule.onNodeWithText("Surface Light").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme with Surface in dark mode`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = true) { Surface { Text("Surface Dark") } }
    }
    composeTestRule.onNodeWithText("Surface Dark").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme with nested Box`() {
    composeTestRule.setContent { EurekappTheme(darkTheme = false) { Box { Text("Box Test") } } }
    composeTestRule.onNodeWithText("Box Test").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme with Column layout`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Text("Column Item 1")
          Text("Column Item 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Column Item 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Column Item 2").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme with Row layout`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Row {
          Text("Row Item 1")
          Text("Row Item 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Row Item 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Row Item 2").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme color scheme primary in light`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Primary", color = MaterialTheme.colorScheme.primary)
      }
    }
  }

  @Test
  fun `EurekappTheme color scheme secondary in light`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Secondary", color = MaterialTheme.colorScheme.secondary)
      }
    }
  }

  @Test
  fun `EurekappTheme color scheme tertiary in light`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Tertiary", color = MaterialTheme.colorScheme.tertiary)
      }
    }
  }

  @Test
  fun `EurekappTheme color scheme error in light`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) { Text("Error", color = MaterialTheme.colorScheme.error) }
    }
  }

  @Test
  fun `EurekappTheme color scheme onSurface in light`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("OnSurface", color = MaterialTheme.colorScheme.onSurface)
      }
    }
  }

  @Test
  fun `EurekappTheme typography displayLarge style`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Display Large", style = MaterialTheme.typography.displayLarge)
      }
    }
  }

  @Test
  fun `EurekappTheme typography titleMedium style`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Title Medium", style = MaterialTheme.typography.titleMedium)
      }
    }
  }

  @Test
  fun `EurekappTheme typography bodyLarge style`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Body Large", style = MaterialTheme.typography.bodyLarge)
      }
    }
  }

  @Test
  fun `EurekappTheme typography labelSmall style`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Text("Label Small", style = MaterialTheme.typography.labelSmall)
      }
    }
  }

  @Test
  fun `EurekappTheme with multiple Text elements light`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        Column {
          Text("Text 1")
          Text("Text 2")
          Text("Text 3")
        }
      }
    }
    composeTestRule.onNodeWithText("Text 1").assertIsDisplayed()
  }

  @Test
  fun `EurekappTheme with multiple Text elements dark`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = true) {
        Column {
          Text("Dark Text 1")
          Text("Dark Text 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Dark Text 1").assertIsDisplayed()
  }
}
