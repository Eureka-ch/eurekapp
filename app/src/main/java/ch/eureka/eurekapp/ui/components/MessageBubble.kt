package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.ui.theme.DarkColorScheme
import ch.eureka.eurekapp.ui.theme.Typography
import ch.eureka.eurekapp.utils.Formatters
import coil.compose.AsyncImage
import com.google.firebase.Timestamp

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

private val TEXT_WEIGHT = FontWeight(500)
private const val MESSAGE_WEIGHT = 3f

object MessageBubbleTestTags {
  const val BUBBLE = "messageBubble"
  const val TEXT = "messageText"
  const val TIMESTAMP = "messageTimestamp"
  const val PHOTO_VIEWER = "photoViewer"
  const val EDITED_INDICATOR = "editedIndicator"
}

/**
 * State holder for MessageBubble component parameters.
 *
 * @param senderPhotoUrl photo url of the sender
 * @param senderDisplayName display name of the sender
 * @param text The message text to display.
 * @param timestamp The timestamp of the message (null if pending server timestamp).
 * @param isFromCurrentUser Whether this message was sent by the current user (affects alignment and
 *   color).
 * @param fileAttachment Configuration for file attachment display.
 * @param editedAt Timestamp when the message was last edited (null if never edited).
 * @param interactions Configuration for message interactions (link clicks, long press).
 */
data class MessageBubbleState(
    val senderPhotoUrl: String = "",
    val senderDisplayName: String = "",
    val text: String,
    val timestamp: Timestamp?,
    val isFromCurrentUser: Boolean,
    val fileAttachment: MessageBubbleFileAttachment = MessageBubbleFileAttachment(),
    val editedAt: Timestamp? = null,
    val interactions: MessageBubbleInteractions = MessageBubbleInteractions()
)

/**
 * A reusable chat-style message bubble component.
 *
 * Displays message text and timestamp in a rounded bubble. Can be aligned to the right (sent) or
 * left (received) side of the screen with different color schemes.
 *
 * @param state The state holder containing all message bubble parameters.
 * @param modifier Optional modifier for the bubble container.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    modifier: Modifier = Modifier,
    state: MessageBubbleState,
) {
  val (containerColor, contentColor, alignment) = getBubbleColors(state.isFromCurrentUser)
  val onLongClick = state.interactions.onLongClick

  Box(
      modifier = modifier.fillMaxWidth().padding(vertical = Spacing.sm),
      contentAlignment = alignment) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = arrangementBasedOnUser(state.isFromCurrentUser),
            verticalAlignment = Alignment.CenterVertically) {
              if (state.isFromCurrentUser) {
                DisplayNameText(state.senderDisplayName)
                Row(
                    modifier = Modifier.weight(MESSAGE_WEIGHT),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      MessageDisplay(
                          MessageDisplayState(
                              onLongClick = onLongClick,
                              containerColor = containerColor,
                              contentColor = contentColor,
                              text = state.text,
                              fileAttachment = state.fileAttachment,
                              interactions = state.interactions,
                              timestamp = state.timestamp,
                              editedAt = state.editedAt))
                    }
                ProfileIcon(state.senderPhotoUrl)
              } else {
                ProfileIcon(state.senderPhotoUrl)
                Row(
                    modifier = Modifier.weight(MESSAGE_WEIGHT),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically) {
                      MessageDisplay(
                          MessageDisplayState(
                              onLongClick = onLongClick,
                              containerColor = containerColor,
                              contentColor = contentColor,
                              text = state.text,
                              fileAttachment = state.fileAttachment,
                              interactions = state.interactions,
                              timestamp = state.timestamp,
                              editedAt = state.editedAt))
                    }
                DisplayNameText(state.senderDisplayName)
              }
            }
      }
}

private data class MessageDisplayState(
    val onLongClick: (() -> Unit)?,
    val containerColor: Color,
    val contentColor: Color,
    val text: String,
    val fileAttachment: MessageBubbleFileAttachment,
    val interactions: MessageBubbleInteractions,
    val timestamp: Timestamp?,
    val editedAt: Timestamp?
)

@Composable
private fun MessageDisplay(messageDisplayState: MessageDisplayState) {
  Surface(
      shape = EurekaStyles.CardShape,
      color = messageDisplayState.containerColor,
      tonalElevation = EurekaStyles.CardElevation,
      modifier =
          Modifier.widthIn(max = 280.dp)
              .testTag(MessageBubbleTestTags.BUBBLE)
              .then(
                  if (messageDisplayState.onLongClick != null) {
                    Modifier.combinedClickable(
                        onClick = {}, onLongClick = messageDisplayState.onLongClick)
                  } else {
                    Modifier
                  })) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
              if (messageDisplayState.text.isNotEmpty()) {
                Text(
                    text =
                        buildAnnotatedText(
                            messageDisplayState.text, messageDisplayState.interactions.onLinkClick),
                    style = MaterialTheme.typography.bodyMedium,
                    color = messageDisplayState.contentColor,
                    modifier = Modifier.testTag(MessageBubbleTestTags.TEXT))
              }

              FileAttachment(messageDisplayState.fileAttachment, messageDisplayState.contentColor)

              Row(
                  horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = getFormattedTime(messageDisplayState.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = messageDisplayState.contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.testTag(MessageBubbleTestTags.TIMESTAMP))
                    if (messageDisplayState.editedAt != null) {
                      Text(
                          text = stringResource(R.string.message_edited_indicator),
                          style = MaterialTheme.typography.labelSmall,
                          color = messageDisplayState.contentColor.copy(alpha = 0.5f),
                          modifier = Modifier.testTag(MessageBubbleTestTags.EDITED_INDICATOR))
                    }
                  }
            }
      }
}

@Composable
private fun DisplayNameText(senderDisplayName: String) {
  if (senderDisplayName != "") {
    Row(
        modifier = Modifier.padding(Spacing.xxs),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
          Text(
              text = senderDisplayName,
              color = DarkColorScheme.background,
              fontWeight = TEXT_WEIGHT,
              style = Typography.titleMedium)
        }
  }
}

@Composable
private fun ProfileIcon(senderPhotoUrl: String) {
  if (senderPhotoUrl != "") {
    Row(
        modifier = Modifier.padding(Spacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically) {
          AsyncImage(
              model = senderPhotoUrl,
              contentDescription =
                  stringResource(R.string.message_profile_picture_description, senderPhotoUrl),
              modifier = Modifier.size(48.dp).clip(CircleShape),
              contentScale = ContentScale.Crop)
        }
  }
}

data class MessageBubbleFileAttachment(
    val isFile: Boolean = false,
    val fileUrl: String = "",
    val onDownloadClick: (String) -> Unit = {}
)

/**
 * Configuration for message interactions (edit, link clicks, long press).
 *
 * @param onLinkClick Callback when a link in the text is clicked.
 * @param onLongClick Callback when the message is long-pressed (null to disable).
 */
