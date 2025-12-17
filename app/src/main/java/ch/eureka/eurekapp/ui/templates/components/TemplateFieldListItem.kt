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
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R
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
      FieldListItemHeader(
          field = field,
          isExpanded = isExpanded,
          hasError = error != null,
          onExpand = callbacks.onExpand,
          dragHandle = dragHandle)

      if (isExpanded) {
        HorizontalDivider()
        FieldListItemExpandedContent(
            localField = localField,
            onLocalFieldChange = { localField = it },
            callbacks = callbacks)
      }
    }
  }
}

@Composable
private fun FieldListItemHeader(
    field: FieldDefinition,
    isExpanded: Boolean,
    hasError: Boolean,
    onExpand: () -> Unit,
    dragHandle: @Composable () -> Unit
) {
  Row(
      modifier =
          Modifier.fillMaxWidth().clickable(enabled = !isExpanded) { onExpand() }.padding(16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically) {
        dragHandle()
        Icon(field.type.icon, null, tint = MaterialTheme.colorScheme.primary)

        Column(modifier = Modifier.weight(1f)) {
          Row {
            Text(field.label, style = MaterialTheme.typography.bodyLarge)
            if (field.required) Text(stringResource(R.string.field_required_indicator), color = MaterialTheme.colorScheme.error)
          }
          Text(fieldTypeName(field.type), style = MaterialTheme.typography.bodySmall)
        }

        if (hasError) Icon(Icons.Default.Warning, contentDescription = stringResource(R.string.action_error), tint = MaterialTheme.colorScheme.error)
        if (!isExpanded) Icon(Icons.Default.ExpandMore, contentDescription = stringResource(R.string.action_expand))
      }
}

@Composable
private fun FieldListItemExpandedContent(
    localField: FieldDefinition,
    onLocalFieldChange: (FieldDefinition) -> Unit,
    callbacks: TemplateFieldCallbacks
) {
  Column(modifier = Modifier.padding(16.dp)) {
    CommonFieldConfiguration(field = localField, onFieldUpdate = onLocalFieldChange, enabled = true)

    Spacer(modifier = Modifier.height(8.dp))

    TypeSpecificConfiguration(localField, onLocalFieldChange)

    FieldItemActionButtons(
        onSave = {
          callbacks.onFieldChange(localField)
          callbacks.onSave()
        },
        onCancel = callbacks.onCancel,
        onDuplicate = callbacks.onDuplicate,
        onDelete = callbacks.onDelete)
  }
}

@Composable
private fun TypeSpecificConfiguration(
    localField: FieldDefinition,
    onLocalFieldChange: (FieldDefinition) -> Unit
) {
  when (val type = localField.type) {
    is FieldType.Text ->
        TextFieldConfiguration(type, { onLocalFieldChange(localField.copy(type = it)) }, true)
    is FieldType.Number ->
        NumberFieldConfiguration(type, { onLocalFieldChange(localField.copy(type = it)) }, true)
    is FieldType.Date ->
        DateFieldConfiguration(type, { onLocalFieldChange(localField.copy(type = it)) }, true)
    is FieldType.SingleSelect ->
        SingleSelectFieldConfiguration(
            type, { onLocalFieldChange(localField.copy(type = it)) }, true)
    is FieldType.MultiSelect ->
        MultiSelectFieldConfiguration(
            type, { onLocalFieldChange(localField.copy(type = it)) }, true)
  }
}

@Composable
private fun FieldItemActionButtons(
    onSave: () -> Unit,
    onCancel: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
  Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
    IconButton(onClick = onSave) {
      Icon(Icons.Default.Check, contentDescription = stringResource(R.string.action_save), tint = MaterialTheme.colorScheme.primary)
    }
    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_cancel)) }
    Spacer(Modifier.weight(1f))
    IconButton(onClick = onDuplicate) { Icon(Icons.Default.ContentCopy, contentDescription = stringResource(R.string.action_duplicate)) }
    IconButton(onClick = onDelete) {
      Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete), tint = MaterialTheme.colorScheme.error)
    }
  }
}

/**
 * Helper to return localized name for a FieldType (must be @Composable to use stringResource).
 */
@Composable
private fun fieldTypeName(type: FieldType): String =
    when (type) {
      is FieldType.Text -> stringResource(R.string.field_type_text)
      is FieldType.Number -> stringResource(R.string.field_type_number)
      is FieldType.Date -> stringResource(R.string.field_type_date)
      is FieldType.SingleSelect -> stringResource(R.string.field_type_single_select)
      is FieldType.MultiSelect -> stringResource(R.string.field_type_multi_select)
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
