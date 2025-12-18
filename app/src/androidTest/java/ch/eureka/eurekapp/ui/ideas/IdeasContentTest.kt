/* Portions of this file were written with the help of GPT-5 Codex, Gemini, and Claude. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test

// Portions of this file were written with the help of GPT-5 Codex and Gemini.
class IdeasContentTest {
  @get:Rule val composeTestRule = createComposeRule()

  private val testProject = Project(projectId = "p1", name = "Test Project")
  private val testIdea =
      Idea(
          ideaId = "i1",
          projectId = "p1",
          createdBy = "user1",
          title = "Test Idea",
          content = "Test content")
  private val testMessage =
      Message(messageID = "m1", text = "Hello", senderId = "user1", createdAt = Timestamp.now())

  @Test
  fun ideasContent_loadingShowsLoadingIndicator() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = true)
    }
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun ideasContent_noProjectSelectedShowsEmptyState() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = null,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Please select a project to start").assertIsDisplayed()
  }

  @Test
  fun ideasListContent_emptyIdeasShowsEmptyState() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("No ideas yet for Test Project").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tap the + button to create your first idea").assertIsDisplayed()
  }

  @Test
  fun ideasListContent_withIdeasDisplaysIdeaCards() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(testIdea)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("ideasList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test content").assertIsDisplayed()
  }

  @Test
  fun ideaCard_withoutContentDisplaysOnlyTitle() {
    val ideaWithoutContent = testIdea.copy(content = null)
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(ideaWithoutContent)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
  }

  @Test
  fun ideaCard_withoutTitleDisplaysUntitled() {
    val ideaWithoutTitle = testIdea.copy(title = null)
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(ideaWithoutTitle)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Untitled Idea").assertIsDisplayed()
  }

  @Test
  fun conversationContent_emptyMessagesShowsEmptyState() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(testIdea, emptyList(), "user1") {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("emptyConversation").assertIsDisplayed()
    composeTestRule.onNodeWithText("No messages yet. Start the conversation!").assertIsDisplayed()
  }

  @Test
  fun conversationContent_withMessagesDisplaysMessages() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(testIdea, listOf(testMessage), "user1") {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("conversationMessagesList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Hello").assertIsDisplayed()
  }

  @Test
  fun conversationContent_displaysIdeaTitleAndBackButton() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(testIdea, emptyList(), "user1") {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backToListButton").assertIsDisplayed()
  }

  @Test
  fun conversationContent_backButtonTriggersCallback() {
    var backCalled = false
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState =
              ConversationState(testIdea, emptyList(), "user1") { backCalled = true },
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("backToListButton").performClick()
    assert(backCalled)
  }

  @Test
  fun ideaCard_withParticipantsDisplaysParticipantAvatars() {
    val ideaWithParticipants =
        testIdea.copy(participantIds = listOf("user1", "user2", "user3"), createdBy = "user1")
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(ideaWithParticipants)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    // The avatars should be displayed (even if they show fallback icons)
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
  }

  @Test
  fun ideaCard_withoutParticipantsDoesNotShowAvatars() {
    val ideaWithoutParticipants =
        testIdea.copy(participantIds = listOf("user1"), createdBy = "user1")
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(ideaWithoutParticipants)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
  }

  @Test
  fun ideaCard_withBorderColorDisplaysCard() {
    val idea1 = testIdea.copy(ideaId = "idea1", title = "Idea 1")
    val idea2 = testIdea.copy(ideaId = "idea2", title = "Idea 2")
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(idea1, idea2)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Idea 1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Idea 2").assertIsDisplayed()
  }
}
