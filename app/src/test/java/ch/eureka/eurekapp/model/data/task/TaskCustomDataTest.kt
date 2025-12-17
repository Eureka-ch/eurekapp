/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.task

import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskCustomDataTest {

  private fun createSchema(vararg fields: FieldDefinition) = TaskTemplateSchema(fields.toList())

  private fun textField(id: String, label: String = id, required: Boolean = false) =
      FieldDefinition(id, label, FieldType.Text(), required)

  private fun numberField(id: String, label: String = id, required: Boolean = false) =
      FieldDefinition(id, label, FieldType.Number(), required)

  private fun assertValidationSuccess(customData: TaskCustomData, schema: TaskTemplateSchema) {
    val result = customData.validate(schema)
    assertTrue(result is ValidationResult.Success)
  }

  private fun assertValidationFailure(
      customData: TaskCustomData,
      schema: TaskTemplateSchema,
      expectedErrorCount: Int,
      errorSubstring: String? = null
  ) {
    val result = customData.validate(schema)
    assertTrue(result is ValidationResult.Failure)
    val errors = (result as ValidationResult.Failure).errors
    assertEquals(expectedErrorCount, errors.size)
    if (errorSubstring != null) {
      assertTrue(errors.any { it.contains(errorSubstring) })
    }
  }

  @Test
  fun taskCustomData_emptyDataIsValid() {
    val customData = TaskCustomData()
    assertEquals(0, customData.data.size)
  }

  @Test
  fun taskCustomData_dataWithValuesStoresThemCorrectly() {
    val customData =
        TaskCustomData(
            mapOf(
                "title" to FieldValue.TextValue("My Task"),
                "priority" to FieldValue.NumberValue(5.0)))

    assertEquals(2, customData.data.size)
    assertEquals(FieldValue.TextValue("My Task"), customData.getValue("title"))
    assertEquals(FieldValue.NumberValue(5.0), customData.getValue("priority"))
  }

  @Test
  fun taskCustomData_getValueReturnsCorrectValue() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertEquals(FieldValue.TextValue("My Task"), customData.getValue("title"))
  }

  @Test
  fun taskCustomData_getValueReturnsNullForNonExistentField() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertNull(customData.getValue("non_existent"))
  }

  @Test
  fun taskCustomData_hasValueReturnsTrueForExistingField() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertTrue(customData.hasValue("title"))
  }

  @Test
  fun taskCustomData_hasValueReturnsFalseForNonExistentField() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertFalse(customData.hasValue("non_existent"))
  }

  @Test
  fun taskCustomData_setValueAddsNewValue() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.setValue("priority", FieldValue.NumberValue(5.0))

    assertEquals(2, newData.data.size)
    assertEquals(FieldValue.TextValue("My Task"), newData.getValue("title"))
    assertEquals(FieldValue.NumberValue(5.0), newData.getValue("priority"))
  }

  @Test
  fun taskCustomData_setValueReplacesExistingValue() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.setValue("title", FieldValue.TextValue("Updated Task"))

    assertEquals(1, newData.data.size)
    assertEquals(FieldValue.TextValue("Updated Task"), newData.getValue("title"))
  }

  @Test
  fun taskCustomData_setValueIsImmutable() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.setValue("priority", FieldValue.NumberValue(5.0))

    assertEquals(1, customData.data.size)
    assertEquals(2, newData.data.size)
  }

  @Test
  fun taskCustomData_removeValueRemovesExistingValue() {
    val customData =
        TaskCustomData(
            mapOf(
                "title" to FieldValue.TextValue("My Task"),
                "priority" to FieldValue.NumberValue(5.0)))

    val newData = customData.removeValue("title")

    assertEquals(1, newData.data.size)
    assertFalse(newData.hasValue("title"))
    assertTrue(newData.hasValue("priority"))
  }

  @Test
  fun taskCustomData_removeValueWithNonExistentFieldDoesNothing() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.removeValue("non_existent")

    assertEquals(1, newData.data.size)
    assertTrue(newData.hasValue("title"))
  }

  @Test
  fun taskCustomData_removeValueIsImmutable() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.removeValue("title")

    assertEquals(1, customData.data.size)
    assertEquals(0, newData.data.size)
  }

  @Test
  fun taskCustomData_validateReturnsSuccessForValidData() {
    val schema =
        createSchema(
            textField("title", "Title", required = true), numberField("priority", "Priority"))
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))
    assertValidationSuccess(customData, schema)
  }

  @Test
  fun taskCustomData_validateReturnsFailureForMissingRequiredField() {
    val schema = createSchema(textField("title", "Title", required = true))
    val customData = TaskCustomData()
    assertValidationFailure(customData, schema, 1, "Required field 'title' is missing")
  }

  @Test
  fun taskCustomData_validateReturnsFailureForUndefinedField() {
    val schema = createSchema(textField("title", "Title"))
    val customData = TaskCustomData(mapOf("undefined_field" to FieldValue.TextValue("Some value")))
    assertValidationFailure(
        customData, schema, 1, "Field 'undefined_field' is not defined in schema")
  }

  @Test
  fun taskCustomData_validateReturnsFailureForTypeMismatch() {
    val schema = createSchema(numberField("priority", "Priority"))
    val customData = TaskCustomData(mapOf("priority" to FieldValue.TextValue("high")))
    assertValidationFailure(customData, schema, 1, "has incorrect type")
  }

  @Test
  fun taskCustomData_validateReturnsMultipleErrors() {
    val schema =
        createSchema(
            textField("title", "Title", required = true),
            numberField("priority", "Priority", required = true))
    val customData =
        TaskCustomData(
            mapOf(
                "priority" to FieldValue.TextValue("wrong type"),
                "extra" to FieldValue.TextValue("extra")))
    assertValidationFailure(customData, schema, 3)
  }

  @Test
  fun taskCustomData_validateSucceedsWithAllFieldTypes() {
    val schema =
        createSchema(
            textField("text", "Text"),
            numberField("number", "Number"),
            FieldDefinition("date", "Date", FieldType.Date()),
            FieldDefinition(
                "select",
                "Select",
                FieldType.SingleSelect(listOf(SelectOption("opt1", "Option 1")))),
            FieldDefinition(
                "multi", "Multi", FieldType.MultiSelect(listOf(SelectOption("opt1", "Option 1")))))
    val customData =
        TaskCustomData(
            mapOf(
                "text" to FieldValue.TextValue("value"),
                "number" to FieldValue.NumberValue(42.0),
                "date" to FieldValue.DateValue("2024-01-15"),
                "select" to FieldValue.SingleSelectValue("opt1"),
                "multi" to FieldValue.MultiSelectValue(listOf("opt1"))))
    assertValidationSuccess(customData, schema)
  }

  @Test
  fun taskCustomData_validateSucceedsWithEmptyDataAndNoRequiredFields() {
    val schema = createSchema(textField("title", "Title", required = false))
    val customData = TaskCustomData()
    assertValidationSuccess(customData, schema)
  }
}
