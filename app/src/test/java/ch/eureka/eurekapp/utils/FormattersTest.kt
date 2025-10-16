package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.*
import junit.framework.TestCase.*
import org.junit.Test

/**
 * Test suite for Formatters utility object.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class FormattersTest {

  @Test
  fun testFormatDateTime() {
    // Create a fixed date
    val calendar = Calendar.getInstance()
    calendar.set(2025, Calendar.OCTOBER, 15, 14, 30, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    val date = calendar.time

    // Call the formatter
    val formatted = Formatters.formatDateTime(date)

    // Assert non-null
    assertNotNull(formatted)

    // Assert pattern roughly matches "EEE, MMM d · h:mm a"
    val regex = "\\w{3}, \\w{3} \\d{1,2} · \\d{1,2}:\\d{2} [AP]M".toRegex()
    assertTrue(regex.matches(formatted))

    // Optional: check exact values for reproducibility
    val expectedDay = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(date)
    val expectedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    assertEquals("$expectedDay · $expectedTime", formatted)
  }
}
