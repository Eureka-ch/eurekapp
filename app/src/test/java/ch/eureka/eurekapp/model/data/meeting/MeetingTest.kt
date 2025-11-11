package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.data.map.Location
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for Meeting model.
 *
 * Note: Some of these tests were co-authored by Claude Code and chatGPT. Updated by Gemini to
 * support List<DateTimeVote>.
 */
class MeetingTest {

  @Test
  fun meeting_defaultConstructor_createsEmptyMeeting() {
    val meeting = Meeting()

    assertEquals("", meeting.meetingID)
    assertEquals("", meeting.projectId)
    assertNull(meeting.taskId)
    assertEquals("", meeting.title)
    assertEquals(MeetingStatus.OPEN_TO_VOTES, meeting.status)
    assertEquals(30, meeting.duration)
    assertEquals(emptyList<String>(), meeting.attachmentUrls)
    assertTrue(meeting.dateTimeVotes.isEmpty()) // This now checks an empty List
    assertTrue(meeting.formatVotes.isEmpty())
    assertNotNull(meeting.datetime) // Default is Timestamp.now()
    assertNull(meeting.format)
    assertNull(meeting.location)
    assertNull(meeting.link)
    assertEquals("", meeting.createdBy)
    assertTrue(meeting.participantIds.isEmpty())
  }

  @Test
  fun meeting_withParameters_setsCorrectValues() {
    val attachments = listOf("url1", "url2")
    val testTimestamp = Timestamp.now()
    val testDuration = 60

    val meeting =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED,
            attachmentUrls = attachments,
            duration = testDuration,
            datetime = testTimestamp)

    assertEquals("mtg123", meeting.meetingID)
    assertEquals("prj123", meeting.projectId)
    assertEquals("task123", meeting.taskId)
    assertEquals("Sprint Planning", meeting.title)
    assertEquals(MeetingStatus.SCHEDULED, meeting.status)
    assertEquals(attachments, meeting.attachmentUrls)
    assertEquals(testTimestamp, meeting.datetime)
    assertEquals(testDuration, meeting.duration)
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
    assertNotSame(meeting, copiedMeeting)
  }

  @Test
  fun meeting_equals_comparesCorrectly() {
    val fixedTimestamp = Timestamp(Date(0))
    val fixedDuration = 60
    val meeting1 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            datetime = fixedTimestamp,
            duration = fixedDuration,
            status = MeetingStatus.SCHEDULED)
    val meeting2 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            datetime = fixedTimestamp,
            duration = fixedDuration,
            status = MeetingStatus.SCHEDULED)
    val meeting3 =
        Meeting(
            meetingID = "mtg456",
            projectId = "prj456",
            taskId = null,
            title = "Retrospective",
            datetime = fixedTimestamp,
            duration = fixedDuration,
            status = MeetingStatus.COMPLETED)

    assertEquals(meeting1, meeting2)
    assertNotEquals(meeting1, meeting3)
  }

  @Test
  fun meeting_hashCode_isConsistent() {
    val fixedTimestamp = Timestamp(Date(0))
    val fixedDuration = 60
    val meeting1 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            datetime = fixedTimestamp,
            duration = fixedDuration,
            status = MeetingStatus.SCHEDULED)
    val meeting2 =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            taskId = "task123",
            title = "Sprint Planning",
            datetime = fixedTimestamp,
            duration = fixedDuration,
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

    assertTrue(meetingString.contains("mtg123"))
    assertTrue(meetingString.contains("prj123"))
    assertTrue(meetingString.contains("Sprint Planning"))
    assertTrue(meetingString.contains("duration=30"))
  }

  @Test
  fun testFullConstructorAndPropertyValues() {
    val timestamp = Timestamp.now()
    val location = Location(latitude = 46.0, longitude = 7.0)
    val duration = 90

    // *** MODIFIED HERE ***
    // Updated to match List<DateTimeVote>
    val timestamp2 = Timestamp(Date(0))
    val dateVotes =
        listOf(
            DateTimeVote(dateTime = timestamp, voters = listOf("u1", "u2")),
            DateTimeVote(dateTime = timestamp2, voters = listOf("u3")))
    // *** END MODIFICATION ***

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
            createdBy = "user123",
            participantIds = listOf("u1", "u2"),
            duration = duration,
            dateTimeVotes = dateVotes, // Assign new list
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
    assertEquals("user123", meeting.createdBy)
    assertEquals(listOf("u1", "u2"), meeting.participantIds)
    assertEquals(duration, meeting.duration)
    assertEquals(dateVotes, meeting.dateTimeVotes) // Check new list
    assertEquals(formatVotes, meeting.formatVotes)
  }

  @Test
  fun testCopyAndEquality() {
    val m1 = Meeting(meetingID = "id1", title = "A")
    val m2 = m1.copy(title = "B")

    assertNotSame(m1, m2)
    assertEquals("id1", m2.meetingID)
    assertEquals("B", m2.title)
    assertNotEquals(m1, m2)
    assertNotEquals(m1.hashCode(), m2.hashCode())
  }

  @Test
  fun testToStringAndComponents() {
    val meeting = Meeting(meetingID = "id123", projectId = "proj1", title = "Kickoff")

    val str = meeting.toString()
    assertTrue(str.contains("id123"))
    assertTrue(str.contains("proj1"))
    assertTrue(str.contains("Kickoff"))

    // Test all component functions for 100% data class coverage
    assertEquals("id123", meeting.component1()) // meetingID
    assertEquals("proj1", meeting.component2()) // projectId
    assertNull(meeting.component3()) // taskId
    assertEquals("Kickoff", meeting.component4()) // title
    assertEquals(MeetingStatus.OPEN_TO_VOTES, meeting.component5()) // status
    assertEquals(30, meeting.component6()) // duration

    // *** MODIFIED HERE ***
    assertEquals(emptyList<DateTimeVote>(), meeting.component7()) // dateTimeVotes
    // *** END MODIFICATION ***

    assertEquals(emptyList<MeetingFormatVote>(), meeting.component8()) // formatVotes
    assertNotNull(meeting.component9()) // datetime
    assertNull(meeting.component10()) // format
    assertNull(meeting.component11()) // location
    assertNull(meeting.component12()) // link
    assertEquals(emptyList<String>(), meeting.component13()) // attachmentUrls
    assertNull(meeting.component14())
    assertNull(meeting.component15())
    assertEquals("", meeting.component16()) // createdBy
    assertEquals(emptyList<String>(), meeting.component17()) // participantIds
  }
}
