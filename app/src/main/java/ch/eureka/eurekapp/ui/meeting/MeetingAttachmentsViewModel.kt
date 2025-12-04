package ch.eureka.eurekapp.ui.meeting
// Portions of this code were generated with the help of Gemini 3 Pro.

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MeetingAttachmentsViewModel(
    private val fileStorageRepository: FileStorageRepository = RepositoriesProvider.fileRepository,
    private val meetingsRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
    private val connectivityObserver: ConnectivityObserver =
        ConnectivityObserverProvider.connectivityObserver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

  private val _downloadingFilesSet: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
  val downloadingFilesSet = _downloadingFilesSet.asStateFlow()

  private val _isUploadingFile = MutableStateFlow(false)
  val isUploadingFile = _isUploadingFile.asStateFlow()

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  private val _attachmentUrlsToFileNames: MutableStateFlow<Map<String, String>> =
      MutableStateFlow(mapOf())
  val attachmentUrlsToFileNames = _attachmentUrlsToFileNames.asStateFlow()

  fun uploadMeetingFileToFirestore(
      contentResolver: ContentResolver,
      uri: Uri,
      projectId: String,
      meetingId: String,
      onSuccess: () -> Unit,
      onFailure: (String) -> Unit
  ) {
    if (!_isConnected.value) {
      onFailure("You are not connected to the internet!")
      return
    }

    fun checkFileSize(): String? {
      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
          val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
          if (sizeIndex != -1) {
            val sizeInBytes = cursor.getLong(sizeIndex)
            if (sizeInBytes / (1024.0 * 1024.0) > 50) {
              return "File you are trying to upload is too big!"
            }
            return null
          }
          return "Failed to get the size of the file!"
        }
      }
      return null
    }

    viewModelScope.launch {
      withContext(ioDispatcher) {
        _isUploadingFile.value = true
        try {
          checkFileSize()?.let {
            onFailure(it)
            return@withContext
          }

          val fileName = getFileNameFromUri(uri, contentResolver)
          checkNotNull(fileName)

          val uploadResult =
              fileStorageRepository.uploadFile(
                  StoragePaths.meetingAttachmentPath(projectId, meetingId, fileName), uri)

          val downloadUrl = uploadResult.getOrNull()
          if (!(uploadResult.isSuccess && downloadUrl != null)) {
            onFailure("Unexpected error occurred!")
            return@withContext
          }

          val meeting = meetingsRepository.getMeetingById(projectId, meetingId).first()

          if (meeting == null) {
            deleteFileFromMeetingAttachments(projectId, meetingId, downloadUrl)
            onFailure("Meeting whose attachment you want no longer exists!")
            return@withContext
          }

          val updatedMeeting = meeting.copy(attachmentUrls = meeting.attachmentUrls + downloadUrl)

          val updateResult = meetingsRepository.updateMeeting(updatedMeeting)
          if (updateResult.isSuccess) {
            onSuccess()
          } else {
            deleteFileFromMeetingAttachments(projectId, meetingId, downloadUrl)
            onFailure("Unexpected error occurred!")
          }
        } catch (e: Exception) {
          onFailure(e.message.toString())
        } finally {
          _isUploadingFile.value = false
        }
      }
    }
  }

  fun deleteFileFromMeetingAttachments(
      projectId: String,
      meetingId: String,
      downloadUrl: String,
      onFailure: (String) -> Unit = {},
      onSuccess: () -> Unit = {}
  ) {
    if (!_isConnected.value) {
      onFailure("You are not connected to the internet!")
      return
    }
    viewModelScope.launch {
      withContext(ioDispatcher) {
        try {
          val deletedFile = fileStorageRepository.deleteFile(downloadUrl)
          val meetingGot = meetingsRepository.getMeetingById(projectId, meetingId).first()
          if (meetingGot != null && deletedFile.isSuccess) {
            val updatedMeeting =
                meetingGot.copy(attachmentUrls = meetingGot.attachmentUrls - downloadUrl)
            meetingsRepository.updateMeeting(updatedMeeting)
            onSuccess()
          } else {
            onFailure("Unexpected error occurred")
          }
        } catch (e: Exception) {
          onFailure(e.message.toString())
        }
      }
    }
  }

  fun downloadFileToPhone(
      context: Context,
      downloadUrl: String,
      onSuccess: () -> Unit,
      onFailure: (String) -> Unit
  ) {
    viewModelScope.launch {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        withContext(ioDispatcher) {
          try {
            val storageRef = FirebaseStorage.getInstance().reference
            val linkReference = storageRef.storage.getReferenceFromUrl(downloadUrl)
            val fileName = linkReference.name
            val mimeType = linkReference.metadata.await().contentType ?: "application/octet-stream"

            val contentValues =
                ContentValues().apply {
                  put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                  put(MediaStore.Downloads.MIME_TYPE, mimeType)
                  put(MediaStore.Downloads.IS_PENDING, 1)
                }

            val resolver = context.contentResolver
            val fileUri =
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw IOException("Failed to create MediaStore entry")

            val tempFile = File.createTempFile("temp_download", null, context.cacheDir)

            _downloadingFilesSet.value += downloadUrl

            linkReference.getFile(tempFile).await()

            resolver.openOutputStream(fileUri).use { outputStream ->
              if (outputStream == null) {
                resolver.delete(fileUri, null, null)
                _downloadingFilesSet.value -= downloadUrl
                onFailure("Failed to download the file")
                throw IOException("Temp file did not exist!")
              }

              tempFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
                contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                resolver.update(fileUri, contentValues, null, null)
                _downloadingFilesSet.value -= downloadUrl
                onSuccess()
              }
            }
          } catch (e: Exception) {
            _downloadingFilesSet.value -= downloadUrl
            onFailure("Failed to download the file: ${e.message}")
          }
        }
      }
    }
  }

  fun getFilenameFromDownloadURL(downloadUrl: String) {
    viewModelScope.launch {
      withContext(ioDispatcher) {
        try {
          val storageRef = FirebaseStorage.getInstance().reference
          val linkReference = storageRef.storage.getReferenceFromUrl(downloadUrl)
          _attachmentUrlsToFileNames.value += downloadUrl to linkReference.name
        } catch (e: Exception) {}
      }
    }
  }

  private fun getFileNameFromUri(uri: Uri, contentResolver: ContentResolver): String? {
    var name: String? = null
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0) {
          name = cursor.getString(nameIndex)
        }
      }
    }
    return name
  }

  private fun getFileSizeWithinLimits(
      contentResolver: ContentResolver,
      uri: Uri,
      onFailure: (String) -> Unit
  ): Boolean {
    contentResolver.query(uri, null, null, null, null)?.use { cursor ->
      if (cursor.moveToFirst()) {
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (sizeIndex != -1) {
          val sizeInBytes = cursor.getLong(sizeIndex)
          if (sizeInBytes / (1024.0 * 1024.0) > 50) {
            _isUploadingFile.value = false
            onFailure("File you are trying to upload is too big!")
            return false
          }
        } else {
          _isUploadingFile.value = false
          onFailure("Failed to get the size of the file!")
          return true
        }
      }
    }
    return false
  }
}
