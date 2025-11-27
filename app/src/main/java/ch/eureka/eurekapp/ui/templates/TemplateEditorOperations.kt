package ch.eureka.eurekapp.ui.templates

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.validation.TemplateValidation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TemplateEditorOperations(private val stateFlow: MutableStateFlow<TemplateEditorState>) {

  fun updateTitle(title: String) {
    stateFlow.update {
      it.updateTitle(title).setTitleError(TemplateValidation.validateTitle(title))
    }
  }

  fun updateDescription(description: String) {
    stateFlow.update { it.updateDescription(description) }
  }

  fun addField(field: FieldDefinition) {
    stateFlow.update { it.addField(field) }
    validateField(field.id)
  }

  fun updateField(fieldId: String, field: FieldDefinition) {
    stateFlow.update { it.updateField(fieldId) { field } }
    validateField(fieldId)
  }

  fun removeField(fieldId: String) {
    stateFlow.update { it.removeField(fieldId) }
  }

  fun duplicateField(fieldId: String): String? {
    stateFlow.update { it.duplicateField(fieldId) }
    return stateFlow.value.fields.find { it.label.endsWith(" (copy)") }?.id
  }

  fun reorderFields(fromIndex: Int, toIndex: Int) {
    stateFlow.update { it.reorderFields(fromIndex, toIndex) }
  }

  fun setEditingFieldId(fieldId: String?) {
    stateFlow.update { it.setEditingFieldId(fieldId) }
  }

  private fun validateField(fieldId: String) {
    val field = stateFlow.value.fields.find { it.id == fieldId } ?: return
    stateFlow.update {
      it.setFieldError(fieldId, TemplateValidation.validateFieldDefinition(field))
    }
  }

  fun validateAll(): Boolean {
    stateFlow.update { it.setTitleError(TemplateValidation.validateTitle(it.title)) }
    if (TemplateValidation.validateFields(stateFlow.value.fields).isFailure) return false
    stateFlow.value.fields.forEach { validateField(it.id) }
    return stateFlow.value.canSave
  }

  fun setSaving(saving: Boolean) {
    stateFlow.update { it.setSaving(saving) }
  }
}
