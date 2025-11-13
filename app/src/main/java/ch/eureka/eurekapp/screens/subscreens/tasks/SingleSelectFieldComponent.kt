package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

/**
 * Single select field component for template fields.
 *
 * @param fieldDefinition The field definition containing label, constraints, etc.
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param onModeToggle Callback when mode toggle button is clicked
 * @param onSave Optional callback when save button is clicked (Toggleable mode only)
 * @param onCancel Optional callback when cancel button is clicked (Toggleable mode only)
 * @param showValidationErrors Whether to display validation errors
 * @param modifier The modifier to apply to the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleSelectFieldComponent(
    fieldDefinition: FieldDefinition,
    value: FieldValue.SingleSelectValue?,
    onValueChange: (FieldValue.SingleSelectValue) -> Unit,
    mode: FieldInteractionMode,
    onModeToggle: () -> Unit = {},
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    showValidationErrors: Boolean = false,
    modifier: Modifier = Modifier
) {
  val fieldType = fieldDefinition.type as FieldType.SingleSelect

  BaseFieldComponent(
      fieldDefinition = fieldDefinition,
      fieldType = fieldType,
      value = value,
      onValueChange = onValueChange,
      mode = mode,
      onModeToggle = onModeToggle,
      onSave = onSave,
      onCancel = onCancel,
      showValidationErrors = showValidationErrors,
      modifier = modifier) { currentValue, onChange, isEditing ->
        if (isEditing) {
          var expanded by remember { mutableStateOf(false) }
          var isInCustomMode by remember { mutableStateOf(false) }
          var customText by remember { mutableStateOf("") }

          val isCustomValue =
              currentValue?.value?.let { currentVal ->
                fieldType.options.none { it.value == currentVal }
              } ?: false

          LaunchedEffect(currentValue) {
            if (isCustomValue) {
              isInCustomMode = true
              customText = currentValue?.value ?: ""
            } else {
              isInCustomMode = false
              customText = ""
            }
          }

          val displayValue =
              when {
                isInCustomMode -> customText
                else -> {
                  currentValue?.value?.let { value ->
                    fieldType.options.find { it.value == value }?.label
                  } ?: ""
                }
              }

          ExposedDropdownMenuBox(
              expanded = expanded,
              onExpandedChange = { expanded = !expanded },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag("single_select_field_dropdown_${fieldDefinition.id}")) {
                OutlinedTextField(
                    value = displayValue,
                    onValueChange = { newValue ->
                      if (isInCustomMode) {
                        customText = newValue
                        onChange(FieldValue.SingleSelectValue(newValue))
                      }
                    },
                    readOnly =
                        when {
                          !fieldType.allowCustom -> true
                          isInCustomMode -> false
                          else -> true
                        },
                    label = { Text(if (isInCustomMode) "Enter custom value" else "Select option") },
                    trailingIcon = {
                      ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    modifier =
                        Modifier.fillMaxWidth()
                            .menuAnchor()
                            .testTag("single_select_field_input_${fieldDefinition.id}"),
                    colors = EurekaStyles.TextFieldColors())

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.testTag("single_select_field_menu_${fieldDefinition.id}")) {
                      fieldType.options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                              Column {
                                Text(
                                    text = option.label, style = MaterialTheme.typography.bodyLarge)
                                option.description?.let { desc ->
                                  Text(
                                      text = desc,
                                      style = MaterialTheme.typography.bodySmall,
                                      color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                              }
                            },
                            onClick = {
                              isInCustomMode = false
                              customText = ""
                              onChange(FieldValue.SingleSelectValue(option.value))
                              expanded = false
                            },
                            modifier = Modifier.testTag("single_select_option_${option.value}"))
                      }

                      if (fieldType.allowCustom) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text("Custom value") },
                            onClick = {
                              isInCustomMode = true
                              expanded = false
                            },
                            modifier = Modifier.testTag("single_select_option_custom"))
                      }
                    }
              }
        } else {
          val displayText =
              currentValue?.value?.let { currentVal ->
                fieldType.options.find { it.value == currentVal }?.label ?: currentVal
              } ?: ""

          Text(
              text = displayText,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag("single_select_field_value_${fieldDefinition.id}"))
        }
      }
}
