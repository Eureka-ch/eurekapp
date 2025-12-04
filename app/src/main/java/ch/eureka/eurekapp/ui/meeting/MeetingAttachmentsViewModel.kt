package ch.eureka.eurekapp.ui.meeting
//Portions of this code were generated with the help of Gemini 3 Pro.
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException


class MeetingAttachmentsViewModel(
    private val fileStorageRepository: FileStorageRepository = RepositoriesProvider.fileRepository,
    private val meetingsRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
    private val connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
): ViewModel() {
    private val _downloadingFilesSet: MutableStateFlow<Set<String>> = MutableStateFlow(setOf())
    val isDownloadingFile = _downloadingFilesSet.asStateFlow()

    private val _isUploadingFile = MutableStateFlow(false)
    val isUploadingFile = _isUploadingFile.asStateFlow()

    private val _isConnected =
        connectivityObserver.isConnected.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    fun uploadMeetingFileToFirestore(contentResolver: ContentResolver, uri: Uri,
                                     projectId: String, meetingId: String, onSuccess: () -> Unit,
                                     onFailure: (String) -> Unit){
        if(!_isConnected.value){
            onFailure("You are not connected to the internet!")
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    _isUploadingFile.value = true
                    //First of all we do not want to exceed 50MB file size so we will check this before
                    //doing anything
                    contentResolver.query(uri,null,null,null,null)?.use { cursor ->
                        if(cursor.moveToFirst()){
                            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                            if(sizeIndex != -1) {
                                val sizeInBytes = cursor.getLong(sizeIndex)
                                if(sizeInBytes / (1024.0*1024.0) > 50){
                                    _isUploadingFile.value = false
                                    onFailure("File you are trying to upload is too big!")
                                    return@withContext
                                }
                            }else{
                                _isUploadingFile.value = false
                                onFailure("Failed to get the size of the file!")
                                return@withContext
                            }
                        }
                    }


                    val fileName = getFileNameFromUri(uri, contentResolver)
                    checkNotNull(fileName)
                    val downloadUrlResult = fileStorageRepository.uploadFile(StoragePaths
                        .meetingAttachmentPath(projectId, meetingId, fileName), uri)
                    if(downloadUrlResult.isSuccess && downloadUrlResult.getOrNull() != null){
                        val meetingToUpdate = meetingsRepository.getMeetingById(projectId, meetingId).first()
                        if(meetingToUpdate != null){
                            val updatedMeeting = meetingToUpdate
                                .copy(attachmentUrls = meetingToUpdate.attachmentUrls
                                        + downloadUrlResult.getOrNull()!!)
                            val updatedMeetingResult = meetingsRepository.updateMeeting(updatedMeeting)
                            if(updatedMeetingResult.isSuccess){
                                onSuccess()
                            }else{
                                deleteFileFromMeetingAttachments(projectId, meetingId,
                                    downloadUrlResult.getOrNull()!!)
                                onFailure("Unexpected error occurred!")
                            }
                        }else{
                            deleteFileFromMeetingAttachments(projectId, meetingId, downloadUrlResult.getOrNull()!!)
                            onFailure("Meeting whose attachment you want no longer exists!")
                        }
                    }else{
                        onFailure("Unexpected error occurred!")
                    }
                }catch (e: Exception){
                    onFailure(e.message.toString())
                }finally {
                    _isUploadingFile.value = false
                }
            }
        }
    }

    fun deleteFileFromMeetingAttachments(projectId: String, meetingId: String, downloadUrl: String, onFailure: (String) -> Unit = {}, onSuccess: () -> Unit = {}){
        if(!_isConnected.value){
            onFailure("You are not connected to the internet!")
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val deletedFile = fileStorageRepository.deleteFile(downloadUrl)
                    val meetingGot = meetingsRepository.getMeetingById(projectId, meetingId).first()
                    if(meetingGot != null && deletedFile.isSuccess){
                        val updatedMeeting = meetingGot.copy(attachmentUrls = meetingGot.attachmentUrls - downloadUrl)
                        meetingsRepository.updateMeeting(updatedMeeting)
                        onSuccess()
                    }else{
                        onFailure("Unexpected error occurred")
                    }
                }catch (e: Exception){
                    onFailure(e.message.toString())
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun downloadFileToPhone(context: Context, downloadUrl: String, onSuccess: () -> Unit, onFailure: (String) -> Unit){
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                try {
                    val storageRef = FirebaseStorage.getInstance().reference
                    val linkReference = storageRef.storage.getReferenceFromUrl(downloadUrl)
                    val fileName = linkReference.name
                    val mimeType = linkReference.metadata.await().contentType ?: "application/octet-stream"


                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                        put(MediaStore.Downloads.MIME_TYPE, mimeType)
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val resolver = context.contentResolver
                    val fileUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                        ?: throw IOException("Failed to create MediaStore entry")

                    val tempFile = File.createTempFile("temp_download", null, context.cacheDir)

                    //Set that we are downloading a file:
                    _downloadingFilesSet.value += downloadUrl

                    linkReference.getFile(tempFile).await()

                    resolver.openOutputStream(fileUri).use { outputStream ->
                        if (outputStream == null){
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

                }catch (e: Exception){
                    _downloadingFilesSet.value -= downloadUrl
                    onFailure("Failed to download the file: ${e.message}")
                }
            }
        }
    }
    fun getFilenameFromDownloadURL(downloadUrl: String): String?{
        val storageRef = FirebaseStorage.getInstance().reference
        val linkReference = storageRef.storage.getReferenceFromUrl(downloadUrl)
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
}