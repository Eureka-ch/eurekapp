package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** Defines the app color schemes (light/dark) mapped from the Figma palette. */
object EColors {

  // Brand colors from updated guidelines
  private val PrimaryRed = Color(0xFFFF3B30)
  private val DarkPrimaryRed = Color(0xFFB71C1C) // Darker variant for dark theme
  private val SecondaryOrange = Color(0xFFFF9500)
  private val TertiaryGreen = Color(0xFF22C55E)

  // Light theme colors
  private val LightBackground = Color(0xFFF5F5F5)
  private val LightSurface = Color.White
  private val LightOnSurface = Color.Black
  private val LightOnSurfaceVariant = Color(0xFF666666)
  private val LightOutlineVariant = Color(0xFFE5E5E5)

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
