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
  fun participant_shouldHaveCorrectDefaultValues() {
    val participant = Participant()
    assertEquals("", participant.userId)
    assertEquals("", participant.role)
  }

  @Test
  fun participant_shouldInitializeWithProvidedValues() {
    val participant = Participant(userId = "user456", role = "host")
    assertEquals("user456", participant.userId)
    assertEquals("host", participant.role)
  }

  @Test
  fun participant_shouldSupportCopy() {
    val participant = Participant(userId = "user456", role = "host")
    val copiedParticipant = participant.copy(role = "participant")
    assertEquals("user456", copiedParticipant.userId)
    assertEquals("participant", copiedParticipant.role)
    assertEquals("host", participant.role)
  }

  @Test
  fun participant_shouldSupportEquality() {
    val participant1 = Participant(userId = "user456", role = "host")
    val participant2 = Participant(userId = "user456", role = "host")
    assertEquals(participant1, participant2)
  }

  @Test
  fun participant_shouldSupportHashCode() {
    val participant1 = Participant(userId = "user456", role = "host")
    val participant2 = Participant(userId = "user456", role = "host")
    assertEquals(participant1.hashCode(), participant2.hashCode())
  }

  @Test
  fun participant_shouldSupportToString() {
    val participant = Participant(userId = "user456", role = "host")
    val string = participant.toString()
    assert(string.contains("user456"))
    assert(string.contains("host"))
  }

  @Test
  fun participant_differentInstances_shouldNotBeSame() {
    val participant1 = Participant(userId = "user456", role = "host")
    val participant2 = Participant(userId = "user456", role = "host")
    assertNotSame(participant1, participant2)
  }

  @Test
  fun participant_shouldSupportComponentFunctions() {
    val participant = Participant(userId = "user456", role = "host")
    val (userId, role) = participant
    assertEquals("user456", userId)
    assertEquals("host", role)
  }
}
