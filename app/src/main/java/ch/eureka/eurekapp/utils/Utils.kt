package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Utils {
  /**
   * @param millis a Unix timestamp to convert to a date
   * @return a date formatted in the dd/MM/yyyy pattern *
   */
  fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
  }

  fun stringIsEmptyOrBlank(stringToCheck: String): Boolean {
    return stringToCheck.isEmpty() || stringToCheck.isBlank()
  }

  private val dateFormatter = SimpleDateFormat("dd/MM/yyyy")

  fun isDateParseableToStandardAppPattern(stringToCheck: String): Boolean {
    try {
      dateFormatter.parse(stringToCheck)
      return true
    } catch (e: Exception) {
      return false
    }
  }
}
