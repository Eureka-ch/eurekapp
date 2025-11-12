package ch.eureka.eurekapp.screens.subscreens.tasks

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * - Mode toggle button (for Toggleable mode) with save/cancel actions
 * - Field-specific rendering via renderer lambda
 *
 * ## Value Change Behavior by Mode:
 *
 * **EditOnly Mode:**
 * - Changes are immediate
 * - `onValueChange` is called on every user input
 * - No save/cancel buttons shown
 * - Example: Task creation form where all fields are editable
 *
 * **Toggleable Mode:**
 * - Changes are buffered locally until user clicks save (✓) or cancel (✗)
 * - Edit button shown when viewing, save/cancel buttons shown when editing
 * - `onValueChange` is ONLY called when user clicks save button
 * - `onCancel` is called when user clicks cancel button (changes discarded)
 * - Example: Task editing where user can toggle individual fields
 *
 * **ViewOnly Mode:**
 * - No editing allowed
 * - `onValueChange` is never called
 * - No buttons shown
 * - Example: Completed or locked fields
 *
 * ## Usage Example:
 * ```kotlin
 * var fieldValue by remember { mutableStateOf<FieldValue.TextValue?>(null) }
 * var mode by remember { mutableStateOf<FieldInteractionMode>(Toggleable(false)) }
 *
 * TextFieldComponent(
 *     value = fieldValue,
 *     onValueChange = { fieldValue = it }, // Only called on save in Toggleable mode
 *     mode = mode,
 *     onModeToggle = { mode = mode.toggleEditingState() },
 *     onSave = {
 *         // Optional: Trigger Firebase save, validation, etc.
 *         viewModel.saveField(fieldValue)
 *     },
 *     onCancel = {
 *         // Optional: Log cancellation, show message, etc.
 *         Log.d("Form", "Edit cancelled")
 *     }
 * )
 * ```
 *
 * Portions of this code were generated with the help of AI.
 *
 * @param T The specific FieldType subtype
 * @param V The specific FieldValue subtype
 * @param fieldDefinition The field definition containing label, type, and constraints
 * @param fieldType The specific field type instance
 * @param value The current field value from parent state (null if empty)
 * @param onValueChange Callback when value should be persisted. Called immediately in EditOnly
 *   mode, only on save in Toggleable mode, never in ViewOnly mode.
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param onModeToggle Callback when edit button is clicked (Toggleable mode only)
 * @param onSave Optional callback when save button (✓) is clicked after `onValueChange`. Use for
 *   persistence logic like Firebase saves. (Toggleable mode only)
 * @param onCancel Optional callback when cancel button (✗) is clicked. Changes are automatically
 *   discarded. (Toggleable mode only)
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
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    showValidationErrors: Boolean = false,
    modifier: Modifier = Modifier,
    renderer: @Composable (value: V?, onValueChange: (V) -> Unit, isEditing: Boolean) -> Unit
) {
  var editingValue by
      remember(mode) {
        mutableStateOf(
            if (mode is FieldInteractionMode.Toggleable && mode.isEditing) value else null)
      }
  var originalValue by
      remember(mode) {
        mutableStateOf(
            if (mode is FieldInteractionMode.Toggleable && mode.isEditing) value else null)
      }

  var prevIsEditing by remember { mutableStateOf(mode.isEditing) }

  LaunchedEffect(mode.isEditing) {
    if (mode.isEditing && !prevIsEditing && mode is FieldInteractionMode.Toggleable) {
      originalValue = value
      editingValue = value
    } else if (!mode.isEditing && prevIsEditing) {
      editingValue = null
      originalValue = null
    }
    prevIsEditing = mode.isEditing
  }

  val currentValue =
      if (mode is FieldInteractionMode.Toggleable && mode.isEditing) {
        editingValue
      } else {
        value
      }

  val handleValueChange: (V) -> Unit = { newValue ->
    when (mode) {
      is FieldInteractionMode.EditOnly -> {
        onValueChange(newValue)
      }
      is FieldInteractionMode.Toggleable -> {
        if (mode.isEditing) {
          editingValue = newValue
        }
      }
      is FieldInteractionMode.ViewOnly -> {}
    }
  }

  val validationResult =
      if (showValidationErrors && currentValue != null) {
        FieldValidator.validate(currentValue, fieldDefinition)
      } else if (showValidationErrors && fieldDefinition.required && currentValue == null) {
        FieldValidationResult.Invalid(listOf("This field is required"))
      } else {
        FieldValidationResult.Valid
      }

  Column(modifier = modifier.fillMaxWidth().testTag("base_field_${fieldDefinition.id}")) {
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
        if (mode.isEditing) {
          IconButton(
              onClick = {
                editingValue?.let { onValueChange(it) }
                onSave()
                onModeToggle()
              },
              modifier = Modifier.testTag("field_save_${fieldDefinition.id}")) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Save changes",
                    tint = MaterialTheme.colorScheme.primary)
              }
          IconButton(
              onClick = {
                editingValue = originalValue
                onCancel()
                onModeToggle()
              },
              modifier = Modifier.testTag("field_cancel_${fieldDefinition.id}")) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Cancel changes",
                    tint = MaterialTheme.colorScheme.error)
              }
        } else {
          IconButton(
              onClick = onModeToggle,
              modifier = Modifier.testTag("field_toggle_${fieldDefinition.id}")) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Switch to edit mode",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
              }
        }
      }
    }

    fieldDefinition.description?.let { description ->
      Text(
          text = description,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier =
              Modifier.padding(top = 4.dp).testTag("field_description_${fieldDefinition.id}"))
    }

    Spacer(modifier = Modifier.height(8.dp))

    renderer(currentValue, handleValueChange, mode.isEditing)

    val hint = getConstraintHint(fieldType)
    if (hint != null && mode.isEditing) {
      Text(
          text = hint,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp).testTag("field_hint_${fieldDefinition.id}"))
    }

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
