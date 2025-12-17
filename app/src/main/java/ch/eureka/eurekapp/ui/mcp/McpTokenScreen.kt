// Co-authored by Claude Code
package ch.eureka.eurekapp.ui.mcp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.mcp.McpToken
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object McpTokenScreenTestTags {
  const val SCREEN = "mcp_token_screen"
  const val BACK_BUTTON = "mcp_token_back_button"
  const val TOKEN_LIST = "mcp_token_list"
  const val TOKEN_ITEM = "mcp_token_item"
  const val CREATE_BUTTON = "mcp_token_create_button"
  const val CREATE_DIALOG = "mcp_token_create_dialog"
  const val TOKEN_NAME_FIELD = "mcp_token_name_field"
  const val CONFIRM_CREATE = "mcp_token_confirm_create"
  const val CANCEL_CREATE = "mcp_token_cancel_create"
  const val DELETE_BUTTON = "mcp_token_delete_button"
  const val LOADING_INDICATOR = "mcp_token_loading"
  const val ERROR_TEXT = "mcp_token_error"
  const val EMPTY_STATE = "mcp_token_empty_state"
  const val TOKEN_CREATED_DIALOG = "mcp_token_created_dialog"
}

@Composable
fun McpTokenScreen(viewModel: McpTokenViewModel, onNavigateBack: () -> Unit) {
  val uiState by viewModel.uiState.collectAsState()
  var showCreateDialog by remember { mutableStateOf(false) }
  var tokenName by remember { mutableStateOf("") }

  Scaffold(
      topBar = {
        Column {
          BackButton(
              onClick = onNavigateBack,
              modifier = Modifier.testTag(McpTokenScreenTestTags.BACK_BUTTON))
          EurekaTopBar(title = stringResource(R.string.mcp_tokens_title))
        }
      },
      floatingActionButton = {
        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier.testTag(McpTokenScreenTestTags.CREATE_BUTTON),
            containerColor = MaterialTheme.colorScheme.primary) {
              Icon(
                  Icons.Default.Add,
                  contentDescription = stringResource(R.string.mcp_token_create_button))
            }
      },
      containerColor = Color.White) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .testTag(McpTokenScreenTestTags.SCREEN)) {
              when {
                uiState.isLoading -> {
                  CircularProgressIndicator(
                      modifier =
                          Modifier.align(Alignment.Center)
                              .testTag(McpTokenScreenTestTags.LOADING_INDICATOR))
                }
                uiState.error != null -> {
                  Column(
                      modifier = Modifier.align(Alignment.Center).padding(16.dp),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = uiState.error ?: "Unknown error",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag(McpTokenScreenTestTags.ERROR_TEXT))
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadTokens() }) {
                          Text(stringResource(R.string.mcp_token_retry_button))
                        }
                      }
                }
                uiState.tokens.isEmpty() -> {
                  Column(
                      modifier = Modifier.align(Alignment.Center).padding(16.dp),
                      horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.mcp_token_empty_state_title),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.testTag(McpTokenScreenTestTags.EMPTY_STATE))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.mcp_token_empty_state_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                      }
                }
                else -> {
                  LazyColumn(
                      modifier =
                          Modifier.fillMaxSize()
                              .padding(16.dp)
                              .testTag(McpTokenScreenTestTags.TOKEN_LIST),
                      verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.tokens, key = { it.tokenHash }) { token ->
                          TokenCard(
                              token = token, onDelete = { viewModel.revokeToken(token.tokenHash) })
                        }
                      }
                }
              }
            }
      }

  if (showCreateDialog) {
    CreateTokenDialog(
        tokenName = tokenName,
        onTokenNameChange = { tokenName = it },
        onConfirm = {
          viewModel.createToken(tokenName.ifBlank { "MCP Tokens" })
          tokenName = ""
          showCreateDialog = false
        },
        onDismiss = {
          tokenName = ""
          showCreateDialog = false
        })
  }

  uiState.newlyCreatedToken?.let { token ->
    TokenCreatedDialog(token = token, onDismiss = { viewModel.clearNewlyCreatedToken() })
  }
}

