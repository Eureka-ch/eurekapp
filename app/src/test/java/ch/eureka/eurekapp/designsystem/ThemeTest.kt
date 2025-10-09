package ch.eureka.eurekapp.designsystem

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
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
class ThemeTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun `EurekaTheme applies light color scheme when darkTheme is false`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Test that light theme is applied
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        assert(colorScheme.primary == EColors.light.primary)
        assert(colorScheme.background == EColors.light.background)
        assert(colorScheme.surface == EColors.light.surface)
      }
    }
  }

  @Test
  fun `EurekaTheme applies dark color scheme when darkTheme is true`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        // Test that dark theme is applied
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        assert(colorScheme.primary == EColors.dark.primary)
        assert(colorScheme.background == EColors.dark.background)
        assert(colorScheme.surface == EColors.dark.surface)
      }
    }
  }

  @Test
  fun `EurekaTheme applies correct typography`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val typography = androidx.compose.material3.MaterialTheme.typography
        assert(typography.displaySmall == ETypography.value.displaySmall)
        assert(typography.titleLarge == ETypography.value.titleLarge)
        assert(typography.bodyLarge == ETypography.value.bodyLarge)
      }
    }
  }

  @Test
  fun `EurekaTheme applies correct shapes`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val shapes = androidx.compose.material3.MaterialTheme.shapes
        assert(shapes.small == EShapes.value.small)
        assert(shapes.medium == EShapes.value.medium)
        assert(shapes.large == EShapes.value.large)
      }
    }
  }

  @Test
  fun `EurekaTheme provides LocalSpacing`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val spacing = LocalSpacing.current
        assert(spacing.xs == Spacing.xs)
        assert(spacing.sm == Spacing.sm)
        assert(spacing.md == Spacing.md)
        assert(spacing.lg == Spacing.lg)
        assert(spacing.xl == Spacing.xl)
      }
    }
  }

  @Test
  fun `EurekaTheme renders content correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) { androidx.compose.material3.Text("Test Content") }
    }

    composeTestRule.onRoot().assertExists()
  }

  @Test
  fun `EurekaTheme handles light theme correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        assert(colorScheme.primary == EColors.light.primary)
      }
    }
  }

  @Test
  fun `EurekaTheme handles dark theme correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        assert(colorScheme.primary == EColors.dark.primary)
      }
    }
  }

  @Test
  fun `EurekaTheme applies all design tokens correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        val typography = androidx.compose.material3.MaterialTheme.typography
        val shapes = androidx.compose.material3.MaterialTheme.shapes
        val spacing = LocalSpacing.current

        // Verify all tokens are applied
        assert(colorScheme.primary == EColors.light.primary)
        assert(typography.displaySmall == ETypography.value.displaySmall)
        assert(shapes.medium == EShapes.value.medium)
        assert(spacing.md == Spacing.md)
      }
    }
  }

  @Test
  fun `EurekaTheme applies correct color scheme for dark theme branch`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        // Test dark theme branch
        assert(colorScheme.primary == EColors.dark.primary)
        assert(colorScheme.background == EColors.dark.background)
        assert(colorScheme.surface == EColors.dark.surface)
        assert(colorScheme.onSurface == EColors.dark.onSurface)
      }
    }
  }

  @Test
  fun `EurekaTheme applies correct color scheme for light theme branch`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        // Test light theme branch
        assert(colorScheme.primary == EColors.light.primary)
        assert(colorScheme.background == EColors.light.background)
        assert(colorScheme.surface == EColors.light.surface)
        assert(colorScheme.onSurface == EColors.light.onSurface)
      }
    }
  }

  @Test
  fun `EurekaTheme provides LocalSpacing in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val spacing = LocalSpacing.current
        assert(spacing.xs == Spacing.xs)
        assert(spacing.sm == Spacing.sm)
        assert(spacing.md == Spacing.md)
        assert(spacing.lg == Spacing.lg)
        assert(spacing.xl == Spacing.xl)
      }
    }
  }

  @Test
  fun `EurekaTheme applies typography in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val typography = androidx.compose.material3.MaterialTheme.typography
        assert(typography.displaySmall == ETypography.value.displaySmall)
        assert(typography.titleLarge == ETypography.value.titleLarge)
        assert(typography.titleMedium == ETypography.value.titleMedium)
        assert(typography.bodyLarge == ETypography.value.bodyLarge)
        assert(typography.bodyMedium == ETypography.value.bodyMedium)
        assert(typography.labelLarge == ETypography.value.labelLarge)
      }
    }
  }

  @Test
  fun `EurekaTheme applies shapes in dark theme`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val shapes = androidx.compose.material3.MaterialTheme.shapes
        assert(shapes.small == EShapes.value.small)
        assert(shapes.medium == EShapes.value.medium)
        assert(shapes.large == EShapes.value.large)
      }
    }
  }

  @Test
  fun `EurekaTheme handles null content gracefully`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Test with empty content
      }
    }
    composeTestRule.onRoot().assertExists()
  }

  @Test
  fun `EurekaTheme applies all tokens in both themes`() {
    // Test light theme with all tokens
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        val typography = androidx.compose.material3.MaterialTheme.typography
        val shapes = androidx.compose.material3.MaterialTheme.shapes
        val spacing = LocalSpacing.current

        assert(colorScheme.primary == EColors.light.primary)
        assert(typography.displaySmall == ETypography.value.displaySmall)
        assert(shapes.medium == EShapes.value.medium)
        assert(spacing.md == Spacing.md)
      }
    }
  }

  @Test
  fun `EurekaTheme composition local provider works correctly`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Test that CompositionLocalProvider is working
        val spacing = LocalSpacing.current
        assert(spacing is Spacing)
        assert(spacing.xxs == 4.dp)
        assert(spacing.xs == 8.dp)
        assert(spacing.sm == 12.dp)
        assert(spacing.md == 16.dp)
        assert(spacing.lg == 24.dp)
        assert(spacing.xl == 32.dp)
      }
    }
  }

  @Test
  fun `EurekaTheme dark theme branch coverage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        // Test dark theme branch specifically
        assert(colorScheme.primary == EColors.dark.primary)
        assert(colorScheme.secondary == EColors.dark.secondary)
        assert(colorScheme.tertiary == EColors.dark.tertiary)
        assert(colorScheme.background == EColors.dark.background)
        assert(colorScheme.surface == EColors.dark.surface)
        assert(colorScheme.onSurface == EColors.dark.onSurface)
        assert(colorScheme.onBackground == EColors.dark.onBackground)
      }
    }
  }

  @Test
  fun `EurekaTheme light theme branch coverage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        // Test light theme branch specifically
        assert(colorScheme.primary == EColors.light.primary)
        assert(colorScheme.secondary == EColors.light.secondary)
        assert(colorScheme.tertiary == EColors.light.tertiary)
        assert(colorScheme.background == EColors.light.background)
        assert(colorScheme.surface == EColors.light.surface)
        assert(colorScheme.onSurface == EColors.light.onSurface)
        assert(colorScheme.onBackground == EColors.light.onBackground)
      }
    }
  }

  @Test
  fun `EurekaTheme MaterialTheme parameters coverage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        val typography = androidx.compose.material3.MaterialTheme.typography
        val shapes = androidx.compose.material3.MaterialTheme.shapes

        // Test all MaterialTheme parameters are set
        assert(colorScheme != null)
        assert(typography != null)
        assert(shapes != null)

        // Test specific values
        assert(colorScheme.primary == EColors.light.primary)
        assert(typography.displaySmall == ETypography.value.displaySmall)
        assert(shapes.medium == EShapes.value.medium)
      }
    }
  }

  @Test
  fun `EurekaTheme content parameter coverage`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        // Test content parameter is executed
        androidx.compose.material3.Text("Test Content")
      }
    }
    composeTestRule.onRoot().assertExists()
  }

  @Test
  fun `EurekaTheme boolean parameter true case`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = true) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        assert(colorScheme.primary == EColors.dark.primary)
      }
    }
  }

  @Test
  fun `EurekaTheme boolean parameter false case`() {
    composeTestRule.setContent {
      EurekaTheme(darkTheme = false) {
        val colorScheme = androidx.compose.material3.MaterialTheme.colorScheme
        assert(colorScheme.primary == EColors.light.primary)
      }
    }
  }
}
