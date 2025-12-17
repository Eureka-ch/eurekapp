package ch.eureka.eurekapp.screens.subscreens.meetings

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
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
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.RecordingState
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors.BorderGrayColor
import ch.eureka.eurekapp.ui.theme.DarkColorScheme
import ch.eureka.eurekapp.ui.theme.LightColorScheme
import ch.eureka.eurekapp.ui.theme.Typography
import ch.eureka.eurekapp.utils.Formatters
import kotlinx.coroutines.delay

object MeetingAudioScreenTestTags {
  const val START_RECORDING_BUTTON = "start recording button"
  const val PAUSE_RECORDING_BUTTON = "pause recording button"
  const val STOP_RECORDING_BUTTON = "stop recording button"
  const val UPLOAD_TO_DATABASE_BUTTON = "upload to database button"
  const val GENERATE_AI_TRANSCRIPT_BUTTON = "generate ai transcript button"
  const val BACK_BUTTON = "BackButton"
}

/**
 * Note :This file was partially written by ChatGPT (GPT-5) and Grok. Co-author : GPT-5 Co-author :
 * Grok
 */
@Composable
fun MeetingAudioRecordingScreen(
    context: Context = LocalContext.current,
    projectId: String,
    meetingId: String,
    audioRecordingViewModel: AudioRecordingViewModel = viewModel(),
    meetingRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
    onNavigateToTranscript: (String, String) -> Unit = { _, _ -> },
    onBackClick: () -> Unit = {}
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

  // HandleMicrophonePermission(microphonePermissionIsGranted, launcher)

  HandleMicrophonePermission(microphonePermissionIsGranted, launcher)

  val timeInSeconds = remember { audioRecordingViewModel.recordingTimeInSeconds }.collectAsState()

  var errorText by remember { mutableStateOf<String>("") }
  var uploadText by remember { mutableStateOf<String>("") }
  var canPressUploadButton by remember { mutableStateOf<Boolean>(true) }

  // If a transcript already exists for this meeting, show the View Transcript button
  HandleExistingTranscript(
      meetingState.value, onTranscriptFound = { canShowAITranscriptButton = true })

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

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = stringResource(id = R.string.audio_recording_title),
            navigationIcon = {
              BackButton(
                  onClick = onBackClick,
                  modifier = Modifier.testTag(MeetingAudioScreenTestTags.BACK_BUTTON))
            })
      },
      content = { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center) {
              Row(
                  modifier = Modifier.height(120.dp),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.Top) {
                    Text(
                        stringResource(id = R.string.audio_recording_title_with_emoji),
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
                                    Formatters.formatTime(timeInSeconds.value),
                                    modifier = Modifier.padding(10.dp),
                                    style = Typography.titleMedium,
                                    color = DarkColorScheme.background)
                              }

                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.Center,
                              verticalAlignment = Alignment.CenterVertically) {
                                when (recordingStatus.value) {
                                  RecordingState.PAUSED -> {
                                    StopButton(
                                        onClick = {
                                          audioRecordingViewModel.stopRecording()
                                          audioRecordingViewModel.deleteLocalRecording()
                                        },
                                        testTag = MeetingAudioScreenTestTags.STOP_RECORDING_BUTTON)
                                    PlayButton(
                                        onClick = { audioRecordingViewModel.resumeRecording() },
                                        testTag = MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
                                    SaveButton(
                                        enabled = canPressUploadButton,
                                        onClick = {
                                          canPressUploadButton = false
                                          audioRecordingViewModel.uploadRecordingToDatabase(
                                              projectId,
                                              meetingId,
                                              onSuccesfulUpload = {
                                                // use context to obtain string here because this callback is not executed during composition
                                                uploadText = context.getString(R.string.uploaded_successfully)
                                                canShowAITranscriptButton = true
                                              },
                                              onFailureUpload = { exception ->
                                                errorText = exception.message ?: ""
                                              },
                                              onCompletion = { canPressUploadButton = true })
                                        },
                                        testTag =
                                            MeetingAudioScreenTestTags.UPLOAD_TO_DATABASE_BUTTON)
                                  }
                                  RecordingState.STOPPED -> {
                                    PlayButton(
                                        onClick = {
                                          audioRecordingViewModel.startRecording(
                                              context, "${projectId}_${meetingId}.mp4")
                                          canPressUploadButton = true
                                        },
                                        testTag = MeetingAudioScreenTestTags.START_RECORDING_BUTTON)
                                  }
                                  RecordingState.RUNNING -> {
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
                                        Row() {
                                          Text(stringResource(id = R.string.view_transcript), style = Typography.titleMedium)
                                        }
                                      }
                                }
                          }
                        }
                  }
            }
      })
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

@Composable
private fun HandleMicrophonePermission(
    permissionGranted: Boolean,
    launcher: ManagedActivityResultLauncher<String, Boolean>
) {
  LaunchedEffect(permissionGranted) {
    if (!permissionGranted) launcher.launch(Manifest.permission.RECORD_AUDIO)
  }
}

@Composable
private fun HandleExistingTranscript(meeting: Meeting?, onTranscriptFound: () -> Unit) {
  LaunchedEffect(meeting?.transcriptId) {
    if (meeting?.transcriptId?.isNotBlank() == true) onTranscriptFound()
  }
}
