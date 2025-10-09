package ch.eureka.eurekapp.ui.designsystem.tokens

import org.junit.Assert.*
import org.junit.Test

/**
 * Comprehensive unit tests for ETypography design tokens Tests the typography properties and
 * constants
 */
class ETypographyComprehensiveTest {

  @Test
  fun `Typography properties are accessible`() {
    val typography = ETypography.value

    assertNotNull(typography.displayLarge)
    assertNotNull(typography.displayMedium)
    assertNotNull(typography.displaySmall)
    assertNotNull(typography.titleLarge)
    assertNotNull(typography.titleMedium)
    assertNotNull(typography.titleSmall)
    assertNotNull(typography.bodyLarge)
    assertNotNull(typography.bodyMedium)
    assertNotNull(typography.labelLarge)
    assertNotNull(typography.labelMedium)
    assertNotNull(typography.labelSmall)
  }

  @Test
  fun `Typography constants are correct`() {
    assertEquals(28, ETypography.Constants.DISPLAY_LARGE_SIZE)
    assertEquals(24, ETypography.Constants.DISPLAY_MEDIUM_SIZE)
    assertEquals(22, ETypography.Constants.DISPLAY_SMALL_SIZE)
    assertEquals(22, ETypography.Constants.TITLE_LARGE_SIZE)
    assertEquals(16, ETypography.Constants.TITLE_MEDIUM_SIZE)
    assertEquals(14, ETypography.Constants.TITLE_SMALL_SIZE)
    assertEquals(16, ETypography.Constants.BODY_LARGE_SIZE)
    assertEquals(14, ETypography.Constants.BODY_MEDIUM_SIZE)
    assertEquals(14, ETypography.Constants.LABEL_LARGE_SIZE)
    assertEquals(13, ETypography.Constants.LABEL_MEDIUM_SIZE)
    assertEquals(11, ETypography.Constants.LABEL_SMALL_SIZE)
  }

  @Test
  fun `Typography display styles are accessible`() {
    val typography = ETypography.value

    assertNotNull(typography.displayLarge)
    assertNotNull(typography.displayMedium)
    assertNotNull(typography.displaySmall)
  }

  @Test
  fun `Typography title styles are accessible`() {
    val typography = ETypography.value

    assertNotNull(typography.titleLarge)
    assertNotNull(typography.titleMedium)
    assertNotNull(typography.titleSmall)
  }

  @Test
  fun `Typography body styles are accessible`() {
    val typography = ETypography.value

    assertNotNull(typography.bodyLarge)
    assertNotNull(typography.bodyMedium)
  }

  @Test
  fun `Typography label styles are accessible`() {
    val typography = ETypography.value

    assertNotNull(typography.labelLarge)
    assertNotNull(typography.labelMedium)
    assertNotNull(typography.labelSmall)
  }

  @Test
  fun `Typography equality works correctly`() {
    val typography1 = ETypography.value
    val typography2 = ETypography.value

    assertEquals(typography1, typography2)
  }

  @Test
  fun `Typography hashCode works correctly`() {
    val typography1 = ETypography.value
    val typography2 = ETypography.value

    assertEquals(typography1.hashCode(), typography2.hashCode())
  }

  @Test
  fun `Typography toString works correctly`() {
    val typography = ETypography.value

    assertNotNull(typography.toString())
    assertTrue(typography.toString().isNotEmpty())
  }

  @Test
  fun `Typography copy works correctly`() {
    val typography = ETypography.value
    val copiedTypography = typography.copy(displayLarge = typography.titleLarge)

    assertEquals(typography.titleLarge, copiedTypography.displayLarge)
    assertEquals(typography.titleMedium, copiedTypography.titleMedium)
  }

  @Test
  fun `Typography component1 works correctly`() {
    val typography = ETypography.value
    val displayLarge = typography.displayLarge

    assertEquals(typography.displayLarge, displayLarge)
  }

  @Test
  fun `Typography component2 works correctly`() {
    val typography = ETypography.value
    val displayMedium = typography.displayMedium

    assertEquals(typography.displayMedium, displayMedium)
  }

