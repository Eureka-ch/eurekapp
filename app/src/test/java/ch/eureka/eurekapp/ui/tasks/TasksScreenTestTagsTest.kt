package ch.eureka.eurekapp.ui.tasks

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TasksScreenTestTagsTest {

  @Test
  fun `TasksScreenTestTags has correct values`() {
    assertEquals("tasksScreenContent", TasksScreenTestTags.TASKS_SCREEN_CONTENT)
    assertEquals("tasksScreenText", TasksScreenTestTags.TASKS_SCREEN_TEXT)
    assertEquals("loadingIndicator", TasksScreenTestTags.LOADING_INDICATOR)
    assertEquals("errorMessage", TasksScreenTestTags.ERROR_MESSAGE)
    assertEquals("emptyState", TasksScreenTestTags.EMPTY_STATE)
    assertEquals("taskList", TasksScreenTestTags.TASK_LIST)
  }

  @Test
  fun `TasksScreenTestTags object exists`() {
    assertNotNull(TasksScreenTestTags::class.java)
  }
}
