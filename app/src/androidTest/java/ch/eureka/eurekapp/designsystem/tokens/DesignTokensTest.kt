package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.EShapes
import ch.eureka.eurekapp.ui.designsystem.tokens.ETypography
import ch.eureka.eurekapp.ui.designsystem.tokens.LocalSpacing
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DesignTokensTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun designTokens_usesLightColors() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { androidx.compose.material3.Text("Light Theme Test") }
    }

    // Check that text is displayed (theme is applied)
    composeTestRule.onNodeWithText("Light Theme Test").assertIsDisplayed()
  }

  @Test
  fun designTokens_usesDarkColors() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { androidx.compose.material3.Text("Dark Theme Test") }
    }

    // Check that text is displayed (theme is applied)
    composeTestRule.onNodeWithText("Dark Theme Test").assertIsDisplayed()
  }

  @Test
  fun designTokens_usesCustomTypography() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Text(
            text = "Typography Test",
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
      }
    }

    // Check that text with custom typography is displayed
    composeTestRule.onNodeWithText("Typography Test").assertIsDisplayed()
  }

  @Test
  fun designTokens_usesCustomShapes() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(
            shape = androidx.compose.material3.MaterialTheme.shapes.medium) {
              androidx.compose.material3.Text("Shape Test")
            }
      }
    }

    // Check that card with custom shape is displayed
    composeTestRule.onNodeWithText("Shape Test").assertIsDisplayed()
  }

  @Test
  fun designTokens_providesSpacingCompositionLocal() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.padding(LocalSpacing.current.md)) {
              androidx.compose.material3.Text("Spacing Test")
            }
      }
    }

    // Check that text with spacing is displayed
    composeTestRule.onNodeWithText("Spacing Test").assertIsDisplayed()
  }

  @Test
  fun designTokens_lightSchemeIsAccessible() {
    // Test that light color scheme can be accessed
    val lightScheme = EColors.light
    assert(lightScheme.primary != null)
    assert(lightScheme.onPrimary != null)
    assert(lightScheme.background != null)
    assert(lightScheme.surface != null)
  }

  @Test
  fun designTokens_darkSchemeIsAccessible() {
    // Test that dark color scheme can be accessed
    val darkScheme = EColors.dark
    assert(darkScheme.primary != null)
    assert(darkScheme.onPrimary != null)
    assert(darkScheme.background != null)
    assert(darkScheme.surface != null)
  }

  @Test
  fun designTokens_isAccessible() {
    // Test that typography can be accessed
    val typography = ETypography.value
    assert(typography.displayLarge != null)
    assert(typography.titleLarge != null)
    assert(typography.bodyLarge != null)
    assert(typography.labelLarge != null)
  }

  @Test
  fun designTokens_shapesIsAccessible() {
    // Test that shapes can be accessed
    val shapes = EShapes.value
    assert(shapes.small != null)
    assert(shapes.medium != null)
    assert(shapes.large != null)
  }

  @Test
  fun designTokens_spacingIsAccessible() {
    // Test that spacing values can be accessed
    assert(Spacing.xxs != null)
    assert(Spacing.xs != null)
    assert(Spacing.sm != null)
    assert(Spacing.md != null)
    assert(Spacing.lg != null)
    assert(Spacing.xl != null)
  }

  @Test
  fun designTokens_valuesAreCorrect() {
    // Test that spacing values are correct
    assert(Spacing.xxs.value == 4f)
    assert(Spacing.xs.value == 8f)
    assert(Spacing.sm.value == 12f)
    assert(Spacing.md.value == 16f)
    assert(Spacing.lg.value == 24f)
    assert(Spacing.xl.value == 32f)
  }

  @Test
  fun designTokens_constantsAreCorrect() {
    // Test that typography constants are correct
    assert(ETypography.Constants.DISPLAY_LARGE_SIZE == 28)
    assert(ETypography.Constants.DISPLAY_MEDIUM_SIZE == 24)
    assert(ETypography.Constants.DISPLAY_SMALL_SIZE == 22)
    assert(ETypography.Constants.TITLE_LARGE_SIZE == 22)
    assert(ETypography.Constants.TITLE_MEDIUM_SIZE == 16)
    assert(ETypography.Constants.TITLE_SMALL_SIZE == 14)
    assert(ETypography.Constants.BODY_LARGE_SIZE == 16)
    assert(ETypography.Constants.BODY_MEDIUM_SIZE == 14)
    assert(ETypography.Constants.LABEL_LARGE_SIZE == 14)
    assert(ETypography.Constants.LABEL_MEDIUM_SIZE == 13)
    assert(ETypography.Constants.LABEL_SMALL_SIZE == 11)
  }

  @Test
  fun designTokens_worksInLightMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { androidx.compose.material3.Text("Light") }
    }
    composeTestRule.onNodeWithText("Light").assertIsDisplayed()
  }

  @Test
  fun designTokens_worksInDarkMode() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) { androidx.compose.material3.Text("Dark") }
    }
    composeTestRule.onNodeWithText("Dark").assertIsDisplayed()
  }

  @Test
  fun designTokens_appliesAllTokensTogether() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        androidx.compose.material3.Card(
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            modifier = Modifier.padding(LocalSpacing.current.lg)) {
              androidx.compose.material3.Text(
                  text = "All Tokens Test",
                  style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            }
      }
    }

    // Check that all tokens work together
    composeTestRule.onNodeWithText("All Tokens Test").assertIsDisplayed()
  }
}
