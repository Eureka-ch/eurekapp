package ch.eureka.eurekapp.model.data.activity

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.*
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FirestoreActivityRepositoryTest {
  private lateinit var firestore: FirebaseFirestore
  private lateinit var repository: FirestoreActivityRepository

  @Before
  fun setup() {
    firestore = mockk(relaxed = true)
    repository = FirestoreActivityRepository(firestore)
  }

  @Test
  fun createActivity_returnsId() = runTest {
    val collection = mockk<CollectionReference>(relaxed = true)
    val doc = mockk<DocumentReference>(relaxed = true)
    every { firestore.collection("activities") } returns collection
    every { collection.document() } returns doc
    every { doc.id } returns "new-id"
    every { doc.set(any()) } returns Tasks.forResult(null)
    val result = repository.createActivity(Activity(entityType = EntityType.TASK))
    assertTrue(result.isSuccess)
    assertEquals("new-id", result.getOrNull())
  }

  @Test
  fun deleteActivity_succeeds() = runTest {
    val collection = mockk<CollectionReference>(relaxed = true)
    val doc = mockk<DocumentReference>(relaxed = true)
    every { firestore.collection("activities") } returns collection
    every { collection.document("test-id") } returns doc
    every { doc.delete() } returns Tasks.forResult(null)
    val result = repository.deleteActivity("test-id")
    assertTrue(result.isSuccess)
  }
}
