/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
      modifier = modifier.testTag(CreateIdeaBottomSheetTestTags.SHEET)) {
        Box(modifier = Modifier.fillMaxWidth()) {
          Column(
              modifier =
                  Modifier.fillMaxWidth().padding(Spacing.md).verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Text(
                    text = "Create New Idea",
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

                TitleField(
                    title = uiState.title,
                    onTitleChange = { viewModel.updateTitle(it) })

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
      label = { Text("Idea Title (Optional)") },
      placeholder = { Text("Enter a title for your idea...") },
      modifier = Modifier.fillMaxWidth().testTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD),
      singleLine = true,
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
          text = "Project",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Medium)
      Spacer(modifier = Modifier.height(Spacing.xs))
      Text(
          text = "No projects available",
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
                placeholder = { Text("Select a project") },
                label = { Text("Project") },
                trailingIcon = {
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded)
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
            text = "Please select a project",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = Spacing.xs))
      }
    }
  }
}

@Composable
private fun ParticipantsSelector(
    availableUsers: List<User>,
    selectedParticipantIds: List<String>,
    onToggleParticipant: (String) -> Unit
) {
  var participantsDropdownExpanded by remember { mutableStateOf(false) }
  val selectedCount = selectedParticipantIds.size
  val displayText =
      when {
        selectedCount == 0 -> "No participants selected"
        selectedCount == 1 -> {
          val user =
              availableUsers.firstOrNull { it.uid == selectedParticipantIds.first() }
          user?.displayName?.ifBlank { user.email } ?: "1 participant selected"
        }
        else -> "$selectedCount participants selected"
      }

  Column {
    Text(
        text = "Add Participants (Optional)",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium)
    Spacer(modifier = Modifier.height(Spacing.xs))
    if (availableUsers.isEmpty()) {
      Text(
          text = "No users available in this project",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .clickable { participantsDropdownExpanded = !participantsDropdownExpanded }
                  .testTag(CreateIdeaBottomSheetTestTags.PARTICIPANTS_DROPDOWN)) {
            OutlinedTextField(
                value = displayText,
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select participants to share with") },
                label = { Text("Participants") },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                colors = EurekaStyles.textFieldColors())
            DropdownMenu(
                expanded = participantsDropdownExpanded,
                onDismissRequest = { participantsDropdownExpanded = false },
                modifier = Modifier.fillMaxWidth()) {
                  availableUsers.forEach { user ->
                    DropdownMenuItem(
                        text = {
                          Row(
                              modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                              horizontalArrangement = Arrangement.SpaceBetween,
                              verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.displayName.ifBlank { user.email },
                                    style = MaterialTheme.typography.bodyMedium)
                                Checkbox(
                                    checked = selectedParticipantIds.contains(user.uid),
                                    onCheckedChange = { onToggleParticipant(user.uid) })
                              }
                        },
                        onClick = { onToggleParticipant(user.uid) })
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
            modifier =
                Modifier.weight(1f).testTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON),
            colors = EurekaStyles.outlinedButtonColors()) {
              Text("Cancel")
            }

        Button(
            onClick = onCreate,
            enabled = canCreate && !isCreating,
            modifier =
                Modifier.weight(1f).testTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON),
            colors = EurekaStyles.primaryButtonColors()) {
              if (isCreating) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
              } else {
                Text("Create")
              }
            }
      }
}
