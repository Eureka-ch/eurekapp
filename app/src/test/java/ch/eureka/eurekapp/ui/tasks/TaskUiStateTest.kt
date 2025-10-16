/**
 * Unit tests for TaskUiState data class
 *
 * Tests UI state management including custom value creation and state updates.
 *
 * @author Assisted by AI for comprehensive test coverage
 */
package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskUiStateTest {

  @Test
  fun `TaskUiState has correct default values`() {
    val state = TaskUiState()

    assertEquals(TaskFilter.MINE, state.selectedFilter)
    assertTrue(state.rawTasks.isEmpty())
    assertFalse(state.isLoading)
    assertNull(state.error)
    assertNull(state.projectId)
  }

  @Test
  fun `TaskUiState can be created with custom values`() {
    val tasks = listOf(Task(taskID = "1"), Task(taskID = "2"))
    val state =
        TaskUiState(
            selectedFilter = TaskFilter.ALL,
            rawTasks = tasks,
            isLoading = true,
            error = "Test error",
            projectId = "project-123")

    assertEquals(TaskFilter.ALL, state.selectedFilter)
    assertEquals(2, state.rawTasks.size)
    assertTrue(state.isLoading)
    assertEquals("Test error", state.error)
    assertEquals("project-123", state.projectId)
  }

  @Test
  fun `TaskUiState copy works correctly`() {
    val originalState = TaskUiState()
    val newState = originalState.copy(selectedFilter = TaskFilter.TEAM, isLoading = true)

    assertEquals(TaskFilter.TEAM, newState.selectedFilter)
    assertTrue(newState.isLoading)
    // Other values should remain the same
    assertTrue(newState.rawTasks.isEmpty())
    assertNull(newState.error)
    assertNull(newState.projectId)
  }
}
