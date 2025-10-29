/*
Portions of the code in this file were written with the help of chatGPT.
Portions of the code in this file are copy-pasted from the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Test tags for the create meeting screen. */
object CreateMeetingScreenTestTags {
  const val CREATE_MEETING_SCREEN_TITLE = "CreateMeetingScreenTitle"
  const val CREATE_MEETING_SCREEN_DESCRIPTION = "CreateMeetingScreenDescription"
  const val ERROR_MSG = "ErrorMsg"
  const val INPUT_MEETING_TITLE = "InputMeetingTitle"
  const val INPUT_MEETING_DATE = "InputMeetingDate"
  const val INPUT_MEETING_START_TIME = "InputMeetingStartTime"
  const val INPUT_MEETING_END_TIME = "InputMeetingEndTime"
  const val CREATE_MEETING_BUTTON = "CreateMeetingButton"
}

/**
 * Composable that displays the create meeting proposal screen.
 *
 * @param projectId The ID of the project on which to create meetings for.
 * @param onDone Function called when meeting proposal was correctly created and saved on the
 *   database.
 * @param createMeetingViewModel View model associated with create meeting screen.
 */
@Composable
fun CreateMeetingScreen(
    projectId: String,
    onDone: () -> Unit,
    createMeetingViewModel: CreateMeetingViewModel = viewModel()
) {

  val context = LocalContext.current
  val uiState by createMeetingViewModel.uiState.collectAsState()

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      createMeetingViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(uiState.meetingSaved) {
    if (uiState.meetingSaved) {
      onDone()
    }
  }

  Scaffold(
      content = { padding ->
        /*
        `padding` value is not symmetric here, it is zero on the left and on the right but non-zero
        on the top and on the bottom. Thus it is more beautiful to only apply a constant padding of
        `10` on the top, bottom, left and right.
         */
        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
          Text(
              modifier = Modifier.testTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_TITLE),
              text = "Create Meeting",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
              modifier =
                  Modifier.testTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_DESCRIPTION),
              text = "Create a team meeting proposal",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.Gray)

          Spacer(Modifier.height(16.dp))

          OutlinedTextField(
              value = uiState.title,
              onValueChange = { createMeetingViewModel.setTitle(it) },
              label = { Text("Title") },
              placeholder = { Text("Title of the meeting") },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
                      .onFocusChanged { focusState ->
                        if (focusState.isFocused) {
                          createMeetingViewModel.touchTitle()
                        }
                      })
          if (uiState.title.isBlank() && uiState.hasTouchedTitle) {
            Text(
                text = "Title cannot be empty",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(CreateMeetingScreenTestTags.ERROR_MSG))
          }

          Spacer(Modifier.height(8.dp))

          DateInputField(
              selectedDate = uiState.date,
              label = "Date",
              placeHolder = "Select date",
              tag = CreateMeetingScreenTestTags.INPUT_MEETING_DATE,
              onDateSelected = { createMeetingViewModel.setDate(it) })

          Spacer(Modifier.height(8.dp))

          val starTimeConfig =
              TimeInputFieldConfig(
                  selectedTime = uiState.startTime,
                  label = "Start Time",
                  placeHolder = "Select start time",
                  onFieldTouched = { createMeetingViewModel.touchStartTime() },
                  isInvalid =
                      uiState.hasTouchedStartTime && uiState.startTime.isAfter(uiState.endTime),
                  invalidTimeMsg = "Start time should be smaller than end time",
                  tag = CreateMeetingScreenTestTags.INPUT_MEETING_START_TIME,
                  onTimeSelected = { createMeetingViewModel.setStartTime(it) })
          TimeInputField(config = starTimeConfig)

          Spacer(Modifier.height(8.dp))

          val endTimeConfig =
              TimeInputFieldConfig(
                  selectedTime = uiState.endTime,
                  label = "End Time",
                  placeHolder = "Select end time",
                  onFieldTouched = { createMeetingViewModel.touchEndTime() },
                  isInvalid =
                      uiState.hasTouchedEndTime && uiState.endTime.isBefore(uiState.startTime),
                  invalidTimeMsg = "End time should be greater than start time",
                  tag = CreateMeetingScreenTestTags.INPUT_MEETING_END_TIME,
                  onTimeSelected = { createMeetingViewModel.setEndTime(it) })
          TimeInputField(config = endTimeConfig)

          Spacer(Modifier.height(8.dp))

          Button(
              onClick = { createMeetingViewModel.createMeeting(projectId) },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON),
              enabled = uiState.isValid) {
                Text("Save")
              }
        }
      })
}

