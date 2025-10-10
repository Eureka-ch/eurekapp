package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Filter bar with segmented control for filtering content */
@Composable
fun EurekaFilterBar(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  Row(
      modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        options.forEach { option ->
          FilterChip(
              onClick = { onOptionSelected(option) },
              label = { Text(option) },
              selected = option == selectedOption,
              colors =
                  FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primary,
                      selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                      containerColor = MaterialTheme.colorScheme.surface,
                      labelColor = MaterialTheme.colorScheme.onSurface))
        }
      }
}
