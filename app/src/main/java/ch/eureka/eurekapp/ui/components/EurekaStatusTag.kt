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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Status tag component for showing status, priority, or category information */
@Composable
fun EurekaStatusTag(
    modifier: Modifier = Modifier,
    text: String,
    type: StatusType = StatusType.INFO
) {
  Card(
      colors =
          CardDefaults.cardColors(
              containerColor =
                  when (type) {
                    StatusType.SUCCESS -> Color(0xFFF0FDF4) // Light green-tinted background
                    StatusType.WARNING -> Color(0xFFFFF7ED) // Light orange background
                    StatusType.ERROR -> Color(0xFFFEF2F2) // Light red background
                    StatusType.INFO -> Color(0xFFF1F5F9) // Light gray background
                  }),
      shape = RoundedCornerShape(8.dp),
      modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color =
                when (type) {
                  StatusType.SUCCESS -> Color(0xFF16A34A) // Dark green text
                  StatusType.WARNING -> Color(0xFFEA580C) // Dark orange text
                  StatusType.ERROR -> Color(0xFFDC2626) // Dark red text
                  StatusType.INFO -> Color(0xFF475569) // Dark gray text
                },
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
      }
}

enum class StatusType {
  SUCCESS,
  WARNING,
  ERROR,
  INFO
}
