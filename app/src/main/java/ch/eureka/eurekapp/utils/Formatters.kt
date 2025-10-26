package ch.eureka.eurekapp.utils

import android.annotation.SuppressLint
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

  /**
   * @param millis a Unix timestamp to convert to a date
   * @return a date formatted in the dd/MM/yyyy pattern *
   */
  fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
  }

  /**
   * @param seconds seconds to format to mm:ss
   * @return a string which formats seconds in mm:ss
   */
  fun formatTime(seconds: Long): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%${if(minutes > 100) "03" else "02"}d:%02d", minutes, secs)
  }
}
