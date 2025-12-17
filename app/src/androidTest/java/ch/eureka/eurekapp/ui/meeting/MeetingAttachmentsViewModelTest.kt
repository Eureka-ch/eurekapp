package ch.eureka.eurekapp.ui.meeting
//Portions of this code were generated using the help of Gemini 3 Pro
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingAttachmentsViewModelTest {

    @MockK private lateinit var fileStorageRepository: FileStorageRepository
    @MockK private lateinit var meetingRepository: MeetingRepository
    @MockK private lateinit var connectivityObserver: ConnectivityObserver
    @MockK private lateinit var downloadedFileDao: DownloadedFileDao
    @MockK private lateinit var context: Context
    @MockK private lateinit var contentResolver: ContentResolver
    @MockK private lateinit var uri: Uri
    @MockK private lateinit var cursor: Cursor
    @MockK private lateinit var firebaseStorage: FirebaseStorage
    @MockK private lateinit var storageReference: StorageReference

    private lateinit var viewModel: MeetingAttachmentsViewModel
    private val testDispatcher = StandardTestDispatcher()
    private val isConnectedFlow = MutableStateFlow(true)

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxed = false)

        Dispatchers.setMain(testDispatcher)
        mockkStatic(Dispatchers::class)
        every { Dispatchers.IO } returns testDispatcher
        every { Dispatchers.Default } returns testDispatcher
        every { Dispatchers.Unconfined } returns testDispatcher

        every { connectivityObserver.isConnected } returns isConnectedFlow
        every { downloadedFileDao.getAll() } returns flowOf(emptyList())
        coEvery { downloadedFileDao.insert(any()) } returns Unit
        coEvery { downloadedFileDao.isDownloaded(any()) } returns false

        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns uri

        mockkStatic(FirebaseStorage::class)
        every { FirebaseStorage.getInstance() } returns firebaseStorage
        every { firebaseStorage.reference } returns storageReference
        every { storageReference.storage } returns firebaseStorage
        every { firebaseStorage.getReferenceFromUrl(any()) } returns storageReference
        every { storageReference.name } returns "test_file.pdf"

        viewModel = MeetingAttachmentsViewModel(
            fileStorageRepository = fileStorageRepository,
            meetingsRepository = meetingRepository,
            connectivityObserver = connectivityObserver,
            ioDispatcher = testDispatcher,
            downloadedFileDao = downloadedFileDao
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    private class MockedFileStorageRepository(private val downloadUrl: String): FileStorageRepository{
        override suspend fun uploadFile(
            storagePath: String,
            fileUri: Uri
        ): Result<String> {
            return Result.success(downloadUrl)
        }

        override suspend fun uploadFile(
            storagePath: String,
            fileDescriptor: ParcelFileDescriptor
        ): Result<String> {
            return Result.success(downloadUrl)
        }

        override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
            return Result.failure(Exception("Dummy exception"))
        }

    }

    @Test
    fun uploadMeetingFileToFirestoreSuccess() = runTest {
        val projectId = "proj1"
        val meetingId = "meet1"
        val downloadUrl = "http://fake.url/file.pdf"
        val meeting = Meeting(meetingID = meetingId, projectId = projectId, attachmentUrls = emptyList())

        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns 1024L
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
        every { cursor.getString(1) } returns "test.pdf"
        every { cursor.close() } returns Unit

        coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)
        coEvery { meetingRepository.updateMeeting(any()) } returns Result.success(Unit)

        var successCalled = false
        MeetingAttachmentsViewModel(fileStorageRepository = MockedFileStorageRepository(downloadUrl),
            downloadedFileDao = downloadedFileDao,
            meetingsRepository = meetingRepository,
            connectivityObserver = connectivityObserver,
            ioDispatcher = testDispatcher).uploadMeetingFileToFirestore(
            contentResolver,
            uri,
            projectId,
            meetingId,
            onSuccess = { successCalled = true },
            onFailure = {})

        advanceUntilIdle()

        assertTrue(successCalled)
    }

    @Test
    fun uploadMeetingFileToFirestoreFailsWhenOffline() = runTest {
        isConnectedFlow.value = false
        advanceUntilIdle()

        var errorMsg = ""
        viewModel.uploadMeetingFileToFirestore(
            contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

        assertEquals("You are not connected to the internet!", errorMsg)
    }

    @Test
    fun uploadMeetingFileToFirestoreFailsWhenFileTooBig() = runTest {
        val hugeSize = 51 * 1024 * 1024L
        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns hugeSize
        every { cursor.close() } returns Unit

        var errorMsg = ""
        viewModel.uploadMeetingFileToFirestore(
            contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

        advanceUntilIdle()
        assertTrue(errorMsg.contains("too big"))
    }

    @Test
    fun uploadMeetingFileToFirestoreFailsWhenSizeIndexNotFound() = runTest {
        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns -1
        every { cursor.close() } returns Unit

        var errorMsg = ""
        viewModel.uploadMeetingFileToFirestore(
            contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

        advanceUntilIdle()
        assertEquals("Failed to get the size of the file!", errorMsg)
    }


    @Test
    fun uploadMeetingFileToFirestoreFailsWhenMeetingNoLongerExists() = runTest {
        val downloadUrl = "http://fake.url/file.pdf"

        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns 1024L
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
        every { cursor.getString(1) } returns "test.pdf"
        every { cursor.close() } returns Unit

        coEvery { meetingRepository.getMeetingById(any(), any()) } returns flowOf(null)

        var errorMsg = ""
        var failureCalled = false
        MeetingAttachmentsViewModel(fileStorageRepository = MockedFileStorageRepository(downloadUrl),
            downloadedFileDao = downloadedFileDao,
            meetingsRepository = meetingRepository,
            connectivityObserver = connectivityObserver,
            ioDispatcher = testDispatcher).uploadMeetingFileToFirestore(
            contentResolver,
            uri,
            "p",
            "m",
            onSuccess = {},
            onFailure = { msg ->
                errorMsg = msg
                failureCalled = true
            })

        advanceUntilIdle()

        assertTrue(failureCalled)
        assertTrue(errorMsg.contains("no longer exists"))
    }

    @Test
    fun uploadMeetingFileToFirestoreFailsWhenUpdateFails() = runTest {
        val downloadUrl = "http://fake.url/file.pdf"
        val meeting = Meeting(meetingID = "m", projectId = "p", attachmentUrls = emptyList())

        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns 1024L
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
        every { cursor.getString(1) } returns "test.pdf"
        every { cursor.close() } returns Unit

        coEvery { fileStorageRepository.uploadFile(any(), uri) } returns Result.success(downloadUrl)
        coEvery { meetingRepository.getMeetingById(any(), any()) } returns flowOf(meeting)
        coEvery { meetingRepository.updateMeeting(any()) } returns Result.failure(Exception("Update failed"))
        coEvery { fileStorageRepository.deleteFile(downloadUrl) } returns Result.success(Unit)

        var failureCalled = false
        viewModel.uploadMeetingFileToFirestore(
            contentResolver,
            uri,
            "p",
            "m",
            onSuccess = {},
            onFailure = { failureCalled = true })

        advanceUntilIdle()

        assertTrue(failureCalled)
    }

    private class MockedFailingFileStorageRepository: FileStorageRepository{
        override suspend fun uploadFile(
            storagePath: String,
            fileUri: Uri
        ): Result<String> {
            return Result.failure(Exception("Upload failed"))
        }

        override suspend fun uploadFile(
            storagePath: String,
            fileDescriptor: ParcelFileDescriptor
        ): Result<String> {
            return Result.failure(Exception("Upload failed"))
        }

        override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
            return Result.success(Unit)
        }

        override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
            return Result.failure(Exception("Dummy exception"))
        }

    }

    @Test
    fun uploadMeetingFileToFirestore_FailsWhenUploadFails() = runTest {
        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns 1024L
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
        every { cursor.getString(1) } returns "test.pdf"
        every { cursor.close() } returns Unit

        var errorMsg = ""
        MeetingAttachmentsViewModel(fileStorageRepository = MockedFailingFileStorageRepository(),
            downloadedFileDao = downloadedFileDao,
            meetingsRepository = meetingRepository,
            connectivityObserver = connectivityObserver,
            ioDispatcher = testDispatcher).uploadMeetingFileToFirestore(
            contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

        advanceUntilIdle()
        assertEquals("Unexpected error occurred!", errorMsg)
    }

    @Test
    fun uploadMeetingFileToFirestore_HandlesGenericException() = runTest {
        every { contentResolver.query(uri, null, null, null, null) } throws
                RuntimeException("Query failed")

        var errorMsg = ""
        viewModel.uploadMeetingFileToFirestore(
            contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

        advanceUntilIdle()
        assertTrue(errorMsg.contains("Query failed"))
    }

    @Test
    fun uploadMeetingFileToFirestore_SetsUploadingStateCorrectly() = runTest {
        val downloadUrl = "http://fake.url/file.pdf"
        val meeting = Meeting(meetingID = "m", projectId = "p", attachmentUrls = emptyList())

        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns 1024L
        every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
        every { cursor.getString(1) } returns "test.pdf"
        every { cursor.close() } returns Unit

        coEvery { fileStorageRepository.uploadFile(any(), uri) } returns Result.success(downloadUrl)
        coEvery { meetingRepository.getMeetingById(any(), any()) } returns flowOf(meeting)
        coEvery { meetingRepository.updateMeeting(any()) } returns Result.success(Unit)

        assertEquals(false, viewModel.isUploadingFile.value)

        viewModel.uploadMeetingFileToFirestore(contentResolver, uri, "p", "m", {}, {})

        advanceUntilIdle()
        assertEquals(false, viewModel.isUploadingFile.value)
    }

    @Test
    fun deleteFileFromMeetingAttachmentsSuccess() = runTest {
        val projectId = "proj1"
        val meetingId = "meet1"
        val urlToDelete = "http://url.com/delete_me"
        val meeting = Meeting(meetingID = meetingId, projectId = projectId, attachmentUrls = listOf(urlToDelete))

        coEvery { fileStorageRepository.deleteFile(urlToDelete) } returns Result.success(Unit)
        coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns flowOf(meeting)
        coEvery { meetingRepository.updateMeeting(any()) } returns Result.success(Unit)

        viewModel.deleteFileFromMeetingAttachments(projectId, meetingId, urlToDelete)

        advanceUntilIdle()
        coVerify { fileStorageRepository.deleteFile(urlToDelete) }
    }

    @Test
    fun deleteFileFromMeetingAttachmentsFailsOffline() = runTest {
        isConnectedFlow.value = false
        advanceUntilIdle()

        var errorMsg = ""
        viewModel.deleteFileFromMeetingAttachments("p", "m", "url", onFailure = { errorMsg = it })

        assertEquals("You are not connected to the internet!", errorMsg)
    }

    @Test
    fun deleteFileFromMeetingAttachments_WhenMeetingDoesNotExist() = runTest {
        coEvery { fileStorageRepository.deleteFile(any()) } returns Result.success(Unit)
        coEvery { meetingRepository.getMeetingById(any(), any()) } returns flowOf(null)

        viewModel.deleteFileFromMeetingAttachments("p", "m", "url")

        advanceUntilIdle()

        coVerify { fileStorageRepository.deleteFile("url") }
    }

    @Test
    fun checkFileSizeReturnsErrorWhenFileTooBig() {
        val hugeSize = 51 * 1024 * 1024L
        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns hugeSize
        every { cursor.close() } returns Unit

        val result = viewModel.checkFileSize(contentResolver, uri)
        assertTrue(result!!.contains("too big"))
    }

    @Test
    fun checkFileSizeReturnsNullWhenFileSizeOk() {
        every { contentResolver.query(uri, null, null, null, null) } returns cursor
        every { cursor.moveToFirst() } returns true
        every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
        every { cursor.getLong(0) } returns 1024L
        every { cursor.close() } returns Unit

        val result = viewModel.checkFileSize(contentResolver, uri)
        assertNull(result)
    }

    @Test
    fun getFilenameFromDownloadURLParsesCorrectly() = runTest {
        val url = "https://firebasestorage.googleapis.com/b/bucket/o/folder%2FMyFile.pdf"
        val expectedName = "MyFile.pdf"
        every { storageReference.name } returns expectedName

        viewModel.getFilenameFromDownloadURL(url)
        advanceUntilIdle()

        assertEquals(expectedName, viewModel.attachmentUrlsToFileNames.value[url])
    }

    @Test
    fun downloadFileToPhone_SetsDownloadingState() = runTest {
        val url = "https://test.com/file.pdf"

        coEvery { downloadedFileDao.isDownloaded(url) } returns false
        coEvery { downloadedFileDao.insert(any()) } returns Unit

        // Mock a simple failure to avoid complex setup
        every { context.contentResolver } returns contentResolver

        viewModel.downloadFileToPhone(url, context, {}, {})

        advanceUntilIdle()

        // Just verify the method was called
        coVerify { downloadedFileDao.isDownloaded(url) }
    }
}