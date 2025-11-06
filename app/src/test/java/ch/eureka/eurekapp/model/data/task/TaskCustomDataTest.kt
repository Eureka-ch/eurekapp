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
  fun `empty data is valid`() {
    val customData = TaskCustomData()
    assertEquals(0, customData.data.size)
  }

  @Test
  fun `data with values stores them correctly`() {
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
  fun `getValue returns correct value`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertEquals(FieldValue.TextValue("My Task"), customData.getValue("title"))
  }

  @Test
  fun `getValue returns null for non-existent field`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertNull(customData.getValue("non_existent"))
  }

  @Test
  fun `hasValue returns true for existing field`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertTrue(customData.hasValue("title"))
  }

  @Test
  fun `hasValue returns false for non-existent field`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    assertFalse(customData.hasValue("non_existent"))
  }

  @Test
  fun `setValue adds new value`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.setValue("priority", FieldValue.NumberValue(5.0))

    assertEquals(2, newData.data.size)
    assertEquals(FieldValue.TextValue("My Task"), newData.getValue("title"))
    assertEquals(FieldValue.NumberValue(5.0), newData.getValue("priority"))
  }

  @Test
  fun `setValue replaces existing value`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.setValue("title", FieldValue.TextValue("Updated Task"))

    assertEquals(1, newData.data.size)
    assertEquals(FieldValue.TextValue("Updated Task"), newData.getValue("title"))
  }

  @Test
  fun `setValue is immutable`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.setValue("priority", FieldValue.NumberValue(5.0))

    assertEquals(1, customData.data.size)
    assertEquals(2, newData.data.size)
  }

  @Test
  fun `removeValue removes existing value`() {
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
  fun `removeValue with non-existent field does nothing`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.removeValue("non_existent")

    assertEquals(1, newData.data.size)
    assertTrue(newData.hasValue("title"))
  }

  @Test
  fun `removeValue is immutable`() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val newData = customData.removeValue("title")

    assertEquals(1, customData.data.size)
    assertEquals(0, newData.data.size)
  }

  @Test
  fun `validate returns success for valid data`() {
    val schema = createSchema(textField("title", "Title", required = true), numberField("priority", "Priority"))
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))
    assertValidationSuccess(customData, schema)
  }

  @Test
  fun `validate returns failure for missing required field`() {
    val schema = createSchema(textField("title", "Title", required = true))
    val customData = TaskCustomData()
    assertValidationFailure(customData, schema, 1, "Required field 'title' is missing")
  }

  @Test
  fun `validate returns failure for undefined field`() {
    val schema = createSchema(textField("title", "Title"))
    val customData = TaskCustomData(mapOf("undefined_field" to FieldValue.TextValue("Some value")))
    assertValidationFailure(customData, schema, 1, "Field 'undefined_field' is not defined in schema")
  }

  @Test
  fun `validate returns failure for type mismatch`() {
    val schema = createSchema(numberField("priority", "Priority"))
    val customData = TaskCustomData(mapOf("priority" to FieldValue.TextValue("high")))
    assertValidationFailure(customData, schema, 1, "has incorrect type")
  }

  @Test
  fun `validate returns multiple errors`() {
    val schema = createSchema(textField("title", "Title", required = true), numberField("priority", "Priority", required = true))
    val customData = TaskCustomData(mapOf("priority" to FieldValue.TextValue("wrong type"), "extra" to FieldValue.TextValue("extra")))
    assertValidationFailure(customData, schema, 3)
  }

  @Test
  fun `validate succeeds with all field types`() {
    val schema = createSchema(
        textField("text", "Text"),
        numberField("number", "Number"),
        FieldDefinition("date", "Date", FieldType.Date()),
        FieldDefinition("select", "Select", FieldType.SingleSelect(listOf(SelectOption("opt1", "Option 1")))),
        FieldDefinition("multi", "Multi", FieldType.MultiSelect(listOf(SelectOption("opt1", "Option 1")))))
    val customData = TaskCustomData(mapOf(
        "text" to FieldValue.TextValue("value"),
        "number" to FieldValue.NumberValue(42.0),
        "date" to FieldValue.DateValue("2024-01-15"),
        "select" to FieldValue.SingleSelectValue("opt1"),
        "multi" to FieldValue.MultiSelectValue(listOf("opt1"))))
    assertValidationSuccess(customData, schema)
  }

  @Test
  fun `validate succeeds with empty data and no required fields`() {
    val schema = createSchema(textField("title", "Title", required = false))
    val customData = TaskCustomData()
    assertValidationSuccess(customData, schema)
  }
}
