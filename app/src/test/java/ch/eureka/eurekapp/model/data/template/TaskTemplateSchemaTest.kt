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
  fun emptySchemaIsValid() {
    val schema = TaskTemplateSchema()
    assertEquals(0, schema.fields.size)
  }

  @Test
  fun schemaWithFieldsStoresThemCorrectly() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    assertEquals(2, schema.fields.size)
    assertEquals(field1, schema.fields[0])
    assertEquals(field2, schema.fields[1])
  }

  @Test(expected = IllegalArgumentException::class)
  fun schemaRejectsDuplicateFieldIds() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("title", "Another Title", FieldType.Number())
    TaskTemplateSchema(listOf(field1, field2))
  }

  @Test
  fun getFieldReturnsCorrectFieldById() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    assertEquals(field1, schema.getField("title"))
    assertEquals(field2, schema.getField("priority"))
  }

  @Test
  fun getFieldReturnsNullForNonExistentField() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertNull(schema.getField("non_existent"))
  }

  @Test
  fun hasFieldReturnsTrueForExistingField() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertTrue(schema.hasField("title"))
  }

  @Test
  fun hasFieldReturnsFalseForNonExistentField() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertFalse(schema.hasField("non_existent"))
  }

  @Test
  fun getRequiredFieldsReturnsOnlyRequiredFields() {
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
  fun getRequiredFieldsReturnsEmptyListWhenNoRequiredFields() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text(), required = false)
    val schema = TaskTemplateSchema(listOf(field1))

    assertEquals(0, schema.getRequiredFields().size)
  }

  @Test
  fun addFieldAddsNewField() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val newSchema = schema.addField(field2)

    assertEquals(2, newSchema.fields.size)
    assertTrue(newSchema.hasField("title"))
    assertTrue(newSchema.hasField("priority"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun addFieldRejectsDuplicateFieldId() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("title", "Another Title", FieldType.Number())
    schema.addField(field2)
  }

  @Test
  fun addFieldIsImmutable() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val newSchema = schema.addField(field2)

    assertEquals(1, schema.fields.size)
    assertEquals(2, newSchema.fields.size)
  }

  @Test
  fun removeFieldRemovesExistingField() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    val newSchema = schema.removeField("title")

    assertEquals(1, newSchema.fields.size)
    assertFalse(newSchema.hasField("title"))
    assertTrue(newSchema.hasField("priority"))
  }

  @Test
  fun removeFieldWithNonExistentIdDoesNothing() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val newSchema = schema.removeField("non_existent")

    assertEquals(1, newSchema.fields.size)
    assertTrue(newSchema.hasField("title"))
  }

  @Test
  fun removeFieldIsImmutable() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val newSchema = schema.removeField("title")

    assertEquals(1, schema.fields.size)
    assertEquals(0, newSchema.fields.size)
  }

  @Test
  fun updateFieldUpdatesExistingField() {
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
  fun updateFieldRejectsNonExistentFieldId() {
    val schema = TaskTemplateSchema()
    val field = FieldDefinition("title", "Title", FieldType.Text())
    schema.updateField("non_existent", field)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateFieldRejectsChangingFieldId() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField = FieldDefinition("new_id", "Updated Title", FieldType.Text())
    schema.updateField("title", updatedField)
  }

  @Test
  fun updateFieldIsImmutable() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField = FieldDefinition("title", "Updated Title", FieldType.Text())
    val newSchema = schema.updateField("title", updatedField)

    assertEquals("Title", schema.getField("title")!!.label)
    assertEquals("Updated Title", newSchema.getField("title")!!.label)
  }

  @Test
  fun schemaSupportsMultipleFieldTypes() {
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
