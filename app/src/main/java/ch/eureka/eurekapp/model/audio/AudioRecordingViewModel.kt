package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioRecordingViewModel(
    val fileStorageRepository: FileStorageRepository =
        FirebaseFileStorageRepository(FirebaseStorage.getInstance(), FirebaseAuth.getInstance()),
    val recordingRepository: LocalAudioRecordingRepository =
        AudioRecordingRepositoryProvider.repository
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

  suspend fun saveRecordingToDatabase(
      projectId: String,
      meetingId: String,
      onSuccesfulUpload: (String) -> Unit,
      onFailureUpload: (Throwable) -> Unit
  ) {
    if (_recordingUri.value != null && isRecording.value == RECORDING_STATE.PAUSED) {
      stopRecording()
      val result =
          fileStorageRepository.uploadFile(
              StoragePaths.meetingAttachmentPath(
                  projectId, meetingId, "${_recordingUri.value!!.lastPathSegment}"),
              _recordingUri.value!!)
      if (result.isFailure) {
        onFailureUpload(result.exceptionOrNull()!!)
      } else {
        deleteLocalRecording()
        onSuccesfulUpload(result.getOrNull()!!)
      }
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
