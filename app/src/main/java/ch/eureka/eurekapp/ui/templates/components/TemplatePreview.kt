// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.*

/**
 * Preview of how template fields will appear. Shows actual field components or error placeholders.
 *
 * @param fields List of field definitions
 * @param fieldErrors Map of field errors
 * @param onErrorFieldClick Callback when error field is clicked
 */
@Composable
fun TemplatePreview(
    fields: List<FieldDefinition>,
    fieldErrors: Map<String, String>,
    onErrorFieldClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
  LazyColumn(
      modifier = modifier,
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (fields.isEmpty()) {
          item {
            Box(Modifier.fillMaxWidth().padding(32.dp), Alignment.Center) {
              Text("No fields to preview")
            }
          }
        } else {
          items(fields, key = { it.id }) { field ->
            fieldErrors[field.id]?.let { error ->
              ErrorFieldPlaceholder(field.label, error) { onErrorFieldClick(field.id) }
            } ?: FieldPreview(field)
          }
        }
      }
}

@Composable
private fun ErrorFieldPlaceholder(label: String, error: String, onClick: () -> Unit) {
  Card(
      onClick = onClick,
      colors =
          CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer.copy(0.2f))) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Icon(Icons.Default.Warning, null, tint = MaterialTheme.colorScheme.error)
          Column {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(
                error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
          }
        }
      }
}

@Composable
private fun FieldPreview(field: FieldDefinition) {
  when (field.type) {
    is FieldType.Text ->
        TextFieldComponent(
            Modifier, field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
    is FieldType.Number ->
        NumberFieldComponent(
            Modifier, field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
    is FieldType.Date ->
        DateFieldComponent(
            Modifier, field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
    is FieldType.SingleSelect ->
        SingleSelectFieldComponent(
            Modifier, field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
    is FieldType.MultiSelect ->
        MultiSelectFieldComponent(
            Modifier, field, null, {}, FieldInteractionMode.EditOnly, showHeader = true)
  }
}
