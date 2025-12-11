/*
Note: This file was co-authored by Claude Code.
Note: This file was co-authored by Grok.
Co-author: Claude 4.5 Sonnet
Portions of the code in this file are inspired by the Bootcamp solution B3 provided by the SwEnt staff.
*/
package ch.eureka.eurekapp.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.EurekaInfoCard
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

/** Test tags for the Profile screen UI elements. */
object ProfileScreenTestTags {
  const val PROFILE_SCREEN = "ProfileScreen"
  const val PROFILE_PICTURE = "ProfilePicture"
  const val DISPLAY_NAME_TEXT = "DisplayNameText"
  const val DISPLAY_NAME_FIELD = "DisplayNameField"
  const val EMAIL_TEXT = "EmailText"
  const val LAST_ACTIVE_TEXT = "LastActiveText"
  const val EDIT_BUTTON = "EditButton"
  const val SAVE_BUTTON = "SaveButton"
  const val CANCEL_BUTTON = "CancelButton"
  const val LOADING_INDICATOR = "LoadingIndicator"
  const val ERROR_TEXT = "ErrorText"
  const val SIGN_OUT_BUTTON = "SignOutButton"
  const val SETTINGS_BUTTON = "SettingsButton"
  const val MCP_TOKENS_BUTTON = "McpTokensButton"
}

/**
 * Profile screen displaying user information and edit capabilities.
 *
 * @param viewModel The [ProfileViewModel] managing the screen's state and logic.
 * @param firebaseAuth The [FirebaseAuth] instance for authentication operations.
 * @param onNavigateToActivityFeed Callback to navigate to activity feed screen.
 * @param onNavigateToPreferences Callback to navigate to preferences screen.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    onNavigateToActivityFeed: () -> Unit = {},
    onNavigateToMcpTokens: () -> Unit = {},
    onNavigateToPreferences: () -> Unit = {},
) {
  val uiState by viewModel.uiState.collectAsState()

  var editedDisplayName by remember { mutableStateOf("") }

  LaunchedEffect(uiState.user) {
    uiState.user?.let { user -> editedDisplayName = user.displayName }
  }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Profile",
            actions = {
              IconButton(
                  onClick = onNavigateToMcpTokens,
                  modifier = Modifier.testTag(ProfileScreenTestTags.MCP_TOKENS_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "MCP Tokens",
                        tint = MaterialTheme.colorScheme.onPrimary)
                  }
              IconButton(
                  onClick = onNavigateToPreferences,
                  modifier = Modifier.testTag(ProfileScreenTestTags.SETTINGS_BUTTON)) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onPrimary)
                  }
            })
      },
      containerColor = Color.White) { paddingValues ->
        Box(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .testTag(ProfileScreenTestTags.PROFILE_SCREEN)) {
              Column(
                  modifier =
                      Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Top) {
                    uiState.user?.let { user ->
                      Spacer(modifier = Modifier.height(32.dp))

                      // Profile Picture (real google profile picture)
                      if (user.photoUrl.isNotEmpty()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier.size(120.dp)
                                    .clip(CircleShape)
                                    .testTag(ProfileScreenTestTags.PROFILE_PICTURE),
                            contentScale = ContentScale.Crop)
                      } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier =
                                Modifier.size(120.dp)
                                    .testTag(ProfileScreenTestTags.PROFILE_PICTURE),
                            tint = MaterialTheme.colorScheme.primary)
                      }

                      Spacer(modifier = Modifier.height(24.dp))

                      if (uiState.isEditing) {
                        OutlinedTextField(
                            value = editedDisplayName,
                            onValueChange = {
                              if (it.length <= 50) {
                                editedDisplayName = it
                              }
                            },
                            label = { Text("Display Name") },
                            supportingText = { Text("${editedDisplayName.length}/50") },
                            singleLine = true,
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag(ProfileScreenTestTags.DISPLAY_NAME_FIELD))
                      } else {
                        Text(
                            text = user.displayName,
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.testTag(ProfileScreenTestTags.DISPLAY_NAME_TEXT))

                        IconButton(
                            onClick = { viewModel.setEditing(true) },
                            modifier = Modifier.testTag(ProfileScreenTestTags.EDIT_BUTTON)) {
                              Icon(Icons.Default.Edit, contentDescription = "Edit Name")
                            }
                      }

                      Spacer(modifier = Modifier.height(16.dp))

                      EurekaInfoCard(
                          title = "Email",
                          primaryValue = user.email,
                          modifier = Modifier.testTag(ProfileScreenTestTags.EMAIL_TEXT))

                      Spacer(modifier = Modifier.height(8.dp))

                      val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                      val lastActiveDate = user.lastActive.toDate()
                      EurekaInfoCard(
                          title = "Last Active",
                          primaryValue = dateFormat.format(lastActiveDate),
                          modifier = Modifier.testTag(ProfileScreenTestTags.LAST_ACTIVE_TEXT))

                      Spacer(modifier = Modifier.height(24.dp))

                      Button(
                          onClick = onNavigateToActivityFeed,
                          modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.primaryContainer,
                                  contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                            Text("View Activity Feed")
                          }

                      if (!uiState.isEditing) {
                        Button(
                            onClick = { firebaseAuth.signOut() },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .testTag(ProfileScreenTestTags.SIGN_OUT_BUTTON),
                            colors =
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError)) {
                              Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                              Spacer(modifier = Modifier.size(8.dp))
                              Text("Sign Out")
                            }
                      }

                      Spacer(modifier = Modifier.height(16.dp))

                      // Save/Cancel Buttons (shown when editing)
                      if (uiState.isEditing) {
                        Button(
                            onClick = { viewModel.updateDisplayName(editedDisplayName) },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag(ProfileScreenTestTags.SAVE_BUTTON)) {
                              Text("Save")
                            }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                              viewModel.setEditing(false)
                              editedDisplayName = user.displayName
                            },
                            modifier =
                                Modifier.fillMaxWidth()
                                    .testTag(ProfileScreenTestTags.CANCEL_BUTTON)) {
                              Text("Cancel")
                            }
                      }
                    }
                        ?: run {
                          Text(
                              text = "No user data available",
                              modifier = Modifier.testTag(ProfileScreenTestTags.ERROR_TEXT))
                        }
                  }
            }
      }
}
