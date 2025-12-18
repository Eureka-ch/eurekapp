package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidationResult
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidator

object BaseFieldTestTags {
  fun toggle(fieldId: String) = "field_toggle_$fieldId"

  fun save(fieldId: String) = "field_save_$fieldId"

  fun cancel(fieldId: String) = "field_cancel_$fieldId"
}

/**
 * Callbacks for field interaction events.
 *
 * @param onModeToggle Callback when mode toggle button is clicked
 * @param onSave Callback when save button is clicked (Toggleable mode only)
 * @param onCancel Callback when cancel button is clicked (Toggleable mode only)
 */
data class FieldCallbacks(
    val onModeToggle: () -> Unit = {},
    val onSave: () -> Unit = {},
    val onCancel: () -> Unit = {}
)

/**
 * Test tags for field components.
 *
 * Provides consistent test tag generation for all field-related UI elements.
 */
object FieldComponentTestTags {
  fun base(fieldId: String) = "base_field_$fieldId"

  fun label(fieldId: String) = "field_label_$fieldId"

  fun save(fieldId: String) = "field_save_$fieldId"

  fun cancel(fieldId: String) = "field_cancel_$fieldId"

  fun toggle(fieldId: String) = "field_toggle_$fieldId"

  fun description(fieldId: String) = "field_description_$fieldId"

  fun hint(fieldId: String) = "field_hint_$fieldId"

  fun error(fieldId: String) = "field_error_$fieldId"
}

/**
 * Generic base component for all template field types.
 *
 * @param T The specific FieldType subtype
 * @param V The specific FieldValue subtype
 * @param modifier The modifier to apply to the component
 * @param fieldDefinition The field definition containing label, type, and constraints
 * @param fieldType The specific field type instance
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param callbacks Callbacks for field interaction events
 * @param showValidationErrors Whether to display validation errors
 * @param showHeader Whether to show the header (label, description, action buttons)
 * @param renderer Lambda that renders the field-specific UI
 */
@Composable
fun <T : FieldType, V : FieldValue> BaseFieldComponent(
    modifier: Modifier = Modifier,
    fieldDefinition: FieldDefinition,
    fieldType: T,
    value: V?,
    onValueChange: (V) -> Unit,
    mode: FieldInteractionMode,
    callbacks: FieldCallbacks = FieldCallbacks(),
    showValidationErrors: Boolean = false,
    showHeader: Boolean = true,
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
      is FieldInteractionMode.EditOnly -> onValueChange(newValue)
      is FieldInteractionMode.Toggleable -> {
        if (mode.isEditing) {
          editingValue = newValue
        }
      }
      is FieldInteractionMode.ViewOnly -> Unit
    }
  }

  val validationResult = getValidationResult(showValidationErrors, currentValue, fieldDefinition)

  Column(
      modifier = modifier.fillMaxWidth().testTag(FieldComponentTestTags.base(fieldDefinition.id))) {
        if (showHeader) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
                text =
                    buildString {
                      append(fieldDefinition.label)
                      if (fieldDefinition.required) {
                        append(stringResource(R.string.field_required_indicator))
                      }
                    },
                style = MaterialTheme.typography.labelLarge,
                modifier =
                    Modifier.weight(1f).testTag(FieldComponentTestTags.label(fieldDefinition.id)))

            FieldActionButtons(
                mode = mode,
                fieldDefinition = fieldDefinition,
                editingValue = editingValue,
                originalValue = originalValue,
                onValueChange = onValueChange,
                callbacks = callbacks,
                onEditingValueChange = { editingValue = it })
          }

          FieldDescription(fieldDefinition)
          Spacer(modifier = Modifier.height(8.dp))
        }
        renderer(currentValue, handleValueChange, mode.isEditing)
        FieldHint(fieldType, mode, fieldDefinition)
        ValidationErrors(validationResult, fieldDefinition)
      }
}

@Composable
private fun <V : FieldValue> FieldActionButtons(
    mode: FieldInteractionMode,
    fieldDefinition: FieldDefinition,
    editingValue: V?,
    originalValue: V?,
    onValueChange: (V) -> Unit,
    callbacks: FieldCallbacks,
    onEditingValueChange: (V?) -> Unit
) {
  if (!mode.canToggle) return

  if (mode.isEditing) {
    SaveButton(
        fieldDefinition = fieldDefinition,
        editingValue = editingValue,
        onValueChange = onValueChange,
        callbacks = callbacks)
    CancelButton(
        fieldDefinition = fieldDefinition,
        originalValue = originalValue,
        callbacks = callbacks,
        onEditingValueChange = onEditingValueChange)
  } else {
    EditButton(fieldDefinition = fieldDefinition, callbacks = callbacks)
  }
}

@Composable
private fun <V : FieldValue> SaveButton(
    fieldDefinition: FieldDefinition,
    editingValue: V?,
    onValueChange: (V) -> Unit,
    callbacks: FieldCallbacks
) {
  IconButton(
      onClick = {
        if (editingValue != null) {
          // Validate before saving
          val validationResult = FieldValidator.validate(editingValue, fieldDefinition)
          if (validationResult is FieldValidationResult.Valid) {
            onValueChange(editingValue)
            callbacks.onSave()
            callbacks.onModeToggle()
          }
          // If invalid, don't save - errors will be shown via showValidationErrors
        } else {
          // No value to save, just toggle mode
          callbacks.onModeToggle()
        }
      },
      modifier = Modifier.testTag(BaseFieldTestTags.save(fieldDefinition.id))) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = stringResource(R.string.field_save_changes),
            tint = MaterialTheme.colorScheme.primary)
      }
}

