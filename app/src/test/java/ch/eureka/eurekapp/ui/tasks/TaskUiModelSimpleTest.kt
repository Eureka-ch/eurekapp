package ch.eureka.eurekapp.ui.tasks

import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskStatus
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

class TaskUiModelTest {

    @Test
    fun `formatDueDate overdue returns Overdue`() {
        // Given
        val yesterday = Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        val timestamp = Timestamp(yesterday)
        val task = Task(dueDate = timestamp)
        val uiModel = TaskUiModel(task = task)

        // When
        val result = uiModel.dueDate

        // Then
        assertEquals("Overdue", result)
    }

    @Test
    fun `formatDueDate today returns Due today`() {
        // Given
        val today = Date()
        val timestamp = Timestamp(today)
        val task = Task(dueDate = timestamp)
        val uiModel = TaskUiModel(task = task)

        // When
        val result = uiModel.dueDate

        // Then
        assertEquals("Due today", result)
    }

    @Test
    fun `formatDueDate tomorrow returns Due tomorrow`() {
        // Given
        val tomorrow = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
        val timestamp = Timestamp(tomorrow)
        val task = Task(dueDate = timestamp)
        val uiModel = TaskUiModel(task = task)

        // When
        val result = uiModel.dueDate

        // Then
        assertEquals("Due tomorrow", result)
    }

    @Test
    fun `formatDueDate null returns No due date`() {
        // Given
        val task = Task(dueDate = null)
        val uiModel = TaskUiModel(task = task)

        // When
        val result = uiModel.dueDate

        // Then
        assertEquals("No due date", result)
    }

    @Test
    fun `assigneeName with no assignee returns Unassigned`() {
        // Given
        val task = Task()
        val uiModel = TaskUiModel(task = task, assignee = null)

        // When
        val result = uiModel.assigneeName

        // Then
        assertEquals("Unassigned", result)
    }

    @Test
    fun `title with empty task title returns Untitled Task`() {
        // Given
        val task = Task(title = "")
        val uiModel = TaskUiModel(task = task, template = null)

        // When
        val result = uiModel.title

        // Then
        assertEquals("Untitled Task", result)
    }

    @Test
    fun `isCompleted true when task status is completed`() {
        // Given
        val task = Task(status = TaskStatus.COMPLETED)
        val uiModel = TaskUiModel(task = task)

        // When
        val result = uiModel.isCompleted

        // Then
        assertTrue(result)
    }

    @Test
    fun `isCompleted false when task status is not completed`() {
        // Given
        val task = Task(status = TaskStatus.IN_PROGRESS)
        val uiModel = TaskUiModel(task = task)

        // When
        val result = uiModel.isCompleted

        // Then
        assertFalse(result)
    }

    @Test
    fun `progressText returns correct percentage`() {
        // Given
        val task = Task()
        val uiModel = TaskUiModel(task = task, progress = 0.65f)

        // When
        val result = uiModel.progressText

        // Then
        assertEquals("65%", result)
    }

    @Test
    fun `progressValue returns correct float value`() {
        // Given
        val task = Task()
        val uiModel = TaskUiModel(task = task, progress = 0.75f)

        // When
        val result = uiModel.progressValue

        // Then
        assertEquals(0.75f, result, 0.01f)
    }
}
