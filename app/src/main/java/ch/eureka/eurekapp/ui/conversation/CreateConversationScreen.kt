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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.R
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
 * When both are selected, the user can create the conversation. If a conversation already exists,
 * navigates to it directly.
 *
 * @param onNavigateToConversation Callback invoked with conversation ID to navigate to.
 * @param viewModel The ViewModel managing the create conversation state.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateConversationScreen(
    onNavigateToConversation: (String) -> Unit,
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

  // Side effect: Navigate to conversation when created or existing one found
  LaunchedEffect(uiState.navigateToConversationId) {
    uiState.navigateToConversationId?.let { conversationId ->
      viewModel.resetNavigation()
      onNavigateToConversation(conversationId)
    }
  }

  Scaffold(modifier = Modifier.testTag(CreateConversationScreenTestTags.SCREEN)) { padding ->
    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.Top) {
          Spacer(modifier = Modifier.height(Spacing.md))

          ScreenHeader()

          Spacer(modifier = Modifier.height(Spacing.lg))

          ProjectDropdown(
              uiState = uiState,
              projectDropdownExpanded = projectDropdownExpanded,
              onExpandedChange = { projectDropdownExpanded = it },
              onProjectSelect = { project ->
                viewModel.selectProject(project)
                projectDropdownExpanded = false
              })

          Spacer(modifier = Modifier.height(Spacing.md))

          if (uiState.selectedProject != null) {
            MemberSelection(
                uiState = uiState,
                memberDropdownExpanded = memberDropdownExpanded,
                onExpandedChange = { memberDropdownExpanded = it },
                onMemberSelect = { member -> viewModel.selectMember(member) },
                onMemberDeselect = { viewModel.removeMember(it) })
          }

          Spacer(modifier = Modifier.height(Spacing.lg))

          CreateButton(uiState = uiState, onClick = { viewModel.createConversation() })

          if (!uiState.isConnected) {
            Spacer(modifier = Modifier.height(Spacing.sm))
            Text(
                text = stringResource(R.string.create_conversation_offline_message),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error)
          }
        }
  }
}

@Composable
private fun ScreenHeader() {
  Text(
      text = stringResource(R.string.create_conversation_title),
      style = MaterialTheme.typography.headlineSmall,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.testTag(CreateConversationScreenTestTags.TITLE))
  Spacer(modifier = Modifier.height(Spacing.xs))
  Text(
      text = stringResource(R.string.create_conversation_description),
      style = MaterialTheme.typography.bodyMedium,
      color = Color.Gray)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectDropdown(
    uiState: CreateConversationState,
    projectDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onProjectSelect: (ch.eureka.eurekapp.model.data.project.Project) -> Unit
) {
  Text(
      text = stringResource(R.string.create_conversation_project_label),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Medium)
  Spacer(modifier = Modifier.height(Spacing.xs))

  ExposedDropdownMenuBox(
      expanded = projectDropdownExpanded,
      onExpandedChange = onExpandedChange,
      modifier = Modifier.testTag(CreateConversationScreenTestTags.PROJECT_DROPDOWN)) {
        OutlinedTextField(
            value = uiState.selectedProject?.name ?: "",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text(stringResource(R.string.create_conversation_project_placeholder)) },
            trailingIcon = {
              if (uiState.isLoadingProjects) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(end = 8.dp), strokeWidth = 2.dp)
              } else {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded)
              }
            },
            modifier =
                Modifier.fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))

        ExposedDropdownMenu(
            expanded = projectDropdownExpanded, onDismissRequest = { onExpandedChange(false) }) {
              uiState.projects.forEach { project ->
                DropdownMenuItem(
                    text = { Text(project.name) },
                    onClick = { onProjectSelect(project) },
                    modifier =
                        Modifier.testTag(CreateConversationScreenTestTags.PROJECT_DROPDOWN_ITEM))
              }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MemberSelection(
    uiState: CreateConversationState,
    memberDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onMemberSelect: (MemberDisplayData) -> Unit,
    onMemberDeselect: (MemberDisplayData) -> Unit
) {
  Text(
      text = stringResource(R.string.create_conversation_member_label),
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.Medium)
  Spacer(modifier = Modifier.height(Spacing.xs))

  when {
    uiState.isLoadingMembers -> {
      Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            modifier = Modifier.testTag(CreateConversationScreenTestTags.LOADING_INDICATOR))
      }
    }
    uiState.members.isEmpty() -> {
      Text(
          text = stringResource(R.string.create_conversation_no_members),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.error,
          modifier = Modifier.testTag(CreateConversationScreenTestTags.NO_MEMBERS_MESSAGE))
    }
    else -> {
      ExposedDropdownMenuBox(
          expanded = memberDropdownExpanded,
          onExpandedChange = onExpandedChange,
          modifier = Modifier.testTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN)) {
            OutlinedTextField(
                value = stringResource(R.string.create_conversation_members_selected, uiState.selectedMembers.size),
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(stringResource(R.string.create_conversation_member_placeholder)) },
                trailingIcon = {
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded)
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable))

            ExposedDropdownMenu(
                expanded = memberDropdownExpanded, onDismissRequest = { onExpandedChange(false) }) {
                  uiState.members.forEach { memberData ->
                    MemberDropdownItem(
                        selectedMembersList = uiState.selectedMembers,
                        memberData = memberData,
                        onMemberSelect = onMemberSelect,
                        onMemberDeselect = onMemberDeselect)
                  }
                }
          }
    }
  }
}

@Composable
private fun MemberDropdownItem(
    selectedMembersList: List<MemberDisplayData>,
    memberData: MemberDisplayData,
    onMemberSelect: (MemberDisplayData) -> Unit,
    onMemberDeselect: (MemberDisplayData) -> Unit
) {
  DropdownMenuItem(
      modifier = Modifier.testTag(CreateConversationScreenTestTags.MEMBER_DROPDOWN_ITEM),
      text = {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically) {
              Row(
                  modifier = Modifier.weight(1f),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = selectedMembersList.contains(memberData),
                        onCheckedChange = { value ->
                          if (value) {
                            onMemberSelect(memberData)
                          } else {
                            onMemberDeselect(memberData)
                          }
                        })
                  }
              Row(
                  modifier = Modifier.weight(3f),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically) {
                    Text(memberData.user.displayName)
                  }
            }
      },
      onClick = {
        if (selectedMembersList.contains(memberData)) {
          onMemberDeselect(memberData)
        } else {
          onMemberSelect(memberData)
        }
      })
}

@Composable
private fun CreateButton(uiState: CreateConversationState, onClick: () -> Unit) {
  val canCreate =
      uiState.selectedProject != null &&
          uiState.selectedMembers.isNotEmpty() &&
          !uiState.isCreating &&
          uiState.isConnected

  Button(
      onClick = onClick,
      enabled = canCreate,
      modifier = Modifier.fillMaxWidth().testTag(CreateConversationScreenTestTags.CREATE_BUTTON)) {
        if (uiState.isCreating) {
          CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
        } else {
          Text(stringResource(R.string.create_conversation_button))
        }
      }
}
