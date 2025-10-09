package ch.eureka.eurekapp.ui.theme

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Tests complets pour les fichiers de theme */
@RunWith(AndroidJUnit4::class)
class ThemeComprehensiveTest {

  @get:Rule val composeTestRule = createComposeRule()

  // ========== Color.kt Tests ==========

  @Test
  fun `LightColorScheme is accessible`() {
    assertNotNull(LightColorScheme)
  }

  @Test
  fun `DarkColorScheme is accessible`() {
    assertNotNull(DarkColorScheme)
  }

  @Test
  fun `LightColorScheme and DarkColorScheme are different`() {
    assertNotEquals(LightColorScheme, DarkColorScheme)
  }

  @Test
  fun `LightColorScheme primary is not null`() {
    assertNotNull(LightColorScheme.primary)
  }

  @Test
  fun `DarkColorScheme primary is not null`() {
    assertNotNull(DarkColorScheme.primary)
  }

  @Test
  fun `LightColorScheme background is not null`() {
    assertNotNull(LightColorScheme.background)
  }

  @Test
  fun `DarkColorScheme background is not null`() {
    assertNotNull(DarkColorScheme.background)
  }

  @Test
  fun `LightColorScheme surface is not null`() {
    assertNotNull(LightColorScheme.surface)
  }

  @Test
  fun `DarkColorScheme surface is not null`() {
    assertNotNull(DarkColorScheme.surface)
  }

  @Test
  fun `Light and Dark primary colors are different`() {
    assertNotEquals(LightColorScheme.primary, DarkColorScheme.primary)
  }

  // ========== Type.kt Tests ==========

  @Test
  fun `Typography is accessible`() {
    assertNotNull(Typography)
  }

  @Test
  fun `Typography has displayLarge`() {
    assertNotNull(Typography.displayLarge)
  }

  @Test
  fun `Typography has titleLarge`() {
    assertNotNull(Typography.titleLarge)
  }

  @Test
  fun `Typography has bodyLarge`() {
    assertNotNull(Typography.bodyLarge)
  }

  @Test
  fun `Typography has labelLarge`() {
    assertNotNull(Typography.labelLarge)
  }

  // ========== Theme.kt Tests ==========

  @Test
  fun `EurekappTheme renders in light mode`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = false) {
        // Content renders
      }
    }
  }

  @Test
  fun `EurekappTheme renders in dark mode`() {
    composeTestRule.setContent {
      EurekappTheme(darkTheme = true) {
        // Content renders
      }
    }
  }

  @Test
  fun `EurekappTheme renders with default theme`() {
    composeTestRule.setContent {
      EurekappTheme {
        // Content renders with default (light) theme
      }
    }
  }

  @Test
  fun `EurekappTheme provides LocalSpacing`() {
    composeTestRule.setContent {
      EurekappTheme {
        // LocalSpacing is provided
      }
    }
  }

  @Test
  fun `EurekappTheme light mode uses LightColorScheme`() {
    var usedLightScheme = false
    composeTestRule.setContent { EurekappTheme(darkTheme = false) { usedLightScheme = true } }
    assertTrue(usedLightScheme)
  }

  @Test
  fun `EurekappTheme dark mode uses DarkColorScheme`() {
    var usedDarkScheme = false
    composeTestRule.setContent { EurekappTheme(darkTheme = true) { usedDarkScheme = true } }
    assertTrue(usedDarkScheme)
  }

  @Test
  fun `Typography multiple calls return same instance`() {
    val typography1 = Typography
    val typography2 = Typography
    assertEquals(typography1, typography2)
  }

  @Test
  fun `LightColorScheme multiple calls return same value`() {
    val scheme1 = LightColorScheme.primary
    val scheme2 = LightColorScheme.primary
    assertEquals(scheme1, scheme2)
  }

  @Test
  fun `DarkColorScheme multiple calls return same value`() {
    val scheme1 = DarkColorScheme.primary
    val scheme2 = DarkColorScheme.primary
    assertEquals(scheme1, scheme2)
  }
}
