package ch.eureka.eurekapp.ui.tasks.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Action buttons for the Task screen "+ New Task" and "Auto-assign" buttons */
@Composable
fun TaskActionButtons(
    onCreateTaskClick: () -> Unit = {},
    onAutoAssignClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        // "+ New Task" button (outlined)
        OutlinedButton(
            onClick = onCreateTaskClick,
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface)) {
              Text(
                  text = "+ New Task",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium)
            }

        // "Auto-assign" button (filled)
        Button(
            onClick = onAutoAssignClick,
            modifier = Modifier.weight(1f),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary)) {
              Text(
                  text = "Auto-assign",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Medium)
            }
      }
}
