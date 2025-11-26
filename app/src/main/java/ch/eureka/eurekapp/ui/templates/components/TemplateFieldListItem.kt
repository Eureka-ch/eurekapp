// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.*
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.*

/**
 * Callbacks for template field list item actions.
 *
 * @param onExpand Callback to expand the field
 * @param onFieldChange Callback when field definition changes
 * @param onSave Callback to save changes (collapses)
 * @param onCancel Callback to cancel changes (collapses)
 * @param onDelete Callback to delete field
 * @param onDuplicate Callback to duplicate field
 */
data class TemplateFieldCallbacks(
    val onExpand: () -> Unit,
    val onFieldChange: (FieldDefinition) -> Unit,
    val onSave: () -> Unit,
    val onCancel: () -> Unit,
    val onDelete: () -> Unit,
    val onDuplicate: () -> Unit
)

/**
 * List item for a template field with expand/collapse editing.
 *
 * @param modifier Modifier to apply to the root card
 * @param field The field definition
 * @param isExpanded Whether the field is expanded for editing
 * @param error Validation error if any
 * @param callbacks Grouped callbacks for field actions
 * @param dragHandle Composable for drag handle
 */
@Composable
fun TemplateFieldListItem(
    modifier: Modifier = Modifier,
    field: FieldDefinition,
    isExpanded: Boolean,
    error: String?,
    callbacks: TemplateFieldCallbacks,
    dragHandle: @Composable () -> Unit
) {
  var localField by remember(field, isExpanded) { mutableStateOf(field) }

  Card(modifier = modifier.fillMaxWidth()) {
    Column {
      // Collapsed header
      Row(
          modifier =
              Modifier.fillMaxWidth()
                  .clickable(enabled = !isExpanded) { callbacks.onExpand() }
                  .padding(16.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically) {
            dragHandle()
            Icon(field.type.icon, null, tint = MaterialTheme.colorScheme.primary)

            Column(modifier = Modifier.weight(1f)) {
              Row {
                Text(field.label, style = MaterialTheme.typography.bodyLarge)
                if (field.required) Text(" *", color = MaterialTheme.colorScheme.error)
              }
              Text(field.type.name, style = MaterialTheme.typography.bodySmall)
            }

            if (error != null)
                Icon(Icons.Default.Warning, "Error", tint = MaterialTheme.colorScheme.error)
            if (!isExpanded) Icon(Icons.Default.ExpandMore, "Expand")
          }

      // Expanded content
      if (isExpanded) {
        HorizontalDivider()
        Column(modifier = Modifier.padding(16.dp)) {
          CommonFieldConfiguration(
              field = localField, onFieldUpdate = { localField = it }, enabled = true)

          Spacer(modifier = Modifier.height(8.dp))

          // Type-specific config (reuse PR 312 components)
          when (val type = localField.type) {
            is FieldType.Text ->
                TextFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
            is FieldType.Number ->
                NumberFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
            is FieldType.Date ->
                DateFieldConfiguration(type, { localField = localField.copy(type = it) }, true)
            is FieldType.SingleSelect ->
                SingleSelectFieldConfiguration(
                    type, { localField = localField.copy(type = it) }, true)
            is FieldType.MultiSelect ->
                MultiSelectFieldConfiguration(
                    type, { localField = localField.copy(type = it) }, true)
          }

          // Actions
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(
                onClick = {
                  callbacks.onFieldChange(localField)
                  callbacks.onSave()
                }) {
                  Icon(Icons.Default.Check, "Save", tint = MaterialTheme.colorScheme.primary)
                }
            IconButton(onClick = callbacks.onCancel) { Icon(Icons.Default.Close, "Cancel") }
            Spacer(Modifier.weight(1f))
            IconButton(onClick = callbacks.onDuplicate) {
              Icon(Icons.Default.ContentCopy, "Duplicate")
            }
            IconButton(onClick = callbacks.onDelete) {
              Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}

private val FieldType.icon
  get() =
      when (this) {
        is FieldType.Text -> Icons.Default.TextFields
        is FieldType.Number -> Icons.Default.Numbers
        is FieldType.Date -> Icons.Default.CalendarToday
        is FieldType.SingleSelect -> Icons.Default.RadioButtonChecked
        is FieldType.MultiSelect -> Icons.Default.CheckBox
      }

private val FieldType.name
  get() =
      when (this) {
        is FieldType.Text -> "Text"
        is FieldType.Number -> "Number"
        is FieldType.Date -> "Date"
        is FieldType.SingleSelect -> "Single Select"
        is FieldType.MultiSelect -> "Multi Select"
      }
