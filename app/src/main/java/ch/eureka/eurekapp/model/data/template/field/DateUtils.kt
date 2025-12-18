package ch.eureka.eurekapp.model.data.template.field

object DateUtils {
  private val DATE_REGEX = Regex("""\d{2}-\d{2}-\d{4}""")

  fun isValidDateFormat(date: String): Boolean {
    if (date.isBlank()) return true
    return DATE_REGEX.matches(date)
  }

  fun compareDates(date1: String, date2: String): Int {
    if (!DATE_REGEX.matches(date1) || !DATE_REGEX.matches(date2)) return 0
    val parts1 = date1.split("-")
    val parts2 = date2.split("-")
    val (d1, m1, y1) = parts1.map { it.toIntOrNull() ?: 0 }
    val (d2, m2, y2) = parts2.map { it.toIntOrNull() ?: 0 }
    // Compare year first, then month, then day
    return when {
      y1 != y2 -> y1.compareTo(y2)
      m1 != m2 -> m1.compareTo(m2)
      else -> d1.compareTo(d2)
    }
  }
}
