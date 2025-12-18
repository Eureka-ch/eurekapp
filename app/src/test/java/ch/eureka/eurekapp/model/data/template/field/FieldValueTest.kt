/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldValueTest {

  @Test
  fun textValue_hasCorrectTypeKey() {
    val textValue = FieldValue.TextValue("Hello")
    assertEquals(FieldTypeKey.TEXT, textValue.typeKey)
  }

  @Test
  fun textValue_storesValueCorrectly() {
    val textValue = FieldValue.TextValue("Hello World")
    assertEquals("Hello World", textValue.value)
  }

  @Test
  fun textValue_acceptsEmptyString() {
    val textValue = FieldValue.TextValue("")
    assertEquals("", textValue.value)
  }

  @Test
  fun numberValue_hasCorrectTypeKey() {
    val numberValue = FieldValue.NumberValue(42.0)
    assertEquals(FieldTypeKey.NUMBER, numberValue.typeKey)
  }

  @Test
  fun numberValue_storesValueCorrectly() {
    val numberValue = FieldValue.NumberValue(123.456)
    assertEquals(123.456, numberValue.value)
  }

  @Test
  fun numberValue_acceptsNegativeValues() {
    val numberValue = FieldValue.NumberValue(-10.5)
    assertEquals(-10.5, numberValue.value)
  }

  @Test
  fun numberValue_acceptsZero() {
    val numberValue = FieldValue.NumberValue(0.0)
    assertEquals(0.0, numberValue.value)
  }

  @Test
  fun numberValue_acceptsNull() {
    val numberValue = FieldValue.NumberValue(null)
    assertEquals(null, numberValue.value)
  }

  @Test
  fun dateValue_hasCorrectTypeKey() {
    val dateValue = FieldValue.DateValue("2024-01-15")
    assertEquals(FieldTypeKey.DATE, dateValue.typeKey)
  }

  @Test
  fun dateValue_storesValueCorrectly() {
    val dateValue = FieldValue.DateValue("2024-01-15T10:30:00")
    assertEquals("2024-01-15T10:30:00", dateValue.value)
  }

  @Test
  fun dateValue_acceptsEmptyString() {
    val dateValue = FieldValue.DateValue("")
    assertEquals("", dateValue.value)
  }

  @Test
  fun singleSelectValue_hasCorrectTypeKey() {
    val singleSelectValue = FieldValue.SingleSelectValue("option1")
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelectValue.typeKey)
  }

  @Test
  fun singleSelectValue_storesValueCorrectly() {
    val singleSelectValue = FieldValue.SingleSelectValue("high")
    assertEquals("high", singleSelectValue.value)
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectValue_rejectsBlankValue() {
    FieldValue.SingleSelectValue("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectValue_rejectsWhitespaceOnlyValue() {
    FieldValue.SingleSelectValue("   ")
  }

  @Test
  fun multiSelectValue_hasCorrectTypeKey() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1", "tag2"))
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelectValue.typeKey)
  }

  @Test
  fun multiSelectValue_storesValuesCorrectly() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag3"))
    assertEquals(3, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
    assertEquals("tag2", multiSelectValue.values[1])
    assertEquals("tag3", multiSelectValue.values[2])
  }

  @Test
  fun multiSelectValue_acceptsEmptyList() {
    val multiSelectValue = FieldValue.MultiSelectValue(emptyList())
    assertEquals(0, multiSelectValue.values.size)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectValue_rejectsBlankValues() {
    FieldValue.MultiSelectValue(listOf("tag1", "", "tag3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectValue_rejectsWhitespaceOnlyValues() {
    FieldValue.MultiSelectValue(listOf("tag1", "   ", "tag3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectValue_rejectsDuplicateValues() {
    FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag1"))
  }

  @Test
  fun multiSelectValue_withSingleValue() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1"))
    assertEquals(1, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
  }
}
