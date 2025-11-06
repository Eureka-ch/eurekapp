package ch.eureka.eurekapp.screens.subscreens.meetings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.RECORDING_STATE
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.theme.DarkColorScheme
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography
import ch.eureka.eurekapp.utils.Formatters
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

object MeetingAudioScreenTestTags {
  const val START_RECORDING_BUTTON = "start recording button"
  const val PAUSE_RECORDING_BUTTON = "pause recording button"
  const val STOP_RECORDING_BUTTON = "stop recording button"
  const val UPLOAD_TO_DATABASE_BUTTON = "upload to database button"
  const val GENERATE_AI_TRANSCRIPT_BUTTON = "generate ai transcript button"
}

@Composable
fun MeetingAudioRecordingScreen(
    context: Context = LocalContext.current,
    projectId: String,
    meetingId: String,
    audioRecordingViewModel: AudioRecordingViewModel = viewModel(),
    meetingRepository: MeetingRepository = FirestoreRepositoriesProvider.meetingRepository,
    onNavigateToTranscript: (String, String) -> Unit = { _, _ -> }
) {

  var microphonePermissionIsGranted by remember {
    mutableStateOf(
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED)
  }

  val launcher =
      rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
          isGranted: Boolean ->
        microphonePermissionIsGranted = isGranted
      }

  val recordingStatus = audioRecordingViewModel.isRecording.collectAsState()

  val meetingState =
      meetingRepository.getMeetingById(projectId, meetingId).collectAsState(initial = null)

  var canShowAITranscriptButton by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    if (!microphonePermissionIsGranted) {
      launcher.launch(Manifest.permission.RECORD_AUDIO)
    }
  }

  var timeInSeconds by remember { mutableStateOf<Long>(0L) }

  var errorText by remember { mutableStateOf<String>("") }
  var uploadText by remember { mutableStateOf<String>("") }
  var canPressUploadButton by remember { mutableStateOf<Boolean>(true) }

  var firebaseDownloadURI by remember { mutableStateOf<String?>(null) }

  // If a transcript already exists for this meeting, show the View Transcript button
  LaunchedEffect(meetingState.value?.transcriptId) {
    if (meetingState.value?.transcriptId != null) {
      canShowAITranscriptButton = true
    }
  }

  LaunchedEffect(recordingStatus.value) {
    while (recordingStatus.value == RECORDING_STATE.RUNNING) {
      delay(1000)
      timeInSeconds += 1
    }
  }

  LaunchedEffect(uploadText) {
    if (uploadText != "") {
      delay(5000)
      uploadText = ""
    }
  }

  LaunchedEffect(errorText) {
    if (errorText != "") {
      delay(5000)
      errorText = ""
    }
  }

  Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        Row(
            modifier = Modifier.height(120.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top) {
              Text(
                  "\uD83C\uDFA4 Audio Recording",
                  modifier = Modifier.padding(10.dp),
                  style = Typography.titleLarge,
                  color = DarkColorScheme.background)
            }

        Surface(
            modifier =
                Modifier.border(
                        border = BorderStroke(width = 1.dp, color = BorderGrayColor),
                        shape = RoundedCornerShape(16.dp))
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.6f),
            shadowElevation = 3.dp,
            color = Color.White,
            shape = RoundedCornerShape(16.dp)) {
              Column(
                  modifier = Modifier.fillMaxSize(),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Center) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              Formatters.formatTime(timeInSeconds),
                              modifier = Modifier.padding(10.dp),
                              style = Typography.titleMedium,
                              color = DarkColorScheme.background)
                        }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          when (recordingStatus.value) {
                            RECORDING_STATE.PAUSED -> {
                              StopButton(
                                  onClick = {
                                    audioRecordingViewModel.stopRecording()
                                    audioRecordingViewModel.deleteLocalRecording()
                                    timeInSeconds = 0
                                  },
                                  testTag = MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON)
                              PlayButton(
                                  onClick = { audioRecordingViewModel.resumeRecording() },
                                  testTag = MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
                              SaveButton(
                                  enabled = canPressUploadButton,
                                  onClick = {
                                    audioRecordingViewModel.viewModelScope.launch {
                                      canPressUploadButton = false
                                      audioRecordingViewModel.saveRecordingToDatabase(
                                          projectId,
                                          meetingId,
                                          onSuccesfulUpload = { firebaseURL ->
                                            uploadText = "Uploaded successfully!"
                                            canShowAITranscriptButton = true
                                            firebaseDownloadURI = firebaseURL

                                            // Save audio URL
                                            audioRecordingViewModel.viewModelScope.launch {
                                              val meeting =
                                                  meetingRepository
                                                      .getMeetingById(projectId, meetingId)
                                                      .first()
                                              meeting?.let {
                                                val updatedMeeting = it.copy(audioUrl = firebaseURL)
                                                meetingRepository.updateMeeting(updatedMeeting)
                                              }
                                            }
                                          },
                                          onFailureUpload = { exception ->
                                            errorText =
                                                if (exception.message != null)
                                                    exception.message.toString()
                                                else ""
                                          })
                                    }
                                  },
                                  testTag = MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
                            }
                            RECORDING_STATE.STOPPED -> {
                              PlayButton(
                                  onClick = {
                                    audioRecordingViewModel.startRecording(
                                        context, "${projectId}_${meetingId}.mp4")
                                    canPressUploadButton = true
                                    timeInSeconds = 0
                                  },
                                  testTag = MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
                            }
                            RECORDING_STATE.RUNNING -> {
                              PauseButton(
                                  onClick = { audioRecordingViewModel.pauseRecording() },
                                  testTag = MeetingAudioScreenTestTags.PAUSE_RECORDING_BUTTON)
                            }
                          }
                        }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically) {
                          Text(
                              errorText,
                              style = Typography.labelMedium,
                              fontWeight = FontWeight(500),
                              color = LightColorScheme.error,
                              modifier = Modifier.padding(vertical = 10.dp))
                          Text(
                              uploadText,
                              style = Typography.labelMedium,
                              fontWeight = FontWeight(500),
                              color = DarkColorScheme.background,
                              modifier = Modifier.padding(vertical = 10.dp))
                        }

                    Spacer(modifier = Modifier.weight(1f))

                    if (canShowAITranscriptButton) {
                      Row(
                          modifier = Modifier.fillMaxWidth().padding(20.dp),
                          horizontalArrangement = Arrangement.Center,
                          verticalAlignment = Alignment.CenterVertically) {
                            ElevatedButton(
                                modifier =
                                    Modifier.size(width = 250.dp, height = 50.dp)
                                        .testTag(
                                            MeetingAudioScreenTestTags
                                                .GENERATE_AI_TRANSCRIPT_BUTTON),
                                onClick = { onNavigateToTranscript(projectId, meetingId) }) {
                                  Row() { Text("View Transcript", style = Typography.titleMedium) }
                                }
                          }
                    }
                  }
            }
      }
}

