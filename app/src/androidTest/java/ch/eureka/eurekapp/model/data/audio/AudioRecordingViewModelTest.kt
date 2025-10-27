package ch.eureka.eurekapp.model.data.audio

import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.GrantPermissionRule
import ch.eureka.eurekapp.model.audio.AudioRecordingViewModel
import ch.eureka.eurekapp.model.audio.LocalAudioRecordingRepository
import ch.eureka.eurekapp.model.audio.RECORDING_STATE
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AudioRecordingViewModelTest {

    class MockedStorageRepository: FileStorageRepository{
        override suspend fun uploadFile(
            storagePath: String,
            fileUri: Uri
        ): Result<String> {
            return Result.success("test")
        }

        override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
            return Result.failure(RuntimeException(""))
        }

    }

    class ErrorMockedStorageRepository: FileStorageRepository {
        override suspend fun uploadFile(
            storagePath: String,
            fileUri: Uri
        ): Result<String> {
            return Result.failure(RuntimeException(""))
        }

        override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
            return Result.failure(RuntimeException(""))
        }
    }



    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.RECORD_AUDIO
    )

    @Test
    fun startRecordingWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(
            recordingRepository = LocalAudioRecordingRepository())

        viewModel.startRecording(context, "test_recording")

        assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)

        viewModel.startRecording(context, "test_recording")

        assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
    }

    @Test
    fun pauseRecordingWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()

        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(
            recordingRepository = LocalAudioRecordingRepository())
        viewModel.startRecording(context, "test_recording")
        viewModel.pauseRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
        viewModel.pauseRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
    }

    @Test
    fun stopRecordingWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(
            recordingRepository = LocalAudioRecordingRepository())
        viewModel.startRecording(context, "test_recording")
        viewModel.stopRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
        viewModel.pauseRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
        viewModel.stopRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.STOPPED)
    }

    @Test
    fun resumeRecordingWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(
            recordingRepository = LocalAudioRecordingRepository())
        viewModel.startRecording(context, "test_recording")
        viewModel.resumeRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
        viewModel.pauseRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.PAUSED)
        viewModel.resumeRecording()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.RUNNING)
    }

    @Test
    fun deleteRecordingWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(
            recordingRepository = LocalAudioRecordingRepository())
        viewModel.startRecording(context, "test_recording")
        viewModel.pauseRecording()
        viewModel.stopRecording()
        viewModel.deleteLocalRecording()
    }

    @Test
    fun onClearedWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(
            recordingRepository = LocalAudioRecordingRepository())
        viewModel.startRecording(context, "test_recording")
        viewModel.testOnCleared()
        assertTrue(viewModel.isRecording.value == RECORDING_STATE.STOPPED)
    }

    @Test
    fun saveRecordingToDatabaseWorksAsExpected(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(fileStorageRepository =
            MockedStorageRepository(), recordingRepository = LocalAudioRecordingRepository())

        var runned = false

        viewModel.startRecording(context, "test_recording")
        viewModel.pauseRecording()
        runBlocking {viewModel.saveRecordingToDatabase("", "",
            {runned = true},{})}
        assertTrue(runned)
    }

    @Test
    fun saveRecordingToDatabaseWorksAsExpectedOnFailure(){
        val context = ApplicationProvider.getApplicationContext<Context>()
        val viewModel: AudioRecordingViewModel = AudioRecordingViewModel(fileStorageRepository =
            MockedStorageRepository(), recordingRepository = LocalAudioRecordingRepository())

        var runned = false

        viewModel.startRecording(context, "test_recording")
        viewModel.pauseRecording()
        runBlocking {viewModel.saveRecordingToDatabase("", "",
            {},{runned = true})}
        assertTrue(runned)
    }

}