data class MessageBubbleInteractions(
    val onLinkClick: (String) -> Unit = {},
    val onLongClick: (() -> Unit)? = null
)

@Composable
private fun getBubbleColors(isFromCurrentUser: Boolean): Triple<Color, Color, Alignment> {
  return if (isFromCurrentUser) {
    Triple(
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.onPrimaryContainer,
        Alignment.CenterEnd)
  } else {
    Triple(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.onSecondaryContainer,
        Alignment.CenterStart)
  }
}

private fun buildAnnotatedText(
    text: String,
    onLinkClick: (String) -> Unit
): androidx.compose.ui.text.AnnotatedString {
  return buildAnnotatedString {
    val matches = URL_REGEX.findAll(text).toList()
    var lastIndex = 0
    for (match in matches) {
      append(text.substring(lastIndex, match.range.first))
      withLink(LinkAnnotation.Clickable(match.value) { onLinkClick(match.value) }) {
        append(match.value)
      }
      lastIndex = match.range.last + 1
    }
    append(text.substring(lastIndex))
  }
}

@Composable
private fun FileAttachment(fileAttachment: MessageBubbleFileAttachment, contentColor: Color) {
  if (!fileAttachment.isFile || fileAttachment.fileUrl.isEmpty()) return

  Row(
      modifier = Modifier.padding(top = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        // Extract filename from Firebase Storage URL by decoding the path
        val fileName =
            fileAttachment.fileUrl
                .substringAfter("/o/")
                .substringBefore("?")
                .let { java.net.URLDecoder.decode(it, "UTF-8") }
                .substringAfterLast("/")
        val isImage = isImageFile(fileName)
        if (isImage) {
          PhotoViewer(
              image = fileAttachment.fileUrl,
              modifier =
                  Modifier.size(100.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .testTag(MessageBubbleTestTags.PHOTO_VIEWER))
        } else {
          Icon(
              imageVector = Icons.Default.AttachFile,
              contentDescription = stringResource(R.string.message_file_attachment_description),
              tint = contentColor,
              modifier = Modifier.size(24.dp))
          // Remove timestamp from display name (format: name_timestamp.ext)
          val displayName = fileName.replace(TIMESTAMP_REGEX, "")
          val truncatedName =
              if (displayName.length > 20) displayName.take(17) + "..." else displayName
          Text(
              text = truncatedName,
              color = contentColor,
              style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { fileAttachment.onDownloadClick(fileAttachment.fileUrl) }) {
          Icon(
              imageVector = Icons.Default.Download,
              contentDescription = stringResource(R.string.message_download_file),
              tint = contentColor)
        }
      }
}

private fun getFormattedTime(timestamp: Timestamp?): String {
  return timestamp?.let { Formatters.formatDateTime(it.toDate()) } ?: "Sending..."
}

private fun isImageFile(filename: String): Boolean {
  val extension = filename.substringAfterLast(".").lowercase()
  return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
}

private val URL_REGEX = Regex("https?://\\S+")
private val TIMESTAMP_REGEX = Regex("_\\d{13}(?=\\.[^.]+$|$)")

private fun arrangementBasedOnUser(isCurrentUser: Boolean): Arrangement.Horizontal {
  return if (isCurrentUser) Arrangement.End else Arrangement.Start
}
