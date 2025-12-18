// Portions of this code were generated with the help of Claude Sonnet 4.5 in Claude Code
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
  fun emptySchema_isValid() {
    val schema = TaskTemplateSchema()
    assertEquals(0, schema.fields.size)
  }

  @Test
  fun schemaWithFields_storesThemCorrectly() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    assertEquals(2, schema.fields.size)
    assertEquals(field1, schema.fields[0])
    assertEquals(field2, schema.fields[1])
  }

  @Test(expected = IllegalArgumentException::class)
  fun schema_rejectsDuplicateFieldIDs() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("title", "Another Title", FieldType.Number())
    TaskTemplateSchema(listOf(field1, field2))
  }

  @Test
  fun getField_returnsCorrectFieldById() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    assertEquals(field1, schema.getField("title"))
    assertEquals(field2, schema.getField("priority"))
  }

  @Test
  fun getField_returnsNullForNonExistentField() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertNull(schema.getField("non_existent"))
  }

  @Test
  fun hasField_returnsTrueForExistingField() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertTrue(schema.hasField("title"))
  }

  @Test
  fun hasField_returnsFalseForNonExistentField() {
    val schema = TaskTemplateSchema(listOf(FieldDefinition("title", "Title", FieldType.Text())))

    assertFalse(schema.hasField("non_existent"))
  }

  @Test
  fun getRequiredFields_returnsOnlyRequiredFields() {
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
  fun getRequiredFields_returnsEmptyListWhenNoRequiredFields() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text(), required = false)
    val schema = TaskTemplateSchema(listOf(field1))

    assertEquals(0, schema.getRequiredFields().size)
  }

  @Test
  fun addField_addsNewField() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val newSchema = schema.addField(field2)

    assertEquals(2, newSchema.fields.size)
    assertTrue(newSchema.hasField("title"))
    assertTrue(newSchema.hasField("priority"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun addField_rejectsDuplicateFieldId() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("title", "Another Title", FieldType.Number())
    schema.addField(field2)
  }

  @Test
  fun addField_isImmutable() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val newSchema = schema.addField(field2)

    assertEquals(1, schema.fields.size)
    assertEquals(2, newSchema.fields.size)
  }

  @Test
  fun removeField_removesExistingField() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val field2 = FieldDefinition("priority", "Priority", FieldType.Number())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    val newSchema = schema.removeField("title")

    assertEquals(1, newSchema.fields.size)
    assertFalse(newSchema.hasField("title"))
    assertTrue(newSchema.hasField("priority"))
  }

  @Test
  fun removeField_withNonExistentIdDoesNothing() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val newSchema = schema.removeField("non_existent")

    assertEquals(1, newSchema.fields.size)
    assertTrue(newSchema.hasField("title"))
  }

  @Test
  fun removeField_isImmutable() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val newSchema = schema.removeField("title")

    assertEquals(1, schema.fields.size)
    assertEquals(0, newSchema.fields.size)
  }

  @Test
  fun updateField_updatesExistingField() {
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
  fun updateField_rejectsNonExistentFieldId() {
    val schema = TaskTemplateSchema()
    val field = FieldDefinition("title", "Title", FieldType.Text())
    schema.updateField("non_existent", field)
  }

  @Test(expected = IllegalArgumentException::class)
  fun updateField_rejectsChangingFieldId() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField = FieldDefinition("new_id", "Updated Title", FieldType.Text())
    schema.updateField("title", updatedField)
  }

  @Test
  fun updateField_isImmutable() {
    val field1 = FieldDefinition("title", "Title", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val updatedField = FieldDefinition("title", "Updated Title", FieldType.Text())
    val newSchema = schema.updateField("title", updatedField)

    assertEquals("Title", schema.getField("title")!!.label)
    assertEquals("Updated Title", newSchema.getField("title")!!.label)
  }

  @Test
  fun schema_supportsMultipleFieldTypes() {
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

  @Test
  fun reorderField_movesFieldToNewPosition() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val field2 = FieldDefinition("field2", "Field 2", FieldType.Text())
    val field3 = FieldDefinition("field3", "Field 3", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1, field2, field3))

    val reordered = schema.reorderField(0, 2)

    assertEquals(3, reordered.fields.size)
    assertEquals("field2", reordered.fields[0].id)
    assertEquals("field3", reordered.fields[1].id)
    assertEquals("field1", reordered.fields[2].id)
  }

  @Test
  fun reorderField_withInvalidIndicesReturnsUnchangedSchema() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val result1 = schema.reorderField(-1, 0)
    val result2 = schema.reorderField(0, 5)
    val result3 = schema.reorderField(5, 0)

    assertEquals(schema, result1)
    assertEquals(schema, result2)
    assertEquals(schema, result3)
  }

  @Test
  fun reorderField_isImmutable() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val field2 = FieldDefinition("field2", "Field 2", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    val reordered = schema.reorderField(0, 1)

    assertEquals("field1", schema.fields[0].id)
    assertEquals("field2", reordered.fields[0].id)
  }

  @Test
  fun duplicateField_createsCopyWithNewIdAndModifiedLabel() {
    val field1 = FieldDefinition("field1", "Original", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val duplicated = schema.duplicateField("field1")

    assertEquals(2, duplicated.fields.size)
    assertEquals("Original", duplicated.fields[0].label)
    assertEquals("Original (copy)", duplicated.fields[1].label)
    assertFalse(duplicated.fields[0].id == duplicated.fields[1].id)
  }

  @Test
  fun duplicateField_insertsCopyAfterOriginal() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val field2 = FieldDefinition("field2", "Field 2", FieldType.Text())
    val field3 = FieldDefinition("field3", "Field 3", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1, field2, field3))

    val duplicated = schema.duplicateField("field2")

    assertEquals(4, duplicated.fields.size)
    assertEquals("field1", duplicated.fields[0].id)
    assertEquals("field2", duplicated.fields[1].id)
    assertEquals("Field 2 (copy)", duplicated.fields[2].label)
    assertEquals("field3", duplicated.fields[3].id)
  }

  @Test
  fun duplicateField_withNonExistentIdReturnsUnchangedSchema() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val result = schema.duplicateField("non_existent")

    assertEquals(schema, result)
  }

  @Test
  fun duplicateField_isImmutable() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1))

    val duplicated = schema.duplicateField("field1")

    assertEquals(1, schema.fields.size)
    assertEquals(2, duplicated.fields.size)
  }

  @Test
  fun duplicateField_preservesFieldProperties() {
    val field1 =
        FieldDefinition(
            "field1",
            "Original",
            FieldType.Text(maxLength = 100),
            required = true,
            description = "Test description")
    val schema = TaskTemplateSchema(listOf(field1))

    val duplicated = schema.duplicateField("field1")

    val copy = duplicated.fields[1]
    assertEquals(true, copy.required)
    assertEquals("Test description", copy.description)
    assertTrue(copy.type is FieldType.Text)
    assertEquals(100, (copy.type as FieldType.Text).maxLength)
  }

  @Test
  fun reorderField_withSameIndicesReturnsUnchangedSchema() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val field2 = FieldDefinition("field2", "Field 2", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    val result = schema.reorderField(0, 0)

    assertEquals("field1", result.fields[0].id)
    assertEquals("field2", result.fields[1].id)
  }

  @Test
  fun addField_toEmptySchemaSucceeds() {
    val schema = TaskTemplateSchema()
    val field = FieldDefinition("field1", "Field 1", FieldType.Text())

    val newSchema = schema.addField(field)

    assertEquals(1, newSchema.fields.size)
    assertEquals("field1", newSchema.fields[0].id)
  }

  @Test
  fun duplicateField_atEndOfListAppendsCorrectly() {
    val field1 = FieldDefinition("field1", "Field 1", FieldType.Text())
    val field2 = FieldDefinition("field2", "Field 2", FieldType.Text())
    val schema = TaskTemplateSchema(listOf(field1, field2))

    val duplicated = schema.duplicateField("field2")

    assertEquals(3, duplicated.fields.size)
    assertEquals("field1", duplicated.fields[0].id)
    assertEquals("field2", duplicated.fields[1].id)
    assertEquals("Field 2 (copy)", duplicated.fields[2].label)
  }
}
