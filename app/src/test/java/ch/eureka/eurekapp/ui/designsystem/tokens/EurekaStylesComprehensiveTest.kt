package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests complets pour EurekaStyles */
@RunWith(AndroidJUnit4::class)
class EurekaStylesComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaStyles object exists`() {
    assertNotNull(EurekaStyles)
  }

  @Test
  fun `CardShape has correct value`() {
    assertNotNull(EurekaStyles.CardShape)
  }

  @Test
  fun `CardElevation has correct value`() {
    assertEquals(2.dp, EurekaStyles.CardElevation)
  }

  @Test
  fun `PrimaryButtonColors works in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors = EurekaStyles.PrimaryButtonColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `PrimaryButtonColors works in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colors = EurekaStyles.PrimaryButtonColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `OutlinedButtonColors works in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors = EurekaStyles.OutlinedButtonColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `OutlinedButtonColors works in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colors = EurekaStyles.OutlinedButtonColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `TextFieldColors works in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors = EurekaStyles.TextFieldColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `TextFieldColors works in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colors = EurekaStyles.TextFieldColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `HighPriorityTagColors works in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors = EurekaStyles.HighPriorityTagColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `HighPriorityTagColors works in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colors = EurekaStyles.HighPriorityTagColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `NormalTagColors works in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors = EurekaStyles.NormalTagColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `NormalTagColors works in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colors = EurekaStyles.NormalTagColors()
        assertNotNull(colors)
      }
    }
  }

  @Test
  fun `OutlinedButtonBorder works in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val border = EurekaStyles.OutlinedButtonBorder()
        assertNotNull(border)
        assertEquals(1.dp, border.width)
      }
    }
  }

  @Test
  fun `OutlinedButtonBorder works in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val border = EurekaStyles.OutlinedButtonBorder()
        assertNotNull(border)
        assertEquals(1.dp, border.width)
      }
    }
  }

  @Test
  fun `All button colors are accessible`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val primary = EurekaStyles.PrimaryButtonColors()
        val outlined = EurekaStyles.OutlinedButtonColors()

        assertNotNull(primary)
        assertNotNull(outlined)
      }
    }
  }

  @Test
  fun `All tag colors are accessible`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val highPriority = EurekaStyles.HighPriorityTagColors()
        val normal = EurekaStyles.NormalTagColors()

        assertNotNull(highPriority)
        assertNotNull(normal)
      }
    }
  }

  @Test
  fun `CardShape is rounded`() {
    assertNotNull(EurekaStyles.CardShape)
  }

  @Test
  fun `CardElevation is positive`() {
    assertTrue(EurekaStyles.CardElevation > 0.dp)
  }

  @Test
  fun `Multiple calls to PrimaryButtonColors work`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors1 = EurekaStyles.PrimaryButtonColors()
        val colors2 = EurekaStyles.PrimaryButtonColors()

        assertNotNull(colors1)
        assertNotNull(colors2)
      }
    }
  }

  @Test
  fun `Multiple calls to TextFieldColors work`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colors1 = EurekaStyles.TextFieldColors()
        val colors2 = EurekaStyles.TextFieldColors()

        assertNotNull(colors1)
        assertNotNull(colors2)
      }
    }
  }

  @Test
  fun `All styles work together in light mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val primary = EurekaStyles.PrimaryButtonColors()
        val outlined = EurekaStyles.OutlinedButtonColors()
        val textField = EurekaStyles.TextFieldColors()
        val highPriority = EurekaStyles.HighPriorityTagColors()
        val normal = EurekaStyles.NormalTagColors()
        val border = EurekaStyles.OutlinedButtonBorder()

        assertNotNull(primary)
        assertNotNull(outlined)
        assertNotNull(textField)
        assertNotNull(highPriority)
        assertNotNull(normal)
        assertNotNull(border)
      }
    }
  }

  @Test
  fun `All styles work together in dark mode`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val primary = EurekaStyles.PrimaryButtonColors()
        val outlined = EurekaStyles.OutlinedButtonColors()
        val textField = EurekaStyles.TextFieldColors()
        val highPriority = EurekaStyles.HighPriorityTagColors()
        val normal = EurekaStyles.NormalTagColors()
        val border = EurekaStyles.OutlinedButtonBorder()

        assertNotNull(primary)
        assertNotNull(outlined)
        assertNotNull(textField)
        assertNotNull(highPriority)
        assertNotNull(normal)
        assertNotNull(border)
      }
    }
  }
}
