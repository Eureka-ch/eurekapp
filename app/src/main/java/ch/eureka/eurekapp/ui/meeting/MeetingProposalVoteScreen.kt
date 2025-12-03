/* Portions of this code were written with the help of Gemini and Grok */
package ch.eureka.eurekapp.ui.meeting

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingProposal
import ch.eureka.eurekapp.model.data.meeting.MeetingProposalVote
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.components.help.HelpContext
import ch.eureka.eurekapp.ui.components.help.ScreenWithHelp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.utils.Formatters

/** Test tags for the datetime vote screen. */
object MeetingProposalVoteScreenTestTags {
  const val MEETING_PROPOSALS_VOTE_SCREEN = "MeetingProposalsVoteScreen"
  const val MEETING_PROPOSALS_VOTE_SCREEN_TITLE = "MeetingProposalsVoteScreenTitle"
  const val MEETING_PROPOSALS_VOTE_SCREEN_DESCRIPTION = "MeetingProposalsVoteScreenDescription"
  const val MEETING_PROPOSALS_VOTE_CARD = "MeetingProposalsVoteCard"
  const val MEETING_PROPOSALS_VOTE_DATETIME = "MeetingProposalsVoteDateTime"
  const val MEETING_PROPOSALS_VOTE_BUTTON = "MeetingProposalsVoteButton"
  const val ADD_MEETING_PROPOSALS = "AddMeetingProposalsVote"
  const val CONFIRM_MEETING_PROPOSALS_VOTES = "ConfirmMeetingProposalsVotes"
  const val MEETING_FORMAT_POPUP_VALIDATE = "MeetingFormatPopupValidate"
  const val MEETING_FORMAT_POPUP_CANCEL = "MeetingFormatPopupCancel"
  const val IN_PERSON_OPTION = "InPersonOption"
  const val VIRTUAL_OPTION = "VirtualOption"
  const val IN_PERSON_BUTTON = "InPersonButton"
  const val VIRTUAL_BUTTON = "VirtualButton"
  const val BACK_BUTTON = "BackButton"
}

/**
 * Main composable to draw the screen that displays the meeting proposals vote for a meeting.
 *
 * @param projectId The ID of the project in which the meeting from which the proposals votes are
 *   displayed is in.
 * @param meetingId The ID of the meeting in for which the meeting proposals votes are displayed.
 * @param onDone Function executed when the user have want to validate their vote choices.
 * @param meetingProposalVoteViewModel The view model associated to that screen.
 */
