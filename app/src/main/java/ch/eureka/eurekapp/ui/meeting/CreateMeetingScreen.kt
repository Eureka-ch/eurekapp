/*
Portions of the code in this file were written with the help of chatGPT, Gemini and Grok.
Portions of the code in this file are copy-pasted from the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
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
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.utils.MeetingPlatform
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
  const val INPUT_MEETING_LINK = "InputMeetingLink"
  const val MEETING_LINK_WARNING = "MeetingLinkWarning"
  const val PLATFORM_ICON = "PlatformIcon"
  const val CREATE_MEETING_BUTTON = "CreateMeetingButton"
  const val INPUT_MEETING_LOCATION = "InputMeetingLocation"
  const val LOCATION_SUGGESTION = "LocationSuggestion"
  const val PICK_LOCATION = "PickLocation"
  const val BACK_BUTTON = "BackButton"
}

/** Spacing between each component or subcomponent on the screen. */
const val SPACING = 8

/**
 * Data class holding all the action callbacks for the Create Meeting screen. This reduces the
 * number of parameters passed to the content composable.
 *
 * @property onTitleChange Callback when the title text changes.
 * @property onTitleTouch Callback when the title field is focused/touched.
 * @property onDateSelected Callback when a date is selected.
 * @property onDateTouched Callback when the date field is clicked.
 * @property onTimeSelected Callback when a time is selected.
 * @property onTimeTouched Callback when the time field is clicked.
 * @property onDurationSelected Callback when a duration is selected.
 * @property onFormatSelected Callback when a meeting format is selected.
 * @property onLinkChange Callback when the meeting link text changes.
 * @property onLinkTouch Callback when the link field is focused/touched.
 * @property onLocationQueryChange Callback when the location search query changes.
 * @property onLocationSelected Callback when a specific location is selected from suggestions.
 * @property onPickLocationOnMap Callback when the user clicks the map icon to pick a location.
 * @property onSave Callback when the save button is clicked.
 */
data class CreateMeetingActions(
    val onTitleChange: (String) -> Unit,
    val onTitleTouch: () -> Unit,
    val onDateSelected: (LocalDate) -> Unit,
    val onDateTouched: () -> Unit,
    val onTimeSelected: (LocalTime) -> Unit,
    val onTimeTouched: () -> Unit,
    val onDurationSelected: (Int) -> Unit,
    val onFormatSelected: (MeetingFormat) -> Unit,
    val onLinkChange: (String) -> Unit,
    val onLinkTouch: () -> Unit,
    val onLocationQueryChange: (String) -> Unit,
    val onLocationSelected: (Location) -> Unit,
    val onPickLocationOnMap: () -> Unit,
    val onSave: () -> Unit
)

/**
 * Composable that displays the create meeting proposal screen.
 *
 * @param projectId The ID of the project on which to create meetings for.
 * @param onDone Function called when meeting proposal was correctly created and saved on the
 *   database.
 * @param onPickLocationOnMap Function called when the user wants to select a location on the map.
 * @param onBackClick Function called when the back button is clicked.
 * @param createMeetingViewModel View model associated with create meeting screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateMeetingScreen(
    projectId: String,
    onDone: () -> Unit,
    onPickLocationOnMap: () -> Unit = {},
    onBackClick: () -> Unit = {},
    createMeetingViewModel: CreateMeetingViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by createMeetingViewModel.uiState.collectAsState()

  val isConnected by createMeetingViewModel.isConnected.collectAsState()

  // Navigate back if connection is lost
  LaunchedEffect(isConnected) {
    if (!isConnected) {
      Toast.makeText(context, "Connection lost. Returning to previous screen.", Toast.LENGTH_SHORT)
          .show()
      onBackClick()
    }
  }

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

  val actions =
      CreateMeetingActions(
          onTitleChange = createMeetingViewModel::setTitle,
          onTitleTouch = createMeetingViewModel::touchTitle,
          onDateSelected = createMeetingViewModel::setDate,
          onDateTouched = createMeetingViewModel::touchDate,
          onTimeSelected = createMeetingViewModel::setTime,
          onTimeTouched = createMeetingViewModel::touchTime,
          onDurationSelected = createMeetingViewModel::setDuration,
          onFormatSelected = createMeetingViewModel::setFormat,
          onLinkChange = createMeetingViewModel::setMeetingLink,
          onLinkTouch = createMeetingViewModel::touchLink,
          onLocationQueryChange = createMeetingViewModel::setLocationQuery,
          onLocationSelected = createMeetingViewModel::setLocation,
          onPickLocationOnMap = onPickLocationOnMap,
          onSave = { createMeetingViewModel.createMeeting(projectId) })

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Create Meeting",
            navigationIcon = {
              BackButton(
                  onClick = onBackClick,
                  modifier = Modifier.testTag(CreateMeetingScreenTestTags.BACK_BUTTON))
            })
      },
      content = { padding ->
        CreateMeetingContent(
            modifier = Modifier.padding(padding), uiState = uiState, actions = actions)
      })
}

/**
 * Pure UI Composable that defines the layout of the screen. It relies on [CreateMeetingUIState] for
 * data and [CreateMeetingActions] for events.
 *
 * @param modifier The modifier to apply to this layout.
 * @param uiState The current state of the UI.
 * @param actions The collection of callbacks for user interactions.
 */
