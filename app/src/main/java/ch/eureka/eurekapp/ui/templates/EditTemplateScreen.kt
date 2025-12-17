package ch.eureka.eurekapp.ui.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R

@Composable
fun EditTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateSaved: () -> Unit,
    viewModel: EditTemplateViewModel
) {
  val state by viewModel.state.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val loadError by viewModel.loadError.collectAsState()

  TemplateEditorContent(
      config =
          TemplateEditorConfig(
              title = stringResource(R.string.template_editor_title_edit),
              isLoading = isLoading,
              loadError = loadError),
      state = state,
      onNavigateBack = onNavigateBack,
      onSave = { onSuccess, onFailure -> viewModel.save(onSuccess, onFailure) },
      onSaveSuccess = onTemplateSaved,
      callbacks =
          TemplateEditorCallbacks(
              onTitleChange = viewModel::updateTitle,
              onDescriptionChange = viewModel::updateDescription,
              onFieldEdit = viewModel::setEditingFieldId,
              onFieldSave = viewModel::updateField,
              onFieldDelete = viewModel::removeField,
              onFieldDuplicate = viewModel::duplicateField,
              onFieldsReorder = viewModel::reorderFields,
              onFieldAdd = viewModel::addField))
}
