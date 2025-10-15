package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MockTaskRepositoryTest {
  private lateinit var mockRepository: MockTaskRepository

  @Before
  fun setup() {
    mockRepository = MockTaskRepository()
  }

  @Test
  fun `getTaskById returns null`() = runTest {
    val result = mockRepository.getTaskById("project1", "task1")
    result.collect { task -> assertNull(task) }
  }

  @Test
  fun `getTasksInProject returns empty list`() = runTest {
    val result = mockRepository.getTasksInProject("project1")
    result.collect { tasks -> assertTrue(tasks.isEmpty()) }
  }

  @Test
  fun `getTasksForCurrentUser returns empty list`() = runTest {
    val result = mockRepository.getTasksForCurrentUser()
    result.collect { tasks -> assertTrue(tasks.isEmpty()) }
  }

  @Test
  fun `createTask returns success with mock task id`() = runTest {
    val task = Task(taskID = "1", title = "Test Task")
    val result = mockRepository.createTask(task)

    assertTrue(result.isSuccess)
    assertEquals("mock-task-id", result.getOrNull())
  }

  @Test
  fun `updateTask returns success`() = runTest {
    val task = Task(taskID = "1", title = "Updated Task")
    val result = mockRepository.updateTask(task)

    assertTrue(result.isSuccess)
  }

  @Test
  fun `deleteTask returns success`() = runTest {
    val result = mockRepository.deleteTask("project1", "task1")

    assertTrue(result.isSuccess)
  }

  @Test
  fun `assignUser returns success`() = runTest {
    val result = mockRepository.assignUser("project1", "task1", "user1")

    assertTrue(result.isSuccess)
  }

  @Test
  fun `unassignUser returns success`() = runTest {
    val result = mockRepository.unassignUser("project1", "task1", "user1")

    assertTrue(result.isSuccess)
  }
}
