/*
Portions of the code in this file were written with the help of chatGPT and Gemini.
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat

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
 * @property createMeetingProposalViewModel ViewModel associated to that screen.
 */
@Composable
fun CreateDateTimeFormatProposalForMeetingScreen(
    projectId: String,
    meetingId: String,
    onDone: () -> Unit,
    createMeetingProposalViewModel: CreateDateTimeFormatProposalForMeetingViewModel = viewModel {
      CreateDateTimeFormatProposalForMeetingViewModel(projectId, meetingId)
    }
) {

  val context = LocalContext.current
  val uiState by createMeetingProposalViewModel.uiState.collectAsState()

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
      content = { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(10.dp)) {
          Text(
              modifier =
                  Modifier.testTag(
                      CreateDateTimeFormatMeetingProposalScreenTestTags
                          .CREATE_MEETING_PROPOSAL_SCREEN_TITLE),
              text = stringResource(R.string.create_datetime_proposal_title),
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold)
          Spacer(modifier = Modifier.height(SPACING.dp))
          Text(
              modifier =
                  Modifier.testTag(
                      CreateDateTimeFormatMeetingProposalScreenTestTags
                          .CREATE_MEETING_PROPOSAL_SCREEN_DESCRIPTION),
              text = stringResource(R.string.create_datetime_proposal_description),
              style = MaterialTheme.typography.bodyMedium,
              color = Color.Gray)

          Spacer(Modifier.height((2 * SPACING).dp))

          DateInputField(
              selectedDate = uiState.date,
              label = stringResource(R.string.create_datetime_proposal_date_label),
              placeHolder = stringResource(R.string.create_datetime_proposal_date_placeholder),
              tag = CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_DATE,
              onDateSelected = { createMeetingProposalViewModel.setDate(it) },
              onDateTouched = { createMeetingProposalViewModel.touchDate() })

          Spacer(Modifier.height(SPACING.dp))

          TimeInputField(
              selectedTime = uiState.time,
              label = stringResource(R.string.create_datetime_proposal_time_label),
              placeHolder = stringResource(R.string.create_datetime_proposal_time_placeholder),
              tag = CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_MEETING_TIME,
              onTimeSelected = { createMeetingProposalViewModel.setTime(it) },
              onTimeTouched = { createMeetingProposalViewModel.touchTime() })

          Spacer(Modifier.height(SPACING.dp))

          SingleChoiceInputField(
              config =
                  SingleChoiceInputFieldConfig(
                      currentValue = uiState.format,
                      displayValue = { f -> f.description },
                      label = stringResource(R.string.create_datetime_proposal_format_label),
                      placeholder =
                          stringResource(R.string.create_datetime_proposal_format_placeholder),
                      icon = Icons.Default.Description,
                      iconDescription =
                          stringResource(R.string.create_datetime_proposal_format_placeholder),
                      alertDialogTitle =
                          stringResource(R.string.create_datetime_proposal_format_dialog_title),
                      options = listOf(MeetingFormat.IN_PERSON, MeetingFormat.VIRTUAL),
                      tag = CreateDateTimeFormatMeetingProposalScreenTestTags.INPUT_FORMAT,
                      onOptionSelected = { createMeetingProposalViewModel.setFormat(it) }))

          Spacer(Modifier.height(SPACING.dp))

          if (uiState.hasTouchedDate && uiState.hasTouchedTime && !uiState.isValid) {
            Text(
                text = stringResource(R.string.create_datetime_proposal_future_validation_error),
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
                Text(stringResource(R.string.create_datetime_proposal_save_button))
              }
        }
      })
}
