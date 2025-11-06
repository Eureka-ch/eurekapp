/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template

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
}
