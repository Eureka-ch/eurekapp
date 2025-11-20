// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import androidx.lifecycle.ViewModel
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.validation.TemplateValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for creating new templates.
 *
 * @param repository Template repository for persistence
 * @param initialProjectId Optional initial project ID from navigation
 */
class CreateTemplateViewModel(
    private val repository: TaskTemplateRepository,
    initialProjectId: String? = null
) : ViewModel() {

  private val _state = MutableStateFlow(TemplateEditorState(projectId = initialProjectId))
  val state: StateFlow<TemplateEditorState> = _state.asStateFlow()

  /** Updates template title and validates. */
  fun updateTitle(title: String) {
    _state.update { it.updateTitle(title).setTitleError(TemplateValidation.validateTitle(title)) }
  }

  /** Updates template description. */
  fun updateDescription(description: String) {
    _state.update { it.updateDescription(description) }
  }

  /** Adds a new field and validates it. */
  fun addField(field: FieldDefinition) {
    _state.update { it.addField(field) }
    validateField(field.id)
  }

  /** Updates an existing field and validates it. */
  fun updateField(fieldId: String, field: FieldDefinition) {
    _state.update { it.updateField(fieldId) { field } }
    validateField(fieldId)
  }

  /** Removes a field. */
  fun removeField(fieldId: String) {
    _state.update { it.removeField(fieldId) }
  }

  /** Duplicates a field and sets it as editing. */
  fun duplicateField(fieldId: String) {
    _state.update { it.duplicateField(fieldId) }
    _state.value.fields.find { it.label.endsWith(" (copy)") }?.id?.let { setEditingFieldId(it) }
  }

  /** Reorders fields. */
  fun reorderFields(fromIndex: Int, toIndex: Int) {
    _state.update { it.reorderFields(fromIndex, toIndex) }
  }

  /** Sets which field is currently being edited. */
  fun setEditingFieldId(fieldId: String?) {
    _state.update { it.setEditingFieldId(fieldId) }
  }

  private fun validateField(fieldId: String) {
    val field = _state.value.fields.find { it.id == fieldId } ?: return
    val error = TemplateValidation.validateFieldDefinition(field)
    _state.update { it.setFieldError(fieldId, error) }
  }

  /** Validates all fields. Returns true if valid. */
  fun validateAll(): Boolean {
    _state.update { it.setTitleError(TemplateValidation.validateTitle(it.title)) }

    TemplateValidation.validateFields(_state.value.fields)?.let {
      return false
    }

    _state.value.fields.forEach { validateField(it.id) }

    return _state.value.canSave
  }

  /**
   * Saves the template. Repository will automatically set createdBy to current user.
   *
   * @return Result with template ID on success
   */
  suspend fun save(): Result<String> {
    if (!validateAll()) return Result.failure(Exception("Validation failed"))

    _state.update { it.setSaving(true) }

    val template =
        TaskTemplate(
            templateID = IdGenerator.generateTaskTemplateId(),
            projectId = _state.value.projectId ?: "",
            title = _state.value.title,
            description = _state.value.description,
            definedFields = TaskTemplateSchema(_state.value.fields),
            createdBy = "" // Repository will set this to current user
            )

    return repository.createTemplate(template).also { _state.update { it.setSaving(false) } }
  }
}
