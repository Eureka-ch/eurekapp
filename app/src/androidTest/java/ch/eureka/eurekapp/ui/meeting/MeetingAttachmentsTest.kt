package ch.eureka.eurekapp.ui.meeting
//Portions of this code were generated using the help of Gemini 3 Pro
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import ch.eureka.eurekapp.model.downloads.DownloadedFile
import ch.eureka.eurekapp.model.downloads.DownloadedFileDao
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MeetingAttachmentsTest {

    @get:Rule val composeTestRule = createComposeRule()

    private val mockViewModel: MeetingAttachmentsViewModel = mockk(relaxed = true)
    private val mockDao: DownloadedFileDao = mockk(relaxed = true)

    // Updated to use Map instead of Set
    private val downloadingFilesFlow = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    private val uploadingFileFlow = MutableStateFlow(false)

    @Before
    fun setup() {
        // Wire up the StateFlows
        every { mockViewModel.downloadingFileStateUrlToBoolean } returns downloadingFilesFlow
        every { mockViewModel.isUploadingFile } returns uploadingFileFlow
        every { mockViewModel.attachmentUrlsToFileNames } returns
                MutableStateFlow(mapOf("someUrl" to "file.txt"))
        every { mockViewModel.downloadedFiles } returns MutableStateFlow<List<DownloadedFile>>(emptyList()).asStateFlow()

        // Mock the filename getter logic
        every { mockViewModel.getFilenameFromDownloadURL(any()) } answers
                {
                    val url = firstArg<String>()
                    "TEST_FILE_${url.takeLast(5)}"
                }
    }

    @Test
    fun attachmentsSection_emptyList_showsNoAttachmentsMessage() {
        val meeting = createDummyMeeting(attachments = emptyList())

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.ATTACHMENT_ITEM)
            .assertIsNotDisplayed()
    }

    @Test
    fun attachmentsSection_filePicker_showsButton_whenNotUploading() {
        val meeting = createDummyMeeting()
        uploadingFileFlow.value = false

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_BUTTON)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_PROGRESS_INDICATOR)
            .assertIsNotDisplayed()
    }

    @Test
    fun attachmentsSection_filePicker_showsProgress_whenUploading() {
        val meeting = createDummyMeeting()
        uploadingFileFlow.value = true

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_BUTTON)
            .assertIsNotDisplayed()

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_PROGRESS_INDICATOR)
            .assertIsDisplayed()
    }

    @Test
    fun attachmentsSection_populatedList_showsItemsAndHidesEmptyMessage() {
        val attachments = listOf("http://test.com/file1", "http://test.com/file2")
        val meeting = createDummyMeeting(attachments = attachments)

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        composeTestRule
            .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
            .assertIsNotDisplayed()
    }

    @Test
    fun attachmentItem_clickDelete_callsViewModel() {
        val url = "http://test.com/file1"
        val meeting = createDummyMeeting(attachments = listOf(url))

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        val deleteTag = AttachmentItemTestTags.deleteButtonAttachmentTestTag(url)

        composeTestRule.onNodeWithTag(deleteTag).performClick()

        verify {
            mockViewModel.deleteFileFromMeetingAttachments(
                projectId = meeting.projectId,
                meetingId = meeting.meetingID,
                downloadUrl = url,
                onFailure = any(),
                onSuccess = any())
        }
    }

    @Test
    fun attachmentItem_clickDownload_callsViewModel() {
        val url = "http://test.com/file1"
        val meeting = createDummyMeeting(attachments = listOf(url))
        downloadingFilesFlow.value = emptyMap()

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        val downloadButtonTag = AttachmentItemTestTags.downloadButtonAttachmentTestTag(url)

        composeTestRule.onNodeWithTag(downloadButtonTag).performClick()

        // Updated parameter order: url comes first, then context
        verify {
            mockViewModel.downloadFileToPhone(
                url = url,
                context = any(),
                onSuccess = any(),
                onFailure = any())
        }
    }

    @Test
    fun attachmentItem_isDownloading_showsProgressIndicator() {
        val url = "http://test.com/file1"
        val meeting = createDummyMeeting(attachments = listOf(url))

        // Updated to use Map with true value
        downloadingFilesFlow.value = mapOf(url to true)

        composeTestRule.setContent {
            AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
        }

        val downloadButtonTag = AttachmentItemTestTags.downloadButtonAttachmentTestTag(url)
        composeTestRule.onNodeWithTag(downloadButtonTag).assertIsNotDisplayed()

        val progressTag = AttachmentItemTestTags.downloadButtonCircularProgressIndicatorTestTag(url)
        composeTestRule.onNodeWithTag(progressTag).assertIsDisplayed()
    }

    private fun createDummyMeeting(
        projectId: String = "p1",
        meetingId: String = "m1",
        attachments: List<String> = emptyList()
    ): Meeting {
        return Meeting(
            projectId = projectId,
            meetingID = meetingId,
            title = "Test Meeting",
            attachmentUrls = attachments,
            datetime = Timestamp.now(),
            duration = 60,
            status = MeetingStatus.SCHEDULED,
            format = MeetingFormat.VIRTUAL,
            location = null,
            link = "http://zoom.us",
        )
    }
}