/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.components.MessageBubble
import ch.eureka.eurekapp.ui.components.MessageBubbleState
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Data class to group conversation-related parameters. */
data class ConversationState(
    val selectedIdea: Idea?,
    val messages: List<Message>,
    val currentUserId: String?,
    val onBackToList: () -> Unit
)

/** Data class to group list-related parameters. */
data class ListState(val ideas: List<Idea>, val onIdeaClick: (Idea) -> Unit)

@Composable
private fun IdeaCard(idea: Idea, onIdeaClick: () -> Unit) {
  Card(onClick = onIdeaClick, modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(Spacing.md)) {
      Text(text = idea.title ?: "Untitled Idea", style = MaterialTheme.typography.titleMedium)
      if (idea.content != null) {
        Text(
            text = idea.content,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            modifier = Modifier.padding(top = Spacing.xs))
      }
    }
  }
}

@Composable
fun IdeasContent(
    viewMode: IdeasViewMode,
    selectedProject: Project?,
    listState: ListState,
    conversationState: ConversationState,
    lazyListState: LazyListState,
    paddingValues: PaddingValues,
    isLoading: Boolean
) {
  Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    when {
      isLoading -> {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center).testTag("loadingIndicator"))
      }
      selectedProject == null -> {
        Text(
            text = "Please select a project to start",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(Spacing.lg).testTag("emptyState"))
      }
      viewMode == IdeasViewMode.CONVERSATION && conversationState.selectedIdea != null -> {
        IdeaConversationContent(
            idea = conversationState.selectedIdea,
            messages = conversationState.messages,
            currentUserId = conversationState.currentUserId,
            listState = lazyListState,
            onBackToList = conversationState.onBackToList)
      }
      else -> {
        IdeasListContent(
            ideas = listState.ideas,
            selectedProject = selectedProject,
            onIdeaClick = listState.onIdeaClick)
      }
    }
  }
}

@Composable
private fun IdeasListContent(
    ideas: List<Idea>,
    selectedProject: Project?,
    onIdeaClick: (Idea) -> Unit
) {
  if (ideas.isEmpty()) {
    Column(
        modifier =
            Modifier.fillMaxSize().padding(Spacing.lg).testTag(IdeasScreenTestTags.EMPTY_STATE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Text(
              text = "No ideas yet for ${selectedProject?.name ?: "this project"}",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center)
          Text(
              text = "Tap the + button to create your first idea",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = Spacing.sm))
        }
  } else {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.md).testTag("ideasList"),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          items(items = ideas, key = { it.ideaId }) { idea ->
            IdeaCard(idea = idea, onIdeaClick = { onIdeaClick(idea) })
          }
        }
  }
}

@Composable
private fun IdeaConversationContent(
    idea: Idea,
    messages: List<Message>,
    currentUserId: String?,
    listState: LazyListState,
    onBackToList: () -> Unit
) {
  Column(modifier = Modifier.fillMaxSize()) {
    // Header with back button and idea title
    Row(
        modifier = Modifier.fillMaxWidth().padding(Spacing.md),
        verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onBackToList, modifier = Modifier.testTag("backToListButton")) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to list")
          }
          Column(modifier = Modifier.weight(1f).padding(start = Spacing.sm)) {
            Text(
                text = idea.title ?: "Untitled Idea",
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1)
            if (idea.content != null) {
              Text(
                  text = idea.content,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1)
            }
          }
        }

    // Messages list
    if (messages.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize().testTag("emptyConversation")) {
        Text(
            text = "No messages yet. Start the conversation!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(Spacing.lg))
      }
    } else {
      LazyColumn(
          state = listState,
          modifier = Modifier.weight(1f).fillMaxWidth().testTag("conversationMessagesList"),
          reverseLayout = true,
          verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            items(items = messages, key = { it.messageID }) { message ->
              MessageBubble(
                  state = MessageBubbleState(
                      text = message.text,
                      timestamp = message.createdAt,
                      isFromCurrentUser = message.senderId == currentUserId))
            }
          }
    }
    Spacer(modifier = Modifier.height(Spacing.sm))
    // Message input will be added in separate PR as it requires send functionality
  }
}