@Composable
fun CustomIconButtonForAudioRecording(
    onClick: () -> Unit,
    iconVector: ImageVector,
    backgroundColor: Color = LightColorScheme.surface,
    enabled: Boolean = true,
    testTag: String
) {
  Row(
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(15.dp)) {
        Surface(
            modifier = Modifier.size(70.dp),
            shape = CircleShape,
            color = backgroundColor,
            tonalElevation = 6.dp, // or shadowElevation in M2
            border = BorderStroke(1.dp, BorderGrayColor)) {
              IconButton(
                  modifier = Modifier.testTag(testTag),
                  enabled = enabled,
                  onClick = onClick,
              ) {
                Icon(
                    modifier = Modifier.size(35.dp),
                    imageVector = iconVector,
                    contentDescription = null)
              }
            }
      }
}

@Composable
fun PlayButton(onClick: () -> Unit, testTag: String) {
  CustomIconButtonForAudioRecording(
      onClick = onClick, iconVector = Icons.Outlined.PlayArrow, testTag = testTag)
}

@Composable
fun PauseButton(onClick: () -> Unit, testTag: String) {
  CustomIconButtonForAudioRecording(
      onClick = onClick,
      iconVector = Icons.Outlined.Pause,
      backgroundColor = LightColorScheme.primary,
      testTag = testTag)
}

@Composable
fun StopButton(onClick: () -> Unit, testTag: String) {
  CustomIconButtonForAudioRecording(
      onClick = onClick, iconVector = Icons.Outlined.Stop, testTag = testTag)
}

@Composable
fun SaveButton(onClick: () -> Unit, enabled: Boolean, testTag: String) {
  CustomIconButtonForAudioRecording(
      onClick = onClick,
      enabled = enabled,
      iconVector = Icons.Outlined.CloudUpload,
      testTag = testTag)
}
