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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.meeting.DateTimeVote
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.utils.Formatters

/** Test tags for the datetime vote screen. */
object DateTimeVoteScreenTestTags {
  const val DATETIME_VOTE_SCREEN = "DateTimeVoteScreen"
  const val DATETIME_VOTE_SCREEN_TITLE = "DateTimeVoteScreenTitle"
  const val DATETIME_VOTE_SCREEN_DESCRIPTION = "DateTimeVoteScreenDescription"
  const val DATETIME_VOTE_CARD = "DateTimeVoteCard"
  const val DATETIME_VOTE_DATETIME = "DateTimeVoteDateTime"
  const val DATETIME_VOTE_VOTE_BUTTON = "DateTimeVoteButton"
  const val ADD_DATETIME_VOTE = "AddDateTimeVote"
  const val CONFIRM_DATETIME_VOTES = "ConfirmDateTimeVotes"
}

/**
 * Main composable to draw the screen that displays the datetime vote for a meeting.
 *
 * @param projectId The ID of the project in which the meeting from which the datetime votes are
 *   displayed is in.
 * @param meetingId The ID of the meeting in for which the datetime votes are displayed.
 * @param onDone Function executed when the user have want to validate their vote choices.
 * @param dateTimeVoteViewModel The view model associated to that screen.
 */
@Composable
fun DateTimeVoteScreen(
    projectId: String,
    meetingId: String,
    onDone: () -> Unit,
    dateTimeVoteViewModel: DateTimeVoteViewModel = viewModel {
      DateTimeVoteViewModel(projectId, meetingId)
    },
) {

  val context = LocalContext.current
  val uiState by dateTimeVoteViewModel.uiState.collectAsState()

  // Show error message if there is any
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let {
      Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
      dateTimeVoteViewModel.clearErrorMsg()
    }
  }

  LaunchedEffect(Unit) { dateTimeVoteViewModel.loadDateTimeVotes() }

  LaunchedEffect(uiState.votesSaved) {
    if (uiState.votesSaved) {
      onDone()
    }
  }

  Scaffold(
      floatingActionButton = {
        Column(horizontalAlignment = Alignment.End) {
          FloatingActionButton(
              onClick = {},
              modifier = Modifier.testTag(DateTimeVoteScreenTestTags.ADD_DATETIME_VOTE)) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Propose datetime vote")
              }

          Spacer(modifier = Modifier.height(16.dp))

          FloatingActionButton(
              onClick = { dateTimeVoteViewModel.confirmDateTimeVotes() },
              modifier = Modifier.testTag(DateTimeVoteScreenTestTags.CONFIRM_DATETIME_VOTES)) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Confirm votes")
              }
        }
      },
      content = { padding ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(10.dp)
                    .testTag(DateTimeVoteScreenTestTags.DATETIME_VOTE_SCREEN)) {
              Text(
                  modifier =
                      Modifier.testTag(DateTimeVoteScreenTestTags.DATETIME_VOTE_SCREEN_TITLE),
                  text = "Vote for date datetime of meeting",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold)
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                  modifier =
                      Modifier.testTag(DateTimeVoteScreenTestTags.DATETIME_VOTE_SCREEN_DESCRIPTION),
                  text = "Vote for existing datetime or propose your own",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.Gray)

              Spacer(Modifier.height(8.dp))

              if (dateTimeVoteViewModel.userId == null) {
                dateTimeVoteViewModel.setErrorMsg("Not logged in")
              } else {
                DateTimeVoteList(
                    modifier = Modifier.padding(padding),
                    dateTimeVotes = uiState.dateTimeVotes,
                    hasVoted = { dateTimeVote ->
                      dateTimeVote.voters.contains(dateTimeVoteViewModel.userId)
                    },
                    addVote = { dateTimeVote ->
                      dateTimeVoteViewModel.voteForDateTime(dateTimeVote)
                    },
                    removeVote = { dateTimeVote ->
                      dateTimeVoteViewModel.retractVoteForDateTime(dateTimeVote)
                    },
                )
              }
            }
      })
}

/**
 * Composable that displays the list of datetime votes for a meeting.
 *
 * @param modifier Modifier for the composable
 * @param dateTimeVotes Datetime votes to be displayed
 * @param hasVoted Function that returns true if the current user has already voted for a datetime
 *   vote.
 * @param addVote Function executed when a user add a vote for a particular datetime.
 * @param removeVote Function executed when a user retract a vote for a particular datetime.
 */
@Composable
fun DateTimeVoteList(
    modifier: Modifier,
    dateTimeVotes: List<DateTimeVote>,
    hasVoted: (DateTimeVote) -> Boolean,
    addVote: (DateTimeVote) -> Unit,
    removeVote: (DateTimeVote) -> Unit,
) {
  if (dateTimeVotes.isNotEmpty()) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 8.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
          items(dateTimeVotes.size) { index ->
            DateTimeVoteCard(
                dateTimeVote = dateTimeVotes[index],
                hasVoted = hasVoted(dateTimeVotes[index]),
                addVote = addVote,
                removeVote = removeVote)
          }
        }
  }
}

/**
 * Composable that displays a vote for a certain datetime and allows a user to vote for it.
 *
 * @param dateTimeVote The datetime vote to display and potentially vote for.
 * @param hasVoted True if the datetime was voted byt the current user, false otherwise.
 * @param addVote Function executed when the user add votes on a datetime.
 * @param removeVote Function executed when the user retract a vote on a datetime.
 *
 * Note : this composable was written with the help of Gemini
 */
@Composable
fun DateTimeVoteCard(
    dateTimeVote: DateTimeVote,
    hasVoted: Boolean,
    addVote: (DateTimeVote) -> Unit,
    removeVote: (DateTimeVote) -> Unit,
) {
  Card(
      modifier =
          Modifier.fillMaxWidth()
              .padding(5.dp)
              .wrapContentHeight()
              .testTag(DateTimeVoteScreenTestTags.DATETIME_VOTE_CARD),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                  text = Formatters.formatDateTime(dateTimeVote.dateTime.toDate()),
                  style = MaterialTheme.typography.bodyLarge,
                  fontWeight = FontWeight.SemiBold,
                  modifier = Modifier.testTag(DateTimeVoteScreenTestTags.DATETIME_VOTE_DATETIME))

              AssistChip(
                  modifier = Modifier.testTag(DateTimeVoteScreenTestTags.DATETIME_VOTE_VOTE_BUTTON),
                  onClick = { if (hasVoted) removeVote(dateTimeVote) else addVote(dateTimeVote) },
                  label = { Text(text = "${dateTimeVote.voters.size}") },
                  leadingIcon = {
                    Icon(imageVector = Icons.Default.People, contentDescription = "Total votes")
                  },
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
      }
}
