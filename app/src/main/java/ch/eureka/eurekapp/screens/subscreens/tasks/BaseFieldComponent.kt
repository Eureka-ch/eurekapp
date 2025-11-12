package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidationResult
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidator

/**
 * Generic base component for all template field types.
 *
 * Provides shared functionality including:
 * - Label display with required indicator (*)
 * - Validation error display
 * - Constraint hints
 * - Mode toggle button (for Toggleable mode)
 * - Field-specific rendering via renderer lambda
 *
 * Portions of this code were generated with the help of AI.
 *
 * @param T The specific FieldType subtype
 * @param V The specific FieldValue subtype
 * @param fieldDefinition The field definition containing label, type, and constraints
 * @param fieldType The specific field type instance
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param onModeToggle Callback when mode toggle button is clicked (only for Toggleable mode)
 * @param showValidationErrors Whether to display validation errors
 * @param modifier The modifier to apply to the root composable
 * @param renderer Lambda that renders the field-specific UI
 */
@Composable
fun <T : FieldType, V : FieldValue> BaseFieldComponent(
    fieldDefinition: FieldDefinition,
    fieldType: T,
    value: V?,
    onValueChange: (V) -> Unit,
    mode: FieldInteractionMode,
    onModeToggle: () -> Unit = {},
    showValidationErrors: Boolean = false,
    modifier: Modifier = Modifier,
    renderer: @Composable (value: V?, onValueChange: (V) -> Unit, isEditing: Boolean) -> Unit
) {
  val validationResult =
      if (showValidationErrors && value != null) {
        FieldValidator.validate(value, fieldDefinition)
      } else if (showValidationErrors && fieldDefinition.required && value == null) {
        FieldValidationResult.Invalid(listOf("This field is required"))
      } else {
        FieldValidationResult.Valid
      }

  Column(modifier = modifier.fillMaxWidth().testTag("base_field_${fieldDefinition.id}")) {
    // Label row with required indicator and mode toggle
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          text =
              buildString {
                append(fieldDefinition.label)
                if (fieldDefinition.required) {
                  append(" *")
                }
              },
          style = MaterialTheme.typography.labelLarge,
          modifier = Modifier.weight(1f).testTag("field_label_${fieldDefinition.id}"))

      if (mode.canToggle) {
        IconButton(
            onClick = onModeToggle,
            modifier = Modifier.testTag("field_toggle_${fieldDefinition.id}")) {
              Icon(
                  imageVector = Icons.Filled.Edit,
                  contentDescription =
                      if (mode.isEditing) "Switch to view mode" else "Switch to edit mode",
                  tint =
                      if (mode.isEditing) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurfaceVariant)
            }
      }
    }

    // Description if available
    fieldDefinition.description?.let { description ->
      Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier =
              Modifier.padding(top = 4.dp).testTag("field_description_${fieldDefinition.id}"))
    }

    Spacer(modifier = Modifier.height(8.dp))

    // Field-specific renderer
    renderer(value, onValueChange, mode.isEditing)

    // Constraint hints
    val hint = getConstraintHint(fieldType)
    if (hint != null && mode.isEditing) {
      Text(
          text = hint,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp).testTag("field_hint_${fieldDefinition.id}"))
    }

    // Validation errors
    if (validationResult is FieldValidationResult.Invalid) {
      validationResult.errors.forEach { error ->
        Text(
            text = error,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Red,
            modifier = Modifier.padding(top = 4.dp).testTag("field_error_${fieldDefinition.id}"))
      }
    }
  }
}

/**
 * Generates constraint hint text based on field type constraints.
 *
 * @param fieldType The field type to generate hints for
 * @return A hint string describing the constraints, or null if no hints available
 */
internal fun getConstraintHint(fieldType: FieldType): String? {
  return when (fieldType) {
    is FieldType.Text -> {
      buildList {
            fieldType.maxLength?.let { add("Max $it characters") }
            fieldType.minLength?.let { add("Min $it characters") }
            fieldType.pattern?.let { add("Pattern: $it") }
          }
          .joinToString(" • ")
          .takeIf { it.isNotEmpty() }
    }
    is FieldType.Number -> {
      buildList {
            if (fieldType.min != null && fieldType.max != null) {
              add("Range: ${fieldType.min} - ${fieldType.max}")
            } else {
              fieldType.min?.let { add("Min: $it") }
              fieldType.max?.let { add("Max: $it") }
            }
            fieldType.unit?.let { add("Unit: $it") }
          }
          .joinToString(" • ")
          .takeIf { it.isNotEmpty() }
    }
    is FieldType.Date -> {
      buildList {
            if (fieldType.minDate != null && fieldType.maxDate != null) {
              add("Range: ${fieldType.minDate} - ${fieldType.maxDate}")
            } else {
              fieldType.minDate?.let { add("From: $it") }
              fieldType.maxDate?.let { add("Until: $it") }
            }
            fieldType.format?.let { add("Format: $it") }
            if (fieldType.includeTime) add("Includes time")
          }
          .joinToString(" • ")
          .takeIf { it.isNotEmpty() }
    }
    is FieldType.SingleSelect -> {
      val count = fieldType.options.size
      buildString {
        append("$count option${if (count != 1) "s" else ""}")
        if (fieldType.allowCustom) append(" (custom values allowed)")
      }
    }
    is FieldType.MultiSelect -> {
      buildList {
            val count = fieldType.options.size
            add("$count option${if (count != 1) "s" else ""}")
            if (fieldType.minSelections != null && fieldType.maxSelections != null) {
              add("Select ${fieldType.minSelections}-${fieldType.maxSelections}")
            } else {
              fieldType.minSelections?.let { add("Min: $it") }
              fieldType.maxSelections?.let { add("Max: $it") }
            }
            if (fieldType.allowCustom) add("Custom allowed")
          }
          .joinToString(" • ")
    }
  }
}
