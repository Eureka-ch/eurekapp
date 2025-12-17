/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertTrue
import org.junit.Test

class FieldValidatorTest {

  @Test
  fun fieldValidator_textValueWithinConstraintsIsValid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(maxLength = 10, minLength = 2))
    val value = FieldValue.TextValue("hello")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_textValueExceedingMaxLengthIsInvalid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(maxLength = 5))
    val value = FieldValue.TextValue("too long text")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("maxLength") })
  }

  @Test
  fun fieldValidator_textValueBelowMinLengthIsInvalid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(minLength = 10))
    val value = FieldValue.TextValue("short")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("minLength") })
  }

  @Test
  fun fieldValidator_textValueMatchingPatternIsValid() {
    val fieldDef = FieldDefinition("email", "Email", FieldType.Text(pattern = ".*@.*\\..*"))
    val value = FieldValue.TextValue("test@example.com")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_textValueNotMatchingPatternIsInvalid() {
    val fieldDef = FieldDefinition("email", "Email", FieldType.Text(pattern = ".*@.*\\..*"))
    val value = FieldValue.TextValue("invalid-email")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("pattern") })
  }

  @Test
  fun fieldValidator_textValueWithoutConstraintsIsValid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text())
    val value = FieldValue.TextValue("any text of any length")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_numberValueWithinRangeIsValid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 0.0, max = 100.0))
    val value = FieldValue.NumberValue(50.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_numberValueBelowMinIsInvalid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 10.0))
    val value = FieldValue.NumberValue(5.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("minimum") })
  }

  @Test
  fun fieldValidator_numberValueAboveMaxIsInvalid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(max = 100.0))
    val value = FieldValue.NumberValue(150.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("maximum") })
  }

  @Test
  fun fieldValidator_numberValueWithoutConstraintsIsValid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number())
    val value = FieldValue.NumberValue(-999.999)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_dateValueIsValid() {
    val fieldDef = FieldDefinition("date", "Date", FieldType.Date())
    val value = FieldValue.DateValue("2024-01-15")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_singleSelectValueInOptionsIsValid() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition(
            "priority", "Priority", FieldType.SingleSelect(options, allowCustom = false))
    val value = FieldValue.SingleSelectValue("low")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_singleSelectValueNotInOptionsIsInvalidWhenCustomNotAllowed() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition(
            "priority", "Priority", FieldType.SingleSelect(options, allowCustom = false))
    val value = FieldValue.SingleSelectValue("medium")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("not in allowed options") })
  }

  @Test
  fun fieldValidator_singleSelectValueNotInOptionsIsValidWhenCustomAllowed() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition("priority", "Priority", FieldType.SingleSelect(options, allowCustom = true))
    val value = FieldValue.SingleSelectValue("custom_value")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_multiSelectValuesInOptionsAreValid() {
    val options =
        listOf(
            SelectOption("tag1", "Tag 1"),
            SelectOption("tag2", "Tag 2"),
            SelectOption("tag3", "Tag 3"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, allowCustom = false))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "tag2"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_multiSelectValuesNotInOptionsAreInvalidWhenCustomNotAllowed() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, allowCustom = false))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "invalid_tag"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("not in allowed options") })
  }

  @Test
  fun fieldValidator_multiSelectValuesNotInOptionsAreValidWhenCustomAllowed() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, allowCustom = true))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "custom_tag"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_multiSelectBelowMinSelectionsIsInvalid() {
    val options =
        listOf(
            SelectOption("tag1", "Tag 1"),
            SelectOption("tag2", "Tag 2"),
            SelectOption("tag3", "Tag 3"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, minSelections = 2))
    val value = FieldValue.MultiSelectValue(listOf("tag1"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("at least") })
  }

  @Test
  fun fieldValidator_multiSelectAboveMaxSelectionsIsInvalid() {
    val options =
        listOf(
            SelectOption("tag1", "Tag 1"),
            SelectOption("tag2", "Tag 2"),
            SelectOption("tag3", "Tag 3"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, maxSelections = 2))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag3"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("at most") })
  }

  @Test
  fun fieldValidator_multiSelectWithinSelectionConstraintsIsValid() {
    val options =
        listOf(
            SelectOption("tag1", "Tag 1"),
            SelectOption("tag2", "Tag 2"),
            SelectOption("tag3", "Tag 3"))
    val fieldDef =
        FieldDefinition(
            "tags", "Tags", FieldType.MultiSelect(options, minSelections = 1, maxSelections = 3))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "tag2"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_typeMismatchIsInvalid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number())
    val value = FieldValue.TextValue("not a number")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("Type mismatch") })
  }

  @Test
  fun fieldValidator_multipleValidationErrorsAreCollected() {
    val fieldDef =
        FieldDefinition(
            "text", "Text", FieldType.Text(maxLength = 5, minLength = 3, pattern = "^[0-9]+$"))
    val value = FieldValue.TextValue("text that is too long")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.size >= 2)
  }

  @Test
  fun fieldValidator_nullNumberValueIsValidRegardlessOfRequiredStatus() {
    val requiredFieldDef = FieldDefinition("number", "Number", FieldType.Number(), required = true)
    val optionalFieldDef = FieldDefinition("number", "Number", FieldType.Number(), required = false)
    val value = FieldValue.NumberValue(null)

    val requiredResult = FieldValidator.validate(value, requiredFieldDef)
    val optionalResult = FieldValidator.validate(value, optionalFieldDef)

    assertTrue(requiredResult is FieldValidationResult.Valid)
    assertTrue(optionalResult is FieldValidationResult.Valid)
  }

  @Test
  fun fieldValidator_nullNumberValueVsZeroAreDistinctInValidation() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 1.0))

    val nullValue = FieldValue.NumberValue(null)
    val zeroValue = FieldValue.NumberValue(0.0)

    val nullResult = FieldValidator.validate(nullValue, fieldDef)
    val zeroResult = FieldValidator.validate(zeroValue, fieldDef)

    assertTrue(nullResult is FieldValidationResult.Valid)

    assertTrue(zeroResult is FieldValidationResult.Invalid)
  }

  @Test
  fun fieldValidator_nullNumberValueIgnoresMinAndMaxConstraints() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 10.0, max = 100.0))
    val value = FieldValue.NumberValue(null)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }
}
