package ch.eureka.eurekapp.ui.conversation

import android.net.Uri
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.ui.components.DeleteConfirmationDialogTestTags
import ch.eureka.eurekapp.ui.components.MessageActionMenuTestTags
import ch.eureka.eurekapp.ui.components.MessageBubbleTestTags
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

@RunWith(AndroidJUnit4::class)
class ConversationDetailScreenTest {

  @get:Rule val composeTestRule = createComposeRule()
  private val currentUserId = "currentUser123"

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @Test
  fun conversationDetailScreen_showsLoadingState() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(isLoading = true),
          onNavigateBack = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_showsEmptyState() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(messages = emptyList()),
          onNavigateBack = {})
    }
    composeTestRule.onNodeWithTag(ConversationDetailScreenTestTags.EMPTY_STATE).assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_showsMessages() {
    val messages =
        listOf(
            ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"),
            ConversationMessage(messageId = "msg2", senderId = "otherUser", text = "Hi there!"))

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(messages = messages),
          onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.MESSAGES_LIST)
        .assertIsDisplayed()
    composeTestRule.onAllNodesWithTag(MessageBubbleTestTags.BUBBLE).assertCountEquals(2)
  }

  @Test
  fun conversationDetailScreen_backButtonTriggersCallback() {
    var backClicked = false
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(),
          onNavigateBack = { backClicked = true })
    }
    composeTestRule.onNodeWithTag(ConversationDetailScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)
  }

  @Test
  fun conversationDetailScreen_showsFallbackTitleWhenNameEmpty() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(otherMemberNames = emptyList()),
          onNavigateBack = {})
    }
    composeTestRule.onNodeWithText("Chat").assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_showsAttachFileButton() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = createMockViewModel(), onNavigateBack = {})
    }
    composeTestRule.onNodeWithContentDescription("Attach file").assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_displaysSelectedFile() {
    val mockUri = Uri.parse("content://test/file.pdf")
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(selectedFileUri = mockUri),
          onNavigateBack = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.SELECTED_FILE_TEXT)
        .assertIsDisplayed()
    composeTestRule.onNodeWithContentDescription("Remove selected file").assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_clearSelectedFile() {
    val mockUri = Uri.parse("content://test/file.pdf")
    val mockConversationRepository =
        mockk<ch.eureka.eurekapp.model.data.conversation.ConversationRepository>()
    val mockUserRepository = mockk<ch.eureka.eurekapp.model.data.user.UserRepository>()
    val mockProjectRepository = mockk<ch.eureka.eurekapp.model.data.project.ProjectRepository>()
    val mockFileStorageRepository =
        mockk<ch.eureka.eurekapp.model.data.file.FileStorageRepository>(relaxed = true)
    val mockConnectivityObserver = mockk<ch.eureka.eurekapp.model.connection.ConnectivityObserver>()

    every { mockConversationRepository.getConversationById(any()) } returns
        kotlinx.coroutines.flow.flowOf(null)
    every { mockConversationRepository.getMessages(any()) } returns
        kotlinx.coroutines.flow.flowOf(emptyList())
    every { mockConnectivityObserver.isConnected } returns kotlinx.coroutines.flow.flowOf(true)
    coEvery { mockConversationRepository.markMessagesAsRead(any()) } returns Result.success(Unit)

    val viewModel =
        ConversationDetailViewModel(
            conversationId = "test-conv",
            conversationRepository = mockConversationRepository,
            userRepository = mockUserRepository,
            projectRepository = mockProjectRepository,
            fileStorageRepository = mockFileStorageRepository,
            getCurrentUserId = { currentUserId },
            connectivityObserver = mockConnectivityObserver)

    viewModel.setSelectedFile(mockUri)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = viewModel, onNavigateBack = {})
    }

    // Verify file is displayed
    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.SELECTED_FILE_TEXT)
        .assertIsDisplayed()

    // Click remove button
    composeTestRule.onNodeWithContentDescription("Remove selected file").performClick()

    // Verify file is removed from UI
    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.SELECTED_FILE_TEXT)
        .assertDoesNotExist()
  }

  @Test
  fun conversationDetailScreen_showsUploadingIndicator() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(isUploadingFile = true),
          onNavigateBack = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.UPLOADING_TEXT)
        .assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_sendsFileWhenFileSelected() {
    val mockUri = Uri.parse("content://test/file.pdf")
    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(selectedFileUri = mockUri),
          onNavigateBack = {})
    }
    // When file is selected, send button should be enabled
    composeTestRule.onNodeWithContentDescription("Send").assertIsEnabled()
  }

  @Test
  fun conversationDetailScreen_sendButtonCallsSendMessageWhenNoFileSelected() {
    val mockViewModel = mockk<ConversationDetailViewModel>(relaxed = true)
    val stateFlow = MutableStateFlow(ConversationDetailState(currentMessage = "Test message"))

    every { mockViewModel.uiState } returns stateFlow
    every { mockViewModel.currentUserId } returns currentUserId
    every { mockViewModel.snackbarMessage } returns MutableStateFlow(null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = mockViewModel, onNavigateBack = {})
    }

    composeTestRule.onNodeWithContentDescription("Send").performClick()

    verify { mockViewModel.sendMessage() }
  }

  @Test
  fun conversationDetailScreen_sendButtonCallsSendFileMessageWhenFileSelected() {
    val mockUri = Uri.parse("content://test/file.pdf")
    val mockViewModel = mockk<ConversationDetailViewModel>(relaxed = true)
    val stateFlow =
        MutableStateFlow(
            ConversationDetailState(
                selectedFileUri = mockUri, currentMessage = "Test file message"))

    every { mockViewModel.uiState } returns stateFlow
    every { mockViewModel.currentUserId } returns currentUserId
    every { mockViewModel.snackbarMessage } returns MutableStateFlow(null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = mockViewModel, onNavigateBack = {})
    }

    composeTestRule.onNodeWithContentDescription("Send").performClick()

    verify { mockViewModel.sendFileMessage(mockUri, any()) }
  }

  private fun createMockViewModel(
      messages: List<ConversationMessage> = emptyList(),
      otherMemberNames: List<String> = listOf("Test User"),
      projectName: String = "Test Project",
      isLoading: Boolean = false,
      isConnected: Boolean = true,
      isUploadingFile: Boolean = false,
      selectedFileUri: Uri? = null,
      editingMessageId: String? = null,
      selectedMessageId: String? = null,
      showDeleteConfirmation: Boolean = false,
      currentMessage: String = ""
  ): ConversationDetailViewModel {
    return object :
        ConversationDetailViewModel(
            conversationId = "test-conv", getCurrentUserId = { currentUserId }) {
      override val uiState: StateFlow<ConversationDetailState> =
          MutableStateFlow(
              ConversationDetailState(
                  messages = messages,
                  otherMemberNames = otherMemberNames,
                  projectName = projectName,
                  isLoading = isLoading,
                  isConnected = isConnected,
                  isUploadingFile = isUploadingFile,
                  selectedFileUri = selectedFileUri,
                  editingMessageId = editingMessageId,
                  selectedMessageId = selectedMessageId,
                  showDeleteConfirmation = showDeleteConfirmation,
                  currentMessage = currentMessage))

      override val currentUserId: String?
        get() = this@ConversationDetailScreenTest.currentUserId
    }
  }

  // --- Edit/Delete Message Tests ---

  @Test
  fun conversationDetailScreen_showsEditingTopBarInEditMode() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel =
              createMockViewModel(
                  messages = messages, editingMessageId = "msg1", currentMessage = "Hello!"),
          onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.EDITING_TOP_BAR)
        .assertIsDisplayed()
    composeTestRule.onNodeWithText("Editing Message").assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_showsCancelButtonInEditMode() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel =
              createMockViewModel(
                  messages = messages, editingMessageId = "msg1", currentMessage = "Hello!"),
          onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_cancelEditButtonCallsCancelEditing() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    val mockViewModel = mockk<ConversationDetailViewModel>(relaxed = true)
    val stateFlow =
        MutableStateFlow(
            ConversationDetailState(
                messages = messages, editingMessageId = "msg1", currentMessage = "Hello!"))

    every { mockViewModel.uiState } returns stateFlow
    every { mockViewModel.currentUserId } returns currentUserId
    every { mockViewModel.snackbarMessage } returns MutableStateFlow(null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = mockViewModel, onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.CANCEL_EDIT_BUTTON)
        .performClick()

    verify { mockViewModel.cancelEditing() }
  }

  @Test
  fun conversationDetailScreen_hidesAttachFileButtonInEditMode() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel =
              createMockViewModel(
                  messages = messages, editingMessageId = "msg1", currentMessage = "Hello!"),
          onNavigateBack = {})
    }

    composeTestRule.onNodeWithContentDescription("Attach file").assertDoesNotExist()
  }

  @Test
  fun conversationDetailScreen_showsDeleteConfirmationDialog() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel =
              createMockViewModel(
                  messages = messages, selectedMessageId = "msg1", showDeleteConfirmation = true),
          onNavigateBack = {})
    }

    composeTestRule.onNodeWithTag(DeleteConfirmationDialogTestTags.DIALOG).assertIsDisplayed()
    composeTestRule.onNodeWithText("Delete Message").assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_confirmDeleteButtonCallsConfirmDelete() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    val mockViewModel = mockk<ConversationDetailViewModel>(relaxed = true)
    val stateFlow =
        MutableStateFlow(
            ConversationDetailState(
                messages = messages, selectedMessageId = "msg1", showDeleteConfirmation = true))

    every { mockViewModel.uiState } returns stateFlow
    every { mockViewModel.currentUserId } returns currentUserId
    every { mockViewModel.snackbarMessage } returns MutableStateFlow(null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = mockViewModel, onNavigateBack = {})
    }

    composeTestRule.onNodeWithTag(DeleteConfirmationDialogTestTags.CONFIRM_BUTTON).performClick()

    verify { mockViewModel.confirmDeleteMessage() }
  }

  @Test
  fun conversationDetailScreen_cancelDeleteButtonCallsCancelDelete() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    val mockViewModel = mockk<ConversationDetailViewModel>(relaxed = true)
    val stateFlow =
        MutableStateFlow(
            ConversationDetailState(
                messages = messages, selectedMessageId = "msg1", showDeleteConfirmation = true))

    every { mockViewModel.uiState } returns stateFlow
    every { mockViewModel.currentUserId } returns currentUserId
    every { mockViewModel.snackbarMessage } returns MutableStateFlow(null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = mockViewModel, onNavigateBack = {})
    }

    composeTestRule.onNodeWithTag(DeleteConfirmationDialogTestTags.CANCEL_BUTTON).performClick()

    verify { mockViewModel.cancelDeleteMessage() }
  }

  @Test
  fun conversationDetailScreen_showsEditedIndicator() {
    val editedMessage =
        ConversationMessage(
            messageId = "msg1",
            senderId = currentUserId,
            text = "Edited message",
            editedAt = Timestamp.now())

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(messages = listOf(editedMessage)),
          onNavigateBack = {})
    }

    // Use unmerged tree because combinedClickable on message bubble merges descendants
    composeTestRule
        .onNodeWithTag(MessageBubbleTestTags.EDITED_INDICATOR, useUnmergedTree = true)
        .assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_doesNotShowEditedIndicatorForUnedited() {
    val message =
        ConversationMessage(
            messageId = "msg1", senderId = currentUserId, text = "Regular message", editedAt = null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(messages = listOf(message)),
          onNavigateBack = {})
    }

    // Use unmerged tree because combinedClickable on message bubble merges descendants
    composeTestRule
        .onNodeWithTag(MessageBubbleTestTags.EDITED_INDICATOR, useUnmergedTree = true)
        .assertDoesNotExist()
  }

  @Test
  fun conversationDetailScreen_sendButtonCallsSaveEditedMessageInEditMode() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    val mockViewModel = mockk<ConversationDetailViewModel>(relaxed = true)
    val stateFlow =
        MutableStateFlow(
            ConversationDetailState(
                messages = messages, editingMessageId = "msg1", currentMessage = "Updated message"))

    every { mockViewModel.uiState } returns stateFlow
    every { mockViewModel.currentUserId } returns currentUserId
    every { mockViewModel.snackbarMessage } returns MutableStateFlow(null)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv", viewModel = mockViewModel, onNavigateBack = {})
    }

    composeTestRule.onNodeWithContentDescription("Send").performClick()

    verify { mockViewModel.saveEditedMessage() }
  }

  @Test
  fun conversationDetailScreen_showsActionMenuForSelectedOwnMessage() {
    val messages =
        listOf(ConversationMessage(messageId = "msg1", senderId = currentUserId, text = "Hello!"))

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel = createMockViewModel(messages = messages, selectedMessageId = "msg1"),
          onNavigateBack = {})
    }

    composeTestRule.onNodeWithTag(MessageActionMenuTestTags.MENU).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MessageActionMenuTestTags.EDIT_OPTION).assertIsDisplayed()
    composeTestRule.onNodeWithTag(MessageActionMenuTestTags.DELETE_OPTION).assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_showsRemoveAttachmentOptionForFileMessage() {
    val fileMessage =
        ConversationMessage(
            messageId = "msg1",
            senderId = currentUserId,
            text = "File",
            isFile = true,
            fileUrl = "https://example.com/file.pdf")

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel =
              createMockViewModel(messages = listOf(fileMessage), selectedMessageId = "msg1"),
          onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(MessageActionMenuTestTags.REMOVE_ATTACHMENT_OPTION)
        .assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_doesNotShowRemoveAttachmentForTextOnlyMessage() {
    val textMessage =
        ConversationMessage(
            messageId = "msg1", senderId = currentUserId, text = "Hello!", isFile = false)

    composeTestRule.setContent {
      ConversationDetailScreen(
          conversationId = "test-conv",
          viewModel =
              createMockViewModel(messages = listOf(textMessage), selectedMessageId = "msg1"),
          onNavigateBack = {})
    }

    composeTestRule
        .onNodeWithTag(MessageActionMenuTestTags.REMOVE_ATTACHMENT_OPTION)
        .assertDoesNotExist()
  }
}
