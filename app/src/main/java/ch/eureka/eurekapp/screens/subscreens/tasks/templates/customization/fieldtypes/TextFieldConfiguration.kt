package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.validation.FieldTypeConstraintValidator
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object TextFieldConfigurationTestTags {
  const val MAX_LENGTH = "text_max_length"
  const val MIN_LENGTH = "text_min_length"
  const val PLACEHOLDER = "text_placeholder"
}

@Composable
fun TextFieldConfiguration(
    fieldType: FieldType.Text,
    onUpdate: (FieldType.Text) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  var localMinLength by
      remember(fieldType) { mutableStateOf(fieldType.minLength?.toString() ?: "") }
  var localMaxLength by
      remember(fieldType) { mutableStateOf(fieldType.maxLength?.toString() ?: "") }

  val parsedMinLength = localMinLength.trim().toIntOrNull()
  val parsedMaxLength = localMaxLength.trim().toIntOrNull()

  val minLengthParseError = localMinLength.isNotBlank() && parsedMinLength == null
  val maxLengthParseError = localMaxLength.isNotBlank() && parsedMaxLength == null

  val rangeError =
      FieldTypeConstraintValidator.validateTextLengthRange(parsedMinLength, parsedMaxLength)
  val minLengthConstraintError = FieldTypeConstraintValidator.validateMinLength(parsedMinLength)
  val maxLengthConstraintError = FieldTypeConstraintValidator.validateMaxLength(parsedMaxLength)

  val minLengthError =
      when {
        minLengthParseError -> "Invalid number"
        minLengthConstraintError != null -> minLengthConstraintError
        rangeError != null -> rangeError
        else -> null
      }
  val maxLengthError =
      when {
        maxLengthParseError -> "Invalid number"
        maxLengthConstraintError != null -> maxLengthConstraintError
        rangeError != null -> rangeError
        else -> null
      }

  fun tryUpdateModel() {
    if (minLengthParseError || maxLengthParseError) return
    if (rangeError != null || minLengthConstraintError != null || maxLengthConstraintError != null)
        return
    try {
      onUpdate(fieldType.copy(minLength = parsedMinLength, maxLength = parsedMaxLength))
    } catch (e: IllegalArgumentException) {
      // Safety catch - shouldn't happen with proper validation
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = localMaxLength,
        onValueChange = {
          localMaxLength = it
          tryUpdateModel()
        },
        label = { Text("Max Length") },
        enabled = enabled,
        isError = maxLengthError != null,
        supportingText = maxLengthError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(TextFieldConfigurationTestTags.MAX_LENGTH),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localMinLength,
        onValueChange = {
          localMinLength = it
          tryUpdateModel()
        },
        label = { Text("Min Length") },
        enabled = enabled,
        isError = minLengthError != null,
        supportingText = minLengthError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(TextFieldConfigurationTestTags.MIN_LENGTH),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = fieldType.placeholder ?: "",
        onValueChange = { onUpdate(fieldType.copy(placeholder = it.ifBlank { null })) },
        label = { Text("Placeholder") },
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().testTag(TextFieldConfigurationTestTags.PLACEHOLDER),
        colors = EurekaStyles.textFieldColors())
  }
}
