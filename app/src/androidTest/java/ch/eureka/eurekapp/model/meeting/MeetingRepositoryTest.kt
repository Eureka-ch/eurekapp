package ch.eureka.eurekapp.model.meeting

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

class MeetingRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: MeetingRepository
  private val testWorkspaceId = "workspace_meeting_test"

  override fun getCollectionPaths(): List<String> {
    return listOf("workspaces/$testWorkspaceId/meetings")
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
    val meeting =
        Meeting(
            meetingID = "meeting1",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Team Meeting",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())

    val result = repository.createMeeting(meeting)

    assertTrue(result.isSuccess)
    assertEquals("meeting1", result.getOrNull())

    val savedMeeting =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("meetings")
            .document("meeting1")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNotNull(savedMeeting)
    assertEquals(meeting.meetingID, savedMeeting?.meetingID)
    assertEquals(meeting.title, savedMeeting?.title)
    assertEquals(meeting.status, savedMeeting?.status)
  }

  @Test
  fun getMeetingById_shouldReturnMeetingWhenExists() = runBlocking {
    val meeting =
        Meeting(
            meetingID = "meeting2",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Sprint Planning",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting)

    val flow = repository.getMeetingById(testWorkspaceId, "meeting2")
    val retrievedMeeting = flow.first()

    assertNotNull(retrievedMeeting)
    assertEquals(meeting.meetingID, retrievedMeeting?.meetingID)
    assertEquals(meeting.title, retrievedMeeting?.title)
  }

  @Test
  fun getMeetingById_shouldReturnNullWhenMeetingDoesNotExist() = runBlocking {
    val flow = repository.getMeetingById(testWorkspaceId, "non_existent_meeting")
    val retrievedMeeting = flow.first()

    assertNull(retrievedMeeting)
  }

  @Test
  fun getMeetingsInWorkspace_shouldReturnAllMeetings() = runBlocking {
    val meeting1 =
        Meeting(
            meetingID = "meeting3",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Meeting 3",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    val meeting2 =
        Meeting(
            meetingID = "meeting4",
            workspaceId = testWorkspaceId,
            contextId = "group1",
            contextType = ContextType.GROUP,
            title = "Meeting 4",
            status = "scheduled",
            participants = mapOf(testUserId to "participant"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting1)
    repository.createMeeting(meeting2)

    val flow = repository.getMeetingsInWorkspace(testWorkspaceId)
    val meetings = flow.first()

    assertEquals(2, meetings.size)
    assertTrue(meetings.any { it.meetingID == "meeting3" })
    assertTrue(meetings.any { it.meetingID == "meeting4" })
  }

  @Test
  fun getMeetingsInWorkspace_shouldReturnEmptyListWhenNoMeetings() = runBlocking {
    val flow = repository.getMeetingsInWorkspace(testWorkspaceId)
    val meetings = flow.first()

    assertTrue(meetings.isEmpty())
  }

  @Test
  fun getMeetingsForContext_shouldReturnMeetingsForSpecificContext() = runBlocking {
    val meeting1 =
        Meeting(
            meetingID = "meeting5",
            workspaceId = testWorkspaceId,
            contextId = "project1",
            contextType = ContextType.PROJECT,
            title = "Project Meeting 1",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    val meeting2 =
        Meeting(
            meetingID = "meeting6",
            workspaceId = testWorkspaceId,
            contextId = "project2",
            contextType = ContextType.PROJECT,
            title = "Project Meeting 2",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting1)
    repository.createMeeting(meeting2)

    val flow = repository.getMeetingsForContext(testWorkspaceId, "project1", ContextType.PROJECT)
    val meetings = flow.first()

    assertEquals(1, meetings.size)
    assertEquals("meeting5", meetings[0].meetingID)
  }

  @Test
  fun getMeetingsForCurrentUser_shouldReturnMeetingsWhereUserIsParticipant() = runBlocking {
    val meeting1 =
        Meeting(
            meetingID = "meeting7",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "My Meeting",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    val meeting2 =
        Meeting(
            meetingID = "meeting8",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Other Meeting",
            status = "scheduled",
            participants = mapOf("otherUser" to "host"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting1)
    repository.createMeeting(meeting2)

    val flow = repository.getMeetingsForCurrentUser(testWorkspaceId)
    val meetings = flow.first()

    assertEquals(1, meetings.size)
    assertEquals("meeting7", meetings[0].meetingID)
  }

  @Test
  fun getMeetingsForCurrentUser_shouldReturnEmptyListWhenUserNotInAnyMeeting() = runBlocking {
    val flow = repository.getMeetingsForCurrentUser(testWorkspaceId)
    val meetings = flow.first()

    assertTrue(meetings.isEmpty())
  }

  @Test
  fun updateMeeting_shouldUpdateMeetingDetails() = runBlocking {
    val meeting =
        Meeting(
            meetingID = "meeting9",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Original Title",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting)

    val updatedMeeting = meeting.copy(title = "Updated Title", status = "in_progress")
    val result = repository.updateMeeting(updatedMeeting)

    assertTrue(result.isSuccess)

    val savedMeeting =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("meetings")
            .document("meeting9")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNotNull(savedMeeting)
    assertEquals("Updated Title", savedMeeting?.title)
    assertEquals("in_progress", savedMeeting?.status)
  }

  @Test
  fun deleteMeeting_shouldDeleteMeetingFromFirestore() = runBlocking {
    val meeting =
        Meeting(
            meetingID = "meeting10",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "To Delete",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting)

    val result = repository.deleteMeeting(testWorkspaceId, "meeting10")

    assertTrue(result.isSuccess)

    val deletedMeeting =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("meetings")
            .document("meeting10")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNull(deletedMeeting)
  }

  @Test
  fun addParticipant_shouldAddParticipantToMeeting() = runBlocking {
    val meeting =
        Meeting(
            meetingID = "meeting11",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Meeting 11",
            status = "scheduled",
            participants = mapOf(testUserId to "host"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting)

    val newParticipantId = "newParticipant123"
    val result =
        repository.addParticipant(testWorkspaceId, "meeting11", newParticipantId, "participant")

    assertTrue(result.isSuccess)

    val updatedMeeting =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("meetings")
            .document("meeting11")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNotNull(updatedMeeting)
    assertTrue(updatedMeeting?.participants?.containsKey(newParticipantId) == true)
    assertEquals("participant", updatedMeeting?.participants?.get(newParticipantId))
  }

  @Test
  fun removeParticipant_shouldRemoveParticipantFromMeeting() = runBlocking {
    val participantId = "participant456"
    val meeting =
        Meeting(
            meetingID = "meeting12",
            workspaceId = testWorkspaceId,
            contextId = testWorkspaceId,
            contextType = ContextType.WORKSPACE,
            title = "Meeting 12",
            status = "scheduled",
            participants = mapOf(testUserId to "host", participantId to "participant"),
            attachmentUrls = emptyList())
    repository.createMeeting(meeting)

    val result = repository.removeParticipant(testWorkspaceId, "meeting12", participantId)

    assertTrue(result.isSuccess)

    val updatedMeeting =
        FirebaseEmulator.firestore
            .collection("workspaces")
            .document(testWorkspaceId)
            .collection("meetings")
            .document("meeting12")
            .get()
            .await()
            .toObject(Meeting::class.java)

    assertNotNull(updatedMeeting)
    assertTrue(updatedMeeting?.participants?.containsKey(participantId) == false)
  }
}
