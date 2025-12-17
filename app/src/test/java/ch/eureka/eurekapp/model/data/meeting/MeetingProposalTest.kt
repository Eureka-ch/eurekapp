package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp
import java.util.Date
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Test suite for the MeetingProposal data class.
 *
 * Note : some tests were generated with the help of Gemini
 */
class MeetingProposalTest {

  // Helper data for tests
  private val testVote1 = MeetingProposalVote(userId = "user1")
  private val testVote2 = MeetingProposalVote(userId = "user2")
  private val testVoteList = listOf(testVote1, testVote2)

  @Test
  fun meetingProposal_defaultConstructor() {
    val proposal = MeetingProposal()

    assertNotNull(proposal.dateTime)
    assertEquals(0, proposal.votes.size)
    assertTrue(proposal.votes.isEmpty())
  }

  @Test
  fun meetingProposal_withParameters() {
    val timestamp = Timestamp(Date(123456789L))
    val proposal = MeetingProposal(dateTime = timestamp, votes = testVoteList)

    assertEquals(timestamp, proposal.dateTime)
    assertEquals(2, proposal.votes.size)
    assertEquals(testVoteList, proposal.votes)
  }

  @Test
  fun meetingProposal_copy() {
    val timestamp = Timestamp(Date(0L))
    val initialVotes = listOf(testVote1)
    val proposal1 = MeetingProposal(dateTime = timestamp, votes = initialVotes)

    val updatedVotes = listOf(testVote1, testVote2)
    val proposal2 = proposal1.copy(votes = updatedVotes)

    assertNotSame(proposal1, proposal2)
    assertEquals(timestamp, proposal2.dateTime)
    assertEquals(2, proposal2.votes.size)
    assertEquals(updatedVotes, proposal2.votes)
  }

  @Test
  fun meetingProposal_equals() {
    val timestamp = Timestamp(Date(1000L))
    val proposal1 = MeetingProposal(dateTime = timestamp, votes = listOf(testVote1))
    val proposal2 = MeetingProposal(dateTime = timestamp, votes = listOf(testVote1))
    val proposal3 = MeetingProposal(dateTime = timestamp, votes = testVoteList)

    assertEquals(proposal1, proposal2)
    assertNotEquals(proposal1, proposal3)
  }

  @Test
  fun meetingProposal_hashCode() {
    val timestamp = Timestamp(Date(1000L))
    val proposal1 = MeetingProposal(dateTime = timestamp, votes = listOf(testVote1))
    val proposal2 = MeetingProposal(dateTime = timestamp, votes = listOf(testVote1))

    assertEquals(proposal1.hashCode(), proposal2.hashCode())
  }

  @Test
  fun meetingProposal_toString() {
    val timestamp = Timestamp(Date(0L))
    val proposal = MeetingProposal(dateTime = timestamp, votes = testVoteList)
    val proposalString = proposal.toString()

    assertTrue(proposalString.contains("dateTime=Timestamp(seconds=0, nanoseconds=0)"))
    assertTrue(proposalString.contains("votes=$testVoteList"))
  }

  @Test
  fun meetingProposal_components() {
    val timestamp = Timestamp.now()
    val proposal = MeetingProposal(dateTime = timestamp, votes = testVoteList)

    val (dt, vs) = proposal

    assertEquals(timestamp, dt)
    assertEquals(testVoteList, vs)

    // Explicitly test components
    assertEquals(timestamp, proposal.component1())
    assertEquals(testVoteList, proposal.component2())
  }
}
