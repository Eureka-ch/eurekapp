package ch.eureka.eurekapp.model.data.template.field.validation

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FieldTypeConstraintValidatorTest {

  // validateDateFormat tests

  @Test
  fun validateDateFormat_returnsNullForValidFormat() {
    assertNull(FieldTypeConstraintValidator.validateDateFormat("yyyy-MM-dd"))
  }

  @Test
  fun validateDateFormat_returnsNullForNullFormat() {
    assertNull(FieldTypeConstraintValidator.validateDateFormat(null))
  }

  @Test
  fun validateDateFormat_returnsNullForBlankFormat() {
    assertNull(FieldTypeConstraintValidator.validateDateFormat("  "))
  }

  @Test
  fun validateDateFormat_returnsErrorForInvalidFormat() {
    val error = FieldTypeConstraintValidator.validateDateFormat("invalid-format-xyz")
    assertNotNull(error)
  }

  // validateDateString tests

  @Test
  fun validateDateString_returnsNullForValidDate() {
    assertNull(FieldTypeConstraintValidator.validateDateString("2025-01-15"))
  }

  @Test
  fun validateDateString_returnsNullForNullDate() {
    assertNull(FieldTypeConstraintValidator.validateDateString(null))
  }

  @Test
  fun validateDateString_returnsNullForBlankDate() {
    assertNull(FieldTypeConstraintValidator.validateDateString("  "))
  }

  @Test
  fun validateDateString_returnsErrorForInvalidDate() {
    val error = FieldTypeConstraintValidator.validateDateString("not-a-date")
    assertNotNull(error)
  }

  @Test
  fun validateDateString_returnsErrorForWrongFormat() {
    val error = FieldTypeConstraintValidator.validateDateString("15-01-2025")
    assertNotNull(error)
  }

  // validateDateRange tests

  @Test
  fun validateDateRange_returnsNullForValidRange() {
    assertNull(FieldTypeConstraintValidator.validateDateRange("2025-01-01", "2025-12-31"))
  }

  @Test
  fun validateDateRange_returnsNullForEqualDates() {
    assertNull(FieldTypeConstraintValidator.validateDateRange("2025-06-15", "2025-06-15"))
  }

  @Test
  fun validateDateRange_returnsNullWhenMinIsNull() {
    assertNull(FieldTypeConstraintValidator.validateDateRange(null, "2025-12-31"))
  }

  @Test
  fun validateDateRange_returnsNullWhenMaxIsNull() {
    assertNull(FieldTypeConstraintValidator.validateDateRange("2025-01-01", null))
  }

  @Test
  fun validateDateRange_returnsNullWhenBothNull() {
    assertNull(FieldTypeConstraintValidator.validateDateRange(null, null))
  }

  @Test
  fun validateDateRange_returnsErrorWhenMinAfterMax() {
    val error = FieldTypeConstraintValidator.validateDateRange("2025-12-31", "2025-01-01")
    assertNotNull(error)
  }

  @Test
  fun validateDateRange_returnsNullWhenDatesInvalid() {
    assertNull(FieldTypeConstraintValidator.validateDateRange("invalid", "2025-01-01"))
  }

  // validateMinMax tests

  @Test
  fun validateMinMax_returnsNullForValidRange() {
    assertNull(FieldTypeConstraintValidator.validateMinMax(0.0, 100.0))
  }

  @Test
  fun validateMinMax_returnsNullForEqualValues() {
    assertNull(FieldTypeConstraintValidator.validateMinMax(50.0, 50.0))
  }

  @Test
  fun validateMinMax_returnsNullWhenMinIsNull() {
    assertNull(FieldTypeConstraintValidator.validateMinMax(null, 100.0))
  }

  @Test
  fun validateMinMax_returnsNullWhenMaxIsNull() {
    assertNull(FieldTypeConstraintValidator.validateMinMax(0.0, null))
  }

  @Test
  fun validateMinMax_returnsNullWhenBothNull() {
    assertNull(FieldTypeConstraintValidator.validateMinMax(null, null))
  }

  @Test
  fun validateMinMax_returnsErrorWhenMinGreaterThanMax() {
    val error = FieldTypeConstraintValidator.validateMinMax(100.0, 50.0)
    assertNotNull(error)
  }

  // validateStep tests

  @Test
  fun validateStep_returnsNullForPositiveStep() {
    assertNull(FieldTypeConstraintValidator.validateStep(0.5))
  }

  @Test
  fun validateStep_returnsNullForNullStep() {
    assertNull(FieldTypeConstraintValidator.validateStep(null))
  }

  @Test
  fun validateStep_returnsErrorForZeroStep() {
    val error = FieldTypeConstraintValidator.validateStep(0.0)
    assertNotNull(error)
  }

  @Test
  fun validateStep_returnsErrorForNegativeStep() {
    val error = FieldTypeConstraintValidator.validateStep(-1.0)
    assertNotNull(error)
  }

  // validateDecimals tests

  @Test
  fun validateDecimals_returnsNullForPositiveDecimals() {
    assertNull(FieldTypeConstraintValidator.validateDecimals(2))
  }

  @Test
  fun validateDecimals_returnsNullForZeroDecimals() {
    assertNull(FieldTypeConstraintValidator.validateDecimals(0))
  }

  @Test
  fun validateDecimals_returnsNullForNullDecimals() {
    assertNull(FieldTypeConstraintValidator.validateDecimals(null))
  }

  @Test
  fun validateDecimals_returnsErrorForNegativeDecimals() {
    val error = FieldTypeConstraintValidator.validateDecimals(-1)
    assertNotNull(error)
  }

  // validateMaxLength tests

  @Test
  fun validateMaxLength_returnsNullForPositiveMaxLength() {
    assertNull(FieldTypeConstraintValidator.validateMaxLength(100))
  }

  @Test
  fun validateMaxLength_returnsNullForNullMaxLength() {
    assertNull(FieldTypeConstraintValidator.validateMaxLength(null))
  }

  @Test
  fun validateMaxLength_returnsErrorForZeroMaxLength() {
    val error = FieldTypeConstraintValidator.validateMaxLength(0)
    assertNotNull(error)
  }

  @Test
  fun validateMaxLength_returnsErrorForNegativeMaxLength() {
    val error = FieldTypeConstraintValidator.validateMaxLength(-1)
    assertNotNull(error)
  }

  // validateMinLength tests

  @Test
  fun validateMinLength_returnsNullForPositiveMinLength() {
    assertNull(FieldTypeConstraintValidator.validateMinLength(10))
  }

  @Test
  fun validateMinLength_returnsNullForZeroMinLength() {
    assertNull(FieldTypeConstraintValidator.validateMinLength(0))
  }

  @Test
  fun validateMinLength_returnsNullForNullMinLength() {
    assertNull(FieldTypeConstraintValidator.validateMinLength(null))
  }

  @Test
  fun validateMinLength_returnsErrorForNegativeMinLength() {
    val error = FieldTypeConstraintValidator.validateMinLength(-1)
    assertNotNull(error)
  }

  // validateTextLengthRange tests

  @Test
  fun validateTextLengthRange_returnsNullForValidRange() {
    assertNull(FieldTypeConstraintValidator.validateTextLengthRange(5, 100))
  }

  @Test
  fun validateTextLengthRange_returnsNullForEqualValues() {
    assertNull(FieldTypeConstraintValidator.validateTextLengthRange(50, 50))
  }

  @Test
  fun validateTextLengthRange_returnsNullWhenMinIsNull() {
    assertNull(FieldTypeConstraintValidator.validateTextLengthRange(null, 100))
  }

  @Test
  fun validateTextLengthRange_returnsNullWhenMaxIsNull() {
    assertNull(FieldTypeConstraintValidator.validateTextLengthRange(5, null))
  }

  @Test
  fun validateTextLengthRange_returnsNullWhenBothNull() {
    assertNull(FieldTypeConstraintValidator.validateTextLengthRange(null, null))
  }

  @Test
  fun validateTextLengthRange_returnsErrorWhenMinGreaterThanMax() {
    val error = FieldTypeConstraintValidator.validateTextLengthRange(100, 50)
    assertNotNull(error)
  }

  // validateMinSelections tests

  @Test
  fun validateMinSelections_returnsNullForPositiveMinSelections() {
    assertNull(FieldTypeConstraintValidator.validateMinSelections(2))
  }

  @Test
  fun validateMinSelections_returnsNullForZeroMinSelections() {
    assertNull(FieldTypeConstraintValidator.validateMinSelections(0))
  }

  @Test
  fun validateMinSelections_returnsNullForNullMinSelections() {
    assertNull(FieldTypeConstraintValidator.validateMinSelections(null))
  }

  @Test
  fun validateMinSelections_returnsErrorForNegativeMinSelections() {
    val error = FieldTypeConstraintValidator.validateMinSelections(-1)
    assertNotNull(error)
  }

  // validateMaxSelections tests

  @Test
  fun validateMaxSelections_returnsNullForPositiveMaxSelections() {
    assertNull(FieldTypeConstraintValidator.validateMaxSelections(5))
  }

  @Test
  fun validateMaxSelections_returnsNullForNullMaxSelections() {
    assertNull(FieldTypeConstraintValidator.validateMaxSelections(null))
  }

  @Test
  fun validateMaxSelections_returnsErrorForZeroMaxSelections() {
    val error = FieldTypeConstraintValidator.validateMaxSelections(0)
    assertNotNull(error)
  }

  @Test
  fun validateMaxSelections_returnsErrorForNegativeMaxSelections() {
    val error = FieldTypeConstraintValidator.validateMaxSelections(-1)
    assertNotNull(error)
  }

  // validateSelectionsRange tests

  @Test
  fun validateSelectionsRange_returnsNullForValidRange() {
    assertNull(FieldTypeConstraintValidator.validateSelectionsRange(1, 5))
  }

  @Test
  fun validateSelectionsRange_returnsNullForEqualValues() {
    assertNull(FieldTypeConstraintValidator.validateSelectionsRange(3, 3))
  }

  @Test
  fun validateSelectionsRange_returnsNullWhenMinIsNull() {
    assertNull(FieldTypeConstraintValidator.validateSelectionsRange(null, 5))
  }

  @Test
  fun validateSelectionsRange_returnsNullWhenMaxIsNull() {
    assertNull(FieldTypeConstraintValidator.validateSelectionsRange(1, null))
  }

  @Test
  fun validateSelectionsRange_returnsNullWhenBothNull() {
    assertNull(FieldTypeConstraintValidator.validateSelectionsRange(null, null))
  }

  @Test
  fun validateSelectionsRange_returnsErrorWhenMinGreaterThanMax() {
    val error = FieldTypeConstraintValidator.validateSelectionsRange(10, 5)
    assertNotNull(error)
  }
}
