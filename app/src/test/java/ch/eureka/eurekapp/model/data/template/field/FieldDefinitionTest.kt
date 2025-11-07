/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldDefinitionTest {

  @Test
  fun `FieldDefinition accepts valid data`() {
    val fieldDef =
        FieldDefinition(
            id = "severity",
            label = "Severity",
            type = FieldType.SingleSelect(listOf(SelectOption("low", "Low"))),
            required = true,
            description = "Bug severity level")

    assertEquals("severity", fieldDef.id)
    assertEquals("Severity", fieldDef.label)
    assertEquals(FieldTypeKey.SINGLE_SELECT, fieldDef.type.typeKey)
    assertEquals(true, fieldDef.required)
    assertEquals("Bug severity level", fieldDef.description)
  }

  @Test
  fun `FieldDefinition accepts minimal data`() {
    val fieldDef = FieldDefinition(id = "title", label = "Title", type = FieldType.Text())

    assertEquals("title", fieldDef.id)
    assertEquals("Title", fieldDef.label)
    assertEquals(false, fieldDef.required)
    assertEquals(null, fieldDef.description)
    assertEquals(null, fieldDef.defaultValue)
  }

  @Test
  fun `FieldDefinition accepts matching defaultValue`() {
    val fieldDef =
        FieldDefinition(
            id = "priority",
            label = "Priority",
            type = FieldType.Text(),
            defaultValue = FieldValue.TextValue("medium"))

    assertEquals(FieldValue.TextValue("medium"), fieldDef.defaultValue)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `FieldDefinition rejects blank id`() {
    FieldDefinition(id = "", label = "Label", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `FieldDefinition rejects blank label`() {
    FieldDefinition(id = "id", label = "", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `FieldDefinition rejects whitespace-only id`() {
    FieldDefinition(id = "   ", label = "Label", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `FieldDefinition rejects whitespace-only label`() {
    FieldDefinition(id = "id", label = "   ", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `FieldDefinition rejects mismatched defaultValue type`() {
    FieldDefinition(
        id = "count",
        label = "Count",
        type = FieldType.Number(),
        defaultValue = FieldValue.TextValue("not a number"))
  }

  @Test
  fun `FieldDefinition accepts all field types`() {
    val textField = FieldDefinition(id = "text", label = "Text", type = FieldType.Text())
    assertEquals(FieldTypeKey.TEXT, textField.type.typeKey)

    val numberField = FieldDefinition(id = "number", label = "Number", type = FieldType.Number())
    assertEquals(FieldTypeKey.NUMBER, numberField.type.typeKey)

    val dateField = FieldDefinition(id = "date", label = "Date", type = FieldType.Date())
    assertEquals(FieldTypeKey.DATE, dateField.type.typeKey)

    val singleSelectField =
        FieldDefinition(
            id = "select",
            label = "Select",
            type = FieldType.SingleSelect(listOf(SelectOption("opt1", "Option 1"))))
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelectField.type.typeKey)

    val multiSelectField =
        FieldDefinition(
            id = "multi",
            label = "Multi",
            type = FieldType.MultiSelect(listOf(SelectOption("opt1", "Option 1"))))
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelectField.type.typeKey)
  }
}
