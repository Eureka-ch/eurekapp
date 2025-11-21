/*
 * ConversationCard.kt
 *
 * Reusable card component for displaying a conversation in a list.
 * Shows the other participant's name and the project context.
 */

package ch.eureka.eurekapp.ui.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/**
 * Test tags for the ConversationCard component.
 */
object ConversationCardTestTags {
  const val CONVERSATION_CARD = "ConversationCard"
  const val MEMBER_NAME = "ConversationMemberName"
  const val PROJECT_NAME = "ConversationProjectName"
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
  // Main card container with rounded corners and elevation
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
              // Avatar/icon representing the other user in the conversation
              Icon(
                  imageVector = Icons.Default.Person,
                  contentDescription = "Member icon",
                  modifier = Modifier.size(40.dp),
                  tint = MaterialTheme.colorScheme.primary)

              Spacer(modifier = Modifier.width(12.dp))

              // Text content container taking remaining horizontal space
              Column(modifier = Modifier.weight(1f)) {
                // Primary text: display name of the other conversation participant
                Text(
                    text = displayData.otherMemberName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.testTag(ConversationCardTestTags.MEMBER_NAME))

                // Secondary text: project name provides context for this conversation
                Text(
                    text = displayData.projectName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag(ConversationCardTestTags.PROJECT_NAME))
              }
            }
      }
}
