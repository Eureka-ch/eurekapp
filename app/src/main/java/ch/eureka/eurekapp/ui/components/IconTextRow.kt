package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * A reusable composable that displays a row containing an icon and text. Useful for displaying
 * information with an associated icon (e.g., project description, user info).
 *
 * @param text text to display next to the icon.
 * @param iconVector vector icon to display.
 * @param iconColor color of the icon.
 * @param modifier modifier to be applied to the composable.
 */
@Composable
fun IconTextRow(
    text: String,
    iconVector: ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier.fillMaxWidth().padding(horizontal = 5.dp),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.Top) {
              Icon(iconVector, null, tint = iconColor)
            }
        Row(
            modifier = Modifier.fillMaxHeight().padding(vertical = 5.dp, horizontal = 3.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.Start) {
              Text(text, style = MaterialTheme.typography.labelMedium)
            }
      }
}
