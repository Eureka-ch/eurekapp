package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import ch.eureka.eurekapp.ui.designsystem.EurekaTheme
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

private sealed interface SelectState {
  data object Empty : SelectState

  data class Predefined(val value: String) : SelectState

  data class Custom(val text: String) : SelectState
}

/**
 * Single select field component for template fields.
 *
 * @param fieldDefinition The field definition containing label, constraints, etc.
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param showValidationErrors Whether to display validation errors
 * @param modifier The modifier to apply to the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleSelectFieldComponent(
    modifier: Modifier = Modifier,
    fieldDefinition: FieldDefinition,
    value: FieldValue.SingleSelectValue?,
    onValueChange: (FieldValue.SingleSelectValue) -> Unit,
    mode: FieldInteractionMode,
    showValidationErrors: Boolean = false,
    callbacks: FieldCallbacks = FieldCallbacks(),
) {
  val fieldType = fieldDefinition.type as FieldType.SingleSelect

  BaseFieldComponent(
      modifier = modifier,
      fieldDefinition = fieldDefinition,
      fieldType = fieldType,
      value = value,
      onValueChange = onValueChange,
      mode = mode,
      callbacks = callbacks,
      showValidationErrors = showValidationErrors,
  ) { currentValue, onChange, isEditing ->
    if (isEditing) {
      var expanded by remember { mutableStateOf(false) }
      var selectState by
          remember(currentValue) { mutableStateOf(toSelectState(currentValue, fieldType)) }

      SingleSelectEditMode(
          fieldDefinition = fieldDefinition,
          fieldType = fieldType,
          selectState = selectState,
          expanded = expanded,
          onExpandedChange = { expanded = it },
          onSelectStateChange = { selectState = it },
          onChange = onChange)
    } else {
      SingleSelectViewMode(fieldDefinition, fieldType, currentValue)
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SingleSelectEditMode(
    fieldDefinition: FieldDefinition,
    fieldType: FieldType.SingleSelect,
    selectState: SelectState,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelectStateChange: (SelectState) -> Unit,
    onChange: (FieldValue.SingleSelectValue) -> Unit,
) {
  val displayValue = getDisplayValue(selectState, fieldType)

  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = { onExpandedChange(!expanded) },
      modifier =
          Modifier.fillMaxWidth().testTag("single_select_field_dropdown_${fieldDefinition.id}")) {
        OutlinedTextField(
            value = displayValue,
            onValueChange = { newValue ->
              handleTextFieldValueChange(
                  newValue, selectState, fieldType, onSelectStateChange, onChange)
            },
            readOnly = selectState !is SelectState.Custom || !fieldType.allowCustom,
            label = { Text(getTextFieldLabel(selectState)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier =
                Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .testTag("single_select_field_input_${fieldDefinition.id}"),
            colors = EurekaStyles.TextFieldColors())

        RenderDropdownMenu(
            fieldDefinition,
            fieldType,
            expanded,
            onDismissRequest = { onExpandedChange(false) },
            onSelectStateChange,
            onChange)
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenderDropdownMenu(
    fieldDefinition: FieldDefinition,
    fieldType: FieldType.SingleSelect,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onSelectStateChange: (SelectState) -> Unit,
    onChange: (FieldValue.SingleSelectValue) -> Unit,
) {
  ExposedDropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismissRequest,
      modifier = Modifier.testTag("single_select_field_menu_${fieldDefinition.id}")) {
        RenderOptionMenuItems(fieldType, onSelectStateChange, onChange, onDismissRequest)
        if (fieldType.allowCustom) {
          RenderCustomMenuOption(onSelectStateChange, onDismissRequest)
        }
      }
}

@Composable
private fun RenderOptionMenuItems(
    fieldType: FieldType.SingleSelect,
    onSelectStateChange: (SelectState) -> Unit,
    onChange: (FieldValue.SingleSelectValue) -> Unit,
    onDismiss: () -> Unit,
) {
  fieldType.options.forEach { option ->
    DropdownMenuItem(
        text = {
          Column {
            Text(text = option.label, style = MaterialTheme.typography.bodyLarge)
            option.description?.let { desc ->
              Text(
                  text = desc,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        },
        onClick = {
          onSelectStateChange(SelectState.Predefined(option.value))
          onChange(FieldValue.SingleSelectValue(option.value))
          onDismiss()
        },
        modifier = Modifier.testTag("single_select_option_${option.value}"))
  }
}

@Composable
private fun RenderCustomMenuOption(
    onSelectStateChange: (SelectState) -> Unit,
    onDismiss: () -> Unit,
) {
  HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
  DropdownMenuItem(
      text = { Text("Custom value") },
      onClick = {
        onSelectStateChange(SelectState.Custom(""))
        onDismiss()
      },
      modifier = Modifier.testTag("single_select_option_custom"))
}

@Composable
private fun SingleSelectViewMode(
    fieldDefinition: FieldDefinition,
    fieldType: FieldType.SingleSelect,
    currentValue: FieldValue.SingleSelectValue?,
) {
  val displayText = getViewModeDisplayText(currentValue, fieldType)
  Text(
      text = displayText,
      style = MaterialTheme.typography.bodyLarge,
      modifier = Modifier.testTag("single_select_field_value_${fieldDefinition.id}"))
}

private fun toSelectState(
    value: FieldValue.SingleSelectValue?,
    fieldType: FieldType.SingleSelect
): SelectState =
    when {
      value == null -> SelectState.Empty
      fieldType.options.any { it.value == value.value } -> SelectState.Predefined(value.value)
      else -> SelectState.Custom(value.value)
    }

private fun getDisplayValue(selectState: SelectState, fieldType: FieldType.SingleSelect): String =
    when (selectState) {
      is SelectState.Empty -> ""
      is SelectState.Predefined ->
          fieldType.options.find { it.value == selectState.value }?.label ?: ""
      is SelectState.Custom -> selectState.text
    }

private fun getTextFieldLabel(selectState: SelectState): String =
    if (selectState is SelectState.Custom) "Enter custom value" else "Select option"

private fun handleTextFieldValueChange(
    newValue: String,
    selectState: SelectState,
    fieldType: FieldType.SingleSelect,
    onSelectStateChange: (SelectState) -> Unit,
    onChange: (FieldValue.SingleSelectValue) -> Unit,
) {
  if (selectState is SelectState.Custom) {
    onSelectStateChange(SelectState.Custom(newValue))
    onChange(FieldValue.SingleSelectValue(newValue))
  }
}

private fun getViewModeDisplayText(
    currentValue: FieldValue.SingleSelectValue?,
    fieldType: FieldType.SingleSelect
): String =
    currentValue?.value?.let { currentVal ->
      fieldType.options.find { it.value == currentVal }?.label ?: currentVal
    } ?: ""

@Preview(showBackground = true)
@Composable
private fun SingleSelectFieldComponentPreview() {
  val testOptions =
      listOf(
          SelectOption("low", "Low", "Low priority"),
          SelectOption("medium", "Medium", "Medium priority"),
          SelectOption("high", "High", "High priority"))

  val testFieldDefinition =
      FieldDefinition(
          id = "test_select",
          label = "Test Single Select",
          type = FieldType.SingleSelect(options = testOptions, allowCustom = true),
          required = false)

  var currentValue by remember { mutableStateOf<FieldValue.SingleSelectValue?>(null) }
  val focusManager = LocalFocusManager.current

  EurekaTheme(false) {
    Surface {
      Box(modifier = Modifier.height(400.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          SingleSelectFieldComponent(
              fieldDefinition = testFieldDefinition,
              value = currentValue,
              onValueChange = { currentValue = it },
              mode = FieldInteractionMode.EditOnly,
              showValidationErrors = false)
          Spacer(
              modifier =
                  Modifier.fillMaxSize()
                      .clickable(
                          onClick = { focusManager.clearFocus() },
                          indication = null,
                          interactionSource = remember { MutableInteractionSource() }))
        }
      }
    }
  }
}
