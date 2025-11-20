// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.model.data.template.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidationResult
import ch.eureka.eurekapp.model.data.template.field.validation.FieldValidator

/**
 * Validation logic for template definitions.
 *
 * This validates field configurations (not runtime values). Use FieldValidator for validating field
 * values at runtime.
 */
object TemplateValidation {

  /** Validates template title is not blank. */
  fun validateTitle(title: String): String? = if (title.isBlank()) "Title is required" else null

  /** Validates template has at least one field. */
  fun validateFields(fields: List<FieldDefinition>): String? =
      if (fields.isEmpty()) "At least one field is required" else null

  /** Validates field label is not blank. */
  fun validateFieldLabel(label: String): String? =
      if (label.isBlank()) "Field label is required" else null

  /**
   * Validates a field definition's configuration. Checks constraints are valid and default value is
   * valid if present.
   *
   * @param field The field definition to validate
   * @return Error message if invalid, null if valid
   */
  fun validateFieldDefinition(field: FieldDefinition): String? {
    validateFieldLabel(field.label)?.let {
      return it
    }

    when (val type = field.type) {
      is FieldType.Text -> {
        if (type.minLength != null && type.maxLength != null && type.minLength > type.maxLength) {
          return "Min length cannot exceed max length"
        }
      }
      is FieldType.Number -> {
        if (type.min != null && type.max != null && type.min > type.max) {
          return "Min value cannot exceed max value"
        }
        if (type.decimals != null && type.decimals < 0) {
          return "Decimals must be non-negative"
        }
      }
      is FieldType.MultiSelect -> {
        if (type.options.isEmpty()) return "At least one option required"
        if (type.minSelections != null &&
            type.maxSelections != null &&
            type.minSelections > type.maxSelections) {
          return "Min selections cannot exceed max selections"
        }
      }
      is FieldType.SingleSelect -> {
        if (type.options.isEmpty()) return "At least one option required"
      }
      else -> {}
    }

    field.defaultValue?.let { value ->
      val result = FieldValidator.validate(value, field)
      if (result is FieldValidationResult.Invalid) {
        return "Invalid default value: ${result.errors.firstOrNull()}"
      }
    }

    return null
  }
}
