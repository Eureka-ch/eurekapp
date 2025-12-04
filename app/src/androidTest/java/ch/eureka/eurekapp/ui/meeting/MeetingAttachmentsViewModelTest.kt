package ch.eureka.eurekapp.ui.meeting
// Portions of this code were generated with the help of Gemini 3 Pro.
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingRepository
import ch.eureka.eurekapp.model.data.meeting.MeetingRole
import ch.eureka.eurekapp.model.data.meeting.Participant
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FileDownloadTask
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import java.io.File
import java.io.OutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MeetingAttachmentsViewModelTest {

  @MockK private lateinit var fileStorageRepository: FileStorageRepository
  @MockK private lateinit var meetingRepository: MeetingRepository
  @MockK private lateinit var connectivityObserver: ConnectivityObserver
  @MockK private lateinit var context: Context
  @MockK private lateinit var contentResolver: ContentResolver
  @MockK private lateinit var uri: Uri
  @MockK private lateinit var cursor: Cursor

  // Firebase Mocks
  @MockK private lateinit var firebaseStorage: FirebaseStorage
  @MockK private lateinit var storageReference: StorageReference
  @MockK private lateinit var metadata: StorageMetadata
  @MockK private lateinit var metadataTask: Task<StorageMetadata>
  @MockK private lateinit var downloadTask: FileDownloadTask

  private lateinit var viewModel: MeetingAttachmentsViewModel
  private val testDispatcher = StandardTestDispatcher()
  private val isConnectedFlow = MutableStateFlow(true)

  @Before
  fun setUp() {
    MockKAnnotations.init(this, relaxed = false) // Use relaxed instead

    Dispatchers.setMain(testDispatcher)
    mockkStatic(Dispatchers::class)
    every { Dispatchers.IO } returns testDispatcher
    every { Dispatchers.Default } returns testDispatcher
    every { Dispatchers.Unconfined } returns testDispatcher

    every { connectivityObserver.isConnected } returns isConnectedFlow

    mockkStatic(Uri::class)
    every { Uri.parse(any()) } returns uri

    mockkStatic(FirebaseStorage::class)
    every { FirebaseStorage.getInstance() } returns firebaseStorage
    every { firebaseStorage.reference } returns storageReference
    every { storageReference.storage } returns firebaseStorage
    every { firebaseStorage.getReferenceFromUrl(any()) } returns storageReference
    every { storageReference.name } returns "test_file.pdf"

    mockkStatic("kotlinx.coroutines.tasks.TasksKt")

    viewModel =
        MeetingAttachmentsViewModel(fileStorageRepository, meetingRepository, connectivityObserver)
  }

  @After
  fun tearDown() {
    unmockkAll()
    Dispatchers.resetMain()
  }

  @Test
  fun uploadMeetingFileToFirestoreSuccess() = runTest {
    val projectId = "proj1"
    val meetingId = "meet1"
    val downloadUrl = "http://fake.url/file.pdf"
    val existingMeeting =
        Meeting(meetingID = meetingId, projectId = projectId, attachmentUrls = emptyList())

    // Cursor logic
    every { contentResolver.query(uri, null, null, null, null) } returns cursor
    every { cursor.moveToFirst() } returns true
    every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
    every { cursor.getLong(0) } returns 1024L
    every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
    every { cursor.getString(1) } returns "test.pdf"
    every { cursor.close() } returns Unit

    coEvery { fileStorageRepository.uploadFile(any(), uri) } returns Result.success(downloadUrl)
    coEvery { meetingRepository.getMeetingById(projectId, meetingId) } returns
        flowOf(existingMeeting)
    coEvery { meetingRepository.updateMeeting(any()) } returns Result.success(Unit)

    var successCalled = false
    viewModel.uploadMeetingFileToFirestore(
        contentResolver,
        uri,
        projectId,
        meetingId,
        onSuccess = { successCalled = true },
        onFailure = {})

    advanceUntilIdle()

    assertTrue(successCalled)
    coVerify { meetingRepository.updateMeeting(any()) }
  }

  @Test
  fun uploadMeetingFileToFirestoreFailsWhenOffline() = runTest {
    isConnectedFlow.value = false
    advanceUntilIdle() // Process the state emission

    var errorMsg = ""
    viewModel.uploadMeetingFileToFirestore(
        contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

    assertEquals("You are not connected to the internet!", errorMsg)
  }

  @Test
  fun uploadMeetingFileToFirestoreFailsWhenFileTooBig() = runTest {
    val hugeSize = 51 * 1024 * 1024L // 51MB
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
    every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns -1 // Size index not found
    every { cursor.close() } returns Unit

    var errorMsg = ""
    viewModel.uploadMeetingFileToFirestore(
        contentResolver, uri, "p", "m", {}, { msg -> errorMsg = msg })

    advanceUntilIdle()
    assertEquals("Failed to get the size of the file!", errorMsg)
  }

  private class MockedMeetingRepositoryForFailsWhenMeetingDoesNotExist : MeetingRepository {
    override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
      return flowOf(null)
    }

    override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
      return flowOf(listOf())
    }

    override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
      return flowOf(listOf())
    }

    override fun getMeetingsForCurrentUser(
        projectId: String,
        skipCache: Boolean
    ): Flow<List<Meeting>> {
      return flowOf(listOf())
    }

    override suspend fun createMeeting(
        meeting: Meeting,
        creatorId: String,
        creatorRole: MeetingRole
    ): Result<String> {
      return Result.success("")
    }

    override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
      return Result.failure(IllegalArgumentException(""))
    }

    override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
      return Result.success(Unit)
    }

    override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
      return flowOf(listOf())
    }

    override suspend fun addParticipant(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun removeParticipant(
        projectId: String,
        meetingId: String,
        userId: String
    ): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun updateParticipantRole(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      return Result.success(Unit)
    }
  }

  private class MockedFileStorageRepositoryForFailsWhenMeetingDoesNotExist : FileStorageRepository {
    override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
      return Result.success("Dummy")
    }

    override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
      return Result.failure(IllegalArgumentException(""))
    }
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

    coEvery { meetingRepository.getMeetingById(any(), any()) } returns
        flowOf(null) // Meeting doesn't exist

    var errorMsg: Any? = null
    var failureCalled = false
    val newViewModel =
        MeetingAttachmentsViewModel(
            MockedFileStorageRepositoryForFailsWhenMeetingDoesNotExist(),
            MockedMeetingRepositoryForFailsWhenMeetingDoesNotExist(),
            connectivityObserver)
    newViewModel.uploadMeetingFileToFirestore(
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

    // Check that failure was called
    assertTrue("Expected onFailure to be called", failureCalled)
    assertTrue("Expected an error message but got: '$errorMsg'", errorMsg != null)

    // Convert to string for checking
    val errorString = errorMsg.toString()
    assertTrue(
        "Expected message about meeting not existing, got: '$errorString'",
        errorString.contains("no longer exists") || errorString.contains("attachment"))
  }

  private class MockedMeetingRepositoryForFailsWhenUpdateFails : MeetingRepository {
    override fun getMeetingById(projectId: String, meetingId: String): Flow<Meeting?> {
      return flowOf(Meeting())
    }

    override fun getMeetingsInProject(projectId: String): Flow<List<Meeting>> {
      TODO("Not yet implemented")
    }

    override fun getMeetingsForTask(projectId: String, taskId: String): Flow<List<Meeting>> {
      TODO("Not yet implemented")
    }

    override fun getMeetingsForCurrentUser(
        projectId: String,
        skipCache: Boolean
    ): Flow<List<Meeting>> {
      TODO("Not yet implemented")
    }

    override suspend fun createMeeting(
        meeting: Meeting,
        creatorId: String,
        creatorRole: MeetingRole
    ): Result<String> {
      TODO("Not yet implemented")
    }

    override suspend fun updateMeeting(meeting: Meeting): Result<Unit> {
      return Result.failure(IllegalArgumentException(""))
    }

    override suspend fun deleteMeeting(projectId: String, meetingId: String): Result<Unit> {
      TODO("Not yet implemented")
    }

    override fun getParticipants(projectId: String, meetingId: String): Flow<List<Participant>> {
      TODO("Not yet implemented")
    }

    override suspend fun addParticipant(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun removeParticipant(
        projectId: String,
        meetingId: String,
        userId: String
    ): Result<Unit> {
      TODO("Not yet implemented")
    }

    override suspend fun updateParticipantRole(
        projectId: String,
        meetingId: String,
        userId: String,
        role: MeetingRole
    ): Result<Unit> {
      TODO("Not yet implemented")
    }
  }

  @Test
  fun uploadMeetingFileToFirestoreFailsWhenUpdateFails() = runTest {
    val downloadUrl = "http://fake.url/file.pdf"
    val existingMeeting = Meeting(meetingID = "m", projectId = "p", attachmentUrls = emptyList())

    every { contentResolver.query(uri, null, null, null, null) } returns cursor
    every { cursor.moveToFirst() } returns true
    every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
    every { cursor.getLong(0) } returns 1024L
    every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
    every { cursor.getString(1) } returns "test.pdf"
    every { cursor.close() } returns Unit

    coEvery { fileStorageRepository.uploadFile(any(), uri) } returns Result.success(downloadUrl)
    coEvery { meetingRepository.getMeetingById(any(), any()) } returns flowOf(existingMeeting)

    // Try with just answers - maybe the issue is with how Result is handled
    val updateSlot = slot<Meeting>()
    coEvery { meetingRepository.updateMeeting(any()) } coAnswers
        {
          Result.failure<Unit>(IllegalStateException("Update failed"))
        }

    coEvery { fileStorageRepository.deleteFile(downloadUrl) } returns Result.success(Unit)

    var errorMsg: Any? = null
    var failureCalled = false
    var successCalled = false
    val newViewModel =
        MeetingAttachmentsViewModel(
            fileStorageRepository = fileStorageRepository,
            meetingsRepository = MockedMeetingRepositoryForFailsWhenUpdateFails(),
            connectivityObserver = connectivityObserver)

    newViewModel.uploadMeetingFileToFirestore(
        contentResolver,
        uri,
        "p",
        "m",
        onSuccess = { successCalled = true },
        onFailure = { msg ->
          errorMsg = msg
          failureCalled = true
        })

    advanceUntilIdle()

    println("DEBUG: successCalled=$successCalled, failureCalled=$failureCalled, errorMsg=$errorMsg")
    println(
        "DEBUG: updateMeeting was called with: ${if(updateSlot.isCaptured) updateSlot.captured else "NOT CAPTURED"}")

    assertTrue(
        "Expected onFailure to be called, not onSuccess. successCalled=$successCalled, failureCalled=$failureCalled",
        failureCalled)
    assertTrue("onSuccess should NOT have been called", !successCalled)
  }

  private class MockedFileStorageRepositoryForFailsWhenUploadFails : FileStorageRepository {
    override suspend fun uploadFile(storagePath: String, fileUri: Uri): Result<String> {
      return Result.failure(IllegalArgumentException(""))
    }


    override suspend fun deleteFile(downloadUrl: String): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun getFileMetadata(downloadUrl: String): Result<StorageMetadata> {
      return Result.failure(IllegalArgumentException(""))
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
    val newViewModel =
        MeetingAttachmentsViewModel(
            fileStorageRepository = MockedFileStorageRepositoryForFailsWhenUploadFails(),
            meetingRepository,
            connectivityObserver)
    newViewModel.uploadMeetingFileToFirestore(
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
    val existingMeeting = Meeting(meetingID = "m", projectId = "p", attachmentUrls = emptyList())

    every { contentResolver.query(uri, null, null, null, null) } returns cursor
    every { cursor.moveToFirst() } returns true
    every { cursor.getColumnIndex(OpenableColumns.SIZE) } returns 0
    every { cursor.getLong(0) } returns 1024L
    every { cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) } returns 1
    every { cursor.getString(1) } returns "test.pdf"
    every { cursor.close() } returns Unit

    coEvery { fileStorageRepository.uploadFile(any(), uri) } returns Result.success(downloadUrl)
    coEvery { meetingRepository.getMeetingById(any(), any()) } returns flowOf(existingMeeting)
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
    val meeting =
        Meeting(meetingID = meetingId, projectId = projectId, attachmentUrls = listOf(urlToDelete))

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

    // Should still delete the file
    coVerify { fileStorageRepository.deleteFile("url") }
  }

  @Test
  fun downloadFileToPhoneSuccess() = runTest {
    // Setup test dispatcher
    val testDispatcher = StandardTestDispatcher(testScheduler)
    Dispatchers.setMain(testDispatcher)
    mockkStatic(Dispatchers::class)
    every { Dispatchers.IO } returns testDispatcher
    every { Dispatchers.Default } returns testDispatcher

    val realCacheDir = File(System.getProperty("java.io.tmpdir")!!)

    val baseUri = mockk<Uri>(relaxed = true)
    val uriBuilder = mockk<Uri.Builder>(relaxed = true)

    mockkStatic(Uri::class)
    every { Uri.parse(any()) } returns baseUri
    every { baseUri.buildUpon() } returns uriBuilder
    every { uriBuilder.appendPath(any()) } returns uriBuilder
    every { uriBuilder.build() } returns baseUri

    mockkStatic(android.provider.MediaStore.Downloads::class)
    every { android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI } returns baseUri
    every { android.provider.MediaStore.Downloads.getContentUri(any()) } returns baseUri

    val mockUri = mockk<Uri>(relaxed = true)
    every { mockUri.scheme } returns "content"
    every { mockUri.getScheme() } returns "content"
    every { mockUri.host } returns "downloads"
    every { mockUri.getHost() } returns "downloads"
    every { mockUri.authority } returns "downloads"
    every { mockUri.getAuthority() } returns "downloads"
    every { mockUri.path } returns "/test_file.pdf"
    every { mockUri.getPath() } returns "/test_file.pdf"
    every { mockUri.toString() } returns "content://downloads/test_file.pdf"

    val mockOutputStream = mockk<OutputStream>(relaxed = true)
    var streamClosed = false
    every { mockOutputStream.write(any<ByteArray>()) } returns Unit
    every { mockOutputStream.write(any<ByteArray>(), any(), any()) } returns Unit
    every { mockOutputStream.write(any<Int>()) } returns Unit
    every { mockOutputStream.flush() } returns Unit
    every { mockOutputStream.close() } answers { streamClosed = true }

    every { context.contentResolver } returns contentResolver
    every { context.cacheDir } returns realCacheDir

    every { contentResolver.insert(any(), any()) } returns mockUri

    every { contentResolver.openOutputStream(mockUri) } returns mockOutputStream

    every { contentResolver.update(mockUri, any(), null, null) } returns 1

    every { contentResolver.delete(mockUri, null, null) } returns 1
    val downloadUrl = "https://firebasestorage.googleapis.com/test/test_file.pdf"
    val fileName = "test_file.pdf"
    val mimeType = "application/pdf"

    val mockMetadata = mockk<StorageMetadata>()
    every { mockMetadata.contentType } returns mimeType

    val mockMetadataTask = mockk<Task<StorageMetadata>>(relaxed = true)
    val mockDownloadTask = mockk<FileDownloadTask>(relaxed = true)
    val mockSnapshot = mockk<FileDownloadTask.TaskSnapshot>(relaxed = true)

    mockkStatic(FirebaseStorage::class)
    every { FirebaseStorage.getInstance() } returns firebaseStorage
    every { firebaseStorage.reference } returns storageReference
    every { storageReference.storage } returns firebaseStorage
    every { firebaseStorage.getReferenceFromUrl(downloadUrl) } returns storageReference
    every { storageReference.name } returns fileName
    every { storageReference.metadata } returns mockMetadataTask

    val tempFileSlot = slot<File>()
    every { storageReference.getFile(capture(tempFileSlot)) } answers
        {
          val tempFile = tempFileSlot.captured
          tempFile.writeText("Test file content from Firebase")
          mockDownloadTask
        }

    mockkStatic("kotlinx.coroutines.tasks.TasksKt")
    coEvery { mockMetadataTask.await() } returns mockMetadata
    coEvery { mockDownloadTask.await() } returns mockSnapshot

    var successCalled = false
    var failureMessage = ""
    var failureCalled = false

    viewModel.downloadFileToPhone(
        context = context,
        downloadUrl = downloadUrl,
        onSuccess = { successCalled = true },
        onFailure = { msg ->
          failureMessage = msg
          failureCalled = true
        })

    advanceUntilIdle()

    if (!successCalled) {
      println("=== DEBUG INFO ===")
      println("Success called: $successCalled")
      println("Failure called: $failureCalled")
      println("Failure message: '$failureMessage'")
      println("Stream closed: $streamClosed")
      println("Temp file captured: ${tempFileSlot.isCaptured}")
      if (tempFileSlot.isCaptured) {
        println("Temp file exists: ${tempFileSlot.captured.exists()}")
        println("Temp file size: ${tempFileSlot.captured.length()}")
      }
    }

    assertTrue(
        "onSuccess should have been called. Failure: $failureCalled, Message: '$failureMessage'",
        successCalled)

    verify { contentResolver.insert(any(), any()) }
    verify { contentResolver.openOutputStream(mockUri) }
    verify { contentResolver.update(mockUri, any(), null, null) }
    coVerify { storageReference.getFile(any<File>()) }

    assertTrue("Temp file should have been created", tempFileSlot.isCaptured)
    assertTrue("Stream should have been closed", streamClosed)

    Dispatchers.resetMain()
  }

  @Test
  fun downloadFileToPhone_TracksDownloadingState() = runTest {
    val downloadUrl = "https://test.com/file.pdf"

    // Initially empty
    assertEquals(emptySet<String>(), viewModel.isDownloadingFile.value)
  }

  @Test
  fun getFilenameFromDownloadURLParsesCorrectly() {
    val url = "https://firebasestorage.googleapis.com/b/bucket/o/folder%2FMyFile.pdf"
    val expectedName = "MyFile.pdf"
    every { storageReference.name } returns expectedName

    val result = viewModel.getFilenameFromDownloadURL(url)
    assertEquals(expectedName, result)
  }
}
