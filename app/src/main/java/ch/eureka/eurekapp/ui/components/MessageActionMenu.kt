package ch.eureka.eurekapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R

/*
Co-author: Claude 4.5 Sonnet
*/

object MessageActionMenuTestTags {
  const val MENU = "messageActionMenu"
  const val EDIT_OPTION = "editOption"
  const val DELETE_OPTION = "deleteOption"
  const val REMOVE_ATTACHMENT_OPTION = "removeAttachmentOption"
}

/**
 * A dropdown menu for message actions (edit, delete, remove attachment).
 *
 * @param expanded Whether the menu is currently visible.
 * @param onDismiss Callback when the menu is dismissed.
 * @param onEdit Callback when the edit option is selected.
 * @param onDelete Callback when the delete option is selected.
 * @param onRemoveAttachment Callback when the remove attachment option is selected (null if no
 *   attachment).
 * @param hasAttachment Whether the message has an attachment that can be removed.
 */
@Composable
fun MessageActionMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRemoveAttachment: (() -> Unit)? = null,
    hasAttachment: Boolean = false
) {
  DropdownMenu(
      expanded = expanded,
      onDismissRequest = onDismiss,
      modifier = Modifier.testTag(MessageActionMenuTestTags.MENU)) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.message_action_edit)) },
            onClick = {
              onDismiss()
              onEdit()
            },
            leadingIcon = {
              Icon(
                  Icons.Default.Edit,
                  contentDescription = stringResource(R.string.message_action_edit_description))
            },
            modifier = Modifier.testTag(MessageActionMenuTestTags.EDIT_OPTION))

        if (hasAttachment && onRemoveAttachment != null) {
          DropdownMenuItem(
              text = { Text(stringResource(R.string.message_action_remove_attachment)) },
              onClick = {
                onDismiss()
                onRemoveAttachment()
              },
              leadingIcon = {
                Icon(
                    Icons.Default.RemoveCircleOutline,
                    contentDescription =
                        stringResource(R.string.message_action_remove_attachment_description))
              },
              modifier = Modifier.testTag(MessageActionMenuTestTags.REMOVE_ATTACHMENT_OPTION))
        }

        DropdownMenuItem(
            text = { Text(stringResource(R.string.message_action_delete)) },
            onClick = {
              onDismiss()
              onDelete()
            },
            leadingIcon = {
              Icon(
                  Icons.Default.Delete,
                  contentDescription = stringResource(R.string.message_action_delete_description))
            },
            modifier = Modifier.testTag(MessageActionMenuTestTags.DELETE_OPTION))
      }
}
