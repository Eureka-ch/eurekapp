package ch.eureka.eurekapp.ui.tasks

import org.junit.Assert.assertEquals
import org.junit.Test

class TaskFilterTest {

  @Test
  fun `TaskFilter enum has correct values`() {
    assertEquals(TaskFilter.MINE, TaskFilter.valueOf("MINE"))
    assertEquals(TaskFilter.TEAM, TaskFilter.valueOf("TEAM"))
    assertEquals(TaskFilter.THIS_WEEK, TaskFilter.valueOf("THIS_WEEK"))
    assertEquals(TaskFilter.ALL, TaskFilter.valueOf("ALL"))
    assertEquals(TaskFilter.PROJECT, TaskFilter.valueOf("PROJECT"))
  }

  @Test
  fun `TaskFilter enum has correct number of values`() {
    val values = TaskFilter.values()
    assertEquals(5, values.size)
  }
}
