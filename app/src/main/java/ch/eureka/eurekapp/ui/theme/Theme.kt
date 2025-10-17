package ch.eureka.eurekapp.ui.theme

import androidx.compose.runtime.Composable
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme

/**
 * Legacy theme wrapper that now delegates to the new token-based values. Screens calling
 * EurekappTheme() will automatically pick the Figma palette.
 */
@Composable
fun EurekappTheme(darkTheme: Boolean = false, content: @Composable () -> Unit) {
  // Delegate to the design system theme
  EurekaTheme(darkTheme = darkTheme, content = content)
}
