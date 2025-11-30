/*
 * ConversationCard.kt
 *
 * Reusable card component for displaying a conversation in a list.
 * Shows the other participant's name and the project context.
 */

package ch.eureka.eurekapp.ui.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import coil.compose.AsyncImage

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/** Test tags for the ConversationCard component. */
object ConversationCardTestTags {
  const val CONVERSATION_CARD = "ConversationCard"
  const val MEMBER_NAME = "ConversationMemberName"
  const val PROJECT_NAME = "ConversationProjectName"
  const val LAST_MESSAGE = "ConversationLastMessage"
  const val LAST_MESSAGE_TIME = "ConversationLastMessageTime"
  const val UNREAD_INDICATOR = "ConversationUnreadIndicator"
}

@Composable
private fun MemberAvatar(photoUrl: String, memberName: String) {
  if (photoUrl.isNotEmpty()) {
    AsyncImage(
        model = photoUrl,
        contentDescription = "Profile picture of $memberName",
        modifier = Modifier.size(48.dp).clip(CircleShape),
        contentScale = ContentScale.Crop)
  } else {
    Box(
        modifier =
            Modifier.size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center) {
          Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "Member icon",
              modifier = Modifier.size(28.dp),
              tint = MaterialTheme.colorScheme.onPrimaryContainer)
        }
  }
}

@Composable
private fun RowScope.MemberNameText(name: String, hasUnread: Boolean) {
  Text(
      text = name,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.SemiBold,
      modifier = Modifier.weight(1f).testTag(ConversationCardTestTags.MEMBER_NAME))
}

@Composable
private fun LastMessageTimeText(time: String) {
  Text(
      text = time,
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.testTag(ConversationCardTestTags.LAST_MESSAGE_TIME))
}

@Composable
private fun RowScope.LastMessagePreviewText(preview: String, hasUnread: Boolean) {
  val textColor: Color
  val textWeight: FontWeight?
  if (hasUnread) {
    textColor = MaterialTheme.colorScheme.onSurface
    textWeight = FontWeight.Medium
  } else {
    textColor = MaterialTheme.colorScheme.onSurfaceVariant
    textWeight = null
  }
  Text(
      text = preview,
      style = MaterialTheme.typography.bodySmall,
      color = textColor,
      fontWeight = textWeight,
      maxLines = 1,
      modifier = Modifier.weight(1f).testTag(ConversationCardTestTags.LAST_MESSAGE))
}

@Composable
private fun UnreadIndicator() {
  Spacer(modifier = Modifier.width(8.dp))
  Box(
      modifier =
          Modifier.size(8.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primary)
              .testTag(ConversationCardTestTags.UNREAD_INDICATOR))
}

/**
 * A card component displaying a conversation in the list.
 *
 * Shows the other member's name as the primary text and the project name as a subtitle. Clicking
 * the card triggers the onClick callback for future navigation to the chat screen.
 *
 * @param displayData The resolved display data containing member name and project name.
 * @param onClick Callback invoked when the card is clicked.
 * @param modifier Optional modifier for the card.
 */
@Composable
fun ConversationCard(
    displayData: ConversationDisplayData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
  Card(
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = EurekaStyles.CardElevation),
      modifier =
          modifier
              .fillMaxWidth()
              .clickable(onClick = onClick)
              .testTag(ConversationCardTestTags.CONVERSATION_CARD)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically) {
              MemberAvatar(displayData.otherMemberPhotoUrl, displayData.otherMemberName)
              Spacer(modifier = Modifier.width(12.dp))
              ConversationCardContent(displayData)
            }
      }
}

@Composable
private fun RowScope.ConversationCardContent(displayData: ConversationDisplayData) {
  Column(modifier = Modifier.weight(1f)) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
      MemberNameText(displayData.otherMemberName, displayData.hasUnread)
      displayData.lastMessageTime?.let { LastMessageTimeText(it) }
    }

    Text(
        text = displayData.projectName,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.testTag(ConversationCardTestTags.PROJECT_NAME))

    displayData.lastMessagePreview?.let { preview ->
      Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        LastMessagePreviewText(preview, displayData.hasUnread)
        if (displayData.hasUnread) {
          UnreadIndicator()
        }
      }
    }
  }
}
