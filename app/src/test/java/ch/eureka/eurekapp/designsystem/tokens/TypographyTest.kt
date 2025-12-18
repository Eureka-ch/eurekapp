// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.unit.sp
import ch.eureka.eurekapp.ui.designsystem.tokens.ETypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TypographyTest {

  @Test
  fun typographyDisplayLarge_hasCorrectSize() {
    val displayLarge = ETypography.value.displayLarge
    assertNotNull("DisplayLarge should be defined", displayLarge)
    assertEquals("DisplayLarge should be 28sp", 28.sp, displayLarge.fontSize)
  }

  @Test
  fun typographyDisplayMedium_hasCorrectSize() {
    val displayMedium = ETypography.value.displayMedium
    assertNotNull("DisplayMedium should be defined", displayMedium)
    assertEquals("DisplayMedium should be 24sp", 24.sp, displayMedium.fontSize)
  }

  @Test
  fun typographyDisplaySmall_hasCorrectSize() {
    val displaySmall = ETypography.value.displaySmall
    assertNotNull("DisplaySmall should be defined", displaySmall)
    assertEquals("DisplaySmall should be 22sp", 22.sp, displaySmall.fontSize)
  }

  @Test
  fun typographyTitleLarge_hasCorrectSize() {
    val titleLarge = ETypography.value.titleLarge
    assertNotNull("TitleLarge should be defined", titleLarge)
    assertEquals("TitleLarge should be 22sp", 22.sp, titleLarge.fontSize)
  }

  @Test
  fun typographyTitleMedium_hasCorrectSize() {
    val titleMedium = ETypography.value.titleMedium
    assertNotNull("TitleMedium should be defined", titleMedium)
    assertEquals("TitleMedium should be 16sp", 16.sp, titleMedium.fontSize)
  }

  @Test
  fun typographyBodyLarge_hasCorrectSize() {
    val bodyLarge = ETypography.value.bodyLarge
    assertNotNull("BodyLarge should be defined", bodyLarge)
    assertEquals("BodyLarge should be 16sp", 16.sp, bodyLarge.fontSize)
  }

  @Test
  fun typographyBodyMedium_hasCorrectSize() {
    val bodyMedium = ETypography.value.bodyMedium
    assertNotNull("BodyMedium should be defined", bodyMedium)
    assertEquals("BodyMedium should be 14sp", 14.sp, bodyMedium.fontSize)
  }

  @Test
  fun typographyLabelLarge_hasCorrectSize() {
    val labelLarge = ETypography.value.labelLarge
    assertNotNull("LabelLarge should be defined", labelLarge)
    assertEquals("LabelLarge should be 14sp", 14.sp, labelLarge.fontSize)
  }

  @Test
  fun typographyLabelMedium_hasCorrectSize() {
    val labelMedium = ETypography.value.labelMedium
    assertNotNull("LabelMedium should be defined", labelMedium)
    assertEquals("LabelMedium should be 13sp", 13.sp, labelMedium.fontSize)
  }

  @Test
  fun typographyLabelSmall_hasCorrectSize() {
    val labelSmall = ETypography.value.labelSmall
    assertNotNull("LabelSmall should be defined", labelSmall)
    assertEquals("LabelSmall should be 11sp", 11.sp, labelSmall.fontSize)
  }
}
