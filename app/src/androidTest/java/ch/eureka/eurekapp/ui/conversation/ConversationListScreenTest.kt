package ch.eureka.eurekapp.ui.conversation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.conversation.Conversation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * UI tests for ConversationListScreen.
 *
 * Tests verify screen layout, empty state, conversation display, and FAB interaction.
 */
@RunWith(AndroidJUnit4::class)
class ConversationListScreenTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getInstrumentation().targetContext
    ConnectivityObserverProvider.initialize(context)
  }

  @Test
  fun conversationListScreen_fabTriggersCreateCallback() {
    // Arrange: Track if callback was triggered
    var createClicked = false

    composeTestRule.setContent {
      ConversationListScreen(
          onCreateConversation = { createClicked = true },
          onConversationClick = {},
          viewModel = createMockViewModel(emptyList()))
    }

    // Act: Click the FAB
    composeTestRule.onNodeWithTag(ConversationListScreenTestTags.CREATE_BUTTON).performClick()

    // Assert: Callback was invoked
    assertTrue(createClicked)
  }

  @Test
  fun conversationListScreen_cardClickTriggersCallback() {
    // Arrange: Track which conversation was clicked
    var clickedId: String? = null
    val conversations =
        listOf(
            ConversationDisplayData(
                conversation = Conversation(conversationId = "conv123", projectId = "p1"),
                otherMemberName = "Jane Smith",
                otherMemberPhotoUrl = "",
                projectName = "Project X"))

    composeTestRule.setContent {
      ConversationListScreen(
          onCreateConversation = {},
          onConversationClick = { clickedId = it },
          viewModel = createMockViewModel(conversations))
    }

    // Act: Click on the conversation card
    composeTestRule.onNodeWithTag(ConversationCardTestTags.CONVERSATION_CARD).performClick()

    // Assert: Correct conversation ID was passed to callback
    assertTrue(clickedId == "conv123")
  }

  /** Helper to create a mock ViewModel with predefined state */
  private fun createMockViewModel(
      conversations: List<ConversationDisplayData>
  ): ConversationListViewModel {
    return object : ConversationListViewModel() {
      override val uiState =
          kotlinx.coroutines.flow.MutableStateFlow(
              ConversationListState(
                  conversations = conversations, isLoading = false, isConnected = true))
    }
  }
}
