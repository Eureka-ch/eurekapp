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
  fun textValueWithinConstraintsIsValid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(maxLength = 10, minLength = 2))
    val value = FieldValue.TextValue("hello")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun textValueExceedingMaxLengthIsInvalid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(maxLength = 5))
    val value = FieldValue.TextValue("too long text")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("maxLength") })
  }

  @Test
  fun textValueBelowMinLengthIsInvalid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(minLength = 10))
    val value = FieldValue.TextValue("short")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("minLength") })
  }

  @Test
  fun textValueMatchingPatternIsValid() {
    val fieldDef = FieldDefinition("email", "Email", FieldType.Text(pattern = ".*@.*\\..*"))
    val value = FieldValue.TextValue("test@example.com")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun textValueNotMatchingPatternIsInvalid() {
    val fieldDef = FieldDefinition("email", "Email", FieldType.Text(pattern = ".*@.*\\..*"))
    val value = FieldValue.TextValue("invalid-email")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("pattern") })
  }

  @Test
  fun textValueWithoutConstraintsIsValid() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text())
    val value = FieldValue.TextValue("any text of any length")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun numberValueWithinRangeIsValid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 0.0, max = 100.0))
    val value = FieldValue.NumberValue(50.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun numberValueBelowMinIsInvalid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 10.0))
    val value = FieldValue.NumberValue(5.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("minimum") })
  }

  @Test
  fun numberValueAboveMaxIsInvalid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(max = 100.0))
    val value = FieldValue.NumberValue(150.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("maximum") })
  }

  @Test
  fun numberValueWithoutConstraintsIsValid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number())
    val value = FieldValue.NumberValue(-999.999)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun dateValueIsValid() {
    val fieldDef = FieldDefinition("date", "Date", FieldType.Date())
    val value = FieldValue.DateValue("2024-01-15")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun singleSelectValueInOptionsIsValid() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition(
            "priority", "Priority", FieldType.SingleSelect(options, allowCustom = false))
    val value = FieldValue.SingleSelectValue("low")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun singleSelectValueNotInOptionsIsInvalidWhenCustomNotAllowed() {
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
  fun singleSelectValueNotInOptionsIsValidWhenCustomAllowed() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition("priority", "Priority", FieldType.SingleSelect(options, allowCustom = true))
    val value = FieldValue.SingleSelectValue("custom_value")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun multiSelectValuesInOptionsAreValid() {
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
  fun multiSelectValuesNotInOptionsAreInvalidWhenCustomNotAllowed() {
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
  fun multiSelectValuesNotInOptionsAreValidWhenCustomAllowed() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, allowCustom = true))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "custom_tag"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun multiSelectBelowMinSelectionsIsInvalid() {
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
  fun multiSelectAboveMaxSelectionsIsInvalid() {
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
  fun multiSelectWithinSelectionConstraintsIsValid() {
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
  fun typeMismatchIsInvalid() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number())
    val value = FieldValue.TextValue("not a number")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("Type mismatch") })
  }

  @Test
  fun multipleValidationErrorsAreCollected() {
    val fieldDef =
        FieldDefinition(
            "text", "Text", FieldType.Text(maxLength = 5, minLength = 3, pattern = "^[0-9]+$"))
    val value = FieldValue.TextValue("text that is too long")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.size >= 2)
  }
}
