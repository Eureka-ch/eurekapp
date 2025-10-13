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
 * Note: Some of these tests were co-authored by Claude Code.
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
  fun createMeeting_shouldCreateMeetingInFirestore() = runBlocking {
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

    val result = repository.createMeeting(meeting1, testUserId, "host")

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
  fun getMeetingById_shouldReturnMeetingWhenExists() = runBlocking {
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
    repository.createMeeting(meeting2, testUserId, "host")

    val flow = repository.getMeetingById(projectId, "meeting2")
    val retrievedMeeting = flow.first()

    assertNotNull(retrievedMeeting)
    assertEquals(meeting2.meetingID, retrievedMeeting?.meetingID)
    assertEquals(meeting2.title, retrievedMeeting?.title)
  }

  @Test
  fun getMeetingById_shouldReturnNullWhenMeetingDoesNotExist() = runBlocking {
    val projectId = "project_meeting_3"
    setupTestProject(projectId)

    val flow = repository.getMeetingById(projectId, "non_existent_meeting")
    val retrievedMeeting = flow.first()

    assertNull(retrievedMeeting)
  }

  @Test
  fun getMeetingsInProject_shouldReturnAllMeetings() = runBlocking {
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
    repository.createMeeting(meeting3, testUserId, "host")
    repository.createMeeting(meeting4, testUserId, "participant")

    val flow = repository.getMeetingsInProject(projectId)
    val meetings = flow.first()

    assertEquals(2, meetings.size)
    assertTrue(meetings.any { it.meetingID == "meeting3" })
    assertTrue(meetings.any { it.meetingID == "meeting4" })
  }

  @Test
  fun getMeetingsInProject_shouldReturnEmptyListWhenNoMeetings() = runBlocking {
    val projectId = "project_meeting_5"
    setupTestProject(projectId)

    val flow = repository.getMeetingsInProject(projectId)
    val meetings = flow.first()

    assertTrue(meetings.isEmpty())
  }

  @Test
  fun getMeetingsForTask_shouldReturnMeetingsForSpecificTask() = runBlocking {
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
    repository.createMeeting(meeting5, testUserId, "host")
    repository.createMeeting(meeting6, testUserId, "host")

    val flow = repository.getMeetingsForTask(projectId, "task1")
    val meetings = flow.first()

    assertEquals(1, meetings.size)
    assertEquals("meeting5", meetings[0].meetingID)
    assertEquals("task1", meetings[0].taskId)
  }

  @Test
  fun getMeetingsForCurrentUser_shouldReturnMeetingsWhereUserIsParticipant() = runBlocking {
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
    repository.createMeeting(meeting7, testUserId, "host")

    // Create meeting8 with testUser as creator but remove them as participant
    repository.createMeeting(meeting8, testUserId, "host")
    repository.removeParticipant(projectId, "meeting8", testUserId)

    val flow = repository.getMeetingsForCurrentUser(projectId)
    val meetings = flow.first()

    assertEquals(1, meetings.size)
    assertEquals("meeting7", meetings[0].meetingID)
  }

  @Test
  fun getMeetingsForCurrentUser_shouldReturnEmptyListWhenUserNotInAnyMeeting() = runBlocking {
    val projectId = "project_meeting_8"
    setupTestProject(projectId)

    val flow = repository.getMeetingsForCurrentUser(projectId)
    val meetings = flow.first()

    assertTrue(meetings.isEmpty())
  }

  @Test
  fun updateMeeting_shouldUpdateMeetingDetails() = runBlocking {
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
    repository.createMeeting(meeting9, testUserId, "host")

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
  fun deleteMeeting_shouldDeleteMeetingFromFirestore() = runBlocking {
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
    repository.createMeeting(meeting10, testUserId, "host")

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
  fun addParticipant_shouldAddParticipantToMeeting() = runBlocking {
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
    repository.createMeeting(meeting11, testUserId, "host")

    val newParticipantId = "newParticipant123"
    val result = repository.addParticipant(projectId, "meeting11", newParticipantId, "participant")

    assertTrue(result.isSuccess)

    val participants = repository.getParticipants(projectId, "meeting11").first()
    assertEquals(2, participants.size)
    assertTrue(
        participants.any { it.userId == newParticipantId && it.role == MeetingRole.PARTICIPANT })
  }

  @Test
  fun removeParticipant_shouldRemoveParticipantFromMeeting() = runBlocking {
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
    repository.createMeeting(meeting12, testUserId, "host")
    repository.addParticipant(projectId, "meeting12", participantId, "participant")

    val result = repository.removeParticipant(projectId, "meeting12", participantId)

    assertTrue(result.isSuccess)

    val participants = repository.getParticipants(projectId, "meeting12").first()
    assertEquals(1, participants.size)
    assertTrue(participants.all { it.userId == testUserId })
  }

  @Test
  fun updateParticipantRole_shouldUpdateRole() = runBlocking {
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
    repository.createMeeting(meeting13, testUserId, "host")
    repository.addParticipant(projectId, "meeting13", participantId, "participant")

    val result = repository.updateParticipantRole(projectId, "meeting13", participantId, "host")

    assertTrue(result.isSuccess)

    val participants = repository.getParticipants(projectId, "meeting13").first()
    val updatedParticipant = participants.find { it.userId == participantId }
    assertNotNull(updatedParticipant)
    assertEquals(MeetingRole.HOST, updatedParticipant?.role)
  }
}
