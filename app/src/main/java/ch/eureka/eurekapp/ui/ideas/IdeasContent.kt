/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

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
    ideas: List<Idea>,
    selectedIdea: Idea?,
    messages: List<Message>,
    currentUserId: String?,
    listState: LazyListState,
    paddingValues: PaddingValues,
    isLoading: Boolean,
    onIdeaClick: (Idea) -> Unit,
    onDeleteIdea: (String) -> Unit,
    onShareIdea: (String, String) -> Unit
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
      else -> {
        // Always show list view - conversation mode in separate PR
        IdeasListContent(
            ideas = ideas,
            selectedProject = selectedProject,
            onIdeaClick = onIdeaClick,
            onDeleteIdea = onDeleteIdea,
            onShareIdea = onShareIdea)
      }
    }
  }
}

@Composable
private fun IdeasListContent(
    ideas: List<Idea>,
    selectedProject: Project?,
    onIdeaClick: (Idea) -> Unit,
    onDeleteIdea: (String) -> Unit,
    onShareIdea: (String, String) -> Unit
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

// Conversation content will be added in separate PR
