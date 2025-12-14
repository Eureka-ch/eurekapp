/*
 * ConversationListScreen.kt
 *
 * Main screen displaying all conversations for the current user.
 * Provides navigation to create new conversations via a FAB.
 */

package ch.eureka.eurekapp.ui.conversation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/** Test tags for the ConversationListScreen component. */
object ConversationListScreenTestTags {
  const val SCREEN = "ConversationListScreen"
  const val TITLE = "ConversationListTitle"
  const val DESCRIPTION = "ConversationListDescription"
  const val CONVERSATION_LIST = "ConversationList"
  const val EMPTY_STATE = "ConversationEmptyState"
  const val LOADING_INDICATOR = "ConversationLoadingIndicator"
  const val CREATE_BUTTON = "CreateConversationButton"
  const val OFFLINE_MESSAGE = "ConversationOfflineMessage"
}

/**
 * Main screen displaying the list of conversations for the current user.
 *
 * Shows all conversations across all projects. Each conversation displays the other member's name
 * and the project it belongs to. A FAB allows creating new conversations.
 *
 * @param onCreateConversation Callback invoked when the user wants to create a new conversation.
 * @param onConversationClick Callback invoked when a conversation is clicked, receives conversation
 *   ID.
 * @param viewModel The ViewModel managing the conversation list state.
 */
@Composable
fun ConversationListScreen(
    onCreateConversation: () -> Unit,
    onConversationClick: (String) -> Unit = {},
    viewModel: ConversationListViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  // Side effect: Show error messages as toast notifications
  // Clears the error after showing to prevent repeated toasts on recomposition
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  Scaffold(
      modifier = Modifier.testTag(ConversationListScreenTestTags.SCREEN),
      topBar = { EurekaTopBar(title = "Conversations") },
      floatingActionButton = {
        // FAB to create new conversation
        FloatingActionButton(
            onClick = onCreateConversation,
            modifier = Modifier.testTag(ConversationListScreenTestTags.CREATE_BUTTON),
            containerColor =
                if (uiState.isConnected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceVariant) {
              Icon(imageVector = Icons.Default.Add, contentDescription = "Create conversation")
            }
      }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = Spacing.md)) {
              // Screen title
              Spacer(modifier = Modifier.height(Spacing.md))
              Text(
                  text = "Conversations",
                  style = MaterialTheme.typography.headlineSmall,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.testTag(ConversationListScreenTestTags.TITLE))

              Spacer(modifier = Modifier.height(Spacing.xs))

              // Screen description
              Text(
                  text = "Chat with your project members",
                  style = MaterialTheme.typography.bodyMedium,
                  color = Color.Gray,
                  modifier = Modifier.testTag(ConversationListScreenTestTags.DESCRIPTION))

              Spacer(modifier = Modifier.height(Spacing.md))

              // Offline warning message
              if (!uiState.isConnected) {
                Text(
                    text = "You are offline. Some features may be unavailable.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier =
                        Modifier.padding(bottom = Spacing.md)
                            .testTag(ConversationListScreenTestTags.OFFLINE_MESSAGE))
              }

              // Content area: conditional rendering based on state
              // Shows loading spinner, empty state message, or the list of conversations
              when {
                uiState.isLoading -> {
                  // Loading state: centered spinner while fetching conversations
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.testTag(ConversationListScreenTestTags.LOADING_INDICATOR))
                  }
                }
                uiState.conversations.isEmpty() -> {
                  // Empty state: encourage user to create their first conversation
                  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center) {
                          Text(
                              text = "No conversations yet",
                              style = MaterialTheme.typography.titleMedium,
                              color = Color.Gray,
                              modifier =
                                  Modifier.testTag(ConversationListScreenTestTags.EMPTY_STATE))
                          Spacer(modifier = Modifier.height(Spacing.xs))
                          Text(
                              text = "Tap + to start a conversation",
                              style = MaterialTheme.typography.bodyMedium,
                              color = Color.Gray)
                        }
                  }
                }
                else -> {
                  // Success state: display scrollable list of conversation cards
                  // Uses LazyColumn for efficient rendering of potentially large lists
                  LazyColumn(
                      modifier =
                          Modifier.fillMaxWidth()
                              .testTag(ConversationListScreenTestTags.CONVERSATION_LIST),
                      contentPadding = PaddingValues(vertical = Spacing.sm),
                      verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                        items(
                            items = uiState.conversations,
                            key = { it.conversation.conversationId }) { displayData ->
                              ConversationCard(
                                  displayData = displayData,
                                  onClick = {
                                    onConversationClick(displayData.conversation.conversationId)
                                  })
                            }
                      }
                }
              }
            }
      }
}
