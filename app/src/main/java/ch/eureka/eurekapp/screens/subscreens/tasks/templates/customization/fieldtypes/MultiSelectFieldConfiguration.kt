package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.validation.FieldTypeConstraintValidator
import ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.SelectOptionsEditor
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object MultiSelectFieldConfigurationTestTags {
  const val MIN = "multi_select_min"
  const val MAX = "multi_select_max"
  const val ALLOW_CUSTOM = "multi_select_allow_custom"
}

@Composable
fun MultiSelectFieldConfiguration(
    fieldType: FieldType.MultiSelect,
    onUpdate: (FieldType.MultiSelect) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  var localMinSelections by
      remember(fieldType) { mutableStateOf(fieldType.minSelections?.toString() ?: "") }
  var localMaxSelections by
      remember(fieldType) { mutableStateOf(fieldType.maxSelections?.toString() ?: "") }

  val parsedMinSelections = localMinSelections.trim().toIntOrNull()
  val parsedMaxSelections = localMaxSelections.trim().toIntOrNull()

  val minSelectionsParseError = localMinSelections.isNotBlank() && parsedMinSelections == null
  val maxSelectionsParseError = localMaxSelections.isNotBlank() && parsedMaxSelections == null

  val rangeError =
      FieldTypeConstraintValidator.validateSelectionsRange(parsedMinSelections, parsedMaxSelections)
  val minSelectionsConstraintError =
      FieldTypeConstraintValidator.validateMinSelections(parsedMinSelections)
  val maxSelectionsConstraintError =
      FieldTypeConstraintValidator.validateMaxSelections(parsedMaxSelections)

  val minSelectionsError =
      when {
        minSelectionsParseError -> "Invalid number"
        minSelectionsConstraintError != null -> minSelectionsConstraintError
        rangeError != null -> rangeError
        else -> null
      }
  val maxSelectionsError =
      when {
        maxSelectionsParseError -> "Invalid number"
        maxSelectionsConstraintError != null -> maxSelectionsConstraintError
        rangeError != null -> rangeError
        else -> null
      }

  fun tryUpdateModel(minStr: String, maxStr: String) {
    val newParsedMin = minStr.trim().toIntOrNull()
    val newParsedMax = maxStr.trim().toIntOrNull()

    val newMinParseError = minStr.isNotBlank() && newParsedMin == null
    val newMaxParseError = maxStr.isNotBlank() && newParsedMax == null
    if (newMinParseError || newMaxParseError) return

    val newRangeError =
        FieldTypeConstraintValidator.validateSelectionsRange(newParsedMin, newParsedMax)
    val newMinConstraintError = FieldTypeConstraintValidator.validateMinSelections(newParsedMin)
    val newMaxConstraintError = FieldTypeConstraintValidator.validateMaxSelections(newParsedMax)
    if (newRangeError != null || newMinConstraintError != null || newMaxConstraintError != null)
        return

    try {
      onUpdate(fieldType.copy(minSelections = newParsedMin, maxSelections = newParsedMax))
    } catch (e: IllegalArgumentException) {
      // Safety catch - shouldn't happen with proper validation
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    SelectOptionsEditor(
        options = fieldType.options,
        onOptionsChange = { onUpdate(fieldType.copy(options = it)) },
        enabled = enabled)

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = localMinSelections,
        onValueChange = {
          localMinSelections = it
          tryUpdateModel(it, localMaxSelections)
        },
        label = { Text(stringResource(R.string.multi_select_min_label)) },
        enabled = enabled,
        isError = minSelectionsError != null,
        supportingText = minSelectionsError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(MultiSelectFieldConfigurationTestTags.MIN),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localMaxSelections,
        onValueChange = {
          localMaxSelections = it
          tryUpdateModel(localMinSelections, it)
        },
        label = { Text(stringResource(R.string.multi_select_max_label)) },
        enabled = enabled,
        isError = maxSelectionsError != null,
        supportingText = maxSelectionsError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().testTag(MultiSelectFieldConfigurationTestTags.MAX),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = fieldType.allowCustom,
          onCheckedChange = { onUpdate(fieldType.copy(allowCustom = it)) },
          enabled = enabled,
          modifier = Modifier.testTag(MultiSelectFieldConfigurationTestTags.ALLOW_CUSTOM))
      Text(stringResource(R.string.multi_select_allow_custom))
    }
  }
}
