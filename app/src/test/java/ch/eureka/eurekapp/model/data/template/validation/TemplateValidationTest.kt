// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code

package ch.eureka.eurekapp.model.data.template.validation

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TemplateValidationTest {

  @Test
  fun validateTitleReturnsNullForValidTitle() {
    assertNull(TemplateValidation.validateTitle("Valid Title"))
  }

  @Test
  fun validateTitleReturnsErrorForBlankTitle() {
    assertEquals("Title is required", TemplateValidation.validateTitle(""))
  }

  @Test
  fun validateTitleReturnsErrorForWhitespaceOnlyTitle() {
    assertEquals("Title is required", TemplateValidation.validateTitle("   "))
  }

  @Test
  fun validateFieldsReturnsNullForNonEmptyList() {
    val fields = listOf(FieldDefinition(id = "field1", label = "Label", type = FieldType.Text()))
    assertNull(TemplateValidation.validateFields(fields))
  }

  @Test
  fun validateFieldsReturnsErrorForEmptyList() {
    assertEquals("At least one field is required", TemplateValidation.validateFields(emptyList()))
  }

  @Test
  fun validateFieldLabelReturnsNullForValidLabel() {
    assertNull(TemplateValidation.validateFieldLabel("Valid Label"))
  }

  @Test
  fun validateFieldLabelReturnsErrorForBlankLabel() {
    assertEquals("Field label is required", TemplateValidation.validateFieldLabel(""))
  }

  @Test
  fun validateFieldLabelReturnsErrorForWhitespaceOnlyLabel() {
    assertEquals("Field label is required", TemplateValidation.validateFieldLabel("   "))
  }

  @Test
  fun validateFieldDefinitionReturnsNullForValidTextField() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 5, maxLength = 10))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForInvalidTextFieldMinMaxLength() {
    try {
      FieldDefinition(
          id = "text1", label = "Text Field", type = FieldType.Text(minLength = 10, maxLength = 5))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("maxLength must be >= minLength", e.message)
    }
  }

  @Test
  fun validateFieldDefinitionReturnsNullForValidNumberField() {
    val field =
        FieldDefinition(
            id = "number1",
            label = "Number Field",
            type = FieldType.Number(min = 0.0, max = 100.0, decimals = 2))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForInvalidNumberFieldMinMax() {
    try {
      FieldDefinition(
          id = "number1", label = "Number Field", type = FieldType.Number(min = 100.0, max = 0.0))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("max must be >= min", e.message)
    }
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForNegativeDecimals() {
    try {
      FieldDefinition(
          id = "number1", label = "Number Field", type = FieldType.Number(decimals = -1))
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("decimals must be non-negative", e.message)
    }
  }

  @Test
  fun validateFieldDefinitionReturnsNullForValidDateField() {
    val field = FieldDefinition(id = "date1", label = "Date Field", type = FieldType.Date())
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsNullForValidSingleSelectField() {
    val field =
        FieldDefinition(
            id = "select1",
            label = "Select Field",
            type =
                FieldType.SingleSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsNullForValidMultiSelectField() {
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
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForInvalidMultiSelectMinMax() {
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
  fun validateFieldDefinitionReturnsErrorForBlankFieldLabel() {
    try {
      FieldDefinition(id = "field1", label = "", type = FieldType.Text())
      org.junit.Assert.fail("Expected IllegalArgumentException")
    } catch (e: IllegalArgumentException) {
      assertEquals("label must not be blank", e.message)
    }
  }

  @Test
  fun validateFieldDefinitionReturnsNullForTextFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 3, maxLength = 10),
            defaultValue = FieldValue.TextValue("hello"))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForTextFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 10, maxLength = 20),
            defaultValue = FieldValue.TextValue("short"))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertEquals("Invalid default value: Text is shorter than minLength of 10 characters", result)
  }

  @Test
  fun validateFieldDefinitionReturnsNullForNumberFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "number1",
            label = "Number Field",
            type = FieldType.Number(min = 0.0, max = 100.0),
            defaultValue = FieldValue.NumberValue(50.0))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForNumberFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "number1",
            label = "Number Field",
            type = FieldType.Number(min = 0.0, max = 100.0),
            defaultValue = FieldValue.NumberValue(150.0))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertEquals("Invalid default value: Number is greater than maximum of 100.0", result)
  }

  @Test
  fun validateFieldDefinitionReturnsNullForDateFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "date1",
            label = "Date Field",
            type = FieldType.Date(minDate = "2020-01-01", maxDate = "2030-12-31"),
            defaultValue = FieldValue.DateValue("2025-06-15"))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForDateFieldWithInvalidDefaultValue() {
    val field =
        FieldDefinition(
            id = "date1",
            label = "Date Field",
            type = FieldType.Date(minDate = "2020-01-01", maxDate = "2030-12-31"),
            defaultValue = FieldValue.DateValue("2035-01-01"))
    val result = TemplateValidation.validateFieldDefinition(field)
    assertEquals("Invalid default value: Date is after maximum date of 2030-12-31", result)
  }

  @Test
  fun validateFieldDefinitionReturnsNullForSingleSelectFieldWithValidDefaultValue() {
    val field =
        FieldDefinition(
            id = "select1",
            label = "Select Field",
            type =
                FieldType.SingleSelect(
                    options =
                        listOf(SelectOption("opt1", "Option 1"), SelectOption("opt2", "Option 2"))),
            defaultValue = FieldValue.SingleSelectValue("opt1"))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForSingleSelectFieldWithInvalidDefaultValue() {
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
    assertEquals(
        "Invalid default value: Value 'opt3' is not in allowed options: [opt1, opt2]", result)
  }

  @Test
  fun validateFieldDefinitionReturnsNullForMultiSelectFieldWithValidDefaultValue() {
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
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForMultiSelectFieldWithInvalidDefaultValueTooFew() {
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
    assertEquals("Invalid default value: Must select at least 2 options", result)
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForMultiSelectFieldWithInvalidDefaultValueTooMany() {
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
    assertEquals("Invalid default value: Must select at most 2 options", result)
  }

  @Test
  fun validateFieldDefinitionReturnsErrorForMultiSelectFieldWithInvalidDefaultValueNotInOptions() {
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
    assertEquals(
        "Invalid default value: Values [opt3] are not in allowed options: [opt1, opt2]", result)
  }

  @Test
  fun validateFieldDefinitionReturnsNullForTextFieldWithNullDefaultValue() {
    val field =
        FieldDefinition(
            id = "text1",
            label = "Text Field",
            type = FieldType.Text(minLength = 5, maxLength = 10),
            defaultValue = null)
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsNullForNumberFieldWithZeroDecimals() {
    val field =
        FieldDefinition(
            id = "number1", label = "Number Field", type = FieldType.Number(decimals = 0))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsNullWhenMinLengthEqualsMaxLength() {
    val field =
        FieldDefinition(
            id = "text1", label = "Text Field", type = FieldType.Text(minLength = 5, maxLength = 5))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsNullWhenMinEqualsMax() {
    val field =
        FieldDefinition(
            id = "number1", label = "Number Field", type = FieldType.Number(min = 50.0, max = 50.0))
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }

  @Test
  fun validateFieldDefinitionReturnsNullWhenMinSelectionsEqualsMaxSelections() {
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
    assertNull(TemplateValidation.validateFieldDefinition(field))
  }
}
