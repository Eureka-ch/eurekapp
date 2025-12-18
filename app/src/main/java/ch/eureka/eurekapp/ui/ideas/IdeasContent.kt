/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.components.MessageBubble
import ch.eureka.eurekapp.ui.components.MessageBubbleState
import ch.eureka.eurekapp.ui.designsystem.tokens.EColors
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.first

/** Data class to group conversation-related parameters. */
data class ConversationState(
    val selectedIdea: Idea?,
    val messages: List<Message>,
    val currentUserId: String?,
    val onBackToList: () -> Unit
)

/** Data class to group list-related parameters. */
data class ListState(val ideas: List<Idea>, val onIdeaClick: (Idea) -> Unit)

/**
 * Generates a gradient brush for idea card border with rotating effect. Uses vibrant but subtle
 * colors: electric blue, neon violet, mint green, hot orange, light red.
 */
@Composable
private fun getIdeaBorderGradient(): Brush {
  val gradientColors =
      listOf(
          MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
          MaterialTheme.colorScheme.secondary.copy(alpha = 0.18f),
          MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
          EColors.IconBackgroundColor.copy(alpha = 0.24f))
  return Brush.linearGradient(
      colors = gradientColors,
      start = androidx.compose.ui.geometry.Offset.Zero,
      end = androidx.compose.ui.geometry.Offset(220f, 220f))
}

/**
 * Small avatar component for participant display. Similar to ConversationCard but smaller (24dp).
 * Has a white border to distinguish overlapping avatars.
 */
@Composable
private fun ParticipantAvatar(photoUrl: String, modifier: Modifier = Modifier) {
  if (photoUrl.isNotEmpty()) {
    AsyncImage(
        model = photoUrl,
        contentDescription = "Participant avatar",
        modifier =
            modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape),
        contentScale = ContentScale.Crop)
  } else {
    Box(
        modifier =
            modifier
                .size(24.dp)
                .clip(CircleShape)
                .border(
                    width = 2.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center) {
          Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Participant icon",
              modifier = Modifier.size(14.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
  }
}

/**
 * Displays participant avatars in a row with overlapping style (like Instagram group chats). Shows
 * up to 3 participants (excluding the creator). Loads user data to get photoUrls.
 */
@Composable
private fun ParticipantAvatars(
    participantIds: List<String>,
    createdBy: String,
    modifier: Modifier = Modifier
) {
  // Filter out creator and limit to 3 participants
  val otherParticipants = participantIds.filter { it != createdBy }.take(3)

  if (otherParticipants.isEmpty()) {
    return
  }

  // Load users for participants
  var users by remember(otherParticipants) { mutableStateOf<List<User?>>(emptyList()) }

  LaunchedEffect(otherParticipants) {
    val userRepository = RepositoriesProvider.userRepository
    users =
        otherParticipants.map { userId ->
          try {
            userRepository.getUserById(userId).first()
          } catch (e: Exception) {
            null
          }
        }
  }

  Box(modifier = modifier) {
    when (otherParticipants.size) {
      1 -> {
        // Single participant
        val user = users.getOrNull(0)
        ParticipantAvatar(photoUrl = user?.photoUrl ?: "")
      }
      2 -> {
        // Two participants - offset second one (reduced to keep within card)
        val user1 = users.getOrNull(0)
        val user2 = users.getOrNull(1)
        ParticipantAvatar(photoUrl = user1?.photoUrl ?: "")
        ParticipantAvatar(photoUrl = user2?.photoUrl ?: "", modifier = Modifier.offset(x = 10.dp))
      }
      else -> {
        // Three or more participants - show first 3 with reduced offsets to keep within card
        val user1 = users.getOrNull(0)
        val user2 = users.getOrNull(1)
        val user3 = users.getOrNull(2)
        ParticipantAvatar(photoUrl = user1?.photoUrl ?: "")
        ParticipantAvatar(photoUrl = user2?.photoUrl ?: "", modifier = Modifier.offset(x = 10.dp))
        ParticipantAvatar(photoUrl = user3?.photoUrl ?: "", modifier = Modifier.offset(x = 18.dp))
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeaCard(idea: Idea, onIdeaClick: () -> Unit) {
  val borderGradient = getIdeaBorderGradient()

  // Outer box with gradient border effect
  Box(
      modifier =
          Modifier.fillMaxWidth()
              .shadow(
                  elevation = 4.dp,
                  shape = RoundedCornerShape(20.dp),
                  spotColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
              .background(brush = borderGradient, shape = RoundedCornerShape(20.dp))
              .padding(2.5.dp)) {
        // Inner card with white background
        Card(
            onClick = onIdeaClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.5.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)) {
              Row(
                  modifier =
                      Modifier.fillMaxWidth()
                          .padding(vertical = Spacing.md, horizontal = Spacing.md),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                          text = idea.title ?: "Untitled Idea",
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.SemiBold)
                      if (idea.content != null) {
                        Text(
                            text = idea.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            modifier = Modifier.padding(top = Spacing.xs))
                      }
                    }

                    // Participant avatars on the right with padding to keep them inside card
                    if (idea.participantIds.isNotEmpty() &&
                        idea.participantIds.any { it != idea.createdBy }) {
                      Spacer(modifier = Modifier.padding(start = Spacing.sm))
                      Box(modifier = Modifier.padding(end = Spacing.xs)) {
                        ParticipantAvatars(
                            participantIds = idea.participantIds, createdBy = idea.createdBy)
                      }
                    }
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
    isLoading: Boolean
) {
  Box(modifier = Modifier.fillMaxSize()) {
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
        verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
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
                  state =
                      MessageBubbleState(
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
