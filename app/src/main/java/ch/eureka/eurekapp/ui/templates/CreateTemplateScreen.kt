package ch.eureka.eurekapp.ui.templates

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@Composable
fun CreateTemplateScreen(
    onNavigateBack: () -> Unit,
    onTemplateCreated: (String) -> Unit,
    viewModel: CreateTemplateViewModel
) {
  val state by viewModel.state.collectAsState()

  TemplateEditorContent(
      config = TemplateEditorConfig(title = "Create Template"),
      state = state,
      onNavigateBack = onNavigateBack,
      onSave = viewModel::save,
      onSaveSuccess = { result -> @Suppress("UNCHECKED_CAST") onTemplateCreated(result as String) },
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
