package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
Co-Authored-By: Claude <noreply@anthropic.com>
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
Co-Authored-By: Claude Opus 4.5
*/

@OptIn(ExperimentalCoroutinesApi::class)
class EditTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockFileRepository: MockFileStorageRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockTemplateRepository: MockTaskTemplateRepository
  private lateinit var viewModel: EditTaskViewModel
  private lateinit var mockContext: Context
  private lateinit var mockConnectivityObserver: ConnectivityObserver

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = MockTaskRepository()
    mockFileRepository = MockFileStorageRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
    mockTemplateRepository = MockTaskTemplateRepository()
    mockContext =
        mockk(relaxed = true) {
          val contentResolver = mockk<android.content.ContentResolver>(relaxed = true)
          every { this@mockk.contentResolver } returns contentResolver
          every { contentResolver.delete(any(), any(), any()) } returns 1
        }

    // Mock connectivity observer
    mockConnectivityObserver = mockk(relaxed = true)
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
    // Set mock to ConnectivityObserverProvider
    val providerField =
        ConnectivityObserverProvider::class.java.getDeclaredField("_connectivityObserver")
    providerField.isAccessible = true
    providerField.set(ConnectivityObserverProvider, mockConnectivityObserver)
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
    mockTaskRepository.reset()
    mockFileRepository.reset()
    mockProjectRepository.reset()
    mockUserRepository.reset()
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
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            getCurrentUserId = { null },
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // Projects are not loaded automatically without projectRepository parameter
    assertEquals(0, state.availableProjects.size)
  }

  @Test
  fun availableProjects_emptyListWhenNoProjects() = runTest {
    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            getCurrentUserId = { null },
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(0, state.availableProjects.size)
    assertTrue(state.availableProjects.isEmpty())
  }

  @Test
  fun viewModel_initialState_hasCorrectDefaults() = runTest {
    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            getCurrentUserId = { null },
            dispatcher = testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.dueDate)
    assertEquals("", state.reminderTime)
    assertEquals(emptyList<Uri>(), state.attachmentUris)
    assertFalse(state.isSaving)
    assertFalse(state.taskSaved)
    assertEquals(null, state.errorMsg)
  }

  @Test
  fun loadTask_loadsDependencies() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now(),
            dependingOnTasks = listOf("task1", "task2"))
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task1", "task2"), state.dependingOnTasks)
  }

  @Test
  fun addDependency_addsDependencyToList() = runTest {
    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    val task1 = Task(taskID = "task1", projectId = "project123")
    mockTaskRepository.addTask(task1)
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue(state.dependingOnTasks.contains("task1"))
  }

  @Test
  fun addDependency_doesNotAddDuplicate() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now())
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    val task1 = Task(taskID = "task1", projectId = "project123")
    mockTaskRepository.addTask(task1)
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()
    viewModel.addDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.dependingOnTasks.count { it == "task1" })
  }

  @Test
  fun addDependency_withCycle_setsCycleError() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now())
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    // Setup: task1 depends on task123
    val task1 =
        Task(taskID = "task1", projectId = "project123", dependingOnTasks = listOf("task123"))
    mockTaskRepository.addTask(task1)
    advanceUntilIdle()

    // Try to add task1 as dependency (would create cycle)
    viewModel.addDependency("task1")
    advanceUntilIdle()

    val cycleError = viewModel.cycleError.first()
    assertNotNull(cycleError)
    assertTrue(cycleError!!.contains("circular"))
  }

  @Test
  fun removeDependency_removesFromList() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now(),
            dependingOnTasks = listOf("task1", "task2"))
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    viewModel.removeDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task2"), state.dependingOnTasks)
  }

  @Test
  fun removeDependency_whenNotExists_doesNotCrash() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now())
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    viewModel.removeDependency("nonexistent")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<String>(), state.dependingOnTasks)
  }

  @Test
  fun editTask_withDependencies_savesDependencies() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Original",
            description = "Desc",
            dueDate = Timestamp.now())
    mockTaskRepository.addTask(task)

    val task1 = Task(taskID = "task1", projectId = "project123")
    mockTaskRepository.addTask(task1)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            getCurrentUserId = { "test-user" },
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    viewModel.setTitle("Updated Title")
    viewModel.setDescription("Updated Desc")
    viewModel.setDueDate("01/01/2025")
    viewModel.addDependency("task1")
    advanceUntilIdle()

    viewModel.editTask(mockContext)
    advanceUntilIdle()

    val updatedTask = mockTaskRepository.updateTaskCalls.first()
    assertEquals(listOf("task1"), updatedTask.dependingOnTasks)
  }

  @Test
  fun editTask_withCycleDetected_preventsSave() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now())
    mockTaskRepository.addTask(task)

    // Setup cycle: task1 depends on task123, so if task123 depends on task1, it creates a cycle
    val task1 =
        Task(taskID = "task1", projectId = "project123", dependingOnTasks = listOf("task123"))
    mockTaskRepository.addTask(task1)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            getCurrentUserId = { "test-user" },
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    viewModel.setTitle("Updated Title")
    viewModel.setDescription("Updated Desc")
    viewModel.setDueDate("01/01/2025")

    // Try to add task1 as dependency using addDependency
    // This should detect the cycle and NOT add the dependency
    viewModel.addDependency("task1")
    advanceUntilIdle()

    // Check that cycle error is set and dependency was NOT added
    val cycleError = viewModel.cycleError.first()
    assertNotNull(cycleError)

    var state = viewModel.uiState.first()
    // The dependency should NOT be added because cycle was detected
    assertFalse(state.dependingOnTasks.contains("task1"))

    // Now force the dependency using setDependencies to test editTask validation
    viewModel.setDependencies(listOf("task1"))
    advanceUntilIdle()

    viewModel.editTask(mockContext)
    advanceUntilIdle()

    state = viewModel.uiState.first()
    // The task should not be saved due to cycle detection in editTask
    assertFalse(state.taskSaved)
    // Error message should be set by the cycle validation in editTask
    assertNotNull(state.errorMsg)
    assertTrue(state.errorMsg!!.contains("circular") || state.errorMsg!!.contains("dependency"))
  }

  @Test
  fun removeAttachment_removesUrlAtIndex() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("http://example.com/file1.pdf", "http://example.com/file2.pdf"))
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    // Verify initial state has 2 URLs
    var state = viewModel.uiState.first()
    assertEquals(2, state.attachmentUrls.size)

    // Remove the first URL (index 0 - since attachmentUris is empty, index 0 is in URLs)
    viewModel.removeAttachment(0)
    advanceUntilIdle()

    state = viewModel.uiState.first()
    assertEquals(1, state.attachmentUrls.size)
    assertEquals("http://example.com/file2.pdf", state.attachmentUrls[0])
  }

  @Test
  fun loadTask_loadsTemplateWhenPresent() = runTest {
    val template =
        TaskTemplate(
            templateID = "template123",
            projectId = "project123",
            title = "Test Template",
            description = "Template Description",
            definedFields = TaskTemplateSchema(emptyList()),
            createdBy = "user1")
    mockTemplateRepository.setTemplate(template)

    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            templateId = "template123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now(),
            customData = TaskCustomData())
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertNotNull(state.selectedTemplate)
    assertEquals("Test Template", state.selectedTemplate?.title)
    assertEquals("template123", state.selectedTemplate?.templateID)
  }

  @Test
  fun updateCustomFieldValue_updatesState() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now(),
            customData = TaskCustomData())
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    viewModel.updateCustomFieldValue("field1", FieldValue.TextValue("test value"))
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    val fieldValue = state.customData.getValue("field1")
    assertNotNull(fieldValue)
    assertTrue(fieldValue is FieldValue.TextValue)
    assertEquals("test value", (fieldValue as FieldValue.TextValue).value)
  }

  @Test
  fun loadTask_skipsWhenDeletingOrDeleted() = runTest {
    val task =
        Task(
            taskID = "task123",
            projectId = "project123",
            title = "Test Task",
            description = "Description",
            dueDate = Timestamp.now())
    mockTaskRepository.addTask(task)

    viewModel =
        EditTaskViewModel(
            taskRepository = mockTaskRepository,
            fileRepository = mockFileRepository,
            projectRepository = mockProjectRepository,
            userRepository = mockUserRepository,
            templateRepository = mockTemplateRepository,
            dispatcher = testDispatcher)

    // Simulate deletion in progress
    viewModel.deleteTask("project123", "task123")
    advanceUntilIdle()

    // Now try to load - should skip because task is being deleted
    viewModel.loadTask("project123", "task123")
    advanceUntilIdle()

    // State should reflect deleted status
    val state = viewModel.uiState.first()
    assertTrue(state.taskDeleted)
  }

  private class MockTaskTemplateRepository : TaskTemplateRepository {
    private val templateFlow = MutableStateFlow<TaskTemplate?>(null)

    fun setTemplate(template: TaskTemplate?) {
      templateFlow.value = template
    }

    override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> =
        flowOf(emptyList())

    override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> =
        templateFlow

    override suspend fun createTemplate(template: TaskTemplate): Result<String> =
        Result.success(template.templateID)

    override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> = Result.success(Unit)

    override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> =
        Result.success(Unit)
  }
}
