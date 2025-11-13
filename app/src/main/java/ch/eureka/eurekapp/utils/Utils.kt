package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.Locale

object Utils {
  /**
   * checks if string is empty or blank
   *
   * @param stringToCheck the string to check *
   */
  fun stringIsEmptyOrBlank(stringToCheck: String): Boolean {
    return stringToCheck.isEmpty() || stringToCheck.isBlank()
  }

  private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)

  /**
   * checks if the date is parseable in the dd/MM/yyyy format
   *
   * @param stringToCheck the string to check *
   */
  fun isDateParseableToStandardAppPattern(stringToCheck: String): Boolean {
    try {
      dateFormatter.parse(stringToCheck)
      return true
    } catch (e: Exception) {
      return false
    }
  }
}
