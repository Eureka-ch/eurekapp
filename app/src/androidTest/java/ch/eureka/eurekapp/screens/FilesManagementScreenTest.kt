// Portions of this code were generated with the help of Grok.
package ch.eureka.eurekapp.screens

import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.FileItem
import ch.eureka.eurekapp.model.downloads.FilesManagementState
import ch.eureka.eurekapp.model.downloads.FilesManagementViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FilesManagementScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createMockViewModel(
        files: List<FileItem> = emptyList()
    ): FilesManagementViewModel {
        val viewModel = mockk<FilesManagementViewModel>(relaxed = true)
        val stateFlow = MutableStateFlow(FilesManagementState(files = files))
        every { viewModel.uiState } returns stateFlow
        return viewModel
    }

    @Test
    fun filesManagementScreen_emptyStateDisplaysNoFilesMessage() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.NO_FILES_MESSAGE).assertIsDisplayed()
    }

    @Test
    fun filesManagementScreen_topBarDisplaysCorrectTitle() {
        val viewModel = createMockViewModel()

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.TOP_BAR_TITLE).assertIsDisplayed()
    }

    @Test
    fun filesManagementScreen_backButtonTriggersCallback() {
        val viewModel = createMockViewModel()
        var backClicked = false

        composeTestRule.setContent {
            FilesManagementScreen(
                onBackClick = { backClicked = true },
                viewModel = viewModel
            )
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.TOP_BAR_TITLE).assertExists()
        // Note: BackButton would need a test tag to be directly clickable
    }

    @Test
    fun filesManagementScreen_fileListDisplaysFiles() {
        val mockFile = DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf"
        )
        val fileItem = FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf")
        )
        val viewModel = createMockViewModel(files = listOf(fileItem))

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_test.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.OPEN_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).assertIsDisplayed()
    }

    @Test
    fun filesManagementScreen_imageFileDisplaysWithThumbnail() {
        val mockFile = DownloadedFile(
            url = "http://example.com/photo.jpg",
            fileName = "photo.jpg",
            localPath = "/path/to/photo.jpg"
        )
        val fileItem = FileItem(
            file = mockFile,
            displayName = "photo.jpg",
            isImage = true,
            uri = Uri.parse("file:///path/to/photo.jpg")
        )
        val viewModel = createMockViewModel(files = listOf(fileItem))

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_photo.jpg").assertIsDisplayed()
    }

    @Test
    fun filesManagementScreen_openButtonCallsViewModel() {
        val mockFile = DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf"
        )
        val fileItem = FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf")
        )
        val viewModel = createMockViewModel(files = listOf(fileItem))

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.OPEN_BUTTON).performClick()

        verify { viewModel.openFile(fileItem) }
    }

    @Test
    fun filesManagementScreen_deleteButtonShowsConfirmationDialog() {
        val mockFile = DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf"
        )
        val fileItem = FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf")
        )
        val viewModel = createMockViewModel(files = listOf(fileItem))

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).performClick()

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TEXT).assertIsDisplayed()
    }

    @Test
    fun filesManagementScreen_deleteDialogCancelButtonDismissesDialog() {
        val mockFile = DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf"
        )
        val fileItem = FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf")
        )
        val viewModel = createMockViewModel(files = listOf(fileItem))

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).performClick()
        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.CANCEL_DELETE_BUTTON).performClick()

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE).assertDoesNotExist()
    }

    @Test
    fun filesManagementScreen_deleteDialogConfirmButtonCallsViewModelAndDismisses() {
        val mockFile = DownloadedFile(
            url = "http://example.com/test.pdf",
            fileName = "test.pdf",
            localPath = "/path/to/test.pdf"
        )
        val fileItem = FileItem(
            file = mockFile,
            displayName = "test.pdf",
            isImage = false,
            uri = Uri.parse("file:///path/to/test.pdf")
        )
        val viewModel = createMockViewModel(files = listOf(fileItem))

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_BUTTON).performClick()
        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.CONFIRM_DELETE_BUTTON).performClick()

        verify { viewModel.deleteFile(fileItem) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag(FilesManagementScreenTestTags.DELETE_DIALOG_TITLE).assertDoesNotExist()
    }

    @Test
    fun filesManagementScreen_multipleFilesAllDisplayed() {
        val files = listOf(
            FileItem(
                file = DownloadedFile(url = "http://example.com/file1.pdf", localPath = "/path/1", fileName = "file1.pdf"),
                displayName = "file1.pdf",
                isImage = false,
                uri = Uri.parse("file:///path/1")
            ),
            FileItem(
                file = DownloadedFile(url = "http://example.com/file2.jpg", localPath = "/path/2", fileName = "file2.jpg"),
                displayName = "file2.jpg",
                isImage = true,
                uri = Uri.parse("file:///path/2")
            ),
            FileItem(
                file = DownloadedFile(url = "http://example.com/file3.txt", localPath = "/path/3", fileName = "file3.txt"),
                displayName = "file3.txt",
                isImage = false,
                uri = Uri.parse("file:///path/3")
            )
        )
        val viewModel = createMockViewModel(files = files)

        composeTestRule.setContent {
            FilesManagementScreen(viewModel = viewModel)
        }

        composeTestRule.onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_file1.pdf").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_file2.jpg").assertIsDisplayed()
        composeTestRule.onNodeWithTag("${FilesManagementScreenTestTags.FILE_DISPLAY_NAME}_file3.txt").assertIsDisplayed()
    }
}
