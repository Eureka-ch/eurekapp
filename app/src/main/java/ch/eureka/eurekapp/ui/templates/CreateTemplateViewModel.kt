package ch.eureka.eurekapp.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateTemplateViewModel(
    private val repository: TaskTemplateRepository,
    initialProjectId: String? = null,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _state = MutableStateFlow(TemplateEditorState(projectId = initialProjectId))
  val state: StateFlow<TemplateEditorState> = _state.asStateFlow()

  private val ops = TemplateEditorOperations(_state)

  fun updateTitle(title: String) = ops.updateTitle(title)

  fun updateDescription(description: String) = ops.updateDescription(description)

  fun addField(field: FieldDefinition) = ops.addField(field)

  fun updateField(fieldId: String, field: FieldDefinition) = ops.updateField(fieldId, field)

  fun removeField(fieldId: String) = ops.removeField(fieldId)

  fun duplicateField(fieldId: String) =
      ops.duplicateField(fieldId)?.let { ops.setEditingFieldId(it) }

  fun reorderFields(fromIndex: Int, toIndex: Int) = ops.reorderFields(fromIndex, toIndex)

  fun setEditingFieldId(fieldId: String?) = ops.setEditingFieldId(fieldId)

  fun validateAll() = ops.validateAll()

  fun save(onSuccess: (String) -> Unit, onFailure: (String) -> Unit) {
    if (!ops.validateAll()) {
      onFailure("Validation failed")
      return
    }
    ops.setSaving(true)
    viewModelScope.launch(ioDispatcher) {
      val template =
          TaskTemplate(
              templateID = IdGenerator.generateTaskTemplateId(),
              projectId = _state.value.projectId ?: "",
              title = _state.value.title,
              description = _state.value.description,
              definedFields = TaskTemplateSchema(_state.value.fields),
              createdBy = "")
      val result = repository.createTemplate(template)
      withContext(Dispatchers.Main) {
        result.fold(onSuccess = onSuccess, onFailure = { onFailure(it.message ?: "Save failed") })
        ops.setSaving(false)
      }
    }
  }
}
