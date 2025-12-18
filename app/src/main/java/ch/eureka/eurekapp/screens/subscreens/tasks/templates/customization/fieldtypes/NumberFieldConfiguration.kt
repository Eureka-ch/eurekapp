package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.validation.FieldTypeConstraintValidator
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
  var localMin by remember(fieldType) { mutableStateOf(fieldType.min?.toString() ?: "") }
  var localMax by remember(fieldType) { mutableStateOf(fieldType.max?.toString() ?: "") }
  var localStep by remember(fieldType) { mutableStateOf(fieldType.step?.toString() ?: "") }
  var localDecimals by remember(fieldType) { mutableStateOf(fieldType.decimals?.toString() ?: "") }

  val parsedMin = localMin.trim().toDoubleOrNull()
  val parsedMax = localMax.trim().toDoubleOrNull()
  val parsedStep = localStep.trim().toDoubleOrNull()
  val parsedDecimals = localDecimals.trim().toIntOrNull()

  val minParseError = localMin.isNotBlank() && parsedMin == null
  val maxParseError = localMax.isNotBlank() && parsedMax == null
  val stepParseError = localStep.isNotBlank() && parsedStep == null
  val decimalsParseError = localDecimals.isNotBlank() && parsedDecimals == null

  val rangeError = FieldTypeConstraintValidator.validateMinMax(parsedMin, parsedMax)
  val stepError = FieldTypeConstraintValidator.validateStep(parsedStep)
  val decimalsError = FieldTypeConstraintValidator.validateDecimals(parsedDecimals)

  val minError =
      when {
        minParseError -> "Invalid number"
        rangeError != null -> rangeError
        else -> null
      }
  val maxError =
      when {
        maxParseError -> "Invalid number"
        rangeError != null -> rangeError
        else -> null
      }
  val stepDisplayError = if (stepParseError) "Invalid number" else stepError
  val decimalsDisplayError = if (decimalsParseError) "Invalid number" else decimalsError

  fun tryUpdateModel(minStr: String, maxStr: String, stepStr: String, decimalsStr: String) {
    val newMin = minStr.trim().toDoubleOrNull()
    val newMax = maxStr.trim().toDoubleOrNull()
    val newStep = stepStr.trim().toDoubleOrNull()
    val newDecimals = decimalsStr.trim().toIntOrNull()

    val newMinParseError = minStr.isNotBlank() && newMin == null
    val newMaxParseError = maxStr.isNotBlank() && newMax == null
    val newStepParseError = stepStr.isNotBlank() && newStep == null
    val newDecimalsParseError = decimalsStr.isNotBlank() && newDecimals == null
    if (newMinParseError || newMaxParseError || newStepParseError || newDecimalsParseError) return

    val newRangeError = FieldTypeConstraintValidator.validateMinMax(newMin, newMax)
    val newStepError = FieldTypeConstraintValidator.validateStep(newStep)
    val newDecimalsError = FieldTypeConstraintValidator.validateDecimals(newDecimals)
    if (newRangeError != null || newStepError != null || newDecimalsError != null) return

    try {
      onUpdate(fieldType.copy(min = newMin, max = newMax, step = newStep, decimals = newDecimals))
    } catch (e: IllegalArgumentException) {
      // Safety catch - shouldn't happen with proper validation
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = localMin,
        onValueChange = {
          localMin = it
          tryUpdateModel(it, localMax, localStep, localDecimals)
        },
        label = { Text(stringResource(R.string.number_min_label)) },
        enabled = enabled,
        isError = minError != null,
        supportingText = minError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.MIN),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localMax,
        onValueChange = {
          localMax = it
          tryUpdateModel(localMin, it, localStep, localDecimals)
        },
        label = { Text(stringResource(R.string.number_max_label)) },
        enabled = enabled,
        isError = maxError != null,
        supportingText = maxError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.MAX),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localStep,
        onValueChange = {
          localStep = it
          tryUpdateModel(localMin, localMax, it, localDecimals)
        },
        label = { Text(stringResource(R.string.number_step_label)) },
        enabled = enabled,
        isError = stepDisplayError != null,
        supportingText = stepDisplayError?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth().testTag(NumberFieldConfigurationTestTags.STEP),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localDecimals,
        onValueChange = {
          localDecimals = it
          tryUpdateModel(localMin, localMax, localStep, it)
        },
        label = { Text(stringResource(R.string.number_decimals_label)) },
        enabled = enabled,
        isError = decimalsDisplayError != null,
        supportingText = decimalsDisplayError?.let { { Text(it) } },
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
  }
}
