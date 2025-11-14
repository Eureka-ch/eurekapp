package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

/**
 * Test tags for text field components.
 *
 * Provides consistent test tag generation for text field-specific UI elements.
 */
object TextFieldComponentTestTags {
  fun input(fieldId: String) = "text_field_input_$fieldId"

  fun value(fieldId: String) = "text_field_value_$fieldId"
}

/**
 * Text field component for template fields.
 *
 * @param modifier The modifier to apply to the component
 * @param fieldDefinition The field definition containing label, constraints, etc.
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param callbacks Callbacks for field interaction events
 * @param showValidationErrors Whether to display validation errors
 */
@Composable
fun TextFieldComponent(
    modifier: Modifier = Modifier,
    fieldDefinition: FieldDefinition,
    value: FieldValue.TextValue?,
    onValueChange: (FieldValue.TextValue) -> Unit,
    mode: FieldInteractionMode,
    callbacks: FieldCallbacks = FieldCallbacks(),
    showValidationErrors: Boolean = false
) {
  val fieldType = fieldDefinition.type as FieldType.Text

  BaseFieldComponent(
      modifier = modifier,
      fieldDefinition = fieldDefinition,
      fieldType = fieldType,
      value = value,
      onValueChange = onValueChange,
      mode = mode,
      callbacks = callbacks,
      showValidationErrors = showValidationErrors) { currentValue, onChange, isEditing ->
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
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(TextFieldComponentTestTags.input(fieldDefinition.id)),
              colors = EurekaStyles.TextFieldColors())
        } else {
          Text(
              text = currentValue?.value ?: "",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(TextFieldComponentTestTags.value(fieldDefinition.id)))
        }
      }
}
