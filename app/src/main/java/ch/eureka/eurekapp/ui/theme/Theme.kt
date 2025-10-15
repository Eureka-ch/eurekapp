package ch.eureka.eurekapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ch.eureka.eurekapp.ui.designsystem.tokens.LocalSpacing
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/**
 * Legacy theme wrapper that now delegates to the new token-based values. Screens calling
 * EurekappTheme() will automatically pick the Figma palette.
 */
@Composable
fun EurekappTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
  CompositionLocalProvider(LocalSpacing provides Spacing) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content)
  }
}
