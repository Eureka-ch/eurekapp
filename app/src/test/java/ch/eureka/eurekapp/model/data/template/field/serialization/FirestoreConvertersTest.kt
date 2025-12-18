/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field.serialization

import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreConvertersTest {

  @Test
  fun emptySchema_roundTripConversion() {
    val schema = TaskTemplateSchema()
    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    assertEquals(schema.fields.size, converted.fields.size)
  }

  @Test
  fun schemaWithTextField_roundTripConversion() {
    val field =
        FieldDefinition(
            id = "title",
            label = "Title",
            type = FieldType.Text(maxLength = 100, minLength = 10, placeholder = "Enter title"),
            required = true,
            description = "Task title")
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    assertEquals(1, converted.fields.size)
    val convertedField = converted.fields[0]
    assertEquals("title", convertedField.id)
    assertEquals("Title", convertedField.label)
    assertEquals(true, convertedField.required)
    assertEquals("Task title", convertedField.description)

    val textType = convertedField.type as FieldType.Text
    assertEquals(100, textType.maxLength)
    assertEquals(10, textType.minLength)
    assertEquals("Enter title", textType.placeholder)
  }

  @Test
  fun schemaWithNumberField_roundTripConversion() {
    val field =
        FieldDefinition(
            id = "hours",
            label = "Hours",
            type = FieldType.Number(min = 0.0, max = 100.0, step = 0.5, decimals = 2, unit = "h"),
            required = false)
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    val convertedField = converted.fields[0]
    val numberType = convertedField.type as FieldType.Number
    assertEquals(0.0, numberType.min!!, 0.0)
    assertEquals(100.0, numberType.max!!, 0.0)
    assertEquals(0.5, numberType.step!!, 0.0)
    assertEquals(2, numberType.decimals)
    assertEquals("h", numberType.unit)
  }

  @Test
  fun schemaWithDateField_roundTripConversion() {
    val field =
        FieldDefinition(
            id = "due_date",
            label = "Due Date",
            type =
                FieldType.Date(
                    minDate = "2024-01-01",
                    maxDate = "2024-12-31",
                    includeTime = true,
                    format = "yyyy-MM-dd"))
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    val convertedField = converted.fields[0]
    val dateType = convertedField.type as FieldType.Date
    assertEquals("2024-01-01", dateType.minDate)
    assertEquals("2024-12-31", dateType.maxDate)
    assertEquals(true, dateType.includeTime)
    assertEquals("yyyy-MM-dd", dateType.format)
  }

  @Test
  fun schemaWithSingleSelectField_roundTripConversion() {
    val options =
        listOf(
            SelectOption("low", "Low", "Low priority"),
            SelectOption("medium", "Medium"),
            SelectOption("high", "High", "High priority"))
    val field =
        FieldDefinition(
            id = "priority",
            label = "Priority",
            type = FieldType.SingleSelect(options, allowCustom = true))
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    val convertedField = converted.fields[0]
    val selectType = convertedField.type as FieldType.SingleSelect
    assertEquals(3, selectType.options.size)
    assertEquals("low", selectType.options[0].value)
    assertEquals("Low", selectType.options[0].label)
    assertEquals("Low priority", selectType.options[0].description)
    assertEquals(true, selectType.allowCustom)
  }

  @Test
  fun schemaWithMultiSelectField_roundTripConversion() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val field =
        FieldDefinition(
            id = "tags",
            label = "Tags",
            type =
                FieldType.MultiSelect(
                    options, minSelections = 1, maxSelections = 2, allowCustom = false))
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    val convertedField = converted.fields[0]
    val multiSelectType = convertedField.type as FieldType.MultiSelect
    assertEquals(2, multiSelectType.options.size)
    assertEquals(1, multiSelectType.minSelections)
    assertEquals(2, multiSelectType.maxSelections)
    assertEquals(false, multiSelectType.allowCustom)
  }

  @Test
  fun schemaWithMultipleFields_roundTripConversion() {
    val fields =
        listOf(
            FieldDefinition("title", "Title", FieldType.Text(), required = true),
            FieldDefinition("hours", "Hours", FieldType.Number()),
            FieldDefinition("due_date", "Due Date", FieldType.Date()),
            FieldDefinition(
                "priority", "Priority", FieldType.SingleSelect(listOf(SelectOption("low", "Low")))),
            FieldDefinition(
                "tags", "Tags", FieldType.MultiSelect(listOf(SelectOption("tag1", "Tag 1")))))
    val schema = TaskTemplateSchema(fields)

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    assertEquals(5, converted.fields.size)
  }

  @Test
  fun schemaWithDefaultValue_roundTripConversion() {
    val field =
        FieldDefinition(
            id = "priority",
            label = "Priority",
            type = FieldType.Text(),
            defaultValue = FieldValue.TextValue("medium"))
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    val convertedField = converted.fields[0]
    assertEquals(FieldValue.TextValue("medium"), convertedField.defaultValue)
  }

  @Test
  fun emptyCustomData_roundTripConversion() {
    val customData = TaskCustomData()
    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    assertEquals(0, converted.data.size)
  }

  @Test
  fun customDataWithTextValue_roundTripConversion() {
    val customData = TaskCustomData(mapOf("title" to FieldValue.TextValue("My Task")))

    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    assertEquals(1, converted.data.size)
    assertEquals(FieldValue.TextValue("My Task"), converted.getValue("title"))
  }

  @Test
  fun customDataWithNumberValue_roundTripConversion() {
    val customData = TaskCustomData(mapOf("hours" to FieldValue.NumberValue(42.5)))

    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    assertEquals(FieldValue.NumberValue(42.5), converted.getValue("hours"))
  }

  @Test
  fun customDataWithDateValue_roundTripConversion() {
    val customData = TaskCustomData(mapOf("due_date" to FieldValue.DateValue("2024-01-15")))

    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    assertEquals(FieldValue.DateValue("2024-01-15"), converted.getValue("due_date"))
  }

  @Test
  fun customDataWithSingleSelectValue_roundTripConversion() {
    val customData = TaskCustomData(mapOf("priority" to FieldValue.SingleSelectValue("high")))

    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    assertEquals(FieldValue.SingleSelectValue("high"), converted.getValue("priority"))
  }

  @Test
  fun customDataWithMultiSelectValue_roundTripConversion() {
    val customData =
        TaskCustomData(mapOf("tags" to FieldValue.MultiSelectValue(listOf("tag1", "tag2"))))

    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    val multiSelectValue = converted.getValue("tags") as FieldValue.MultiSelectValue
    assertEquals(2, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
    assertEquals("tag2", multiSelectValue.values[1])
  }

  @Test
  fun customDataWithAllFieldTypes_roundTripConversion() {
    val customData =
        TaskCustomData(
            mapOf(
                "text" to FieldValue.TextValue("value"),
                "number" to FieldValue.NumberValue(42.0),
                "date" to FieldValue.DateValue("2024-01-15"),
                "select" to FieldValue.SingleSelectValue("opt1"),
                "multi" to FieldValue.MultiSelectValue(listOf("opt1", "opt2"))))

    val map = FirestoreConverters.customDataToMap(customData)
    val converted = FirestoreConverters.mapToCustomData(map)

    assertEquals(5, converted.data.size)
    assertEquals(FieldValue.TextValue("value"), converted.getValue("text"))
    assertEquals(FieldValue.NumberValue(42.0), converted.getValue("number"))
    assertEquals(FieldValue.DateValue("2024-01-15"), converted.getValue("date"))
    assertEquals(FieldValue.SingleSelectValue("opt1"), converted.getValue("select"))

    val multiSelectValue = converted.getValue("multi") as FieldValue.MultiSelectValue
    assertEquals(2, multiSelectValue.values.size)
  }

  @Test
  fun fieldTypeWithMinimalProperties_roundTripConversion() {
    val field = FieldDefinition(id = "simple", label = "Simple", type = FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field))

    val map = FirestoreConverters.schemaToMap(schema)
    val converted = FirestoreConverters.mapToSchema(map)

    val convertedField = converted.fields[0]
    val textType = convertedField.type as FieldType.Text
    assertEquals(null, textType.maxLength)
    assertEquals(null, textType.minLength)
    assertEquals(null, textType.pattern)
    assertEquals(null, textType.placeholder)
  }
}
