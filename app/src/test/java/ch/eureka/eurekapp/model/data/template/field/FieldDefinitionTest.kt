/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldDefinitionTest {

  @Test
  fun fieldDefinitionAcceptsValidData() {
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
  fun fieldDefinitionAcceptsMinimalData() {
    val fieldDef = FieldDefinition(id = "title", label = "Title", type = FieldType.Text())

    assertEquals("title", fieldDef.id)
    assertEquals("Title", fieldDef.label)
    assertEquals(false, fieldDef.required)
    assertEquals(null, fieldDef.description)
    assertEquals(null, fieldDef.defaultValue)
  }

  @Test
  fun fieldDefinitionAcceptsMatchingDefaultValue() {
    val fieldDef =
        FieldDefinition(
            id = "priority",
            label = "Priority",
            type = FieldType.Text(),
            defaultValue = FieldValue.TextValue("medium"))

    assertEquals(FieldValue.TextValue("medium"), fieldDef.defaultValue)
  }

  @Test(expected = IllegalArgumentException::class)
  fun fieldDefinitionRejectsBlankId() {
    FieldDefinition(id = "", label = "Label", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun fieldDefinitionRejectsBlankLabel() {
    FieldDefinition(id = "id", label = "", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun fieldDefinitionRejectsWhitespaceOnlyId() {
    FieldDefinition(id = "   ", label = "Label", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun fieldDefinitionRejectsWhitespaceOnlyLabel() {
    FieldDefinition(id = "id", label = "   ", type = FieldType.Text())
  }

  @Test(expected = IllegalArgumentException::class)
  fun fieldDefinitionRejectsMismatchedDefaultValueType() {
    FieldDefinition(
        id = "count",
        label = "Count",
        type = FieldType.Number(),
        defaultValue = FieldValue.TextValue("not a number"))
  }

  @Test
  fun fieldDefinitionAcceptsAllFieldTypes() {
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
