package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Reusable component styles for buttons, text fields, and cards. */
object EurekaStyles {

  @Composable
  fun PrimaryButtonColors() =
      ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)

  @Composable
  fun OutlinedButtonColors() =
      ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)

  @Composable
  fun TextFieldColors() =
      OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          cursorColor = MaterialTheme.colorScheme.primary)

  // Card style with slight shadow
  val CardShape = RoundedCornerShape(16.dp)
  val CardElevation = 2.dp
}
