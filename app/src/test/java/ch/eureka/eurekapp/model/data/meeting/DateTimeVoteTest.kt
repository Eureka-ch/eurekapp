package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Test suite for the DateTimeVote data class. */
class DateTimeVoteTest {

  @Test
  fun dateTimeVote_defaultConstructor_createsDefaultVote() {
    val vote = DateTimeVote()

    assertNotNull(vote.dateTime) // Default is Timestamp.now()
    assertEquals(0, vote.votes)
    assertTrue(vote.voters.isEmpty())
  }

  @Test
  fun dateTimeVote_withParameters_setsCorrectValues() {
    val timestamp = Timestamp(Date(123456789L))
    val voters = listOf("user1", "user2")
    val vote = DateTimeVote(dateTime = timestamp, votes = 2, voters = voters)

    assertEquals(timestamp, vote.dateTime)
    assertEquals(2, vote.votes)
    assertEquals(voters, vote.voters)
  }

  @Test
  fun dateTimeVote_copy_createsNewInstance() {
    val timestamp = Timestamp(Date(0L))
    val vote1 = DateTimeVote(dateTime = timestamp, votes = 1, voters = listOf("user1"))
    val vote2 = vote1.copy(votes = 2, voters = listOf("user1", "user2"))

    assertNotSame(vote1, vote2)
    assertEquals(timestamp, vote2.dateTime)
    assertEquals(2, vote2.votes)
    assertEquals(listOf("user1", "user2"), vote2.voters)
  }

  @Test
  fun dateTimeVote_equals_comparesCorrectly() {
    val timestamp = Timestamp(Date(1000L))
    val vote1 = DateTimeVote(dateTime = timestamp, votes = 1, voters = listOf("user1"))
    val vote2 = DateTimeVote(dateTime = timestamp, votes = 1, voters = listOf("user1"))
    val vote3 = DateTimeVote(dateTime = timestamp, votes = 2, voters = listOf("user1", "user2"))

    assertEquals(vote1, vote2)
    assertNotEquals(vote1, vote3)
  }

  @Test
  fun dateTimeVote_hashCode_isConsistent() {
    val timestamp = Timestamp(Date(1000L))
    val vote1 = DateTimeVote(dateTime = timestamp, votes = 1, voters = listOf("user1"))
    val vote2 = DateTimeVote(dateTime = timestamp, votes = 1, voters = listOf("user1"))

    assertEquals(vote1.hashCode(), vote2.hashCode())
  }

  @Test
  fun dateTimeVote_toString_containsAllFields() {
    val timestamp = Timestamp(Date(0L))
    val vote = DateTimeVote(dateTime = timestamp, votes = 5, voters = listOf("u1"))
    val voteString = vote.toString()

    assertTrue(voteString.contains("dateTime=Timestamp(seconds=0, nanoseconds=0)"))
    assertTrue(voteString.contains("votes=5"))
    assertTrue(voteString.contains("voters=[u1]"))
  }

  @Test
  fun dateTimeVote_components_returnCorrectValues() {
    val timestamp = Timestamp.now()
    val voters = listOf("a", "b")
    val vote = DateTimeVote(dateTime = timestamp, votes = 2, voters = voters)

    assertEquals(timestamp, vote.component1())
    assertEquals(2, vote.component2())
    assertEquals(voters, vote.component3())
  }
}
