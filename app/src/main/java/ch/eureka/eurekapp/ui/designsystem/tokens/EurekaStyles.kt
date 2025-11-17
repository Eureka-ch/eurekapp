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
  fun primaryButtonColors() =
      ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)

  @Composable
  fun outlinedButtonColors() =
      ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.onSurface,
          containerColor = MaterialTheme.colorScheme.surface)

  @Composable
  fun textFieldColors() =
      OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary,
          unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
          cursorColor = MaterialTheme.colorScheme.primary)

  // Card style with slight shadow
  val CardShape = RoundedCornerShape(16.dp)
  val CardElevation = 2.dp

  // Additional styles matching Figma
  @Composable
  fun highPriorityTagColors() =
      CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.errorContainer,
          contentColor = MaterialTheme.colorScheme.onErrorContainer)

  @Composable
  fun normalTagColors() =
      CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant)

  @Composable
  fun outlinedButtonBorder() =
      BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant)

  // Card styles using Eureka colors directly
  @Composable
  fun taskCardColors() =
      CardDefaults.cardColors(
          containerColor = EColors.LightSurface, // Pure white from Eureka
          contentColor = EColors.LightOnSurface)

  @Composable fun taskCardBorder() = BorderStroke(width = 1.dp, color = EColors.LightOutlineVariant)

  // Text colors using Eureka colors directly
  @Composable
  fun taskTitleColor(isCompleted: Boolean) =
      if (isCompleted) EColors.LightOnSurfaceVariant else EColors.LightOnSurface

  @Composable fun taskSecondaryTextColor() = EColors.LightOnSurfaceVariant

  @Composable fun taskSeparatorColor() = EColors.LightOutlineVariant
}
