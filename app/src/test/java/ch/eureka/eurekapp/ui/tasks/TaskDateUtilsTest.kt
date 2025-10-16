/**
 * Unit tests for TaskDateUtils utility class
 *
 * Tests basic date utility functions including overdue detection, week calculations, and day
 * counting.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.utils.TaskDateUtils
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDateUtilsTest {

  @Test
  fun `isDueThisWeek returns false for null due date`() {
    val result = TaskDateUtils.isDueThisWeek(null)
    assertFalse(result)
  }

  @Test
  fun `isDueThisWeek returns true for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val result = TaskDateUtils.isDueThisWeek(timestamp)
    assertTrue(result)
  }

  @Test
  fun `isDueThisWeek returns true for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val result = TaskDateUtils.isDueThisWeek(timestamp)
    assertTrue(result)
  }

  @Test
  fun `isDueThisWeek returns true for task due in 7 days`() {
    val sevenDaysFromNow = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(sevenDaysFromNow)
    val result = TaskDateUtils.isDueThisWeek(timestamp)
    assertTrue(result)
  }

  @Test
  fun `isDueThisWeek returns false for task due in 8 days`() {
    val eightDaysFromNow = Date(System.currentTimeMillis() + 8 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(eightDaysFromNow)
    val result = TaskDateUtils.isDueThisWeek(timestamp)
    assertFalse(result)
  }

  @Test
  fun `isDueThisWeek returns false for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val result = TaskDateUtils.isDueThisWeek(timestamp)
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns false for null due date`() {
    val result = TaskDateUtils.isOverdue(null)
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns true for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val result = TaskDateUtils.isOverdue(timestamp)
    assertTrue(result)
  }

  @Test
  fun `isOverdue returns false for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val result = TaskDateUtils.isOverdue(timestamp)
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns false for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val result = TaskDateUtils.isOverdue(timestamp)
    assertFalse(result)
  }

  @Test
  fun `isOverdue returns false for task due in future`() {
    val futureDate = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(futureDate)
    val result = TaskDateUtils.isOverdue(timestamp)
    assertFalse(result)
  }

  @Test
  fun `getDaysUntilDue returns Long MAX_VALUE for null due date`() {
    val result = TaskDateUtils.getDaysUntilDue(null)
    assertTrue(result == Long.MAX_VALUE)
  }

  @Test
  fun `getDaysUntilDue returns 0 for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    val result = TaskDateUtils.getDaysUntilDue(timestamp)
    assertTrue(result == 0L)
  }

  @Test
  fun `getDaysUntilDue returns positive value for future task`() {
    val tomorrow = Date(System.currentTimeMillis() + 25 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    val result = TaskDateUtils.getDaysUntilDue(timestamp)
    assertTrue(result > 0)
  }

  @Test
  fun `getDaysUntilDue returns negative value for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    val result = TaskDateUtils.getDaysUntilDue(timestamp)
    assertTrue(result < 0)
  }
}
