package ch.eureka.eurekapp.screens.subscreens.tasks

/* Portions of this code were generated with the help of Claude Sonnet 4.5. */

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object DateFieldTestTags {
  fun button(fieldId: String) = "date_field_button_$fieldId"

  const val DATE_PICKER_DIALOG = "date_picker_dialog"

  const val TIME_PICKER_DIALOG = "time_picker_dialog"

  fun value(fieldId: String) = "date_field_value_$fieldId"
}

/**
 * Date field component for template fields.
 *
 * @param fieldDefinition The field definition containing label, constraints, etc.
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param showValidationErrors Whether to display validation errors
 * @param showHeader Whether to show the header (label, description, action buttons)
 * @param modifier The modifier to apply to the component
 * @param callbacks the callbacks to be used by the parent
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFieldComponent(
    modifier: Modifier = Modifier,
    fieldDefinition: FieldDefinition,
    value: FieldValue.DateValue?,
    onValueChange: (FieldValue.DateValue) -> Unit,
    mode: FieldInteractionMode,
    callbacks: FieldCallbacks = FieldCallbacks(),
    showValidationErrors: Boolean = false,
    showHeader: Boolean = true
) {
  val fieldType = fieldDefinition.type as FieldType.Date

  BaseFieldComponent(
      fieldDefinition = fieldDefinition,
      fieldType = fieldType,
      value = value,
      onValueChange = onValueChange,
      mode = mode,
      callbacks = callbacks,
      showValidationErrors = showValidationErrors,
      showHeader = showHeader,
      modifier = modifier) { currentValue, onChange, isEditing ->
        if (isEditing) {
          var showDatePicker by remember { mutableStateOf(false) }
          var showTimePicker by remember { mutableStateOf(false) }
          var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

          OutlinedButton(
              onClick = { showDatePicker = true },
              modifier =
                  Modifier.fillMaxWidth().testTag(DateFieldTestTags.button(fieldDefinition.id))) {
                Text(text = getDateButtonText(currentValue, fieldType))
              }

          if (showDatePicker) {
            DatePickerDialogContent(
                onDismiss = { showDatePicker = false },
                onDateSelected = { date ->
                  selectedDate = date
                  handleDateSelection(
                      date, fieldType, onChange, onShowTimePicker = { showTimePicker = true }) {
                        showDatePicker = false
                      }
                })
          }

          if (showTimePicker && selectedDate != null) {
            TimePickerDialogContent(
                onDismiss = { showTimePicker = false },
                onTimeSelected = { hour, minute ->
                  handleTimeSelection(selectedDate!!, hour, minute, onChange)
                  showTimePicker = false
                })
          }
        } else {
          Text(
              text = currentValue?.let { formatDateValue(it, fieldType) } ?: "",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag(DateFieldTestTags.value(fieldDefinition.id)))
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
) {
  val datePickerState = rememberDatePickerState()
  DatePickerDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(
            onClick = {
              datePickerState.selectedDateMillis?.let { millis ->
                val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                onDateSelected(date)
              }
            }) {
              Text(stringResource(R.string.date_picker_ok))
            }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.date_picker_cancel)) }
      },
      modifier = Modifier.testTag(DateFieldTestTags.DATE_PICKER_DIALOG)) {
        DatePicker(state = datePickerState)
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialogContent(
    onDismiss: () -> Unit,
    onTimeSelected: (Int, Int) -> Unit,
) {
  val timePickerState = rememberTimePickerState()
  DatePickerDialog(
      onDismissRequest = onDismiss,
      confirmButton = {
        TextButton(onClick = { onTimeSelected(timePickerState.hour, timePickerState.minute) }) {
          Text(stringResource(R.string.date_picker_ok))
        }
      },
      dismissButton = {
        TextButton(onClick = onDismiss) { Text(stringResource(R.string.date_picker_cancel)) }
      },
      modifier = Modifier.testTag(DateFieldTestTags.TIME_PICKER_DIALOG)) {
        TimePicker(state = timePickerState)
      }
}

private fun getDateButtonText(value: FieldValue.DateValue?, fieldType: FieldType.Date): String {
  return value?.let { formatDateValue(it, fieldType) }
      ?: "Select Date${if (fieldType.includeTime) " & Time" else ""}"
}

private fun handleDateSelection(
    date: LocalDate,
    fieldType: FieldType.Date,
    onChange: (FieldValue.DateValue) -> Unit,
    onShowTimePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
) {
  if (fieldType.includeTime) {
    onHideDatePicker()
    onShowTimePicker()
  } else {
    val isoString = date.atStartOfDay(ZoneId.systemDefault()).toString()
    onChange(FieldValue.DateValue(isoString))
    onHideDatePicker()
  }
}

private fun handleTimeSelection(
    date: LocalDate,
    hour: Int,
    minute: Int,
    onChange: (FieldValue.DateValue) -> Unit,
) {
  val time = LocalTime.of(hour, minute)
  val dateTime = LocalDateTime.of(date, time)
  val isoString = dateTime.atZone(ZoneId.systemDefault()).toString()
  onChange(FieldValue.DateValue(isoString))
}

private fun formatDateValue(dateValue: FieldValue.DateValue, fieldType: FieldType.Date): String {
  return try {
    val instant = Instant.parse(dateValue.value)
    val zonedDateTime = instant.atZone(ZoneId.systemDefault())

    val formatter =
        if (fieldType.format != null) {
          DateTimeFormatter.ofPattern(fieldType.format)
        } else if (fieldType.includeTime) {
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        } else {
          DateTimeFormatter.ofPattern("yyyy-MM-dd")
        }

    zonedDateTime.format(formatter)
  } catch (_: Exception) {
    dateValue.value
  }
}
