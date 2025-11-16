package ch.eureka.eurekapp.model.data.note

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.utils.FirebaseEmulator
import ch.eureka.eurekapp.utils.FirestoreRepositoryTest
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Before
import org.junit.Test

/*
Co-author: GPT-5 Codex
*/

/**
 * Test suite for SelfNotesRepository implementation.
 *
 * Tests CRUD operations for self-notes stored in Firestore.
 */
class SelfNotesRepositoryTest : FirestoreRepositoryTest() {

  private lateinit var repository: SelfNotesRepository

  override fun getCollectionPaths(): List<String> {
    return listOf("users/$testUserId/selfNotes")
  }

  @Before
  override fun setup() {
    runBlocking {
      super.setup()
      repository =
          FirestoreSelfNotesRepository(
              firestore = FirebaseEmulator.firestore, auth = FirebaseEmulator.auth)

      // Create user document first (required for subcollection)
      FirebaseEmulator.firestore
          .collection("users")
          .document(testUserId)
          .set(mapOf("uid" to testUserId))
          .await()
    }
  }

  @Test
  fun createNote_shouldCreateNoteInFirestore() = runBlocking {
    val message =
        Message(
            messageID = "note1",
            text = "Test note content",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())

    val result = repository.createNote(testUserId, message)

    assertTrue(result.isSuccess)
    assertEquals("note1", result.getOrNull())

    val savedNote =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .collection("selfNotes")
            .document("note1")
            .get()
            .await()
            .toObject(Message::class.java)

    assertEquals(message.messageID, savedNote?.messageID)
    assertEquals(message.text, savedNote?.text)
    assertEquals(message.senderId, savedNote?.senderId)
  }

  @Test
  fun getNotesForUser_shouldReturnAllNotes() = runBlocking {
    val noteId1 = "return_all_note1_${System.currentTimeMillis()}"
    val noteId2 = "return_all_note2_${System.currentTimeMillis()}"
    val message1 =
        Message(
            messageID = noteId1,
            text = "First note",
            senderId = testUserId,
            createdAt = Timestamp(100, 0),
            references = emptyList())
    val message2 =
        Message(
            messageID = noteId2,
            text = "Second note",
            senderId = testUserId,
            createdAt = Timestamp(200, 0),
            references = emptyList())

    repository.createNote(testUserId, message1)
    repository.createNote(testUserId, message2)

    val flow = repository.getNotesForUser(testUserId)
    val notes = flow.first()

    assertTrue(notes.size >= 2)
    assertTrue(notes.any { it.messageID == noteId1 })
    assertTrue(notes.any { it.messageID == noteId2 })
  }

  @Test
  fun getNotesForUser_shouldOrderByCreatedAtDescending() = runBlocking {
    val message1 =
        Message(
            messageID = "note6",
            text = "Older note",
            senderId = testUserId,
            createdAt = Timestamp(100, 0),
            references = emptyList())
    val message2 =
        Message(
            messageID = "note7",
            text = "Newer note",
            senderId = testUserId,
            createdAt = Timestamp(200, 0),
            references = emptyList())

    repository.createNote(testUserId, message1)
    repository.createNote(testUserId, message2)

    val flow = repository.getNotesForUser(testUserId)
    val notes = flow.first()

    assertEquals(2, notes.size)
    // Should be descending: newer first
    assertEquals("note7", notes[0].messageID)
    assertEquals("note6", notes[1].messageID)
  }

  @Test
  fun getNotesForUser_shouldRespectLimit() = runBlocking {
    // Create 5 notes
    for (i in 1..5) {
      val message =
          Message(
              messageID = "limited_note_$i",
              text = "Note $i",
              senderId = testUserId,
              createdAt = Timestamp(i.toLong() * 100, 0),
              references = emptyList())
      repository.createNote(testUserId, message)
    }

    val flow = repository.getNotesForUser(testUserId, limit = 3)
    val notes = flow.first()

    assertEquals(3, notes.size)
  }

  @Test
  fun deleteNote_shouldRemoveNoteFromFirestore() = runBlocking {
    val message =
        Message(
            messageID = "note_to_delete",
            text = "This will be deleted",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.createNote(testUserId, message)

    val result = repository.deleteNote(testUserId, "note_to_delete")

    assertTrue(result.isSuccess)

    val deletedNote =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .collection("selfNotes")
            .document("note_to_delete")
            .get()
            .await()

    assertTrue(!deletedNote.exists())
  }

  @Test
  fun deleteNote_shouldSucceedEvenIfNoteDoesNotExist() = runBlocking {
    val result = repository.deleteNote(testUserId, "non_existent_note")

    // Firestore delete is idempotent
    assertTrue(result.isSuccess)
  }

  @Test
  fun createNote_shouldPreserveTimestamp() = runBlocking {
    // Firestore truncates nanoseconds to microsecond precision (last 3 digits become 000)
    val specificTimestamp = Timestamp(1234567890, 123456000)
    val message =
        Message(
            messageID = "timestamped_note",
            text = "Timestamp test",
            senderId = testUserId,
            createdAt = specificTimestamp,
            references = emptyList())

    repository.createNote(testUserId, message)

    val savedNote =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .collection("selfNotes")
            .document("timestamped_note")
            .get()
            .await()
            .toObject(Message::class.java)

    assertEquals(specificTimestamp.seconds, savedNote?.createdAt?.seconds)
    assertEquals(specificTimestamp.nanoseconds, savedNote?.createdAt?.nanoseconds)
  }
}
