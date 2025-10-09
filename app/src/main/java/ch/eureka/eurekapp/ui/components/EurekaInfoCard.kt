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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Information card component used on dashboard and summary screens */
@Composable
fun EurekaInfoCard(
    title: String,
    primaryValue: String,
    secondaryValue: String? = null,
    iconText: String? = null,
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically) {
              // Icon container
              iconText?.let {
                Box(
                    modifier =
                        Modifier.size(40.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center) {
                      Text(
                          text = it,
                          style = MaterialTheme.typography.titleMedium,
                          color = MaterialTheme.colorScheme.primary)
                    }
              }

              Spacer(modifier = Modifier.width(Spacing.md))

              Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = primaryValue,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface)
                secondaryValue?.let {
                  Text(
                      text = it,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
              }
            }
      }
}
