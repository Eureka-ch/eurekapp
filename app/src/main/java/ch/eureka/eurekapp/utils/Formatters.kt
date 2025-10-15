package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.*

/** Represent a toolbox containing various formatters. */
object Formatters {
  /**
   * Produce formated representation of the given datetime as a string.
   *
   * @param dateTime The given datetime to format.
   * @return the formatted string representing [dateTime]
   */
  fun formatDateTime(dateTime: Date): String {
    val dayFormatter = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())

    val dayPart = dayFormatter.format(dateTime)
    val timePart = timeFormatter.format(dateTime)

    return "$dayPart · $timePart"
  }
}
