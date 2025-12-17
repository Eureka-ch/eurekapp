/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.ideas.Idea
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.designsystem.tokens.EurekaStyles
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Test tags for Create Idea Bottom Sheet */
object CreateIdeaBottomSheetTestTags {
  const val SHEET = "createIdeaBottomSheet"
  const val TITLE_FIELD = "ideaTitleField"
  const val PROJECT_DROPDOWN = "ideaProjectDropdown"
  const val PARTICIPANTS_DROPDOWN = "ideaParticipantsDropdown"
  const val CREATE_BUTTON = "createIdeaButton"
  const val CANCEL_BUTTON = "cancelCreateIdeaButton"
}

/**
 * Bottom sheet for creating a new Idea.
 *
 * Allows user to:
 * - Enter a title for the idea
 * - Select a project
 * - Add participants (users) to share the idea with
 *
 * @param onDismiss Callback to dismiss the bottom sheet
 * @param onIdeaCreated Callback when idea is successfully created with the new idea
 * @param viewModel ViewModel managing the create idea state
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIdeaBottomSheet(
    onDismiss: () -> Unit,
    onIdeaCreated: (Idea) -> Unit,
    viewModel: CreateIdeaViewModel,
    modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  // Handle navigation when idea is created
  LaunchedEffect(uiState.navigateToIdea) {
    uiState.navigateToIdea?.let { idea ->
      onIdeaCreated(idea)
      viewModel.resetNavigation()
      onDismiss()
    }
  }

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      modifier = modifier.testTag(CreateIdeaBottomSheetTestTags.SHEET),
      shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Column(
              modifier =
                  Modifier.fillMaxWidth().padding(Spacing.md).verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    text = stringResource(R.string.create_idea_title),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = Spacing.xs))

                // Error display
                uiState.errorMsg?.let { error ->
                  Text(
                      text = error,
                      color = MaterialTheme.colorScheme.error,
                      style = MaterialTheme.typography.bodyMedium,
                      modifier = Modifier.padding(vertical = Spacing.xs))
                }

                TitleField(title = uiState.title, onTitleChange = { viewModel.updateTitle(it) })

                ProjectSelector(
                    availableProjects = uiState.availableProjects,
                    selectedProject = uiState.selectedProject,
                    onProjectSelect = { viewModel.selectProject(it) })

                if (uiState.selectedProject != null) {
                  ParticipantsSelector(
                      availableUsers = uiState.availableUsers,
                      selectedParticipantIds = uiState.selectedParticipantIds.toList(),
                      onToggleParticipant = { viewModel.toggleParticipant(it) })
                }

                ActionButtons(
                    isCreating = uiState.isCreating,
                    canCreate = uiState.selectedProject != null,
                    onCancel = {
                      viewModel.reset()
                      onDismiss()
                    },
                    onCreate = { viewModel.createIdea() })
              }
        }
      }
}

@Composable
private fun TitleField(title: String, onTitleChange: (String) -> Unit) {
  OutlinedTextField(
      value = title,
      onValueChange = onTitleChange,
      label = { Text(stringResource(R.string.create_idea_title_label)) },
      placeholder = { Text(stringResource(R.string.create_idea_title_placeholder)) },
      modifier = Modifier.fillMaxWidth().testTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD),
      singleLine = true,
      shape = RoundedCornerShape(16.dp),
      colors = EurekaStyles.textFieldColors())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectSelector(
    availableProjects: List<Project>,
    selectedProject: Project?,
    onProjectSelect: (Project) -> Unit
) {
  if (availableProjects.isEmpty()) {
    Column {
      Text(
          text = stringResource(R.string.create_idea_project_label),
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium)
      Spacer(modifier = Modifier.height(Spacing.xs))
      Text(
          text = stringResource(R.string.create_idea_no_projects),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  } else {
    var projectDropdownExpanded by remember { mutableStateOf(false) }
    Column {
      ExposedDropdownMenuBox(
          expanded = projectDropdownExpanded,
          onExpandedChange = { projectDropdownExpanded = it },
          modifier =
              Modifier.fillMaxWidth().testTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN)) {
            OutlinedTextField(
                value = selectedProject?.name ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text(stringResource(R.string.create_idea_project_placeholder)) },
                label = { Text(stringResource(R.string.create_idea_project_label)) },
                trailingIcon = {
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded)
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                shape = RoundedCornerShape(16.dp),
                colors = EurekaStyles.textFieldColors())
            ExposedDropdownMenu(
                expanded = projectDropdownExpanded,
                onDismissRequest = { projectDropdownExpanded = false }) {
                  availableProjects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = {
                          onProjectSelect(project)
                          projectDropdownExpanded = false
                        })
                  }
                }
          }
      if (selectedProject == null) {
        Text(
            text = stringResource(R.string.create_idea_project_error),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Spacing.xs))
      }
    }
  }
}

/**
 * Selector component for choosing participants to share the idea with.
 *
 * Displays a read-only text field that opens a scrollable modal dialog when clicked. Users can
 * select/deselect participants using checkboxes in the modal.
 *
 * @param availableUsers List of users available in the selected project
 * @param selectedParticipantIds List of user IDs currently selected as participants
 * @param onToggleParticipant Callback invoked when a participant is toggled
 */
