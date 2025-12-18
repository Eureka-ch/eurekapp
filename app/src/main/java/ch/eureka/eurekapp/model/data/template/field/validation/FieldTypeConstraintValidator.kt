package ch.eureka.eurekapp.model.data.template.field.validation

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object FieldTypeConstraintValidator {

  fun validateMinMax(min: Double?, max: Double?): String? {
    if (min != null && max != null && min > max) {
      return "Max must be ≥ min"
    }
    return null
  }

  fun validateDateFormat(format: String?): String? {
    if (format.isNullOrBlank()) return null
    return try {
      DateTimeFormatter.ofPattern(format)
      null
    } catch (e: IllegalArgumentException) {
      "Invalid date format pattern"
    }
  }

  fun validateDateString(dateString: String?): String? {
    if (dateString.isNullOrBlank()) return null
    return try {
      LocalDate.parse(dateString)
      null
    } catch (e: DateTimeParseException) {
      "Invalid date (use YYYY-MM-DD)"
    }
  }

  fun validateDateRange(minDate: String?, maxDate: String?): String? {
    if (minDate.isNullOrBlank() || maxDate.isNullOrBlank()) return null
    return try {
      val min = LocalDate.parse(minDate)
      val max = LocalDate.parse(maxDate)
      if (min.isAfter(max)) "Max date must be ≥ min date" else null
    } catch (e: DateTimeParseException) {
      null // Parse errors handled by validateDateString
    }
  }

  fun validateStep(step: Double?): String? {
    if (step != null && step <= 0) {
      return "Step must be positive"
    }
    return null
  }

  fun validateDecimals(decimals: Int?): String? {
    if (decimals != null && decimals < 0) {
      return "Decimals must be non-negative"
    }
    return null
  }

  fun validateMaxLength(maxLength: Int?): String? {
    if (maxLength != null && maxLength <= 0) {
      return "Max length must be positive"
    }
    return null
  }

  fun validateMinLength(minLength: Int?): String? {
    if (minLength != null && minLength < 0) {
      return "Min length must be non-negative"
    }
    return null
  }

  fun validateTextLengthRange(minLength: Int?, maxLength: Int?): String? {
    if (minLength != null && maxLength != null && minLength > maxLength) {
      return "Max length must be ≥ min length"
    }
    return null
  }

  fun validateMinSelections(minSelections: Int?): String? {
    if (minSelections != null && minSelections < 0) {
      return "Min selections must be non-negative"
    }
    return null
  }

  fun validateMaxSelections(maxSelections: Int?): String? {
    if (maxSelections != null && maxSelections <= 0) {
      return "Max selections must be positive"
    }
    return null
  }

  fun validateSelectionsRange(minSelections: Int?, maxSelections: Int?): String? {
    if (minSelections != null && maxSelections != null && minSelections > maxSelections) {
      return "Max selections must be ≥ min selections"
    }
    return null
  }
}
