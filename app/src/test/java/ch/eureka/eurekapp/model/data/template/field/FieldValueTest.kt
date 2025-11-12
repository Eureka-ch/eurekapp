/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldValueTest {

  @Test
  fun `TextValue has correct typeKey`() {
    val textValue = FieldValue.TextValue("Hello")
    assertEquals(FieldTypeKey.TEXT, textValue.typeKey)
  }

  @Test
  fun `TextValue stores value correctly`() {
    val textValue = FieldValue.TextValue("Hello World")
    assertEquals("Hello World", textValue.value)
  }

  @Test
  fun `TextValue accepts empty string`() {
    val textValue = FieldValue.TextValue("")
    assertEquals("", textValue.value)
  }

  @Test
  fun `NumberValue has correct typeKey`() {
    val numberValue = FieldValue.NumberValue(42.0)
    assertEquals(FieldTypeKey.NUMBER, numberValue.typeKey)
  }

  @Test
  fun `NumberValue stores value correctly`() {
    val numberValue = FieldValue.NumberValue(123.456)
    assertEquals(123.456, numberValue.value, 0.0)
  }

  @Test
  fun `NumberValue accepts negative values`() {
    val numberValue = FieldValue.NumberValue(-10.5)
    assertEquals(-10.5, numberValue.value, 0.0)
  }

  @Test
  fun `NumberValue accepts zero`() {
    val numberValue = FieldValue.NumberValue(0.0)
    assertEquals(0.0, numberValue.value, 0.0)
  }

  @Test
  fun `DateValue has correct typeKey`() {
    val dateValue = FieldValue.DateValue("2024-01-15")
    assertEquals(FieldTypeKey.DATE, dateValue.typeKey)
  }

  @Test
  fun `DateValue stores value correctly`() {
    val dateValue = FieldValue.DateValue("2024-01-15T10:30:00")
    assertEquals("2024-01-15T10:30:00", dateValue.value)
  }

  @Test
  fun `DateValue accepts empty string`() {
    val dateValue = FieldValue.DateValue("")
    assertEquals("", dateValue.value)
  }

  @Test
  fun `SingleSelectValue has correct typeKey`() {
    val singleSelectValue = FieldValue.SingleSelectValue("option1")
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelectValue.typeKey)
  }

  @Test
  fun `SingleSelectValue stores value correctly`() {
    val singleSelectValue = FieldValue.SingleSelectValue("high")
    assertEquals("high", singleSelectValue.value)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SingleSelectValue rejects blank value`() {
    FieldValue.SingleSelectValue("")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SingleSelectValue rejects whitespace-only value`() {
    FieldValue.SingleSelectValue("   ")
  }

  @Test
  fun `MultiSelectValue has correct typeKey`() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1", "tag2"))
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelectValue.typeKey)
  }

  @Test
  fun `MultiSelectValue stores values correctly`() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag3"))
    assertEquals(3, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
    assertEquals("tag2", multiSelectValue.values[1])
    assertEquals("tag3", multiSelectValue.values[2])
  }

  @Test
  fun `MultiSelectValue accepts empty list`() {
    val multiSelectValue = FieldValue.MultiSelectValue(emptyList())
    assertEquals(0, multiSelectValue.values.size)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelectValue rejects blank values`() {
    FieldValue.MultiSelectValue(listOf("tag1", "", "tag3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelectValue rejects whitespace-only values`() {
    FieldValue.MultiSelectValue(listOf("tag1", "   ", "tag3"))
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelectValue rejects duplicate values`() {
    FieldValue.MultiSelectValue(listOf("tag1", "tag2", "tag1"))
  }

  @Test
  fun `MultiSelectValue with single value`() {
    val multiSelectValue = FieldValue.MultiSelectValue(listOf("tag1"))
    assertEquals(1, multiSelectValue.values.size)
    assertEquals("tag1", multiSelectValue.values[0])
  }
}
