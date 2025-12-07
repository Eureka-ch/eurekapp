/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.model.data.ideas

import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.project.ProjectRepository
import ch.eureka.eurekapp.ui.ideas.Idea
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Unit tests for FirestoreIdeasRepository. */
class FirestoreIdeasRepositoryTest {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var projectRepository: ProjectRepository
  private lateinit var repository: FirestoreIdeasRepository
  private val testUserId = "user-123"
  private val testProjectId = "project-456"

  @Before
  fun setup() {
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)
    projectRepository = mockk(relaxed = true)
    val mockUser: FirebaseUser = mockk(relaxed = true)
    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId
    repository = FirestoreIdeasRepository(firestore, auth, projectRepository)
  }

  @Test
  fun `getIdeasForProject returns empty list when user not authenticated`() = runTest {
    every { auth.currentUser } returns null
    val result = repository.getIdeasForProject(testProjectId).first()
    assertTrue(result.isEmpty())
  }

  @Test
  fun `getIdeasForProject returns ideas list when snapshot succeeds`() = runTest {
    val idea = Idea(ideaId = "idea-1", projectId = testProjectId, createdBy = testUserId)
    val collectionRef = mockk<CollectionReference>(relaxed = true)
    val query = mockk<Query>(relaxed = true)
    val snapshot = mockk<QuerySnapshot>(relaxed = true)
    val doc = mockk<DocumentSnapshot>(relaxed = true)
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val registration = mockk<ListenerRegistration>(relaxed = true)

    every { firestore.collection("projects") } returns collectionRef
    every { collectionRef.document(testProjectId) } returns mockk(relaxed = true)
    every { collectionRef.document(testProjectId).collection("ideas") } returns collectionRef
    every { collectionRef.whereArrayContains("participantIds", testUserId) } returns query
    every { query.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(snapshot, null)
          registration
        }
    every { snapshot.documents } returns listOf(doc)
    every { doc.id } returns "idea-1"
    every { doc.toObject(Idea::class.java) } returns idea.copy(ideaId = "")

    val result = repository.getIdeasForProject(testProjectId).first()

    assertEquals(1, result.size)
    assertEquals("idea-1", result[0].ideaId)
  }

  @Test
  fun `createIdea succeeds with valid data`() = runTest {
    val idea = Idea(ideaId = "idea-1", projectId = testProjectId, createdBy = testUserId)
    val docRef: DocumentReference = mockk(relaxed = true)
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.set(any()) } returns Tasks.forResult(null)

    val result = repository.createIdea(idea)

    assertTrue(result.isSuccess)
    assertEquals("idea-1", result.getOrNull())
  }

  @Test
  fun `createIdea fails when Firestore fails`() = runTest {
    val idea = Idea(ideaId = "idea-1", projectId = testProjectId, createdBy = testUserId)
    val docRef: DocumentReference = mockk(relaxed = true)
    val exception = FirebaseFirestoreException("Error", FirebaseFirestoreException.Code.UNAVAILABLE)
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.set(any()) } returns Tasks.forException(exception)

    val result = repository.createIdea(idea)

    assertTrue(result.isFailure)
  }

  @Test
  fun `deleteIdea succeeds when idea exists`() = runTest {
    val docRef: DocumentReference = mockk(relaxed = true)
    val collectionRef: CollectionReference = mockk(relaxed = true)
    val snapshot: QuerySnapshot = mockk(relaxed = true)

    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.collection(any()) } returns collectionRef
    every { docRef.delete() } returns Tasks.forResult(null)
    every { collectionRef.get() } returns Tasks.forResult(snapshot)
    every { snapshot.documents } returns emptyList()

    val result = repository.deleteIdea(testProjectId, "idea-1")

    assertTrue(result.isSuccess)
  }

  @Test
  fun `sendMessage fails when user not authenticated`() = runTest {
    every { auth.currentUser } returns null
    val message = Message(messageID = "msg-1", senderId = testUserId, text = "Hi")

    val result = repository.sendMessage("idea-1", message)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("not authenticated") == true)
  }

  @Test
  fun `addParticipant succeeds with valid data`() = runTest {
    val docRef: DocumentReference = mockk(relaxed = true)
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.update(any<String>(), any()) } returns Tasks.forResult(null)

    val result = repository.addParticipant(testProjectId, "idea-1", testUserId)

    assertTrue(result.isSuccess)
  }

  @Test
  fun `getMessagesForIdea returns empty list when no projects`() = runTest {
    every { projectRepository.getProjectsForCurrentUser(skipCache = false) } returns
        flowOf(emptyList())

    val result = repository.getMessagesForIdea("idea-1").first()

    assertTrue(result.isEmpty())
  }

  @Test
  fun `sendMessage finds idea and sends message successfully`() = runTest {
    val message = Message(messageID = "msg-1", senderId = testUserId, text = "Hello")
    val project =
        ch.eureka.eurekapp.model.data.project.Project(projectId = testProjectId, name = "Test")
    val ideaDoc = mockk<DocumentSnapshot>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)
    val collectionRef = mockk<CollectionReference>(relaxed = true)

    every { projectRepository.getProjectsForCurrentUser(skipCache = false) } returns
        flowOf(listOf(project))
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.get() } returns Tasks.forResult(ideaDoc)
    every { ideaDoc.exists() } returns true
    every { docRef.collection(any()) } returns collectionRef
    every { collectionRef.document(any()) } returns docRef
    every { docRef.set(any()) } returns Tasks.forResult(null)

    val result = repository.sendMessage("idea-1", message)

    assertTrue(result.isSuccess)
  }

  @Test
  fun `sendMessage fails when idea not found in any project`() = runTest {
    val message = Message(messageID = "msg-1", senderId = testUserId, text = "Hello")
    val project =
        ch.eureka.eurekapp.model.data.project.Project(projectId = testProjectId, name = "Test")
    val ideaDoc = mockk<DocumentSnapshot>(relaxed = true)
    val docRef = mockk<DocumentReference>(relaxed = true)

    every { projectRepository.getProjectsForCurrentUser(skipCache = false) } returns
        flowOf(listOf(project))
    every { firestore.collection(any()).document(any()).collection(any()).document(any()) } returns
        docRef
    every { docRef.get() } returns Tasks.forResult(ideaDoc)
    every { ideaDoc.exists() } returns false

    val result = repository.sendMessage("idea-1", message)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
  }
}