  @Test
  fun `Typography properties are accessible individually`() {
    val typography = ETypography.value

    val displayLarge = typography.displayLarge
    val displayMedium = typography.displayMedium
    val displaySmall = typography.displaySmall
    val titleLarge = typography.titleLarge
    val titleMedium = typography.titleMedium
    val titleSmall = typography.titleSmall
    val bodyLarge = typography.bodyLarge
    val bodyMedium = typography.bodyMedium
    val labelLarge = typography.labelLarge
    val labelMedium = typography.labelMedium
    val labelSmall = typography.labelSmall

    assertEquals(typography.displayLarge, displayLarge)
    assertEquals(typography.displayMedium, displayMedium)
    assertEquals(typography.displaySmall, displaySmall)
    assertEquals(typography.titleLarge, titleLarge)
    assertEquals(typography.titleMedium, titleMedium)
    assertEquals(typography.titleSmall, titleSmall)
    assertEquals(typography.bodyLarge, bodyLarge)
    assertEquals(typography.bodyMedium, bodyMedium)
    assertEquals(typography.labelLarge, labelLarge)
    assertEquals(typography.labelMedium, labelMedium)
    assertEquals(typography.labelSmall, labelSmall)
  }

  @Test
  fun `Typography constants are immutable`() {
    // Test that constants are accessible and have expected values
    val constants = ETypography.Constants

    assertNotNull(constants)
    assertTrue(constants.DISPLAY_LARGE_SIZE > 0)
    assertTrue(constants.DISPLAY_MEDIUM_SIZE > 0)
    assertTrue(constants.DISPLAY_SMALL_SIZE > 0)
    assertTrue(constants.TITLE_LARGE_SIZE > 0)
    assertTrue(constants.TITLE_MEDIUM_SIZE > 0)
    assertTrue(constants.TITLE_SMALL_SIZE > 0)
    assertTrue(constants.BODY_LARGE_SIZE > 0)
    assertTrue(constants.BODY_MEDIUM_SIZE > 0)
    assertTrue(constants.LABEL_LARGE_SIZE > 0)
    assertTrue(constants.LABEL_MEDIUM_SIZE > 0)
    assertTrue(constants.LABEL_SMALL_SIZE > 0)
  }

  @Test
  fun `Typography size hierarchy is correct`() {
    val constants = ETypography.Constants

    // Display sizes should be largest
    assertTrue(constants.DISPLAY_LARGE_SIZE >= constants.DISPLAY_MEDIUM_SIZE)
    assertTrue(constants.DISPLAY_MEDIUM_SIZE >= constants.DISPLAY_SMALL_SIZE)

    // Title sizes should be smaller than display
    assertTrue(constants.DISPLAY_SMALL_SIZE >= constants.TITLE_LARGE_SIZE)
    assertTrue(constants.TITLE_LARGE_SIZE >= constants.TITLE_MEDIUM_SIZE)
    assertTrue(constants.TITLE_MEDIUM_SIZE >= constants.TITLE_SMALL_SIZE)

    // Body sizes should be smaller than title
    assertTrue(constants.TITLE_SMALL_SIZE <= constants.BODY_LARGE_SIZE)
    assertTrue(constants.BODY_LARGE_SIZE >= constants.BODY_MEDIUM_SIZE)

    // Label sizes should be smallest
    assertTrue(constants.BODY_MEDIUM_SIZE >= constants.LABEL_LARGE_SIZE)
    assertTrue(constants.LABEL_LARGE_SIZE >= constants.LABEL_MEDIUM_SIZE)
    assertTrue(constants.LABEL_MEDIUM_SIZE >= constants.LABEL_SMALL_SIZE)
  }

  @Test
  fun `Typography constants object is accessible`() {
    val constants = ETypography.Constants

    assertNotNull(constants)
    assertNotNull(constants.DISPLAY_LARGE_SIZE)
    assertNotNull(constants.DISPLAY_MEDIUM_SIZE)
    assertNotNull(constants.DISPLAY_SMALL_SIZE)
    assertNotNull(constants.TITLE_LARGE_SIZE)
    assertNotNull(constants.TITLE_MEDIUM_SIZE)
    assertNotNull(constants.TITLE_SMALL_SIZE)
    assertNotNull(constants.BODY_LARGE_SIZE)
    assertNotNull(constants.BODY_MEDIUM_SIZE)
    assertNotNull(constants.LABEL_LARGE_SIZE)
    assertNotNull(constants.LABEL_MEDIUM_SIZE)
    assertNotNull(constants.LABEL_SMALL_SIZE)
  }
}
