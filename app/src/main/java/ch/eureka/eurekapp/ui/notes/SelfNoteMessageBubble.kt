package ch.eureka.eurekapp.ui.notes

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.ui.components.MessageBubble

/** Test tags for the Self Note Message Bubble component. */
object SelfNoteMessageBubbleTestTags {
  const val MESSAGE_BUBBLE = "messageBubble"
  const val MESSAGE_TEXT = "messageText"
  const val MESSAGE_TIMESTAMP = "messageTimestamp"
}

/*
Co-author: GPT-5 Codex
*/

/**
 * A chat-style message bubble for displaying a self-note.
 *
 * Displays the note's text content and timestamp in a rounded bubble, aligned to the right side of
 * the screen (representing the user's own message to themselves).
 *
 * @param message The message to display.
 * @param modifier Optional modifier for the bubble container.
 */
@Composable
fun SelfNoteMessageBubble(message: Message, modifier: Modifier = Modifier) {
  MessageBubble(
      text = message.text,
      timestamp = message.createdAt,
      isFromCurrentUser = true,
      modifier = modifier)
}
