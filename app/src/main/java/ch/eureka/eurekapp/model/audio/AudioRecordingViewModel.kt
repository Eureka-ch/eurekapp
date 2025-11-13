package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5 */
class AudioRecordingViewModel(
    val fileStorageRepository: FileStorageRepository =
        FirebaseFileStorageRepository(FirebaseStorage.getInstance(), FirebaseAuth.getInstance()),
    val recordingRepository: AudioRecordingRepository = AudioRecordingRepositoryProvider.repository,
    private val meetingRepository: MeetingRepository =
        FirestoreRepositoriesProvider.meetingRepository
) : ViewModel() {
  private val _recordingUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
  val recordingUri = _recordingUri.asStateFlow()

  val isRecording: StateFlow<RECORDING_STATE> = recordingRepository.getRecordingStateFlow()

  fun startRecording(context: Context, fileName: String) {
    val createdRecordingUri = recordingRepository.createRecording(context, fileName)
    if (createdRecordingUri.isFailure) {
      return
    }
    _recordingUri.value = createdRecordingUri.getOrNull()
  }

  fun resumeRecording() {
    if (isRecording.value == RECORDING_STATE.PAUSED) {
      if (recordingRepository.resumeRecording().isFailure) {
        return
      }
    }
  }

  fun pauseRecording() {
    if (isRecording.value == RECORDING_STATE.RUNNING) {
      val result = recordingRepository.pauseRecording()
      if (result.isFailure) {
        return
      }
    }
  }

  fun stopRecording() {
    if (isRecording.value == RECORDING_STATE.PAUSED) {
      val result = recordingRepository.clearRecording()
      if (result.isFailure) {
        return
      }
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
    viewModelScope.launch {
      try {
        saveRecordingToDatabase(projectId, meetingId, onSuccesfulUpload, onFailureUpload)
      } finally {
        onCompletion()
      }
    }
  }

  suspend fun saveRecordingToDatabase(
      projectId: String,
      meetingId: String,
      onSuccesfulUpload: (String) -> Unit,
      onFailureUpload: (Throwable) -> Unit
  ) {
    if (_recordingUri.value == null || isRecording.value != RECORDING_STATE.PAUSED) {
      onFailureUpload(IllegalStateException("No paused recording available to upload"))
      return
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

            val updateResult = meetingRepository.updateMeeting(meeting.copy(audioUrl = firebaseUrl))
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
        }
  }

  override fun onCleared() {
    super.onCleared()
    // We need to clean up the recording if the view model is cleared
    if (isRecording.value == RECORDING_STATE.RUNNING) {
      pauseRecording()
      stopRecording()
      deleteLocalRecording()
    } else if (isRecording.value == RECORDING_STATE.PAUSED) {
      stopRecording()
    }
  }

  fun testOnCleared() {
    onCleared()
  }
}
