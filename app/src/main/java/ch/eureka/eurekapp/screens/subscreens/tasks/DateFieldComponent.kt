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
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Date field component for template fields.
 *
 * @param fieldDefinition The field definition containing label, constraints, etc.
 * @param value The current field value (null if empty)
 * @param onValueChange Callback when the value changes
 * @param mode The interaction mode (EditOnly, ViewOnly, or Toggleable)
 * @param onModeToggle Callback when mode toggle button is clicked
 * @param onSave Optional callback when save button is clicked (Toggleable mode only)
 * @param onCancel Optional callback when cancel button is clicked (Toggleable mode only)
 * @param showValidationErrors Whether to display validation errors
 * @param modifier The modifier to apply to the component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateFieldComponent(
    fieldDefinition: FieldDefinition,
    value: FieldValue.DateValue?,
    onValueChange: (FieldValue.DateValue) -> Unit,
    mode: FieldInteractionMode,
    onModeToggle: () -> Unit = {},
    onSave: () -> Unit = {},
    onCancel: () -> Unit = {},
    showValidationErrors: Boolean = false,
    modifier: Modifier = Modifier
) {
  val fieldType = fieldDefinition.type as FieldType.Date

  BaseFieldComponent(
      fieldDefinition = fieldDefinition,
      fieldType = fieldType,
      value = value,
      onValueChange = onValueChange,
      mode = mode,
      showValidationErrors = showValidationErrors,
      modifier = modifier) { currentValue, onChange, isEditing ->
        if (isEditing) {
          var showDatePicker by remember { mutableStateOf(false) }
          var showTimePicker by remember { mutableStateOf(false) }
          var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
          var selectedTime by remember { mutableStateOf<LocalTime?>(null) }

          OutlinedButton(
              onClick = { showDatePicker = true },
              modifier =
                  Modifier.fillMaxWidth().testTag("date_field_button_${fieldDefinition.id}")) {
                Text(
                    text =
                        currentValue?.let { formatDateValue(it, fieldType) }
                            ?: "Select Date${if (fieldType.includeTime) " & Time" else ""}")
              }

          if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                  TextButton(
                      onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                          val date =
                              Instant.ofEpochMilli(millis)
                                  .atZone(ZoneId.systemDefault())
                                  .toLocalDate()
                          selectedDate = date

                          if (fieldType.includeTime) {
                            showDatePicker = false
                            showTimePicker = true
                          } else {
                            val isoString = date.atStartOfDay(ZoneId.systemDefault()).toString()
                            onChange(FieldValue.DateValue(isoString))
                            showDatePicker = false
                          }
                        }
                      }) {
                        Text("OK")
                      }
                },
                dismissButton = {
                  TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                },
                modifier = Modifier.testTag("date_picker_dialog")) {
                  DatePicker(state = datePickerState)
                }
          }

          if (showTimePicker && selectedDate != null) {
            val timePickerState = rememberTimePickerState()
            DatePickerDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                  TextButton(
                      onClick = {
                        val time = LocalTime.of(timePickerState.hour, timePickerState.minute)
                        selectedTime = time
                        val dateTime = LocalDateTime.of(selectedDate!!, time)
                        val isoString = dateTime.atZone(ZoneId.systemDefault()).toString()
                        onChange(FieldValue.DateValue(isoString))
                        showTimePicker = false
                      }) {
                        Text("OK")
                      }
                },
                dismissButton = {
                  TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                modifier = Modifier.testTag("time_picker_dialog")) {
                  TimePicker(state = timePickerState)
                }
          }
        } else {
          Text(
              text = currentValue?.let { formatDateValue(it, fieldType) } ?: "",
              style = MaterialTheme.typography.bodyLarge,
              modifier = Modifier.testTag("date_field_value_${fieldDefinition.id}"))
        }
      }
}

/**
 * Formats a date value according to the field type constraints.
 *
 * @param dateValue The date value to format
 * @param fieldType The field type containing format constraints
 * @return Formatted date string
 */
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
  } catch (e: Exception) {
    dateValue.value
  }
}
