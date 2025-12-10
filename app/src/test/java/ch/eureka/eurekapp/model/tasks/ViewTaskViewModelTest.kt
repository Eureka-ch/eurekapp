package ch.eureka.eurekapp.model.tasks

import android.net.Uri
import ch.eureka.eurekapp.model.connection.ConnectivityObserver
import ch.eureka.eurekapp.model.data.task.Task
import ch.eureka.eurekapp.model.data.task.TaskCustomData
import ch.eureka.eurekapp.model.data.task.TaskStatus
import ch.eureka.eurekapp.model.data.template.TaskTemplate
import ch.eureka.eurekapp.model.data.template.TaskTemplateRepository
import ch.eureka.eurekapp.model.data.template.TaskTemplateSchema
import ch.eureka.eurekapp.model.downloads.DownloadService
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
import com.google.firebase.Timestamp
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/*
Portions of this code were generated with the help of Grok.
Note: This file was partially written by GPT-5 Codex
Co-author : GPT-5
*/

@OptIn(ExperimentalCoroutinesApi::class)
class ViewTaskViewModelTest {

  private val testDispatcher = UnconfinedTestDispatcher()

  private lateinit var mockTaskRepository: ch.eureka.eurekapp.model.data.task.TaskRepository
  private lateinit var mockTemplateRepository: TaskTemplateRepository
  private lateinit var mockUserRepository: ch.eureka.eurekapp.model.data.user.UserRepository
  private lateinit var mockConnectivityObserver: ConnectivityObserver
  private lateinit var mockDownloadedFileDao: DownloadedFileDao
  private lateinit var viewModel: ViewTaskViewModel

  @Before
  fun setUp() {
    Dispatchers.setMain(testDispatcher)
    mockTaskRepository = mockk()
    mockTemplateRepository = mockk()
    every { mockTemplateRepository.getTemplateById(any(), any()) } returns flowOf(null)
    mockConnectivityObserver = mockk()
    every { mockConnectivityObserver.isConnected } returns flowOf(true)
    mockUserRepository = mockk()
    mockDownloadedFileDao = mockk()
    every { mockDownloadedFileDao.getAll() } returns flowOf(emptyList())
  }

  @After
  fun tearDown() {
    Dispatchers.resetMain()
  }

  @Test
  fun viewModelWithValidTaskEmitsCorrectState() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("url1", "url2"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("Test Task", state.title)
    assertEquals("Test Description", state.description)
    assertEquals(projectId, state.projectId)
    assertEquals(taskId, state.taskId)
    assertEquals(listOf("url1", "url2"), state.attachmentUrls)
    assertEquals(TaskStatus.TODO, state.status)
    assertFalse(state.isLoading)
    assertNull(state.errorMsg)
    assertEquals(emptyList<ch.eureka.eurekapp.model.data.user.User>(), state.assignedUsers)
  }

