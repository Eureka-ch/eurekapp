/*
Portions of the code in this file were written with the help of chatGPT, Gemini, and Grok.
Portions of the code in this file are copy-pasted from the Bootcamp solution B3 provided by the SwEnt staff.
*/

package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar

/** Test tags for the create meeting proposal for datetime/format screen. */
object CreateDateTimeFormatMeetingProposalScreenTestTags {
  const val CREATE_MEETING_PROPOSAL_SCREEN_TITLE = "CreateMeetingProposalScreenTitle"
  const val CREATE_MEETING_PROPOSAL_SCREEN_DESCRIPTION = "CreateMeetingProposalScreenDescription"
  const val ERROR_MSG = "ErrorMsg"
  const val INPUT_MEETING_DATE = "InputMeetingDate"
  const val INPUT_MEETING_TIME = "InputMeetingTime"
  const val INPUT_FORMAT = "InputFormat"
  const val CREATE_MEETING_PROPOSAL_BUTTON = "CreateMeetingProposalButton"
}

/**
 * Main composable for the screen that enables users to create a new meeting proposal that proposes
 * a new datetime and format for a meeting.
 *
 * @property projectId The project ID in which the meeting to add a proposal resides.
 * @property meetingId The meeting ID of the meeting to add the datetime/format proposal to.
 * @property onDone Function executed when the user have saved their meeting proposal.
 * @property onBackClick Function executed when the user wants to navigate back.
 * @property createMeetingProposalViewModel ViewModel associated to that screen.
 */
@Composable
fun CreateDateTimeFormatProposalForMeetingScreen(
    projectId: String,
    meetingId: String,
    onDone: () -> Unit,
    onBackClick: () -> Unit = {},
    createMeetingProposalViewModel: CreateDateTimeFormatProposalForMeetingViewModel = viewModel {
      CreateDateTimeFormatProposalForMeetingViewModel(projectId, meetingId)
    }
) {

  val context = LocalContext.current
  val uiState by createMeetingProposalViewModel.uiState.collectAsState()

  val connectivityObserver = ConnectivityObserverProvider.connectivityObserver
  val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

  // Navigate back if connection is lost
  LaunchedEffect(isConnected) {
    if (!isConnected) {
      onBackClick()
    }
  }

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      createMeetingProposalViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(uiState.saved) {
    if (uiState.saved) {
      onDone()
    }
  }

  LaunchedEffect(Unit) { createMeetingProposalViewModel.loadMeeting() }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Create Meeting Proposal",
            navigationIcon = { BackButton(onClick = onBackClick) })
      },
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(10.dp)) {
          Text(
              modifier =
                  Modifier.testTag(
                      CreateDateTimeFormatMeetingProposalScreenTestTags
                          .CREATE_MEETING_PROPOSAL_SCREEN_TITLE),
              text = "Create Meeting Proposal",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(SPACING.dp))
          Text(
              modifier =
                  Modifier.testTag(
                      CreateDateTimeFormatMeetingProposalScreenTestTags
                          .CREATE_MEETING_PROPOSAL_SCREEN_DESCRIPTION),
              text = "Create a team meeting proposal for datetime/format of a meeting",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.Gray)

          Spacer(Modifier.height((2 * SPACING).dp))

          DateInputField(
              selectedDate = uiState.date,
              label = "Date",
              placeHolder = "Select date",
              tag = CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE,
              onDateSelected = { createMeetingProposalViewModel.setDate(it) },
              onDateTouched = { createMeetingProposalViewModel.touchDate() })

          Spacer(Modifier.height(SPACING.dp))

          TimeInputField(
              selectedTime = uiState.time,
              label = "Time",
              placeHolder = "Select time",
              tag = CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME,
              onTimeSelected = { createMeetingProposalViewModel.setTime(it) },
              onTimeTouched = { createMeetingProposalViewModel.touchTime() })

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
                      tag = CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_FORMAT,
                      onOptionSelected = { createMeetingProposalViewModel.setFormat(it) }))

          Spacer(Modifier.height(SPACING.dp))

          if (uiState.hasTouchedDate && uiState.hasTouchedTime && !uiState.isValid) {
            Text(
                text = "Meeting should be scheduled in the future.",
                color = Color.Red,
                style = MaterialTheme.typography.bodySmall,
                modifier =
                    Modifier.testTag(CreateDateTimeFormatMeetingProposalScreenTestTags.ERROR_MSG))
          }
          Button(
              onClick = { createMeetingProposalViewModel.createDateTimeFormatProposalForMeeting() },
              modifier =
                  Modifier.fillMaxWidth()
                      .testTag(
                          CreateDateTimeFormatMeetingProposalScreenTestTags
                              .CREATE_MEETING_PROPOSAL_BUTTON),
              enabled = uiState.isValid) {
                Text("Save")
              }
        }
      })
}
