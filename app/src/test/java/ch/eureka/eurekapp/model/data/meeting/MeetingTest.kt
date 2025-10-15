package ch.eureka.eurekapp.model.data.meeting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Test suite for Meeting model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class MeetingTest {

  @Test
  fun meeting_defaultConstructor_createsEmptyMeeting() {
    val meeting = Meeting()

    assertEquals("", meeting.meetingID)
    assertEquals("", meeting.projectId)
    assertNull(meeting.taskId)
    assertEquals("", meeting.title)
    assertEquals(MeetingStatus.SCHEDULED, meeting.status)
    assertEquals(emptyList<String>(), meeting.attachmentUrls)
  }

  @Test
  fun meeting_withParameters_setsCorrectValues() {
    val attachments = listOf("url1", "url2")
    val meeting =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = attachments)

    assertEquals("mtg123", meeting.meetingID)
    assertEquals("prj123", meeting.projectId)
    assertEquals("task123", meeting.taskId)
    assertEquals("Sprint Planning", meeting.title)
    assertEquals(MeetingStatus.SCHEDULED, meeting.status)
    assertEquals(attachments, meeting.attachmentUrls)
  }

  @Test
  fun meeting_withoutTaskId_setsNullTaskId() {
    val meeting =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)

    assertNull(meeting.taskId)
  }

  @Test
  fun meeting_copy_createsNewInstance() {
    val meeting =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)
    val copiedMeeting = meeting.copy(status = MeetingStatus.IN_PROGRESS)

    assertEquals("mtg123", copiedMeeting.meetingID)
    assertEquals("prj123", copiedMeeting.projectId)
    assertEquals("Sprint Planning", copiedMeeting.title)
    assertEquals(MeetingStatus.IN_PROGRESS, copiedMeeting.status)
  }

  @Test
  fun meeting_equals_comparesCorrectly() {
    val meeting1 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)
    val meeting2 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)
    val meeting3 =
        Meeting(
            meetingID = "mtg456",
            projectId = "prj456",
            taskId = null,
            title = "Retrospective",
            status = MeetingStatus.COMPLETED)

    assertEquals(meeting1, meeting2)
    assertNotEquals(meeting1, meeting3)
  }

  @Test
  fun meeting_hashCode_isConsistent() {
    val meeting1 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)
    val meeting2 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)

    assertEquals(meeting1.hashCode(), meeting2.hashCode())
  }

  @Test
  fun meeting_toString_containsAllFields() {
    val meeting =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)
    val meetingString = meeting.toString()

    assert(meetingString.contains("mtg123"))
    assert(meetingString.contains("prj123"))
    assert(meetingString.contains("Sprint Planning"))
  }
}
