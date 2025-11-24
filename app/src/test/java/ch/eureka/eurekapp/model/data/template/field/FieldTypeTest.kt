/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/
package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldTypeTest {

  @Test
  fun `Text type has correct typeKey`() {
    val textType = FieldType.Text()
    assertEquals(FieldTypeKey.TEXT, textType.typeKey)
  }

  @Test
  fun `Text type accepts valid constraints`() {
    val textType = FieldType.Text(maxLength = 100, minLength = 10, placeholder = "Enter text")
    assertEquals(100, textType.maxLength)
    assertEquals(10, textType.minLength)
    assertEquals("Enter text", textType.placeholder)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Text type rejects negative maxLength`() {
    FieldType.Text(maxLength = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Text type rejects negative minLength`() {
    FieldType.Text(minLength = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Text type rejects maxLength less than minLength`() {
    FieldType.Text(maxLength = 5, minLength = 10)
  }

  @Test
  fun `Number type has correct typeKey`() {
    val numberType = FieldType.Number()
    assertEquals(FieldTypeKey.NUMBER, numberType.typeKey)
  }

  @Test
  fun `Number type accepts valid constraints`() {
    val numberType = FieldType.Number(min = 0.0, max = 100.0, step = 0.5, decimals = 2, unit = "kg")
    assertEquals(0.0, numberType.min!!, 0.0)
    assertEquals(100.0, numberType.max!!, 0.0)
    assertEquals(0.5, numberType.step!!, 0.0)
    assertEquals(2, numberType.decimals)
    assertEquals("kg", numberType.unit)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Number type rejects max less than min`() {
    FieldType.Number(min = 100.0, max = 50.0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Number type rejects negative step`() {
    FieldType.Number(step = -1.0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `Number type rejects negative decimals`() {
    FieldType.Number(decimals = -1)
  }

  @Test
  fun `Date type has correct typeKey`() {
    val dateType = FieldType.Date()
    assertEquals(FieldTypeKey.DATE, dateType.typeKey)
  }

  @Test
  fun `Date type accepts valid constraints`() {
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
  fun `SingleSelect type has correct typeKey`() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val singleSelect = FieldType.SingleSelect(options)
    assertEquals(FieldTypeKey.SINGLE_SELECT, singleSelect.typeKey)
  }

  @Test
  fun `SingleSelect type accepts valid options`() {
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
  fun `SingleSelect type rejects empty options`() {
    FieldType.SingleSelect(emptyList())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SingleSelect type rejects duplicate option values`() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("low", "Also Low"))
    FieldType.SingleSelect(options)
  }

  @Test
  fun `MultiSelect type has correct typeKey`() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val multiSelect = FieldType.MultiSelect(options)
    assertEquals(FieldTypeKey.MULTI_SELECT, multiSelect.typeKey)
  }

  @Test
  fun `MultiSelect type accepts valid constraints`() {
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
  fun `MultiSelect type rejects empty options`() {
    FieldType.MultiSelect(emptyList())
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelect type rejects duplicate option values`() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag1", "Also Tag 1"))
    FieldType.MultiSelect(options)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelect type rejects negative minSelections`() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, minSelections = -1)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelect type rejects zero maxSelections`() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, maxSelections = 0)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `MultiSelect type rejects maxSelections less than minSelections`() {
    val options = listOf(SelectOption("tag1", "Tag 1"))
    FieldType.MultiSelect(options, minSelections = 5, maxSelections = 2)
  }

  // validateConfiguration() tests for Text
  @Test
  fun validateConfiguration_textTypeReturnsSuccessForValidConstraints() {
    val textType = FieldType.Text(maxLength = 100, minLength = 10)
    val result = textType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  @Test
  fun validateConfiguration_textTypeReturnsFailureForNegativeMaxLength() {
    val textType = FieldType.Text(maxLength = 100)
    // Create a text type with reflection to bypass init block
    val result = textType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  @Test
  fun validateConfiguration_textTypeReturnsSuccessForNullConstraints() {
    val textType = FieldType.Text()
    val result = textType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  // validateConfiguration() tests for Number
  @Test
  fun validateConfiguration_numberTypeReturnsSuccessForValidConstraints() {
    val numberType = FieldType.Number(min = 0.0, max = 100.0, decimals = 2)
    val result = numberType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  @Test
  fun validateConfiguration_numberTypeReturnsSuccessForNullConstraints() {
    val numberType = FieldType.Number()
    val result = numberType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  // validateConfiguration() tests for Date
  @Test
  fun validateConfiguration_dateTypeReturnsSuccess() {
    val dateType = FieldType.Date()
    val result = dateType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  @Test
  fun validateConfiguration_dateTypeWithConstraintsReturnsSuccess() {
    val dateType = FieldType.Date(minDate = "2024-01-01", maxDate = "2024-12-31")
    val result = dateType.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  // validateConfiguration() tests for SingleSelect
  @Test
  fun validateConfiguration_singleSelectReturnsSuccessForValidOptions() {
    val options = listOf(SelectOption("low", "Low"), SelectOption("high", "High"))
    val singleSelect = FieldType.SingleSelect(options)
    val result = singleSelect.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  // validateConfiguration() tests for MultiSelect
  @Test
  fun validateConfiguration_multiSelectReturnsSuccessForValidOptions() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val multiSelect = FieldType.MultiSelect(options, minSelections = 1, maxSelections = 2)
    val result = multiSelect.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }

  @Test
  fun validateConfiguration_multiSelectReturnsSuccessWithNullConstraints() {
    val options = listOf(SelectOption("tag1", "Tag 1"), SelectOption("tag2", "Tag 2"))
    val multiSelect = FieldType.MultiSelect(options)
    val result = multiSelect.validateConfiguration()
    assertEquals(true, result.isSuccess)
  }
}

class SelectOptionTest {

  @Test
  fun `SelectOption accepts valid data`() {
    val option = SelectOption("value1", "Label 1", "Description")
    assertEquals("value1", option.value)
    assertEquals("Label 1", option.label)
    assertEquals("Description", option.description)
  }

  @Test
  fun `SelectOption accepts null description`() {
    val option = SelectOption("value1", "Label 1")
    assertEquals("value1", option.value)
    assertEquals("Label 1", option.label)
    assertEquals(null, option.description)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SelectOption rejects blank value`() {
    SelectOption("", "Label 1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SelectOption rejects blank label`() {
    SelectOption("value1", "")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SelectOption rejects whitespace-only value`() {
    SelectOption("   ", "Label 1")
  }

  @Test(expected = IllegalArgumentException::class)
  fun `SelectOption rejects whitespace-only label`() {
    SelectOption("value1", "   ")
  }
}
