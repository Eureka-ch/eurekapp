package ch.eureka.eurekapp.ui.designsystem.tokens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
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
      ButtonDefaults.outlinedButtonColors(
          contentColor = Color(0xFF424242), // Dark gray text
          containerColor = Color.White // White background
          )

  @Composable
  fun TextFieldColors() =
      OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          cursorColor = MaterialTheme.colorScheme.primary)

  // Card style with slight shadow
  val CardShape = RoundedCornerShape(16.dp)
  val CardElevation = 2.dp

  // Additional styles matching Figma
  @Composable
  fun HighPriorityTagColors() =
      CardDefaults.cardColors(
          containerColor = Color(0xFFFFEBEE), // Light red background
          contentColor = Color(0xFFE57373) // Red text
          )

  @Composable
  fun NormalTagColors() =
      CardDefaults.cardColors(
          containerColor = Color(0xFFEEEEEE), // Light gray background
          contentColor = Color(0xFF424242) // Dark gray text
          )

  // Button border style matching Figma
  val OutlinedButtonBorder =
      androidx.compose.foundation.BorderStroke(
          width = 1.dp, color = Color(0xFFE5E5E5) // Light gray border
          )
}
