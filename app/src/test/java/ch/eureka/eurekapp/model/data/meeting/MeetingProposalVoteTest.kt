package ch.eureka.eurekapp.model.data.meeting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for [MeetingProposalVote]
 *
 * Note : some tests were generated with the help of Gemini
 */
class MeetingProposalVoteTest {

  private lateinit var vote: MeetingProposalVote
  private val testUserId = "user-123"
  private val testPreferences = listOf(MeetingFormat.IN_PERSON, MeetingFormat.VIRTUAL)

  @Before
  fun setUp() {
    vote = MeetingProposalVote(userId = testUserId, formatPreferences = testPreferences)
  }

  @Test
  fun testDefaultConstructorValues() {
    val defaultVote = MeetingProposalVote()

    assertEquals("User ID should be an empty string by default", "", defaultVote.userId)
    assertTrue(
        "Format preferences should be an empty list by default",
        defaultVote.formatPreferences.isEmpty())
  }

  @Test
  fun testParameterizedConstructorAndGetters() {
    assertEquals(testUserId, vote.userId)
    assertEquals(testPreferences, vote.formatPreferences)
  }

  @Test
  fun testEqualsForIdenticalObjects() {
    val vote1 = MeetingProposalVote(testUserId, testPreferences)
    val vote2 = MeetingProposalVote(testUserId, testPreferences)

    assertEquals("Two instances with the same data should be equal", vote1, vote2)
  }

  @Test
  fun testEqualsForDifferentObjects() {
    val differentVoteId = MeetingProposalVote("user-456", testPreferences)
    val differentVotePrefs = MeetingProposalVote(testUserId, listOf(MeetingFormat.VIRTUAL))

    assertNotEquals("Objects with different userId should not be equal", vote, differentVoteId)
    assertNotEquals(
        "Objects with different formatPreferences should not be equal", vote, differentVotePrefs)
  }

  @Test
  fun testHashCodeForEqualObjects() {
    val vote1 = MeetingProposalVote(testUserId, testPreferences)
    val vote2 = MeetingProposalVote(testUserId, testPreferences)

    assertEquals(
        "Hash codes for equal objects must be the same", vote1.hashCode(), vote2.hashCode())
  }

  @Test
  fun testHashCodeForUnequalObjects() {
    val differentVote = MeetingProposalVote("user-456", testPreferences)

    assertNotEquals(
        "Hash codes for unequal objects should (usually) be different",
        vote.hashCode(),
        differentVote.hashCode())
  }

  @Test
  fun testToStringFormatting() {
    val voteString = vote.toString()

    assertTrue(voteString.startsWith("MeetingProposalVote("))
    assertTrue(voteString.contains("userId=$testUserId"))
    assertTrue(voteString.contains("formatPreferences=$testPreferences"))
    assertTrue(voteString.endsWith(")"))
  }

  @Test
  fun testCopyWithNoChanges() {
    val copiedVote = vote.copy()

    assertEquals("Copied object should be equal to the original", vote, copiedVote)
    assertNotSame("Copied object should be a new instance (different reference)", vote, copiedVote)
  }

  @Test
  fun testCopyWithModifiedUserId() {
    val newUserId = "new-user-789"
    val copiedVote = vote.copy(userId = newUserId)

    assertEquals(newUserId, copiedVote.userId)
    assertEquals(vote.formatPreferences, copiedVote.formatPreferences)
  }

  @Test
  fun testCopyWithModifiedFormatPreferences() {
    val newPrefs = listOf(MeetingFormat.IN_PERSON)
    val copiedVote = vote.copy(formatPreferences = newPrefs)

    assertEquals(vote.userId, copiedVote.userId)
    assertEquals(newPrefs, copiedVote.formatPreferences)
  }

  @Test
  fun testDestructuringDeclarations() {
    val (id, prefs) = vote

    assertEquals("component1() should return userId", testUserId, id)
    assertEquals("component2() should return formatPreferences", testPreferences, prefs)
  }
}
