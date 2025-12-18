package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5 */
class AudioRecordingViewModel(
    val fileStorageRepository: FileStorageRepository =
        FirebaseFileStorageRepository(FirebaseStorage.getInstance(), FirebaseAuth.getInstance()),
    val recordingRepository: LocalAudioRecordingRepository =
        AudioRecordingRepositoryProvider.repository,
    private val meetingRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
) : ViewModel() {
  private val _recordingUri: MutableStateFlow<Uri?> = MutableStateFlow(null)

  val isRecording: StateFlow<RecordingState> = recordingRepository.getRecordingStateFlow()

  private val _recordingTimeInSeconds: MutableStateFlow<Long> = MutableStateFlow(0L)
  val recordingTimeInSeconds = _recordingTimeInSeconds.asStateFlow()

  private val connectivityObserver = ConnectivityObserverProvider.connectivityObserver
  val isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  fun startRecording(context: Context, fileName: String) {
    val createdRecordingUri = recordingRepository.createRecording(context, fileName)
    if (createdRecordingUri.isFailure) {
      return
    }
    _recordingUri.value = createdRecordingUri.getOrNull()
    addTimeToRecording()
  }

  fun resumeRecording() {
    if (isRecording.value == RecordingState.PAUSED) {
      if (recordingRepository.resumeRecording().isFailure) {
        return
      }
      addTimeToRecording()
    }
  }

  fun pauseRecording() {
    if (isRecording.value == RecordingState.RUNNING) {
      val result = recordingRepository.pauseRecording()
      if (result.isFailure) {
        return
      }
    }
  }

  fun stopRecording() {
    if (isRecording.value == RecordingState.PAUSED) {
      val result = recordingRepository.clearRecording()
      if (result.isFailure) {
        return
      }
      resetRecordingTime()
    }
  }

  fun deleteLocalRecording() {
    recordingRepository.deleteRecording()
  }

  fun uploadRecordingToDatabase(
      projectId: String,
      meetingId: String,
      onSuccesfulUpload: (String) -> Unit,
      onFailureUpload: (Throwable) -> Unit,
      onCompletion: () -> Unit = {}
  ) {
    try {
      saveRecordingToDatabase(
          projectId, meetingId, onSuccesfulUpload, onFailureUpload, onCompletion)
    } finally {}
  }

  fun saveRecordingToDatabase(
      projectId: String,
      meetingId: String,
      onSuccesfulUpload: (String) -> Unit,
      onFailureUpload: (Throwable) -> Unit,
      onCompletion: () -> Unit
  ) {
    viewModelScope.launch {
      if (_recordingUri.value == null || isRecording.value != RecordingState.PAUSED) {
        onFailureUpload(IllegalStateException("No paused recording available to upload"))
        return@launch
      }

      stopRecording()
      val uploadResult =
          fileStorageRepository.uploadFile(
              StoragePaths.meetingTranscriptionAudioPath(
                  projectId, meetingId, "${_recordingUri.value!!.lastPathSegment}"),
              _recordingUri.value!!)

      uploadResult
          .onFailure { error -> onFailureUpload(error) }
          .onSuccess { firebaseUrl ->
            val meetingResult = runCatching {
              val meeting =
                  meetingRepository.getMeetingById(projectId, meetingId).first()
                      ?: throw IllegalStateException("Meeting not found")

              val updateResult =
                  meetingRepository.updateMeeting(meeting.copy(audioUrl = firebaseUrl))
              if (updateResult.isFailure) {
                throw updateResult.exceptionOrNull()
                    ?: RuntimeException("Failed to update meeting audio URL")
              }
              firebaseUrl
            }

            meetingResult
                .onSuccess { url ->
                  deleteLocalRecording()
                  onSuccesfulUpload(url)
                }
                .onFailure { error -> onFailureUpload(error) }

            onCompletion()
          }
    }
  }

  private var recordingTimeJob: Job? = null

  fun addTimeToRecording() {
    recordingTimeJob?.cancel()
    recordingTimeJob =
        viewModelScope.launch {
          while (isRecording.value == RecordingState.RUNNING) {
            delay(1000L)
            if (isRecording.value == RecordingState.RUNNING) {
              _recordingTimeInSeconds.value += 1
            }
          }
        }
  }

  fun resetRecordingTime() {
    _recordingTimeInSeconds.value = 0
  }

  override fun onCleared() {
    super.onCleared()
    // We need to clean up the recording if the view model is cleared
    if (isRecording.value == RecordingState.RUNNING) {
      pauseRecording()
      stopRecording()
      deleteLocalRecording()
    } else if (isRecording.value == RecordingState.PAUSED) {
      stopRecording()
    }
  }

  fun testOnCleared() {
    onCleared()
  }
}
