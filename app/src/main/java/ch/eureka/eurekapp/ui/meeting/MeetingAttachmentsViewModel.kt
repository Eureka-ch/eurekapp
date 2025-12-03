package ch.eureka.eurekapp.ui.meeting

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.StoragePaths
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
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
import java.io.IOException


class MeetingAttachmentsViewModel(
    private val fileStorageRepository: FileStorageRepository = RepositoriesProvider.fileRepository,
    private val meetingsRepository: MeetingRepository = RepositoriesProvider.meetingRepository,
    private val connectivityObserver: ConnectivityObserver = ConnectivityObserverProvider.connectivityObserver
): ViewModel() {
    private val _isDownloadingFile = MutableStateFlow(false)
    val isDownloadingFile = _isDownloadingFile.asStateFlow()

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
                                deleteFileFromMeetingAttachments(
                                    downloadUrlResult.getOrNull()!!)
                                onFailure("Unexpected error occurred!")
                            }
                        }else{
                            deleteFileFromMeetingAttachments(downloadUrlResult.getOrNull()!!)
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

    fun deleteFileFromMeetingAttachments(meeting: Meeting, downloadUrl: String){
        viewModelScope.launch {
            fileStorageRepository.deleteFile(downloadUrl)
            val updatedMeeting = meeting.copy(attachmentUrls = meeting.attachmentUrls - downloadUrl)
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

                    //Set that we are downloading a file:
                    _isDownloadingFile.value = true

                    linkReference.getFile(fileUri).addOnCompleteListener {
                        contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                        resolver.update(fileUri, contentValues, null, null)
                        _isDownloadingFile.value = false
                        onSuccess()
                    }.addOnFailureListener {
                        resolver.delete(fileUri, null, null)
                        _isDownloadingFile.value = false
                        onFailure("Failed to download the file")
                    }

                }catch (e: Exception){
                    onFailure("Failed to download the file: ${e.message}")
                }
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
}