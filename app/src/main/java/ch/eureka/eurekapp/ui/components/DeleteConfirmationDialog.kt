package ch.eureka.eurekapp.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R

/*
Co-author: Claude 4.5 Sonnet
*/

object DeleteConfirmationDialogTestTags {
  const val DIALOG = "deleteConfirmationDialog"
  const val CONFIRM_BUTTON = "confirmDeleteButton"
  const val CANCEL_BUTTON = "cancelDeleteButton"
}

/**
 * A confirmation dialog shown before deleting a message.
 *
 * @param onConfirm Callback when the user confirms deletion.
 * @param onDismiss Callback when the user cancels or dismisses the dialog.
 */
@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
  AlertDialog(
      onDismissRequest = onDismiss,
      modifier = Modifier.testTag(DeleteConfirmationDialogTestTags.DIALOG),
      title = { Text(stringResource(R.string.delete_confirmation_title)) },
      text = { Text(stringResource(R.string.delete_confirmation_message)) },
      confirmButton = {
        TextButton(
            onClick = onConfirm,
            modifier = Modifier.testTag(DeleteConfirmationDialogTestTags.CONFIRM_BUTTON)) {
              Text(stringResource(R.string.delete_confirmation_confirm))
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(DeleteConfirmationDialogTestTags.CANCEL_BUTTON)) {
              Text(stringResource(R.string.delete_confirmation_cancel))
            }
      })
}
