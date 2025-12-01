/* Portions of this file were written with the help of Gemini and GPT-5 Codex. */
package ch.eureka.eurekapp.ui.notes

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.ui.components.MessageBubble

/** Test tags for the Self Note Message Bubble component. */
object SelfNoteMessageBubbleTestTags {
  const val MESSAGE_TEXT = "messageText"
  const val MESSAGE_TIMESTAMP = "messageTimestamp"
  const val MENU_EDIT = "menuEdit"
  const val MENU_DELETE = "menuDelete"
}

/**
 * A chat-style message bubble for displaying a self-note. Supports Long-Press to show options
 * (Edit, Delete).
 *
 * @param message The message to display.
 * @param modifier Optional modifier for the bubble container.
 * @param onEditClick Callback invoked when user selects "Edit".
 * @param onDeleteClick Callback invoked when user selects "Delete".
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SelfNoteMessageBubble(
    message: Message,
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
  var showMenu by remember { mutableStateOf(false) }

  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    Box(modifier = modifier) {
      MessageBubble(
          text = message.text,
          timestamp = message.createdAt,
          isFromCurrentUser = true,
          modifier =
              Modifier.combinedClickable(
                  onClick = {}, // No action on normal click
                  onLongClick = { showMenu = true }))

      DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
        DropdownMenuItem(
            text = { Text("Edit Note") },
            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
            onClick = {
              showMenu = false
              onEditClick()
            },
            modifier = Modifier.testTag(SelfNoteMessageBubbleTestTags.MENU_EDIT))
        DropdownMenuItem(
            text = { Text("Delete Note") },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
            onClick = {
              showMenu = false
              onDeleteClick()
            },
            modifier = Modifier.testTag(SelfNoteMessageBubbleTestTags.MENU_DELETE))
      }
    }
  }
}
