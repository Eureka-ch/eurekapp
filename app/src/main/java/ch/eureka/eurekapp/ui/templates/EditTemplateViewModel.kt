package ch.eureka.eurekapp.ui.templates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.validation.TemplateValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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

  fun updateTitle(title: String) {
    _state.update { it.updateTitle(title).setTitleError(TemplateValidation.validateTitle(title)) }
  }

  fun updateDescription(description: String) {
    _state.update { it.updateDescription(description) }
  }

  fun addField(field: FieldDefinition) {
    _state.update { it.addField(field) }
    validateField(field.id)
  }

  fun updateField(fieldId: String, field: FieldDefinition) {
    _state.update { it.updateField(fieldId) { field } }
    validateField(fieldId)
  }

  fun removeField(fieldId: String) {
    _state.update { it.removeField(fieldId) }
  }

  fun duplicateField(fieldId: String) {
    _state.update { it.duplicateField(fieldId) }
    _state.value.fields.find { it.label.endsWith(" (copy)") }?.id?.let { setEditingFieldId(it) }
  }

  fun reorderFields(fromIndex: Int, toIndex: Int) {
    _state.update { it.reorderFields(fromIndex, toIndex) }
  }

  fun setEditingFieldId(fieldId: String?) {
    _state.update { it.setEditingFieldId(fieldId) }
  }

  private fun validateField(fieldId: String) {
    val field = _state.value.fields.find { it.id == fieldId } ?: return
    val error = TemplateValidation.validateFieldDefinition(field)
    _state.update { it.setFieldError(fieldId, error) }
  }

  fun validateAll(): Boolean {
    _state.update { it.setTitleError(TemplateValidation.validateTitle(it.title)) }
    if (TemplateValidation.validateFields(_state.value.fields).isFailure) return false
    _state.value.fields.forEach { validateField(it.id) }
    return _state.value.canSave
  }

  suspend fun save(): Result<Unit> {
    if (!validateAll()) return Result.failure(Exception("Validation failed"))

    _state.update { it.setSaving(true) }

    val template =
        TaskTemplate(
            templateID = templateId,
            projectId = projectId,
            title = _state.value.title,
            description = _state.value.description,
            definedFields = TaskTemplateSchema(_state.value.fields),
            createdBy = "")

    return repository.updateTemplate(template).also { _state.update { it.setSaving(false) } }
  }
}
