package ch.eureka.eurekapp.ui.meeting
// Portions of this code were generated with the help of Gemini 3 Pro, and Grok.
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.eureka.eurekapp.model.data.meeting.Meeting
import ch.eureka.eurekapp.model.data.meeting.MeetingFormat
import ch.eureka.eurekapp.model.data.meeting.MeetingStatus
import com.google.firebase.Timestamp
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MeetingAttachmentsTest {

  @get:Rule val composeTestRule = createComposeRule()

  // Mock the ViewModel using Mockk
  // relaxed = true allows methods to return default values (null, 0, etc) without explicit stubbing
  private val mockViewModel: MeetingAttachmentsViewModel = mockk(relaxed = true)

  // Control the state flows to test loading/idle states
  private val downloadingFilesFlow = MutableStateFlow<Set<String>>(emptySet())
  private val uploadingFileFlow = MutableStateFlow(false)

  @Before
  fun setup() {
    // Wire up the StateFlows using Mockk syntax
    every { mockViewModel.downloadingFilesSet } returns downloadingFilesFlow
    every { mockViewModel.isUploadingFile } returns uploadingFileFlow
    every { mockViewModel.attachmentUrlsToFileNames } returns
        MutableStateFlow(mapOf("someUrl" to "file.txt"))

    // Mock the filename getter logic
    // 'answers' is equivalent to Mockito's 'thenAnswer'
    // 'firstArg()' fetches the first argument passed to the function
    every { mockViewModel.getFilenameFromDownloadURL(any()) } answers
        {
          val url = firstArg<String>()
          "TEST_FILE_${url.takeLast(5)}"
        }
  }

  // --- Tests for AttachmentsSection (Empty State & File Picker) ---

  @Test
  fun meetingAttachments_attachmentsSectionEmptyListShowsNoAttachmentsMessage() {
    val meeting = createDummyMeeting(attachments = emptyList())

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Verify "No attachments" message is visible
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
        .assertIsDisplayed()

    // Verify items are NOT displayed
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.ATTACHMENT_ITEM)
        .assertIsNotDisplayed()
  }

  @Test
  fun meetingAttachments_attachmentsSectionFilePickerShowsButtonWhenNotUploading() {
    val meeting = createDummyMeeting()
    uploadingFileFlow.value = false // Idle state

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Verify File Picker Button is visible
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_BUTTON)
        .assertIsDisplayed()

    // Verify Progress indicator is hidden
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_PROGRESS_INDICATOR)
        .assertIsNotDisplayed()
  }

  @Test
  fun meetingAttachments_attachmentsSectionFilePickerShowsProgressWhenUploading() {
    val meeting = createDummyMeeting()
    uploadingFileFlow.value = true // Uploading state

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Verify File Picker Button is hidden
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_BUTTON)
        .assertIsNotDisplayed()

    // Verify Progress indicator is visible
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.DOWNLOADING_FILES_PROGRESS_INDICATOR)
        .assertIsDisplayed()
  }

  // --- Tests for AttachmentItem (Populated List & Interactions) ---

  @Test
  fun meetingAttachments_attachmentsSectionPopulatedListShowsItemsAndHidesEmptyMessage() {
    val attachments = listOf("http://test.com/file1", "http://test.com/file2")
    val meeting = createDummyMeeting(attachments = attachments)

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Verify "No attachments" message is HIDDEN
    composeTestRule
        .onNodeWithTag(MeetingDetailScreenTestTags.NO_ATTACHMENTS_MESSAGE)
        .assertIsNotDisplayed()
  }

  @Test
  fun meetingAttachments_attachmentItemClickDeleteCallsViewModel() {
    val url = "http://test.com/file1"
    val meeting = createDummyMeeting(attachments = listOf(url))

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Generate the dynamic tag for the delete button
    val deleteTag = AttachmentItemTestTags.deleteButtonAttachmentTestTag(url)

    // Perform Click
    composeTestRule.onNodeWithTag(deleteTag).performClick()

    // Verify ViewModel interaction using Mockk verify
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
  fun meetingAttachments_attachmentItemClickDownloadCallsViewModel() {
    val url = "http://test.com/file1"
    val meeting = createDummyMeeting(attachments = listOf(url))
    downloadingFilesFlow.value = emptySet() // Not downloading yet

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Generate dynamic tag for download button
    val downloadButtonTag = AttachmentItemTestTags.downloadButtonAttachmentTestTag(url)

    // Click download
    composeTestRule.onNodeWithTag(downloadButtonTag).performClick()

    // Verify ViewModel call
    verify {
      mockViewModel.downloadFileToPhone(
          context = any(), downloadUrl = url, onSuccess = any(), onFailure = any())
    }
  }

  @Test
  fun meetingAttachments_attachmentItemIsDownloadingShowsProgressIndicator() {
    val url = "http://test.com/file1"
    val meeting = createDummyMeeting(attachments = listOf(url))

    // Set state to downloading for this specific URL
    downloadingFilesFlow.value = setOf(url)

    composeTestRule.setContent {
      AttachmentsSection(meeting = meeting, attachmentsViewModel = mockViewModel)
    }

    // Verify Download Button is hidden
    val downloadButtonTag = AttachmentItemTestTags.downloadButtonAttachmentTestTag(url)
    composeTestRule.onNodeWithTag(downloadButtonTag).assertIsNotDisplayed()

    // Verify Progress Indicator is displayed
    val progressTag = AttachmentItemTestTags.downloadButtonCircularProgressIndicatorTestTag(url)
    composeTestRule.onNodeWithTag(progressTag).assertIsDisplayed()
  }

  // --- Helper to create dummy data ---
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
