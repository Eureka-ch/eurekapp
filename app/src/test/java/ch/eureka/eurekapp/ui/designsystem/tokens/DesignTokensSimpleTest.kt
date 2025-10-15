package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.junit.Assert.*
import org.junit.Test

/** Tests simples pour tous les design tokens */
class DesignTokensSimpleTest {

  // ========== EColors Tests ==========

  @Test
  fun `EColors light scheme exists`() {
    assertNotNull(EColors.light)
  }

  @Test
  fun `EColors dark scheme exists`() {
    assertNotNull(EColors.dark)
  }

  @Test
  fun `EColors light primary is not null`() {
    assertNotNull(EColors.light.primary)
  }

  @Test
  fun `EColors dark primary is not null`() {
    assertNotNull(EColors.dark.primary)
  }

  @Test
  fun `EColors light and dark are different`() {
    assertNotEquals(EColors.light, EColors.dark)
  }

  // ========== ETypography Tests ==========

  @Test
  fun `ETypography value exists`() {
    assertNotNull(ETypography.value)
  }

  @Test
  fun `ETypography has display large`() {
    assertNotNull(ETypography.value.displayLarge)
  }

  @Test
  fun `ETypography has title large`() {
    assertNotNull(ETypography.value.titleLarge)
  }

  @Test
  fun `ETypography has body large`() {
    assertNotNull(ETypography.value.bodyLarge)
  }

  @Test
  fun `ETypography has label large`() {
    assertNotNull(ETypography.value.labelLarge)
  }

  @Test
  fun `ETypography constants are defined`() {
    assertTrue(ETypography.Constants.DISPLAY_LARGE_SIZE > 0)
    assertTrue(ETypography.Constants.TITLE_LARGE_SIZE > 0)
    assertTrue(ETypography.Constants.BODY_LARGE_SIZE > 0)
    assertTrue(ETypography.Constants.LABEL_LARGE_SIZE > 0)
  }

  @Test
  fun `ETypography displayLarge fontSize matches constant`() {
    assertEquals(
        ETypography.Constants.DISPLAY_LARGE_SIZE.sp, ETypography.value.displayLarge.fontSize)
  }

  @Test
  fun `ETypography titleLarge fontSize matches constant`() {
    assertEquals(ETypography.Constants.TITLE_LARGE_SIZE.sp, ETypography.value.titleLarge.fontSize)
  }

  @Test
  fun `ETypography bodyLarge fontSize matches constant`() {
    assertEquals(ETypography.Constants.BODY_LARGE_SIZE.sp, ETypography.value.bodyLarge.fontSize)
  }

  @Test
  fun `ETypography labelLarge fontSize matches constant`() {
    assertEquals(ETypography.Constants.LABEL_LARGE_SIZE.sp, ETypography.value.labelLarge.fontSize)
  }

  // ========== EShapes Tests ==========

  @Test
  fun `EShapes value exists`() {
    assertNotNull(EShapes.value)
  }

  @Test
  fun `EShapes has small`() {
    assertNotNull(EShapes.value.small)
  }

  @Test
  fun `EShapes has medium`() {
    assertNotNull(EShapes.value.medium)
  }

  @Test
  fun `EShapes has large`() {
    assertNotNull(EShapes.value.large)
  }

  // ========== Spacing Tests ==========

  @Test
  fun `Spacing xxs exists`() {
    assertEquals(4.dp, Spacing.xxs)
  }

  @Test
  fun `Spacing xs exists`() {
    assertEquals(8.dp, Spacing.xs)
  }

  @Test
  fun `Spacing sm exists`() {
    assertEquals(12.dp, Spacing.sm)
  }

  @Test
  fun `Spacing md exists`() {
    assertEquals(16.dp, Spacing.md)
  }

  @Test
  fun `Spacing lg exists`() {
    assertEquals(24.dp, Spacing.lg)
  }

  @Test
  fun `Spacing xl exists`() {
    assertEquals(32.dp, Spacing.xl)
  }

  @Test
  fun `Spacing values are positive`() {
    assertTrue(Spacing.xxs > 0.dp)
    assertTrue(Spacing.xs > 0.dp)
    assertTrue(Spacing.sm > 0.dp)
    assertTrue(Spacing.md > 0.dp)
    assertTrue(Spacing.lg > 0.dp)
    assertTrue(Spacing.xl > 0.dp)
  }

  @Test
  fun `Spacing values increase`() {
    assertTrue(Spacing.xxs < Spacing.xs)
    assertTrue(Spacing.xs < Spacing.sm)
    assertTrue(Spacing.sm < Spacing.md)
    assertTrue(Spacing.md < Spacing.lg)
    assertTrue(Spacing.lg < Spacing.xl)
  }

  // ========== Integration Tests ==========

  @Test
  fun `All tokens are accessible`() {
    assertNotNull(EColors.light)
    assertNotNull(EColors.dark)
    assertNotNull(ETypography.value)
    assertNotNull(EShapes.value)
    assertTrue(Spacing.md > 0.dp)
  }

  @Test
  fun `Typography constants match actual sizes`() {
    assertEquals(ETypography.Constants.DISPLAY_LARGE_SIZE, 28)
    assertEquals(ETypography.Constants.TITLE_LARGE_SIZE, 22)
    assertEquals(ETypography.Constants.BODY_LARGE_SIZE, 16)
    assertEquals(ETypography.Constants.LABEL_LARGE_SIZE, 14)
  }

  @Test
  fun `Spacing follows Material Design scale`() {
    // Material Design uses 4dp increments
    assertEquals(4, Spacing.xxs.value.toInt())
    assertEquals(8, Spacing.xs.value.toInt())
    assertEquals(12, Spacing.sm.value.toInt())
    assertEquals(16, Spacing.md.value.toInt())
    assertEquals(24, Spacing.lg.value.toInt())
    assertEquals(32, Spacing.xl.value.toInt())
  }
}
