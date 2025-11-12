package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

@Composable
fun NumberFieldConfiguration(
    fieldType: FieldType.Number,
    onUpdate: (FieldType.Number) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = fieldType.min?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(min = it.toDoubleOrNull())) },
        label = { Text("Min") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag("number_min"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.max?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(max = it.toDoubleOrNull())) },
        label = { Text("Max") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag("number_max"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.step?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(step = it.toDoubleOrNull())) },
        label = { Text("Step") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag("number_step"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.decimals.toString(),
        onValueChange = { onUpdate(fieldType.copy(decimals = it.toIntOrNull() ?: 0)) },
        label = { Text("Decimals") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("number_decimals"),
        colors = EurekaStyles.TextFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.unit ?: "",
        onValueChange = { onUpdate(fieldType.copy(unit = it.ifBlank { null })) },
        label = { Text("Unit (e.g., kg, m/s)") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("number_unit"),
        colors = EurekaStyles.TextFieldColors())
  }
}
