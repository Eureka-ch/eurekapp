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
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.map.Location
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.FlowPreview

/** Test tags for the create meeting screen. */
object CreateMeetingScreenTestTags {
  const val CREATE_MEETING_SCREEN_TITLE = "CreateMeetingScreenTitle"
  const val CREATE_MEETING_SCREEN_DESCRIPTION = "CreateMeetingScreenDescription"
  const val ERROR_MSG = "ErrorMsg"
  const val INPUT_MEETING_TITLE = "InputMeetingTitle"
  const val INPUT_MEETING_DATE = "InputMeetingDate"
  const val INPUT_MEETING_TIME = "InputMeetingTime"
  const val INPUT_MEETING_DURATION = "InputMeetingDuration"
  const val INPUT_FORMAT = "InputFormat"
  const val CREATE_MEETING_BUTTON = "CreateMeetingButton"
  const val INPUT_MEETING_LOCATION = "InputMeetingLocation"
  const val LOCATION_SUGGESTION = "LocationSuggestion"
  const val PICK_LOCATION = "PickLocation"
}

/** Spacing between each component or subcomponent on the screen. */
const val SPACING = 8

/**
 * Composable that displays the create meeting proposal screen.
 *
 * @param projectId The ID of the project on which to create meetings for.
 * @param onDone Function called when meeting proposal was correctly created and saved on the
 *   database.
 * @param createMeetingViewModel View model associated with create meeting screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
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
          Spacer(modifier = Modifier.height(SPACING.dp))
          Text(
              modifier =
                  Modifier.testTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_DESCRIPTION),
              text = "Create a team meeting proposal",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.Gray)

          Spacer(Modifier.height((2 * SPACING).dp))

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

          Spacer(Modifier.height(SPACING.dp))

          DateInputField(
              selectedDate = uiState.date,
              label = "Date",
              placeHolder = "Select date",
              tag = CreateMeetingScreenTestTags.INPUT_MEETING_DATE,
              onDateSelected = { createMeetingViewModel.setDate(it) },
              onDateTouched = { createMeetingViewModel.touchDate() })

          Spacer(Modifier.height(SPACING.dp))

          TimeInputField(
              selectedTime = uiState.time,
              label = "Time",
              placeHolder = "Select time",
              tag = CreateMeetingScreenTestTags.INPUT_MEETING_TIME,
              onTimeSelected = { createMeetingViewModel.setTime(it) },
              onTimeTouched = { createMeetingViewModel.touchTime() })

          Spacer(Modifier.height(SPACING.dp))

          SingleChoiceInputField(
              config =
                  SingleChoiceInputFieldConfig(
                      currentValue = uiState.duration,
                      displayValue = { d -> "$d minutes" },
                      label = "Duration",
                      placeholder = "Select duration",
                      icon = Icons.Default.HourglassTop,
                      iconDescription = "Select duration",
                      alertDialogTitle = "Select a duration",
                      options = listOf(5, 10, 15, 20, 30, 45, 60),
                      tag = CreateMeetingScreenTestTags.INPUT_MEETING_DURATION,
                      onOptionSelected = { createMeetingViewModel.setDuration(it) }))

          Spacer(Modifier.height(SPACING.dp))

          SingleChoiceInputField(
              config =
                  SingleChoiceInputFieldConfig(
                      currentValue = uiState.format,
                      displayValue = { f -> f.description },
                      label = "Format",
                      placeholder = "Select format",
                      icon = Icons.Default.Description,
                      iconDescription = "Select format",
                      alertDialogTitle = "Select a format",
                      options = listOf(MeetingFormat.IN_PERSON, MeetingFormat.VIRTUAL),
                      tag = CreateMeetingScreenTestTags.INPUT_FORMAT,
                      onOptionSelected = { createMeetingViewModel.setFormat(it) }))

          if (uiState.format == MeetingFormat.IN_PERSON) {
            Spacer(Modifier.height(SPACING.dp))

            LocationInputField(
                locationQuery = uiState.locationQuery,
                locationSuggestions = uiState.locationSuggestions,
                selectLocationQuery = { createMeetingViewModel.setLocationQuery(it) },
                selectLocation = { createMeetingViewModel.setLocation(it) },
                onPickLocationOnMap = {})
          }

          Spacer(Modifier.height(SPACING.dp))

          if (uiState.hasTouchedDate &&
              uiState.hasTouchedTime &&
              LocalDateTime.of(uiState.date, uiState.time).isBefore(LocalDateTime.now())) {
            Text(
                text = "Meeting should be scheduled in the future.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.testTag(CreateMeetingScreenTestTags.ERROR_MSG))
          }
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
 * @param onDateTouched Function executed when the text field has been touched.
 */
