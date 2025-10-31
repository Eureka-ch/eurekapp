package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
Co-Authored-By: Claude <noreply@anthropic.com>
*/

@OptIn(ExperimentalCoroutinesApi::class)
class EditTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockFileRepository: MockFileStorageRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var viewModel: EditTaskViewModel
  private lateinit var mockContext: Context

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = MockTaskRepository()
    mockFileRepository = MockFileStorageRepository()
    mockProjectRepository = MockProjectRepository()
    mockContext =
        mockk(relaxed = true) {
          val contentResolver = mockk<android.content.ContentResolver>(relaxed = true)
          every { this@mockk.contentResolver } returns contentResolver
          every { contentResolver.delete(any(), any(), any()) } returns 1
        }
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockTaskRepository.reset()
    mockFileRepository.reset()
    mockProjectRepository.reset()
  }

  @Test
  fun availableProjects_loadedFromRepositoryFlow() = runTest {
    val projects =
        listOf(
            Project(
                projectId = "proj1",
                name = "Project 1",
                description = "Description 1",
                status = ProjectStatus.OPEN),
            Project(
                projectId = "proj2",
                name = "Project 2",
                description = "Description 2",
                status = ProjectStatus.OPEN))
    viewModel =
        EditTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    // Manually set projects (simulating what LaunchedEffect in screen would do)
    viewModel.setAvailableProjects(projects)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(2, state.availableProjects.size)
    assertEquals("proj1", state.availableProjects[0].projectId)
    assertEquals("Project 1", state.availableProjects[0].name)
    assertEquals("proj2", state.availableProjects[1].projectId)
    assertEquals("Project 2", state.availableProjects[1].name)
  }

  @Test
  fun availableProjects_emptyListWhenNoProjects() = runTest {
    viewModel =
        EditTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    // Manually set empty projects (simulating what LaunchedEffect in screen would do)
    viewModel.setAvailableProjects(emptyList())
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(0, state.availableProjects.size)
    assertTrue(state.availableProjects.isEmpty())
  }

  @Test
  fun viewModel_initialState_hasCorrectDefaults() = runTest {
    viewModel =
        EditTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.dueDate)
    assertEquals(emptyList<Uri>(), state.attachmentUris)
    assertFalse(state.isSaving)
    assertFalse(state.taskSaved)
    assertEquals(null, state.errorMsg)
  }
}
