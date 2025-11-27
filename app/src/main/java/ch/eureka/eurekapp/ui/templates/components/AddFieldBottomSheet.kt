// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.*
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes.*

/**
 * Bottom sheet for adding a new field. Shows type selector, then field editor.
 *
 * @param onDismiss Callback to dismiss sheet
 * @param onFieldCreated Callback when field is created
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFieldBottomSheet(
    onDismiss: () -> Unit,
    onFieldCreated: (FieldDefinition) -> Unit,
    modifier: Modifier = Modifier
) {
  var selectedType by remember { mutableStateOf<FieldType?>(null) }
  var currentField by remember { mutableStateOf<FieldDefinition?>(null) }

  ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
    Column(modifier = Modifier.padding(16.dp)) {
      selectedType?.let { _ -> currentField?.let { FieldEditor(it, onDismiss, onFieldCreated) } }
          ?: FieldTypeSelector { type ->
            selectedType = type
            currentField =
                FieldDefinition(
                    id = IdGenerator.generateFieldId("New Field"),
                    label = "New Field",
                    type = type,
                    required = false)
          }
    }
  }
}

@Composable
private fun FieldTypeSelector(onTypeSelected: (FieldType) -> Unit) {
  Text(
      "Select Field Type",
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(bottom = 16.dp))

  listOf(
          "Text" to FieldType.Text(),
          "Number" to FieldType.Number(),
          "Date" to FieldType.Date(),
          "Single Select" to FieldType.SingleSelect(listOf(SelectOption("option1", "Option 1"))),
          "Multi Select" to FieldType.MultiSelect(listOf(SelectOption("option1", "Option 1"))))
      .forEach { (name, type) ->
        Card(
            onClick = { onTypeSelected(type) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
              Text(name, modifier = Modifier.padding(16.dp))
            }
      }
}

@Composable
private fun ColumnScope.FieldEditor(
    field: FieldDefinition,
    onDismiss: () -> Unit,
    onFieldCreated: (FieldDefinition) -> Unit
) {
  var editingField by remember { mutableStateOf(field) }

  Text(
      "Configure Field",
      style = MaterialTheme.typography.headlineSmall,
      modifier = Modifier.padding(bottom = 16.dp))

  LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
    item {
      CommonFieldConfiguration(editingField, { editingField = it }, true)
      Spacer(Modifier.height(8.dp))

      when (val type = editingField.type) {
        is FieldType.Text ->
            TextFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
        is FieldType.Number ->
            NumberFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
        is FieldType.Date ->
            DateFieldConfiguration(type, { editingField = editingField.copy(type = it) }, true)
        is FieldType.SingleSelect ->
            SingleSelectFieldConfiguration(
                type, { editingField = editingField.copy(type = it) }, true)
        is FieldType.MultiSelect ->
            MultiSelectFieldConfiguration(
                type, { editingField = editingField.copy(type = it) }, true)
      }
    }
  }

  Row(
      Modifier.fillMaxWidth().padding(top = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
        Button(
            onClick = {
              onFieldCreated(editingField)
              onDismiss()
            },
            modifier = Modifier.weight(1f)) {
              Text("Add")
            }
      }
}
