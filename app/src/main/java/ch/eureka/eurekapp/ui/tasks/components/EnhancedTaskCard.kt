package ch.eureka.eurekapp.ui.tasks.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.components.EurekaStatusTag
import ch.eureka.eurekapp.ui.components.StatusType
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.tasks.TaskUiModel

/**
 * Enhanced task card component with multiple tags and progress display Extends the existing
 * EurekaTaskCard with additional features
 */
@Composable
fun EnhancedTaskCard(
    task: TaskUiModel,
    onTaskClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = modifier.fillMaxWidth().clickable { onTaskClick(task.id) }) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically) {
              // Checkbox or completion indicator
              if (task.isCompleted) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.semantics { contentDescription = "Completed task" })
              } else {
                Checkbox(
                    checked = false,
                    onCheckedChange = { /* No logic - just display */},
                    colors =
                        CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.semantics { contentDescription = "Task checkbox" })
              }

              Spacer(modifier = Modifier.width(Spacing.sm))

              Column(modifier = Modifier.weight(1f)) {
                // Task title
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)

                // Due date and assignee row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = Spacing.xs)) {
                      // Due date
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "⏰",
                            modifier = Modifier.semantics { contentDescription = "Due date" })
                        Text(
                            text = "Due: ${task.dueDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = Spacing.xs))
                      }

                      Spacer(modifier = Modifier.width(Spacing.sm))

                      // Assignee
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👤",
                            modifier = Modifier.semantics { contentDescription = "Assigned to" })
                        Text(
                            text = "Assigné: ${task.assigneeName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = Spacing.xs))
                      }
                    }

                // Tags row
                Row(
                    modifier = Modifier.padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                      // Priority tag
                      EurekaStatusTag(
                          text = task.priority,
                          type =
                              when (task.priority) {
                                "Priorité Haute" -> StatusType.ERROR
                                "Priorité Moyenne" -> StatusType.WARNING
                                "Priorité Basse" -> StatusType.SUCCESS
                                else -> StatusType.INFO
                              })

                      // Category tags
                      task.tags.forEach { tag ->
                        EurekaStatusTag(text = tag, type = StatusType.INFO)
                      }
                    }
              }

              // Progress indicator
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.padding(start = Spacing.sm)) {
                    LinearProgressIndicator(
                        progress = { task.progress },
                        modifier =
                            Modifier.width(60.dp).semantics {
                              contentDescription = "Task progress ${(task.progress * 100).toInt()}%"
                            },
                        color = MaterialTheme.colorScheme.tertiary)
                    Text(
                        text = "${(task.progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = Spacing.xs))
                  }
            }
      }
}
