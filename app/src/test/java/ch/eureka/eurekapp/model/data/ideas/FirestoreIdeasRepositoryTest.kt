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
}
