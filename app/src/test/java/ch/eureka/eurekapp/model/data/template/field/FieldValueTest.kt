/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldValueTest {

  @Test
  fun textValueHasCorrectTypeKey() {
    val textValue = FieldValue.TextValue("Hello")
    assertEquals(FieldTypeKey.TEXT, textValue.typeKey)
  }

  @Test
  fun textValueStoresValueCorrectly() {
    val textValue = FieldValue.TextValue("Hello World")
    assertEquals("Hello World", textValue.value)
  }

  @Test
  fun textValueAcceptsEmptyString() {
    val textValue = FieldValue.TextValue("")
    assertEquals("", textValue.value)
  }

  @Test
  fun numberValueHasCorrectTypeKey() {
    val numberValue = FieldValue.NumberValue(42.0)
    assertEquals(FieldTypeKey.NUMBER, numberValue.typeKey)
  }

  @Test
  fun numberValueStoresValueCorrectly() {
    val numberValue = FieldValue.NumberValue(123.456)
    assertEquals(123.456, numberValue.value, 0.0)
  }

  @Test
  fun numberValueAcceptsNegativeValues() {
    val numberValue = FieldValue.NumberValue(-10.5)
    assertEquals(-10.5, numberValue.value, 0.0)
  }

  @Test
  fun numberValueAcceptsZero() {
    val numberValue = FieldValue.NumberValue(0.0)
    assertEquals(0.0, numberValue.value, 0.0)
  }

  @Test
  fun dateValueHasCorrectTypeKey() {
    val dateValue = FieldValue.DateValue("2024-01-15")
    assertEquals(FieldTypeKey.DATE, dateValue.typeKey)
  }

  @Test
  fun dateValueStoresValueCorrectly() {
    val dateValue = FieldValue.DateValue("2024-01-15T10:30:00")
    assertEquals("2024-01-15T10:30:00", dateValue.value)
  }

  @Test
  fun dateValueAcceptsEmptyString() {
    val dateValue = FieldValue.DateValue("")
    assertEquals("", dateValue.value)
  }

  @Test
  fun singleSelectValueHasCorrectTypeKey() {
    val singleSelectValue = FieldValue.SingleSelectValue("option1")
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelectValue.typeKey)
  }

  @Test
  fun singleSelectValueStoresValueCorrectly() {
    val singleSelectValue = FieldValue.SingleSelectValue("high")
    assertEquals("high", singleSelectValue.value)
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectValueRejectsBlankValue() {
    FieldValue.SingleSelectValue("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectValueRejectsWhitespaceOnlyValue() {
    FieldValue.SingleSelectValue("   ")
  }

  @Test
  fun multiSelectValueHasCorrectTypeKey() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1", "tag2"))
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelectValue.typeKey)
  }

  @Test
  fun multiSelectValueStoresValuesCorrectly() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag3"))
    assertEquals(3, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
    assertEquals("tag2", multiSelectValue.values[1])
    assertEquals("tag3", multiSelectValue.values[2])
  }

  @Test
  fun multiSelectValueAcceptsEmptyList() {
    val multiSelectValue = FieldValue.MultiSelectValue(emptyList())
    assertEquals(0, multiSelectValue.values.size)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectValueRejectsBlankValues() {
    FieldValue.MultiSelectValue(listOf("tag1", "", "tag3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectValueRejectsWhitespaceOnlyValues() {
    FieldValue.MultiSelectValue(listOf("tag1", "   ", "tag3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectValueRejectsDuplicateValues() {
    FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag1"))
  }

  @Test
  fun multiSelectValueWithSingleValue() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1"))
    assertEquals(1, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
  }
}
