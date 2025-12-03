package ch.eureka.eurekapp.ui.conversation

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.conversation.Conversation
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.model.data.conversation.ConversationRepository
import ch.eureka.eurekapp.model.data.file.FileStorageRepository
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.user.UserRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

@OptIn(ExperimentalCoroutinesApi::class)
class ConversationDetailViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()
  private lateinit var mockConversationRepository: ConversationRepository
  private lateinit var mockUserRepository: UserRepository
  private lateinit var mockProjectRepository: ProjectRepository
  private lateinit var mockConnectivityObserver: ConnectivityObserver
  private lateinit var mockFileStorageRepository: FileStorageRepository
  private lateinit var mockContext: Context
  private lateinit var mockDownloadManager: DownloadManager
  private val currentUserId = "currentUser123"
  private val conversationId = "conv123"

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockConversationRepository = mockk()
    mockUserRepository = mockk()
    mockProjectRepository = mockk()
    mockConnectivityObserver = mockk()
    mockFileStorageRepository = mockk(relaxed = true)
    mockContext = mockk(relaxed = true)
    mockDownloadManager = mockk<DownloadManager>(relaxed = true)
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
    every { mockContext.getSystemService(Context.DOWNLOAD_SERVICE) } returns mockDownloadManager
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  private fun createViewModel(): ConversationDetailViewModel {
    return ConversationDetailViewModel(
        conversationId = conversationId,
        conversationRepository = mockConversationRepository,
        userRepository = mockUserRepository,
        projectRepository = mockProjectRepository,
        fileStorageRepository = mockFileStorageRepository,
        getCurrentUserId = { currentUserId },
        connectivityObserver = mockConnectivityObserver)
  }

  @Test
  fun conversationDetailViewModel_initialStateIsLoading() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    assertTrue(viewModel.uiState.value.isLoading)
  }

  @Test
  fun conversationDetailViewModel_loadConversationResolvesDisplayDataCorrectly() = runTest {
    val otherUser = User(uid = "otherUser", displayName = "Jane Doe")
    val project = Project(projectId = "project1", name = "Test Project")
    val conversation =
        Conversation(
            conversationId = conversationId,
            projectId = "project1",
            memberIds = listOf(currentUserId, "otherUser"))

    every { mockConversationRepository.getConversationById(conversationId) } returns
        flowOf(conversation)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    every { mockUserRepository.getUserById("otherUser") } returns flowOf(otherUser)
    every { mockProjectRepository.getProjectById("project1") } returns flowOf(project)

    val viewModel = createViewModel()
    val state = viewModel.uiState.first { it.otherMemberName.isNotEmpty() }

    assertEquals("Jane Doe", state.otherMemberName)
    assertEquals("Test Project", state.projectName)
  }

  @Test
  fun conversationDetailViewModel_loadMessagesPopulatesMessagesList() = runTest {
    val messages =
        listOf(
            ConversationMessage(messageId = "msg1", text = "Hello"),
            ConversationMessage(messageId = "msg2", text = "Hi"))

    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(messages)

    val viewModel = createViewModel()
    val state = viewModel.uiState.first { it.messages.isNotEmpty() }

    assertEquals(2, state.messages.size)
  }

  @Test
  fun conversationDetailViewModel_sendMessageSendsAndClearsInput() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.sendMessage(conversationId, "Test") } returns
        Result.success(ConversationMessage(messageId = "new", text = "Test"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.updateMessage("Test")
    viewModel.sendMessage()
    advanceUntilIdle()

    assertEquals("", viewModel.uiState.value.currentMessage)
    coVerify { mockConversationRepository.sendMessage(conversationId, "Test") }
  }

  @Test
  fun conversationDetailViewModel_sendMessageValidatesEmptyAndLongMessages() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.updateMessage("   ")
    viewModel.sendMessage()
    advanceUntilIdle()
    coVerify(exactly = 0) { mockConversationRepository.sendMessage(any(), any()) }

    viewModel.updateMessage("a".repeat(5001))
    viewModel.sendMessage()
    advanceUntilIdle()
    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("too long") == true)
  }

  @Test
  fun conversationDetailViewModel_markAsReadCallsRepository() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.markMessagesAsRead(conversationId) } returns
        Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()
    viewModel.markAsRead()
    advanceUntilIdle()

    coVerify { mockConversationRepository.markMessagesAsRead(conversationId) }
  }

  @Test
  fun conversationDetailViewModel_connectivityStateUpdatesUi() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    every { mockConnectivityObserver.isConnected } returns flowOf(false)

    val viewModel = createViewModel()
    val state = viewModel.uiState.first { !it.isConnected }

    assertFalse(state.isConnected)
  }

  @Test
  fun conversationDetailViewModel_setSelectedFileUpdatesSelectedFileUri() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    val testUri = mockk<Uri>()
    viewModel.setSelectedFile(testUri)
    advanceUntilIdle()

    val state = viewModel.uiState.first { it.selectedFileUri != null }
    assertEquals(testUri, state.selectedFileUri)
  }

  @Test
  fun conversationDetailViewModel_clearSelectedFileClearsSelectedFileUri() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    val testUri = mockk<Uri>()
    viewModel.setSelectedFile(testUri)
    advanceUntilIdle()

    viewModel.clearSelectedFile()
    advanceUntilIdle()

    val state = viewModel.uiState.first { it.selectedFileUri == null }
    assertEquals(null, state.selectedFileUri)
  }

  @Test
  fun conversationDetailViewModel_sendFileMessageUploadsFileAndSendsMessage() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val testUri = mockk<Uri>()
    val downloadUrl = "https://storage.example.com/file.pdf"
    val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
    val mockCursor = mockk<android.database.Cursor>(relaxed = true)
    val mockFileDescriptor = mockk<android.os.ParcelFileDescriptor>(relaxed = true)

    every { mockContext.contentResolver } returns mockContentResolver
    every { mockContentResolver.query(testUri, null, null, null, null) } returns mockCursor
    every { mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns 0
    every { mockCursor.moveToFirst() } returns true
    every { mockCursor.getString(0) } returns "test_file.pdf"
    every { mockContentResolver.openFileDescriptor(testUri, "r") } returns mockFileDescriptor
    every { mockFileDescriptor.statSize } returns 1024L

    coEvery { mockFileStorageRepository.uploadFile(any(), testUri) } returns
        Result.success(downloadUrl)
    coEvery {
      mockConversationRepository.sendFileMessage(conversationId, "Test message", downloadUrl)
    } returns Result.success(ConversationMessage(messageId = "msg1", text = "Test message"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.updateMessage("Test message")
    viewModel.sendFileMessage(testUri, mockContext)
    advanceUntilIdle()

    coVerify { mockFileStorageRepository.uploadFile(any(), testUri) }
    coVerify {
      mockConversationRepository.sendFileMessage(conversationId, "Test message", downloadUrl)
    }

    val state = viewModel.uiState.first { !it.isSending }
    assertEquals("", state.currentMessage)
    assertEquals(null, state.selectedFileUri)
  }

  @Test
  fun conversationDetailViewModel_sendFileMessageValidatesFileSize() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val testUri = mockk<Uri>()
    val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
    val mockFileDescriptor = mockk<android.os.ParcelFileDescriptor>(relaxed = true)

    every { mockContext.contentResolver } returns mockContentResolver
    every { mockContentResolver.openFileDescriptor(testUri, "r") } returns mockFileDescriptor
    every { mockFileDescriptor.statSize } returns 101L * 1024L * 1024L // 101 MB

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.sendFileMessage(testUri, mockContext)
    advanceUntilIdle()

    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("too large") == true)
    assertFalse(state.isSending)
    assertFalse(state.isUploadingFile)
  }

  @Test
  fun conversationDetailViewModel_sendFileMessageHandlesUploadFailure() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val testUri = mockk<Uri>()
    val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
    val mockCursor = mockk<android.database.Cursor>(relaxed = true)
    val mockFileDescriptor = mockk<android.os.ParcelFileDescriptor>(relaxed = true)

    every { mockContext.contentResolver } returns mockContentResolver
    every { mockContentResolver.query(testUri, null, null, null, null) } returns mockCursor
    every { mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns 0
    every { mockCursor.moveToFirst() } returns true
    every { mockCursor.getString(0) } returns "test_file.pdf"
    every { mockContentResolver.openFileDescriptor(testUri, "r") } returns mockFileDescriptor
    every { mockFileDescriptor.statSize } returns 1024L

    coEvery { mockFileStorageRepository.uploadFile(any(), testUri) } returns
        Result.failure(Exception("Upload failed"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.sendFileMessage(testUri, mockContext)
    advanceUntilIdle()

    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("uploading file") == true)
    assertFalse(state.isSending)
    assertFalse(state.isUploadingFile)
  }

  @Test
  fun conversationDetailViewModel_openUrlStartsActivityWithCorrectIntent() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    val testUrl = "https://example.com"
    viewModel.openUrl(testUrl, mockContext)

    verify { mockContext.startActivity(any()) }
  }

  @Test
  fun conversationDetailViewModel_downloadFileEnqueuesDownloadAndShowsSnackbar() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    val testUrl = "https://storage.example.com/file.pdf"
    viewModel.downloadFile(testUrl, mockContext)
    advanceUntilIdle()

    val snackbar = viewModel.snackbarMessage.first { it != null }
    assertEquals("Download started", snackbar)
    verify { mockDownloadManager.enqueue(any()) }
  }

  @Test
  fun conversationDetailViewModel_clearSnackbarMessageClearsSnackbar() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.downloadFile("https://test.com/file.pdf", mockContext)
    advanceUntilIdle()

    viewModel.clearSnackbarMessage()
    advanceUntilIdle()

    assertEquals(null, viewModel.snackbarMessage.value)
    verify { mockDownloadManager.enqueue(any()) }
  }

  @Test
  fun conversationDetailViewModel_sendFileMessage_usesDefaultFilenameWhenQueryReturnsNull() =
      runTest {
        every { mockConversationRepository.getConversationById(conversationId) } returns
            flowOf(null)
        every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

        val testUri = mockk<Uri>()
        val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
        val mockFileDescriptor = mockk<android.os.ParcelFileDescriptor>(relaxed = true)

        every { mockContext.contentResolver } returns mockContentResolver
        every { mockContentResolver.query(testUri, null, null, null, null) } returns null
        every { mockContentResolver.openFileDescriptor(testUri, "r") } returns mockFileDescriptor
        every { mockFileDescriptor.statSize } returns 1024L

        coEvery { mockFileStorageRepository.uploadFile(any(), testUri) } returns
            Result.success("url")
        coEvery { mockConversationRepository.sendFileMessage(any(), any(), any()) } returns
            Result.success(mockk())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sendFileMessage(testUri, mockContext)
        advanceUntilIdle()

        // Verify uploadFile is called with a path containing the default "file" filename
        coVerify { mockFileStorageRepository.uploadFile(match { it.contains("file_") }, testUri) }
      }

  @Test
  fun conversationDetailViewModel_sendFileMessage_handlesCursorMoveToFirstFailure() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val testUri = mockk<Uri>()
    val mockContentResolver = mockk<android.content.ContentResolver>(relaxed = true)
    val mockCursor = mockk<android.database.Cursor>(relaxed = true)
    val mockFileDescriptor = mockk<android.os.ParcelFileDescriptor>(relaxed = true)

    every { mockContext.contentResolver } returns mockContentResolver
    every { mockContentResolver.query(testUri, null, null, null, null) } returns mockCursor
    every { mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) } returns 0
    every { mockCursor.moveToFirst() } returns false // Simulate failure
    every { mockCursor.getString(0) } returns "fallback_name" // Mock getString even if invalid
    every { mockContentResolver.openFileDescriptor(testUri, "r") } returns mockFileDescriptor
    every { mockFileDescriptor.statSize } returns 1024L

    coEvery { mockFileStorageRepository.uploadFile(any(), testUri) } returns Result.success("url")
    coEvery { mockConversationRepository.sendFileMessage(any(), any(), any()) } returns
        Result.success(mockk())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.sendFileMessage(testUri, mockContext)
    advanceUntilIdle()

    // Verify the lines are attempted (cursor methods called), and upload proceeds with the mocked
    // name
    verify { mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) }
    verify { mockCursor.moveToFirst() }
    verify { mockCursor.getString(0) }
    coVerify {
      mockFileStorageRepository.uploadFile(match { it.contains("fallback_name_") }, testUri)
    }
  }
}
