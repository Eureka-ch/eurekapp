// Portions of this file were generated with the help of Claude (Sonnet 4.5).
package ch.eureka.eurekapp.model.data.meeting

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotSame
import org.junit.Test

/**
 * Test suite for Participant model.
 *
 * Note: Some of these tests were co-authored by Claude Code.
 */
class ParticipantTest {

  @Test
  fun participant_hasCorrectDefaultValues() {
    val participant = Participant()
    assertEquals("", participant.userId)
    assertEquals(MeetingRole.PARTICIPANT, participant.role)
  }

  @Test
  fun participant_initializesWithProvidedValues() {
    val participant = Participant(userId = "user456", role = MeetingRole.HOST)
    assertEquals("user456", participant.userId)
    assertEquals(MeetingRole.HOST, participant.role)
  }

  @Test
  fun participant_supportsCopy() {
    val participant = Participant(userId = "user456", role = MeetingRole.HOST)
    val copiedParticipant = participant.copy(role = MeetingRole.PARTICIPANT)
    assertEquals("user456", copiedParticipant.userId)
    assertEquals(MeetingRole.PARTICIPANT, copiedParticipant.role)
    assertEquals(MeetingRole.HOST, participant.role)
  }

  @Test
  fun participant_supportsEquality() {
    val participant1 = Participant(userId = "user456", role = MeetingRole.HOST)
    val participant2 = Participant(userId = "user456", role = MeetingRole.HOST)
    assertEquals(participant1, participant2)
  }

  @Test
  fun participant_supportsHashCode() {
    val participant1 = Participant(userId = "user456", role = MeetingRole.HOST)
    val participant2 = Participant(userId = "user456", role = MeetingRole.HOST)
    assertEquals(participant1.hashCode(), participant2.hashCode())
  }

  @Test
  fun participant_supportsToString() {
    val participant = Participant(userId = "user456", role = MeetingRole.HOST)
    val string = participant.toString()
    assert(string.contains("user456"))
    assert(string.contains("HOST"))
  }

  @Test
  fun participant_differentInstancesAreNotSame() {
    val participant1 = Participant(userId = "user456", role = MeetingRole.HOST)
    val participant2 = Participant(userId = "user456", role = MeetingRole.HOST)
    assertNotSame(participant1, participant2)
  }

  @Test
  fun participant_supportsComponentFunctions() {
    val participant = Participant(userId = "user456", role = MeetingRole.HOST)
    val (userId, role) = participant
    assertEquals("user456", userId)
    assertEquals(MeetingRole.HOST, role)
  }
}
