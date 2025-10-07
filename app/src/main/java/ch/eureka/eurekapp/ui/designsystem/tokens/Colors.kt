package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Defines the app color schemes (light/dark) mapped from the Figma palette.
 */
object EColors {
    
    // Brand colors from Figma palette
    private val PrimaryRed = Color(0xFFE53935)
    private val PrimaryRedDark = Color(0xFFB71C1C)
    private val SecondaryBlue = Color(0xFF2962FF)
    private val TertiaryGreen = Color(0xFF2E7D32)
    private val WarningYellow = Color(0xFFF9A825)
    
    // Surface colors
    private val LightSurface = Color(0xFFFFFBFE)
    private val LightBackground = Color(0xFFFFFBFE)
    private val DarkSurface = Color(0xFF1C1B1F)
    private val DarkBackground = Color(0xFF121212)
    
    // Text colors
    private val LightOnSurface = Color(0xFF1C1B1F)
    private val LightOnBackground = Color(0xFF1C1B1F)
    private val DarkOnSurface = Color(0xFFE6E1E5)
    private val DarkOnBackground = Color(0xFFE6E1E5)
    
    // Error colors
    private val ErrorRed = Color(0xFFB3261E)
    private val OnErrorWhite = Color(0xFFFFFFFF)
    
    val light = lightColorScheme(
        primary = PrimaryRed,
        onPrimary = Color.White,
        secondary = SecondaryBlue,
        onSecondary = Color.White,
        tertiary = TertiaryGreen,
        onTertiary = Color.White,
        background = LightBackground,
        onBackground = LightOnBackground,
        surface = LightSurface,
        onSurface = LightOnSurface,
        error = ErrorRed,
        onError = OnErrorWhite
    )
    
    val dark = darkColorScheme(
        primary = PrimaryRedDark,
        onPrimary = Color.White,
        secondary = SecondaryBlue,
        onSecondary = Color.White,
        tertiary = TertiaryGreen,
        onTertiary = Color.White,
        background = DarkBackground,
        onBackground = DarkOnBackground,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        error = ErrorRed,
        onError = OnErrorWhite
    )
}
