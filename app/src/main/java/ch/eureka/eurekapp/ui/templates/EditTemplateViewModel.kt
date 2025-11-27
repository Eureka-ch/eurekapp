package ch.eureka.eurekapp.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditTemplateViewModel(
    private val repository: TaskTemplateRepository,
    private val projectId: String,
    private val templateId: String
) : ViewModel() {

  private val _state = MutableStateFlow(TemplateEditorState(projectId = projectId))
  val state: StateFlow<TemplateEditorState> = _state.asStateFlow()

  private val _isLoading = MutableStateFlow(true)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _loadError = MutableStateFlow<String?>(null)
  val loadError: StateFlow<String?> = _loadError.asStateFlow()

  private val ops = TemplateEditorOperations(_state)

  init {
    viewModelScope.launch {
      repository.getTemplateById(projectId, templateId).collect { template ->
        if (template != null) {
          _state.value =
              TemplateEditorState(
                  title = template.title,
                  description = template.description,
                  projectId = template.projectId,
                  fields = template.definedFields.fields)
          _isLoading.value = false
        } else {
          _loadError.value = "Template not found"
          _isLoading.value = false
        }
      }
    }
  }

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

  suspend fun save(): Result<Unit> {
    if (!ops.validateAll()) return Result.failure(Exception("Validation failed"))
    ops.setSaving(true)
    val template =
        TaskTemplate(
            templateID = templateId,
            projectId = projectId,
            title = _state.value.title,
            description = _state.value.description,
            definedFields = TaskTemplateSchema(_state.value.fields),
            createdBy = "")
    return repository.updateTemplate(template).also { ops.setSaving(false) }
  }
}