  @Test
  fun viewModelWithNullTaskEmitsErrorState() = runTest {
    val projectId = "project123"
    val taskId = "task123"

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(null)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.dueDate)
    assertEquals("", state.projectId)
    assertEquals("", state.taskId)
    assertTrue(state.attachmentUrls.isEmpty())
    assertFalse(state.isLoading)
    assertEquals("Task not found.", state.errorMsg)
  }

  @Test
  fun viewModelWithExceptionEmitsErrorState() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val exception = Exception("Network error")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flow { throw exception }

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.title)
    assertEquals("", state.description)
    assertEquals("", state.dueDate)
    assertEquals("", state.projectId)
    assertEquals("", state.taskId)
    assertTrue(state.attachmentUrls.isEmpty())
    assertFalse(state.isLoading)
    assertEquals("Failed to load Task: Network error", state.errorMsg)
  }

  // This test checks that when a task has no due date, the ViewModel emits an empty string for
  // dueDate.
  // It seems to me like the cleanest solution for this case.
  // The main goal of this test is to ensure that the ViewModel won't crash or misbehave.
  @Test
  fun viewModelWithTaskWithoutDueDateEmitsEmptyDueDate() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = null,
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals("", state.dueDate)
  }

  @Test
  fun viewModelWithSingleAssignedUserLoadsUserCorrectly() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val userId = "user1"
    val user =
        ch.eureka.eurekapp.model.data.user.User(
            uid = userId, email = "user1@example.com", displayName = "User One")

    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = listOf(userId),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockUserRepository.getUserById(userId) } returns flowOf(user)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.assignedUsers.size)
    assertEquals(userId, state.assignedUsers[0].uid)
    assertEquals("User One", state.assignedUsers[0].displayName)
    assertEquals("user1@example.com", state.assignedUsers[0].email)
  }

  @Test
  fun viewModelWithMultipleAssignedUsersLoadsAllUsersCorrectly() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val user1Id = "user1"
    val user2Id = "user2"
    val user3Id = "user3"

    val user1 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user1Id, email = "user1@example.com", displayName = "User One")
    val user2 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user2Id, email = "user2@example.com", displayName = "User Two")
    val user3 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user3Id, email = "user3@example.com", displayName = "User Three")

    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = listOf(user1Id, user2Id, user3Id),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockUserRepository.getUserById(user1Id) } returns flowOf(user1)
    every { mockUserRepository.getUserById(user2Id) } returns flowOf(user2)
    every { mockUserRepository.getUserById(user3Id) } returns flowOf(user3)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(3, state.assignedUsers.size)
    assertEquals("User One", state.assignedUsers[0].displayName)
    assertEquals("User Two", state.assignedUsers[1].displayName)
    assertEquals("User Three", state.assignedUsers[2].displayName)
  }

  @Test
  fun viewModelWithNullUserFiltersOutNulls() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val user1Id = "user1"
    val user2Id = "user2"

    val user1 =
        ch.eureka.eurekapp.model.data.user.User(
            uid = user1Id, email = "user1@example.com", displayName = "User One")

    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = listOf(user1Id, user2Id),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockUserRepository.getUserById(user1Id) } returns flowOf(user1)
    every { mockUserRepository.getUserById(user2Id) } returns flowOf(null)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(1, state.assignedUsers.size)
    assertEquals("User One", state.assignedUsers[0].displayName)
  }

  @Test
  fun downloadFile_alreadyDownloaded_skipsInsert() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("http://example.com/file.pdf"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    coEvery { mockDownloadedFileDao.isDownloaded("http://example.com/file.pdf") } returns true
    coEvery { mockDownloadedFileDao.insert(any()) } returns Unit

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val mockContext = mockk<android.content.Context>(relaxed = true)
    viewModel.downloadFile("http://example.com/file.pdf", "file.pdf", mockContext)
    advanceUntilIdle()

    coVerify(exactly = 0) { mockDownloadedFileDao.insert(any()) }
  }

  @Test
  fun uiState_offlineMode_withDownloadedFiles_setsDownloadedUrls() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("http://example.com/file1.pdf", "http://example.com/file2.pdf"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    val downloadedFile =
        DownloadedFile(
            url = "http://example.com/file1.pdf",
            localPath = "file:///local/file1.pdf",
            fileName = "file1.pdf")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockDownloadedFileDao.getAll() } returns flowOf(listOf(downloadedFile))

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(setOf("http://example.com/file1.pdf"), state.downloadedAttachmentUrls)
    assertEquals(listOf("http://example.com/file2.pdf"), state.urlsToDownload)
  }

  @Test
  fun uiState_loadsTemplateWhenTaskHasTemplateId() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val templateId = "template123"
    val template =
        TaskTemplate(
            templateID = templateId,
            projectId = projectId,
            title = "Test Template",
            description = "Template Description",
            definedFields = TaskTemplateSchema(emptyList()),
            createdBy = "user1")
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            templateId = templateId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = emptyList(),
            status = TaskStatus.TODO,
            customData = TaskCustomData(),
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockTemplateRepository.getTemplateById(projectId, templateId) } returns flowOf(template)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertEquals(template, state.selectedTemplate)
    assertEquals("Test Template", state.selectedTemplate?.title)
  }

  @Test
  fun uiState_onlineMode_showsRemoteAttachments() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("http://example.com/file1.pdf", "http://example.com/file2.pdf"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    every { mockConnectivityObserver.isConnected } returns flowOf(true)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    val state = viewModel.uiState.first()
    assertTrue(state.isConnected)
    assertEquals(2, state.effectiveAttachments.size)
    assertTrue(state.effectiveAttachments.all { it is Attachment.Remote })
  }

  @Test
  fun downloadAllAttachments_downloadsMultipleFiles() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val url1 = "http://example.com/file1.pdf"
    val url2 = "http://example.com/file2.pdf"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls =
                listOf("$url1|file1.pdf|application/pdf", "$url2|file2.pdf|application/pdf"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    coEvery { mockDownloadedFileDao.isDownloaded(any()) } returns false
    coEvery { mockDownloadedFileDao.insert(any()) } returns Unit

    mockkConstructor(DownloadService::class)
    val mockUri1 = mockk<Uri>()
    val mockUri2 = mockk<Uri>()
    every { mockUri1.toString() } returns "file:///local/file1.pdf"
    every { mockUri2.toString() } returns "file:///local/file2.pdf"
    coEvery { anyConstructed<DownloadService>().downloadFile(url1, "file1.pdf") } returns
        Result.success(mockUri1)
    coEvery { anyConstructed<DownloadService>().downloadFile(url2, "file2.pdf") } returns
        Result.success(mockUri2)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    // Ensure uiState is activated and contains the task attachment metadata before downloading
    viewModel.uiState.first()

    val mockContext = mockk<android.content.Context>(relaxed = true)
    viewModel.downloadAllAttachments(listOf(url1, url2), mockContext)
    advanceUntilIdle()

    coVerify(exactly = 1) {
      mockDownloadedFileDao.insert(match { it.url == url1 && it.fileName == "file1.pdf" })
    }
    coVerify(exactly = 1) {
      mockDownloadedFileDao.insert(match { it.url == url2 && it.fileName == "file2.pdf" })
    }
  }

  @Test
  fun downloadAllAttachments_skipsAlreadyDownloadedFiles() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val url1 = "http://example.com/file1.pdf"
    val url2 = "http://example.com/file2.pdf"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls =
                listOf("$url1|file1.pdf|application/pdf", "$url2|file2.pdf|application/pdf"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    coEvery { mockDownloadedFileDao.isDownloaded(url1) } returns true
    coEvery { mockDownloadedFileDao.isDownloaded(url2) } returns false
    coEvery { mockDownloadedFileDao.insert(any()) } returns Unit

    mockkConstructor(DownloadService::class)
    val mockUri2 = mockk<Uri>()
    every { mockUri2.toString() } returns "file:///local/file2.pdf"
    coEvery { anyConstructed<DownloadService>().downloadFile(url2, "file2.pdf") } returns
        Result.success(mockUri2)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    // Ensure uiState is activated and contains the task attachment metadata before downloading
    viewModel.uiState.first()

    val mockContext = mockk<android.content.Context>(relaxed = true)
    viewModel.downloadAllAttachments(listOf(url1, url2), mockContext)
    advanceUntilIdle()

    coVerify(exactly = 0) { mockDownloadedFileDao.insert(match { it.url == url1 }) }
    coVerify(exactly = 1) { mockDownloadedFileDao.insert(match { it.url == url2 }) }
  }

  @Test
  fun downloadAllAttachments_extractsDisplayNameFromMetadata() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val url = "http://example.com/file.pdf"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf("$url|CustomName.pdf|application/pdf"),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    coEvery { mockDownloadedFileDao.isDownloaded(any()) } returns false
    coEvery { mockDownloadedFileDao.insert(any()) } returns Unit

    mockkConstructor(DownloadService::class)
    val mockUri = mockk<Uri>()
    every { mockUri.toString() } returns "file:///local/CustomName.pdf"
    coEvery { anyConstructed<DownloadService>().downloadFile(url, "CustomName.pdf") } returns
        Result.success(mockUri)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    // Ensure uiState is activated and contains the task attachment metadata before downloading
    viewModel.uiState.first()

    val mockContext = mockk<android.content.Context>(relaxed = true)
    viewModel.downloadAllAttachments(listOf(url), mockContext)
    advanceUntilIdle()

    coVerify { anyConstructed<DownloadService>().downloadFile(url, "CustomName.pdf") }
    coVerify {
      mockDownloadedFileDao.insert(match { it.fileName == "CustomName.pdf" && it.url == url })
    }
  }

  @Test
  fun downloadAllAttachments_usesUrlFilenameWhenNoMetadata() = runTest {
    val projectId = "project123"
    val taskId = "task123"
    val url = "http://example.com/document.pdf"
    val task =
        Task(
            taskID = taskId,
            projectId = projectId,
            title = "Test Task",
            description = "Test Description",
            assignedUserIds = emptyList(),
            dueDate = Timestamp.now(),
            attachmentUrls = listOf(url),
            status = TaskStatus.TODO,
            createdBy = "user1")

    every { mockTaskRepository.getTaskById(projectId, taskId) } returns flowOf(task)
    coEvery { mockDownloadedFileDao.isDownloaded(any()) } returns false
    coEvery { mockDownloadedFileDao.insert(any()) } returns Unit

    mockkConstructor(DownloadService::class)
    val mockUri = mockk<Uri>()
    every { mockUri.toString() } returns "file:///local/document.pdf"
    coEvery { anyConstructed<DownloadService>().downloadFile(url, "document.pdf") } returns
        Result.success(mockUri)

    viewModel =
        ViewTaskViewModel(
            projectId,
            taskId,
            mockDownloadedFileDao,
            mockTaskRepository,
            mockTemplateRepository,
            mockConnectivityObserver,
            mockUserRepository,
            testDispatcher)
    advanceUntilIdle()

    // Ensure uiState is activated before downloading so fallback filename logic runs against
    // updated state
    viewModel.uiState.first()

    val mockContext = mockk<android.content.Context>(relaxed = true)
    viewModel.downloadAllAttachments(listOf(url), mockContext)
    advanceUntilIdle()

    coVerify { anyConstructed<DownloadService>().downloadFile(url, "document.pdf") }
    coVerify { mockDownloadedFileDao.insert(match { it.fileName == "document.pdf" }) }
  }
}
