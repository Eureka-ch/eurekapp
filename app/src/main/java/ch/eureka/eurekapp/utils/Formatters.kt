package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.*

/*
Co-author: GPT-5 Codex
*/

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

  /**
   * Regex pattern for validating time format in HH:mm (24-hour format) Validates hours from 00-23
   * and minutes from 00-59
   */
  val timeRegex = Regex("^([0-1]?[0-9]|2[0-3]):[0-5][0-9]$")

  /**
   * Format a date as a relative time string (e.g., "2m", "1h", "Yesterday", "Mon").
   *
   * @param date The date to format.
   * @return A short relative time string.
   */
  fun formatRelativeTime(date: Date): String {
    val now = System.currentTimeMillis()
    val diff = now - date.time
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
      minutes < 1 -> "now"
      minutes < 60 -> "${minutes}m"
      hours < 24 -> "${hours}h"
      days < 2 -> "Yesterday"
      days < 7 -> SimpleDateFormat("EEE", Locale.getDefault()).format(date)
      else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
  }
}
