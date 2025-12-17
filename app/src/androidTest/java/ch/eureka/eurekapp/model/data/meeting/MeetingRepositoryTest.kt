package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

/**
 * Test suite for MeetingRepository implementation.
 *
 * Note: Some of these tests were co-authored by Claude Code, and Grok.
 */
class MeetingRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: MeetingRepository
  private val testProjectId = "project_meeting_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("projects/$testProjectId/meetings")
  }

  @Before
  override fun setup() = runBlocking {
    super.setup()
    repository =
        FirestoreMeetingRepository(
            firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)
  }

  @Test
  fun meetingRepository_shouldCreateMeetingInFirestore() = runBlocking {
    val projectId = "project_meeting_1"
    setupTestProject(projectId)

    val meeting1 =
        Meeting(
            meetingID = "meeting1",
            projectId = projectId,
            taskId = null,
            title = "Team Meeting",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)

    val result = repository.createMeeting(meeting1, testUserId, MeetingRole.HOST)

    assertTrue(result.isSuccess)
    assertEquals("meeting1", result.getOrNull())

    val savedMeeting =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("meetings")
            .document("meeting1")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNotNull(savedMeeting)
    assertEquals(meeting1.meetingID, savedMeeting?.meetingID)
    assertEquals(meeting1.title, savedMeeting?.title)
    assertEquals(meeting1.status, savedMeeting?.status)

    val participants = repository.getParticipants(projectId, "meeting1").first()
    assertEquals(1, participants.size)
    assertEquals(testUserId, participants[0].userId)
    assertEquals(MeetingRole.HOST, participants[0].role)
  }

  @Test
  fun meetingRepository_shouldReturnMeetingWhenExists() = runBlocking {
    val projectId = "project_meeting_2"
    setupTestProject(projectId)

    val meeting2 =
        Meeting(
            meetingID = "meeting2",
            projectId = projectId,
            taskId = null,
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting2, testUserId, MeetingRole.HOST)

    val flow = repository.getMeetingById(projectId, "meeting2")
    val retrievedMeeting = flow.first()

    assertNotNull(retrievedMeeting)
    assertEquals(meeting2.meetingID, retrievedMeeting?.meetingID)
    assertEquals(meeting2.title, retrievedMeeting?.title)
  }

  @Test
  fun meetingRepository_shouldReturnNullWhenMeetingDoesNotExist() = runBlocking {
    val projectId = "project_meeting_3"
    setupTestProject(projectId)

    val flow = repository.getMeetingById(projectId, "non_existent_meeting")
    val retrievedMeeting = flow.first()

    assertNull(retrievedMeeting)
  }

  @Test
  fun meetingRepository_shouldReturnAllMeetings() = runBlocking {
    val projectId = "project_meeting_4"
    setupTestProject(projectId)

    val meeting3 =
        Meeting(
            meetingID = "meeting3",
            projectId = projectId,
            taskId = null,
            title = "Meeting 3",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    val meeting4 =
        Meeting(
            meetingID = "meeting4",
            projectId = projectId,
            taskId = "task1",
            title = "Meeting 4",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting3, testUserId, MeetingRole.HOST)
    repository.createMeeting(meeting4, testUserId, MeetingRole.PARTICIPANT)

    val flow = repository.getMeetingsInProject(projectId)
    val meetings = flow.first()

    assertEquals(2, meetings.size)
    assertTrue(meetings.any { it.meetingID == "meeting3" })
    assertTrue(meetings.any { it.meetingID == "meeting4" })
  }

  @Test
  fun meetingRepository_shouldReturnEmptyListWhenNoMeetings() = runBlocking {
    val projectId = "project_meeting_5"
    setupTestProject(projectId)

    val flow = repository.getMeetingsInProject(projectId)
    val meetings = flow.first()

    assertTrue(meetings.isEmpty())
  }

  @Test
  fun meetingRepository_shouldReturnMeetingsForSpecificTask() = runBlocking {
    val projectId = "project_meeting_6"
    setupTestProject(projectId)

    val meeting5 =
        Meeting(
            meetingID = "meeting5",
            projectId = projectId,
            taskId = "task1",
            title = "Task Meeting 1",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    val meeting6 =
        Meeting(
            meetingID = "meeting6",
            projectId = projectId,
            taskId = "task2",
            title = "Task Meeting 2",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting5, testUserId, MeetingRole.HOST)
    repository.createMeeting(meeting6, testUserId, MeetingRole.HOST)

    val flow = repository.getMeetingsForTask(projectId, "task1")
    val meetings = flow.first()

    assertEquals(1, meetings.size)
    assertEquals("meeting5", meetings[0].meetingID)
    assertEquals("task1", meetings[0].taskId)
  }

  @Test
  fun meetingRepository_shouldReturnMeetingsWhereUserIsParticipant() = runBlocking {
    val projectId = "project_meeting_7"
    setupTestProject(projectId)

    val meeting7 =
        Meeting(
            meetingID = "meeting7",
            projectId = projectId,
            taskId = null,
            title = "My Meeting",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    val meeting8 =
        Meeting(
            meetingID = "meeting8",
            projectId = projectId,
            taskId = null,
            title = "Other Meeting",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)

    // Create meeting7 with testUser as participant
    repository.createMeeting(meeting7, testUserId, MeetingRole.HOST)

    // Create meeting8 with testUser as creator but remove them as participant
    repository.createMeeting(meeting8, testUserId, MeetingRole.HOST)
    repository.removeParticipant(projectId, "meeting8", testUserId)

    val flow = repository.getMeetingsForCurrentUser(projectId, skipCache = false)
    val meetings = flow.first()

    assertEquals(1, meetings.size)
    assertEquals("meeting7", meetings[0].meetingID)
  }
  // TODO find a good fix
  /*@Test
  fun getMeetingsForCurrentUser_shouldReturnEmptyListWhenUserNotInAnyMeeting() = runBlocking {
    val projectId = "project_meeting_8"
    setupTestProject(projectId)

    val flow = repository.getMeetingsForCurrentUser(projectId)

    // When skipCache is true and there's no data, the flow won't emit anything
    // So we expect a timeout
    var timedOut = false
    try {
      withTimeout(2000) { flow.first() }
    } catch (e: TimeoutCancellationException) {
      timedOut = true
    }

    assertTrue(timedOut)
  }*/

  @Test
  fun meetingRepository_shouldUpdateMeetingDetails() = runBlocking {
    val projectId = "project_meeting_9"
    setupTestProject(projectId)

    val meeting9 =
        Meeting(
            meetingID = "meeting9",
            projectId = projectId,
            taskId = null,
            title = "Original Title",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting9, testUserId, MeetingRole.HOST)

    val updatedMeeting = meeting9.copy(title = "Updated Title", status = MeetingStatus.IN_PROGRESS)
    val result = repository.updateMeeting(updatedMeeting)

    assertTrue(result.isSuccess)

    val savedMeeting =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("meetings")
            .document("meeting9")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNotNull(savedMeeting)
    assertEquals("Updated Title", savedMeeting?.title)
    assertEquals(MeetingStatus.IN_PROGRESS, savedMeeting?.status)
  }

  @Test
  fun meetingRepository_shouldDeleteMeetingFromFirestore() = runBlocking {
    val projectId = "project_meeting_10"
    setupTestProject(projectId)

    val meeting10 =
        Meeting(
            meetingID = "meeting10",
            projectId = projectId,
            taskId = null,
            title = "To Delete",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting10, testUserId, MeetingRole.HOST)

    val result = repository.deleteMeeting(projectId, "meeting10")

    assertTrue(result.isSuccess)

    val deletedMeeting =
        FirebaseEmulator.firestore
            .collection("projects")
            .document(projectId)
            .collection("meetings")
            .document("meeting10")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNull(deletedMeeting)
  }

  @Test
  fun meetingRepository_shouldAddParticipantToMeeting() = runBlocking {
    val projectId = "project_meeting_11"
    setupTestProject(projectId)

    val meeting11 =
        Meeting(
            meetingID = "meeting11",
            projectId = projectId,
            taskId = null,
            title = "Meeting 11",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting11, testUserId, MeetingRole.HOST)

    val newParticipantId = "newParticipant123"
    val result =
        repository.addParticipant(projectId, "meeting11", newParticipantId, MeetingRole.PARTICIPANT)

    assertTrue(result.isSuccess)

    val participants = repository.getParticipants(projectId, "meeting11").first()
    assertEquals(2, participants.size)
    assertTrue(
        participants.any { it.userId == newParticipantId && it.role == MeetingRole.PARTICIPANT })
  }

  @Test
  fun meetingRepository_shouldRemoveParticipantFromMeeting() = runBlocking {
    val projectId = "project_meeting_12"
    setupTestProject(projectId)

    val participantId = "participant456"
    val meeting12 =
        Meeting(
            meetingID = "meeting12",
            projectId = projectId,
            taskId = null,
            title = "Meeting 12",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting12, testUserId, MeetingRole.HOST)
    repository.addParticipant(projectId, "meeting12", participantId, MeetingRole.PARTICIPANT)

    val result = repository.removeParticipant(projectId, "meeting12", participantId)

    assertTrue(result.isSuccess)

    val participants = repository.getParticipants(projectId, "meeting12").first()
    assertEquals(1, participants.size)
    assertTrue(participants.all { it.userId == testUserId })
  }

  @Test
  fun meetingRepository_shouldUpdateRole() = runBlocking {
    val projectId = "project_meeting_13"
    setupTestProject(projectId)

    val participantId = "participant789"
    val meeting13 =
        Meeting(
            meetingID = "meeting13",
            projectId = projectId,
            taskId = null,
            title = "Meeting 13",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)
    repository.createMeeting(meeting13, testUserId, MeetingRole.HOST)
    repository.addParticipant(projectId, "meeting13", participantId, MeetingRole.PARTICIPANT)

    val result =
        repository.updateParticipantRole(projectId, "meeting13", participantId, MeetingRole.HOST)

    assertTrue(result.isSuccess)

    val participants = repository.getParticipants(projectId, "meeting13").first()
    val updatedParticipant = participants.find { it.userId == participantId }
    assertNotNull(updatedParticipant)
    assertEquals(MeetingRole.HOST, updatedParticipant?.role)
  }

  @Test
  fun meetingRepository_shouldUseDefaultHostRole() = runBlocking {
    val projectId = "project_meeting_14"
    setupTestProject(projectId)

    val meeting14 =
        Meeting(
            meetingID = "meeting14",
            projectId = projectId,
            taskId = null,
            title = "Meeting with Default Role",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = emptyList(),
            createdBy = testUserId)

    // Call without specifying role - should default to "host"
    val result = repository.createMeeting(meeting14, testUserId)

    assertTrue(result.isSuccess)
    assertEquals("meeting14", result.getOrNull())

    val participants = repository.getParticipants(projectId, "meeting14").first()
    assertEquals(1, participants.size)
    assertEquals(testUserId, participants[0].userId)
    assertEquals(MeetingRole.HOST, participants[0].role)
  }
}