@Composable
fun MeetingProposalVoteScreen(
    projectId: String,
    meetingId: String,
    onCreateDateTimeFormatProposalForMeeting: () -> Unit,
    onDone: () -> Unit,
    onBackClick: () -> Unit,
    meetingProposalVoteViewModel: MeetingProposalVoteViewModel = viewModel {
      MeetingProposalVoteViewModel(projectId, meetingId)
    },
) {

  val context = LocalContext.current
  val uiState by meetingProposalVoteViewModel.uiState.collectAsState()

  // Show error message if there is any
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      meetingProposalVoteViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(Unit) { meetingProposalVoteViewModel.loadMeetingProposals() }

  LaunchedEffect(uiState.votesSaved) {
    if (uiState.votesSaved) {
      onDone()
    }
  }

  LaunchedEffect(meetingProposalVoteViewModel.userId) {
    if (meetingProposalVoteViewModel.userId == null) {
      meetingProposalVoteViewModel.setErrorMsg("Not logged in")
    }
  }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Vote for Meeting Proposal",
            navigationIcon = {
              BackButton(
                  onClick = onBackClick,
                  modifier = Modifier.testTag(MeetingProposalVoteScreenTestTags.BACK_BUTTON))
            })
      },
      floatingActionButton = {
        Column(horizontalAlignment = Alignment.End) {
          FloatingActionButton(
              onClick = onCreateDateTimeFormatProposalForMeeting,
              modifier =
                  Modifier.testTag(MeetingProposalVoteScreenTestTags.ADD_MEETING_PROPOSALS)) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Propose new meeting proposal")
              }

          Spacer(modifier = Modifier.height(16.dp))

          FloatingActionButton(
              onClick = { meetingProposalVoteViewModel.confirmMeetingProposalsVotes() },
              modifier =
                  Modifier.testTag(
                      MeetingProposalVoteScreenTestTags.CONFIRM_MEETING_PROPOSALS_VOTES)) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm votes")
              }
        }
      },
      content = { padding ->
        ScreenWithHelp(
            helpContext = HelpContext.MEETING_VOTES,
            content = {
              Column(
                  modifier =
                      Modifier.fillMaxSize()
                          .padding(5.dp)
                          .testTag(
                              MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_SCREEN)) {
                    Text(
                        modifier =
                            Modifier.testTag(
                                MeetingProposalVoteScreenTestTags
                                    .MEETING_PROPOSALS_VOTE_SCREEN_TITLE),
                        text = "Vote for meeting proposal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        modifier =
                            Modifier.testTag(
                                MeetingProposalVoteScreenTestTags
                                    .MEETING_PROPOSALS_VOTE_SCREEN_DESCRIPTION),
                        text = "Vote for existing meeting proposal or propose your own",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray)

                    Spacer(Modifier.height(8.dp))

                    if (meetingProposalVoteViewModel.userId != null) {
                      MeetingProposalsList(
                          modifier = Modifier.padding(padding),
                          meetingProposals = uiState.meetingProposals,
                          hasVoted = { meetingProposal ->
                            meetingProposal.votes
                                .map { v -> v.userId }
                                .contains(meetingProposalVoteViewModel.userId)
                          },
                          hasVotedForFormat = { meetingProposal, format ->
                            meetingProposalVoteViewModel.hasVotedForFormat(meetingProposal, format)
                          },
                          voteActions =
                              MeetingProposalVoteActions(
                                  addVote = { meetingProposal, formats ->
                                    meetingProposalVoteViewModel.voteForMeetingProposal(
                                        meetingProposal,
                                        MeetingProposalVote(
                                            meetingProposalVoteViewModel.userId, formats.toList()))
                                  },
                                  removeVote = { meetingProposal ->
                                    meetingProposalVoteViewModel.retractVoteForMeetingProposal(
                                        meetingProposal)
                                  },
                                  addFormatVote = { meetingProposal, format ->
                                    meetingProposalVoteViewModel
                                        .addFormatVoteForAlreadyVotedMeetingProposal(
                                            meetingProposal, format)
                                  },
                                  retractFormatVote = { meetingProposal, format ->
                                    meetingProposalVoteViewModel
                                        .retractFormatVoteForAlreadyVotedMeetingProposal(
                                            meetingProposal, format)
                                  }))
                    }
                  }
            })
      })
}

/**
 * Data class tha represents the voting actions that can be executed in a [MeetingProposalVoteCard]
 * composable.
 *
 * @param addVote Function executed when the user add votes on a meeting proposal.
 * @param removeVote Function executed when the user retract a vote on a meeting proposal.
 * @param addFormatVote Function executed when the users vote for a specific format.
 * @param retractFormatVote Function executed when the user retract a vote form a specific format.
 */
data class MeetingProposalVoteActions(
    val addVote: (MeetingProposal, Set<MeetingFormat>) -> Unit,
    val removeVote: (MeetingProposal) -> Unit,
    val addFormatVote: (MeetingProposal, MeetingFormat) -> Unit,
    val retractFormatVote: (MeetingProposal, MeetingFormat) -> Unit
)

/**
 * Composable that displays the list of meeting proposals for a meeting.
 *
 * @param modifier Modifier for the composable
 * @param meetingProposals Meeting proposals to be displayed
 * @param hasVoted Function that returns true if the current user has already voted for a meeting
 *   proposal
 * @param hasVotedForFormat Function that returns true if the current user has already voted for a
 *   given format for a given meeting proposal.
 * @param voteActions Voting actions that can be executed by a user on a meeting proposal.
 */
