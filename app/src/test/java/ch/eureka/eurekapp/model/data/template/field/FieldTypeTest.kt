/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldTypeTest {

  @Test
  fun textTypeHasCorrectTypeKey() {
    val textType = FieldType.Text()
    assertEquals(FieldTypeKey.TEXT, textType.typeKey)
  }

  @Test
  fun textTypeAcceptsValidConstraints() {
    val textType = FieldType.Text(maxLength = 100, minLength = 10, placeholder = "Enter text")
    assertEquals(100, textType.maxLength)
    assertEquals(10, textType.minLength)
    assertEquals("Enter text", textType.placeholder)
  }

  @Test(expected = IllegalArgumentException::class)
  fun textTypeRejectsNegativeMaxLength() {
    FieldType.Text(maxLength = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun textTypeRejectsNegativeMinLength() {
    FieldType.Text(minLength = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun textTypeRejectsMaxLengthLessThanMinLength() {
    FieldType.Text(maxLength = 5, minLength = 10)
  }

  @Test
  fun numberTypeHasCorrectTypeKey() {
    val numberType = FieldType.Number()
    assertEquals(FieldTypeKey.NUMBER, numberType.typeKey)
  }

  @Test
  fun numberTypeAcceptsValidConstraints() {
    val numberType = FieldType.Number(min = 0.0, max = 100.0, step = 0.5, decimals = 2, unit = "kg")
    assertEquals(0.0, numberType.min!!, 0.0)
    assertEquals(100.0, numberType.max!!, 0.0)
    assertEquals(0.5, numberType.step!!, 0.0)
    assertEquals(2, numberType.decimals)
    assertEquals("kg", numberType.unit)
  }

  @Test(expected = IllegalArgumentException::class)
  fun numberTypeRejectsMaxLessThanMin() {
    FieldType.Number(min = 100.0, max = 50.0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun numberTypeRejectsNegativeStep() {
    FieldType.Number(step = -1.0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun numberTypeRejectsNegativeDecimals() {
    FieldType.Number(decimals = -1)
  }

  @Test
  fun dateTypeHasCorrectTypeKey() {
    val dateType = FieldType.Date()
    assertEquals(FieldTypeKey.DATE, dateType.typeKey)
  }

  @Test
  fun dateTypeAcceptsValidConstraints() {
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
  fun singleSelectTypeHasCorrectTypeKey() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val singleSelect = FieldType.SingleSelect(options)
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelect.typeKey)
  }

  @Test
  fun singleSelectTypeAcceptsValidOptions() {
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
  fun singleSelectTypeRejectsEmptyOptions() {
    FieldType.SingleSelect(emptyList())
  }

  @Test(expected = IllegalArgumentException::class)
  fun singleSelectTypeRejectsDuplicateOptionValues() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("low", "Also Low"))
    FieldType.SingleSelect(options)
  }

  @Test
  fun multiSelectTypeHasCorrectTypeKey() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val multiSelect = FieldType.MultiSelect(options)
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelect.typeKey)
  }

  @Test
  fun multiSelectTypeAcceptsValidConstraints() {
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
  fun multiSelectTypeRejectsEmptyOptions() {
    FieldType.MultiSelect(emptyList())
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectTypeRejectsDuplicateOptionValues() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag1", "Also Tag 1"))
    FieldType.MultiSelect(options)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectTypeRejectsNegativeMinSelections() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, minSelections = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectTypeRejectsZeroMaxSelections() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, maxSelections = 0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun multiSelectTypeRejectsMaxSelectionsLessThanMinSelections() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, minSelections = 5, maxSelections = 2)
  }
}

class SelectOptionTest {

  @Test
  fun selectOptionAcceptsValidData() {
    val option = SelectOption("value1", "Label 1", "Description")
    assertEquals("value1", option.value)
    assertEquals("Label 1", option.label)
    assertEquals("Description", option.description)
  }

  @Test
  fun selectOptionAcceptsNullDescription() {
    val option = SelectOption("value1", "Label 1")
    assertEquals("value1", option.value)
    assertEquals("Label 1", option.label)
    assertEquals(null, option.description)
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOptionRejectsBlankValue() {
    SelectOption("", "Label 1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOptionRejectsBlankLabel() {
    SelectOption("value1", "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOptionRejectsWhitespaceOnlyValue() {
    SelectOption("   ", "Label 1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun selectOptionRejectsWhitespaceOnlyLabel() {
    SelectOption("value1", "   ")
  }
}
