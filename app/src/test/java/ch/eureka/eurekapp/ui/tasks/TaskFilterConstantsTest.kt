package ch.eureka.eurekapp.ui.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFilterConstantsTest {

  @Test
  fun `TaskFilterConstants has correct values`() {
    assertEquals("My tasks", TaskFilterConstants.FILTER_MY_TASKS)
    assertEquals("Team", TaskFilterConstants.FILTER_TEAM)
    assertEquals("This week", TaskFilterConstants.FILTER_THIS_WEEK)
    assertEquals("All", TaskFilterConstants.FILTER_ALL)
    assertEquals("Project", TaskFilterConstants.FILTER_PROJECT)
  }

  @Test
  fun `TaskFilterConstants FILTER_OPTIONS contains all values`() {
    val options = TaskFilterConstants.FILTER_OPTIONS
    assertEquals(5, options.size)
    assertTrue(options.contains(TaskFilterConstants.FILTER_MY_TASKS))
    assertTrue(options.contains(TaskFilterConstants.FILTER_TEAM))
    assertTrue(options.contains(TaskFilterConstants.FILTER_THIS_WEEK))
    assertTrue(options.contains(TaskFilterConstants.FILTER_ALL))
    assertTrue(options.contains(TaskFilterConstants.FILTER_PROJECT))
  }
}
