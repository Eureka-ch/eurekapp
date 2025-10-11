package ch.eureka.eurekapp.ui.theme

import androidx.compose.material3.ColorScheme
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors

/** Legacy color accessors backed by the new Design System. */
val LightColorScheme: ColorScheme
  get() = EColors.light
val DarkColorScheme: ColorScheme
  get() = EColors.dark
