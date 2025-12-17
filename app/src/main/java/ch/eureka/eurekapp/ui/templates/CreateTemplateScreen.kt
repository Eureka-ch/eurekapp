package ch.eureka.eurekapp.ui.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import ch.eureka.eurekapp.R

@Composable
fun CreateTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateCreated: (String) -> Unit,
    viewModel: CreateTemplateViewModel
) {
  val state by viewModel.state.collectAsState()

  TemplateEditorContent(
      config = TemplateEditorConfig(title = stringResource(R.string.template_editor_title_create)),
      state = state,
      onNavigateBack = onNavigateBack,
      onSave = { onSuccess, onFailure ->
        viewModel.save(
            onSuccess = { templateId ->
              onSuccess()
              onTemplateCreated(templateId)
            },
            onFailure = onFailure)
      },
      onSaveSuccess = {},
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
