/*
Portions of the code in this file were written with the help of chatGPT and Gemini.
Portions of the code in this file are copy-pasted from the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
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
  const val INPUT_MEETING_TIME = "InputMeetingTime"
  const val INPUT_MEETING_DURATION = "InputMeetingDuration"
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

          // TODO : add duration input field

          Spacer(Modifier.height(8.dp))

          DateInputField(
              selectedDate = uiState.date,
              label = "Date",
              placeHolder = "Select date",
              tag = CreateMeetingScreenTestTags.INPUT_MEETING_DATE,
              onDateSelected = { createMeetingViewModel.setDate(it) })

          Spacer(Modifier.height(8.dp))

          TimeInputField(
              selectedTime = uiState.time,
              label = "Time",
              placeHolder = "Select time",
              tag = CreateMeetingScreenTestTags.INPUT_MEETING_TIME,
              onTimeSelected = { createMeetingViewModel.setTime(it) })

          Spacer(Modifier.height(8.dp))

          DurationInputField(
              duration = uiState.duration,
              label = "Duration",
              placeholder = "Select duration",
              durationOptions = listOf(5, 10, 15, 20, 30, 45, 60),
              tag = CreateMeetingScreenTestTags.INPUT_MEETING_DURATION,
              onDurationSelected = { createMeetingViewModel.setDuration(it) },
          )

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
 * Composable that displays a text field to select a time.
 *
 * @param selectedTime The already selected time to display in the text field.
 * @param label The label of the text field.
 * @param placeHolder The placeholder of the text field.
 * @param tag The test tag for the text field.
 * @param onTimeSelected Function executed when the time has been picked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputField(
    selectedTime: LocalTime,
    label: String,
    placeHolder: String,
    tag: String,
    onTimeSelected: (LocalTime) -> Unit
) {
  var showDialog by remember { mutableStateOf(false) }

  val initialHour = selectedTime.hour
  val initialMinute = selectedTime.minute
  val timePickerState =
      rememberTimePickerState(
          initialHour = initialHour, initialMinute = initialMinute, is24Hour = true)

  val displayText = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm"))

  OutlinedTextField(
      value = displayText,
      onValueChange = {},
      label = { Text(label) },
      placeholder = { Text(placeHolder) },
      readOnly = true,
      trailingIcon = {
        IconButton(onClick = { showDialog = true }, modifier = Modifier.testTag(tag)) {
          Icon(Icons.Default.AccessTime, contentDescription = "Select time")
        }
      },
      modifier = Modifier.fillMaxWidth().clickable { showDialog = true })

  if (showDialog) {
    TimePickerDialog(
        title = { Text("Select time") },
        onDismissRequest = { showDialog = false },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog = false
                onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
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

/**
 * Composable that displays a text field to select a duration.
 *
 * @param duration The already selected duration to display in the text field.
 * @param label The label of the text field.
 * @param placeholder The placeholder of the text field.
 * @param durationOptions List of durations available for the user to choose from.
 * @param tag The test tag for the text field.
 * @param onDurationSelected Function executed when the duration has been picked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DurationInputField(
    duration: Int,
    label: String,
    placeholder: String,
    durationOptions: List<Int>,
    tag: String,
    onDurationSelected: (Int) -> Unit,
) {
  var showDialog by remember { mutableStateOf(false) }
  var tempSelectedOption by remember { mutableIntStateOf(durationOptions.first()) }

  OutlinedTextField(
      value = "$duration minutes",
      onValueChange = {},
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      readOnly = true,
      trailingIcon = {
        IconButton(onClick = { showDialog = true }, modifier = Modifier.testTag(tag)) {
          Icon(Icons.Default.HourglassTop, contentDescription = "Select duration")
        }
      },
      modifier = Modifier.fillMaxWidth().clickable { showDialog = true })

  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text("Select Duration") },
        text = {
          Column(Modifier.selectableGroup()) {
            durationOptions.forEach { option ->
              Row(
                  Modifier.fillMaxWidth()
                      .selectable(
                          selected = tempSelectedOption == option,
                          onClick = { tempSelectedOption = option },
                          role = Role.RadioButton)
                      .padding(vertical = 12.dp),
                  verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = tempSelectedOption == option,
                        onClick = null // recommended practice: handle click on `Row`
                        )
                    Spacer(Modifier.width(8.dp))
                    Text("$option minutes")
                  }
            }
          }
        },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog = false
                onDurationSelected(tempSelectedOption)
              }) {
                Text("OK")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } })
  }
}
