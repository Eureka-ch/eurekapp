package ch.eureka.eurekapp.ui.notes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters

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
  Box(
      modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm),
      contentAlignment = Alignment.CenterEnd) {
        Surface(
            shape = EurekaStyles.CardShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = EurekaStyles.CardElevation,
            modifier =
                Modifier.widthIn(max = 320.dp)
                    .testTag(SelfNoteMessageBubbleTestTags.MESSAGE_BUBBLE)) {
              Column(
                  modifier = Modifier.padding(Spacing.md),
                  verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.testTag(SelfNoteMessageBubbleTestTags.MESSAGE_TEXT))

                    Text(
                        text = Formatters.formatDateTime(message.createdAt.toDate()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        modifier =
                            Modifier.testTag(SelfNoteMessageBubbleTestTags.MESSAGE_TIMESTAMP))
                  }
            }
      }
}
