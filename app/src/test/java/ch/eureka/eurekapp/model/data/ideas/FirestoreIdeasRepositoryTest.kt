/* Portions of this file were generated with the help of Claude (Sonnet 4.5). */
package ch.eureka.eurekapp.model.data.ideas

import ch.eureka.eurekapp.model.data.chat.Message
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for FirestoreIdeasRepository. */
class FirestoreIdeasRepositoryTest {
  companion object {
    private const val TEST_USER_ID = "test-user-id"
    private const val TEST_PROJECT_ID = "test-project-id"
    private const val TEST_IDEA_ID = "test-idea-id"
  }

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var repository: FirestoreIdeasRepository

  @Before
  fun setup() {
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)
    val mockUser: FirebaseUser = mockk(relaxed = true)
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns TEST_USER_ID
    repository = FirestoreIdeasRepository(firestore, auth)
  }

  @Test
  fun getIdeasForProject_returnsEmptyListWhenUserNotAuthenticated() = runTest {
    every { auth.currentUser } returns null
    val result = repository.getIdeasForProject(TEST_PROJECT_ID).first()
    assertTrue(result.isEmpty())
  }

  @Test
  fun getIdeasForProject_returnsIdeasListWhenSnapshotSucceeds() = runTest {
    val idea = Idea(ideaId = TEST_IDEA_ID, projectId = TEST_PROJECT_ID, createdBy = TEST_USER_ID)
    val collectionRef = mockk<CollectionReference>(relaxed = true)
    val query = mockk<Query>(relaxed = true)
    val snapshot = mockk<QuerySnapshot>(relaxed = true)
    val doc = mockk<DocumentSnapshot>(relaxed = true)
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val registration = mockk<ListenerRegistration>(relaxed = true)

    every { firestore.collection("projects") } returns collectionRef
    every { collectionRef.document(TEST_PROJECT_ID) } returns mockk(relaxed = true)
    every { collectionRef.document(TEST_PROJECT_ID).collection("ideas") } returns collectionRef
    every { collectionRef.whereArrayContains("participantIds", TEST_USER_ID) } returns query
    every { query.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(snapshot, null)
          registration
        }
    every { snapshot.documents } returns listOf(doc)
    every { doc.id } returns TEST_IDEA_ID
    every { doc.toObject(Idea::class.java) } returns idea.copy(ideaId = "")

    val result = repository.getIdeasForProject(TEST_PROJECT_ID).first()

    assertEquals(1, result.size)
    assertEquals(TEST_IDEA_ID, result[0].ideaId)
  }

  @Test
  fun createIdea_succeedsWithValidData() = runTest {
    val idea = Idea(ideaId = TEST_IDEA_ID, projectId = TEST_PROJECT_ID, createdBy = TEST_USER_ID)
    val docRef: DocumentReference = mockk(relaxed = true)
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.set(any()) } returns Tasks.forResult(null)

    val result = repository.createIdea(idea)

    assertTrue(result.isSuccess)
    assertEquals(TEST_IDEA_ID, result.getOrNull())
  }

  @Test
  fun createIdea_failsWhenFirestoreFails() = runTest {
    val idea = Idea(ideaId = TEST_IDEA_ID, projectId = TEST_PROJECT_ID, createdBy = TEST_USER_ID)
    val docRef: DocumentReference = mockk(relaxed = true)
    val exception = FirebaseFirestoreException("Error", FirebaseFirestoreException.Code.UNAVAILABLE)
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.set(any()) } returns Tasks.forException(exception)

    val result = repository.createIdea(idea)

    assertTrue(result.isFailure)
  }

  @Test
  fun deleteIdea_succeedsWhenIdeaExists() = runTest {
    val docRef: DocumentReference = mockk(relaxed = true)
    val collectionRef: CollectionReference = mockk(relaxed = true)
    val snapshot: QuerySnapshot = mockk(relaxed = true)

    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.collection(any()) } returns collectionRef
    every { docRef.delete() } returns Tasks.forResult(null)
    every { collectionRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.documents } returns emptyList()

    val result = repository.deleteIdea(TEST_PROJECT_ID, TEST_IDEA_ID)

    assertTrue(result.isSuccess)
  }

  @Test
  fun sendMessage_failsWhenUserNotAuthenticated() = runTest {
    every { auth.currentUser } returns null
    val message = Message(messageID = "msg-1", senderId = TEST_USER_ID, text = "Hi")

    val result = repository.sendMessage(TEST_PROJECT_ID, TEST_IDEA_ID, message)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("not authenticated") == true)
  }

  @Test
  fun sendMessage_sendsMessageSuccessfully() = runTest {
    val message = Message(messageID = "msg-1", senderId = TEST_USER_ID, text = "Hello")
    val docRef = mockk<DocumentReference>(relaxed = true)
    val collectionRef = mockk<CollectionReference>(relaxed = true)

    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.collection(any()) } returns collectionRef
    every { collectionRef.document(any()) } returns docRef
    every { docRef.set(any()) } returns Tasks.forResult(null)

    val result = repository.sendMessage(TEST_PROJECT_ID, TEST_IDEA_ID, message)

    assertTrue(result.isSuccess)
  }

  @Test
  fun addParticipant_succeedsWithValidData() = runTest {
    val docRef: DocumentReference = mockk(relaxed = true)
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.update(any<String>(), any()) } returns Tasks.forResult(null)

    val result = repository.addParticipant(TEST_PROJECT_ID, TEST_IDEA_ID, TEST_USER_ID)

    assertTrue(result.isSuccess)
  }

  @Test
  fun getMessagesForIdea_returnsMessages() = runTest {
    val projectCol = mockk<CollectionReference>(relaxed = true)
    val projectDoc = mockk<DocumentReference>(relaxed = true)
    val ideasCol = mockk<CollectionReference>(relaxed = true)
    val ideaDoc = mockk<DocumentReference>(relaxed = true)
    val messagesCol = mockk<CollectionReference>(relaxed = true)
    val query = mockk<Query>(relaxed = true)
    val snapshot = mockk<QuerySnapshot>(relaxed = true)
    val registration = mockk<ListenerRegistration>(relaxed = true)
    val listenerSlot = slot<EventListener<QuerySnapshot>>()

    every { firestore.collection("projects") } returns projectCol
    every { projectCol.document(TEST_PROJECT_ID) } returns projectDoc
    every { projectDoc.collection("ideas") } returns ideasCol
    every { ideasCol.document(TEST_IDEA_ID) } returns ideaDoc
    every { ideaDoc.collection("messages") } returns messagesCol
    every { messagesCol.orderBy("createdAt") } returns query
    every { query.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(snapshot, null)
          registration
        }
    every { snapshot.documents } returns emptyList()

    val result = repository.getMessagesForIdea(TEST_PROJECT_ID, TEST_IDEA_ID).first()

    assertTrue(result.isEmpty())
  }
}