@Composable
fun CreateMeetingContent(
    modifier: Modifier = Modifier,
    uiState: CreateMeetingUIState,
    actions: CreateMeetingActions
) {
  LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(10.dp)) {
    item { CreateMeetingHeader() }

    item { Spacer(Modifier.height((2 * SPACING).dp)) }

    item {
      TitleInputSection(
          title = uiState.title,
          hasTouchedTitle = uiState.hasTouchedTitle,
          onTitleChange = actions.onTitleChange,
          onTitleTouch = actions.onTitleTouch)
    }

    item { Spacer(Modifier.height(SPACING.dp)) }

    item {
      DateInputField(
          selectedDate = uiState.date,
          label = "Date",
          placeHolder = "Select date",
          tag = CreateMeetingScreenTestTags.INPUT_MEETING_DATE,
          onDateSelected = actions.onDateSelected,
          onDateTouched = actions.onDateTouched)
    }

    item { Spacer(Modifier.height(SPACING.dp)) }

    item {
      TimeInputField(
          selectedTime = uiState.time,
          label = "Time",
          placeHolder = "Select time",
          tag = CreateMeetingScreenTestTags.INPUT_MEETING_TIME,
          onTimeSelected = actions.onTimeSelected,
          onTimeTouched = actions.onTimeTouched)
    }

    item { Spacer(Modifier.height(SPACING.dp)) }

    item {
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
                  onOptionSelected = actions.onDurationSelected))
    }

    item { Spacer(Modifier.height(SPACING.dp)) }

    item {
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
                  onOptionSelected = actions.onFormatSelected))
    }

    item {
      MeetingLinkInputSection(
          format = uiState.format,
          linkState =
              MeetingLinkState(
                  meetingLink = uiState.meetingLink,
                  linkValidationError = uiState.linkValidationError,
                  linkValidationWarning = uiState.linkValidationWarning,
                  hasTouchedLink = uiState.hasTouchedLink,
                  detectedPlatform = uiState.detectedPlatform),
          onLinkChange = actions.onLinkChange,
          onLinkTouch = actions.onLinkTouch)
    }

    item {
      LocationInputSection(
          format = uiState.format,
          locationQuery = uiState.locationQuery,
          locationSuggestions = uiState.locationSuggestions,
          onLocationQueryChange = actions.onLocationQueryChange,
          onLocationSelected = actions.onLocationSelected,
          onPickLocationOnMap = actions.onPickLocationOnMap)
    }

    item { Spacer(Modifier.height(SPACING.dp)) }

    item {
      TimeValidationMessage(
          date = uiState.date,
          time = uiState.time,
          hasTouchedDate = uiState.hasTouchedDate,
          hasTouchedTime = uiState.hasTouchedTime)
    }

    item {
      Button(
          onClick = actions.onSave,
          modifier =
              Modifier.fillMaxWidth().testTag(CreateMeetingScreenTestTags.CREATE_MEETING_BUTTON),
          enabled = uiState.isValid) {
            Text("Save")
          }
    }
  }
}

/** Composable for the Header of the Create Meeting screen (Title and Description). */
@Composable
fun CreateMeetingHeader() {
  Text(
      modifier = Modifier.testTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_TITLE),
      text = "Create Meeting",
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold)
  Spacer(modifier = Modifier.height(SPACING.dp))
  Text(
      modifier = Modifier.testTag(CreateMeetingScreenTestTags.CREATE_MEETING_SCREEN_DESCRIPTION),
      text = "Create a team meeting proposal",
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Gray)
}

/**
 * Composable for the Meeting Title input field. Handles displaying the error message if the title
 * is invalid.
 *
 * @param title The current title value.
 * @param hasTouchedTitle Whether the user has interacted with this field.
 * @param onTitleChange Callback when the title text changes.
 * @param onTitleTouch Callback when the field gains focus.
 */
@Composable
fun TitleInputSection(
    title: String,
    hasTouchedTitle: Boolean,
    onTitleChange: (String) -> Unit,
    onTitleTouch: () -> Unit
) {
  OutlinedTextField(
      value = title,
      onValueChange = onTitleChange,
      label = { Text("Title") },
      placeholder = { Text("Title of the meeting") },
      modifier =
          Modifier.fillMaxWidth()
              .testTag(CreateMeetingScreenTestTags.INPUT_MEETING_TITLE)
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onTitleTouch()
                }
              })
  if (title.isBlank() && hasTouchedTitle) {
    Text(
        text = "Title cannot be empty",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CreateMeetingScreenTestTags.ERROR_MSG))
  }
}

