package ch.eureka.eurekapp.model.data.meeting

import junit.framework.TestCase.*

/**
 * Test suite for MeetingFormatVote model.
 *
 * Note: Some of these tests were co-authored by chatGPT.
 */
class MeetingFormatVoteTest {

  @org.junit.Test
  fun testDefaultConstructorValues() {
    val vote = MeetingFormatVote()

    assertEquals("", vote.userId)
    assertEquals(MeetingFormat.IN_PERSON, vote.vote)
  }

  @org.junit.Test
  fun testFullConstructorAndProperties() {
    val vote = MeetingFormatVote("user123", MeetingFormat.VIRTUAL)

    assertEquals("user123", vote.userId)
    assertEquals(MeetingFormat.VIRTUAL, vote.vote)
  }

  @org.junit.Test
  fun testCopyAndEquality() {
    val v1 = MeetingFormatVote("u1", MeetingFormat.IN_PERSON)
    val v2 = v1.copy(vote = MeetingFormat.VIRTUAL)

    assertNotSame(v1, v2)
    assertFalse(v1 == v2)
    assertTrue(v1.hashCode() != v2.hashCode())
    assertEquals("u1", v2.userId)
    assertEquals(MeetingFormat.VIRTUAL, v2.vote)
  }

  @org.junit.Test
  fun testToStringAndComponents() {
    val v = MeetingFormatVote("u5", MeetingFormat.VIRTUAL)

    val str = v.toString()
    assertTrue(str.contains("u5"))
    assertTrue(str.contains("VIRTUAL"))

    // Component functions
    assertEquals("u5", v.component1())
    assertEquals(MeetingFormat.VIRTUAL, v.component2())
  }
}
