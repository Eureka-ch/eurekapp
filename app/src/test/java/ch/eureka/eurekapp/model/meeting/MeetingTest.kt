package ch.eureka.eurekapp.model.meeting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MeetingTest {

  @Test
  fun meeting_defaultConstructor_createsEmptyMeeting() {
    val meeting = Meeting()

    assertEquals("", meeting.meetingID)
    assertEquals("", meeting.workspaceId)
    assertEquals("", meeting.contextId)
    assertEquals(ContextType.WORKSPACE, meeting.contextType)
    assertEquals("", meeting.title)
    assertEquals("", meeting.status)
    assertEquals(emptyMap<String, String>(), meeting.participants)
    assertEquals(emptyList<String>(), meeting.attachmentUrls)
  }

  @Test
  fun meeting_withParameters_setsCorrectValues() {
    val participants = mapOf("user1" to "host", "user2" to "participant")
    val attachments = listOf("url1", "url2")
    val meeting =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            contextType = ContextType.PROJECT,
            title = "Sprint Planning",
            status = "scheduled",
            participants = participants,
            attachmentUrls = attachments)

    assertEquals("mtg123", meeting.meetingID)
    assertEquals("ws123", meeting.workspaceId)
    assertEquals("ctx123", meeting.contextId)
    assertEquals(ContextType.PROJECT, meeting.contextType)
    assertEquals("Sprint Planning", meeting.title)
    assertEquals("scheduled", meeting.status)
    assertEquals(participants, meeting.participants)
    assertEquals(attachments, meeting.attachmentUrls)
  }

  @Test
  fun meeting_copy_createsNewInstance() {
    val meeting =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            title = "Sprint Planning",
            status = "scheduled")
    val copiedMeeting = meeting.copy(status = "in_progress")

    assertEquals("mtg123", copiedMeeting.meetingID)
    assertEquals("ws123", copiedMeeting.workspaceId)
    assertEquals("Sprint Planning", copiedMeeting.title)
    assertEquals("in_progress", copiedMeeting.status)
  }

  @Test
  fun meeting_equals_comparesCorrectly() {
    val meeting1 =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            title = "Sprint Planning",
            status = "scheduled")
    val meeting2 =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            title = "Sprint Planning",
            status = "scheduled")
    val meeting3 =
        Meeting(
            meetingID = "mtg456",
            workspaceId = "ws456",
            contextId = "ctx456",
            title = "Retrospective",
            status = "completed")

    assertEquals(meeting1, meeting2)
    assertNotEquals(meeting1, meeting3)
  }

  @Test
  fun meeting_hashCode_isConsistent() {
    val meeting1 =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            title = "Sprint Planning",
            status = "scheduled")
    val meeting2 =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            title = "Sprint Planning",
            status = "scheduled")

    assertEquals(meeting1.hashCode(), meeting2.hashCode())
  }

  @Test
  fun meeting_toString_containsAllFields() {
    val meeting =
        Meeting(
            meetingID = "mtg123",
            workspaceId = "ws123",
            contextId = "ctx123",
            title = "Sprint Planning",
            status = "scheduled")
    val meetingString = meeting.toString()

    assert(meetingString.contains("mtg123"))
    assert(meetingString.contains("ws123"))
    assert(meetingString.contains("Sprint Planning"))
  }

  @Test
  fun contextType_hasAllValues() {
    val values = ContextType.values()

    assertEquals(4, values.size)
    assert(values.contains(ContextType.WORKSPACE))
    assert(values.contains(ContextType.GROUP))
    assert(values.contains(ContextType.PROJECT))
    assert(values.contains(ContextType.TASK))
  }
}
