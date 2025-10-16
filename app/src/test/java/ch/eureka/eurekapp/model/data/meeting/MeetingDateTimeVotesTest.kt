package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp
import junit.framework.TestCase.*

/**
 * Test suite for MeetingDateTimeVotes model.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class MeetingDateTimeVotesTest {

  @org.junit.Test
  fun testDefaultConstructorValues() {
    val vote = MeetingDateTimeVotes()

    assertEquals("", vote.userId)
    assertTrue(vote.votes.isEmpty())
  }

  @org.junit.Test
  fun testFullConstructorAndProperties() {
    val timestamp1 = Timestamp.now()
    val timestamp2 = Timestamp.now()
    val list = listOf(timestamp1, timestamp2)

    val vote = MeetingDateTimeVotes("user123", list)

    assertEquals("user123", vote.userId)
    assertEquals(list, vote.votes)
  }

  @org.junit.Test
  fun testCopyAndEquality() {
    val ts = Timestamp.now()
    val v1 = MeetingDateTimeVotes("u1", listOf(ts))
    val v2 = v1.copy(userId = "u2")

    assertNotSame(v1, v2)
    assertFalse(v1 == v2)
    assertTrue(v1.hashCode() != v2.hashCode())
    assertEquals("u2", v2.userId)
    assertEquals(listOf(ts), v2.votes)
  }

  @org.junit.Test
  fun testToStringAndComponents() {
    val ts = Timestamp.now()
    val v = MeetingDateTimeVotes("u5", listOf(ts))

    val str = v.toString()
    assertTrue(str.contains("u5"))
    assertTrue(str.contains("votes"))

    assertEquals("u5", v.component1())
    assertEquals(listOf(ts), v.component2())
  }
}
