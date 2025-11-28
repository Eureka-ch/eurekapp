package ch.eureka.eurekapp.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters
import com.google.firebase.Timestamp

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

object MessageBubbleTestTags {
  const val BUBBLE = "messageBubble"
  const val TEXT = "messageText"
  const val TIMESTAMP = "messageTimestamp"
}

/**
 * A reusable chat-style message bubble component.
 *
 * Displays message text and timestamp in a rounded bubble. Can be aligned to the right (sent) or
 * left (received) side of the screen with different color schemes.
 *
 * @param text The message text to display.
 * @param timestamp The timestamp of the message.
 * @param isFromCurrentUser Whether this message was sent by the current user (affects alignment and
 *   color).
 * @param modifier Optional modifier for the bubble container.
 */
@Composable
fun MessageBubble(
    text: String,
    timestamp: Timestamp,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier
) {
  val containerColor: Color
  val contentColor: Color
  val alignment: Alignment

  if (isFromCurrentUser) {
    containerColor = MaterialTheme.colorScheme.primaryContainer
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    alignment = Alignment.CenterEnd
  } else {
    containerColor = MaterialTheme.colorScheme.secondaryContainer
    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    alignment = Alignment.CenterStart
  }

  Box(
      modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm),
      contentAlignment = alignment) {
        Surface(
            shape = EurekaStyles.CardShape,
            color = containerColor,
            tonalElevation = EurekaStyles.CardElevation,
            modifier = Modifier.widthIn(max = 280.dp).testTag(MessageBubbleTestTags.BUBBLE)) {
              Column(
                  modifier = Modifier.padding(Spacing.md),
                  verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = contentColor,
                        modifier = Modifier.testTag(MessageBubbleTestTags.TEXT))

                    Text(
                        text = Formatters.formatDateTime(timestamp.toDate()),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.testTag(MessageBubbleTestTags.TIMESTAMP))
                  }
            }
      }
}
