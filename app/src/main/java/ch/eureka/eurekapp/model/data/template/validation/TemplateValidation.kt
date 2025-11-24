// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.model.data.template.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition

/**
 * Validation logic for template definitions.
 *
 * This validates field configurations (not runtime values). Use FieldValidator for validating field
 * values at runtime.
 */
object TemplateValidation {

  /**
   * Validates template title is not blank.
   *
   * @param title The title to validate
   * @return Result.success if valid, Result.failure with error message if invalid
   */
  fun validateTitle(title: String): Result<Unit> =
      if (title.isBlank()) Result.failure(IllegalArgumentException("Title is required"))
      else Result.success(Unit)

  /**
   * Validates template has at least one field.
   *
   * @param fields The fields to validate
   * @return Result.success if valid, Result.failure with error message if invalid
   */
  fun validateFields(fields: List<FieldDefinition>): Result<Unit> =
      if (fields.isEmpty())
          Result.failure(IllegalArgumentException("At least one field is required"))
      else Result.success(Unit)

  /**
   * Validates field label is not blank.
   *
   * @param label The label to validate
   * @return Result.success if valid, Result.failure with error message if invalid
   */
  fun validateFieldLabel(label: String): Result<Unit> =
      if (label.isBlank()) Result.failure(IllegalArgumentException("Field label is required"))
      else Result.success(Unit)

  /**
   * Validates a field definition's configuration. Delegates to
   * FieldDefinition.validateConfiguration().
   *
   * @param field The field definition to validate
   * @return Result.success if valid, Result.failure with error message if invalid
   */
  fun validateFieldDefinition(field: FieldDefinition): Result<Unit> {
    return field.validateConfiguration()
  }
}
