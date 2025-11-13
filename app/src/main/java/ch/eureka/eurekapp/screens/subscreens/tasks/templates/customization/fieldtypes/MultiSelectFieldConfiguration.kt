package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

@Composable
fun MultiSelectFieldConfiguration(
    fieldType: FieldType.MultiSelect,
    onUpdate: (FieldType.MultiSelect) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = fieldType.minSelections?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(minSelections = it.toIntOrNull())) },
        label = { Text("Min Selections") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("multi_select_min"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.maxSelections?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(maxSelections = it.toIntOrNull())) },
        label = { Text("Max Selections") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("multi_select_max"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = fieldType.allowCustom,
          onCheckedChange = { onUpdate(fieldType.copy(allowCustom = it)) },
          enabled = enabled,
          modifier = Modifier.testTag("multi_select_allow_custom"))
      Text("Allow Custom Values")
    }
  }
}
