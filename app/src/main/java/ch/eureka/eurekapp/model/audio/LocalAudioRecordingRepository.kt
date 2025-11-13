package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.media.MediaRecorder
import android.net.Uri
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalAudioRecordingRepository() : AudioRecordingRepository {
  private var recording: File? = null
  private var audioRecorder: MediaRecorder? = null
  private var recordingState: MutableStateFlow<RecordingState> =
      MutableStateFlow(RecordingState.STOPPED)

  override fun createRecording(context: Context, fileName: String): Result<Uri> {
    return runCatching {
      if (recording != null) {
        throw RuntimeException("You already have a recording!")
      }
      recording = File(context.filesDir, fileName)

      audioRecorder =
          MediaRecorder(context).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(recording!!.absolutePath)
            prepare()
            start()
          }
      recordingState.value = RecordingState.RUNNING
      Uri.fromFile(recording!!)
    }
  }

  override fun clearRecording(): Result<Unit> {
    return runCatching {
      if (recordingState.value == RecordingState.PAUSED) {
        audioRecorder!!.stop()
        audioRecorder!!.release()
        recordingState.value = RecordingState.STOPPED
        Unit
      } else {
        throw RuntimeException("Recording was not paused!")
      }
    }
  }

  override fun pauseRecording(): Result<Unit> {
    return runCatching {
      if (recordingState.value == RecordingState.RUNNING) {
        audioRecorder!!.pause()
        recordingState.value = RecordingState.PAUSED
        Unit
      } else {
        throw RuntimeException("Cannot pause the recording!")
      }
    }
  }

  override fun resumeRecording(): Result<Unit> {
    return runCatching {
      if (recordingState.value == RecordingState.PAUSED) {
        audioRecorder!!.resume()
        recordingState.value = RecordingState.RUNNING
        Unit
      } else {
        throw RuntimeException("Cannot resume the recording!")
      }
    }
  }

  override fun deleteRecording(): Result<Unit> {
    return runCatching {
      if (recording != null) {
        recording!!.delete()
        recording = null
      }
    }
  }

  override fun getRecordingStateFlow(): StateFlow<RecordingState> {
    return recordingState.asStateFlow()
  }
}
