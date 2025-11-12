package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
Note: This file was partially written by GPT-5 Codex Co-author : GPT-5
*/

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockFileRepository: MockFileStorageRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var viewModel: CreateTaskViewModel
  private lateinit var mockContext: Context

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = MockTaskRepository()
    mockFileRepository = MockFileStorageRepository()
    mockProjectRepository = MockProjectRepository()
    mockUserRepository = MockUserRepository()
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
    mockUserRepository.reset()
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
            mockUserRepository,
            { null },
            testDispatcher)
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
  fun setTitle_updatesStateCorrectly() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // Projects are not loaded automatically without projectRepository parameter
    assertEquals(0, state.availableProjects.size)
  }

  @Test
  fun availableProjects_emptyListWhenNoProjects() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
  fun removeAttachment_withInvalidIndex_doesNothing() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
    advanceUntilIdle()

    val uri1 = createMockUri("content://test/photo1.jpg")
    viewModel.addAttachment(uri1)
    advanceUntilIdle()

    // Try to remove at invalid index
    viewModel.removeAttachment(10)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // Attachment should still be there
    assertEquals(1, state.attachmentUris.size)
    assertEquals(uri1, state.attachmentUris[0])
  }

  @Test
  fun deletePhoto_withSecurityException_returnsFalse() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
    advanceUntilIdle()

    val uri = createMockUri("content://test/photo1.jpg")
    // Mock context to throw SecurityException
    every { mockContext.contentResolver.delete(uri, any(), any()) } throws
        SecurityException("Permission denied")

    val result = viewModel.deletePhoto(mockContext, uri)
    assertFalse(result)
  }

  @Test
  fun deletePhoto_withZeroRowsDeleted_returnsFalse() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
    advanceUntilIdle()

    val uri = createMockUri("content://test/photo1.jpg")
    // Mock context to return 0 rows deleted
    every { mockContext.contentResolver.delete(uri, any(), any()) } returns 0

    val result = viewModel.deletePhoto(mockContext, uri)
    assertFalse(result)
  }

  @Test
  fun inputValid_returnsFalseWhenTitleIsBlank() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
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
            mockUserRepository,
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
            mockUserRepository,
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
            mockUserRepository,
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
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
            mockUserRepository,
            { null },
            testDispatcher)
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
            mockUserRepository,
            { null },
            testDispatcher)

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
            mockUserRepository,
            { null },
            testDispatcher)

    assertFalse(viewModel.dateRegex.matches("1/1/2025"))
    assertFalse(viewModel.dateRegex.matches("2025-01-01"))
    assertFalse(viewModel.dateRegex.matches("invalid"))
    assertFalse(viewModel.dateRegex.matches(""))
  }

  @Test
  fun addTask_withValidReminderTime_createsTaskWithReminder() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    viewModel.setReminderTime("14:30")

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify task was created successfully
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertTrue(state.taskSaved)

    // Verify task was created with reminder time
    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertTrue(createdTask.reminderTime != null)
  }

  @Test
  fun addTask_withInvalidReminderTimeFormat_createsTaskWithoutReminder() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    viewModel.setReminderTime("25:70") // Invalid time format

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Task should still be created but without reminder
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertTrue(state.taskSaved)

    // Verify task was created without reminder
    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertTrue(createdTask.reminderTime == null)
  }

  // ========== USER ASSIGNMENT TESTS ==========

  @Test
  fun initialState_currentUserIsPreselectedAsAssignee() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("user-123"), state.selectedAssignedUserIds)
  }

  @Test
  fun initialState_availableUsersIsEmpty() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<User>(), state.availableUsers)
  }

  @Test
  fun loadProjectMembers_loadsUsersFromProjectMembers() = runTest {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.OWNER),
            Member(userId = "user2", role = ProjectRole.MEMBER))

    val user1 =
        User(uid = "user1", displayName = "Alice", email = "alice@example.com", photoUrl = "")
    val user2 = User(uid = "user2", displayName = "Bob", email = "bob@example.com", photoUrl = "")

    mockProjectRepository.setMembers("project123", kotlinx.coroutines.flow.flowOf(members))
    mockUserRepository.setUsers(user1, user2)

    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    viewModel.loadProjectMembers("project123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(2, state.availableUsers.size)
    assertTrue(state.availableUsers.any { it.uid == "user1" })
    assertTrue(state.availableUsers.any { it.uid == "user2" })
  }

  @Test
  fun loadProjectMembers_withBlankProjectId_clearsAvailableUsers() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    viewModel.loadProjectMembers("")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<User>(), state.availableUsers)
  }

  @Test
  fun setAssignedUsers_updatesStateCorrectly() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    viewModel.setAssignedUsers(listOf("user1", "user2", "user3"))
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("user1", "user2", "user3"), state.selectedAssignedUserIds)
  }

  @Test
  fun toggleUserAssignment_addsUserWhenNotPresent() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    // Start with just current user
    viewModel.setAssignedUsers(listOf("user-123"))
    advanceUntilIdle()

    viewModel.toggleUserAssignment("user2")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("user-123", "user2"), state.selectedAssignedUserIds)
  }

  @Test
  fun toggleUserAssignment_removesUserWhenPresent() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    viewModel.setAssignedUsers(listOf("user-123", "user2", "user3"))
    advanceUntilIdle()

    viewModel.toggleUserAssignment("user2")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("user-123", "user3"), state.selectedAssignedUserIds)
  }

  @Test
  fun addTask_usesSelectedAssignedUserIds() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    viewModel.setAssignedUsers(listOf("user1", "user2", "user3"))

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify task was created with selected assigned users
    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertEquals(listOf("user1", "user2", "user3"), createdTask.assignedUserIds)
  }

  @Test
  fun addTask_withEmptySelectedUsers_usesCurrentUserAsDefault() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "test-user-123" },
            testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    viewModel.setAssignedUsers(emptyList())

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify task was created with current user as assignee
    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertEquals(listOf("test-user-123"), createdTask.assignedUserIds)
  }

  @Test
  fun loadProjectMembers_callsProjectRepositoryWithCorrectProjectId() = runTest {
    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    viewModel.loadProjectMembers("project-xyz")
    advanceUntilIdle()

    assertTrue(mockProjectRepository.getMembersCalls.contains("project-xyz"))
  }

  @Test
  fun loadProjectMembers_fetchesUserDetailsForEachMember() = runTest {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.OWNER),
            Member(userId = "user2", role = ProjectRole.MEMBER),
            Member(userId = "user3", role = ProjectRole.ADMIN))

    val user1 =
        User(uid = "user1", displayName = "Alice", email = "alice@example.com", photoUrl = "")
    val user2 = User(uid = "user2", displayName = "Bob", email = "bob@example.com", photoUrl = "")
    val user3 =
        User(uid = "user3", displayName = "Charlie", email = "charlie@example.com", photoUrl = "")

    mockProjectRepository.setMembers("project123", kotlinx.coroutines.flow.flowOf(members))
    mockUserRepository.setUsers(user1, user2, user3)

    viewModel =
        CreateTaskViewModel(
            mockTaskRepository,
            mockFileRepository,
            mockProjectRepository,
            mockUserRepository,
            { "user-123" },
            testDispatcher)
    advanceUntilIdle()

    viewModel.loadProjectMembers("project123")
    advanceUntilIdle()

    // Verify getUserById was called for each member
    assertTrue(mockUserRepository.getUserByIdCalls.contains("user1"))
    assertTrue(mockUserRepository.getUserByIdCalls.contains("user2"))
    assertTrue(mockUserRepository.getUserByIdCalls.contains("user3"))
  @Test
  fun addDependency_addsDependencyToList() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task1"), state.dependingOnTasks)
  }

  @Test
  fun addDependency_doesNotAddDuplicate() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()
    viewModel.addDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task1"), state.dependingOnTasks)
  }

  @Test
  fun removeDependency_removesFromList() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.addDependency("task1")
    viewModel.addDependency("task2")
    advanceUntilIdle()

    viewModel.removeDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task2"), state.dependingOnTasks)
  }

  @Test
  fun removeDependency_whenNotExists_doesNotCrash() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.removeDependency("nonexistent")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<String>(), state.dependingOnTasks)
  }

  @Test
  fun removeDependency_clearsCycleError() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    viewModel.setProjectId("project123")

    val task1 = Task(taskID = "task1", projectId = "project123")
    mockTaskRepository.addTask(task1)
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()

    // Remove dependency should clear any error and remove from list
    viewModel.removeDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertFalse(state.dependingOnTasks.contains("task1"))

    val cycleError = viewModel.cycleError.first()
    assertEquals(null, cycleError)
  }

  @Test
  fun validateDependency_returnsTrueWhenProjectIdEmpty() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    advanceUntilIdle()

    val result = viewModel.validateDependency("task1")

    assertTrue(result)
    assertEquals(null, viewModel.cycleError.first())
  }

  @Test
  fun validateDependency_noCycle_returnsTrueAndClearsError() = runTest {
    mockkObject(IdGenerator)
    try {
      every { IdGenerator.generateTaskId() } returns "task_new"

      viewModel =
          CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
      viewModel.setProjectId("project123")
      advanceUntilIdle()

      val dependencyTask = Task(taskID = "task1", projectId = "project123")
      mockTaskRepository.addTask(dependencyTask)

      val result = viewModel.validateDependency("task1")
      advanceUntilIdle()

      assertTrue(result)
      assertEquals(null, viewModel.cycleError.first())
    } finally {
      unmockkObject(IdGenerator)
    }
  }

  @Test
  fun validateDependency_cycleDetected_setsErrorAndReturnsFalse() = runTest {
    mockkObject(IdGenerator)
    try {
      every { IdGenerator.generateTaskId() } returns "task_new"

      viewModel =
          CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
      viewModel.setProjectId("project123")
      advanceUntilIdle()

      val dependencyTask =
          Task(taskID = "task1", projectId = "project123", dependingOnTasks = listOf("task_new"))
      mockTaskRepository.addTask(dependencyTask)

      val result = viewModel.validateDependency("task1")
      advanceUntilIdle()

      assertFalse(result)
      assertEquals(
          "Adding this dependency would create a circular dependency", viewModel.cycleError.first())
    } finally {
      unmockkObject(IdGenerator)
    }
  }

  @Test
  fun addTask_withDependencies_savesDependencies() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, { "test-user" }, testDispatcher)
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Description")
    viewModel.setDueDate("01/01/2025")

    val task1 = Task(taskID = "task1", projectId = "project123")
    mockTaskRepository.addTask(task1)
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    val createdTask = mockTaskRepository.createTaskCalls.first()
    assertEquals(listOf("task1"), createdTask.dependingOnTasks)
  }

  @Test
  fun setDependencies_setsAllDependencies() = runTest {
    viewModel =
        CreateTaskViewModel(mockTaskRepository, mockFileRepository, dispatcher = testDispatcher)
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.setDependencies(listOf("task1", "task2", "task3"))
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task1", "task2", "task3"), state.dependingOnTasks)
  }
}
