// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.model.data.template.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TemplateValidationTest {

  @Test
  fun validateTitle_returnsSuccessForValidTitle() {
    assertTrue(TemplateValidation.validateTitle("Valid Title").isSuccess)
  }

  @Test
  fun validateTitle_returnsErrorForBlankTitle() {
    val result = TemplateValidation.validateTitle("")
    assertTrue(result.isFailure)
    assertEquals("Title is required", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateTitle_returnsErrorForWhitespaceOnlyTitle() {
    val result = TemplateValidation.validateTitle("   ")
    assertTrue(result.isFailure)
    assertEquals("Title is required", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFields_returnsSuccessForNonEmptyList() {
    val fields = listOf(FieldDefinition(id = "field1", label = "Label", type = FieldType.Text()))
    assertTrue(TemplateValidation.validateFields(fields).isSuccess)
  }

  @Test
  fun validateFields_returnsErrorForEmptyList() {
    val result = TemplateValidation.validateFields(emptyList())
    assertTrue(result.isFailure)
    assertEquals("At least one field is required", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldLabel_returnsSuccessForValidLabel() {
    assertTrue(TemplateValidation.validateFieldLabel("Valid Label").isSuccess)
  }

  @Test
  fun validateFieldLabel_returnsErrorForBlankLabel() {
    val result = TemplateValidation.validateFieldLabel("")
    assertTrue(result.isFailure)
    assertEquals("Field label is required", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldLabel_returnsErrorForWhitespaceOnlyLabel() {
    val result = TemplateValidation.validateFieldLabel("   ")
    assertTrue(result.isFailure)
    assertEquals("Field label is required", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForValidTextField() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 5, maxLength = 10))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForInvalidTextFieldMinMaxLength() {
    try {
      FieldDefinition(
          id = "text1", label = "Text Field", type = FieldType.Text(minLength = 10, maxLength = 5))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("maxLength must be >= minLength", e.message)
    }
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForValidNumberField() {
    val field =
        FieldDefinition(
            id = "number1",
            label = "Number Field",
            type = FieldType.Number(min = 0.0, max = 100.0, decimals = 2))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForInvalidNumberFieldMinMax() {
    try {
      FieldDefinition(
          id = "number1", label = "Number Field", type = FieldType.Number(min = 100.0, max = 0.0))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("max must be >= min", e.message)
    }
  }

  @Test
  fun validateFieldDefinition_returnsErrorForNegativeDecimals() {
    try {
      FieldDefinition(
          id = "number1", label = "Number Field", type = FieldType.Number(decimals = -1))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("decimals must be non-negative", e.message)
    }
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForValidDateField() {
    val field = FieldDefinition(id = "date1", label = "Date Field", type = FieldType.Date())
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForValidSingleSelectField() {
    val field =
        FieldDefinition(
            id = "select1",
            label = "Select Field",
            type =
                FieldType.SingleSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForValidMultiSelectField() {
    val field =
        FieldDefinition(
            id = "multiselect1",
            label = "Multi Select Field",
            type =
                FieldType.MultiSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2")),
                    minSelections = 1,
                    maxSelections = 2))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForInvalidMultiSelectMinMax() {
    try {
      FieldDefinition(
          id = "multiselect1",
          label = "Multi Select Field",
          type =
              FieldType.MultiSelect(
                  options =
                      listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2")),
                  minSelections = 3,
                  maxSelections = 1))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("maxSelections must be >= minSelections", e.message)
    }
  }

  @Test
  fun validateFieldDefinition_returnsErrorForBlankFieldLabel() {
    try {
      FieldDefinition(id = "field1", label = "", type = FieldType.Text())
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("label must not be blank", e.message)
    }
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForTextFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 3, maxLength = 10),
            defaultValue = FieldValue.TextValue("hello"))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForTextFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 10, maxLength = 20),
            defaultValue = FieldValue.TextValue("short"))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Text is shorter than minLength of 10 characters",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForNumberFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "number1",
            label = "Number Field",
            type = FieldType.Number(min = 0.0, max = 100.0),
            defaultValue = FieldValue.NumberValue(50.0))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForNumberFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "number1",
            label = "Number Field",
            type = FieldType.Number(min = 0.0, max = 100.0),
            defaultValue = FieldValue.NumberValue(150.0))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Number is greater than maximum of 100.0",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForDateFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "date1",
            label = "Date Field",
            type = FieldType.Date(minDate = "2020-01-01", maxDate = "2030-12-31"),
            defaultValue = FieldValue.DateValue("2025-06-15"))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForDateFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "date1",
            label = "Date Field",
            type = FieldType.Date(minDate = "2020-01-01", maxDate = "2030-12-31"),
            defaultValue = FieldValue.DateValue("2035-01-01"))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Date is after maximum date of 2030-12-31",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForSingleSelectFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "select1",
            label = "Select Field",
            type =
                FieldType.SingleSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))),
            defaultValue = FieldValue.SingleSelectValue("opt1"))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForSingleSelectFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "select1",
            label = "Select Field",
            type =
                FieldType.SingleSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))),
            defaultValue = FieldValue.SingleSelectValue("opt3"))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Value 'opt3' is not in allowed options: [opt1, opt2]",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForMultiSelectFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "multiselect1",
            label = "Multi Select Field",
            type =
                FieldType.MultiSelect(
                    options =
                        listOf(
                            SelectOption("opt1", "Option 1"),
                            SelectOption("opt2", "Option 2"),
                            SelectOption("opt3", "Option 3")),
                    minSelections = 1,
                    maxSelections = 3),
            defaultValue = FieldValue.MultiSelectValue(listOf("opt1", "opt2")))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForMultiSelectFieldWithInvalidDefaultValueTooFew() {
    val field =
        FieldDefinition(
            id = "multiselect1",
            label = "Multi Select Field",
            type =
                FieldType.MultiSelect(
                    options =
                        listOf(
                            SelectOption("opt1", "Option 1"),
                            SelectOption("opt2", "Option 2"),
                            SelectOption("opt3", "Option 3")),
                    minSelections = 2,
                    maxSelections = 3),
            defaultValue = FieldValue.MultiSelectValue(listOf("opt1")))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Must select at least 2 options", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForMultiSelectFieldWithInvalidDefaultValueTooMany() {
    val field =
        FieldDefinition(
            id = "multiselect1",
            label = "Multi Select Field",
            type =
                FieldType.MultiSelect(
                    options =
                        listOf(
                            SelectOption("opt1", "Option 1"),
                            SelectOption("opt2", "Option 2"),
                            SelectOption("opt3", "Option 3")),
                    minSelections = 1,
                    maxSelections = 2),
            defaultValue = FieldValue.MultiSelectValue(listOf("opt1", "opt2", "opt3")))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Must select at most 2 options", result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsErrorForMultiSelectFieldWithInvalidDefaultValueNotInOptions() {
    val field =
        FieldDefinition(
            id = "multiselect1",
            label = "Multi Select Field",
            type =
                FieldType.MultiSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))),
            defaultValue = FieldValue.MultiSelectValue(listOf("opt1", "opt3")))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertTrue(result.isFailure)
    assertEquals(
        "Invalid default value: Values [opt3] are not in allowed options: [opt1, opt2]",
        result.exceptionOrNull()?.message)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForTextFieldWithNullDefaultValue() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 5, maxLength = 10),
            defaultValue = null)
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessForNumberFieldWithZeroDecimals() {
    val field =
        FieldDefinition(
            id = "number1", label = "Number Field", type = FieldType.Number(decimals = 0))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessWhenMinLengthEqualsMaxLength() {
    val field =
        FieldDefinition(
            id = "text1", label = "Text Field", type = FieldType.Text(minLength = 5, maxLength = 5))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessWhenMinEqualsMax() {
    val field =
        FieldDefinition(
            id = "number1", label = "Number Field", type = FieldType.Number(min = 50.0, max = 50.0))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }

  @Test
  fun validateFieldDefinition_returnsSuccessWhenMinSelectionsEqualsMaxSelections() {
    val field =
        FieldDefinition(
            id = "multiselect1",
            label = "Multi Select Field",
            type =
                FieldType.MultiSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2")),
                    minSelections = 2,
                    maxSelections = 2))
    assertTrue(TemplateValidation.validateFieldDefinition(field).isSuccess)
  }
}
