/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.model.data.chat.Message
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
 * Ideas Screen - Ready for ViewModel integration.
 *
 * This screen provides a chat interface for discussing project ideas with MCP.
 * The screen is prepared to work with IdeasViewModel when it's implemented.
 *
 * @param onNavigateBack Callback to navigate back.
 * @param modifier Optional modifier.
 * @param viewModel Optional ViewModel for state management. If not provided, will use default viewModel().
 *                  For now, pass null to use placeholder UI state until ViewModel is implemented.
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
        IdeasTopBar(
            selectedProject = uiState.selectedProject,
            availableProjects = uiState.availableProjects,
            projectDropdownExpanded = projectDropdownExpanded,
            onProjectDropdownExpandedChange = { projectDropdownExpanded = it },
            onProjectSelected = { project ->
              viewModel?.selectProject(project)
              projectDropdownExpanded = false
            },
            onNavigateBack = onNavigateBack)
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      floatingActionButton = {
        // FAB pour créer une nouvelle Idea (visible seulement en mode LIST)
        if (uiState.viewMode == IdeasViewMode.LIST) {
          FloatingActionButton(
              onClick = { showCreateIdeaDialog = true },
              modifier = Modifier.testTag("createIdeaButton")) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new idea")
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
        onProjectSelected = { projectId ->
          viewModel?.loadUsersForProject(projectId)
        })
  }
}

/**
 * Interface for IdeasViewModel to allow optional ViewModel injection.
 * When ViewModel is implemented, it should implement this interface.
 */
interface IdeasViewModelInterface {
  val uiState: kotlinx.coroutines.flow.StateFlow<IdeasUIState>
  fun selectProject(project: Project)
  fun selectIdea(idea: Idea)
  fun createNewIdea(title: String?, projectId: String, participantIds: List<String>)
  fun deleteIdea(ideaId: String)
  fun addParticipantToIdea(ideaId: String, userId: String)
  fun updateMessage(message: String)
  fun sendMessage()
  fun clearError()
  fun getCurrentUserId(): String?
  fun loadUsersForProject(projectId: String)
}

// Idea and IdeasViewMode are now in IdeasModels.kt

/**
 * Placeholder UI state function for when ViewModel is not yet implemented.
 */
private fun IdeasUIStatePlaceholder(): IdeasUIState =
    IdeasUIState(
        selectedProject = null,
        availableProjects = emptyList(),
        ideas = emptyList(),
        selectedIdea = null,
        messages = emptyList(),
        viewMode = IdeasViewMode.LIST,
        currentMessage = "",
        isSending = false,
        isLoading = false,
        errorMsg = null,
        availableUsers = emptyList(),
        isLoadingUsers = false)

/**
 * UI state data class for Ideas Screen.
 * This will be used by the actual ViewModel when implemented.
 */
data class IdeasUIState(
    val selectedProject: Project? = null,
    val availableProjects: List<Project> = emptyList(),
    val ideas: List<Idea> = emptyList(),
    val selectedIdea: Idea? = null,
    val messages: List<Message> = emptyList(),
    val viewMode: IdeasViewMode = IdeasViewMode.LIST,
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val availableUsers: List<ch.eureka.eurekapp.model.data.user.User> = emptyList(),
    val isLoadingUsers: Boolean = false
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
    viewMode: IdeasViewMode,
    selectedProject: Project?,
    ideas: List<Idea>,
    selectedIdea: Idea?,
    messages: List<Message>,
    currentUserId: String?,
    listState: LazyListState,
    paddingValues: PaddingValues,
    isLoading: Boolean,
    onIdeaClick: (Idea) -> Unit,
    onDeleteIdea: (String) -> Unit,
    onShareIdea: (String, String) -> Unit
) {
  Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
    when {
      isLoading -> {
        CircularProgressIndicator(
            modifier =
                Modifier.align(Alignment.Center)
                    .testTag(IdeasScreenTestTags.LOADING_INDICATOR))
      }
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
      viewMode == IdeasViewMode.LIST -> {
        // Mode LISTE : Afficher la liste des Ideas
        IdeasListContent(
            ideas = ideas,
            selectedProject = selectedProject,
            onIdeaClick = onIdeaClick,
            onDeleteIdea = onDeleteIdea,
            onShareIdea = onShareIdea)
      }
      viewMode == IdeasViewMode.CONVERSATION -> {
        // Mode CONVERSATION : Afficher les messages de l'Idea sélectionnée
        IdeasConversationContent(
            selectedIdea = selectedIdea,
            messages = messages,
            currentUserId = currentUserId,
            listState = listState)
      }
    }
  }
}

@Composable
private fun IdeasListContent(
    ideas: List<Idea>,
    selectedProject: Project?,
    onIdeaClick: (Idea) -> Unit,
    onDeleteIdea: (String) -> Unit,
    onShareIdea: (String, String) -> Unit
) {
  if (ideas.isEmpty()) {
    // Empty state - no ideas yet
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(Spacing.lg)
                .testTag(IdeasScreenTestTags.EMPTY_STATE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Text(
              text = "No ideas yet for ${selectedProject?.name ?: "this project"}",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center)
          Text(
              text = "Tap the + button to create your first idea",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = Spacing.sm))
        }
  } else {
    // Ideas list
    LazyColumn(
        modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = Spacing.md)
                .testTag("ideasList"),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          items(items = ideas, key = { it.ideaId }) { idea ->
            // TODO: Créer un composant IdeaCard pour afficher chaque Idea
            // Pour l'instant, juste un placeholder
            Card(
                onClick = { onIdeaClick(idea) },
                modifier = Modifier.fillMaxWidth()) {
                  Column(modifier = Modifier.padding(Spacing.md)) {
                    Text(
                        text = idea.title ?: "Untitled Idea",
                        style = MaterialTheme.typography.titleMedium)
                    if (idea.content != null) {
                      Text(
                          text = idea.content,
                          style = MaterialTheme.typography.bodyMedium,
                          maxLines = 2,
                          modifier = Modifier.padding(top = Spacing.xs))
                    }
                  }
                }
          }
        }
  }
}

@Composable
private fun IdeasConversationContent(
    selectedIdea: Idea?,
    messages: List<Message>,
    currentUserId: String?,
    listState: LazyListState
) {
  if (messages.isEmpty()) {
    // Empty state - no messages yet
    Column(
        modifier =
            Modifier.fillMaxSize()
                .padding(Spacing.lg)
                .testTag(IdeasScreenTestTags.EMPTY_STATE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
          Text(
              text = "Start a conversation about ${selectedIdea?.title ?: "this idea"}",
              style = MaterialTheme.typography.bodyLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              textAlign = TextAlign.Center)
          Text(
              text = "Ask questions about the project, tasks, meetings, or discussions",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
              textAlign = TextAlign.Center,
              modifier = Modifier.padding(top = Spacing.sm))
        }
  } else {
    // Messages list
    LazyColumn(
        state = listState,
        modifier =
            Modifier.fillMaxSize()
                .padding(horizontal = Spacing.md)
                .testTag(IdeasScreenTestTags.MESSAGES_LIST),
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
          items(items = messages, key = { it.messageID }) { message ->
            MessageBubble(
                text = message.text,
                timestamp = message.createdAt,
                isFromCurrentUser = message.senderId == currentUserId)
          }
        }
  }
}