@Composable
private fun ParticipantsSelector(
    availableUsers: List<User>,
    selectedParticipantIds: List<String>,
    onToggleParticipant: (String) -> Unit
) {
  var showParticipantsModal by remember { mutableStateOf(false) }
  val selectedCount = selectedParticipantIds.size
  val displayText =
      when {
        selectedCount == 0 -> stringResource(R.string.create_idea_no_participants_selected)
        selectedCount == 1 -> {
          val user = availableUsers.firstOrNull { it.uid == selectedParticipantIds.first() }
          user?.displayName?.ifBlank { user.email }
              ?: stringResource(R.string.create_idea_single_participant_selected, "1")
        }
        else -> stringResource(R.string.create_idea_multiple_participants_selected, selectedCount)
      }

  Column {
    Text(
        text = stringResource(R.string.create_idea_participants_label),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(Spacing.xs))
    if (availableUsers.isEmpty()) {
      Text(
          text = stringResource(R.string.create_idea_no_users),
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      OutlinedButton(
          onClick = { showParticipantsModal = true },
          modifier =
              Modifier.fillMaxWidth().testTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN),
          shape = RoundedCornerShape(16.dp),
          colors = EurekaStyles.outlinedButtonColors()) {
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 8.dp))
          }
    }
  }

  // Participants selection modal
  if (showParticipantsModal) {
    ParticipantsSelectionModal(
        availableUsers = availableUsers,
        selectedParticipantIds = selectedParticipantIds,
        onToggleParticipant = onToggleParticipant,
        onDismiss = { showParticipantsModal = false })
  }
}

@Composable
private fun ParticipantsSelectionModal(
    availableUsers: List<User>,
    selectedParticipantIds: List<String>,
    onToggleParticipant: (String) -> Unit,
    onDismiss: () -> Unit
) {
  Dialog(
      onDismissRequest = onDismiss,
      properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier =
                Modifier.fillMaxWidth()
                    .fillMaxHeight(0.7f)
                    .padding(Spacing.md)
                    .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
              Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(Spacing.md)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                      Text(
                          text = stringResource(R.string.create_idea_select_participants_title),
                          style = MaterialTheme.typography.titleLarge,
                          fontWeight = FontWeight.Bold)
                      Button(
                          onClick = onDismiss,
                          colors = EurekaStyles.primaryButtonColors(),
                          shape = RoundedCornerShape(12.dp)) {
                            Text(stringResource(R.string.create_idea_ok_button))
                          }
                    }
                Spacer(modifier = Modifier.height(Spacing.md))

                // Scrollable list of participants
                Column(
                    modifier =
                        Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                      availableUsers.forEach { user ->
                        Row(
                            modifier =
                                Modifier.fillMaxWidth()
                                    .clickable { onToggleParticipant(user.uid) }
                                    .padding(vertical = Spacing.sm, horizontal = Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                              Text(
                                  text = user.displayName.ifBlank { user.email },
                                  style = MaterialTheme.typography.bodyLarge)
                              Checkbox(
                                  checked = selectedParticipantIds.contains(user.uid),
                                  onCheckedChange = { onToggleParticipant(user.uid) })
                            }
                      }
                    }
              }
            }
      }
}

@Composable
private fun ActionButtons(
    isCreating: Boolean,
    canCreate: Boolean,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
  Row(
      modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
      horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f).testTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON),
            shape = RoundedCornerShape(16.dp),
            colors = EurekaStyles.outlinedButtonColors()) {
              Text(stringResource(R.string.create_idea_cancel_button))
            }

        Button(
            onClick = onCreate,
            enabled = canCreate && !isCreating,
            modifier = Modifier.weight(1f).testTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON),
            shape = RoundedCornerShape(16.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    disabledContentColor =
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))) {
              if (isCreating) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
              } else {
                Text(
                    stringResource(R.string.create_idea_create_button),
                    fontWeight = FontWeight.Bold)
              }
            }
      }
}
