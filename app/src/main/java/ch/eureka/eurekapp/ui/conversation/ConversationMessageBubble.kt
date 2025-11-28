package ch.eureka.eurekapp.ui.conversation

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
import ch.eureka.eurekapp.model.data.conversation.ConversationMessage
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters

object ConversationMessageBubbleTestTags {
  const val SENT_BUBBLE = "sentMessageBubble"
  const val RECEIVED_BUBBLE = "receivedMessageBubble"
  const val MESSAGE_TEXT = "messageText"
  const val MESSAGE_TIMESTAMP = "messageTimestamp"
}

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * A chat message bubble for displaying conversation messages.
 *
 * Displays the message text and timestamp in a rounded bubble. Sent messages are aligned to the
 * right with primary color, received messages are aligned to the left with secondary color.
 *
 * @param message The message to display.
 * @param isFromCurrentUser Whether this message was sent by the current user.
 * @param modifier Optional modifier for the bubble container.
 */
@Composable
fun ConversationMessageBubble(
    message: ConversationMessage,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
  Box(
      modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm),
      contentAlignment = if (isFromCurrentUser) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            shape = EurekaStyles.CardShape,
            color =
                if (isFromCurrentUser) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer,
            tonalElevation = EurekaStyles.CardElevation,
            modifier =
                Modifier.widthIn(max = 280.dp)
                    .testTag(
                        if (isFromCurrentUser) ConversationMessageBubbleTestTags.SENT_BUBBLE
                        else ConversationMessageBubbleTestTags.RECEIVED_BUBBLE)) {
              Column(
                  modifier = Modifier.padding(Spacing.md),
                  verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                            if (isFromCurrentUser) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.testTag(ConversationMessageBubbleTestTags.MESSAGE_TEXT))

                    Text(
                        text = Formatters.formatDateTime(message.createdAt.toDate()),
                        style = MaterialTheme.typography.labelSmall,
                        color =
                            if (isFromCurrentUser)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                        modifier =
                            Modifier.testTag(ConversationMessageBubbleTestTags.MESSAGE_TIMESTAMP))
                  }
            }
      }
}
