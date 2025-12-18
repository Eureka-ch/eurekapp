package ch.eureka.eurekapp.ui.ideas

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.ui.components.MessageBubble
import ch.eureka.eurekapp.ui.components.MessageBubbleState
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.components.ThinkingBubble
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import com.google.firebase.Timestamp
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

object MCPChatbotScreenTestTags {
  const val SCREEN = "mcpChatbotScreen"
  const val MESSAGES_LIST = "mcpMessagesList"
  const val EMPTY_STATE = "mcpEmptyState"
  const val LOADING_INDICATOR = "mcpLoadingIndicator"
}

/**
 * Mock reasoning steps that simulate MCP thinking process (BOTA)
 */
private val MOCK_THINKING_STEPS = listOf(
  "Fetching project context (BOTA)…",
  "Reading repository structure…",
  "Analyzing last meeting notes…",
  "Understanding your role & objectives…",
  "Cross-checking with previous decisions…",
  "Checking tasks…"
)

/**
 * Mock responses - Returns AI meeting integration feature idea
 */
private fun getMockResponse(userMessage: String): String {
  return """A great feature would be to integrate AI to be a part of the meeting and add inputs when necessary.

How to integrate it:

• Create an AI service module
• Add real-time meeting transcription
• Build an AI input detection system
• Connect to meeting flow
• Test with sample meetings"""
}

/**
 * MCP Chatbot Screen - Mocked version for presentation
 * 
 * Uses the same interface as ConversationDetailScreen but with mocked MCP responses
 * that include thinking/reasoning steps before the final answer.
 */
@Composable
fun MCPChatbotScreen(
    modifier: Modifier = Modifier,
    idea: Idea?,
    projectName: String? = null,
    onNavigateBack: () -> Unit = {},
    currentUserId: String = "user"
) {
  var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
  var currentMessage by remember { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }
  var isThinking by remember { mutableStateOf(false) }
  var shouldResetSending by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(0)
    }
  }

  // Reset sending state after a delay
  LaunchedEffect(shouldResetSending) {
    if (shouldResetSending) {
      delay(100)
      isSending = false
      shouldResetSending = false
    }
  }

  Column(modifier = modifier.fillMaxSize().testTag(MCPChatbotScreenTestTags.SCREEN)) {
      // Messages list
      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
      when {
        messages.isEmpty() -> {
          Text(
            text = "Start a conversation with MCP to brainstorm ideas!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
              .align(Alignment.Center)
              .padding(Spacing.lg)
              .testTag(MCPChatbotScreenTestTags.EMPTY_STATE)
          )
        }
        else -> {
          LazyColumn(
            state = listState,
            modifier = Modifier
              .fillMaxSize()
              .padding(horizontal = Spacing.md)
              .testTag(MCPChatbotScreenTestTags.MESSAGES_LIST),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)
          ) {
            items(items = messages.reversed(), key = { it.id }) { message ->
              AnimatedMessageBubble(message = message) { msg ->
                when (msg.type) {
                  MessageType.USER -> {
                    MessageBubble(
                      state = MessageBubbleState(
                        text = msg.text,
                        timestamp = msg.timestamp,
                        isFromCurrentUser = true
                      )
                    )
                  }
                  MessageType.THINKING -> {
                    ThinkingBubble(
                      thinkingSteps = MOCK_THINKING_STEPS,
                      onComplete = {
                        // Replace thinking with final response
                        val responseText = getMockResponse(
                          messages.find { it.type == MessageType.USER && it.id == msg.relatedMessageId }?.text ?: ""
                        )
                        messages = messages.map { m ->
                          if (m.id == msg.id) {
                            m.copy(
                              type = MessageType.ASSISTANT,
                              text = responseText
                            )
                          } else {
                            m
                          }
                        }
                        isThinking = false
                      }
                    )
                  }
                  MessageType.ASSISTANT -> {
                    MessageBubble(
                      state = MessageBubbleState(
                        text = msg.text,
                        timestamp = msg.timestamp,
                        isFromCurrentUser = false
                      )
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // Message input
    MessageInputField(
      message = currentMessage,
      onMessageChange = { currentMessage = it },
      onSend = {
        if (currentMessage.isNotBlank() && !isSending && !isThinking) {
          val userMsg = ChatMessage(
            id = System.currentTimeMillis().toString(),
            text = currentMessage,
            type = MessageType.USER,
            timestamp = Timestamp.now()
          )
          
          val thinkingMsg = ChatMessage(
            id = (System.currentTimeMillis() + 1).toString(),
            text = "",
            type = MessageType.THINKING,
            timestamp = null,
            relatedMessageId = userMsg.id
          )
          
          messages = messages + listOf(userMsg, thinkingMsg)
          currentMessage = ""
          isSending = true
          isThinking = true
          shouldResetSending = true
        }
      },
      isSending = isSending,
      placeholder = "Ask MCP about your project..."
    )
  }
}

/**
 * Internal data class for chat messages
 */
private data class ChatMessage(
  val id: String,
  val text: String,
  val type: MessageType,
  val timestamp: Timestamp?,
  val relatedMessageId: String? = null
)

private enum class MessageType {
  USER,
  THINKING,
  ASSISTANT
}

/**
 * Wrapper composable that adds animation to message bubbles
 */
@Composable
private fun AnimatedMessageBubble(
  message: ChatMessage,
  content: @Composable (ChatMessage) -> Unit
) {
  AnimatedVisibility(
    visible = true,
    enter = fadeIn(animationSpec = tween(300)) + 
            slideInHorizontally(
              initialOffsetX = { if (message.type == MessageType.USER) 100 else -100 },
              animationSpec = tween(300)
            )
  ) {
    content(message)
  }
}


