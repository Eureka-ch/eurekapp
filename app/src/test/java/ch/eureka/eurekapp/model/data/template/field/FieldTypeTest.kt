/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldTypeTest {

  @Test
  fun textType_hasCorrectTypeKey() {
    val textType = FieldType.Text()
    assertEquals(FieldTypeKey.TEXT, textType.typeKey)
  }

  @Test
  fun textType_acceptsValidConstraints() {
    val textType = FieldType.Text(maxLength = 100, minLength = 10, placeholder = "Enter text")
    assertEquals(100, textType.maxLength)
    assertEquals(10, textType.minLength)
    assertEquals("Enter text", textType.placeholder)
  }

  @Test(expected = IllegalArgumentException::class)
  fun textType_rejectsNegativeMaxLength() {
    FieldType.Text(maxLength = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun textType_rejectsNegativeMinLength() {
    FieldType.Text(minLength = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun textType_rejectsMaxLengthLessThanMinLength() {
    FieldType.Text(maxLength = 5, minLength = 10)
  }

  @Test
  fun numberType_hasCorrectTypeKey() {
    val numberType = FieldType.Number()
    assertEquals(FieldTypeKey.NUMBER, numberType.typeKey)
  }

  @Test
  fun numberType_acceptsValidConstraints() {
    val numberType = FieldType.Number(min = 0.0, max = 100.0, step = 0.5, decimals = 2, unit = "kg")
    assertEquals(0.0, numberType.min!!, 0.0)
    assertEquals(100.0, numberType.max!!, 0.0)
    assertEquals(0.5, numberType.step!!, 0.0)
    assertEquals(2, numberType.decimals)
    assertEquals("kg", numberType.unit)
  }

  @Test(expected = IllegalArgumentException::class)
  fun numberType_rejectsMaxLessThanMin() {
    FieldType.Number(min = 100.0, max = 50.0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun numberType_rejectsNegativeStep() {
    FieldType.Number(step = -1.0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun numberType_rejectsNegativeDecimals() {
    FieldType.Number(decimals = -1)
  }

  @Test
  fun dateType_hasCorrectTypeKey() {
    val dateType = FieldType.Date()
    assertEquals(FieldTypeKey.DATE, dateType.typeKey)
  }

  @Test
  fun dateType_acceptsValidConstraints() {
    val dateType =
        FieldType.Date(
            minDate = "2024-01-01",
            maxDate = "2024-12-31",
            includeTime = true,
            format = "yyyy-MM-dd")
    assertEquals("2024-01-01", dateType.minDate)
    assertEquals("2024-12-31", dateType.maxDate)
    assertEquals(true, dateType.includeTime)
    assertEquals("yyyy-MM-dd", dateType.format)
  }

  @Test
  fun singleSelectType_hasCorrectTypeKey() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val singleSelect = FieldType.SingleSelect(options)
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelect.typeKey)
  }

  @Test
  fun singleSelectType_acceptsValidOptions() {
    val options =
        listOf(
            SelectOption("low", "Low", "Low priority"),
            SelectOption("medium", "Medium"),
            SelectOption("high", "High"))
    val singleSelect = FieldType.SingleSelect(options, allowCustom = true)
    assertEquals(3, singleSelect.options.size)
    assertEquals(true, singleSelect.allowCustom)
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectType_rejectsEmptyOptions() {
    FieldType.SingleSelect(emptyList())
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectType_rejectsDuplicateOptionValues() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("low", "Also Low"))
    FieldType.SingleSelect(options)
  }

  @Test
  fun multiSelectType_hasCorrectTypeKey() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val multiSelect = FieldType.MultiSelect(options)
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelect.typeKey)
  }

  @Test
  fun multiSelectType_acceptsValidConstraints() {
    val options =
        listOf(
            SelectOption("tag1", "Tag 1"),
            SelectOption("tag2", "Tag 2"),
            SelectOption("tag3", "Tag 3"))
    val multiSelect =
        FieldType.MultiSelect(options, minSelections = 1, maxSelections = 2, allowCustom = true)
    assertEquals(3, multiSelect.options.size)
    assertEquals(1, multiSelect.minSelections)
    assertEquals(2, multiSelect.maxSelections)
    assertEquals(true, multiSelect.allowCustom)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectType_rejectsEmptyOptions() {
    FieldType.MultiSelect(emptyList())
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectType_rejectsDuplicateOptionValues() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag1", "Also Tag 1"))
    FieldType.MultiSelect(options)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectType_rejectsNegativeMinSelections() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, minSelections = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectType_rejectsZeroMaxSelections() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, maxSelections = 0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectType_rejectsMaxSelectionsLessThanMinSelections() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, minSelections = 5, maxSelections = 2)
  }
}

class SelectOptionTest {

  @Test
  fun selectOption_acceptsValidData() {
    val option = SelectOption("value1", "Label 1", "Description")
    assertEquals("value1", option.value)
    assertEquals("Label 1", option.label)
    assertEquals("Description", option.description)
  }

  @Test
  fun selectOption_acceptsNullDescription() {
    val option = SelectOption("value1", "Label 1")
    assertEquals("value1", option.value)
    assertEquals("Label 1", option.label)
    assertEquals(null, option.description)
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOption_rejectsBlankValue() {
    SelectOption("", "Label 1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOption_rejectsBlankLabel() {
    SelectOption("value1", "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOption_rejectsWhitespaceOnlyValue() {
    SelectOption("   ", "Label 1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOption_rejectsWhitespaceOnlyLabel() {
    SelectOption("value1", "   ")
  }
}
