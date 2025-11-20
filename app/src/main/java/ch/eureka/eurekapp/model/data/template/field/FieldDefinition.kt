/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import kotlinx.serialization.Serializable

@Serializable
data class FieldDefinition(
    val id: String,
    val label: String,
    val type: FieldType,
    val required: Boolean = false,
    val description: String? = null,
    val defaultValue: FieldValue? = null
) {
  init {
    require(id.isNotBlank()) { "id must not be blank" }
    if (defaultValue != null) {
      require(defaultValue.typeKey == type.typeKey) { "defaultValue type must match field type" }
    }
  }
}
