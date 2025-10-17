package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Typography scale used across headings, labels and body text. */
object ETypography {

  // Typography constants for easy maintenance
  object Constants {
    const val DISPLAY_LARGE_SIZE = 28
    const val DISPLAY_MEDIUM_SIZE = 24
    const val DISPLAY_SMALL_SIZE = 22
    const val TITLE_LARGE_SIZE = 22
    const val TITLE_MEDIUM_SIZE = 16
    const val TITLE_SMALL_SIZE = 14
    const val BODY_LARGE_SIZE = 16
    const val BODY_MEDIUM_SIZE = 14
    const val LABEL_LARGE_SIZE = 14
    const val LABEL_MEDIUM_SIZE = 13
    const val LABEL_SMALL_SIZE = 11
  }

  val value =
      Typography(
          // Headlines: 32sp
          headlineLarge =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Bold,
                  fontSize = 32.sp,
                  lineHeight = 40.sp,
                  letterSpacing = 0.sp),
          // Titles: 22-28sp
          displayLarge =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Bold,
                  fontSize = Constants.DISPLAY_LARGE_SIZE.sp,
                  lineHeight = 36.sp,
                  letterSpacing = 0.sp),
          displayMedium =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Bold,
                  fontSize = Constants.DISPLAY_MEDIUM_SIZE.sp,
                  lineHeight = 32.sp,
                  letterSpacing = 0.sp),
          displaySmall =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = Constants.DISPLAY_SMALL_SIZE.sp,
                  lineHeight = 28.sp,
                  letterSpacing = 0.sp),
          titleLarge =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.SemiBold,
                  fontSize = Constants.TITLE_LARGE_SIZE.sp,
                  lineHeight = 28.sp,
                  letterSpacing = 0.sp),
          titleMedium =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Medium,
                  fontSize = Constants.TITLE_MEDIUM_SIZE.sp,
                  lineHeight = 24.sp,
                  letterSpacing = 0.15.sp),
          titleSmall =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Medium,
                  fontSize = Constants.TITLE_SMALL_SIZE.sp,
                  lineHeight = 20.sp,
                  letterSpacing = 0.1.sp),
          // Body: 14-16sp
          bodyLarge =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Normal,
                  fontSize = Constants.BODY_LARGE_SIZE.sp,
                  lineHeight = 24.sp,
                  letterSpacing = 0.5.sp),
          bodyMedium =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Normal,
                  fontSize = Constants.BODY_MEDIUM_SIZE.sp,
                  lineHeight = 20.sp,
                  letterSpacing = 0.25.sp),
          // Labels: 11-13sp
          labelLarge =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Medium,
                  fontSize = Constants.LABEL_LARGE_SIZE.sp,
                  lineHeight = 20.sp,
                  letterSpacing = 0.1.sp),
          labelMedium =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Medium,
                  fontSize = Constants.LABEL_MEDIUM_SIZE.sp,
                  lineHeight = 18.sp,
                  letterSpacing = 0.5.sp),
          labelSmall =
              TextStyle(
                  fontFamily = FontFamily.Default,
                  fontWeight = FontWeight.Medium,
                  fontSize = Constants.LABEL_SMALL_SIZE.sp,
                  lineHeight = 16.sp,
                  letterSpacing = 0.5.sp))
}
