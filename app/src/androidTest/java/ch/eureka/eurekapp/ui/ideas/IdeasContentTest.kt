package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import com.google.firebase.Timestamp
import org.junit.Rule
import org.junit.Test

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
  fun ideasContent_loading_showsLoadingIndicator() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = true)
    }
    composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
  }

  @Test
  fun ideasContent_noProjectSelected_showsEmptyState() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = null,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Please select a project to start").assertIsDisplayed()
  }

  @Test
  fun ideasListContent_emptyIdeas_showsEmptyState() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("No ideas yet for Test Project").assertIsDisplayed()
    composeTestRule.onNodeWithText("Tap the + button to create your first idea").assertIsDisplayed()
  }

  @Test
  fun ideasListContent_withIdeas_displaysIdeaCards() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(testIdea)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("ideasList").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
    composeTestRule.onNodeWithText("Test content").assertIsDisplayed()
  }

  @Test
  fun ideaCard_withoutContent_displaysOnlyTitle() {
    val ideaWithoutContent = testIdea.copy(content = null)
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(ideaWithoutContent)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
  }

  @Test
  fun ideaCard_withoutTitle_displaysUntitled() {
    val ideaWithoutTitle = testIdea.copy(title = null)
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.LIST,
          selectedProject = testProject,
          listState = ListState(listOf(ideaWithoutTitle)) {},
          conversationState = ConversationState(null, emptyList(), null) {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Untitled Idea").assertIsDisplayed()
  }

  @Test
  fun conversationContent_emptyMessages_showsEmptyState() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(testIdea, emptyList(), "user1") {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("emptyConversation").assertIsDisplayed()
    composeTestRule.onNodeWithText("No messages yet. Start the conversation!").assertIsDisplayed()
  }

  @Test
  fun conversationContent_withMessages_displaysMessages() {
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState = ConversationState(testIdea, listOf(testMessage), "user1") {},
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
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
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithText("Test Idea").assertIsDisplayed()
    composeTestRule.onNodeWithTag("backToListButton").assertIsDisplayed()
  }

  @Test
  fun conversationContent_backButton_triggersCallback() {
    var backCalled = false
    composeTestRule.setContent {
      IdeasContent(
          viewMode = IdeasViewMode.CONVERSATION,
          selectedProject = testProject,
          listState = ListState(emptyList()) {},
          conversationState =
              ConversationState(testIdea, emptyList(), "user1") { backCalled = true },
          lazyListState = rememberLazyListState(),
          paddingValues = PaddingValues(0.dp),
          isLoading = false)
    }
    composeTestRule.onNodeWithTag("backToListButton").performClick()
    assert(backCalled)
  }
}
