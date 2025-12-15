package ch.eureka.eurekapp.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
  var isPressed by remember { mutableStateOf(false) }
  val scale by
      animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, animationSpec = tween(150))

  Card(
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      colors =
          CardDefaults.cardColors(containerColor = Color.White, contentColor = Color(0xFF212121)),
      modifier =
          modifier
              .fillMaxWidth()
              .scale(scale)
              .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp))
              .border(width = 1.dp, color = Color(0xFFE5E7EB), shape = RoundedCornerShape(24.dp))
              .clickable(role = Role.Button, onClick = onClick)
              .then(
                  Modifier.clickable(
                      role = Role.Button,
                      onClick = {
                        isPressed = true
                        onClick()
                      },
                      onClickLabel = null))) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        if (isCompleted) Color(0xFFF0FDF4) else Color.White,
                                        if (isCompleted) Color(0xFFECFDF5) else Color(0xFFFAFAFA)),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)))) {
              Column(modifier = Modifier.padding(20.dp)) {
                // Header: Title + Status Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      // Title with strong contrast
                      Text(
                          text = title,
                          style = MaterialTheme.typography.titleLarge,
                          color = if (isCompleted) Color(0xFF64748B) else Color(0xFF0F172A),
                          fontWeight = FontWeight.Bold,
                          modifier = Modifier.weight(1f))

                      // Status icon with border
                      Box(
                          modifier =
                              Modifier.size(40.dp)
                                  .then(
                                      if (!isCompleted) Modifier.testTag("checkbox") else Modifier)
                                  .then(
                                      if (canToggleCompletion && !isCompleted)
                                          Modifier.clickable(
                                              role = Role.Checkbox, onClick = onToggleComplete)
                                      else Modifier)
                                  .background(
                                      color =
                                          if (isCompleted)
                                              MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                          else Color(0xFFF1F5F9),
                                      shape = RoundedCornerShape(12.dp))
                                  .border(
                                      width = 1.5.dp,
                                      color =
                                          if (isCompleted) MaterialTheme.colorScheme.primary
                                          else Color(0xFFE2E8F0),
                                      shape = RoundedCornerShape(12.dp)),
                          contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                              Icon(
                                  imageVector = Icons.Default.CheckCircle,
                                  contentDescription = "Completed",
                                  tint = MaterialTheme.colorScheme.primary,
                                  modifier = Modifier.size(20.dp))
                            } else {
                              Box(
                                  modifier =
                                      Modifier.size(16.dp)
                                          .background(
                                              color = Color(0xFFCBD5E1),
                                              shape = RoundedCornerShape(4.dp)))
                            }
                          }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Metadata row with icons
                if (dueDate.isNotEmpty() || assignee.isNotEmpty()) {
                  Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(16.dp),
                      verticalAlignment = Alignment.CenterVertically) {
                        if (dueDate.isNotEmpty()) {
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier.size(16.dp))
                                Text(
                                    text = dueDate,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF475569),
                                    fontWeight = FontWeight.Medium)
                              }
                        }

                        if (assignee.isNotEmpty()) {
                          Row(
                              verticalAlignment = Alignment.CenterVertically,
                              horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint =
                                        Color(0xFF2563EB), // bleu plus prononcé pour l'assignation
                                    modifier = Modifier.size(16.dp))
                                Text(
                                    text = assignee,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF1D4ED8),
                                    fontWeight = FontWeight.Medium)
                              }
                        }
                      }
                  Spacer(modifier = Modifier.height(12.dp))
                }

                // Tags row
                if (dueDateTag != null && !isCompleted) {
                  val tagType =
                      when {
                        dueDateTag.contains("Overdue") -> StatusType.ERROR
                        dueDateTag.contains("hour") -> StatusType.WARNING
                        else -> StatusType.INFO
                      }
                  EurekaStatusTag(text = dueDateTag, type = tagType)
                  Spacer(modifier = Modifier.height(8.dp))
                }

                if (isCompleted) {
                  EurekaStatusTag(text = "Done", type = StatusType.SUCCESS)
                  Spacer(modifier = Modifier.height(8.dp))
                } else if (priority.isNotEmpty()) {
                  EurekaStatusTag(text = priority, type = StatusType.INFO)
                  Spacer(modifier = Modifier.height(8.dp))
                }

                // Progress bar
                if (progressText.isNotEmpty() || progressValue > 0f) {
                  Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              text = "Progress",
                              style = MaterialTheme.typography.labelLarge,
                              color = Color(0xFF64748B),
                              fontWeight = FontWeight.SemiBold)
                          Text(
                              text = if (isCompleted) "100%" else progressText,
                              style = MaterialTheme.typography.labelLarge,
                              color = Color(0xFF0F172A),
                              fontWeight = FontWeight.Bold)
                        }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { if (isCompleted) 1.0f else progressValue },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color(0xFFE2E8F0))
                  }
                }
              }
            }
      }
}
