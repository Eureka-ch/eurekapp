package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.file.FirebaseFileStorageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioRecordingViewModel(
    val fileStorageRepository: FileStorageRepository =
        FirebaseFileStorageRepository(
        FirebaseStorage.getInstance(),
        FirebaseAuth.getInstance()
    )
): ViewModel() {
    private val _recordingUri: MutableStateFlow<Uri?> = MutableStateFlow(null)
    val recordingUri = _recordingUri.asStateFlow()

    private val _isRecording: MutableStateFlow<RECORDING_STATE> =
        MutableStateFlow(RECORDING_STATE.STOPPED)
    val isRecording = _isRecording.asStateFlow()

    private var recordingRepository: AudioRecordingRepository? = null

    fun startRecording(context: Context, fileName: String){
        if(_isRecording.value == RECORDING_STATE.STOPPED){
            if(recordingRepository == null){
                recordingRepository = LocalAudioRecordingRepository(context)
            }
            val createdRecordingUri = recordingRepository!!.createRecording(fileName)
            if(createdRecordingUri.isFailure){
                return
            }
            _recordingUri.value = createdRecordingUri.getOrNull()
            _isRecording.value = RECORDING_STATE.RUNNING
        }
    }

    fun resumeRecording(){
        if(_isRecording.value == RECORDING_STATE.PAUSED){
            if(recordingRepository!!.resumeRecording().isFailure){
                return
            }
            _isRecording.value = RECORDING_STATE.RUNNING
        }
    }

    fun pauseRecording(){
        if(_isRecording.value == RECORDING_STATE.RUNNING){
            val result = recordingRepository!!.pauseRecording()
            if(result.isFailure){
                Log.d("AudioTranscriptScreen", result.exceptionOrNull()?.message.toString())
                return
            }
            _isRecording.value = RECORDING_STATE.PAUSED
            Log.d("AudioTranscriptScreen", _isRecording.value.toString())
        }
    }

    fun stopRecording(){
        if(_isRecording.value == RECORDING_STATE.PAUSED){
            if(recordingRepository!!.clearRecording().isFailure){
                return
            }
            _isRecording.value == RECORDING_STATE.STOPPED
        }
    }

    suspend fun saveRecordingToDatabase(projectId: String, meetingId: String){
        if(_recordingUri.value != null && isRecording.value == RECORDING_STATE.PAUSED){
            if(fileStorageRepository
                .uploadFile(StoragePaths.meetingAttachmentPath(projectId,
                    meetingId, "${_recordingUri.value!!.lastPathSegment}.mp4"),
                    _recordingUri.value!!).isFailure){

            }

        }
    }

    override fun onCleared() {
        super.onCleared()
        //We need to clean up the recording if the view model is cleared
        if(isRecording.value == RECORDING_STATE.RUNNING){
           pauseRecording()
           stopRecording()
        }else if(isRecording.value == RECORDING_STATE.PAUSED){
            stopRecording()
        }
    }
}