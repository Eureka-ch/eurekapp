package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Task card component used on tasks and project screens */
@Composable
fun EurekaTaskCard(
    title: String,
    dueDate: String = "",
    assignee: String = "",
    priority: String = "",
    category: String = "",
    progressText: String = "",
    progressValue: Float = 0f,
    isCompleted: Boolean = false,
    onToggleComplete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically) {
              // Checkbox or completion indicator
              if (isCompleted) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.tertiary)
              } else {
                Checkbox(
                    checked = false,
                    onCheckedChange = { onToggleComplete() },
                    colors =
                        CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary))
              }

              Spacer(modifier = Modifier.width(Spacing.sm))

              Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface)

                // Metadata row
                if (dueDate.isNotEmpty() || assignee.isNotEmpty()) {
                  Row {
                    if (dueDate.isNotEmpty()) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏰ $dueDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }

                    if (assignee.isNotEmpty()) {
                      if (dueDate.isNotEmpty()) Spacer(modifier = Modifier.width(Spacing.sm))
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👤 $assignee",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                    }
                  }
                }

                // Tags row
                if (priority.isNotEmpty() || category.isNotEmpty()) {
                  Row {
                    if (priority.isNotEmpty()) {
                      EurekaStatusTag(text = priority, type = StatusType.INFO)
                    }
                    if (category.isNotEmpty()) {
                      if (priority.isNotEmpty()) Spacer(modifier = Modifier.width(Spacing.xs))
                      EurekaStatusTag(text = category, type = StatusType.INFO)
                    }
                  }
                }
              }

              // Progress indicator
              if (progressText.isNotEmpty() || progressValue > 0f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  LinearProgressIndicator(
                      progress = { progressValue },
                      modifier = Modifier.width(60.dp),
                      color = MaterialTheme.colorScheme.tertiary)
                  if (progressText.isNotEmpty()) {
                    Text(
                        text = progressText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
      }
}
