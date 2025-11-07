/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidationResult
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidator
import kotlinx.serialization.Serializable

@Serializable
data class TaskCustomData(val data: Map<String, FieldValue> = emptyMap()) {

  fun getValue(fieldId: String): FieldValue? = data[fieldId]

  fun hasValue(fieldId: String): Boolean = fieldId in data

  fun setValue(fieldId: String, value: FieldValue): TaskCustomData =
      copy(data = data + (fieldId to value))

  fun removeValue(fieldId: String): TaskCustomData = copy(data = data - fieldId)

  fun validate(schema: TaskTemplateSchema): ValidationResult {
    val errors = mutableListOf<String>()

    for (requiredField in schema.getRequiredFields()) {
      if (!hasValue(requiredField.id)) {
        errors.add("Required field '${requiredField.id}' is missing")
      }
    }

    for ((fieldId, value) in data) {
      val fieldDef = schema.getField(fieldId)
      if (fieldDef == null) {
        errors.add("Field '$fieldId' is not defined in schema")
        continue
      }

      if (value.typeKey != fieldDef.type.typeKey) {
        errors.add(
            "Field '$fieldId' has incorrect type: expected ${fieldDef.type.typeKey}, got ${value.typeKey}")
        continue
      }

      val validationResult = FieldValidator.validate(value, fieldDef)
      if (validationResult is FieldValidationResult.Invalid) {
        errors.addAll(validationResult.errors.map { "Field '$fieldId': $it" })
      }
    }

    return if (errors.isEmpty()) ValidationResult.Success else ValidationResult.Failure(errors)
  }
}

sealed interface ValidationResult {
  data object Success : ValidationResult

  data class Failure(val errors: List<String>) : ValidationResult
}