@Composable
fun MeetingProposalsList(
    modifier: Modifier,
    meetingProposals: List<MeetingProposal>,
    hasVoted: (MeetingProposal) -> Boolean,
    hasVotedForFormat: (MeetingProposal, MeetingFormat) -> Boolean,
    voteActions: MeetingProposalVoteActions,
) {
  if (meetingProposals.isNotEmpty()) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 4.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 1.dp)) {
          items(meetingProposals.size) { index ->
            MeetingProposalVoteCard(
                meetingProposal = meetingProposals[index],
                hasVoted = hasVoted(meetingProposals[index]),
                hasVotedForInPersonFormat =
                    hasVotedForFormat(meetingProposals[index], MeetingFormat.IN_PERSON),
                hasVotedForVirtualFormat =
                    hasVotedForFormat(meetingProposals[index], MeetingFormat.VIRTUAL),
                voteActions = voteActions,
            )
          }
        }
  }
}

/**
 * Displays the dialog for selecting meeting formats when voting.
 *
 * @param onDismissRequest Called when the user dismisses the dialog (e.g., clicks outside).
 * @param onConfirm Called when the user clicks "OK", passing the set of selected formats.
 */
@Composable
private fun MeetingFormatVoteDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Set<MeetingFormat>) -> Unit
) {
  var selectedFormats by remember { mutableStateOf(emptySet<MeetingFormat>()) }

  AlertDialog(
      onDismissRequest = onDismissRequest,
      title = { Text("Select format(s)") },
      text = {
        Column {
          FormatCheckboxRow(
              text = MeetingFormat.IN_PERSON.description,
              isSelected = selectedFormats.contains(MeetingFormat.IN_PERSON),
              onToggle = {
                selectedFormats =
                    if (selectedFormats.contains(MeetingFormat.IN_PERSON)) {
                      selectedFormats - MeetingFormat.IN_PERSON
                    } else {
                      selectedFormats + MeetingFormat.IN_PERSON
                    }
              },
              tag = MeetingProposalVoteScreenTestTags.IN_PERSON_OPTION)

          FormatCheckboxRow(
              text = MeetingFormat.VIRTUAL.description,
              isSelected = selectedFormats.contains(MeetingFormat.VIRTUAL),
              onToggle = {
                selectedFormats =
                    if (selectedFormats.contains(MeetingFormat.VIRTUAL)) {
                      selectedFormats - MeetingFormat.VIRTUAL
                    } else {
                      selectedFormats + MeetingFormat.VIRTUAL
                    }
              },
              tag = MeetingProposalVoteScreenTestTags.VIRTUAL_OPTION)
        }
      },
      confirmButton = {
        TextButton(
            modifier =
                Modifier.testTag(MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_VALIDATE),
            onClick = { onConfirm(selectedFormats) },
            enabled = selectedFormats.isNotEmpty()) {
              Text("OK")
            }
      },
      dismissButton = {
        TextButton(
            modifier =
                Modifier.testTag(MeetingProposalVoteScreenTestTags.MEETING_FORMAT_POPUP_CANCEL),
            onClick = { onDismissRequest() }) {
              Text("Cancel")
            }
      })
}

