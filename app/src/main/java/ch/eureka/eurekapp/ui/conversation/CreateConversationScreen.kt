/*
 * CreateConversationScreen.kt
 *
 * Screen for creating new 1-on-1 conversations between project members.
 * Uses a two-step selection flow: first select project, then select member.
 */

package ch.eureka.eurekapp.ui.conversation

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/*
Co-author: GPT-5 Codex
Co-author: Claude 4.5 Sonnet
*/

/** Test tags for the CreateConversationScreen component. */
object CreateConversationScreenTestTags {
  const val SCREEN = "CreateConversationScreen"
  const val TITLE = "CreateConversationTitle"
  const val PROJECT_DROPDOWN = "ProjectDropdown"
  const val PROJECT_DROPDOWN_ITEM = "ProjectDropdownItem"
  const val MEMBER_DROPDOWN = "MemberDropdown"
  const val MEMBER_DROPDOWN_ITEM = "MemberDropdownItem"
  const val CREATE_BUTTON = "CreateButton"
  const val LOADING_INDICATOR = "CreateConversationLoading"
  const val NO_MEMBERS_MESSAGE = "NoMembersMessage"
}

/**
 * Screen for creating a new conversation.
 *
 * Provides a two-step selection flow:
 * 1. Select a project from the user's projects
 * 2. Select a member from that project (excluding the current user)
 *
 * When both are selected, the user can create the conversation. Duplicate conversations are
 * prevented by checking if one already exists.
 *
 * @param onConversationCreated Callback invoked when a conversation is successfully created.
 * @param onNavigateBack Callback invoked when the user wants to go back.
 * @param viewModel The ViewModel managing the create conversation state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateConversationScreen(
    onConversationCreated: () -> Unit,
    onNavigateBack: () -> Unit = {},
    viewModel: CreateConversationViewModel = viewModel()
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  // Local UI state for controlling dropdown menu visibility
  var projectDropdownExpanded by remember { mutableStateOf(false) }
  var memberDropdownExpanded by remember { mutableStateOf(false) }

  // Side effect: Display error messages as toast notifications
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
      viewModel.clearErrorMsg()
    }
  }

  // Side effect: Handle successful conversation creation
  // Shows success toast and navigates back to conversation list
  LaunchedEffect(uiState.conversationCreated) {
    if (uiState.conversationCreated) {
      Toast.makeText(context, "Conversation created!", Toast.LENGTH_SHORT).show()
      viewModel.resetConversationCreated()
      onConversationCreated()
    }
  }

  Scaffold(modifier = Modifier.testTag(CreateConversationScreenTestTags.SCREEN)) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.Top) {
          // Screen title
          Spacer(modifier = Modifier.height(Spacing.md))
          Text(
              text = "New Conversation",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.testTag(CreateConversationScreenTestTags.TITLE))

          Spacer(modifier = Modifier.height(Spacing.xs))

          // Screen description
          Text(
              text = "Select a project and a member to start chatting",
              style = MaterialTheme.typography.bodyMedium,
              color = Color.Gray)

          Spacer(modifier = Modifier.height(Spacing.lg))

          // Step 1: Project selection dropdown
          // User must first select which project context the conversation belongs to
          Text(
              text = "Project",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Medium)
          Spacer(modifier = Modifier.height(Spacing.xs))

          // ExposedDropdownMenuBox provides Material 3 dropdown menu behavior
          ExposedDropdownMenuBox(
              expanded = projectDropdownExpanded,
              onExpandedChange = { projectDropdownExpanded = it },
              modifier = Modifier.testTag(CreateConversationScreenTestTags.PROJECT_DROPDOWN)) {
                OutlinedTextField(
                    value = uiState.selectedProject?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    placeholder = { Text("Select a project") },
                    trailingIcon = {
                      if (uiState.isLoadingProjects) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
                      } else {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded)
                      }
                    },
                    modifier =
                        Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))

                // Project dropdown menu items
                ExposedDropdownMenu(
                    expanded = projectDropdownExpanded,
                    onDismissRequest = { projectDropdownExpanded = false }) {
                      uiState.projects.forEach { project ->
                        DropdownMenuItem(
                            text = { Text(project.name) },
                            onClick = {
                              viewModel.selectProject(project)
                              projectDropdownExpanded = false
                            },
                            modifier =
                                Modifier.testTag(
                                    CreateConversationScreenTestTags.PROJECT_DROPDOWN_ITEM))
                      }
                    }
              }

          Spacer(modifier = Modifier.height(Spacing.md))

          // Step 2: Member selection dropdown
          // Only rendered after a project is selected - members are loaded dynamically
          if (uiState.selectedProject != null) {
            Text(
                text = "Member",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(Spacing.xs))

            if (uiState.isLoadingMembers) {
              // Show loading indicator while members are being fetched
              Box(
                  modifier = Modifier.fillMaxWidth().height(56.dp),
                  contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.testTag(CreateConversationScreenTestTags.LOADING_INDICATOR))
                  }
            } else if (uiState.members.isEmpty()) {
              // Show message when no other members in project
              Text(
                  text = "No other members in this project",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.error,
                  modifier = Modifier.testTag(CreateConversationScreenTestTags.NO_MEMBERS_MESSAGE))
            } else {
              // Member dropdown
              ExposedDropdownMenuBox(
                  expanded = memberDropdownExpanded,
                  onExpandedChange = { memberDropdownExpanded = it },
                  modifier = Modifier.testTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN)) {
                    OutlinedTextField(
                        value = uiState.selectedMember?.user?.displayName ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select a member") },
                        trailingIcon = {
                          ExposedDropdownMenuDefaults.TrailingIcon(
                              expanded = memberDropdownExpanded)
                        },
                        modifier =
                            Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable))

                    // Member dropdown menu items
                    ExposedDropdownMenu(
                        expanded = memberDropdownExpanded,
                        onDismissRequest = { memberDropdownExpanded = false }) {
                          uiState.members.forEach { memberData ->
                            DropdownMenuItem(
                                text = { Text(memberData.user.displayName) },
                                onClick = {
                                  viewModel.selectMember(memberData)
                                  memberDropdownExpanded = false
                                },
                                modifier =
                                    Modifier.testTag(
                                        CreateConversationScreenTestTags.MEMBER_DROPDOWN_ITEM))
                          }
                        }
                  }
            }
          }

          Spacer(modifier = Modifier.height(Spacing.lg))

          // Create button - enabled only when all conditions are met:
          // 1. Project is selected
          // 2. Member is selected
          // 3. Not currently creating (prevents double-tap)
          // 4. Device is online
          val canCreate =
              uiState.selectedProject != null &&
                  uiState.selectedMember != null &&
                  !uiState.isCreating &&
                  uiState.isConnected

          Button(
              onClick = { viewModel.createConversation() },
              enabled = canCreate,
              modifier =
                  Modifier.fillMaxWidth().testTag(CreateConversationScreenTestTags.CREATE_BUTTON)) {
                if (uiState.isCreating) {
                  // Show loading indicator while creating
                  CircularProgressIndicator(
                      color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                  Text("Create Conversation")
                }
              }

          // Offline warning
          if (!uiState.isConnected) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = "You are offline. Cannot create conversations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
          }
        }
  }
}
