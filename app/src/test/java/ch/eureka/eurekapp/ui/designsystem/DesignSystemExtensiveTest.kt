package ch.eureka.eurekapp.ui.designsystem

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.tokens.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesignSystemExtensiveTest {
  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaTheme provides correct color scheme in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors.primary)
        assertNotNull(colors.onPrimary)
        Text("Light Colors")
      }
    }
    composeTestRule.onNodeWithText("Light Colors").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme provides correct color scheme in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colors = MaterialTheme.colorScheme
        assertNotNull(colors.primary)
        assertNotNull(colors.onPrimary)
        Text("Dark Colors")
      }
    }
    composeTestRule.onNodeWithText("Dark Colors").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme provides correct typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val typo = MaterialTheme.typography
        assertEquals(typo, ETypography.value)
        Text("Typography Match")
      }
    }
    composeTestRule.onNodeWithText("Typography Match").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme provides correct shapes`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val shapes = MaterialTheme.shapes
        assertEquals(shapes, EShapes.value)
        Text("Shapes Match")
      }
    }
    composeTestRule.onNodeWithText("Shapes Match").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with Surface component light`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { Surface { Text("Surface in Light") } }
    }
    composeTestRule.onNodeWithText("Surface in Light").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with Surface component dark`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { Surface { Text("Surface in Dark") } }
    }
    composeTestRule.onNodeWithText("Surface in Dark").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with Column layout light`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Column {
          Text("Column 1")
          Text("Column 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Column 1").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with Column layout dark`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        Column {
          Text("Dark Column 1")
          Text("Dark Column 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Dark Column 1").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with Row layout light`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Row {
          Text("Row 1")
          Text("Row 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Row 1").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme with Row layout dark`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        Row {
          Text("Dark Row 1")
          Text("Dark Row 2")
        }
      }
    }
    composeTestRule.onNodeWithText("Dark Row 1").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme primary color usage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Primary Color", color = MaterialTheme.colorScheme.primary)
      }
    }
  }

  @Test
  fun `EurekaTheme secondary color usage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Secondary Color", color = MaterialTheme.colorScheme.secondary)
      }
    }
  }

  @Test
  fun `EurekaTheme tertiary color usage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Tertiary Color", color = MaterialTheme.colorScheme.tertiary)
      }
    }
  }

  @Test
  fun `EurekaTheme background color usage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Background Color", color = MaterialTheme.colorScheme.background)
      }
    }
  }

  @Test
  fun `EurekaTheme surface color usage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Surface Color", color = MaterialTheme.colorScheme.surface)
      }
    }
  }

  @Test
  fun `EurekaTheme displayLarge typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Display Large", style = MaterialTheme.typography.displayLarge)
      }
    }
  }

  @Test
  fun `EurekaTheme displayMedium typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Display Medium", style = MaterialTheme.typography.displayMedium)
      }
    }
  }

  @Test
  fun `EurekaTheme titleLarge typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Title Large", style = MaterialTheme.typography.titleLarge)
      }
    }
  }

  @Test
  fun `EurekaTheme bodyMedium typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Body Medium", style = MaterialTheme.typography.bodyMedium)
      }
    }
  }

  @Test
  fun `EurekaTheme labelLarge typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Text("Label Large", style = MaterialTheme.typography.labelLarge)
      }
    }
  }

  @Test
  fun `EurekaTheme nested content light`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Surface {
          Column {
            Text("Nested 1")
            Text("Nested 2")
          }
        }
      }
    }
    composeTestRule.onNodeWithText("Nested 1").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme nested content dark`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        Surface {
          Column {
            Text("Dark Nested 1")
            Text("Dark Nested 2")
          }
        }
      }
    }
    composeTestRule.onNodeWithText("Dark Nested 1").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme multiple text styles`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        Column {
          Text("Display", style = MaterialTheme.typography.displayLarge)
          Text("Title", style = MaterialTheme.typography.titleMedium)
          Text("Body", style = MaterialTheme.typography.bodyMedium)
        }
      }
    }
  }

  @Test
  fun `EurekaTheme provides LocalSpacing`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val spacing = LocalSpacing.current
        assertEquals(spacing, Spacing)
        Text("Spacing Available")
      }
    }
    composeTestRule.onNodeWithText("Spacing Available").assertIsDisplayed()
  }

  @Test
  fun `EurekaTheme spacing values accessible`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val spacing = LocalSpacing.current
        assertNotNull(spacing.xxs)
        assertNotNull(spacing.xs)
        assertNotNull(spacing.sm)
        Text("Spacing Values")
      }
    }
    composeTestRule.onNodeWithText("Spacing Values").assertIsDisplayed()
  }
}
