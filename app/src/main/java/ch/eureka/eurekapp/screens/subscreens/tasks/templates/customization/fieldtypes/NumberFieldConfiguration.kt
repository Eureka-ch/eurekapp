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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object NumberFieldConfigurationTestTags {
  const val MIN = "number_min"
  const val MAX = "number_max"
  const val STEP = "number_step"
  const val DECIMALS = "number_decimals"
  const val UNIT = "number_unit"
}

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
        onValueChange = {
          try {
            onUpdate(fieldType.copy(min = it.trim().toDoubleOrNull()))
          } catch (e: IllegalArgumentException) {
            // Invalid state, don't update
          }
        },
        label = { Text(stringResource(R.string.number_min_label)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.MIN),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.max?.toString() ?: "",
        onValueChange = {
          try {
            onUpdate(fieldType.copy(max = it.toDoubleOrNull()))
          } catch (e: IllegalArgumentException) {
            // Invalid state, don't update
          }
        },
        label = { Text(stringResource(R.string.number_max_label)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.MAX),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.step?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(step = it.toDoubleOrNull())) },
        label = { Text(stringResource(R.string.number_step_label)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.STEP),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.decimals?.toString() ?: "",
        onValueChange = { onUpdate(fieldType.copy(decimals = it.trim().toIntOrNull() ?: 0)) },
        label = { Text(stringResource(R.string.number_decimals_label)) },
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.DECIMALS),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.unit ?: "",
        onValueChange = { onUpdate(fieldType.copy(unit = it.trim().ifBlank { null })) },
        label = { Text(stringResource(R.string.number_unit_label)) },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.UNIT),
        colors = EurekaStyles.textFieldColors())

    val min = fieldType.min
    val max = fieldType.max
    if (min != null && max != null && min > max) {
      Spacer(modifier = Modifier.height(4.dp))
      Text(
          text = stringResource(R.string.number_validation_error),
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall)
    }
  }
}
