package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File



class LocalAudioRecordingRepository(
): AudioRecordingRepository {
    private var recording: File? = null
    private var audioRecorder: MediaRecorder? = null
    private var recordingState: MutableStateFlow<RECORDING_STATE> = MutableStateFlow(RECORDING_STATE.STOPPED)

    override fun createRecording(context: Context, fileName: String): Result<Uri> {
        return runCatching {
            if(recording != null){
                throw RuntimeException("You already have a recording!")
            }
            recording = File(context.filesDir, fileName)

            audioRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(recording!!.absolutePath)
                prepare()
                start()
            }
            recordingState.value = RECORDING_STATE.RUNNING
            Uri.fromFile(recording!!)
        }
    }

    override fun clearRecording(): Result<Unit> {
        return runCatching {
            if(recordingState.value == RECORDING_STATE.PAUSED){
                audioRecorder!!.stop()
                audioRecorder!!.release()
                recordingState.value = RECORDING_STATE.STOPPED
                Unit
            }else{
                throw RuntimeException("Recording was not paused!")
            }
        }
    }

    override fun pauseRecording(): Result<Unit> {
        return runCatching {
            if(recordingState.value == RECORDING_STATE.RUNNING){
                audioRecorder!!.pause()
                recordingState.value = RECORDING_STATE.PAUSED
                Unit
            }else{
               throw RuntimeException(
                    "Cannot pause the recording!")
            }
        }
    }

    override fun resumeRecording(): Result<Unit> {
        return runCatching {
            if(recordingState.value == RECORDING_STATE.PAUSED){
                audioRecorder!!.resume()
                recordingState.value = RECORDING_STATE.RUNNING
                Unit
            }else{
                throw RuntimeException("Cannot resume the recording!")
            }
        }
    }

    override fun deleteRecording(): Result<Unit> {
        return runCatching {
            if(recording != null){
                recording!!.delete()
                recording = null
            }
        }
    }

    override fun getRecordingStateFlow(): StateFlow<RECORDING_STATE> {
        return recordingState.asStateFlow()
    }
}