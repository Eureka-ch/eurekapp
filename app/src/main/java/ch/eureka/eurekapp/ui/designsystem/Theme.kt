package ch.eureka.eurekapp.ui.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.EShapes
import ch.eureka.eurekapp.ui.designsystem.tokens.ETypography
import ch.eureka.eurekapp.ui.designsystem.tokens.LocalSpacing
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Material3 theme assembled from design tokens. Not applied in activities yet. */
@Composable
fun EurekaTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
  val colorScheme = if (darkTheme) EColors.dark else EColors.light

  CompositionLocalProvider(LocalSpacing provides Spacing) {
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ETypography.value,
        shapes = EShapes.value,
        content = content)
  }
}
