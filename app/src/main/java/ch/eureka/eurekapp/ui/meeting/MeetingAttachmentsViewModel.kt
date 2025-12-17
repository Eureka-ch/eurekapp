package ch.eureka.eurekapp.ui.meeting
// Portions of this code were generated with the help of Gemini 3 Pro.

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.downloads.DownloadService
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MeetingAttachmentsViewModel(
    private val fileStorageRepository: FileStorageRepository = RepositoriesProvider.fileRepository,
    private val meetingsRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
    private val connectivityObserver: ConnectivityObserver =
        ConnectivityObserverProvider.connectivityObserver,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val downloadedFileDao: DownloadedFileDao
) : ViewModel() {

  val downloadedFiles =
      downloadedFileDao
          .getAll()
          .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _isUploadingFile = MutableStateFlow(false)
  val isUploadingFile = _isUploadingFile.asStateFlow()

  private val _isConnected =
      connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

  private val _attachmentUrlsToFileNames: MutableStateFlow<Map<String, String>> =
      MutableStateFlow(mapOf())
  val attachmentUrlsToFileNames = _attachmentUrlsToFileNames.asStateFlow()

  private val _downloadingFileStateUrlToBoolean = MutableStateFlow<Map<String, Boolean>>(mapOf())
  val downloadingFileStateUrlToBoolean = _downloadingFileStateUrlToBoolean.asStateFlow()

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

    viewModelScope.launch {
      withContext(ioDispatcher) {
        _isUploadingFile.value = true
        launchIO {
          try {
            checkFileSize(contentResolver, uri)?.let {
              onFailure(it)
              return@launchIO
            }

            val fileName = getFileNameFromUri(uri, contentResolver)
            checkNotNull(fileName)

            val uploadResult =
                fileStorageRepository.uploadFile(
                    StoragePaths.meetingAttachmentPath(projectId, meetingId, fileName), uri)

            val downloadUrl = uploadResult.getOrNull()
            if (!(uploadResult.isSuccess && downloadUrl != null)) {
              onFailure("Unexpected error occurred!")
              return@launchIO
            }

            val meeting = meetingsRepository.getMeetingById(projectId, meetingId).first()

            if (meeting == null) {
              deleteFileFromMeetingAttachments(projectId, meetingId, downloadUrl)
              onFailure("Meeting whose attachment you want no longer exists!")
              return@launchIO
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
  }

  fun checkFileSize(contentResolver: ContentResolver, uri: Uri): String? {
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

  fun downloadFileToPhone(
      url: String,
      context: Context,
      onSuccess: () -> Unit,
      onFailure: (String) -> Unit
  ) {
    viewModelScope.launch {
      if (downloadedFileDao.isDownloaded(url)) {
        return@launch
      }
      val displayName = getFileNameFromDownloadURLSuspending(url)
      _downloadingFileStateUrlToBoolean.value += url to true
      val downloadService = DownloadService(context)
      val actualDisplayName =
          if (displayName == null || displayName.isBlank()) url.substringAfterLast("/")
          else displayName
      val result = downloadService.downloadFile(url, actualDisplayName)
      result
          .onSuccess { uri ->
            downloadedFileDao.insert(
                DownloadedFile(url = url, localPath = uri.toString(), fileName = actualDisplayName))
            _downloadingFileStateUrlToBoolean.value += url to false
          }
          .onFailure {
            _downloadingFileStateUrlToBoolean.value += url to false
            onFailure("Failed to download file unexpected error")
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
    launchIO {
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

  fun getFilenameFromDownloadURL(downloadUrl: String) {
    launchIO {
      runCatching {
        val storageRef = FirebaseStorage.getInstance().reference
        val linkReference = storageRef.storage.getReferenceFromUrl(downloadUrl)
        _attachmentUrlsToFileNames.value += downloadUrl to linkReference.name
      }
    }
  }

  private suspend fun getFileNameFromDownloadURLSuspending(downloadUrl: String): String? {
    val storageRef = FirebaseStorage.getInstance().reference
    val linkReference = storageRef.storage.getReferenceFromUrl(downloadUrl)
    _attachmentUrlsToFileNames.value += downloadUrl to linkReference.name
    return linkReference.name
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

  private fun ViewModel.launchIO(block: suspend () -> Unit) {
    viewModelScope.launch { withContext(ioDispatcher) { block() } }
  }
}
