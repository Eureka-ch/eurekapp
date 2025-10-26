package ch.eureka.eurekapp.model.audio

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.flow.Flow

interface AudioRecordingRepository {
    fun createRecording(fileName: String): Result<Uri>
    fun clearRecording(): Result<Unit>
    fun pauseRecording(): Result<Unit>
    fun resumeRecording(): Result<Unit>
}