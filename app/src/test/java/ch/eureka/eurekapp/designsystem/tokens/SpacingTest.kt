package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.LocalSpacing
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class SpacingTest {

  @Test
  fun `spacing values are correct`() {
    assertEquals("XXS spacing should be 4dp", 4.dp, Spacing.xxs)
    assertEquals("XS spacing should be 8dp", 8.dp, Spacing.xs)
    assertEquals("SM spacing should be 12dp", 12.dp, Spacing.sm)
    assertEquals("MD spacing should be 16dp", 16.dp, Spacing.md)
    assertEquals("LG spacing should be 24dp", 24.dp, Spacing.lg)
    assertEquals("XL spacing should be 32dp", 32.dp, Spacing.xl)
  }

  @Test
  fun `LocalSpacing is properly defined`() {
    // Verify that LocalSpacing is properly defined
    assertNotNull("LocalSpacing should be defined", LocalSpacing)

    // Test that we can access Spacing object
    assertNotNull("Spacing object should be accessible", Spacing)

    // Verify spacing values match expected values
    assertEquals("XXS should be 4dp", 4.dp, Spacing.xxs)
    assertEquals("XS should be 8dp", 8.dp, Spacing.xs)
    assertEquals("SM should be 12dp", 12.dp, Spacing.sm)
    assertEquals("MD should be 16dp", 16.dp, Spacing.md)
    assertEquals("LG should be 24dp", 24.dp, Spacing.lg)
    assertEquals("XL should be 32dp", 32.dp, Spacing.xl)
  }

  @Test
  fun `spacing values are in ascending order`() {
    // Verify spacing values increase in order
    assert(Spacing.xxs < Spacing.xs) { "XXS should be smaller than XS" }
    assert(Spacing.xs < Spacing.sm) { "XS should be smaller than SM" }
    assert(Spacing.sm < Spacing.md) { "SM should be smaller than MD" }
    assert(Spacing.md < Spacing.lg) { "MD should be smaller than LG" }
    assert(Spacing.lg < Spacing.xl) { "LG should be smaller than XL" }
  }
}
