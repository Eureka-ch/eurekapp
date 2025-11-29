package ch.eureka.eurekapp.ui.conversation

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.ui.components.MessageBubbleTestTags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

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
          viewModel = createMockViewModel(isLoading = true), onNavigateBack = {})
    }
    composeTestRule
        .onNodeWithTag(ConversationDetailScreenTestTags.LOADING_INDICATOR)
        .assertIsDisplayed()
  }

  @Test
  fun conversationDetailScreen_showsEmptyState() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          viewModel = createMockViewModel(messages = emptyList()), onNavigateBack = {})
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
          viewModel = createMockViewModel(messages = messages), onNavigateBack = {})
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
          viewModel = createMockViewModel(), onNavigateBack = { backClicked = true })
    }
    composeTestRule.onNodeWithTag(ConversationDetailScreenTestTags.BACK_BUTTON).performClick()
    assertTrue(backClicked)
  }

  @Test
  fun conversationDetailScreen_showsFallbackTitleWhenNameEmpty() {
    composeTestRule.setContent {
      ConversationDetailScreen(
          viewModel = createMockViewModel(otherMemberName = ""), onNavigateBack = {})
    }
    composeTestRule.onNodeWithText("Chat").assertIsDisplayed()
  }

  private fun createMockViewModel(
      messages: List<ConversationMessage> = emptyList(),
      otherMemberName: String = "Test User",
      projectName: String = "Test Project",
      isLoading: Boolean = false,
      isConnected: Boolean = true
  ): ConversationDetailViewModel {
    return object :
        ConversationDetailViewModel(
            conversationId = "test-conv", getCurrentUserId = { currentUserId }) {
      override val uiState: StateFlow<ConversationDetailState> =
          MutableStateFlow(
              ConversationDetailState(
                  messages = messages,
                  otherMemberName = otherMemberName,
                  projectName = projectName,
                  isLoading = isLoading,
                  isConnected = isConnected))

      override val currentUserId: String?
        get() = this@ConversationDetailScreenTest.currentUserId
    }
  }
}
