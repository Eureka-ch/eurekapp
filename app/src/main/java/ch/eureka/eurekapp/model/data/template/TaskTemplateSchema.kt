// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.model.data.template

import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import kotlinx.serialization.Serializable

@Serializable
data class TaskTemplateSchema(val fields: List<FieldDefinition> = emptyList()) {

  init {
    val fieldIds = fields.map { it.id }
    require(fieldIds.distinct().size == fieldIds.size) { "Field IDs must be unique" }
  }

  fun getField(id: String): FieldDefinition? = fields.find { it.id == id }

  fun hasField(id: String): Boolean = fields.any { it.id == id }

  fun getRequiredFields(): List<FieldDefinition> = fields.filter { it.required }

  fun addField(field: FieldDefinition): TaskTemplateSchema {
    require(!hasField(field.id)) { "Field with id '${field.id}' already exists" }
    return copy(fields = fields + field)
  }

  fun removeField(id: String): TaskTemplateSchema = copy(fields = fields.filter { it.id != id })

  fun updateField(id: String, updatedField: FieldDefinition): TaskTemplateSchema {
    require(hasField(id)) { "Field with id '$id' does not exist" }
    require(id == updatedField.id) { "Cannot change field ID during update" }
    return copy(fields = fields.map { if (it.id == id) updatedField else it })
  }

  /**
   * Reorders a field from one position to another.
   *
   * @param fromIndex The current index of the field to move
   * @param toIndex The target index to move the field to
   * @return A new TaskTemplateSchema with the field reordered
   */
  fun reorderField(fromIndex: Int, toIndex: Int): TaskTemplateSchema {
    if (fromIndex !in fields.indices || toIndex !in fields.indices) return this
    val newFields = fields.toMutableList()
    newFields.add(toIndex, newFields.removeAt(fromIndex))
    return copy(fields = newFields)
  }

  /**
   * Duplicates a field, appending "(copy)" to its label and generating a new ID.
   *
   * @param fieldId The ID of the field to duplicate
   * @return A new TaskTemplateSchema with the duplicated field inserted after the original
   */
  fun duplicateField(fieldId: String): TaskTemplateSchema {
    val original = getField(fieldId) ?: return this
    val newLabel = "${original.label} (copy)"
    val duplicate = original.copy(id = IdGenerator.generateFieldId(newLabel), label = newLabel)
    val index = fields.indexOfFirst { it.id == fieldId }
    return copy(fields = fields.toMutableList().apply { add(index + 1, duplicate) })
  }
}
