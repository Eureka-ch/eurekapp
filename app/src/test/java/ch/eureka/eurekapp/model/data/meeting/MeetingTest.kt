package ch.eureka.eurekapp.model.data.meeting

import ch.eureka.eurekapp.model.map.Location
import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for Meeting model.
 *
 * Note: Some of these tests were co-authored by Claude Code, chatGPT and Gemini.
 */
class MeetingTest {

  @Test
  fun meeting_defaultConstructor() {
    val meeting = Meeting()

    assertEquals("", meeting.meetingID)
    assertEquals("", meeting.projectId)
    assertNull(meeting.taskId)
    assertEquals("", meeting.title)
    assertEquals(MeetingStatus.OPEN_TO_VOTES, meeting.status)
    assertEquals(30, meeting.duration)
    assertEquals(emptyList<String>(), meeting.attachmentUrls)
    assertTrue(meeting.meetingProposals.isEmpty())
    assertNull(meeting.datetime)
    assertNull(meeting.format)
    assertNull(meeting.location)
    assertNull(meeting.link)
    assertNull(meeting.audioUrl)
    assertNull(meeting.transcriptId)
    assertEquals("", meeting.createdBy)
    assertTrue(meeting.participantIds.isEmpty())
  }

  @Test
  fun meeting_withParameters() {
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
  fun meeting_withoutTaskId() {
    val meeting =
        Meeting(
            meetingID = "mtg123",
            projectId = "prj123",
            title = "Sprint Planning",
            status = MeetingStatus.SCHEDULED)

    assertNull(meeting.taskId)
  }

  @Test
  fun meeting_copy() {
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
  fun meeting_equals() {
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
  fun meeting_hashCode() {
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
  fun meeting_toString() {
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
    assertTrue(meetingString.contains("audioUrl=null"))
    assertTrue(meetingString.contains("transcriptId=null"))
  }

  @Test
  fun meeting_fullConstructorAndPropertyValues() {
    val timestamp = Timestamp.now()
    val location = Location(latitude = 46.0, longitude = 7.0)
    val duration = 90

    val timestamp2 = Timestamp(Date(0))
    val vote1 = MeetingProposalVote(userId = "u1")
    val vote2 = MeetingProposalVote(userId = "u2")
    val proposals =
        listOf(
            MeetingProposal(dateTime = timestamp, votes = listOf(vote1)),
            MeetingProposal(dateTime = timestamp2, votes = listOf(vote2)))

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
            audioUrl = "https://audio.mp3",
            transcriptId = "transcript-abc",
            createdBy = "user123",
            participantIds = listOf("u1", "u2"),
            duration = duration,
            meetingProposals = proposals)

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
    assertEquals("https://audio.mp3", meeting.audioUrl)
    assertEquals("transcript-abc", meeting.transcriptId)
    assertEquals("user123", meeting.createdBy)
    assertEquals(listOf("u1", "u2"), meeting.participantIds)
    assertEquals(duration, meeting.duration)
    assertEquals(proposals, meeting.meetingProposals)
  }

  @Test
  fun meeting_copyAndEquality() {
    val m1 = Meeting(meetingID = "id1", title = "A")
    val m2 = m1.copy(title = "B")

    assertNotSame(m1, m2)
    assertEquals("id1", m2.meetingID)
    assertEquals("B", m2.title)
    assertNotEquals(m1, m2)
    assertNotEquals(m1.hashCode(), m2.hashCode())
  }

  @Test
  fun meeting_toStringAndComponents() {
    val meeting = Meeting(meetingID = "id123", projectId = "proj1", title = "Kickoff")

    val str = meeting.toString()
    assertTrue(str.contains("id123"))
    assertTrue(str.contains("proj1"))
    assertTrue(str.contains("Kickoff"))

    assertEquals("id123", meeting.component1())
    assertEquals("proj1", meeting.component2())
    assertNull(meeting.component3())
    assertEquals("Kickoff", meeting.component4())
    assertEquals(MeetingStatus.OPEN_TO_VOTES, meeting.component5())
    assertEquals(30, meeting.component6())
    assertEquals(emptyList<MeetingProposal>(), meeting.component7())
    assertNull(meeting.component8())
    assertNull(meeting.component9())
    assertNull(meeting.component10())
    assertNull(meeting.component11())
    assertEquals(emptyList<String>(), meeting.component12())
    assertNull(meeting.component13())
    assertNull(meeting.component14())
    assertEquals("", meeting.component15())
    assertEquals(emptyList<String>(), meeting.component16())
  }
}
