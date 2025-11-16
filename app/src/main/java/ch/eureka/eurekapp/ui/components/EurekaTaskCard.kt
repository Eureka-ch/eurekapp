package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

// Portions of this code were generated with the help of AI.
// Portions added by Jiří Gebauer partially generated with the help of Grok.

/** Task card component used on tasks and project screens */
@Composable
fun EurekaTaskCard(
    title: String,
    modifier: Modifier = Modifier,
    dueDate: String = "",
    dueDateTag: String? = null,
    assignee: String = "",
    priority: String = "",
    progressText: String = "",
    progressValue: Float = 0f,
    isCompleted: Boolean = false,
    onToggleComplete: () -> Unit = {},
    onClick: () -> Unit = {},
    canToggleCompletion: Boolean = true
) {
  // No local state - use controlled state from parent
  Card(
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = EurekaStyles.taskCardColors(),
      border = EurekaStyles.taskCardBorder(),
      modifier = modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onClick)) {
        Column(modifier = Modifier.padding(Spacing.lg)) { // Plus de padding

          // Top row: Title (left) + Checkbox (right)
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()) {

                // Task title aligned to the left
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = EurekaStyles.taskTitleColor(isCompleted),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))

                // Checkbox aligned to the right
                Box(
                    modifier =
                        Modifier.clickable(enabled = canToggleCompletion) { onToggleComplete() }
                            .alpha(if (canToggleCompletion) 1f else 0.6f)) {
                      if (isCompleted) {
                        Text(
                            text = "✓",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.tertiary)
                      } else {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggleComplete() },
                            enabled = canToggleCompletion,
                            colors =
                                CheckboxDefaults.colors(
                                    checkedColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier.testTag("checkbox"))
                      }
                    }

                // Pas de pourcentage en haut
              }

          Spacer(modifier = Modifier.height(Spacing.sm))

          // Middle row: Due date + Assignee
          if (dueDate.isNotEmpty() || assignee.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                  if (dueDate.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "⏰ $dueDate",
                          style = MaterialTheme.typography.bodySmall,
                          color = EurekaStyles.taskSecondaryTextColor())
                    }
                  }

                  if (assignee.isNotEmpty()) {
                    if (dueDate.isNotEmpty()) {
                      Spacer(modifier = Modifier.width(Spacing.md))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = "👤 $assignee",
                          style = MaterialTheme.typography.bodySmall,
                          color = EurekaStyles.taskSecondaryTextColor())
                    }
                  }
                }
          }

          Spacer(modifier = Modifier.height(Spacing.sm))

          if (dueDateTag != null && !isCompleted) {
            Row(
                modifier = Modifier.padding(bottom = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically) {
                  val tagType =
                      when {
                        dueDateTag.contains("Overdue") -> StatusType.ERROR
                        dueDateTag.contains("hour") -> StatusType.WARNING
                        else -> StatusType.INFO
                      }
                  EurekaStatusTag(text = dueDateTag, type = tagType)
                }
          }

          // Priority tag ou Done
          if (isCompleted) {
            Row { EurekaStatusTag(text = "Done", type = StatusType.SUCCESS) }
          } else if (priority.isNotEmpty()) {
            Row { EurekaStatusTag(text = priority, type = StatusType.INFO) }
          }

          Spacer(modifier = Modifier.height(Spacing.md))

          // Separator line
          androidx.compose.foundation.layout.Box(
              modifier =
                  Modifier.fillMaxWidth()
                      .height(1.dp)
                      .background(EurekaStyles.taskSeparatorColor()))

          Spacer(modifier = Modifier.height(Spacing.sm))

          // Bottom row: Progress label + Progress bar
          if (progressText.isNotEmpty() || progressValue > 0f) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()) {
                  Text(
                      text = "Progression",
                      style = MaterialTheme.typography.bodySmall,
                      color = EurekaStyles.taskSecondaryTextColor(),
                      modifier = Modifier.weight(1f))

                  LinearProgressIndicator(
                      progress = { if (isCompleted) 1.0f else progressValue }, // 100% si cochée
                      modifier = Modifier.width(60.dp).height(6.dp),
                      color = MaterialTheme.colorScheme.primary,
                      trackColor = EurekaStyles.taskSeparatorColor())

                  Spacer(modifier = Modifier.width(Spacing.xs))

                  // Pourcentage à côté de la barre
                  Text(
                      text = if (isCompleted) "100%" else progressText,
                      style = MaterialTheme.typography.labelMedium,
                      color = EurekaStyles.taskSecondaryTextColor(),
                      fontWeight = FontWeight.Bold)
                }
          }
        }
      }
}
