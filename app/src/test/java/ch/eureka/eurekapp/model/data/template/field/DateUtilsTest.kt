package ch.eureka.eurekapp.model.data.template.field

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {

  @Test
  fun isValidDateFormat_returnsTrueForValidDate() {
    assertTrue(DateUtils.isValidDateFormat("01-01-2024"))
    assertTrue(DateUtils.isValidDateFormat("31-12-2023"))
    assertTrue(DateUtils.isValidDateFormat("15-06-2025"))
  }

  @Test
  fun isValidDateFormat_returnsTrueForBlankDate() {
    assertTrue(DateUtils.isValidDateFormat(""))
    assertTrue(DateUtils.isValidDateFormat("   "))
  }

  @Test
  fun isValidDateFormat_returnsFalseForInvalidFormat() {
    assertFalse(DateUtils.isValidDateFormat("2024-01-01"))
    assertFalse(DateUtils.isValidDateFormat("1-1-2024"))
    assertFalse(DateUtils.isValidDateFormat("01/01/2024"))
    assertFalse(DateUtils.isValidDateFormat("invalid"))
  }

  @Test
  fun compareDates_returnsNegativeWhenFirstDateIsBefore() {
    assertTrue(DateUtils.compareDates("01-01-2024", "02-01-2024") < 0)
    assertTrue(DateUtils.compareDates("31-12-2023", "01-01-2024") < 0)
    assertTrue(DateUtils.compareDates("15-06-2024", "15-07-2024") < 0)
  }

  @Test
  fun compareDates_returnsPositiveWhenFirstDateIsAfter() {
    assertTrue(DateUtils.compareDates("02-01-2024", "01-01-2024") > 0)
    assertTrue(DateUtils.compareDates("01-01-2024", "31-12-2023") > 0)
    assertTrue(DateUtils.compareDates("15-07-2024", "15-06-2024") > 0)
  }

  @Test
  fun compareDates_returnsZeroForSameDates() {
    assertEquals(0, DateUtils.compareDates("01-01-2024", "01-01-2024"))
    assertEquals(0, DateUtils.compareDates("15-06-2025", "15-06-2025"))
  }

  @Test
  fun compareDates_returnsZeroForInvalidFormat() {
    assertEquals(0, DateUtils.compareDates("invalid", "01-01-2024"))
    assertEquals(0, DateUtils.compareDates("01-01-2024", "invalid"))
    assertEquals(0, DateUtils.compareDates("2024-01-01", "01-01-2024"))
  }
}
