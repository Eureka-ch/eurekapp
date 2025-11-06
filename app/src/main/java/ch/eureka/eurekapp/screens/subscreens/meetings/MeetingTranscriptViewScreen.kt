package ch.eureka.eurekapp.screens.subscreens.meetings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.transcription.TranscriptionStatus
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.meeting.TranscriptViewModel
import ch.eureka.eurekapp.ui.theme.Typography

/** Test tags for MeetingTranscriptViewScreen UI elements */
object TranscriptScreenTestTags {
  const val TRANSCRIPT_SCREEN = "TranscriptScreen"
  const val LOADING_INDICATOR = "LoadingIndicator"
  const val ERROR_MESSAGE = "ErrorMessage"
  const val PLAY_PAUSE_BUTTON = "PlayPauseButton"
  const val AUDIO_SEEK_BAR = "AudioSeekBar"
  const val GENERATE_TRANSCRIPT_BUTTON = "GenerateTranscriptButton"
  const val TRANSCRIPTION_LOADING = "TranscriptionLoading"
  const val TRANSCRIPT_TEXT = "TranscriptText"
  const val TRANSCRIPTION_ERROR = "TranscriptionError"
  const val GENERATE_SUMMARY_BUTTON = "GenerateSummaryButton"
  const val SUMMARY_LOADING = "SummaryLoading"
  const val SUMMARY_TEXT = "SummaryText"
}

/**
 * Screen displaying meeting audio with transcript and AI summary generation.
 *
 * Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingTranscriptViewScreen(
    projectId: String,
    meetingId: String,
    viewModel: TranscriptViewModel = remember { TranscriptViewModel(projectId, meetingId) },
    onNavigateBack: () -> Unit
) {
  val uiState by viewModel.uiState.collectAsState()

  Scaffold(
      modifier = Modifier.fillMaxSize().testTag(TranscriptScreenTestTags.TRANSCRIPT_SCREEN),
      topBar = {
        TopAppBar(
            title = { Text("Transcript") },
            navigationIcon = {
              IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
              }
            })
      }) { paddingValues ->
        when {
          uiState.isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  CircularProgressIndicator(
                      modifier = Modifier.testTag(TranscriptScreenTestTags.LOADING_INDICATOR))
                }
          }
          uiState.errorMsg != null && uiState.meeting == null -> {
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = uiState.errorMsg ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag(TranscriptScreenTestTags.ERROR_MESSAGE))
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.clearErrorMsg() }) { Text("Dismiss") }
                  }
                }
          }
          else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)) {
                  // Transcript Section
                  item {
                    if (uiState.errorMsg != null) {
                      Text(
                          text = uiState.errorMsg ?: "",
                          color = MaterialTheme.colorScheme.error,
                          modifier = Modifier.testTag(TranscriptScreenTestTags.ERROR_MESSAGE))
                      Spacer(modifier = Modifier.height(8.dp))
                    }

                    when {
                      !uiState.hasTranscript && !uiState.isGeneratingTranscript -> {
                        Button(
                            onClick = { viewModel.generateTranscript() },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag(TranscriptScreenTestTags.GENERATE_TRANSCRIPT_BUTTON)) {
                              Text("Generate Transcript")
                            }
                      }
                      uiState.isGeneratingTranscript ||
                          uiState.transcriptionStatus == TranscriptionStatus.PENDING -> {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically) {
                              CircularProgressIndicator(
                                  modifier =
                                      Modifier.size(20.dp)
                                          .testTag(TranscriptScreenTestTags.TRANSCRIPTION_LOADING))
                              Spacer(modifier = Modifier.size(12.dp))
                              Text("Generating transcript...")
                            }
                      }
                      uiState.transcriptionStatus == TranscriptionStatus.COMPLETED -> {
                        Text(
                            text = uiState.transcriptionText ?: "",
                            style = Typography.bodyMedium,
                            modifier = Modifier.testTag(TranscriptScreenTestTags.TRANSCRIPT_TEXT))
                      }
                      uiState.transcriptionStatus == TranscriptionStatus.FAILED -> {
                        Text(
                            text = uiState.errorMsg ?: "Transcription failed",
                            color = MaterialTheme.colorScheme.error,
                            modifier =
                                Modifier.testTag(TranscriptScreenTestTags.TRANSCRIPTION_ERROR))
                      }
                    }
                  }

                  // Summary Section
                  item {
                    if (uiState.transcriptionStatus == TranscriptionStatus.COMPLETED) {
                      Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = EColors.BorderGrayColor)
                        Spacer(modifier = Modifier.height(16.dp))

                        when {
                          uiState.isSummarizing -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically) {
                                  CircularProgressIndicator(
                                      modifier =
                                          Modifier.size(20.dp)
                                              .testTag(TranscriptScreenTestTags.SUMMARY_LOADING))
                                  Spacer(modifier = Modifier.size(12.dp))
                                  Text("Generating summary...")
                                }
                          }
                          uiState.summary != null -> {
                            Text(
                                text = uiState.summary ?: "",
                                style = Typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.testTag(TranscriptScreenTestTags.SUMMARY_TEXT))
                          }
                          else -> {
                            Button(
                                onClick = { viewModel.generateSummary() },
                                modifier =
                                    Modifier.fillMaxWidth()
                                        .testTag(
                                            TranscriptScreenTestTags.GENERATE_SUMMARY_BUTTON)) {
                                  Text("Generate Summary")
                                }
                          }
                        }
                      }
                    }
                  }
                }
          }
        }
      }
}
