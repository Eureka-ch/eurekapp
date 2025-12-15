package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Status tag component for showing status, priority, or category information */
@Composable
fun EurekaStatusTag(
    modifier: Modifier = Modifier,
    text: String,
    type: StatusType = StatusType.INFO
) {
  val (containerColor, textColor) =
      when (type) {
        StatusType.SUCCESS ->
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f) to
                MaterialTheme.colorScheme.tertiary
        StatusType.WARNING ->
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f) to
                MaterialTheme.colorScheme.secondary
        StatusType.ERROR ->
            MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        StatusType.INFO ->
            MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
      }

  Card(
      colors = CardDefaults.cardColors(containerColor = containerColor),
      shape = RoundedCornerShape(8.dp),
      modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
      }
}

enum class StatusType {
  SUCCESS,
  WARNING,
  ERROR,
  INFO
}
