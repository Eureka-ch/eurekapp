package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Spacing scale used for paddings and gaps. */
object Spacing {
  val xxs: Dp = 4.dp
  val xs: Dp = 8.dp
  val sm: Dp = 12.dp
  val md: Dp = 16.dp
  val lg: Dp = 24.dp
  val xl: Dp = 32.dp
}

val LocalSpacing = staticCompositionLocalOf { Spacing }
