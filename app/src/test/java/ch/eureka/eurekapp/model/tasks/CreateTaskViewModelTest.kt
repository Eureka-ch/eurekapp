package ch.eureka.eurekapp.model.tasks

import android.content.Context
import android.net.Uri
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.project.Member
import ch.eureka.eurekapp.model.data.project.Project
import ch.eureka.eurekapp.model.data.project.ProjectRole
import ch.eureka.eurekapp.model.data.project.ProjectStatus
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.data.template.field.FieldDefinition
import ch.eureka.eurekapp.model.data.template.field.FieldType
import ch.eureka.eurekapp.model.data.template.field.FieldValue
import ch.eureka.eurekapp.model.data.user.User
import ch.eureka.eurekapp.ui.tasks.MockProjectRepository
import ch.eureka.eurekapp.ui.tasks.MockTaskRepository
import ch.eureka.eurekapp.ui.tasks.MockUserRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
 * Portions of this code were generated with the help of Claude <noreply@anthropic.com> and GPT-5 Codex.
 * Co-Authored-By: Claude Sonnet 4.5
 */

@OptIn(ExperimentalCoroutinesApi::class)
class CreateTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: MockTaskRepository
  private lateinit var mockFileRepository: MockFileStorageRepository
  private lateinit var mockProjectRepository: MockProjectRepository
  private lateinit var mockUserRepository: MockUserRepository
  private lateinit var mockTemplateRepository: MockTaskTemplateRepository
  private lateinit var viewModel: CreateTaskViewModel
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
    mockTemplateRepository.reset()
  }

  private fun createMockUri(path: String): Uri {
    val uri = mockk<Uri>(relaxed = true)
    every { uri.lastPathSegment } returns path.substringAfterLast('/')
    every { uri.toString() } returns path
    return uri
  }

  private fun createViewModel(
      getCurrentUserId: () -> String? = { null },
      templateRepository: MockTaskTemplateRepository = mockTemplateRepository
  ): CreateTaskViewModel {
    return CreateTaskViewModel(
        taskRepository = mockTaskRepository,
        fileRepository = mockFileRepository,
        projectRepository = mockProjectRepository,
        userRepository = mockUserRepository,
        templateRepository = templateRepository,
        getCurrentUserId = getCurrentUserId,
        dispatcher = testDispatcher)
  }

  @Test
  fun viewModel_initialState_hasCorrectDefaults() = runTest {
    viewModel = createViewModel()
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
    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.setTitle("New Task Title")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("New Task Title", state.title)
  }

  @Test
  fun setDescription_updatesStateCorrectly() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.setDescription("Task description")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Task description", state.description)
  }

  @Test
  fun setDueDate_updatesStateCorrectly() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("01/01/2025", state.dueDate)
  }

  @Test
  fun setProjectId_updatesStateCorrectly() = runTest {
    viewModel = createViewModel()
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
    viewModel = createViewModel()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // Projects are not loaded automatically without projectRepository parameter
    assertEquals(0, state.availableProjects.size)
  }

  @Test
  fun availableProjects_emptyListWhenNoProjects() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(0, state.availableProjects.size)
    assertTrue(state.availableProjects.isEmpty())
  }

  @Test
  fun addAttachment_addsUriToList() = runTest {
    viewModel = createViewModel()
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
    viewModel = createViewModel()
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
    viewModel = createViewModel()
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
    viewModel = createViewModel()
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
    viewModel = createViewModel()
    advanceUntilIdle()

    val uri = createMockUri("content://test/photo1.jpg")
    // Mock context to throw SecurityException
    every { mockContext.contentResolver.delete(uri, any(), any()) } throws
        SecurityException("Permission denied")

    var result: Boolean? = null
    viewModel.deletePhotoAsync(mockContext, uri) { success -> result = success }
    advanceUntilIdle()

    assertFalse(result!!)
  }

  @Test
  fun deletePhoto_withZeroRowsDeleted_returnsFalse() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    val uri = createMockUri("content://test/photo1.jpg")
    // Mock context to return 0 rows deleted
    every { mockContext.contentResolver.delete(uri, any(), any()) } returns 0

    var result: Boolean? = null
    viewModel.deletePhotoAsync(mockContext, uri) { success -> result = success }
    advanceUntilIdle()

    assertFalse(result!!)
  }

  @Test
  fun inputValid_returnsFalseWhenTitleIsBlank() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.setDescription("Description")
    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val isValid = viewModel.inputValid.first()
    assertFalse(isValid)
  }

  @Test
  fun inputValid_returnsFalseWhenDescriptionIsBlank() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.setTitle("Title")
    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    val isValid = viewModel.inputValid.first()
    assertFalse(isValid)
  }

  @Test
  fun inputValid_returnsFalseWhenDateIsInvalid() = runTest {
    viewModel = createViewModel()
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
    viewModel = createViewModel()
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
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")

    val uri = createMockUri("content://test/photo1.jpg")
    viewModel.addAttachment(uri)

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify file was uploaded
    assertEquals(1, mockFileRepository.uploadFileDescriptorCalls.size)

    // Verify task was created
    val state = viewModel.uiState.first()
    assertFalse(state.isSaving)
    assertTrue(state.taskSaved)
  }

  @Test
  fun addTask_withFileUploadError_setsIsSavingToFalse() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
    viewModel = createViewModel()
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
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
    viewModel = createViewModel()
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
    viewModel = createViewModel()

    assertTrue(viewModel.dateRegex.matches("01/01/2025"))
    assertTrue(viewModel.dateRegex.matches("31/12/2024"))
    assertTrue(viewModel.dateRegex.matches("15/06/1990"))
  }

  @Test
  fun dateRegex_rejectsInvalidDates() {
    viewModel = createViewModel()

    assertFalse(viewModel.dateRegex.matches("1/1/2025"))
    assertFalse(viewModel.dateRegex.matches("2025-01-01"))
    assertFalse(viewModel.dateRegex.matches("invalid"))
    assertFalse(viewModel.dateRegex.matches(""))
  }

  @Test
  fun addTask_withValidReminderTime_createsTaskWithReminder() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
  fun initialState_noUserIsPreselectedAsAssignee() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // No user should be preselected - user can choose to assign or leave empty
    assertEquals(emptyList<String>(), state.selectedAssignedUserIds)
  }

  @Test
  fun initialState_availableUsersIsEmpty() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
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

    viewModel = createViewModel(getCurrentUserId = { "user-123" })
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
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
    advanceUntilIdle()

    viewModel.loadProjectMembers("")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<User>(), state.availableUsers)
  }

  @Test
  fun setAssignedUsers_updatesStateCorrectly() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
    advanceUntilIdle()

    viewModel.setAssignedUsers(listOf("user1", "user2", "user3"))
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("user1", "user2", "user3"), state.selectedAssignedUserIds)
  }

  @Test
  fun toggleUserAssignment_addsUserWhenNotPresent() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
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
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
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
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
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
  fun addTask_withEmptySelectedUsers_createsTaskWithEmptyAssignedUsers() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    viewModel.setAssignedUsers(emptyList())

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    // Verify task was created with empty assigned users list (no default assignment)
    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertEquals(emptyList<String>(), createdTask.assignedUserIds)
  }

  @Test
  fun loadProjectMembers_callsProjectRepositoryWithCorrectProjectId() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "user-123" })
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

    viewModel = createViewModel(getCurrentUserId = { "user-123" })
    advanceUntilIdle()

    viewModel.loadProjectMembers("project123")
    advanceUntilIdle()

    // Verify getUserById was called for each member
    assertTrue(mockUserRepository.getUserByIdCalls.contains("user1"))
    assertTrue(mockUserRepository.getUserByIdCalls.contains("user2"))
    assertTrue(mockUserRepository.getUserByIdCalls.contains("user3"))
  }

  @Test
  fun loadProjectMembers_includesCurrentUserEvenIfNotInMembers() = runTest {
    val members =
        listOf(
            Member(userId = "user1", role = ProjectRole.MEMBER),
            Member(userId = "user2", role = ProjectRole.MEMBER))

    val user1 =
        User(uid = "user1", displayName = "Alice", email = "alice@example.com", photoUrl = "")
    val user2 = User(uid = "user2", displayName = "Bob", email = "bob@example.com", photoUrl = "")
    val currentUser =
        User(
            uid = "current-user",
            displayName = "Current User",
            email = "current@example.com",
            photoUrl = "")

    mockProjectRepository.setMembers("project123", kotlinx.coroutines.flow.flowOf(members))
    mockUserRepository.setUsers(user1, user2, currentUser)

    viewModel = createViewModel(getCurrentUserId = { "current-user" })
    advanceUntilIdle()

    viewModel.loadProjectMembers("project123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // Should include all members plus current user
    assertEquals(3, state.availableUsers.size)
    assertTrue(state.availableUsers.any { it.uid == "user1" })
    assertTrue(state.availableUsers.any { it.uid == "user2" })
    assertTrue(state.availableUsers.any { it.uid == "current-user" })
  }

  @Test
  fun loadProjectMembers_includesCurrentUserWhenNoMembers() = runTest {
    val currentUser =
        User(
            uid = "current-user",
            displayName = "Current User",
            email = "current@example.com",
            photoUrl = "")

    mockProjectRepository.setMembers("project123", kotlinx.coroutines.flow.flowOf(emptyList()))
    mockUserRepository.setUsers(currentUser)

    viewModel = createViewModel(getCurrentUserId = { "current-user" })
    advanceUntilIdle()

    viewModel.loadProjectMembers("project123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    // Should include current user even when no members
    assertEquals(1, state.availableUsers.size)
    assertTrue(state.availableUsers.any { it.uid == "current-user" })
  }

  @Test
  fun addDependency_addsDependencyToList() = runTest {
    viewModel = createViewModel()
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.addDependency("task1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task1"), state.dependingOnTasks)
  }

  @Test
  fun addDependency_doesNotAddDuplicate() = runTest {
    viewModel = createViewModel()
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
    viewModel = createViewModel()
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
    viewModel = createViewModel()
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.removeDependency("nonexistent")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<String>(), state.dependingOnTasks)
  }

  @Test
  fun removeDependency_clearsCycleError() = runTest {
    viewModel = createViewModel()
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
    viewModel = createViewModel()
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

      viewModel = createViewModel()
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

      viewModel = createViewModel()
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
    viewModel = createViewModel(getCurrentUserId = { "test-user" })
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
    viewModel = createViewModel()
    viewModel.setProjectId("project123")
    advanceUntilIdle()

    viewModel.setDependencies(listOf("task1", "task2", "task3"))
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(listOf("task1", "task2", "task3"), state.dependingOnTasks)
  }

  // ========== TEMPLATE SELECTION TESTS ==========

  @Test
  fun initialState_hasNoTemplateSelected() = runTest {
    viewModel = createViewModel()
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(null, state.templateId)
    assertEquals(null, state.selectedTemplate)
    assertEquals(emptyList<TaskTemplate>(), state.availableTemplates)
    assertEquals(TaskCustomData(), state.customData)
  }

  @Test
  fun loadTemplatesForProject_loadsTemplates() = runTest {
    val templates =
        listOf(
            TaskTemplate(templateID = "t1", title = "Bug Report", projectId = "project123"),
            TaskTemplate(templateID = "t2", title = "Feature Request", projectId = "project123"))
    mockTemplateRepository.setTemplates("project123", templates)

    viewModel = createViewModel()
    advanceUntilIdle()

    viewModel.loadTemplatesForProject("project123")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(2, state.availableTemplates.size)
    assertEquals("t1", state.availableTemplates[0].templateID)
    assertEquals("t2", state.availableTemplates[1].templateID)
  }

  @Test
  fun loadTemplatesForProject_withEmptyProjectId_clearsTemplates() = runTest {
    val templates = listOf(TaskTemplate(templateID = "t1", title = "Bug Report"))
    mockTemplateRepository.setTemplates("project123", templates)

    viewModel = createViewModel()
    viewModel.loadTemplatesForProject("project123")
    advanceUntilIdle()

    viewModel.loadTemplatesForProject("")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(emptyList<TaskTemplate>(), state.availableTemplates)
    assertEquals(null, state.selectedTemplate)
  }

  @Test
  fun selectTemplate_selectsTemplateById() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Bug Report",
            projectId = "project123",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "severity",
                            label = "Severity",
                            type = FieldType.Text(),
                            defaultValue = FieldValue.TextValue("Medium")))))
    mockTemplateRepository.setTemplates("project123", listOf(template))

    viewModel = createViewModel()
    viewModel.loadTemplatesForProject("project123")
    advanceUntilIdle()

    viewModel.selectTemplate("t1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("t1", state.templateId)
    assertEquals(template, state.selectedTemplate)
  }

  @Test
  fun selectTemplate_initializesCustomDataWithDefaults() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Bug Report",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "severity",
                            label = "Severity",
                            type = FieldType.Text(),
                            defaultValue = FieldValue.TextValue("Medium")),
                        FieldDefinition(
                            id = "priority",
                            label = "Priority",
                            type = FieldType.Number(),
                            defaultValue = FieldValue.NumberValue(5.0)))))
    mockTemplateRepository.setTemplates("project123", listOf(template))

    viewModel = createViewModel()
    viewModel.loadTemplatesForProject("project123")
    advanceUntilIdle()

    viewModel.selectTemplate("t1")
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(FieldValue.TextValue("Medium"), state.customData.getValue("severity"))
    assertEquals(FieldValue.NumberValue(5.0), state.customData.getValue("priority"))
  }

  @Test
  fun selectTemplate_withNullId_clearsSelection() = runTest {
    val template = TaskTemplate(templateID = "t1", title = "Bug Report")
    mockTemplateRepository.setTemplates("project123", listOf(template))

    viewModel = createViewModel()
    viewModel.loadTemplatesForProject("project123")
    viewModel.selectTemplate("t1")
    advanceUntilIdle()

    viewModel.selectTemplate(null)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(null, state.templateId)
    assertEquals(null, state.selectedTemplate)
    assertEquals(TaskCustomData(), state.customData)
  }

  @Test
  fun updateCustomFieldValue_updatesFieldValue() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Bug Report",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "severity", label = "Severity", type = FieldType.Text()))))
    mockTemplateRepository.setTemplates("project123", listOf(template))

    viewModel = createViewModel()
    viewModel.loadTemplatesForProject("project123")
    viewModel.selectTemplate("t1")
    advanceUntilIdle()

    viewModel.updateCustomFieldValue("severity", FieldValue.TextValue("High"))
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(FieldValue.TextValue("High"), state.customData.getValue("severity"))
  }

  @Test
  fun addTask_includesTemplateIdAndCustomData() = runTest {
    val template =
        TaskTemplate(
            templateID = "t1",
            title = "Bug Report",
            definedFields =
                TaskTemplateSchema(
                    listOf(
                        FieldDefinition(
                            id = "severity",
                            label = "Severity",
                            type = FieldType.Text(),
                            defaultValue = FieldValue.TextValue("Medium")))))
    mockTemplateRepository.setTemplates("project123", listOf(template))

    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    viewModel.loadTemplatesForProject("project123")
    advanceUntilIdle()

    viewModel.selectTemplate("t1")
    viewModel.updateCustomFieldValue("severity", FieldValue.TextValue("High"))
    advanceUntilIdle()

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertEquals("t1", createdTask.templateId)
    assertEquals(FieldValue.TextValue("High"), createdTask.customData.getValue("severity"))
  }

  @Test
  fun addTask_withNoTemplate_createsTaskWithEmptyTemplateId() = runTest {
    viewModel = createViewModel(getCurrentUserId = { "test-user-123" })
    viewModel.setProjectId("project123")
    viewModel.setTitle("Test Task")
    viewModel.setDescription("Test Description")
    viewModel.setDueDate("01/01/2025")
    advanceUntilIdle()

    viewModel.addTask(mockContext)
    advanceUntilIdle()

    assertTrue(mockTaskRepository.createTaskCalls.isNotEmpty())
    val createdTask = mockTaskRepository.createTaskCalls[0]
    assertEquals("", createdTask.templateId)
    assertEquals(TaskCustomData(), createdTask.customData)
  }

  private class MockTaskTemplateRepository : TaskTemplateRepository {
    private val templatesFlow = MutableStateFlow<List<TaskTemplate>>(emptyList())
    private val templatesByProject = mutableMapOf<String, MutableStateFlow<List<TaskTemplate>>>()

    fun setTemplates(projectId: String, templates: List<TaskTemplate>) {
      val flow = templatesByProject.getOrPut(projectId) { MutableStateFlow(emptyList()) }
      flow.value = templates
    }

    fun reset() {
      templatesFlow.value = emptyList()
      templatesByProject.clear()
    }

    override fun getTemplateById(projectId: String, templateId: String): Flow<TaskTemplate?> {
      return flowOf(templatesByProject[projectId]?.value?.find { it.templateID == templateId })
    }

    override fun getTemplatesInProject(projectId: String): Flow<List<TaskTemplate>> {
      return templatesByProject.getOrPut(projectId) { MutableStateFlow(emptyList()) }
    }

    override suspend fun createTemplate(template: TaskTemplate): Result<String> {
      return Result.success(template.templateID)
    }

    override suspend fun updateTemplate(template: TaskTemplate): Result<Unit> {
      return Result.success(Unit)
    }

    override suspend fun deleteTemplate(projectId: String, templateId: String): Result<Unit> {
      return Result.success(Unit)
    }
  }
}
