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

  // Text colors
  val GrayTextColor2 = Color(0xFF64748B)
  val WhiteTextColor = Color.White
  val BlackTextColor = Color.Black
  val LightingBlue = Color(0xFF2563EB)

  // Border colors
  val BorderGrayColor = Color(0xFFE5E7EB)
  val CardBorderColor = Color(0xFFE2E8F0) // Border color for cards

  // Text colors
  val TitleTextColor = Color(0xFF0F172A) // Dark text for titles
  val SecondaryTextColor = Color(0xFF475569) // Secondary text color

  // Background colors
  val IconBackgroundColor = Color(0xFFF1F5F9) // Background for icon containers
  val GradientLightColor = Color(0xFFFAFAFA) // Light gradient color

  // Status colors
  val StatusBlue = Color(0xFF3B82F6) // Blue for status indicators
  val StatusGreen = Color(0xFF16A34A) // Green for status indicators

  // Top bar gradient colors
  val TopBarGradientStart = Color(0xFFE53935) // Start color for top bar gradient
  val TopBarGradientEnd = Color(0xFFC62828) // End color for top bar gradient

  // Meeting status colors
  val MeetingStatusOpenToVotes = Color(0xFF2196F3) // Blue for open to votes
  val MeetingStatusScheduled = Color(0xFFF44336) // Red for scheduled
  val MeetingStatusInProgress = Color(0xFF4CAF50) // Green for in progress
  val MeetingStatusCompleted = Color(0xFF616161) // Dark gray for completed

  // Error colors
  val ErrorTextColor = Color(0xFFB3261E) // Error text color

  // Google blue
  val GoogleBlue = Color(0xFF4285F4) // Google blue color

  // Activity type colors
  val ActivityCreated = Color(0xFF4CAF50) // Green for created
  val ActivityUpdated = Color(0xFF2196F3) // Blue for updated
  val ActivityDeleted = Color(0xFFF44336) // Red for deleted
  val ActivityUploaded = Color(0xFF9C27B0) // Purple for uploaded
  val ActivityShared = Color(0xFFFF9800) // Orange for shared
  val ActivityCommented = Color(0xFF00BCD4) // Cyan for commented
  val ActivityStatusChanged = Color(0xFFFF5722) // Deep orange for status changed
  val ActivityJoined = Color(0xFF8BC34A) // Light green for joined
  val ActivityLeft = Color(0xFF795548) // Brown for left
  val ActivityAssigned = Color(0xFF3F51B5) // Indigo for assigned
  val ActivityUnassigned = Color(0xFF607D8B) // Blue grey for unassigned
  val ActivityRoleChanged = Color(0xFFE91E63) // Pink for role changed
  val ActivityDownloaded = Color(0xFF673AB7) // Deep purple for downloaded
  val ActivityDefault = Color(0xFF9E9E9E) // Gray for default/unknown

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