/**
 * Composable that displays a vote for a certain meeting proposal and allows a user to vote for it.
 *
 * @param meetingProposal The meeting proposal vote to display and potentially vote for.
 * @param hasVoted True if the meeting proposal was voted by the current user, false otherwise.
 * @param hasVotedForInPersonFormat True if the user voted for in-person format for the voting
 *   proposal, false otherwise.
 * @param hasVotedForVirtualFormat True if the user voted for virtual format for the voting
 *   proposal, false otherwise.
 * @param voteActions Actions that can be executed by a user on a meeting proposal.
 *
 * Note : this composable was written with the help of Gemini
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingProposalVoteCard(
    meetingProposal: MeetingProposal,
    hasVoted: Boolean,
    hasVotedForInPersonFormat: Boolean,
    hasVotedForVirtualFormat: Boolean,
    voteActions: MeetingProposalVoteActions
) {

  var showFormatDialog by remember { mutableStateOf(false) }

  if (showFormatDialog) {
    MeetingFormatVoteDialog(
        onDismissRequest = { showFormatDialog = false },
        onConfirm = { formats ->
          voteActions.addVote(meetingProposal, formats)
          showFormatDialog = false
        })
  }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(2.dp)
              .wrapContentHeight()
              .testTag(MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                  text = Formatters.formatDateTime(meetingProposal.dateTime.toDate()),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  modifier =
                      Modifier.testTag(
                          MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_DATETIME))

              if (hasVoted) {

                VotingScoreButton(
                    hasVoted = hasVotedForInPersonFormat,
                    addVote = {
                      voteActions.addFormatVote(meetingProposal, MeetingFormat.IN_PERSON)
                    },
                    retractVote = {
                      voteActions.retractFormatVote(meetingProposal, MeetingFormat.IN_PERSON)
                    },
                    numberOfVotes =
                        meetingProposal.votes
                            .filter { it.formatPreferences.contains(MeetingFormat.IN_PERSON) }
                            .size,
                    icon = Icons.Default.LocationOn,
                    iconDescription = "In-person votes",
                    tag = MeetingProposalVoteScreenTestTags.IN_PERSON_BUTTON)

                VotingScoreButton(
                    hasVoted = hasVotedForVirtualFormat,
                    addVote = { voteActions.addFormatVote(meetingProposal, MeetingFormat.VIRTUAL) },
                    retractVote = {
                      voteActions.retractFormatVote(meetingProposal, MeetingFormat.VIRTUAL)
                    },
                    numberOfVotes =
                        meetingProposal.votes
                            .filter { it.formatPreferences.contains(MeetingFormat.VIRTUAL) }
                            .size,
                    icon = Icons.Default.VideoCall,
                    iconDescription = "Virtual votes",
                    tag = MeetingProposalVoteScreenTestTags.VIRTUAL_BUTTON)
              }

              VotingScoreButton(
                  hasVoted = hasVoted,
                  addVote = {
                    // selectedFormats = emptySet()
                    showFormatDialog = true
                  },
                  retractVote = { voteActions.removeVote(meetingProposal) },
                  numberOfVotes = meetingProposal.votes.size,
                  icon = Icons.Default.People,
                  iconDescription = "Total votes",
                  tag = MeetingProposalVoteScreenTestTags.MEETING_PROPOSALS_VOTE_BUTTON,
              )
            }
      }
}

/**
 * Composable to display a voting button with votes count.
 *
 * @param hasVoted True if the current have voted, false otherwise.
 * @param addVote Function executed when the user adds a vote.
 * @param retractVote Function executed when the user retracts a vote.
 * @param numberOfVotes The number of votes.
 * @param icon The icon to put inside the vote button.
 * @param iconDescription The description of the [icon]
 * @param tag The test tag for th button.
 */
@Composable
fun VotingScoreButton(
    hasVoted: Boolean,
    addVote: () -> Unit,
    retractVote: () -> Unit,
    numberOfVotes: Int,
    icon: ImageVector,
    iconDescription: String,
    tag: String
) {
  AssistChip(
      modifier = Modifier.testTag(tag),
      onClick = {
        if (hasVoted) {
          retractVote()
        } else {
          addVote()
        }
      },
      label = { Text(text = "$numberOfVotes") },
      leadingIcon = { Icon(imageVector = icon, contentDescription = iconDescription) },
      colors =
          if (hasVoted) {
            AssistChipDefaults.assistChipColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                leadingIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                labelColor = MaterialTheme.colorScheme.onPrimaryContainer)
          } else {
            AssistChipDefaults.assistChipColors()
          })
}

/**
 * Composable to display check box in a row.
 *
 * @param text Text to display next to the check box.
 * @param isSelected true if the checkbox is checked false otherwise.
 * @param onToggle Function executed when the checkbox is checked/unchecked.
 * @param tag Test tag to put on checkbox's text.
 */
@Composable
fun FormatCheckboxRow(text: String, isSelected: Boolean, onToggle: () -> Unit, tag: String) {
  Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier =
          Modifier.fillMaxWidth()
              .toggleable(value = isSelected, onValueChange = { onToggle() }, role = Role.Checkbox)
              .padding(vertical = 4.dp)
              .testTag(tag)) {
        Checkbox(checked = isSelected, onCheckedChange = null)
        Text(text = text, modifier = Modifier.padding(start = 8.dp))
      }
}
