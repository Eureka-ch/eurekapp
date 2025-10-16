/**
 * Advanced unit tests for TaskDateUtils utility class
 *
 * Tests comprehensive date calculation scenarios including overdue tasks, due dates, and various
 * time-based business logic.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
import ch.eureka.eurekapp.model.utils.TaskDateUtils
import com.google.firebase.Timestamp
import java.util.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tests avancés pour TaskDateUtils Teste la logique de calcul des dates avec différents scénarios
 */
class TaskDateUtilsAdvancedTest {

  @Test
  fun `isDueThisWeek returns true for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    assertTrue(TaskDateUtils.isDueThisWeek(timestamp))
  }

  @Test
  fun `isDueThisWeek returns true for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    assertTrue(TaskDateUtils.isDueThisWeek(timestamp))
  }

  @Test
  fun `isDueThisWeek returns true for task due in 7 days`() {
    val in7Days = Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in7Days)
    assertTrue(TaskDateUtils.isDueThisWeek(timestamp))
  }

  @Test
  fun `isDueThisWeek returns false for task due in 8 days`() {
    val in8Days = Date(System.currentTimeMillis() + 8 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in8Days)
    assertFalse(TaskDateUtils.isDueThisWeek(timestamp))
  }

  @Test
  fun `isDueThisWeek returns false for overdue task`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    assertFalse(TaskDateUtils.isDueThisWeek(timestamp))
  }

  @Test
  fun `isDueThisWeek returns false for null due date`() {
    assertFalse(TaskDateUtils.isDueThisWeek(null))
  }

  @Test
  fun `isOverdue returns true for task due yesterday`() {
    val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(yesterday)
    assertTrue(TaskDateUtils.isOverdue(timestamp))
  }

  @Test
  fun `isOverdue returns true for task due 5 days ago`() {
    val fiveDaysAgo = Date(System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(fiveDaysAgo)
    assertTrue(TaskDateUtils.isOverdue(timestamp))
  }

  @Test
  fun `isOverdue returns false for task due today`() {
    val today = Date()
    val timestamp = Timestamp(today)
    assertFalse(TaskDateUtils.isOverdue(timestamp))
  }

  @Test
  fun `isOverdue returns false for task due tomorrow`() {
    val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(tomorrow)
    assertFalse(TaskDateUtils.isOverdue(timestamp))
  }

  @Test
  fun `isOverdue returns false for null due date`() {
    assertFalse(TaskDateUtils.isOverdue(null))
  }

  @Test
  fun `getDaysUntilDue returns correct positive days for future task`() {
    val in3Days = Date(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(in3Days)
    val days = TaskDateUtils.getDaysUntilDue(timestamp)
    assertEquals(3L, days)
  }

  @Test
  fun `getDaysUntilDue returns negative days for overdue task`() {
    val twoDaysAgo = Date(System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000)
    val timestamp = Timestamp(twoDaysAgo)
    val days = TaskDateUtils.getDaysUntilDue(timestamp)
    assertEquals(-2L, days)
  }

  @Test
  fun `getDaysUntilDue returns Long MAX_VALUE for null due date`() {
    val days = TaskDateUtils.getDaysUntilDue(null)
    assertEquals(Long.MAX_VALUE, days)
  }
}
