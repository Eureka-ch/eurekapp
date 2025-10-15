package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Defines the app color schemes (light/dark) mapped from the Figma palette. */
object EColors {

  // Brand colors from Figma templates
  private val PrimaryRed = Color(0xFFE83E3E) // Exact red from Figma header
  private val DarkPrimaryRed = Color(0xFFB71C1C) // Darker variant for dark theme
  private val SecondaryOrange = Color(0xFFFF9500) // Orange for warnings/meetings
  private val TertiaryGreen = Color(0xFF22C55E) // Green for success/tasks

  // Additional colors from Figma
  val IconColor = Color(0xFF424242) // Dark gray for icons
  val NotificationIconColor = Color(0xFFEF9A9A) // Light red for notification icons
  val HighPriorityBackground = Color(0xFFFFEBEE) // Light red background for high priority
  val HighPriorityText = Color(0xFFE57373) // Red text for high priority
  val TagBackground = Color(0xFFEEEEEE) // Light gray background for tags
  val TagText = Color(0xFF424242) // Dark gray text for tags
  val ButtonBorderColor = Color(0xFFE5E5E5) // Light gray border for outlined buttons
  val SuccessGreen = Color(0xFF4CAF50) // Green for success states and progress
  val WarningOrange = Color(0xFFFFB74D) // Orange for warnings
  val InfoBlue = Color(0xFF2196F3) // Blue for info states

  //Text colors
  val GrayTextColor2 = Color(0xFF64748B)
  val WhiteTextColor = Color.White
  val BlackTextColor = Color.Black
  val LightingBlue = Color(0xFF2563EB)

  //Border colors
  val BorderGrayColor = Color(0xFFE5E7EB)

  // Light theme colors from Figma
  private val LightBackground = Color(0xFFF5F5F5) // Light gray background
  private val LightSurface = Color.White // White cards/surfaces
  private val LightOnSurface = Color(0xFF212121) // Dark text for titles
  private val LightOnSurfaceVariant = Color(0xFF757575) // Medium gray for body text
  private val LightOutlineVariant = Color(0xFF9E9E9E) // Light gray for secondary text

  // Dark theme colors
  private val DarkBackground = Color(0xFF121212)
  private val DarkSurface = Color(0xFF1C1B1F)
  private val DarkOnSurface = Color(0xFFE6E1E5)
  private val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
  private val DarkOutlineVariant = Color(0xFF49454F)

  val light =
      lightColorScheme(
          primary = PrimaryRed,
          onPrimary = Color.White,
          secondary = SecondaryOrange,
          onSecondary = Color.White,
          tertiary = TertiaryGreen,
          onTertiary = Color.White,
          background = LightBackground,
          onBackground = LightOnSurface,
          surface = LightSurface,
          onSurface = LightOnSurface,
          onSurfaceVariant = LightOnSurfaceVariant,
          outlineVariant = LightOutlineVariant,
          error = Color(0xFFB3261E),
          onError = Color.White)

  val dark =
      darkColorScheme(
          primary = DarkPrimaryRed,
          onPrimary = Color.White,
          secondary = SecondaryOrange,
          onSecondary = Color.White,
          tertiary = TertiaryGreen,
          onTertiary = Color.White,
          background = DarkBackground,
          onBackground = DarkOnSurface,
          surface = DarkSurface,
          onSurface = DarkOnSurface,
          onSurfaceVariant = DarkOnSurfaceVariant,
          outlineVariant = DarkOutlineVariant,
          error = Color(0xFFB3261E),
          onError = Color.White)
}
