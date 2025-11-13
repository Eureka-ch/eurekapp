package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

/**
 * Multi-select field component for template fields.
 *
 * @param fieldDefinition The field definition containing label, constraints, etc.
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param showValidationErrors Whether to display validation errors
 * @param modifier The modifier to apply to the component
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MultiSelectFieldComponent(
    modifier: Modifier = Modifier,
    fieldDefinition: FieldDefinition,
    value: FieldValue.MultiSelectValue?,
    onValueChange: (FieldValue.MultiSelectValue) -> Unit,
    mode: FieldInteractionMode,
    showValidationErrors: Boolean = false,
    callbacks: FieldCallbacks = FieldCallbacks(),
) {
  val fieldType = fieldDefinition.type as FieldType.MultiSelect

  BaseFieldComponent(
      modifier = modifier,
      fieldDefinition = fieldDefinition,
      fieldType = fieldType,
      value = value,
      onValueChange = onValueChange,
      mode = mode,
      showValidationErrors = showValidationErrors,
      callbacks = callbacks) { currentValue, onChange, isEditing ->
        if (isEditing) {
          var localSelectedValues by remember {
            mutableStateOf(currentValue?.values?.toSet() ?: emptySet())
          }
          var customText by remember { mutableStateOf("") }

          Column(modifier = Modifier.testTag("multi_select_field_chips_${fieldDefinition.id}")) {
            FlowRow(modifier = Modifier.fillMaxWidth()) {
              fieldType.options.forEach { option ->
                FilterChip(
                    selected = option.value in localSelectedValues,
                    onClick = {
                      val newSelectedValues =
                          if (option.value in localSelectedValues) {
                            localSelectedValues - option.value
                          } else {
                            localSelectedValues + option.value
                          }
                      localSelectedValues = newSelectedValues
                      onChange(FieldValue.MultiSelectValue(newSelectedValues.toList()))
                    },
                    label = { Text(option.label) },
                    modifier =
                        Modifier.padding(end = 8.dp).testTag("multi_select_chip_${option.value}"))
              }

              if (fieldType.allowCustom) {
                val customValues =
                    localSelectedValues.filter { value ->
                      fieldType.options.none { it.value == value }
                    }
                customValues.forEach { customValue ->
                  FilterChip(
                      selected = true,
                      onClick = {
                        val newSelectedValues = localSelectedValues - customValue
                        localSelectedValues = newSelectedValues
                        onChange(FieldValue.MultiSelectValue(newSelectedValues.toList()))
                      },
                      label = { Text(customValue) },
                      modifier =
                          Modifier.padding(end = 8.dp).testTag("multi_select_chip_$customValue"))
                }
              }
            }

            if (fieldType.allowCustom) {
              Row(
                  modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        label = { Text("Custom value") },
                        singleLine = true,
                        modifier =
                            Modifier.weight(1f)
                                .testTag("multi_select_custom_input_${fieldDefinition.id}"),
                        colors = EurekaStyles.TextFieldColors())
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                          val trimmedValue = customText.trim()
                          if (trimmedValue.isNotBlank() && trimmedValue !in localSelectedValues) {
                            val newSelectedValues = localSelectedValues + trimmedValue
                            localSelectedValues = newSelectedValues
                            onChange(FieldValue.MultiSelectValue(newSelectedValues.toList()))
                            customText = ""
                          }
                        },
                        enabled = customText.isNotBlank(),
                        modifier =
                            Modifier.testTag("multi_select_custom_add_${fieldDefinition.id}")) {
                          Text("Add")
                        }
                  }
            }
          }
        } else {
          val selectedValues = currentValue?.values ?: emptyList()

          if (selectedValues.isEmpty()) {
            Text(
                text = "None",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("multi_select_field_value_${fieldDefinition.id}"))
          } else {
            FlowRow(
                modifier =
                    Modifier.fillMaxWidth()
                        .testTag("multi_select_field_value_${fieldDefinition.id}")) {
                  selectedValues.forEach { value ->
                    val displayText = fieldType.options.find { it.value == value }?.label ?: value
                    AssistChip(
                        onClick = {},
                        label = { Text(displayText) },
                        modifier =
                            Modifier.padding(end = 8.dp).testTag("multi_select_chip_${value}"))
                  }
                }
          }
        }
      }
}
