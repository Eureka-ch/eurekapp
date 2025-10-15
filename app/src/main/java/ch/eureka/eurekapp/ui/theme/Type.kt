package ch.eureka.eurekapp.ui.theme

import androidx.compose.material3.Typography
import ch.eureka.eurekapp.ui.designsystem.tokens.ETypography

/** Legacy typography accessor backed by the new Design System. */
val Typography: Typography
  get() = ETypography.value
