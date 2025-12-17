package ch.eureka.eurekapp.ui.templates

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import ch.eureka.eurekapp.R
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.ui.components.BackButton
import ch.eureka.eurekapp.ui.components.EurekaTopBar
import ch.eureka.eurekapp.ui.templates.components.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

data class TemplateEditorConfig(
    val title: String,
    val isLoading: Boolean = false,
    val loadError: String? = null
)

data class TemplateEditorCallbacks(
    val onTitleChange: (String) -> Unit,
    val onDescriptionChange: (String) -> Unit,
    val onFieldEdit: (String?) -> Unit,
    val onFieldSave: (String, FieldDefinition) -> Unit,
    val onFieldDelete: (String) -> Unit,
    val onFieldDuplicate: (String) -> Unit,
    val onFieldsReorder: (Int, Int) -> Unit,
    val onFieldAdd: (FieldDefinition) -> Unit
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TemplateEditorContent(
    config: TemplateEditorConfig,
    state: TemplateEditorState,
    onNavigateBack: () -> Unit,
    onSave: (onSuccess: () -> Unit, onFailure: (String) -> Unit) -> Unit,
    onSaveSuccess: () -> Unit,
    callbacks: TemplateEditorCallbacks
) {
  val pagerState = rememberPagerState { 2 }
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }
  var showAddFieldSheet by remember { mutableStateOf(false) }

  Scaffold(
      topBar = {
        EurekaTopBar(
            title = config.title,
            navigationIcon = { BackButton(onClick = onNavigateBack) },
            actions = {
              if (!config.isLoading && config.loadError == null) {
                SaveButton(
                    isSaving = state.isSaving,
                    canSave = state.canSave,
                    onSave = {
                      onSave(
                          { onSaveSuccess() },
                          { message -> scope.launch { snackbarHostState.showSnackbar(message) } })
                    })
              }
            })
      },
      snackbarHost = { SnackbarHost(snackbarHostState, Modifier.testTag("snackbar_host")) },
      floatingActionButton = {
        AddFieldFab(
            isVisible =
                !config.isLoading && config.loadError == null && pagerState.currentPage == 0,
            onClick = { showAddFieldSheet = true })
      }) { padding ->
        ScaffoldContent(padding, config, pagerState, scope, state, callbacks)
      }

  if (showAddFieldSheet) {
    AddFieldBottomSheet(
        onDismiss = { showAddFieldSheet = false },
        onFieldCreated = { field ->
          callbacks.onFieldAdd(field)
          callbacks.onFieldEdit(field.id)
        })
  }
}

@Composable
private fun SaveButton(isSaving: Boolean, canSave: Boolean, onSave: () -> Unit) {
  TextButton(onClick = onSave, enabled = !isSaving) {
    if (isSaving) {
      CircularProgressIndicator(
          Modifier.size(16.dp).testTag("progress_indicator"),
          color = MaterialTheme.colorScheme.onPrimary)
    } else {
      Text(
          stringResource(R.string.template_editor_save),
          color =
              if (canSave) MaterialTheme.colorScheme.onPrimary
              else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f))
    }
  }
}

@Composable
private fun AddFieldFab(isVisible: Boolean, onClick: () -> Unit) {
  if (isVisible) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        icon = {
          Icon(
              Icons.Default.Add,
              contentDescription = stringResource(R.string.template_add_field_cd))
        },
        text = { Text(stringResource(R.string.template_add_field_button)) },
        modifier = Modifier.testTag("add_field_button"))
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ScaffoldContent(
    padding: PaddingValues,
    config: TemplateEditorConfig,
    pagerState: PagerState,
    scope: CoroutineScope,
    state: TemplateEditorState,
    callbacks: TemplateEditorCallbacks
) {
  when {
    config.isLoading ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
    config.loadError != null ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
          Text(config.loadError)
        }
    else ->
        EditorContent(
            padding = padding,
            pagerState = pagerState,
            scope = scope,
            state = state,
            callbacks = callbacks)
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EditorContent(
    padding: PaddingValues,
    pagerState: PagerState,
    scope: CoroutineScope,
    state: TemplateEditorState,
    callbacks: TemplateEditorCallbacks
) {
  Column(Modifier.padding(padding)) {
    PrimaryTabRow(pagerState.currentPage) {
      Tab(
          pagerState.currentPage == 0,
          onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
          text = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.semantics(mergeDescendants = false) {}) {
                  Text(stringResource(R.string.template_tab_configure))
                  if (state.errorCount > 0)
                      Badge(modifier = Modifier.testTag("badge")) { Text("${state.errorCount}") }
                }
          })
      Tab(
          pagerState.currentPage == 1,
          onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
          text = { Text(stringResource(R.string.template_tab_preview)) })
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
                        onFieldEdit = callbacks.onFieldEdit,
                        onFieldSave = callbacks.onFieldSave,
                        onFieldCancel = { callbacks.onFieldEdit(null) },
                        onFieldDelete = callbacks.onFieldDelete,
                        onFieldDuplicate = callbacks.onFieldDuplicate,
                        onFieldsReorder = callbacks.onFieldsReorder),
                headerConfig =
                    TemplateHeaderConfig(
                        title = state.title,
                        description = state.description,
                        titleError = state.titleError,
                        callbacks =
                            TemplateBasicInfoCallbacks(
                                onTitleChange = callbacks.onTitleChange,
                                onDescriptionChange = callbacks.onDescriptionChange)))
        1 ->
            TemplatePreview(
                state.fields,
                state.fieldErrors,
                { fieldId ->
                  scope.launch {
                    pagerState.animateScrollToPage(0)
                    callbacks.onFieldEdit(fieldId)
                  }
                })
      }
    }
  }
}
