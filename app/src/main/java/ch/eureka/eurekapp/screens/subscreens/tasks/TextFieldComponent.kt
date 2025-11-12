package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of AI. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.utils.ExcludeFromJacocoGeneratedReport

/**
 * Text field component for template fields.
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
fun TextFieldComponent(
    fieldDefinition: FieldDefinition,
    value: FieldValue.TextValue?,
    onValueChange: (FieldValue.TextValue) -> Unit,
    mode: FieldInteractionMode,
    onModeToggle: () -> Unit = {},
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    showValidationErrors: Boolean = false,
    modifier: Modifier = Modifier
) {
  val fieldType = fieldDefinition.type as FieldType.Text

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
          OutlinedTextField(
              value = currentValue?.value ?: "",
              onValueChange = { newText -> onChange(FieldValue.TextValue(newText)) },
              placeholder = fieldType.placeholder?.let { { Text(it) } },
              supportingText =
                  fieldType.maxLength?.let { maxLength ->
                    {
                      val currentLength = currentValue?.value?.length ?: 0
                      Text(
                          text = "$currentLength / $maxLength",
                          style = MaterialTheme.typography.bodySmall)
                    }
                  },
              singleLine = false,
              modifier = Modifier.fillMaxWidth().testTag("text_field_input_${fieldDefinition.id}"),
              colors = EurekaStyles.TextFieldColors())
        } else {
          Text(
              text = currentValue?.value ?: "",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag("text_field_value_${fieldDefinition.id}"))
        }
      }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun TextFieldComponentEditPreview() {
  MaterialTheme {
    var value by remember { mutableStateOf<FieldValue.TextValue?>(null) }
    Column(modifier = Modifier.padding(16.dp)) {
      TextFieldComponent(
          fieldDefinition =
              FieldDefinition(
                  id = "description",
                  label = "Description",
                  type = FieldType.Text(maxLength = 200, placeholder = "Enter description"),
                  required = true,
                  description = "Provide a detailed description"),
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
private fun TextFieldComponentViewPreview() {
  MaterialTheme {
    Column(modifier = Modifier.padding(16.dp)) {
      TextFieldComponent(
          fieldDefinition =
              FieldDefinition(
                  id = "description",
                  label = "Description",
                  type = FieldType.Text(maxLength = 200),
                  required = false),
          value = FieldValue.TextValue("This is a sample description text"),
          onValueChange = {},
          mode = FieldInteractionMode.ViewOnly)
    }
  }
}

@ExcludeFromJacocoGeneratedReport
@Preview(showBackground = true)
@Composable
private fun TextFieldComponentToggleablePreview() {
  MaterialTheme {
    var value by remember {
      mutableStateOf<FieldValue.TextValue?>(FieldValue.TextValue("Sample text"))
    }
    var mode by remember {
      mutableStateOf<FieldInteractionMode>(FieldInteractionMode.Toggleable(false))
    }
    Column(modifier = Modifier.padding(16.dp)) {
      TextFieldComponent(
          fieldDefinition =
              FieldDefinition(
                  id = "description",
                  label = "Description",
                  type = FieldType.Text(maxLength = 200),
                  required = false),
          value = value,
          onValueChange = { value = it },
          mode = mode,
          onModeToggle = { mode = mode.toggleEditingState() })
    }
  }
}
