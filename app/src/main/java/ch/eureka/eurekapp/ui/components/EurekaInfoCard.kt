package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Information card component used on dashboard and summary screens */
@Composable
fun EurekaInfoCard(
    modifier: Modifier = Modifier,
    title: String,
    primaryValue: String,
    secondaryValue: String = "",
    icon: ImageVector? = null,
    gradientStart: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
    gradientEnd: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
) {
  Card(
      shape = RoundedCornerShape(20.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
      modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier.fillMaxWidth()
                    .background(
                        brush =
                            Brush.linearGradient(
                                colors = listOf(gradientStart, gradientEnd),
                                start = androidx.compose.ui.geometry.Offset(0f, 0f),
                                end = androidx.compose.ui.geometry.Offset(1000f, 1000f)))) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically) {
                    // Icon container with gradient background
                    if (icon != null) {
                Box(
                    modifier =
                              Modifier.size(48.dp)
                            .background(
                                      brush =
                                          Brush.linearGradient(
                                              colors =
                                                  listOf(
                                                      MaterialTheme.colorScheme.primary.copy(
                                                          alpha = 0.2f),
                                                      MaterialTheme.colorScheme.primary.copy(
                                                          alpha = 0.1f))),
                                      shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp))
                    }
                Spacer(modifier = Modifier.width(Spacing.md))
              }

              Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                      Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = primaryValue,
                          style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface)
                if (secondaryValue.isNotEmpty()) {
                        Spacer(modifier = Modifier.size(2.dp))
                  Text(
                      text = secondaryValue,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                }
              }
            }
      }
}
