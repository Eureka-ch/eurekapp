/* Portions of this file were written with the help of Gemini and GPT-5 Codex. */
package ch.eureka.eurekapp.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.ui.components.MessageBubble

/**
 * A chat-style message bubble for displaying a self-note.
 *
 * @param message The message to display.
 * @param isSelected Whether this note is currently selected (in multi-select mode).
 * @param modifier Optional modifier for the bubble container.
 * @param onClick Action when the bubble is clicked (Standard click).
 * @param onLongClick Action when the bubble is long-pressed.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SelfNoteMessageBubble(
    message: Message,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {}
) {
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else Color.Transparent,
                    shape = RoundedCornerShape(8.dp))
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(4.dp)) {
          MessageBubble(
              text = message.text,
              timestamp = message.createdAt,
              isFromCurrentUser = true,
              modifier = Modifier.align(Alignment.CenterEnd))
        }
  }
}