/**
 * Composable that conditionally displays the location input field if the format is IN_PERSON.
 *
 * @param format The currently selected meeting format.
 * @param locationQuery The current text in the search bar.
 * @param locationSuggestions List of autocomplete suggestions.
 * @param onLocationQueryChange Callback when search text changes.
 * @param onLocationSelected Callback when a location is selected from the list.
 * @param onPickLocationOnMap Callback when the map icon is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationInputSection(
    format: MeetingFormat,
    locationQuery: String,
    locationSuggestions: List<Location>,
    onLocationQueryChange: (String) -> Unit,
    onLocationSelected: (Location) -> Unit,
    onPickLocationOnMap: () -> Unit
) {
  if (format == MeetingFormat.IN_PERSON) {
    Spacer(Modifier.height(SPACING.dp))
    LocationInputField(
        locationQuery = locationQuery,
        locationSuggestions = locationSuggestions,
        selectLocationQuery = onLocationQueryChange,
        selectLocation = onLocationSelected,
        onPickLocationOnMap = onPickLocationOnMap)
  }
}

/**
 * Data class to hold meeting link input state.
 *
 * @param meetingLink The current meeting link URL.
 * @param linkValidationError The validation error message, null if valid.
 * @param linkValidationWarning The validation warning message, null if no warning.
 * @param hasTouchedLink Whether the user has interacted with this field.
 * @param detectedPlatform The detected meeting platform from the link.
 */
data class MeetingLinkState(
    val meetingLink: String?,
    val linkValidationError: String?,
    val linkValidationWarning: String?,
    val hasTouchedLink: Boolean,
    val detectedPlatform: MeetingPlatform
)

/**
 * Composable that conditionally displays the meeting link input field if the format is VIRTUAL.
 *
 * @param format The currently selected meeting format.
 * @param linkState The state containing link data and validation.
 * @param onLinkChange Callback when the link text changes.
 * @param onLinkTouch Callback when the field gains focus.
 */
@Composable
fun MeetingLinkInputSection(
    format: MeetingFormat,
    linkState: MeetingLinkState,
    onLinkChange: (String) -> Unit,
    onLinkTouch: () -> Unit
) {
  if (format != MeetingFormat.VIRTUAL) return

  val showError = linkState.linkValidationError != null && linkState.hasTouchedLink
  val showPlatform =
      linkState.detectedPlatform != MeetingPlatform.UNKNOWN &&
          !linkState.meetingLink.isNullOrBlank()

  Spacer(Modifier.height(SPACING.dp))

  OutlinedTextField(
      value = linkState.meetingLink ?: "",
      onValueChange = onLinkChange,
      label = { Text("Meeting Link") },
      placeholder = { Text("https://zoom.us/j/...") },
      leadingIcon = {
        Icon(
            imageVector =
                when (linkState.detectedPlatform) {
                  MeetingPlatform.ZOOM,
                  MeetingPlatform.GOOGLE_MEET,
                  MeetingPlatform.MICROSOFT_TEAMS,
                  MeetingPlatform.WEBEX -> Icons.Default.VideoCall
                  MeetingPlatform.UNKNOWN -> Icons.Default.Link
                },
            contentDescription = "Meeting link icon",
            modifier = Modifier.testTag(CreateMeetingScreenTestTags.PLATFORM_ICON))
      },
      isError = showError,
      modifier =
          Modifier.fillMaxWidth()
              .testTag(CreateMeetingScreenTestTags.INPUT_MEETING_LINK)
              .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                  onLinkTouch()
                }
              })

  // Show error message if validation failed
  linkState.linkValidationError?.let { errorMessage ->
    if (linkState.hasTouchedLink) {
      Text(
          text = errorMessage,
          color = Color.Red,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(CreateMeetingScreenTestTags.ERROR_MSG))
    }
  }

  // Show warning message for non-whitelisted domains
  linkState.linkValidationWarning?.let { warningMessage ->
    if (linkState.hasTouchedLink && linkState.linkValidationError == null) {
      Text(
          text = warningMessage,
          color = MaterialTheme.colorScheme.secondary,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.testTag(CreateMeetingScreenTestTags.MEETING_LINK_WARNING))
    }
  }

  // Show detected platform name
  if (showPlatform) {
    Spacer(Modifier.height(4.dp))
    Text(
        text = "Platform: ${linkState.detectedPlatform.displayName}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary)
  }
}

/**
 * Composable that displays a validation error message if the selected time is in the past.
 *
 * @param date The selected date.
 * @param time The selected time.
 * @param hasTouchedDate Whether the date field has been touched.
 * @param hasTouchedTime Whether the time field has been touched.
 */
@Composable
fun TimeValidationMessage(
    date: LocalDate,
    time: LocalTime,
    hasTouchedDate: Boolean,
    hasTouchedTime: Boolean
) {
  if (hasTouchedDate &&
      hasTouchedTime &&
      LocalDateTime.of(date, time).isBefore(LocalDateTime.now())) {
    Text(
        text = "Meeting should be scheduled in the future.",
        color = Color.Red,
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.testTag(CreateMeetingScreenTestTags.ERROR_MSG))
  }
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
                Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
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
