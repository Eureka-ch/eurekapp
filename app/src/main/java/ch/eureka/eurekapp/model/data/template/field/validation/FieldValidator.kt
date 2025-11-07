/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import com.google.common.collect.ImmutableList

object FieldValidator {

  fun validate(value: FieldValue, fieldDef: FieldDefinition): FieldValidationResult {
    if (value.typeKey != fieldDef.type.typeKey) {
      return FieldValidationResult.Invalid(
          listOf("Type mismatch: expected ${fieldDef.type.typeKey}, got ${value.typeKey}"))
    }

    val errors = mutableListOf<String>()

    when (value) {
      is FieldValue.TextValue -> validateText(value, fieldDef.type as FieldType.Text, errors)
      is FieldValue.NumberValue -> validateNumber(value, fieldDef.type as FieldType.Number, errors)
      is FieldValue.DateValue -> validateDate(value, fieldDef.type as FieldType.Date, errors)
      is FieldValue.SingleSelectValue ->
          validateSingleSelect(value, fieldDef.type as FieldType.SingleSelect, errors)
      is FieldValue.MultiSelectValue ->
          validateMultiSelect(value, fieldDef.type as FieldType.MultiSelect, errors)
    }

    return if (errors.isEmpty()) FieldValidationResult.Valid
    else FieldValidationResult.Invalid(errors)
  }

  private fun validateText(
      value: FieldValue.TextValue,
      type: FieldType.Text,
      errors: MutableList<String>
  ) {
    type.maxLength
        ?.takeIf { value.value.length > it }
        ?.let { errors.add("Text exceeds maxLength of $it characters") }

    type.minLength
        ?.takeIf { value.value.length < it }
        ?.let { errors.add("Text is shorter than minLength of $it characters") }

    type.pattern?.let { pattern ->
      try {
        if (!Regex(pattern).matches(value.value)) {
          errors.add("Text does not match required pattern")
        }
      } catch (e: Exception) {
        errors.add("Invalid pattern in field definition")
      }
    }
  }

  private fun validateNumber(
      value: FieldValue.NumberValue,
      type: FieldType.Number,
      errors: MutableList<String>
  ) {
    type.min?.takeIf { value.value < it }?.let { errors.add("Number is less than minimum of $it") }

    type.max
        ?.takeIf { value.value > it }
        ?.let { errors.add("Number is greater than maximum of $it") }
  }

  private fun validateDate(
      value: FieldValue.DateValue,
      type: FieldType.Date,
      errors: ImmutableList<String>
  ) {
    type.minDate
        ?.takeIf { value.value < it }
        ?.let { errors.add("Date is before minimum date of $it") }

    type.maxDate
        ?.takeIf { value.value > it }
        ?.let { errors.add("Date is after maximum date of $it") }
  }

  private fun validateSingleSelect(
      value: FieldValue.SingleSelectValue,
      type: FieldType.SingleSelect,
      errors: MutableList<String>
  ) {
    val allowedValues = type.options.map { it.value }

    if (!type.allowCustom && value.value !in allowedValues) {
      errors.add("Value '${value.value}' is not in allowed options: $allowedValues")
    }
  }

  private fun validateMultiSelect(
      value: FieldValue.MultiSelectValue,
      type: FieldType.MultiSelect,
      errors: MutableList<String>
  ) {
    val allowedValues = type.options.map { it.value }

    if (!type.allowCustom) {
      value.values
          .filter { it !in allowedValues }
          .takeIf { it.isNotEmpty() }
          ?.let { invalidValues ->
            errors.add("Values $invalidValues are not in allowed options: $allowedValues")
          }
    }

    type.minSelections
        ?.takeIf { value.values.size < it }
        ?.let { errors.add("Must select at least $it options") }

    type.maxSelections
        ?.takeIf { value.values.size > it }
        ?.let { errors.add("Must select at most $it options") }
  }
}

sealed interface FieldValidationResult {
  data object Valid : FieldValidationResult

  data class Invalid(val errors: List<String>) : FieldValidationResult
}
