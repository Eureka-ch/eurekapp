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
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
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
 * @param onCreateIdea Callback when idea is created with (title, projectId, participantIds)
 * @param availableProjects List of available projects
 * @param availableUsers List of available users (from selected project)
 * @param isLoading Whether data is loading
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIdeaBottomSheet(
    onDismiss: () -> Unit,
    onCreateIdea: (String?, String, List<String>) -> Unit,
    availableProjects: List<Project>,
    availableUsers: List<User>,
    isLoading: Boolean = false,
    onProjectSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
  var title by remember { mutableStateOf("") }
  var selectedProject by remember { mutableStateOf<Project?>(null) }
  var selectedParticipantIds by remember { mutableStateOf<Set<String>>(emptySet()) }
  var projectDropdownExpanded by remember { mutableStateOf(false) }
  var participantsDropdownExpanded by remember { mutableStateOf(false) }

  // Load users when project is selected
  LaunchedEffect(selectedProject?.projectId) {
    selectedProject?.projectId?.let { projectId ->
      onProjectSelected(projectId)
    }
  }

  ModalBottomSheet(
      onDismissRequest = onDismiss,
      modifier = modifier.testTag(CreateIdeaBottomSheetTestTags.SHEET)) {
        Column(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(Spacing.md)
                    .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
              // Title
              Text(
                  text = "Create New Idea",
                  style = MaterialTheme.typography.headlineSmall,
                  modifier = Modifier.padding(bottom = Spacing.xs))

              // Title field
              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = { Text("Idea Title (Optional)") },
                  placeholder = { Text("Enter a title for your idea...") },
                  modifier =
                      Modifier.fillMaxWidth()
                          .testTag(CreateIdeaBottomSheetTestTags.TITLE_FIELD),
                  singleLine = true,
                  colors = EurekaStyles.textFieldColors())

              // Project selection
              ProjectSelectionField(
                  projects = availableProjects,
                  selectedProject = selectedProject,
                  expanded = projectDropdownExpanded,
                  onExpandedChange = { projectDropdownExpanded = it },
                  onProjectSelected = {
                    selectedProject = it
                    projectDropdownExpanded = false
                    // Clear participants when project changes
                    selectedParticipantIds = emptySet()
                  },
                  isLoading = isLoading)

              // Participants selection (only if project is selected)
              if (selectedProject != null) {
                ParticipantsSelectionField(
                    availableUsers = availableUsers,
                    selectedParticipantIds = selectedParticipantIds,
                    expanded = participantsDropdownExpanded,
                    onExpandedChange = { participantsDropdownExpanded = it },
                    onParticipantToggled = { userId ->
                      val newSet = selectedParticipantIds.toMutableSet()
                      if (newSet.contains(userId)) {
                        newSet.remove(userId)
                      } else {
                        newSet.add(userId)
                      }
                      selectedParticipantIds = newSet
                    },
                    isLoading = isLoading)
              }

              // Buttons
              Row(
                  modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                  horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier =
                            Modifier.weight(1f)
                                .testTag(CreateIdeaBottomSheetTestTags.CANCEL_BUTTON),
                        colors = EurekaStyles.outlinedButtonColors()) {
                          Text("Cancel")
                        }

                    Button(
                        onClick = {
                          val projectId = selectedProject?.projectId
                          if (projectId != null) {
                            onCreateIdea(
                                title.takeIf { it.isNotBlank() },
                                projectId,
                                selectedParticipantIds.toList())
                            onDismiss()
                          }
                        },
                        enabled = selectedProject != null && !isLoading,
                        modifier =
                            Modifier.weight(1f).testTag(CreateIdeaBottomSheetTestTags.CREATE_BUTTON),
                        colors = EurekaStyles.primaryButtonColors()) {
                          Text("Create")
                        }
                  }
            }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectSelectionField(
    projects: List<Project>,
    selectedProject: Project?,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onProjectSelected: (Project) -> Unit,
    isLoading: Boolean
) {
  Column {
    Text(
        text = "Project",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Medium)

    Spacer(modifier = Modifier.height(Spacing.xs))

    if (isLoading && projects.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxWidth().height(56.dp),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
          }
    } else if (projects.isEmpty()) {
      Text(
          text = "No projects available",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      ExposedDropdownMenuBox(
          expanded = expanded,
          onExpandedChange = onExpandedChange,
          modifier = Modifier.fillMaxWidth().testTag(CreateIdeaBottomSheetTestTags.PROJECT_DROPDOWN)) {
            OutlinedTextField(
                value = selectedProject?.name ?: "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("Select a project") },
                label = { Text("Project") },
                trailingIcon = {
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier =
                    Modifier.fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                colors = EurekaStyles.textFieldColors())

            ExposedDropdownMenu(
                expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
                  projects.forEach { project ->
                    DropdownMenuItem(
                        text = { Text(project.name) },
                        onClick = {
                          onProjectSelected(project)
                          onExpandedChange(false)
                        })
                  }
                }
          }
    }

    if (projects.isNotEmpty() && selectedProject == null) {
      Text(
          text = "Please select a project",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
          modifier = Modifier.padding(top = Spacing.xs))
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ParticipantsSelectionField(
    availableUsers: List<User>,
    selectedParticipantIds: Set<String>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onParticipantToggled: (String) -> Unit,
    isLoading: Boolean
) {
  val selectedCount = selectedParticipantIds.size
  val displayText =
      when {
        selectedCount == 0 -> "No participants selected"
        selectedCount == 1 -> {
          val user = availableUsers.firstOrNull { it.uid == selectedParticipantIds.first() }
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

    if (isLoading && availableUsers.isEmpty()) {
      Box(
          modifier = Modifier.fillMaxWidth().height(56.dp),
          contentAlignment = Alignment.Center) {
            CircularProgressIndicator(strokeWidth = 2.dp)
          }
    } else if (availableUsers.isEmpty()) {
      Text(
          text = "No users available in this project",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .clickable { onExpandedChange(!expanded) }
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
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
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
                                    onCheckedChange = null)
                              }
                        },
                        onClick = { onParticipantToggled(user.uid) })
                  }
                }
          }
    }
  }
}
