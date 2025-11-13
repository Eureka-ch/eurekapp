package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.utils.ExcludeFromJacocoGeneratedReport

/**
 * Number field component for template fields.
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
@Composable
fun NumberFieldComponent(
    fieldDefinition: FieldDefinition,
    value: FieldValue.NumberValue?,
    onValueChange: (FieldValue.NumberValue) -> Unit,
    mode: FieldInteractionMode,
    onModeToggle: () -> Unit = {},
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    showValidationErrors: Boolean = false,
    showHeader: Boolean = true,
    modifier: Modifier = Modifier
) {
  val fieldType = fieldDefinition.type as FieldType.Number

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
      showHeader = showHeader,
      modifier = modifier) { currentValue, onChange, isEditing ->
        if (isEditing) {
          OutlinedTextField(
              value = currentValue?.value?.toString() ?: "",
              onValueChange = { newText ->
                val trimmedText = newText.trim()
                if (trimmedText.isEmpty()) {
                  return@OutlinedTextField
                }
                if (trimmedText.matches(Regex("^-?\\d*\\.?\\d*$"))) {
                  trimmedText.toDoubleOrNull()?.let { parsedValue ->
                    onChange(FieldValue.NumberValue(parsedValue))
                  }
                }
              },
              suffix =
                  fieldType.unit?.let { unit ->
                    { Text(text = unit, style = MaterialTheme.typography.bodyMedium) }
                  },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier =
                  Modifier.fillMaxWidth().testTag("number_field_input_${fieldDefinition.id}"),
              colors = EurekaStyles.TextFieldColors())
        } else {
          val formattedValue =
              currentValue?.let {
                val decimals = fieldType.decimals ?: 0
                val formatted = "%.${decimals}f".format(it.value)
                if (fieldType.unit != null) {
                  "$formatted ${fieldType.unit}"
                } else {
                  formatted
                }
              } ?: ""

          Text(
              text = formattedValue,
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag("number_field_value_${fieldDefinition.id}"))
        }
      }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun NumberFieldComponentEditPreview() {
  MaterialTheme {
    var value by remember { mutableStateOf<FieldValue.NumberValue?>(null) }
    Column(modifier = Modifier.padding(16.dp)) {
      NumberFieldComponent(
          fieldDefinition =
              FieldDefinition(
                  id = "weight",
                  label = "Weight",
                  type = FieldType.Number(min = 0.0, max = 1000.0, decimals = 2, unit = "kg"),
                  required = true,
                  description = "Enter the weight in kilograms"),
          value = value,
          onValueChange = { value = it },
          mode = FieldInteractionMode.EditOnly,
          showValidationErrors = true)
    }
  }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun NumberFieldComponentViewPreview() {
  MaterialTheme {
    Column(modifier = Modifier.padding(16.dp)) {
      NumberFieldComponent(
          fieldDefinition =
              FieldDefinition(
                  id = "distance",
                  label = "Distance",
                  type = FieldType.Number(decimals = 1, unit = "m"),
                  required = false),
          value = FieldValue.NumberValue(42.5),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }
  }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun NumberFieldComponentToggleablePreview() {
  MaterialTheme {
    var value by remember { mutableStateOf<FieldValue.NumberValue?>(FieldValue.NumberValue(75.0)) }
    var mode by remember {
      mutableStateOf<FieldInteractionMode>(FieldInteractionMode.Toggleable(false))
    }
    Column(modifier = Modifier.padding(16.dp)) {
      NumberFieldComponent(
          fieldDefinition =
              FieldDefinition(
                  id = "percentage",
                  label = "Completion",
                  type = FieldType.Number(min = 0.0, max = 100.0, decimals = 0, unit = "%"),
                  required = false),
          value = value,
          onValueChange = { value = it },
          mode = mode,
          onModeToggle = { mode = mode.toggleEditingState() })
    }
  }
}
