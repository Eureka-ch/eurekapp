/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidationResult
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidator
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
    require(label.isNotBlank()) { "label must not be blank" }
    if (defaultValue != null) {
      require(defaultValue.typeKey == type.typeKey) { "defaultValue type must match field type" }
    }
  }

  /**
   * Validates the field definition configuration without throwing exceptions.
   *
   * @return Result.success if valid, Result.failure with error message if invalid
   */
  fun validateConfiguration(): Result<Unit> {
    if (label.isBlank()) {
      return Result.failure(IllegalArgumentException("label must not be blank"))
    }

    type.validateConfiguration().onFailure {
      return Result.failure(it)
    }

    defaultValue?.let { value ->
      val result = FieldValidator.validate(value, this)
      if (result is FieldValidationResult.Invalid) {
        return Result.failure(
            IllegalArgumentException("Invalid default value: ${result.errors.firstOrNull()}"))
      }
    }

    return Result.success(Unit)
  }
}
