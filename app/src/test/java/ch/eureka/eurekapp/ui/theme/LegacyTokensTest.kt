package ch.eureka.eurekapp.ui.theme

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.ETypography
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LegacyTokensTest {

  @Test
  fun `LightColorScheme returns EColors light`() {
    val lightScheme = LightColorScheme
    assertEquals(EColors.light, lightScheme)
  }

  @Test
  fun `DarkColorScheme returns EColors dark`() {
    val darkScheme = DarkColorScheme
    assertEquals(EColors.dark, darkScheme)
  }

  @Test
  fun `Typography returns ETypography value`() {
    val typography = Typography
    assertEquals(ETypography.value, typography)
  }

  @Test
  fun `LightColorScheme primary color is not null`() {
    assertNotNull(LightColorScheme.primary)
  }

  @Test
  fun `DarkColorScheme primary color is not null`() {
    assertNotNull(DarkColorScheme.primary)
  }

  @Test
  fun `LightColorScheme secondary color is not null`() {
    assertNotNull(LightColorScheme.secondary)
  }

  @Test
  fun `DarkColorScheme secondary color is not null`() {
    assertNotNull(DarkColorScheme.secondary)
  }

  @Test
  fun `LightColorScheme tertiary color is not null`() {
    assertNotNull(LightColorScheme.tertiary)
  }

  @Test
  fun `DarkColorScheme tertiary color is not null`() {
    assertNotNull(DarkColorScheme.tertiary)
  }

  @Test
  fun `LightColorScheme background color is not null`() {
    assertNotNull(LightColorScheme.background)
  }

  @Test
  fun `DarkColorScheme background color is not null`() {
    assertNotNull(DarkColorScheme.background)
  }

  @Test
  fun `LightColorScheme surface color is not null`() {
    assertNotNull(LightColorScheme.surface)
  }

  @Test
  fun `DarkColorScheme surface color is not null`() {
    assertNotNull(DarkColorScheme.surface)
  }

  @Test
  fun `LightColorScheme error color is not null`() {
    assertNotNull(LightColorScheme.error)
  }

  @Test
  fun `DarkColorScheme error color is not null`() {
    assertNotNull(DarkColorScheme.error)
  }

  @Test
  fun `LightColorScheme onPrimary color is not null`() {
    assertNotNull(LightColorScheme.onPrimary)
  }

  @Test
  fun `DarkColorScheme onPrimary color is not null`() {
    assertNotNull(DarkColorScheme.onPrimary)
  }

  @Test
  fun `LightColorScheme onSecondary color is not null`() {
    assertNotNull(LightColorScheme.onSecondary)
  }

  @Test
  fun `DarkColorScheme onSecondary color is not null`() {
    assertNotNull(DarkColorScheme.onSecondary)
  }

  @Test
  fun `LightColorScheme onTertiary color is not null`() {
    assertNotNull(LightColorScheme.onTertiary)
  }

  @Test
  fun `DarkColorScheme onTertiary color is not null`() {
    assertNotNull(DarkColorScheme.onTertiary)
  }

  @Test
  fun `LightColorScheme onBackground color is not null`() {
    assertNotNull(LightColorScheme.onBackground)
  }

  @Test
  fun `DarkColorScheme onBackground color is not null`() {
    assertNotNull(DarkColorScheme.onBackground)
  }

  @Test
  fun `LightColorScheme onSurface color is not null`() {
    assertNotNull(LightColorScheme.onSurface)
  }

  @Test
  fun `DarkColorScheme onSurface color is not null`() {
    assertNotNull(DarkColorScheme.onSurface)
  }

  @Test
  fun `Typography displayLarge is not null`() {
    assertNotNull(Typography.displayLarge)
  }

  @Test
  fun `Typography displayMedium is not null`() {
    assertNotNull(Typography.displayMedium)
  }

  @Test
  fun `Typography displaySmall is not null`() {
    assertNotNull(Typography.displaySmall)
  }

  @Test
  fun `Typography titleLarge is not null`() {
    assertNotNull(Typography.titleLarge)
  }

  @Test
  fun `Typography titleMedium is not null`() {
    assertNotNull(Typography.titleMedium)
  }

  @Test
  fun `Typography titleSmall is not null`() {
    assertNotNull(Typography.titleSmall)
  }

  @Test
  fun `Typography bodyLarge is not null`() {
    assertNotNull(Typography.bodyLarge)
  }

  @Test
  fun `Typography bodyMedium is not null`() {
    assertNotNull(Typography.bodyMedium)
  }

  @Test
  fun `Typography labelLarge is not null`() {
    assertNotNull(Typography.labelLarge)
  }

  @Test
  fun `Typography labelMedium is not null`() {
    assertNotNull(Typography.labelMedium)
  }

  @Test
  fun `Typography labelSmall is not null`() {
    assertNotNull(Typography.labelSmall)
  }
}
