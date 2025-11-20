package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/*
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

object MessageInputFieldTestTags {
  const val INPUT_FIELD = "noteInputField"
  const val SEND_BUTTON = "sendButton"
}

/**
 * Reusable input field for composing and sending messages.
 *
 * Displays a text field with a send button. The send button is disabled when the message is empty
 * or when a message is currently being sent.
 *
 * @param message Current message text.
 * @param onMessageChange Callback when the message text changes.
 * @param onSend Callback when the send button is clicked or Enter is pressed.
 * @param isSending Whether a message is currently being sent.
 * @param placeholder Placeholder text for the input field.
 * @param modifier Optional modifier.
 */
@Composable
fun MessageInputField(
    message: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    isSending: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "Write a message..."
) {
  Row(
      modifier = modifier.fillMaxWidth().padding(Spacing.md),
      verticalAlignment = Alignment.Bottom) {
        OutlinedTextField(
            value = message,
            onValueChange = onMessageChange,
            modifier = Modifier.weight(1f).testTag(MessageInputFieldTestTags.INPUT_FIELD),
            placeholder = { Text(placeholder) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions =
                KeyboardActions(onSend = { if (message.isNotBlank() && !isSending) onSend() }),
            maxLines = 4,
            shape = MaterialTheme.shapes.medium,
            colors = EurekaStyles.textFieldColors())

        IconButton(
            onClick = onSend,
            enabled = message.isNotBlank() && !isSending,
            modifier =
                Modifier.padding(start = Spacing.sm)
                    .testTag(MessageInputFieldTestTags.SEND_BUTTON)) {
              Icon(
                  imageVector = Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Send",
                  tint =
                      if (message.isNotBlank() && !isSending) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f))
            }
      }
}
