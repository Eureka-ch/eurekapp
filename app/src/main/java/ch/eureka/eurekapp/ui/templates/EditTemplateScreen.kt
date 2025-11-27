package ch.eureka.eurekapp.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.templates.components.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSaved: () -> Unit,
    viewModel: EditTemplateViewModel
) {
  val state by viewModel.state.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val loadError by viewModel.loadError.collectAsState()
  val pagerState = rememberPagerState { 2 }
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  var showAddFieldSheet by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = "Edit Template",
            navigationIcon = { BackButton(onClick = onNavigateBack) },
            actions = {
              if (!isLoading && loadError == null) {
                TextButton(
                    onClick = {
                      scope.launch {
                        viewModel
                            .save()
                            .fold(
                                onSuccess = { onTemplateSaved() },
                                onFailure = {
                                  snackbarHostState.showSnackbar(it.message ?: "Save failed")
                                })
                      }
                    },
                    enabled = !state.isSaving) {
                      if (state.isSaving)
                          CircularProgressIndicator(
                              Modifier.size(16.dp), color = MaterialTheme.colorScheme.onPrimary)
                      else
                          Text(
                              "Save",
                              color =
                                  if (state.canSave) MaterialTheme.colorScheme.onPrimary
                                  else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
                    }
              }
            })
      },
      snackbarHost = { SnackbarHost(snackbarHostState) },
      floatingActionButton = {
        if (!isLoading && loadError == null && pagerState.currentPage == 0) {
          ExtendedFloatingActionButton(
              onClick = { showAddFieldSheet = true },
              icon = { Icon(Icons.Default.Add, null) },
              text = { Text("Add Field") },
              modifier = Modifier.testTag("add_field_button"))
        }
      }) { padding ->
        when {
          isLoading ->
              Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
          loadError != null ->
              Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(loadError ?: "Error loading template")
              }
          else -> {
            Column(Modifier.padding(padding)) {
              TabRow(pagerState.currentPage) {
                Tab(
                    pagerState.currentPage == 0,
                    onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                    text = {
                      Row(
                          horizontalArrangement = Arrangement.spacedBy(4.dp),
                          modifier = Modifier.semantics(mergeDescendants = false) {}) {
                            Text("Configure")
                            if (state.errorCount > 0) Badge { Text("${state.errorCount}") }
                          }
                    })
                Tab(
                    pagerState.currentPage == 1,
                    onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text("Preview") })
              }

              HorizontalPager(pagerState, Modifier.fillMaxSize()) { page ->
                when (page) {
                  0 ->
                      TemplateFieldList(
                          modifier = Modifier.fillMaxSize(),
                          fields = state.fields,
                          editingFieldId = state.editingFieldId,
                          fieldErrors = state.fieldErrors,
                          callbacks =
                              TemplateFieldListCallbacks(
                                  onFieldEdit = viewModel::setEditingFieldId,
                                  onFieldSave = viewModel::updateField,
                                  onFieldCancel = { viewModel.setEditingFieldId(null) },
                                  onFieldDelete = viewModel::removeField,
                                  onFieldDuplicate = viewModel::duplicateField,
                                  onFieldsReorder = viewModel::reorderFields),
                          headerConfig =
                              TemplateHeaderConfig(
                                  title = state.title,
                                  description = state.description,
                                  titleError = state.titleError,
                                  callbacks =
                                      TemplateBasicInfoCallbacks(
                                          onTitleChange = viewModel::updateTitle,
                                          onDescriptionChange = viewModel::updateDescription)))
                  1 ->
                      TemplatePreview(
                          state.fields,
                          state.fieldErrors,
                          { fieldId ->
                            scope.launch {
                              pagerState.animateScrollToPage(0)
                              viewModel.setEditingFieldId(fieldId)
                            }
                          })
                }
              }
            }
          }
        }
      }

  if (showAddFieldSheet) {
    AddFieldBottomSheet(
        onDismiss = { showAddFieldSheet = false },
        onFieldCreated = { field ->
          viewModel.addField(field)
          viewModel.setEditingFieldId(field.id)
        })
  }
}