@Composable
fun DateInputField(
    selectedDate: LocalDate,
    label: String,
    placeHolder: String,
    tag: String,
    onDateSelected: (LocalDate) -> Unit,
    onDateTouched: () -> Unit
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
        IconButton(
            onClick = {
              showDialog = true
              onDateTouched()
            },
            modifier = Modifier.testTag(tag)) {
              Icon(Icons.Default.DateRange, contentDescription = "Select date")
            }
      },
      modifier =
          Modifier.fillMaxWidth().clickable {
            showDialog = true
            onDateTouched()
          })

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
 * @param onTimeTouched Function executed when the text field has been touched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeInputField(
    selectedTime: LocalTime,
    label: String,
    placeHolder: String,
    tag: String,
    onTimeSelected: (LocalTime) -> Unit,
    onTimeTouched: () -> Unit
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
        IconButton(
            onClick = {
              showDialog = true
              onTimeTouched()
            },
            modifier = Modifier.testTag(tag)) {
              Icon(Icons.Default.AccessTime, contentDescription = placeHolder)
            }
      },
      modifier =
          Modifier.fillMaxWidth().clickable {
            showDialog = true
            onTimeTouched()
          })

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
 * Generic data class representing the config of a [SingleChoiceInputField] component.
 *
 * @param T The type of the value being selected.
 * @property currentValue The current value to display in the text field.
 * @property displayValue Function to display any value of type [T].
 * @property label The label of the text field.
 * @property placeholder The placeholder of the text field.
 * @property icon The icon to display in the text field.
 * @property iconDescription The description of [icon]
 * @property alertDialogTitle The title of the alert dialog.
 * @property options The options to choose one from.
 * @property tag The test tag to put on tha text field.
 * @property onOptionSelected Function executed when an option is selected.
 */
data class SingleChoiceInputFieldConfig<T>(
    val currentValue: T,
    val displayValue: (T) -> String,
    val label: String,
    val placeholder: String,
    val icon: ImageVector,
    val iconDescription: String,
    val alertDialogTitle: String,
    val options: List<T>,
    val tag: String,
    val onOptionSelected: (T) -> Unit,
)

/**
 * Composable to display a text field that when clicked opens a dialog with a radio button in it.
 *
 * @param T The type of the value being selected.
 * @param config The config for that composable.
 */
@Composable
fun <T> SingleChoiceInputField(config: SingleChoiceInputFieldConfig<T>) {

  var showDialog by remember { mutableStateOf(false) }
  var tempSelectedOption by remember { mutableStateOf(config.options.first()) }

  OutlinedTextField(
      value = config.displayValue(config.currentValue),
      onValueChange = {},
      label = { Text(config.label) },
      placeholder = { Text(config.placeholder) },
      readOnly = true,
      trailingIcon = {
        IconButton(onClick = { showDialog = true }, modifier = Modifier.testTag(config.tag)) {
          Icon(config.icon, contentDescription = config.iconDescription)
        }
      },
      modifier = Modifier.fillMaxWidth().clickable { showDialog = true })

  if (showDialog) {
    AlertDialog(
        onDismissRequest = { showDialog = false },
        title = { Text(config.alertDialogTitle) },
        text = {
          Column(Modifier.selectableGroup()) {
            config.options.forEach { option ->
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
                    Spacer(Modifier.width(SPACING.dp))
                    Text(config.displayValue(option))
                  }
            }
          }
        },
        confirmButton = {
          TextButton(
              onClick = {
                showDialog = false
                config.onOptionSelected(tempSelectedOption)
              }) {
                Text("OK")
              }
        },
        dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } })
  }
}

/**
 * Composable to display the location input text field where the user can either enter a location
 * and the location is searched by name or choose to pick the location directly by marking it on the
 * map.
 *
 * @param locationQuery The location inputted/selected byt the user in the text field.
 * @param locationSuggestions Suggestions for the [locationQuery]
 * @param selectLocationQuery Function executed when the user select a location query (select
 *   location name).
 * @param selectLocation Function executed when the user select the corresponding location.
 * @param onPickLocationOnMap Function executed when the users clicks on hte button to pick the
 *   location on the map.
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun LocationInputField(
    locationQuery: String,
    locationSuggestions: List<Location>,
    selectLocationQuery: (String) -> Unit,
    selectLocation: (Location) -> Unit,
    onPickLocationOnMap: () -> Unit,
) {

  var showDropdown by remember { mutableStateOf(false) }

  ExposedDropdownMenuBox(
      expanded = showDropdown && locationSuggestions.isNotEmpty(),
      onExpandedChange = { showDropdown = it }) {
        OutlinedTextField(
            value = locationQuery,
            onValueChange = {
              selectLocationQuery(it)
              showDropdown = true
            },
            label = { Text("Location") },
            placeholder = { Text("Enter an Address or Location") },
            trailingIcon = {
              IconButton(
                  onClick = onPickLocationOnMap,
                  modifier = Modifier.testTag(CreateMeetingScreenTestTags.PICK_LOCATION)) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Pick location")
                  }
            },
            modifier =
                Modifier.menuAnchor()
                    .fillMaxWidth()
                    .testTag(CreateMeetingScreenTestTags.INPUT_MEETING_LOCATION),
            singleLine = true)

        ExposedDropdownMenu(
            expanded = showDropdown && locationSuggestions.isNotEmpty(),
            onDismissRequest = { showDropdown = false }) {
              locationSuggestions.take(3).forEach { location ->
                DropdownMenuItem(
                    text = {
                      Text(
                          text =
                              location.name.take(30) + if (location.name.length > 30) "..." else "",
                          maxLines = 1)
                    },
                    onClick = {
                      selectLocationQuery(location.name)
                      selectLocation(location)
                      showDropdown = false
                    },
                    modifier =
                        Modifier.padding(8.dp)
                            .testTag(CreateMeetingScreenTestTags.LOCATION_SUGGESTION))
              }

              if (locationSuggestions.size > 3) {
                DropdownMenuItem(
                    text = { Text("More...") }, onClick = {}, modifier = Modifier.padding(8.dp))
              }
            }
      }
}
