// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.ui.templates

import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition

/**
 * UI state for template editor.
 *
 * @property title Template title
 * @property description Template description
 * @property projectId Project ID (null if selectable)
 * @property fields List of field definitions
 * @property editingFieldId ID of field currently being edited
 * @property titleError Title validation error
 * @property fieldErrors Map of field ID to validation error
 * @property isSaving Whether save is in progress
 * @property saveError Error from last save attempt
 */
data class TemplateEditorState(
    val title: String = "",
    val description: String = "",
    val projectId: String? = null,
    val fields: List<FieldDefinition> = emptyList(),
    val editingFieldId: String? = null,
    val titleError: String? = null,
    val fieldErrors: Map<String, String> = emptyMap(),
    val isSaving: Boolean = false,
    val saveError: String? = null
) {
  val errorCount: Int
    get() = (if (titleError != null) 1 else 0) + fieldErrors.size

  val canSave: Boolean
    get() = title.isNotBlank() && fields.isNotEmpty() && errorCount == 0 && !isSaving
}

// Extension functions for concise immutable updates
fun TemplateEditorState.updateTitle(title: String) = copy(title = title)

fun TemplateEditorState.updateDescription(desc: String) = copy(description = desc)

fun TemplateEditorState.setEditingFieldId(id: String?) = copy(editingFieldId = id)

fun TemplateEditorState.setTitleError(error: String?) = copy(titleError = error)

fun TemplateEditorState.setTitleError(result: Result<Unit>) =
    copy(titleError = result.exceptionOrNull()?.message)

fun TemplateEditorState.setFieldError(fieldId: String, error: String?) =
    copy(
        fieldErrors =
            if (error != null) fieldErrors + (fieldId to error) else fieldErrors - fieldId)

fun TemplateEditorState.setFieldError(fieldId: String, result: Result<Unit>) =
    setFieldError(fieldId, result.exceptionOrNull()?.message)

fun TemplateEditorState.setSaving(saving: Boolean) = copy(isSaving = saving)

fun TemplateEditorState.addField(field: FieldDefinition) = copy(fields = fields + field)

fun TemplateEditorState.updateField(
    fieldId: String,
    updater: (FieldDefinition) -> FieldDefinition
) = copy(fields = fields.map { if (it.id == fieldId) updater(it) else it })

fun TemplateEditorState.removeField(fieldId: String) =
    copy(fields = fields.filterNot { it.id == fieldId }, fieldErrors = fieldErrors - fieldId)

fun TemplateEditorState.reorderFields(fromIndex: Int, toIndex: Int) =
    copy(fields = TaskTemplateSchema(fields).reorderField(fromIndex, toIndex).fields)

fun TemplateEditorState.duplicateField(fieldId: String) =
    copy(fields = TaskTemplateSchema(fields).duplicateField(fieldId).fields)
