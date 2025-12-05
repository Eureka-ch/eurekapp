/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.MessageInputField
import ch.eureka.eurekapp.ui.ideas.IdeasContent
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.ui.components.BackButton

/** Test tags for the Ideas Screen. */
object IdeasScreenTestTags {
  const val SCREEN = "ideasScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val MESSAGES_LIST = "messagesList"
  const val PROJECT_SELECTOR = "projectSelector"
}

/**
 * Ideas Screen - Ready for ViewModel integration.
 *
 * This screen provides a chat interface for discussing project ideas with MCP. The screen is
 * prepared to work with IdeasViewModel when it's implemented.
 *
 * @param onNavigateBack Callback to navigate back.
 * @param modifier Optional modifier.
 * @param viewModel Optional ViewModel for state management. If not provided, will use default
 *   viewModel(). For now, pass null to use placeholder UI state until ViewModel is implemented.
 */
@Composable
fun IdeasScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: IdeasViewModelInterface? = null
) {
  // Use ViewModel if provided, otherwise use placeholder state
  val uiState =
      if (viewModel != null) {
        viewModel.uiState.collectAsState().value
      } else {
        // Placeholder UI state until ViewModel is implemented
        IdeasUIStatePlaceholder()
      }

  var projectDropdownExpanded by remember { mutableStateOf(false) }
  var showCreateIdeaDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  // Handle error messages
  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      snackbarHostState.showSnackbar(errorMsg)
      viewModel?.clearError()
    }
  }

  // Auto-scroll when new messages arrive
  LaunchedEffect(uiState.messages.size) {
    if (uiState.messages.isNotEmpty() && !uiState.isLoading) {
      listState.animateScrollToItem(0)
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(IdeasScreenTestTags.SCREEN),
      topBar = {
        @OptIn(ExperimentalMaterial3Api::class)
        TopAppBar(
            title = {
              ExposedDropdownMenuBox(
                  expanded = projectDropdownExpanded,
                  onExpandedChange = { projectDropdownExpanded = it },
                  modifier = Modifier.testTag(IdeasScreenTestTags.PROJECT_SELECTOR)) {
                    OutlinedTextField(
                        value = uiState.selectedProject?.name ?: "Select a project",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select a project") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = projectDropdownExpanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimary,
                            focusedContainerColor = MaterialTheme.colorScheme.primary,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primary))
                    ExposedDropdownMenu(expanded = projectDropdownExpanded, onDismissRequest = { projectDropdownExpanded = false }) {
                      if (uiState.availableProjects.isEmpty()) {
                        DropdownMenuItem(text = { Text("No projects available") }, onClick = {}, enabled = false)
                      } else {
                        uiState.availableProjects.forEach { project ->
                          DropdownMenuItem(
                              text = { Text(project.name) },
                              onClick = {
                                viewModel?.selectProject(project)
                                projectDropdownExpanded = false
                              })
                        }
                      }
                    }
                  }
            },
            navigationIcon = { BackButton(onClick = onNavigateBack, modifier = Modifier.testTag(IdeasScreenTestTags.PROJECT_SELECTOR)) },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primary,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary))
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      floatingActionButton = {
        // FAB pour créer une nouvelle Idea (visible seulement en mode LIST)
        if (uiState.viewMode == IdeasViewMode.LIST) {
          FloatingActionButton(
              onClick = { showCreateIdeaDialog = true },
              modifier = Modifier.testTag("createIdeaButton")) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create new idea")
              }
        }
      },
      bottomBar = {
        // Input field visible seulement en mode CONVERSATION
        if (uiState.viewMode == IdeasViewMode.CONVERSATION && uiState.selectedIdea != null) {
          MessageInputField(
              message = uiState.currentMessage,
              onMessageChange = { viewModel?.updateMessage(it) },
              onSend = { viewModel?.sendMessage() },
              isSending = uiState.isSending,
              placeholder = "Ask about the project...")
        }
      }) { paddingValues ->
        IdeasContent(
            viewMode = uiState.viewMode,
            selectedProject = uiState.selectedProject,
            ideas = uiState.ideas,
            selectedIdea = uiState.selectedIdea,
            messages = uiState.messages,
            currentUserId = viewModel?.getCurrentUserId(),
            listState = listState,
            paddingValues = paddingValues,
            isLoading = uiState.isLoading,
            onIdeaClick = { idea -> viewModel?.selectIdea(idea) },
            onDeleteIdea = { ideaId -> viewModel?.deleteIdea(ideaId) },
            onShareIdea = { ideaId, userId -> viewModel?.addParticipantToIdea(ideaId, userId) })
      }

  // Create Idea Dialog
  if (showCreateIdeaDialog) {
    CreateIdeaBottomSheet(
        onDismiss = { showCreateIdeaDialog = false },
        onCreateIdea = { title, projectId, participantIds ->
          viewModel?.createNewIdea(title, projectId, participantIds)
        },
        availableProjects = uiState.availableProjects,
        availableUsers = uiState.availableUsers,
        isLoading = uiState.isLoadingUsers,
        onProjectSelected = { projectId -> viewModel?.loadUsersForProject(projectId) })
  }
}