@Composable
private fun <V : FieldValue> CancelButton(
    fieldDefinition: FieldDefinition,
    originalValue: V?,
    callbacks: FieldCallbacks,
    onEditingValueChange: (V?) -> Unit
) {
  IconButton(
      onClick = {
        onEditingValueChange(originalValue)
        callbacks.onCancel()
        callbacks.onModeToggle()
      },
      modifier = Modifier.testTag(BaseFieldTestTags.cancel(fieldDefinition.id))) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(R.string.field_cancel_changes),
            tint = MaterialTheme.colorScheme.error)
      }
}

@Composable
private fun EditButton(fieldDefinition: FieldDefinition, callbacks: FieldCallbacks) {
  IconButton(
      onClick = callbacks.onModeToggle,
      modifier = Modifier.testTag(BaseFieldTestTags.toggle(fieldDefinition.id))) {
        Icon(
            imageVector = Icons.Filled.Edit,
            contentDescription = stringResource(R.string.field_edit_mode),
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }
}

@Composable
private fun FieldDescription(fieldDefinition: FieldDefinition) {
  fieldDefinition.description?.let { description ->
    Text(
        text = description,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.padding(top = 4.dp)
                .testTag(FieldComponentTestTags.description(fieldDefinition.id)))
  }
}

@Composable
private fun <T : FieldType> FieldHint(
    fieldType: T,
    mode: FieldInteractionMode,
    fieldDefinition: FieldDefinition
) {
  val hint = getConstraintHint(fieldType)
  if (hint != null && mode.isEditing) {
    Text(
        text = hint,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier =
            Modifier.padding(top = 4.dp).testTag(FieldComponentTestTags.hint(fieldDefinition.id)))
  }
}

@Composable
private fun ValidationErrors(
    validationResult: FieldValidationResult,
    fieldDefinition: FieldDefinition
) {
  if (validationResult is FieldValidationResult.Invalid) {
    validationResult.errors.forEach { error ->
      Text(
          text = error,
          style = MaterialTheme.typography.bodySmall,
          color = Color.Red,
          modifier =
              Modifier.padding(top = 4.dp)
                  .testTag(FieldComponentTestTags.error(fieldDefinition.id)))
    }
  }
}

private fun <V : FieldValue> getValidationResult(
    showValidationErrors: Boolean,
    currentValue: V?,
    fieldDefinition: FieldDefinition
): FieldValidationResult {
  return if (showValidationErrors && currentValue != null) {
    FieldValidator.validate(currentValue, fieldDefinition)
  } else if (showValidationErrors && fieldDefinition.required) {
    FieldValidationResult.Invalid(listOf("This field is required"))
  } else {
    FieldValidationResult.Valid
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
    is FieldType.Text -> getTextFieldHint(fieldType)
    is FieldType.Number -> getNumberFieldHint(fieldType)
    is FieldType.Date -> getDateFieldHint(fieldType)
    is FieldType.SingleSelect -> getSingleSelectFieldHint(fieldType)
    is FieldType.MultiSelect -> getMultiSelectFieldHint(fieldType)
  }
}

private fun getTextFieldHint(fieldType: FieldType.Text): String? {
  return buildList {
        fieldType.maxLength?.let { add("Max $it characters") }
        fieldType.minLength?.let { add("Min $it characters") }
        fieldType.pattern?.let { add("Pattern: $it") }
      }
      .joinToString(" • ")
      .takeIf { it.isNotEmpty() }
}

private fun getNumberFieldHint(fieldType: FieldType.Number): String? {
  return buildList {
        addRangeHint(fieldType.min, fieldType.max, "Range", "Min", "Max")
        fieldType.unit?.let { add("Unit: $it") }
      }
      .joinToString(" • ")
      .takeIf { it.isNotEmpty() }
}

private fun getDateFieldHint(fieldType: FieldType.Date): String? {
  return buildList {
        addRangeHint(fieldType.minDate, fieldType.maxDate, "Range", "From", "Until")
        fieldType.format?.let { add("Format: $it") }
        if (fieldType.includeTime) add("Includes time")
      }
      .joinToString(" • ")
      .takeIf { it.isNotEmpty() }
}

private fun getSingleSelectFieldHint(fieldType: FieldType.SingleSelect): String {
  val count = fieldType.options.size
  return buildString {
    append("$count option${if (count != 1) "s" else ""}")
    if (fieldType.allowCustom) append(" (custom values allowed)")
  }
}

private fun getMultiSelectFieldHint(fieldType: FieldType.MultiSelect): String {
  return buildList {
        val count = fieldType.options.size
        add("$count option${if (count != 1) "s" else ""}")

        val min = fieldType.minSelections
        val max = fieldType.maxSelections
        if (min != null && max != null) {
          add("Select $min-$max")
        } else {
          min?.let { add("Min: $it") }
          max?.let { add("Max: $it") }
        }

        if (fieldType.allowCustom) add("Custom allowed")
      }
      .joinToString(" • ")
}

private fun <T> MutableList<String>.addRangeHint(
    min: T?,
    max: T?,
    rangeLabel: String,
    minLabel: String,
    maxLabel: String
) {
  if (min != null && max != null) {
    add("$rangeLabel: $min - $max")
  } else {
    min?.let { add("$minLabel: $it") }
    max?.let { add("$maxLabel: $it") }
  }
}
