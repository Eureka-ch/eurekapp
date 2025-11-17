package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

@Composable
fun TextFieldConfiguration(
    fieldType: FieldType.Text,
    onUpdate: (FieldType.Text) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = fieldType.maxLength?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(maxLength = it.toIntOrNull())) },
        label = { Text("Max Length") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("text_max_length"),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.minLength?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(minLength = it.toIntOrNull())) },
        label = { Text("Min Length") },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag("text_min_length"),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.placeholder ?: "",
        onValueChange = { onUpdate(fieldType.copy(placeholder = it.ifBlank { null })) },
        label = { Text("Placeholder") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag("text_placeholder"),
        colors = EurekaStyles.textFieldColors())

    val minLength = fieldType.minLength
    val maxLength = fieldType.maxLength
    if (minLength != null && maxLength != null && minLength > maxLength) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = "Minimum length must be less than or equal to maximum length",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall)
    }
  }
}
