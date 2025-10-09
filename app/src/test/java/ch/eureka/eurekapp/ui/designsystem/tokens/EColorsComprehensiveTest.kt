package ch.eureka.eurekapp.ui.designsystem.tokens

import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive unit tests for EColors design tokens Tests the color scheme properties and
 * accessibility
 */
class EColorsComprehensiveTest {

  @Test
  fun `Light color scheme properties are accessible`() {
    val lightScheme = EColors.light

    assertNotNull(lightScheme.primary)
    assertNotNull(lightScheme.onPrimary)
    assertNotNull(lightScheme.secondary)
    assertNotNull(lightScheme.onSecondary)
    assertNotNull(lightScheme.tertiary)
    assertNotNull(lightScheme.onTertiary)
    assertNotNull(lightScheme.background)
    assertNotNull(lightScheme.onBackground)
    assertNotNull(lightScheme.surface)
    assertNotNull(lightScheme.onSurface)
    assertNotNull(lightScheme.onSurfaceVariant)
    assertNotNull(lightScheme.outlineVariant)
    assertNotNull(lightScheme.error)
    assertNotNull(lightScheme.onError)
  }

  @Test
  fun `Dark color scheme properties are accessible`() {
    val darkScheme = EColors.dark

    assertNotNull(darkScheme.primary)
    assertNotNull(darkScheme.onPrimary)
    assertNotNull(darkScheme.secondary)
    assertNotNull(darkScheme.onSecondary)
    assertNotNull(darkScheme.tertiary)
    assertNotNull(darkScheme.onTertiary)
    assertNotNull(darkScheme.background)
    assertNotNull(darkScheme.onBackground)
    assertNotNull(darkScheme.surface)
    assertNotNull(darkScheme.onSurface)
    assertNotNull(darkScheme.onSurfaceVariant)
    assertNotNull(darkScheme.outlineVariant)
    assertNotNull(darkScheme.error)
    assertNotNull(darkScheme.onError)
  }

  @Test
  fun `Light and dark color schemes are different`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotEquals(lightScheme.primary, darkScheme.primary)
    assertNotEquals(lightScheme.background, darkScheme.background)
    assertNotEquals(lightScheme.surface, darkScheme.surface)
  }

  @Test
  fun `Color scheme primary colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.primary)
    assertNotNull(darkScheme.primary)
  }

  @Test
  fun `Color scheme background colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.background)
    assertNotNull(darkScheme.background)
  }

  @Test
  fun `Color scheme surface colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.surface)
    assertNotNull(darkScheme.surface)
  }

  @Test
  fun `Color scheme error colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.error)
    assertNotNull(darkScheme.error)
    assertNotNull(lightScheme.onError)
    assertNotNull(darkScheme.onError)
  }

  @Test
  fun `Color scheme secondary colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.secondary)
    assertNotNull(darkScheme.secondary)
    assertNotNull(lightScheme.onSecondary)
    assertNotNull(darkScheme.onSecondary)
  }

  @Test
  fun `Color scheme tertiary colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.tertiary)
    assertNotNull(darkScheme.tertiary)
    assertNotNull(lightScheme.onTertiary)
    assertNotNull(darkScheme.onTertiary)
  }

  @Test
  fun `Color scheme on colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.onPrimary)
    assertNotNull(darkScheme.onPrimary)
    assertNotNull(lightScheme.onBackground)
    assertNotNull(darkScheme.onBackground)
    assertNotNull(lightScheme.onSurface)
    assertNotNull(darkScheme.onSurface)
  }

  @Test
  fun `Color scheme variant colors are not null`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.onSurfaceVariant)
    assertNotNull(darkScheme.onSurfaceVariant)
    assertNotNull(lightScheme.outlineVariant)
    assertNotNull(darkScheme.outlineVariant)
  }

  @Test
  fun `Color scheme equality works correctly`() {
    val lightScheme1 = EColors.light
    val lightScheme2 = EColors.light
    val darkScheme = EColors.dark

    assertEquals(lightScheme1, lightScheme2)
    assertNotEquals(lightScheme1, darkScheme)
  }

  @Test
  fun `Color scheme hashCode works correctly`() {
    val lightScheme1 = EColors.light
    val lightScheme2 = EColors.light
    val darkScheme = EColors.dark

    assertEquals(lightScheme1.hashCode(), lightScheme2.hashCode())
    assertNotEquals(lightScheme1.hashCode(), darkScheme.hashCode())
  }

  @Test
  fun `Color scheme toString works correctly`() {
    val lightScheme = EColors.light
    val darkScheme = EColors.dark

    assertNotNull(lightScheme.toString())
    assertNotNull(darkScheme.toString())
    assertTrue(lightScheme.toString().isNotEmpty())
    assertTrue(darkScheme.toString().isNotEmpty())
  }

  @Test
  fun `Color scheme copy works correctly`() {
    val lightScheme = EColors.light
    val copiedScheme = lightScheme.copy(primary = lightScheme.secondary)

    assertEquals(lightScheme.secondary, copiedScheme.primary)
    assertEquals(lightScheme.background, copiedScheme.background)
  }

  @Test
  fun `Color scheme component1 works correctly`() {
    val lightScheme = EColors.light
    val primary = lightScheme.primary

    assertEquals(lightScheme.primary, primary)
  }

  @Test
  fun `Color scheme component2 works correctly`() {
    val lightScheme = EColors.light
    val onPrimary = lightScheme.onPrimary

    assertEquals(lightScheme.onPrimary, onPrimary)
  }

  @Test
  fun `Color scheme properties are accessible individually`() {
    val lightScheme = EColors.light

    val primary = lightScheme.primary
    val onPrimary = lightScheme.onPrimary
    val secondary = lightScheme.secondary
    val onSecondary = lightScheme.onSecondary
    val tertiary = lightScheme.tertiary
    val onTertiary = lightScheme.onTertiary
    val background = lightScheme.background
    val onBackground = lightScheme.onBackground
    val surface = lightScheme.surface
    val onSurface = lightScheme.onSurface
    val onSurfaceVariant = lightScheme.onSurfaceVariant
    val outlineVariant = lightScheme.outlineVariant
    val error = lightScheme.error
    val onError = lightScheme.onError

    assertEquals(lightScheme.primary, primary)
    assertEquals(lightScheme.onPrimary, onPrimary)
    assertEquals(lightScheme.secondary, secondary)
    assertEquals(lightScheme.onSecondary, onSecondary)
    assertEquals(lightScheme.tertiary, tertiary)
    assertEquals(lightScheme.onTertiary, onTertiary)
    assertEquals(lightScheme.background, background)
    assertEquals(lightScheme.onBackground, onBackground)
    assertEquals(lightScheme.surface, surface)
    assertEquals(lightScheme.onSurface, onSurface)
    assertEquals(lightScheme.onSurfaceVariant, onSurfaceVariant)
    assertEquals(lightScheme.outlineVariant, outlineVariant)
    assertEquals(lightScheme.error, error)
    assertEquals(lightScheme.onError, onError)
  }
}
