package ch.eureka.eurekapp.designsystem.tokens

import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ColorsTest {

  @Test
  fun `light and dark themes have different primary colors`() {
    // Verify that light and dark themes use different primary colors
    assertNotEquals(EColors.light.primary, EColors.dark.primary)
  }

  @Test
  fun `light theme has essential color roles set`() {
    // Ensure essential roles are properly defined
    assertNotNull("Primary color should be set", EColors.light.primary)
    assertNotNull("OnPrimary color should be set", EColors.light.onPrimary)
    assertNotNull("Secondary color should be set", EColors.light.secondary)
    assertNotNull("OnSecondary color should be set", EColors.light.onSecondary)
    assertNotNull("Tertiary color should be set", EColors.light.tertiary)
    assertNotNull("OnTertiary color should be set", EColors.light.onTertiary)
    assertNotNull("Background color should be set", EColors.light.background)
    assertNotNull("OnBackground color should be set", EColors.light.onBackground)
    assertNotNull("Surface color should be set", EColors.light.surface)
    assertNotNull("OnSurface color should be set", EColors.light.onSurface)
    assertNotNull("OnSurfaceVariant color should be set", EColors.light.onSurfaceVariant)
    assertNotNull("OutlineVariant color should be set", EColors.light.outlineVariant)
    assertNotNull("Error color should be set", EColors.light.error)
    assertNotNull("OnError color should be set", EColors.light.onError)
  }

  @Test
  fun `dark theme has essential color roles set`() {
    // Ensure essential roles are properly defined
    assertNotNull("Primary color should be set", EColors.dark.primary)
    assertNotNull("OnPrimary color should be set", EColors.dark.onPrimary)
    assertNotNull("Secondary color should be set", EColors.dark.secondary)
    assertNotNull("OnSecondary color should be set", EColors.dark.onSecondary)
    assertNotNull("Tertiary color should be set", EColors.dark.tertiary)
    assertNotNull("OnTertiary color should be set", EColors.dark.onTertiary)
    assertNotNull("Background color should be set", EColors.dark.background)
    assertNotNull("OnBackground color should be set", EColors.dark.onBackground)
    assertNotNull("Surface color should be set", EColors.dark.surface)
    assertNotNull("OnSurface color should be set", EColors.dark.onSurface)
    assertNotNull("OnSurfaceVariant color should be set", EColors.dark.onSurfaceVariant)
    assertNotNull("OutlineVariant color should be set", EColors.dark.outlineVariant)
    assertNotNull("Error color should be set", EColors.dark.error)
    assertNotNull("OnError color should be set", EColors.dark.onError)
  }

  @Test
  fun `onSurface colors are not transparent`() {
    // Verify that text colors have proper alpha values
    assertNotEquals("OnSurface should not be transparent", 0f, EColors.light.onSurface.alpha)
    assertNotEquals("OnSurface should not be transparent", 0f, EColors.dark.onSurface.alpha)
  }
}
