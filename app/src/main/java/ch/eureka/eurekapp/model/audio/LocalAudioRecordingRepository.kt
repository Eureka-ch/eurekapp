package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import java.io.File



class LocalAudioRecordingRepository(
    val context: Context
): AudioRecordingRepository {
    private var recording: File? = null
    private var audioRecorder: MediaRecorder? = null
    private var recordingState: RECORDING_STATE = RECORDING_STATE.STOPPED

    override fun createRecording(fileName: String): Result<Uri> {
        if(recording != null){
            return Result.failure(
                IllegalArgumentException("You already have a recording!"))
        }
        recording = File(context.filesDir, fileName)

        audioRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recording!!.absolutePath)
            prepare()
        }
        recordingState = RECORDING_STATE.PAUSED
        return Result.success(Uri.fromFile(recording!!))
    }

    override fun clearRecording(): Result<Unit> {
        if(recordingState == RECORDING_STATE.PAUSED){
            audioRecorder!!.stop()
            recording!!.delete()
            recording = null
            recordingState = RECORDING_STATE.STOPPED
            return Result.success(Unit)
        }else{
            return Result.failure(
                IllegalArgumentException("Recording did not exist!"))
        }
    }

    override fun pauseRecording(): Result<Unit> {
        if(recordingState == RECORDING_STATE.RUNNING){
            audioRecorder!!.pause()
            recordingState = RECORDING_STATE.PAUSED
            return Result.success(Unit)
        }else{
            return Result.failure(RuntimeException(
                "Cannot pause the recording!"))
        }
    }

    override fun resumeRecording(): Result<Unit> {
        if(recordingState == RECORDING_STATE.PAUSED){
            audioRecorder!!.resume()
            return Result.success(Unit)
        }else{
            return Result.failure(
                RuntimeException("Cannot resume the recording!"))
        }
    }
}