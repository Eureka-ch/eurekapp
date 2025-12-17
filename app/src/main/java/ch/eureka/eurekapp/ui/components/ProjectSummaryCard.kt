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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.screens.ProjectStatusDisplay
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors

private const val PRESSED_SCALE = 0.98f
private const val NORMAL_SCALE = 1f
private const val ANIMATION_DURATION_MS = 150

/**
 * Reusable project card component used in HomeOverviewScreen and ProjectSelectionScreen.
 *
 * @param project The project to display
 * @param onClick Callback when the card is clicked
 * @param modifier Modifier for the card
 * @param memberCount Number of members in the project (optional, defaults to
 *   project.memberIds.size)
 * @param actionButton Optional action button to display in the footer (e.g., "Open →" or "View
 *   Members")
 * @param actionButtonTestTag Optional test tag for the action button
 */
@Composable
fun ProjectSummaryCard(
    project: Project,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    memberCount: Int = project.memberIds.size,
    actionButton: @Composable () -> Unit = {
      TextButton(
          onClick = onClick,
          colors =
              androidx.compose.material3.ButtonDefaults.textButtonColors(
                  contentColor = MaterialTheme.colorScheme.primary)) {
            Text(
                "Open →", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
          }
    },
    actionButtonTestTag: String? = null
) {
  var isPressed by remember { mutableStateOf(false) }
  val scale by
      animateFloatAsState(
          targetValue = if (isPressed) PRESSED_SCALE else NORMAL_SCALE,
          animationSpec = tween(ANIMATION_DURATION_MS))

  Card(
      modifier =
          modifier
              .fillMaxWidth()
              .scale(scale)
              .shadow(elevation = 12.dp, shape = RoundedCornerShape(24.dp)),
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = EColors.CardBorderColor,
                        shape = RoundedCornerShape(24.dp))
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors =
                                    listOf(
                                        Color.White,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.03f)),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)))
                    .clickable(role = Role.Button, onClick = onClick)) {
              Column(modifier = Modifier.padding(24.dp)) {
                // Header: Title + Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top) {
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.name.ifEmpty { "Untitled project" },
                            style = MaterialTheme.typography.titleLarge,
                            color = EColors.TitleTextColor,
                            fontWeight = FontWeight.Bold)
                      }
                      // Status badge
                      ProjectStatusDisplay(project.status)
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Description with icon
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()) {
                      Box(
                          modifier =
                              Modifier.size(36.dp)
                                  .background(
                                      color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                      shape = RoundedCornerShape(10.dp)),
                          contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp))
                          }
                      Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = project.description.ifEmpty { "No description provided" },
                            style = MaterialTheme.typography.bodyMedium,
                            color = EColors.SecondaryTextColor,
                            fontWeight = FontWeight.Medium)
                      }
                    }

                Spacer(modifier = Modifier.height(16.dp))

                // Footer: Members + Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      // Members with icon
                      Row(
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier =
                                    Modifier.size(32.dp)
                                        .background(
                                            color = EColors.IconBackgroundColor,
                                            shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center) {
                                  Icon(
                                      imageVector = Icons.Default.Person,
                                      contentDescription = null,
                                      tint = EColors.GrayTextColor2,
                                      modifier = Modifier.size(16.dp))
                                }
                            Text(
                                text = "$memberCount members",
                                style = MaterialTheme.typography.bodyMedium,
                                color = EColors.SecondaryTextColor,
                                fontWeight = FontWeight.SemiBold)
                          }
                      // Action button (can be customized)
                      Box(
                          modifier =
                              actionButtonTestTag?.let { Modifier.testTag(it) } ?: Modifier) {
                            actionButton()
                          }
                    }
              }
            }
      }
}
