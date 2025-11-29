package ch.eureka.eurekapp.utils

import java.text.SimpleDateFormat
import java.util.*
import junit.framework.TestCase.*
import org.junit.Test

/**
 * Test suite for Formatters utility object.
 *
 * Note: Some of these tests were co-authored by chatGPT. Co-author: Claude 4.5 Sonnet
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

  /**
   * Verifies that times within the last minute display as "now". This provides a friendly indicator
   * for very recent messages.
   */
  @Test
  fun testFormatRelativeTime_now() {
    // Arrange: Date 30 seconds ago (within 1 minute threshold)
    val date = Date(System.currentTimeMillis() - 30 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should display "now"
    assertEquals("now", formatted)
  }

  /**
   * Verifies that times within the last hour display as minutes. Format: "Xm" where X is the number
   * of minutes.
   */
  @Test
  fun testFormatRelativeTime_minutes() {
    // Arrange: Date 5 minutes ago
    val date = Date(System.currentTimeMillis() - 5 * 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should display "5m"
    assertEquals("5m", formatted)
  }

  /**
   * Verifies that times within the last day display as hours. Format: "Xh" where X is the number of
   * hours.
   */
  @Test
  fun testFormatRelativeTime_hours() {
    // Arrange: Date 3 hours ago
    val date = Date(System.currentTimeMillis() - 3 * 60 * 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should display "3h"
    assertEquals("3h", formatted)
  }

  /**
   * Verifies that times between 1-2 days ago display as "Yesterday". This provides a natural
   * language indicator for recent past days.
   */
  @Test
  fun testFormatRelativeTime_yesterday() {
    // Arrange: Date 1.5 days ago (36 hours, within 2-day threshold)
    val date = Date(System.currentTimeMillis() - 36 * 60 * 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should display "Yesterday"
    assertEquals("Yesterday", formatted)
  }

  /**
   * Verifies that times within the past week display as day names. Format: Abbreviated day name
   * like "Mon", "Tue", "Wed", etc.
   */
  @Test
  fun testFormatRelativeTime_withinWeek() {
    // Arrange: Date 4 days ago
    val date = Date(System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should return abbreviated day name (e.g., "Mon", "Tue")
    val expectedDayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date)
    assertEquals(expectedDayName, formatted)
  }

  /**
   * Verifies that times older than a week display as month and day. Format: "MMM d" like "Nov 18".
   */
  @Test
  fun testFormatRelativeTime_olderThanWeek() {
    // Arrange: Date 10 days ago
    val date = Date(System.currentTimeMillis() - 10 * 24 * 60 * 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should return "MMM d" format (e.g., "Nov 18")
    val expectedFormat = SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    assertEquals(expectedFormat, formatted)
  }

  /**
   * Verifies the boundary case of exactly 1 minute ago. Should display "1m" not "now" (which is for
   * under 1 minute).
   */
  @Test
  fun testFormatRelativeTime_exactlyOneMinute() {
    // Arrange: Date exactly 1 minute ago
    val date = Date(System.currentTimeMillis() - 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should display "1m"
    assertEquals("1m", formatted)
  }

  /** Verifies the boundary case of exactly 1 hour ago. Should display "1h" not minutes. */
  @Test
  fun testFormatRelativeTime_exactlyOneHour() {
    // Arrange: Date exactly 1 hour ago
    val date = Date(System.currentTimeMillis() - 60 * 60 * 1000)

    // Act: Format the date
    val formatted = Formatters.formatRelativeTime(date)

    // Assert: Should display "1h"
    assertEquals("1h", formatted)
  }
}
