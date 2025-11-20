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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import ch.eureka.eurekapp.screens.TasksScreenTestTags
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

// portions of this code and documentation were generated with the help of AI.

// Portions of this code were generated with the help of Grok.

/** Action buttons for the Task screen "+ New Task" and "Auto-assign" buttons */
@Composable
fun TaskActionButtons(
    modifier: Modifier = Modifier,
    onCreateTaskClick: () -> Unit = {},
    onAutoAssignClick: () -> Unit = {},
    actionsEnabled: Boolean = true
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        // "+ New Task" button (outlined)
        OutlinedButton(
            onClick = { if (actionsEnabled) onCreateTaskClick() },
            enabled = actionsEnabled,
            modifier =
                Modifier.weight(1f)
                    .testTag(TasksScreenTestTags.CREATE_TASK_BUTTON)
                    .alpha(if (actionsEnabled) 1f else 0.6f),
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
            onClick = { if (actionsEnabled) onAutoAssignClick() },
            enabled = actionsEnabled,
            modifier = Modifier.weight(1f).alpha(if (actionsEnabled) 1f else 0.6f),
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
