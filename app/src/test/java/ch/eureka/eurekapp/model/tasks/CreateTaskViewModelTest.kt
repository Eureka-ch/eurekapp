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
import kotlinx.coroutines.flow.flowOf
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
class CreateTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockFileRepository: MockFileStorageRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var viewModel: CreateTaskViewModel
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

  private fun createMockUri(path: String): Uri {
    val uri = mockk<Uri>(relaxed = true)
    every { uri.lastPathSegment } returns path.substringAfterLast('/')
    every { uri.toString() } returns path
    return uri
  }

  @Test
  fun viewModel_initialState_hasCorrectDefaults() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
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

  @Test
  fun setTitle_updatesStateCorrectly() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setTitle("New Task Title")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("New Task Title", state.title)
  }

  @Test
  fun setDescription_updatesStateCorrectly() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setDescription("Task description")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Task description", state.description)
  }

  @Test
  fun setDueDate_updatesStateCorrectly() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("01/01/2025", state.dueDate)
  }

  @Test
  fun setProjectId_updatesStateCorrectly() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setProjectId("project123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("project123", state.projectId)
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
    mockProjectRepository.setCurrentUserProjects(flowOf(projects))

    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
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
    mockProjectRepository.setCurrentUserProjects(flowOf(emptyList()))

    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(0, state.availableProjects.size)
    assertTrue(state.availableProjects.isEmpty())
  }

  @Test
  fun addAttachment_addsUriToList() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val uri = createMockUri("content://test/photo1.jpg")
    viewModel.addAttachment(uri)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.attachmentUris.size)
    assertEquals(uri, state.attachmentUris[0])
  }

  @Test
  fun addAttachment_doesNotAddDuplicate() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val uri = createMockUri("content://test/photo1.jpg")
    viewModel.addAttachment(uri)
    viewModel.addAttachment(uri)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.attachmentUris.size)
  }

  @Test
  fun removeAttachment_removesUriAtIndex() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val uri1 = createMockUri("content://test/photo1.jpg")
    val uri2 = createMockUri("content://test/photo2.jpg")
    viewModel.addAttachment(uri1)
    viewModel.addAttachment(uri2)
    advanceUntilIdle()

    viewModel.removeAttachment(0)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.attachmentUris.size)
    assertEquals(uri2, state.attachmentUris[0])
  }

  @Test
  fun inputValid_returnsFalseWhenTitleIsBlank() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setDescription("Description")
    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val isValid = viewModel.inputValid.first()
    assertFalse(isValid)
  }

  @Test
  fun inputValid_returnsFalseWhenDescriptionIsBlank() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setTitle("Title")
    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val isValid = viewModel.inputValid.first()
    assertFalse(isValid)
  }

  @Test
  fun inputValid_returnsFalseWhenDateIsInvalid() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setTitle("Title")
    viewModel.setDescription("Description")
    viewModel.setDueDate("invalid-date")
    advanceUntilIdle()

    val isValid = viewModel.inputValid.first()
    assertFalse(isValid)
  }

  @Test
  fun inputValid_returnsTrueWhenAllFieldsAreValid() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    advanceUntilIdle()

    viewModel.setTitle("Title")
    viewModel.setDescription("Description")
    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val isValid = viewModel.inputValid.first()
    assertTrue(isValid)
  }

  @Test
  fun addTask_setsIsSavingTrueDuringSave() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")

    // Add task (this will set isSaving to true)
    viewModel.addTask(mockContext)

    // Check that isSaving is false and taskSaved is true after completion
    advanceUntilIdle()
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertTrue(state.taskSaved)
  }

  @Test
  fun addTask_withPhotos_uploadsPhotosAndCreatesTask() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")

    val uri = createMockUri("content://test/photo1.jpg")
    viewModel.addAttachment(uri)

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify file was uploaded
    assertEquals(1, mockFileRepository.uploadFileCalls.size)
    assertEquals(uri, mockFileRepository.uploadFileCalls[0].second)

    // Verify task was created
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertTrue(state.taskSaved)
  }

  @Test
  fun addTask_withFileUploadError_setsIsSavingToFalse() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")

    val uri = createMockUri("content://test/photo1.jpg")
    viewModel.addAttachment(uri)

    // Configure file upload to fail
    mockFileRepository.setUploadFileResult(Result.failure(Exception("Upload failed")))

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify isSaving was reset to false on error
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertFalse(state.taskSaved)
  }

  @Test
  fun addTask_withTaskCreationError_setsIsSavingToFalse() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")

    // Configure task creation to fail
    mockTaskRepository.setCreateTaskResult(Result.failure(Exception("Failed to create task")))

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify isSaving was reset to false on error
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertFalse(state.taskSaved)
  }

  @Test
  fun addTask_withInvalidDate_setsErrorMessage() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("invalid-date")

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Invalid format, date must be DD/MM/YYYY.", state.errorMsg)
    assertFalse(state.isSaving)
    assertFalse(state.taskSaved)
  }

  @Test
  fun resetSaveState_resetsIsSavingAndTaskSaved() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify task was saved
    var state = viewModel.uiState.first()
    assertTrue(state.taskSaved)

    // Reset save state
    viewModel.resetSaveState()
    advanceUntilIdle()

    // Verify state was reset
    state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertFalse(state.taskSaved)
  }

  @Test
  fun clearErrorMsg_clearsErrorMessage() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)
    viewModel.setDueDate("invalid-date")
    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify error message is set
    var state = viewModel.uiState.first()
    assertEquals("Invalid format, date must be DD/MM/YYYY.", state.errorMsg)

    // Clear error message
    viewModel.clearErrorMsg()
    advanceUntilIdle()

    // Verify error message is cleared
    state = viewModel.uiState.first()
    assertEquals(null, state.errorMsg)
  }

  @Test
  fun dateRegex_matchesValidDates() {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)

    assertTrue(viewModel.dateRegex.matches("01/01/2025"))
    assertTrue(viewModel.dateRegex.matches("31/12/2024"))
    assertTrue(viewModel.dateRegex.matches("15/06/1990"))
  }

  @Test
  fun dateRegex_rejectsInvalidDates() {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            dispatcher = testDispatcher)

    assertFalse(viewModel.dateRegex.matches("1/1/2025"))
    assertFalse(viewModel.dateRegex.matches("2025-01-01"))
    assertFalse(viewModel.dateRegex.matches("invalid"))
    assertFalse(viewModel.dateRegex.matches(""))
  }
}