/**
 * Composable that displays a text field to select a date.
 *
 * @param selectedDate The already selected date to display in the text field.
 * @param label The label of the text field.
 * @param placeHolder The placeholder of the text field.
 * @param tag The test tag for the text field.
 * @param onDateSelected Function executed when the date has been picked.
 */
@Composable
fun DateInputField(
    selectedDate: LocalDate,
    label: String,
    placeHolder: String,
    tag: String,
    onDateSelected: (LocalDate) -> Unit
) {
  var showDialog by remember { mutableStateOf(false) }

  val datePickerState =
      rememberDatePickerState(
          initialSelectedDateMillis =
              selectedDate.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli())

  val displayText = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))

  OutlinedTextField(
      value = displayText,
      onValueChange = {},
      label = { Text(label) },
      placeholder = { Text(placeHolder) },
      readOnly = true,
      trailingIcon = {
        IconButton(onClick = { showDialog = true }, modifier = Modifier.testTag(tag)) {
          Icon(Icons.Default.DateRange, contentDescription = "Select date")
        }
      },
      modifier = Modifier.fillMaxWidth().clickable { showDialog = true })

  if (showDialog) {
    DatePickerDialog(
        onDismissRequest = { showDialog = false },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog = false
                datePickerState.selectedDateMillis?.let { millis ->
                  val newDate =
                      Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                  onDateSelected(newDate)
                }
              }) {
                Text("OK")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }) {
          DatePicker(
              state = datePickerState, modifier = Modifier.verticalScroll(rememberScrollState()))
        }
  }
}

/**
 * Data class representing the configuration of a [TimeInputField] composable.
 *
 * @param selectedTime The already selected time to display in the text field.
 * @param label The label of the text field.
 * @param placeHolder The placeholder of the text field.
 * @param onFieldTouched Function executed when the text field is touched (focused).
 * @param isInvalid Marker that is true if the time selected is invalid, false otherwise.
 * @param invalidTimeMsg Message to display in case the selected time is invalid.
 * @param tag The test tag for the text field.
 * @param onTimeSelected Function executed when the time has been picked.
 */
data class TimeInputFieldConfig(
    val selectedTime: LocalTime,
    val label: String,
    val placeHolder: String,
    val onFieldTouched: () -> Unit,
    val isInvalid: Boolean,
    val invalidTimeMsg: String,
    val tag: String,
    val onTimeSelected: (LocalTime) -> Unit
)

/**
 * Composable that displays a text field to select a time.
 *
 * @param config The time input field config.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputField(config: TimeInputFieldConfig) {
  var showDialog by remember { mutableStateOf(false) }

  val initialHour = config.selectedTime.hour
  val initialMinute = config.selectedTime.minute
  val timePickerState =
      rememberTimePickerState(
          initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)

  val displayText = config.selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))

  OutlinedTextField(
      value = displayText,
      onValueChange = {},
      label = { Text(config.label) },
      placeholder = { Text(config.placeHolder) },
      readOnly = true,
      trailingIcon = {
        IconButton(onClick = { showDialog = true }, modifier = Modifier.testTag(config.tag)) {
          Icon(Icons.Default.AccessTime, contentDescription = "Select time")
        }
      },
      modifier =
          Modifier.fillMaxWidth()
              .clickable { showDialog = true }
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  config.onFieldTouched()
                }
              })
  if (config.isInvalid) {
    Text(
        text = config.invalidTimeMsg,
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CreateMeetingScreenTestTags.ERROR_MSG))
  }

  if (showDialog) {
    TimePickerDialog(
        title = { Text("Select time") },
        onDismissRequest = { showDialog = false },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog = false
                config.onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
              }) {
                Text("OK")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }) {
          TimePicker(
              state = timePickerState,
          )
        }
  }
}
