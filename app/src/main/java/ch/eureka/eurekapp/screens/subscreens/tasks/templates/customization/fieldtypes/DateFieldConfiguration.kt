package ch.eureka.eurekapp.screens.subscreens.tasks.templates.customization.fieldtypes

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.validation.FieldTypeConstraintValidator
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

object DateFieldConfigurationTestTags {
  const val MIN = "date_min"
  const val MAX = "date_max"
  const val INCLUDE_TIME = "date_include_time"
  const val FORMAT = "date_format"
}

@Composable
fun DateFieldConfiguration(
    fieldType: FieldType.Date,
    onUpdate: (FieldType.Date) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
  var localMinDate by remember(fieldType) { mutableStateOf(fieldType.minDate ?: "") }
  var localMaxDate by remember(fieldType) { mutableStateOf(fieldType.maxDate ?: "") }
  var localFormat by remember(fieldType) { mutableStateOf(fieldType.format ?: "yyyy-MM-dd") }

  val minDateValue = localMinDate.trim().ifBlank { null }
  val maxDateValue = localMaxDate.trim().ifBlank { null }
  val formatValue = localFormat.trim().ifBlank { null }

  val minDateParseError = FieldTypeConstraintValidator.validateDateString(minDateValue)
  val maxDateParseError = FieldTypeConstraintValidator.validateDateString(maxDateValue)
  val dateRangeError =
      if (minDateParseError == null && maxDateParseError == null) {
        FieldTypeConstraintValidator.validateDateRange(minDateValue, maxDateValue)
      } else null
  val formatError = FieldTypeConstraintValidator.validateDateFormat(formatValue)

  val minDateError =
      when {
        minDateParseError != null -> minDateParseError
        dateRangeError != null -> dateRangeError
        else -> null
      }
  val maxDateError =
      when {
        maxDateParseError != null -> maxDateParseError
        dateRangeError != null -> dateRangeError
        else -> null
      }

  fun tryUpdateModel() {
    if (minDateParseError != null ||
        maxDateParseError != null ||
        dateRangeError != null ||
        formatError != null)
        return
    try {
      onUpdate(fieldType.copy(minDate = minDateValue, maxDate = maxDateValue, format = formatValue))
    } catch (e: IllegalArgumentException) {
      // Safety catch - shouldn't happen with proper validation
    }
  }

  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
        value = localMinDate,
        onValueChange = {
          localMinDate = it
          tryUpdateModel()
        },
        label = { Text("Min Date (YYYY-MM-DD)") },
        enabled = enabled,
        isError = minDateError != null,
        supportingText = minDateError?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth().testTag(DateFieldConfigurationTestTags.MIN),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localMaxDate,
        onValueChange = {
          localMaxDate = it
          tryUpdateModel()
        },
        label = { Text("Max Date (YYYY-MM-DD)") },
        enabled = enabled,
        isError = maxDateError != null,
        supportingText = maxDateError?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth().testTag(DateFieldConfigurationTestTags.MAX),
        colors = EurekaStyles.textFieldColors())

    Spacer(modifier = Modifier.height(8.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
          checked = fieldType.includeTime,
          onCheckedChange = { onUpdate(fieldType.copy(includeTime = it)) },
          enabled = enabled,
          modifier = Modifier.testTag(DateFieldConfigurationTestTags.INCLUDE_TIME))
      Text("Include Time")
    }

    Spacer(modifier = Modifier.height(8.dp))

    OutlinedTextField(
        value = localFormat,
        onValueChange = {
          localFormat = it
          tryUpdateModel()
        },
        label = { Text("Format") },
        enabled = enabled,
        isError = formatError != null,
        supportingText = formatError?.let { { Text(it) } },
        modifier = Modifier.fillMaxWidth().testTag(DateFieldConfigurationTestTags.FORMAT),
        colors = EurekaStyles.textFieldColors())
  }
}
