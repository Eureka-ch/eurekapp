/* Portions of this file were written with the help of Gemini and GPT-5 Codex. */
package ch.eureka.eurekapp.model.data.notes

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

/**
 * Test suite for SelfNotesRepository implementation.
 *
 * Tests CRUD operations for self-notes stored in Firestore. Note: These tests require the user to
 * be authenticated as the repository now enforces that internally.
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
  fun selfNotesRepository_shouldCreateNoteInFirestore() = runBlocking {
    val message =
        Message(
            messageID = "note1",
            text = "Test note content",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())

    val result = repository.createNote(message)

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
    // Note: senderId in the saved note will be from the authenticated user
  }

  @Test
  fun selfNotesRepository_shouldReturnAllNotes() = runBlocking {
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

    repository.createNote(message1)
    repository.createNote(message2)

    val flow = repository.getNotes()
    val notes = flow.first()

    assertTrue(notes.size >= 2)
    assertTrue(notes.any { it.messageID == noteId1 })
    assertTrue(notes.any { it.messageID == noteId2 })
  }

  @Test
  fun selfNotesRepository_shouldOrderByCreatedAtDescending() = runBlocking {
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

    repository.createNote(message1)
    repository.createNote(message2)

    val flow = repository.getNotes()
    val notes = flow.first()

    assertEquals(2, notes.size)
    // Should be descending: newer first
    assertEquals("note7", notes[0].messageID)
    assertEquals("note6", notes[1].messageID)
  }

  @Test
  fun selfNotesRepository_shouldRespectLimit() = runBlocking {
    // Create 5 notes
    for (i in 1..5) {
      val message =
          Message(
              messageID = "limited_note_$i",
              text = "Note $i",
              senderId = testUserId,
              createdAt = Timestamp(i.toLong() * 100, 0),
              references = emptyList())
      repository.createNote(message)
    }

    val flow = repository.getNotes(limit = 3)
    val notes = flow.first()

    assertEquals(3, notes.size)
  }

  @Test
  fun selfNotesRepository_shouldUpdateTextInFirestore() = runBlocking {
    val message =
        Message(
            messageID = "note_to_update",
            text = "Original text",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())

    repository.createNote(message)

    val newText = "Updated text content"
    val result = repository.updateNote("note_to_update", newText)

    assertTrue(result.isSuccess)

    val updatedNote =
        FirebaseEmulator.firestore
            .collection("users")
            .document(testUserId)
            .collection("selfNotes")
            .document("note_to_update")
            .get()
            .await()
            .toObject(Message::class.java)

    assertEquals(newText, updatedNote?.text)
    assertEquals(message.messageID, updatedNote?.messageID)
  }

  @Test
  fun selfNotesRepository_shouldFailIfNoteDoesNotExist() = runBlocking {
    val result = repository.updateNote("non_existent_note_update", "New text")

    // Firestore update fails (throws NOT_FOUND) if document does not exist,
    // which runCatching should capture as a failure.
    assertTrue(result.isFailure)
  }

  @Test
  fun selfNotesRepository_shouldRemoveNoteFromFirestore() = runBlocking {
    val message =
        Message(
            messageID = "note_to_delete",
            text = "This will be deleted",
            senderId = testUserId,
            createdAt = Timestamp.now(),
            references = emptyList())
    repository.createNote(message)

    val result = repository.deleteNote("note_to_delete")

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
  fun selfNotesRepository_shouldSucceedEvenIfNoteDoesNotExist() = runBlocking {
    val result = repository.deleteNote("non_existent_note")

    // Firestore delete is idempotent
    assertTrue(result.isSuccess)
  }

  @Test
  fun selfNotesRepository_shouldPreserveTimestamp() = runBlocking {
    // Firestore truncates nanoseconds to microsecond precision (last 3 digits become 000)
    val specificTimestamp = Timestamp(1234567890, 123456000)
    val message =
        Message(
            messageID = "timestamped_note",
            text = "Timestamp test",
            senderId = testUserId,
            createdAt = specificTimestamp,
            references = emptyList())

    repository.createNote(message)

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
