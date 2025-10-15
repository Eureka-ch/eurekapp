package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Status tag component for showing status, priority, or category information */

@Composable
fun EurekaStatusTag(
    text: String,
    type: StatusType = StatusType.INFO,
    modifier: Modifier = Modifier
) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor =
                  when (type) {
                    StatusType.SUCCESS -> MaterialTheme.colorScheme.tertiary
                    StatusType.WARNING -> MaterialTheme.colorScheme.secondary
                    StatusType.ERROR -> MaterialTheme.colorScheme.error
                    StatusType.INFO -> Color.White // Blanc pur pour les tags
                  }),
      shape = RoundedCornerShape(4.dp),
      modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color =
                when (type) {
                  StatusType.SUCCESS,
                  StatusType.WARNING,
                  StatusType.ERROR -> Color.White
                  StatusType.INFO -> Color(0xFF212121) // Texte foncé sur fond blanc
                },
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
      }
}

enum class StatusType {
  SUCCESS,
  WARNING,
  ERROR,
  INFO
}
