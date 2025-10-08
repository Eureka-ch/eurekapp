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
    dueDate: String? = null,
    assignee: String? = null,
    priority: String? = null,
    category: String? = null,
    progress: Float? = null,
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
                Row {
                  dueDate?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "⏰",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                          text = it,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }

                  assignee?.let {
                    if (dueDate != null) Spacer(modifier = Modifier.width(Spacing.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "👤",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.width(4.dp))
                      Text(
                          text = it,
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                  }
                }

                // Tags row
                Row {
                  priority?.let {
                    EurekaStatusTag(
                        text = it,
                        type =
                            when (it) {
                              "High" -> StatusType.ERROR
                              "Medium" -> StatusType.WARNING
                              else -> StatusType.INFO
                            })
                  }

                  category?.let {
                    if (priority != null) Spacer(modifier = Modifier.width(Spacing.xs))
                    EurekaStatusTag(text = it, type = StatusType.INFO)
                  }
                }
              }

              // Progress indicator
              progress?.let {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  LinearProgressIndicator(
                      progress = { it },
                      modifier = Modifier.width(60.dp),
                      color = MaterialTheme.colorScheme.tertiary)
                  Text(
                      text = "${(it * 100).toInt()}%",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
      }
}
