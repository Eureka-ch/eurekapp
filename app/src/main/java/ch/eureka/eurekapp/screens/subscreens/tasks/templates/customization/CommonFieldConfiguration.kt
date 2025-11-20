package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.screens.subscreens.tasks.DateFieldComponent
import ch.eureka.eurekapp.screens.subscreens.tasks.FieldInteractionMode
import ch.eureka.eurekapp.screens.subscreens.tasks.MultiSelectFieldComponent
import ch.eureka.eurekapp.screens.subscreens.tasks.NumberFieldComponent
import ch.eureka.eurekapp.screens.subscreens.tasks.SingleSelectFieldComponent
import ch.eureka.eurekapp.screens.subscreens.tasks.TextFieldComponent
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object CommonFieldConfigurationTestTags {
  const val LABEL_INPUT = "field_label_input"
  const val DESCRIPTION_INPUT = "field_description_input"
  const val REQUIRED_CHECKBOX = "field_required_checkbox"
}

@Composable
fun CommonFieldConfiguration(
    field: FieldDefinition,
    onFieldUpdate: (FieldDefinition) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = field.label,
        onValueChange = { onFieldUpdate(field.copy(label = it)) },
        label = { Text("Label *") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag(CommonFieldConfigurationTestTags.LABEL_INPUT),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = field.description ?: "",
        onValueChange = { onFieldUpdate(field.copy(description = it.ifBlank { null })) },
        label = { Text("Description") },
        enabled = enabled,
        modifier =
            Modifier.fillMaxWidth().testTag(CommonFieldConfigurationTestTags.DESCRIPTION_INPUT),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = field.required,
          onCheckedChange = { onFieldUpdate(field.copy(required = it)) },
          enabled = enabled,
          modifier = Modifier.testTag(CommonFieldConfigurationTestTags.REQUIRED_CHECKBOX))
      Text("Required")
    }
  }
}

@Composable
fun DefaultValueInput(
    field: FieldDefinition,
    onFieldUpdate: (FieldDefinition) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text("Default Value", style = MaterialTheme.typography.labelLarge)
    Spacer(modifier = Modifier.height(4.dp))

    val mode = if (enabled) FieldInteractionMode.EditOnly else FieldInteractionMode.ViewOnly

    when (field.type) {
      is FieldType.Text -> {
        TextFieldComponent(
            fieldDefinition = field,
            value = field.defaultValue as? FieldValue.TextValue,
            onValueChange = { onFieldUpdate(field.copy(defaultValue = it)) },
            mode = mode,
            showHeader = false)
      }
      is FieldType.Number -> {
        NumberFieldComponent(
            fieldDefinition = field,
            value = field.defaultValue as? FieldValue.NumberValue,
            onValueChange = { onFieldUpdate(field.copy(defaultValue = it)) },
            mode = mode,
            showHeader = false)
      }
      is FieldType.Date -> {
        DateFieldComponent(
            fieldDefinition = field,
            value = field.defaultValue as? FieldValue.DateValue,
            onValueChange = { onFieldUpdate(field.copy(defaultValue = it)) },
            mode = mode,
            showHeader = false)
      }
      is FieldType.SingleSelect -> {
        SingleSelectFieldComponent(
            fieldDefinition = field.copy(type = field.type.copy(allowCustom = false)),
            value = field.defaultValue as? FieldValue.SingleSelectValue,
            onValueChange = { onFieldUpdate(field.copy(defaultValue = it)) },
            mode = mode,
            showHeader = false)
      }
      is FieldType.MultiSelect -> {
        MultiSelectFieldComponent(
            fieldDefinition = field.copy(type = field.type.copy(allowCustom = false)),
            value = field.defaultValue as? FieldValue.MultiSelectValue,
            onValueChange = { onFieldUpdate(field.copy(defaultValue = it)) },
            mode = mode,
            showHeader = false)
      }
    }
  }
}
