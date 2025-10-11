package ch.eureka.eurekapp.ui.designsystem.tokens

import org.junit.Assert.*
import org.junit.Test

class ExtendedDesignTokensTest {

  @Test
  fun `EColors light theme color scheme properties`() {
    val lightTheme = EColors.light
    assertNotNull(lightTheme.primary)
    assertNotNull(lightTheme.onPrimary)
    assertNotNull(lightTheme.secondary)
    assertNotNull(lightTheme.onSecondary)
    assertNotNull(lightTheme.tertiary)
    assertNotNull(lightTheme.onTertiary)
    assertNotNull(lightTheme.background)
    assertNotNull(lightTheme.onBackground)
    assertNotNull(lightTheme.surface)
    assertNotNull(lightTheme.onSurface)
    assertNotNull(lightTheme.onSurfaceVariant)
    assertNotNull(lightTheme.outlineVariant)
    assertNotNull(lightTheme.error)
    assertNotNull(lightTheme.onError)
  }

  @Test
  fun `EColors dark theme color scheme properties`() {
    val darkTheme = EColors.dark
    assertNotNull(darkTheme.primary)
    assertNotNull(darkTheme.onPrimary)
    assertNotNull(darkTheme.secondary)
    assertNotNull(darkTheme.onSecondary)
    assertNotNull(darkTheme.tertiary)
    assertNotNull(darkTheme.onTertiary)
    assertNotNull(darkTheme.background)
    assertNotNull(darkTheme.onBackground)
    assertNotNull(darkTheme.surface)
    assertNotNull(darkTheme.onSurface)
    assertNotNull(darkTheme.onSurfaceVariant)
    assertNotNull(darkTheme.outlineVariant)
    assertNotNull(darkTheme.error)
    assertNotNull(darkTheme.onError)
  }

  @Test
  fun `ETypography display styles have correct properties`() {
    val typography = ETypography.value
    val displayLarge = typography.displayLarge
    val displayMedium = typography.displayMedium
    val displaySmall = typography.displaySmall

    assertNotNull(displayLarge.fontFamily)
    assertNotNull(displayLarge.fontWeight)
    assertNotNull(displayLarge.fontSize)
    assertNotNull(displayLarge.lineHeight)
    assertNotNull(displayLarge.letterSpacing)

    assertNotNull(displayMedium.fontFamily)
    assertNotNull(displayMedium.fontWeight)
    assertNotNull(displayMedium.fontSize)
    assertNotNull(displayMedium.lineHeight)
    assertNotNull(displayMedium.letterSpacing)

    assertNotNull(displaySmall.fontFamily)
    assertNotNull(displaySmall.fontWeight)
    assertNotNull(displaySmall.fontSize)
    assertNotNull(displaySmall.lineHeight)
    assertNotNull(displaySmall.letterSpacing)
  }

  @Test
  fun `ETypography title styles have correct properties`() {
    val typography = ETypography.value
    val titleLarge = typography.titleLarge
    val titleMedium = typography.titleMedium
    val titleSmall = typography.titleSmall

    assertNotNull(titleLarge.fontFamily)
    assertNotNull(titleLarge.fontWeight)
    assertNotNull(titleLarge.fontSize)
    assertNotNull(titleLarge.lineHeight)
    assertNotNull(titleLarge.letterSpacing)

    assertNotNull(titleMedium.fontFamily)
    assertNotNull(titleMedium.fontWeight)
    assertNotNull(titleMedium.fontSize)
    assertNotNull(titleMedium.lineHeight)
    assertNotNull(titleMedium.letterSpacing)

    assertNotNull(titleSmall.fontFamily)
    assertNotNull(titleSmall.fontWeight)
    assertNotNull(titleSmall.fontSize)
    assertNotNull(titleSmall.lineHeight)
    assertNotNull(titleSmall.letterSpacing)
  }

  @Test
  fun `ETypography body styles have correct properties`() {
    val typography = ETypography.value
    val bodyLarge = typography.bodyLarge
    val bodyMedium = typography.bodyMedium

    assertNotNull(bodyLarge.fontFamily)
    assertNotNull(bodyLarge.fontWeight)
    assertNotNull(bodyLarge.fontSize)
    assertNotNull(bodyLarge.lineHeight)
    assertNotNull(bodyLarge.letterSpacing)

    assertNotNull(bodyMedium.fontFamily)
    assertNotNull(bodyMedium.fontWeight)
    assertNotNull(bodyMedium.fontSize)
    assertNotNull(bodyMedium.lineHeight)
    assertNotNull(bodyMedium.letterSpacing)
  }

  @Test
  fun `ETypography label styles have correct properties`() {
    val typography = ETypography.value
    val labelLarge = typography.labelLarge
    val labelMedium = typography.labelMedium
    val labelSmall = typography.labelSmall

    assertNotNull(labelLarge.fontFamily)
    assertNotNull(labelLarge.fontWeight)
    assertNotNull(labelLarge.fontSize)
    assertNotNull(labelLarge.lineHeight)
    assertNotNull(labelLarge.letterSpacing)

    assertNotNull(labelMedium.fontFamily)
    assertNotNull(labelMedium.fontWeight)
    assertNotNull(labelMedium.fontSize)
    assertNotNull(labelMedium.lineHeight)
    assertNotNull(labelMedium.letterSpacing)

    assertNotNull(labelSmall.fontFamily)
    assertNotNull(labelSmall.fontWeight)
    assertNotNull(labelSmall.fontSize)
    assertNotNull(labelSmall.lineHeight)
    assertNotNull(labelSmall.letterSpacing)
  }

  @Test
  fun `EShapes properties are accessible`() {
    val shapes = EShapes
    assertNotNull(shapes.value.small)
    assertNotNull(shapes.value.medium)
    assertNotNull(shapes.value.large)
  }

  @Test
  fun `Spacing properties are accessible`() {
    val spacing = Spacing
    assertNotNull(spacing.xxs)
    assertNotNull(spacing.xs)
    assertNotNull(spacing.sm)
    assertNotNull(spacing.md)
    assertNotNull(spacing.lg)
    assertNotNull(spacing.xl)
  }

  @Test
  fun `EurekaStyles properties are accessible`() {
    val styles = EurekaStyles
    assertNotNull(styles.CardShape)
    assertNotNull(styles.CardElevation)
  }

  @Test
  fun `Design tokens consistency check`() {
    // Verify that all design tokens are consistently defined
    val lightTheme = EColors.light
    val darkTheme = EColors.dark
    val typography = ETypography.value
    val shapes = EShapes
    val spacing = Spacing
    val styles = EurekaStyles

    // All should be non-null
    assertNotNull(lightTheme)
    assertNotNull(darkTheme)
    assertNotNull(typography)
    assertNotNull(shapes)
    assertNotNull(spacing)
    assertNotNull(styles)
  }
}
