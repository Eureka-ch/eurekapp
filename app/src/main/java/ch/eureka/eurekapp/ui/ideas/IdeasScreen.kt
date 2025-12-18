/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar

/** Test tags for the Ideas Screen. */
object IdeasScreenTestTags {
  const val SCREEN = "ideasScreen"
  const val LOADING_INDICATOR = "loadingIndicator"
  const val EMPTY_STATE = "emptyState"
  const val MESSAGES_LIST = "messagesList"
  const val PROJECT_SELECTOR = "projectSelector"
  const val BACK_BUTTON = "backButton"
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
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onIdeaClick: ((String, String?) -> Unit)? = null,
    viewModel: IdeasViewModel = viewModel()
) {
  val uiState by viewModel.uiState.collectAsState()

  var projectDropdownExpanded by remember { mutableStateOf(false) }
  var showCreateIdeaDialog by remember { mutableStateOf(false) }
  val snackbarHostState = remember { SnackbarHostState() }
  val listState = rememberLazyListState()

  LaunchedEffect(uiState.errorMsg) {
    uiState.errorMsg?.let { errorMsg ->
      snackbarHostState.showSnackbar(errorMsg)
      viewModel.clearError()
    }
  }

  LaunchedEffect(uiState.messages.size) {
    if (uiState.messages.isNotEmpty() && !uiState.isLoading) {
      listState.animateScrollToItem(0)
    }
  }

  Scaffold(
      modifier = modifier.fillMaxSize().testTag(IdeasScreenTestTags.SCREEN),
      topBar = {
        EurekaTopBar(
            title = "Ideas",
            navigationIcon = {
              BackButton(
                  onClick = onNavigateBack,
                  modifier = Modifier.testTag(IdeasScreenTestTags.BACK_BUTTON))
            })
      },
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      floatingActionButton = {
        if (uiState.viewMode == IdeasViewMode.LIST) {
          FloatingActionButton(
              onClick = { showCreateIdeaDialog = true },
              modifier = Modifier.offset(y = (-32).dp).testTag("createIdeaButton"),
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = Color.White) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create new idea")
              }
        }
      }) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
          // Project selector button centered below the top bar
          Box(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
              contentAlignment = androidx.compose.ui.Alignment.Center) {
                @OptIn(ExperimentalMaterial3Api::class)
                ExposedDropdownMenuBox(
                    expanded = projectDropdownExpanded,
                    onExpandedChange = { projectDropdownExpanded = it },
                    modifier = Modifier.testTag(IdeasScreenTestTags.PROJECT_SELECTOR)) {
                      Button(
                          onClick = { projectDropdownExpanded = !projectDropdownExpanded },
                          shape = RoundedCornerShape(20.dp),
                          colors =
                              ButtonDefaults.buttonColors(
                                  containerColor = MaterialTheme.colorScheme.surface,
                                  contentColor = MaterialTheme.colorScheme.onSurface)) {
                            Text(
                                text = uiState.selectedProject?.name ?: "Select a project",
                                modifier = Modifier.padding(horizontal = 8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select project",
                                modifier = Modifier.padding(start = 4.dp))
                          }
                      ExposedDropdownMenu(
                          expanded = projectDropdownExpanded,
                          onDismissRequest = { projectDropdownExpanded = false }) {
                            if (uiState.availableProjects.isEmpty()) {
                              DropdownMenuItem(
                                  text = { Text("No projects available") },
                                  onClick = {},
                                  enabled = false)
                            } else {
                              uiState.availableProjects.forEach { project ->
                                DropdownMenuItem(
                                    text = { Text(project.name) },
                                    onClick = {
                                      viewModel.selectProject(project)
                                      projectDropdownExpanded = false
                                    })
                              }
                            }
                          }
                    }
              }

          // Ideas content
          IdeasContent(
              viewMode = uiState.viewMode,
              selectedProject = uiState.selectedProject,
              listState =
                  ch.eureka.eurekapp.ui.ideas.ListState(
                      ideas = uiState.ideas, 
                      onIdeaClick = { idea -> 
                        onIdeaClick?.invoke(idea.ideaId, idea.projectId) 
                          ?: viewModel.selectIdea(idea)
                      }),
              conversationState =
                  ConversationState(
                      selectedIdea = uiState.selectedIdea,
                      messages = uiState.messages,
                      currentUserId = viewModel.getCurrentUserId(),
                      onBackToList = { viewModel.backToList() }),
              lazyListState = listState,
              isLoading = uiState.isLoading)
        }
      }

  if (showCreateIdeaDialog) {
    val createIdeaViewModel: CreateIdeaViewModel = viewModel()
    CreateIdeaBottomSheet(
        onDismiss = {
          createIdeaViewModel.reset()
          showCreateIdeaDialog = false
        },
        onIdeaCreated = { idea ->
          viewModel.onIdeaCreated(idea)
          showCreateIdeaDialog = false
        },
        viewModel = createIdeaViewModel)
  }
}
