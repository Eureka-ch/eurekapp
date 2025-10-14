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
  private val IconColor = Color(0xFF424242) // Dark gray for icons
  private val NotificationIconColor = Color(0xFFEF9A9A) // Light red for notification icons
  private val HighPriorityBackground = Color(0xFFFFEBEE) // Light red background for high priority
  private val HighPriorityText = Color(0xFFE57373) // Red text for high priority
  private val TagBackground = Color(0xFFEEEEEE) // Light gray background for tags
  private val TagText = Color(0xFF424242) // Dark gray text for tags
  private val ButtonBorderColor = Color(0xFFE5E5E5) // Light gray border for outlined buttons
  private val SuccessGreen = Color(0xFF4CAF50) // Green for success states and progress
  private val WarningOrange = Color(0xFFFFB74D) // Orange for warnings
  private val InfoBlue = Color(0xFF2196F3) // Blue for info states

  // Light theme colors from Figma
  val LightBackground = Color.White // Pure white background
  val LightSurface = Color.White // White cards/surfaces
  val LightOnSurface = Color(0xFF212121) // Dark text for titles
  val LightOnSurfaceVariant = Color(0xFF424242) // Darker gray for better contrast
  val LightOutlineVariant = Color(0xFF9E9E9E) // Light gray for secondary text

  // Dark theme colors
  val DarkBackground = Color(0xFF121212)
  val DarkSurface = Color(0xFF1C1B1F)
  val DarkOnSurface = Color(0xFFE6E1E5)
  val DarkOnSurfaceVariant = Color(0xFFCAC4D0)
  val DarkOutlineVariant = Color(0xFF49454F)

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
          surfaceVariant = Color.White, // Force surfaceVariant to white
          onSurfaceVariant = LightOnSurfaceVariant,
          surfaceContainer = Color.White, // Force surfaceContainer to white
          surfaceContainerHigh = Color.White, // Force surfaceContainerHigh to white
          surfaceContainerHighest = Color.White, // Force surfaceContainerHighest to white
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
