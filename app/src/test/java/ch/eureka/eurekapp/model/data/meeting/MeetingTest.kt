package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.map.Location
import com.google.firebase.Timestamp
import java.time.temporal.ChronoUnit
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for Meeting model.
 *
 * Note: Some of these tests were co-authored by Claude Code and chatGPT.
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

  @Test
  fun testDefaultConstructorValues() {
    val meeting = Meeting()

    assertEquals("", meeting.meetingID)
    assertEquals("", meeting.projectId)
    assertNull(meeting.taskId)
    assertEquals("", meeting.title)
    assertNotNull(meeting.datetime)
    assertNull(meeting.location)
    assertEquals(MeetingStatus.SCHEDULED, meeting.status)
    assertNull(meeting.format)
    assertNull(meeting.link)
    assertTrue(meeting.attachmentUrls.isEmpty())
    assertFalse(meeting.ended)
    assertEquals("", meeting.createdBy)
    assertTrue(meeting.participantIds.isEmpty())
    assertTrue(meeting.canVote)
    assertNull(meeting.timeSlot)
    assertTrue(meeting.dateTimeVotes.isEmpty())
    assertTrue(meeting.formatVotes.isEmpty())
  }

  @Test
  fun testFullConstructorAndPropertyValues() {
    val timestamp = Timestamp.now()
    val instant = timestamp.toDate().toInstant()
    val newInstant = instant.plus(5L, ChronoUnit.MINUTES)
    val newTimestamp = Timestamp(Date.from(newInstant))
    val location = Location(latitude = 46.0, longitude = 7.0)
    val timeSlot = TimeSlot(timestamp, newTimestamp)
    val dateVotes = listOf(MeetingDateTimeVotes("user1", listOf(timestamp)))
    val formatVotes = listOf(MeetingFormatVote("user1", MeetingFormat.VIRTUAL))

    val meeting =
        Meeting(
            meetingID = "m123",
            projectId = "p456",
            taskId = "t789",
            title = "Sprint Planning",
            datetime = timestamp,
            location = location,
            status = MeetingStatus.COMPLETED,
            format = MeetingFormat.IN_PERSON,
            link = "https://zoom.us/abc",
            attachmentUrls = listOf("url1", "url2"),
            ended = true,
            createdBy = "user123",
            participantIds = listOf("u1", "u2"),
            canVote = false,
            timeSlot = timeSlot,
            dateTimeVotes = dateVotes,
            formatVotes = formatVotes)

    assertEquals("m123", meeting.meetingID)
    assertEquals("p456", meeting.projectId)
    assertEquals("t789", meeting.taskId)
    assertEquals("Sprint Planning", meeting.title)
    assertEquals(timestamp, meeting.datetime)
    assertEquals(location, meeting.location)
    assertEquals(MeetingStatus.COMPLETED, meeting.status)
    assertEquals(MeetingFormat.IN_PERSON, meeting.format)
    assertEquals("https://zoom.us/abc", meeting.link)
    assertEquals(listOf("url1", "url2"), meeting.attachmentUrls)
    assertTrue(meeting.ended)
    assertEquals("user123", meeting.createdBy)
    assertEquals(listOf("u1", "u2"), meeting.participantIds)
    assertFalse(meeting.canVote)
    assertEquals(timeSlot, meeting.timeSlot)
    assertEquals(dateVotes, meeting.dateTimeVotes)
    assertEquals(formatVotes, meeting.formatVotes)
  }

  @Test
  fun testCopyAndEquality() {
    val m1 = Meeting(meetingID = "id1", title = "A")
    val m2 = m1.copy(title = "B")

    assertNotSame(m1, m2)
    assertEquals("id1", m2.meetingID)
    assertEquals("B", m2.title)
    assertFalse(m1 == m2)
    assertTrue(m1.hashCode() != m2.hashCode())
  }

  @Test
  fun testToStringAndComponents() {
    val meeting = Meeting(meetingID = "id123", projectId = "proj1", title = "Kickoff")

    val str = meeting.toString()
    assertTrue(str.contains("id123"))
    assertTrue(str.contains("proj1"))
    assertTrue(str.contains("Kickoff"))

    // Component functions
    assertEquals("id123", meeting.component1())
    assertEquals("proj1", meeting.component2())
    assertNull(meeting.component3()) // taskId
    assertEquals("Kickoff", meeting.component4())
  }
}
