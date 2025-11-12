/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template

import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.SelectOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskTemplateSchemaTest {

  @Test
  fun `empty schema is valid`() {
    val schema = TaskTemplateSchema()
    assertEquals(0, schema.fields.size)
  }

  @Test
  fun `schema with fields stores them correctly`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    assertEquals(2, schema.fields.size)
    assertEquals(field1, schema.fields[0])
    assertEquals(field2, schema.fields[1])
  }

  @Test(expected = IllegalArgumentException::class)
  fun `schema rejects duplicate field IDs`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("title", "Another Title", FieldType.Number())
    TaskTemplateSchema(listOf(field1, field2))
  }

  @Test
  fun `getField returns correct field by ID`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    assertEquals(field1, schema.getField("title"))
    assertEquals(field2, schema.getField("priority"))
  }

  @Test
  fun `getField returns null for non-existent field`() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertNull(schema.getField("non_existent"))
  }

  @Test
  fun `hasField returns true for existing field`() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertTrue(schema.hasField("title"))
  }

  @Test
  fun `hasField returns false for non-existent field`() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertFalse(schema.hasField("non_existent"))
  }

  @Test
  fun `getRequiredFields returns only required fields`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text(), required = true)
    val field2 = FieldDefinition("description", "Description", FieldType.Text(), required = false)
    val field3 = FieldDefinition("priority", "Priority", FieldType.Number(), required = true)
    val schema = TaskTemplateSchema(listOf(field1, field2, field3))

    val requiredFields = schema.getRequiredFields()
    assertEquals(2, requiredFields.size)
    assertTrue(requiredFields.contains(field1))
    assertTrue(requiredFields.contains(field3))
    assertFalse(requiredFields.contains(field2))
  }

  @Test
  fun `getRequiredFields returns empty list when no required fields`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text(), required = false)
    val schema = TaskTemplateSchema(listOf(field1))

    assertEquals(0, schema.getRequiredFields().size)
  }

  @Test
  fun `addField adds new field`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val newSchema = schema.addField(field2)

    assertEquals(2, newSchema.fields.size)
    assertTrue(newSchema.hasField("title"))
    assertTrue(newSchema.hasField("priority"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `addField rejects duplicate field ID`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("title", "Another Title", FieldType.Number())
    schema.addField(field2)
  }

  @Test
  fun `addField is immutable`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val newSchema = schema.addField(field2)

    assertEquals(1, schema.fields.size)
    assertEquals(2, newSchema.fields.size)
  }

  @Test
  fun `removeField removes existing field`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    val newSchema = schema.removeField("title")

    assertEquals(1, newSchema.fields.size)
    assertFalse(newSchema.hasField("title"))
    assertTrue(newSchema.hasField("priority"))
  }

  @Test
  fun `removeField with non-existent ID does nothing`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val newSchema = schema.removeField("non_existent")

    assertEquals(1, newSchema.fields.size)
    assertTrue(newSchema.hasField("title"))
  }

  @Test
  fun `removeField is immutable`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val newSchema = schema.removeField("title")

    assertEquals(1, schema.fields.size)
    assertEquals(0, newSchema.fields.size)
  }

  @Test
  fun `updateField updates existing field`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField =
        FieldDefinition("title", "Updated Title", FieldType.Text(maxLength = 100), required = true)
    val newSchema = schema.updateField("title", updatedField)

    assertEquals(1, newSchema.fields.size)
    val retrievedField = newSchema.getField("title")
    assertNotNull(retrievedField)
    assertEquals("Updated Title", retrievedField!!.label)
    assertEquals(true, retrievedField.required)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `updateField rejects non-existent field ID`() {
    val schema = TaskTemplateSchema()
    val field = FieldDefinition("title", "Title", FieldType.Text())
    schema.updateField("non_existent", field)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `updateField rejects changing field ID`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField = FieldDefinition("new_id", "Updated Title", FieldType.Text())
    schema.updateField("title", updatedField)
  }

  @Test
  fun `updateField is immutable`() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField = FieldDefinition("title", "Updated Title", FieldType.Text())
    val newSchema = schema.updateField("title", updatedField)

    assertEquals("Title", schema.getField("title")!!.label)
    assertEquals("Updated Title", newSchema.getField("title")!!.label)
  }

  @Test
  fun `schema supports multiple field types`() {
    val textField = FieldDefinition("text", "Text", FieldType.Text())
    val numberField = FieldDefinition("number", "Number", FieldType.Number())
    val dateField = FieldDefinition("date", "Date", FieldType.Date())
    val singleSelectField =
        FieldDefinition(
            "select", "Select", FieldType.SingleSelect(listOf(SelectOption("opt1", "Option 1"))))
    val multiSelectField =
        FieldDefinition(
            "multi", "Multi", FieldType.MultiSelect(listOf(SelectOption("opt1", "Option 1"))))

    val schema =
        TaskTemplateSchema(
            listOf(textField, numberField, dateField, singleSelectField, multiSelectField))

    assertEquals(5, schema.fields.size)
  }
}
