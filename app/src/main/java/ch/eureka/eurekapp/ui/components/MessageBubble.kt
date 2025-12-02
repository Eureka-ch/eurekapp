package ch.eureka.eurekapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.camera.PhotoViewer
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing
import ch.eureka.eurekapp.utils.Formatters
import com.google.firebase.Timestamp

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
Co-author: Grok
*/

object MessageBubbleTestTags {
  const val BUBBLE = "messageBubble"
  const val TEXT = "messageText"
  const val TIMESTAMP = "messageTimestamp"
  const val PHOTO_VIEWER = "photoViewer"
}

/**
 * A reusable chat-style message bubble component.
 *
 * Displays message text and timestamp in a rounded bubble. Can be aligned to the right (sent) or
 * left (received) side of the screen with different color schemes.
 *
 * @param text The message text to display.
 * @param timestamp The timestamp of the message (null if pending server timestamp).
 * @param isFromCurrentUser Whether this message was sent by the current user (affects alignment and
 *   color).
 * @param modifier Optional modifier for the bubble container.
 * @param isFile Whether this message contains a file attachment.
 * @param fileUrl URL to download the file if isFile is true.
 * @param onLinkClick Callback when a link in the text is clicked.
 * @param onDownloadClick Callback when the download button is clicked for file messages.
 */
@Composable
fun MessageBubble(
    text: String,
    timestamp: Timestamp?,
    isFromCurrentUser: Boolean,
    modifier: Modifier = Modifier,
    isFile: Boolean = false,
    fileUrl: String = "",
    onLinkClick: (String) -> Unit = {},
    onDownloadClick: (String) -> Unit = {}
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
                    if (text.isNotEmpty()) {
                      val annotatedText = buildAnnotatedString {
                        val urlRegex = Regex("https?://\\S+")
                        val matches = urlRegex.findAll(text).toList()
                        var lastIndex = 0
                        for (match in matches) {
                          append(text.substring(lastIndex, match.range.first))
                          withLink(
                              LinkAnnotation.Clickable(match.value) { onLinkClick(match.value) }) {
                                append(match.value)
                              }
                          lastIndex = match.range.last + 1
                        }
                        append(text.substring(lastIndex))
                      }
                      Text(
                          text = annotatedText,
                          style = MaterialTheme.typography.bodyMedium,
                          color = contentColor,
                          modifier = Modifier.testTag(MessageBubbleTestTags.TEXT))
                    }

                    if (isFile && fileUrl.isNotEmpty()) {
                      Row(
                          modifier = Modifier.padding(top = 8.dp),
                          verticalAlignment = Alignment.CenterVertically,
                          horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Extract filename from Firebase Storage URL by decoding the path
                            val fileName =
                                fileUrl
                                    .substringAfter("/o/")
                                    .substringBefore("?")
                                    .let { java.net.URLDecoder.decode(it, "UTF-8") }
                                    .substringAfterLast("/")
                            val isImage = isImageFile(fileName)
                            if (isImage) {
                              PhotoViewer(
                                  image = fileUrl,
                                  modifier =
                                      Modifier.size(100.dp)
                                          .clip(RoundedCornerShape(8.dp))
                                          .testTag(MessageBubbleTestTags.PHOTO_VIEWER))
                            } else {
                              Icon(
                                  imageVector = Icons.Default.AttachFile,
                                  contentDescription = "File attachment",
                                  tint = contentColor,
                                  modifier = Modifier.size(24.dp))
                              // Remove timestamp from display name (format: name_timestamp.ext)
                              val displayName =
                                  fileName.replace(Regex("_\\d{13}(?=\\.[^.]+$|$)"), "")
                              val truncatedName =
                                  if (displayName.length > 20) displayName.take(17) + "..."
                                  else displayName
                              Text(
                                  text = truncatedName,
                                  color = contentColor,
                                  style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { onDownloadClick(fileUrl) }) {
                              Icon(
                                  imageVector = Icons.Default.Download,
                                  contentDescription = "Download file",
                                  tint = contentColor)
                            }
                          }
                    }

                    val formattedTime =
                        timestamp?.let { Formatters.formatDateTime(it.toDate()) } ?: "Sending..."
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.7f),
                        modifier = Modifier.testTag(MessageBubbleTestTags.TIMESTAMP))
                  }
            }
      }
}

private fun isImageFile(filename: String): Boolean {
  val extension = filename.substringAfterLast(".").lowercase()
  return extension in listOf("jpg", "jpeg", "png", "gif", "bmp", "webp")
}
