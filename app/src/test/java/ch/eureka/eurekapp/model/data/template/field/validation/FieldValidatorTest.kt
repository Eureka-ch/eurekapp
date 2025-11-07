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
  fun `text value within constraints is valid`() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(maxLength = 10, minLength = 2))
    val value = FieldValue.TextValue("hello")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `text value exceeding maxLength is invalid`() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(maxLength = 5))
    val value = FieldValue.TextValue("too long text")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("maxLength") })
  }

  @Test
  fun `text value below minLength is invalid`() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text(minLength = 10))
    val value = FieldValue.TextValue("short")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("minLength") })
  }

  @Test
  fun `text value matching pattern is valid`() {
    val fieldDef = FieldDefinition("email", "Email", FieldType.Text(pattern = ".*@.*\\..*"))
    val value = FieldValue.TextValue("test@example.com")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `text value not matching pattern is invalid`() {
    val fieldDef = FieldDefinition("email", "Email", FieldType.Text(pattern = ".*@.*\\..*"))
    val value = FieldValue.TextValue("invalid-email")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("pattern") })
  }

  @Test
  fun `text value without constraints is valid`() {
    val fieldDef = FieldDefinition("text", "Text", FieldType.Text())
    val value = FieldValue.TextValue("any text of any length")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `number value within range is valid`() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 0.0, max = 100.0))
    val value = FieldValue.NumberValue(50.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `number value below min is invalid`() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(min = 10.0))
    val value = FieldValue.NumberValue(5.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("minimum") })
  }

  @Test
  fun `number value above max is invalid`() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number(max = 100.0))
    val value = FieldValue.NumberValue(150.0)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("maximum") })
  }

  @Test
  fun `number value without constraints is valid`() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number())
    val value = FieldValue.NumberValue(-999.999)

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `date value is valid`() {
    val fieldDef = FieldDefinition("date", "Date", FieldType.Date())
    val value = FieldValue.DateValue("2024-01-15")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `single select value in options is valid`() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition(
            "priority", "Priority", FieldType.SingleSelect(options, allowCustom = false))
    val value = FieldValue.SingleSelectValue("low")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `single select value not in options is invalid when custom not allowed`() {
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
  fun `single select value not in options is valid when custom allowed`() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val fieldDef =
        FieldDefinition("priority", "Priority", FieldType.SingleSelect(options, allowCustom = true))
    val value = FieldValue.SingleSelectValue("custom_value")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `multi select values in options are valid`() {
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
  fun `multi select values not in options are invalid when custom not allowed`() {
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
  fun `multi select values not in options are valid when custom allowed`() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    val fieldDef =
        FieldDefinition("tags", "Tags", FieldType.MultiSelect(options, allowCustom = true))
    val value = FieldValue.MultiSelectValue(listOf("tag1", "custom_tag"))

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Valid)
  }

  @Test
  fun `multi select below minSelections is invalid`() {
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
  fun `multi select above maxSelections is invalid`() {
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
  fun `multi select within selection constraints is valid`() {
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
  fun `type mismatch is invalid`() {
    val fieldDef = FieldDefinition("number", "Number", FieldType.Number())
    val value = FieldValue.TextValue("not a number")

    val result = FieldValidator.validate(value, fieldDef)
    assertTrue(result is FieldValidationResult.Invalid)
    val errors = (result as FieldValidationResult.Invalid).errors
    assertTrue(errors.any { it.contains("Type mismatch") })
  }

  @Test
  fun `multiple validation errors are collected`() {
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
