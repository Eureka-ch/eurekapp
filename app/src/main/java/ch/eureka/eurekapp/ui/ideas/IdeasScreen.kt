/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.components.MessageBubble
import ch.eureka.eurekapp.ui.designsystem.tokens.Spacing

/** Test tags for the Ideas Screen. */
object IdeasScreenTestTags {
  const val SCREEN = "ideasScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val MESSAGES_LIST = "messagesList"
  const val PROJECT_SELECTOR = "projectSelector"
}

/**
 * Ideas Screen - UI only implementation.
 *
 * This screen provides a chat interface for discussing project ideas with MCP.
 * Currently only implements the UI with project selection capability.
 * Backend logic (ViewModel, Repository, MCP) will be implemented separately.
 *
 * @param selectedProject The currently selected project, or null if none selected.
 * @param availableProjects List of available projects for selection.
 * @param onProjectSelected Callback when a project is selected.
 * @param onNavigateBack Callback to navigate back.
 * @param modifier Optional modifier.
 */
@Composable
fun IdeasScreen(
    selectedProject: Project? = null,
    availableProjects: List<Project> = emptyList(),
    onProjectSelected: (Project) -> Unit = {},
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
  // Local UI state (no ViewModel for now)
  var currentMessage by remember { mutableStateOf("") }
  var isSending by remember { mutableStateOf(false) }
  var projectDropdownExpanded by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  // Placeholder messages list (empty for now, will be populated by ViewModel later)
  val messages = remember { mutableStateOf<List<MessagePlaceholder>>(emptyList()) }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(IdeasScreenTestTags.SCREEN),
      topBar = {
        IdeasTopBar(
            selectedProject = selectedProject,
            availableProjects = availableProjects,
            projectDropdownExpanded = projectDropdownExpanded,
            onProjectDropdownExpandedChange = { projectDropdownExpanded = it },
            onProjectSelected = { project ->
              onProjectSelected(project)
              projectDropdownExpanded = false
            },
            onNavigateBack = onNavigateBack)
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (selectedProject != null) {
          MessageInputField(
              message = currentMessage,
              onMessageChange = { currentMessage = it },
              onSend = {
                // Placeholder: will be handled by ViewModel later
                if (currentMessage.isNotBlank() && !isSending) {
                  isSending = true
                  // TODO: Send message via ViewModel
                  currentMessage = ""
                  isSending = false
                }
              },
              isSending = isSending,
              placeholder = "Ask about the project...")
        }
      }) { paddingValues ->
        IdeasContent(
            selectedProject = selectedProject,
            messages = messages.value,
            listState = listState,
            paddingValues = paddingValues)
      }
}

/**
 * Placeholder data class for messages.
 * Will be replaced with actual Message model when ViewModel is implemented.
 */
private data class MessagePlaceholder(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdeasTopBar(
    selectedProject: Project?,
    availableProjects: List<Project>,
    projectDropdownExpanded: Boolean,
    onProjectDropdownExpandedChange: (Boolean) -> Unit,
    onProjectSelected: (Project) -> Unit,
    onNavigateBack: () -> Unit
) {
  TopAppBar(
      title = {
        ProjectSelectorInTopBar(
            selectedProject = selectedProject,
            availableProjects = availableProjects,
            expanded = projectDropdownExpanded,
            onExpandedChange = onProjectDropdownExpandedChange,
            onProjectSelected = onProjectSelected)
      },
      navigationIcon = {
        BackButton(
            onClick = onNavigateBack,
            modifier = Modifier.testTag(IdeasScreenTestTags.PROJECT_SELECTOR))
      },
      colors =
          TopAppBarDefaults.topAppBarColors(
              containerColor = MaterialTheme.colorScheme.primary,
              titleContentColor = MaterialTheme.colorScheme.onPrimary,
              actionIconContentColor = MaterialTheme.colorScheme.onPrimary))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectSelectorInTopBar(
    selectedProject: Project?,
    availableProjects: List<Project>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onProjectSelected: (Project) -> Unit
) {
  ExposedDropdownMenuBox(
      expanded = expanded,
      onExpandedChange = onExpandedChange,
      modifier = Modifier.testTag(IdeasScreenTestTags.PROJECT_SELECTOR)) {
        OutlinedTextField(
            value = selectedProject?.name ?: "Select a project",
            onValueChange = {},
            readOnly = true,
            placeholder = { Text("Select a project") },
            trailingIcon = {
              ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            colors =
                OutlinedTextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                    focusedContainerColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primary))

        ExposedDropdownMenu(
            expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
              if (availableProjects.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("No projects available") },
                    onClick = {},
                    enabled = false)
              } else {
                availableProjects.forEach { project ->
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
}

@Composable
private fun IdeasContent(
    selectedProject: Project?,
    messages: List<MessagePlaceholder>,
    listState: LazyListState,
    paddingValues: PaddingValues
) {
  Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    when {
      selectedProject == null -> {
        // No project selected
        Text(
            text = "Please select a project to start",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier =
                Modifier.align(Alignment.Center)
                    .padding(Spacing.lg)
                    .testTag(IdeasScreenTestTags.EMPTY_STATE))
      }
      messages.isEmpty() -> {
        // Empty state - no messages yet
        Column(
            modifier =
                Modifier.align(Alignment.Center)
                    .padding(Spacing.lg)
                    .testTag(IdeasScreenTestTags.EMPTY_STATE),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
              Text(
                  text = "Start a conversation about ${selectedProject.name}",
                  style = MaterialTheme.typography.bodyLarge,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  textAlign = TextAlign.Center)
              Text(
                  text = "Ask questions about the project, tasks, meetings, or discussions",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                  textAlign = TextAlign.Center)
            }
      }
      else -> {
        // Messages list
        LazyColumn(
            state = listState,
            modifier =
                Modifier.fillMaxSize()
                    .padding(horizontal = Spacing.md)
                    .testTag(IdeasScreenTestTags.MESSAGES_LIST),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
              items(items = messages, key = { it.id }) { message ->
                MessageBubble(
                    text = message.text,
                    timestamp = null, // TODO: Use actual Timestamp when Message model is available
                    isFromCurrentUser = message.isFromUser)
              }
            }
      }
    }
  }
}
