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
import io.mockk.mockkStatic
import io.mockk.verify
import junit.framework.Assert.assertNotNull
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
import org.junit.Assert.assertNull
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
    val state = viewModel.uiState.first { it.otherMemberNames.isNotEmpty() }

    assertEquals(listOf("Jane Doe"), state.otherMemberNames)
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

    coEvery {
      mockFileStorageRepository.uploadFile(any(), any<android.os.ParcelFileDescriptor>())
    } returns Result.success(downloadUrl)
    coEvery {
      mockConversationRepository.sendFileMessage(conversationId, "Test message", downloadUrl)
    } returns Result.success(ConversationMessage(messageId = "msg1", text = "Test message"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.updateMessage("Test message")
    viewModel.sendFileMessage(testUri, mockContext)
    advanceUntilIdle()

    coVerify { mockFileStorageRepository.uploadFile(any(), any<android.os.ParcelFileDescriptor>()) }
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

    coEvery {
      mockFileStorageRepository.uploadFile(any(), any<android.os.ParcelFileDescriptor>())
    } returns Result.failure(Exception("Upload failed"))

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
    mockkStatic(Uri::class)
    val mockUri = mockk<Uri>(relaxed = true)
    every { Uri.parse(any()) } returns mockUri
    every { mockUri.scheme } returns "https"

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

        coEvery {
          mockFileStorageRepository.uploadFile(any(), any<android.os.ParcelFileDescriptor>())
        } returns Result.success("url")
        coEvery { mockConversationRepository.sendFileMessage(any(), any(), any()) } returns
            Result.success(mockk())

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.sendFileMessage(testUri, mockContext)
        advanceUntilIdle()

        // Verify uploadFile is called with a path containing the default "file" filename
        coVerify {
          mockFileStorageRepository.uploadFile(
              match { it.contains("file_") }, any<android.os.ParcelFileDescriptor>())
        }
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
    every { mockContentResolver.openFileDescriptor(testUri, "r") } returns mockFileDescriptor
    every { mockFileDescriptor.statSize } returns 1024L

    coEvery {
      mockFileStorageRepository.uploadFile(any(), any<android.os.ParcelFileDescriptor>())
    } returns Result.success("url")
    coEvery { mockConversationRepository.sendFileMessage(any(), any(), any()) } returns
        Result.success(mockk())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.sendFileMessage(testUri, mockContext)
    advanceUntilIdle()

    // Verify the cursor methods are called
    verify { mockCursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME) }
    verify { mockCursor.moveToFirst() }
    // Verify upload is called with ParcelFileDescriptor and path containing default filename
    coVerify {
      mockFileStorageRepository.uploadFile(
          match { it.contains("file_") }, any<android.os.ParcelFileDescriptor>())
    }
  }

  // --- Edit/Delete Message Tests ---

  private val testMessage =
      ConversationMessage(messageId = "msg-1", senderId = currentUserId, text = "Test message")

  private val testMessageWithAttachment =
      ConversationMessage(
          messageId = "msg-2",
          senderId = currentUserId,
          text = "File message",
          isFile = true,
          fileUrl = "https://storage.example.com/file.pdf")

  @Test
  fun `selectMessage sets selectedMessageId`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectMessage("msg-1")

    val state = viewModel.uiState.first { it.selectedMessageId == "msg-1" }
    assertEquals("msg-1", state.selectedMessageId)
  }

  @Test
  fun `selectMessage with null clears selection`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectMessage("msg-1")
    viewModel.uiState.first { it.selectedMessageId == "msg-1" }

    viewModel.selectMessage(null)

    val state = viewModel.uiState.first { it.selectedMessageId == null }
    assertNull(state.selectedMessageId)
  }

  @Test
  fun `clearMessageSelection clears selectedMessageId`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectMessage("msg-1")
    viewModel.uiState.first { it.selectedMessageId == "msg-1" }

    viewModel.clearMessageSelection()

    val state = viewModel.uiState.first { it.selectedMessageId == null }
    assertNull(state.selectedMessageId)
  }

  @Test
  fun `startEditing populates editingMessageId and currentMessage`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.startEditing(testMessage)

    val state = viewModel.uiState.first { it.editingMessageId == "msg-1" }
    assertEquals("msg-1", state.editingMessageId)
    assertEquals("Test message", state.currentMessage)
    assertTrue(state.isEditing)
  }

  @Test
  fun `startEditing clears selectedMessageId`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectMessage("msg-1")
    viewModel.uiState.first { it.selectedMessageId == "msg-1" }

    viewModel.startEditing(testMessage)

    val state = viewModel.uiState.first { it.editingMessageId == "msg-1" }
    assertNull(state.selectedMessageId)
  }

  @Test
  fun `cancelEditing clears editing state`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.startEditing(testMessage)
    viewModel.uiState.first { it.editingMessageId == "msg-1" }

    viewModel.cancelEditing()

    val state = viewModel.uiState.first { it.editingMessageId == null && it.currentMessage == "" }
    assertNull(state.editingMessageId)
    assertEquals("", state.currentMessage)
    assertFalse(state.isEditing)
  }

  @Test
  fun `saveEditedMessage calls repository and clears editing state`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.updateMessage(any(), any(), any()) } returns
        Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.startEditing(testMessage)
    viewModel.uiState.first { it.editingMessageId == "msg-1" }

    viewModel.updateMessage("Updated text")
    viewModel.saveEditedMessage()

    val state = viewModel.uiState.first { it.editingMessageId == null && !it.isSending }
    coVerify { mockConversationRepository.updateMessage(conversationId, "msg-1", "Updated text") }
    assertNull(state.editingMessageId)
    assertEquals("", state.currentMessage)
    assertFalse(state.isSending)
  }

  @Test
  fun `saveEditedMessage shows error for empty message`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.startEditing(testMessage)
    viewModel.uiState.first { it.editingMessageId == "msg-1" }

    viewModel.updateMessage("   ") // Empty/whitespace
    viewModel.saveEditedMessage()

    val state = viewModel.uiState.first { it.errorMsg != null }
    assertEquals("Message cannot be empty", state.errorMsg)
    coVerify(exactly = 0) { mockConversationRepository.updateMessage(any(), any(), any()) }
  }

  @Test
  fun `saveEditedMessage shows error for too long message`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.startEditing(testMessage)
    viewModel.uiState.first { it.editingMessageId == "msg-1" }

    viewModel.updateMessage("a".repeat(5001))
    viewModel.saveEditedMessage()

    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("too long") == true)
    coVerify(exactly = 0) { mockConversationRepository.updateMessage(any(), any(), any()) }
  }

  @Test
  fun `saveEditedMessage handles repository failure`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.updateMessage(any(), any(), any()) } returns
        Result.failure(Exception("Update failed"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.startEditing(testMessage)
    viewModel.uiState.first { it.editingMessageId == "msg-1" }
    viewModel.updateMessage("Updated text")
    viewModel.saveEditedMessage()

    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("Update failed") == true)
    assertFalse(state.isSending)
  }

  @Test
  fun `requestDeleteMessage shows confirmation dialog`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.requestDeleteMessage("msg-1")

    val state = viewModel.uiState.first { it.showDeleteConfirmation }
    assertTrue(state.showDeleteConfirmation)
    assertEquals("msg-1", state.selectedMessageId)
  }

  @Test
  fun `confirmDeleteMessage calls repository and clears state`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.deleteMessage(any(), any()) } returns Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.requestDeleteMessage("msg-1")
    viewModel.uiState.first { it.showDeleteConfirmation }

    viewModel.confirmDeleteMessage()

    val state = viewModel.uiState.first { !it.showDeleteConfirmation }
    coVerify { mockConversationRepository.deleteMessage(conversationId, "msg-1") }
    assertFalse(state.showDeleteConfirmation)
    assertNull(state.selectedMessageId)
  }

  @Test
  fun `confirmDeleteMessage deletes file from storage for attachment messages`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockFileStorageRepository.deleteFile(any()) } returns Result.success(Unit)
    coEvery { mockConversationRepository.deleteMessage(any(), any()) } returns Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.requestDeleteMessage("msg-2", "https://storage.example.com/file.pdf")
    viewModel.uiState.first { it.showDeleteConfirmation }

    viewModel.confirmDeleteMessage()
    viewModel.uiState.first { !it.showDeleteConfirmation }

    coVerify { mockFileStorageRepository.deleteFile("https://storage.example.com/file.pdf") }
    coVerify { mockConversationRepository.deleteMessage(conversationId, "msg-2") }
  }

  @Test
  fun `confirmDeleteMessage proceeds even if file deletion fails`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockFileStorageRepository.deleteFile(any()) } returns
        Result.failure(Exception("Storage error"))
    coEvery { mockConversationRepository.deleteMessage(any(), any()) } returns Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.requestDeleteMessage("msg-2", "https://storage.example.com/file.pdf")
    viewModel.uiState.first { it.showDeleteConfirmation }

    viewModel.confirmDeleteMessage()
    viewModel.uiState.first { !it.showDeleteConfirmation }

    // Should still attempt to delete the message even if file deletion fails
    coVerify { mockConversationRepository.deleteMessage(conversationId, "msg-2") }
  }

  @Test
  fun `confirmDeleteMessage handles repository failure`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.deleteMessage(any(), any()) } returns
        Result.failure(Exception("Delete failed"))

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.requestDeleteMessage("msg-1")
    viewModel.uiState.first { it.showDeleteConfirmation }
    viewModel.confirmDeleteMessage()

    val state = viewModel.uiState.first { it.errorMsg != null }
    assertTrue(state.errorMsg?.contains("Delete failed") == true)
  }

  @Test
  fun `cancelDeleteMessage hides confirmation dialog`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.requestDeleteMessage("msg-1")
    viewModel.uiState.first { it.showDeleteConfirmation }

    viewModel.cancelDeleteMessage()

    val state = viewModel.uiState.first { !it.showDeleteConfirmation }
    assertFalse(state.showDeleteConfirmation)
    assertNull(state.selectedMessageId)
  }

  @Test
  fun `removeAttachment removes reference then deletes file`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockConversationRepository.removeAttachment(any(), any()) } returns
        Result.success(Unit)
    coEvery { mockFileStorageRepository.deleteFile(any()) } returns Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.removeAttachment("msg-2", "https://storage.example.com/file.pdf")
    advanceUntilIdle()

    // Firestore reference removed first, then file deleted
    coVerify { mockConversationRepository.removeAttachment(conversationId, "msg-2") }
    coVerify { mockFileStorageRepository.deleteFile("https://storage.example.com/file.pdf") }
  }

  @Test
  fun `removeAttachment shows snackbar if file deletion fails but still removes reference`() =
      runTest {
        every { mockConversationRepository.getConversationById(conversationId) } returns
            flowOf(null)
        every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
        coEvery { mockConversationRepository.removeAttachment(any(), any()) } returns
            Result.success(Unit)
        coEvery { mockFileStorageRepository.deleteFile(any()) } returns
            Result.failure(Exception("Storage error"))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.removeAttachment("msg-2", "https://storage.example.com/file.pdf")
        advanceUntilIdle()

        // Firestore reference is removed first, then file deletion is attempted
        coVerify { mockConversationRepository.removeAttachment(conversationId, "msg-2") }
        coVerify { mockFileStorageRepository.deleteFile("https://storage.example.com/file.pdf") }
        // Snackbar shows partial success message
        val snackbar = viewModel.snackbarMessage.first { it != null }
        assertTrue(snackbar?.contains("file deletion failed") == true)
      }

  @Test
  fun `removeAttachment clears selectedMessageId`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())
    coEvery { mockFileStorageRepository.deleteFile(any()) } returns Result.success(Unit)
    coEvery { mockConversationRepository.removeAttachment(any(), any()) } returns
        Result.success(Unit)

    val viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.selectMessage("msg-2")
    viewModel.uiState.first { it.selectedMessageId == "msg-2" }

    viewModel.removeAttachment("msg-2", "https://storage.example.com/file.pdf")

    val state = viewModel.uiState.first { it.selectedMessageId == null }
    assertNull(state.selectedMessageId)
  }

  @Test
  fun `isEditing returns true when editingMessageId is set`() = runTest {
    every { mockConversationRepository.getConversationById(conversationId) } returns flowOf(null)
    every { mockConversationRepository.getMessages(conversationId) } returns flowOf(emptyList())

    val viewModel = createViewModel()
    advanceUntilIdle()

    assertFalse(viewModel.uiState.value.isEditing)

    viewModel.startEditing(testMessage)

    val state = viewModel.uiState.first { it.isEditing }
    assertTrue(state.isEditing)
  }

  @Test
  fun getUser_returnsUserFlow() = runTest {
    val mockUserRepo = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()
    val mockUser = User(uid = "user1", displayName = "John", email = "john@example.com", photoUrl = "")

    every { mockUserRepo.getUserById("user1") } returns flowOf(mockUser)

    val viewModel = ConversationDetailViewModel(
      conversationId = "conv1",
      conversationRepository = mockk(relaxed = true),
      userRepository = mockUserRepo,
      projectRepository = mockk(relaxed = true),
      fileStorageRepository = mockk(relaxed = true),
      getCurrentUserId = { "currentUser" },
      connectivityObserver = mockk(relaxed = true)
    )

    val result = viewModel.getUser("user1").first()

    assertNotNull(result)
    assertEquals("John", result?.displayName)
  }

  @Test
  fun uiState_withMultipleOtherMembersPopulatesOtherMemberNames() = runTest {
    val mockConversationRepo = mockk<ch.eureka.eurekapp.model.data.conversation.ConversationRepository>()
    val mockUserRepo = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()
    val mockProjectRepo = mockk<ch.eureka.eurekapp.model.data.project.ProjectRepository>()

    val conversation = Conversation(
      conversationId = "conv1",
      projectId = "proj1",
      memberIds = listOf("currentUser", "user1", "user2")
    )

    every { mockConversationRepo.getConversationById("conv1") } returns flowOf(conversation)
    every { mockConversationRepo.getMessages("conv1") } returns flowOf(emptyList())
    every { mockUserRepo.getUserById("user1") } returns flowOf(User("user1", "Alice", "alice@example.com", ""))
    every { mockUserRepo.getUserById("user2") } returns flowOf(User("user2", "Bob", "bob@example.com", ""))
    every { mockProjectRepo.getProjectById("proj1") } returns flowOf(Project(projectId = "proj1", name = "Test Project"))

    val viewModel = ConversationDetailViewModel(
      conversationId = "conv1",
      conversationRepository = mockConversationRepo,
      userRepository = mockUserRepo,
      projectRepository = mockProjectRepo,
      fileStorageRepository = mockk(relaxed = true),
      getCurrentUserId = { "currentUser" },
      connectivityObserver = mockk {
        every { isConnected } returns flowOf(true)
      }
    )

    val state = viewModel.uiState.first { it.otherMemberNames.isNotEmpty() }

    assertEquals(2, state.otherMemberNames.size)
    assertEquals(listOf("Alice", "Bob"), state.otherMemberNames)
  }

  @Test
  fun uiState_withEmptyOtherMembersHandlesGracefully() = runTest {
    val mockConversationRepo = mockk<ch.eureka.eurekapp.model.data.conversation.ConversationRepository>()
    val mockUserRepo = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()
    val mockProjectRepo = mockk<ch.eureka.eurekapp.model.data.project.ProjectRepository>()

    val conversation = Conversation(
      conversationId = "conv1",
      projectId = "proj1",
      memberIds = listOf("currentUser")
    )

    every { mockConversationRepo.getConversationById("conv1") } returns flowOf(conversation)
    every { mockConversationRepo.getMessages("conv1") } returns flowOf(emptyList())
    every { mockProjectRepo.getProjectById("proj1") } returns flowOf(Project(projectId = "proj1", name = "Test Project"))

    val viewModel = ConversationDetailViewModel(
      conversationId = "conv1",
      conversationRepository = mockConversationRepo,
      userRepository = mockUserRepo,
      projectRepository = mockProjectRepo,
      fileStorageRepository = mockk(relaxed = true),
      getCurrentUserId = { "currentUser" },
      connectivityObserver = mockk {
        every { isConnected } returns flowOf(true)
      }
    )

    val state = viewModel.uiState.first()

    assertEquals(emptyList<String>(), state.otherMemberNames)
  }
}
