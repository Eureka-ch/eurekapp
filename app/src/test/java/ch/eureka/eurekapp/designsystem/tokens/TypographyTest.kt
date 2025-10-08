package ch.eureka.eurekapp.designsystem.tokens

import androidx.compose.ui.unit.sp
import ch.eureka.eurekapp.ui.designsystem.tokens.ETypography
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TypographyTest {

  @Test
  fun `displayLarge has correct size`() {
    val displayLarge = ETypography.value.displayLarge
    assertNotNull("DisplayLarge should be defined", displayLarge)
    assertEquals("DisplayLarge should be 28sp", 28.sp, displayLarge.fontSize)
  }

  @Test
  fun `displayMedium has correct size`() {
    val displayMedium = ETypography.value.displayMedium
    assertNotNull("DisplayMedium should be defined", displayMedium)
    assertEquals("DisplayMedium should be 24sp", 24.sp, displayMedium.fontSize)
  }

  @Test
  fun `displaySmall has correct size`() {
    val displaySmall = ETypography.value.displaySmall
    assertNotNull("DisplaySmall should be defined", displaySmall)
    assertEquals("DisplaySmall should be 22sp", 22.sp, displaySmall.fontSize)
  }

  @Test
  fun `titleLarge has correct size`() {
    val titleLarge = ETypography.value.titleLarge
    assertNotNull("TitleLarge should be defined", titleLarge)
    assertEquals("TitleLarge should be 22sp", 22.sp, titleLarge.fontSize)
  }

  @Test
  fun `titleMedium has correct size`() {
    val titleMedium = ETypography.value.titleMedium
    assertNotNull("TitleMedium should be defined", titleMedium)
    assertEquals("TitleMedium should be 16sp", 16.sp, titleMedium.fontSize)
  }

  @Test
  fun `bodyLarge has correct size`() {
    val bodyLarge = ETypography.value.bodyLarge
    assertNotNull("BodyLarge should be defined", bodyLarge)
    assertEquals("BodyLarge should be 16sp", 16.sp, bodyLarge.fontSize)
  }

  @Test
  fun `bodyMedium has correct size`() {
    val bodyMedium = ETypography.value.bodyMedium
    assertNotNull("BodyMedium should be defined", bodyMedium)
    assertEquals("BodyMedium should be 14sp", 14.sp, bodyMedium.fontSize)
  }

  @Test
  fun `labelLarge has correct size`() {
    val labelLarge = ETypography.value.labelLarge
    assertNotNull("LabelLarge should be defined", labelLarge)
    assertEquals("LabelLarge should be 14sp", 14.sp, labelLarge.fontSize)
  }

  @Test
  fun `labelMedium has correct size`() {
    val labelMedium = ETypography.value.labelMedium
    assertNotNull("LabelMedium should be defined", labelMedium)
    assertEquals("LabelMedium should be 13sp", 13.sp, labelMedium.fontSize)
  }

  @Test
  fun `labelSmall has correct size`() {
    val labelSmall = ETypography.value.labelSmall
    assertNotNull("LabelSmall should be defined", labelSmall)
    assertEquals("LabelSmall should be 11sp", 11.sp, labelSmall.fontSize)
  }
}