@Composable
private fun TokenCard(token: McpToken, onDelete: () -> Unit) {
  var showDeleteConfirmation by remember { mutableStateOf(false) }

  Card(
      modifier =
          Modifier.fillMaxWidth()
              .testTag("${McpTokenScreenTestTags.TOKEN_ITEM}_${token.tokenHash}"),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = token.name.ifBlank { stringResource(R.string.mcp_token_unnamed) },
                    style = MaterialTheme.typography.titleMedium)
                IconButton(
                    onClick = { showDeleteConfirmation = true },
                    modifier =
                        Modifier.testTag(
                            "${McpTokenScreenTestTags.DELETE_BUTTON}_${token.tokenHash}")) {
                      Icon(
                          Icons.Default.Delete,
                          contentDescription =
                              stringResource(R.string.mcp_token_delete_dialog_title),
                          tint = MaterialTheme.colorScheme.error)
                    }
              }

          Spacer(modifier = Modifier.height(8.dp))

          token.createdAt?.let { createdAt ->
            Text(
                text =
                    stringResource(R.string.mcp_token_created_prefix) + formatTimestamp(createdAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }

          token.expiresAt?.let { expiresAt ->
            val isExpired = expiresAt.toDate().before(Date())
            Text(
                text =
                    if (isExpired)
                        stringResource(R.string.mcp_token_expired_prefix) +
                            formatTimestamp(expiresAt)
                    else
                        stringResource(R.string.mcp_token_expires_prefix) +
                            formatTimestamp(expiresAt),
                style = MaterialTheme.typography.bodySmall,
                color =
                    if (isExpired) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant)
          }

          token.lastUsedAt?.let { lastUsedAt ->
            Text(
                text =
                    stringResource(R.string.mcp_token_last_used_prefix) +
                        formatTimestamp(lastUsedAt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

  if (showDeleteConfirmation) {
    AlertDialog(
        onDismissRequest = { showDeleteConfirmation = false },
        title = { Text(stringResource(R.string.mcp_token_delete_dialog_title)) },
        text = { Text(stringResource(R.string.mcp_token_delete_confirmation)) },
        confirmButton = {
          TextButton(
              onClick = {
                onDelete()
                showDeleteConfirmation = false
              },
              colors =
                  ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                Text(stringResource(R.string.delete_confirmation_confirm))
              }
        },
        dismissButton = {
          TextButton(onClick = { showDeleteConfirmation = false }) {
            Text(stringResource(R.string.delete_confirmation_cancel))
          }
        })
  }
}

@Composable
private fun CreateTokenDialog(
    tokenName: String,
    onTokenNameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
  AlertDialog(
      onDismissRequest = onDismiss,
      modifier = Modifier.testTag(McpTokenScreenTestTags.CREATE_DIALOG),
      title = { Text(stringResource(R.string.mcp_token_create_dialog_title)) },
      text = {
        Column {
          Text(stringResource(R.string.mcp_token_create_dialog_description))
          Spacer(modifier = Modifier.height(16.dp))
          OutlinedTextField(
              value = tokenName,
              onValueChange = onTokenNameChange,
              label = { Text(stringResource(R.string.mcp_token_name_label)) },
              placeholder = { Text(stringResource(R.string.mcp_token_name_placeholder)) },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag(McpTokenScreenTestTags.TOKEN_NAME_FIELD))
        }
      },
      confirmButton = {
        Button(
            onClick = onConfirm,
            modifier = Modifier.testTag(McpTokenScreenTestTags.CONFIRM_CREATE)) {
              Text(stringResource(R.string.create_conversation_button))
            }
      },
      dismissButton = {
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.testTag(McpTokenScreenTestTags.CANCEL_CREATE)) {
              Text(stringResource(R.string.delete_confirmation_cancel))
            }
      })
}

@Composable
private fun TokenCreatedDialog(token: String, onDismiss: () -> Unit) {
  val context = LocalContext.current

  AlertDialog(
      onDismissRequest = onDismiss,
      modifier = Modifier.testTag(McpTokenScreenTestTags.TOKEN_CREATED_DIALOG),
      title = { Text(stringResource(R.string.mcp_token_created_dialog_title)) },
      text = {
        Column {
          Text(stringResource(R.string.mcp_token_created_dialog_message))
          Spacer(modifier = Modifier.height(16.dp))
          Card(
              colors =
                  CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = token,
                          style = MaterialTheme.typography.bodySmall,
                          fontFamily = FontFamily.Monospace,
                          modifier = Modifier.weight(1f))
                      IconButton(
                          onClick = { copyToClipboard(context, token) },
                          modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription =
                                    stringResource(R.string.mcp_token_copy_description),
                                modifier = Modifier.size(20.dp))
                          }
                    }
              }
        }
      },
      confirmButton = {
        Button(onClick = onDismiss) { Text(stringResource(R.string.help_dialog_confirm)) }
      })
}

private fun copyToClipboard(context: Context, text: String) {
  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
  val clip = ClipData.newPlainText("MCP Token", text)
  clipboard.setPrimaryClip(clip)
  Toast.makeText(context, "Token copied to clipboard", Toast.LENGTH_SHORT).show()
}

private fun formatTimestamp(timestamp: Timestamp): String {
  val formatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
  return formatter.format(timestamp.toDate())
}
