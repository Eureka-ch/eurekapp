// Co-authored by Claude Code
package ch.eureka.eurekapp.model.data.mcp

import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class FirebaseMcpTokenRepositoryTest {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var mockUser: FirebaseUser
  private lateinit var repository: FirebaseMcpTokenRepository
  private lateinit var mockCollection: CollectionReference
  private lateinit var mockDocumentRef: DocumentReference

  @Before
  fun setup() {
    firestore = mockk(relaxed = true)
    auth = mockk()
    mockUser = mockk()
    mockCollection = mockk(relaxed = true)
    mockDocumentRef = mockk(relaxed = true)

    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns "test-user-id"
    every { firestore.collection(any()) } returns mockCollection
    every { firestore.document(any()) } returns mockDocumentRef

    repository = FirebaseMcpTokenRepository(firestore, auth)
  }

  @Test
  fun createToken_returnsTokenOnSuccess() = runTest {
    every { mockDocumentRef.set(any()) } returns Tasks.forResult(null)

    val result = repository.createToken("My Token", 30)

    assertTrue(result.isSuccess)
    val token = result.getOrNull()
    assertNotNull(token)
    assertEquals("My Token", token?.name)
    assertTrue(token?.tokenId?.startsWith("mcp_") == true)
    assertNotNull(token?.createdAt)
    assertNotNull(token?.expiresAt)
  }

  @Test
  fun createToken_returnsFailureOnFirestoreError() = runTest {
    val exception =
        FirebaseFirestoreException("Network error", FirebaseFirestoreException.Code.UNAVAILABLE)
    every { mockDocumentRef.set(any()) } returns Tasks.forException(exception)

    val result = repository.createToken("My Token", 30)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
  }

  @Test
  fun createToken_throwsWhenUserNotAuthenticated() = runTest {
    every { auth.currentUser } returns null

    val result = repository.createToken("Test", 30)

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun revokeToken_returnsSuccess() = runTest {
    every { mockDocumentRef.delete() } returns Tasks.forResult(null)

    val result = repository.revokeToken("token-123")

    assertTrue(result.isSuccess)
    verify { mockDocumentRef.delete() }
  }

  @Test
  fun revokeToken_returnsFailureOnFirestoreError() = runTest {
    val exception =
        FirebaseFirestoreException("Not found", FirebaseFirestoreException.Code.NOT_FOUND)
    every { mockDocumentRef.delete() } returns Tasks.forException(exception)

    val result = repository.revokeToken("token-123")

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("Not found") == true)
  }

  @Test
  fun revokeToken_throwsWhenUserNotAuthenticated() = runTest {
    every { auth.currentUser } returns null

    val result = repository.revokeToken("token-123")

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun listTokens_returnsTokensOnSuccess() = runTest {
    val token1 = McpToken(tokenId = "token-1", name = "Token One", createdAt = Timestamp(1000, 0))
    val token2 = McpToken(tokenId = "token-2", name = "Token Two", createdAt = Timestamp(2000, 0))

    val mockSnapshot: QuerySnapshot = mockk(relaxed = true)
    val mockDoc1: DocumentSnapshot = mockk(relaxed = true)
    val mockDoc2: DocumentSnapshot = mockk(relaxed = true)

    every { mockDoc1.toObject(McpToken::class.java) } returns token1
    every { mockDoc2.toObject(McpToken::class.java) } returns token2
    every { mockSnapshot.documents } returns listOf(mockDoc1, mockDoc2)
    every { mockCollection.get() } returns Tasks.forResult(mockSnapshot)

    val result = repository.listTokens()

    assertTrue(result.isSuccess)
    val tokens = result.getOrNull()
    assertNotNull(tokens)
    assertEquals(2, tokens?.size)
    assertEquals("token-1", tokens?.get(0)?.tokenId)
    assertEquals("Token One", tokens?.get(0)?.name)
    assertEquals("token-2", tokens?.get(1)?.tokenId)
  }

  @Test
  fun listTokens_returnsEmptyListWhenNoTokens() = runTest {
    val mockSnapshot: QuerySnapshot = mockk(relaxed = true)
    every { mockSnapshot.documents } returns emptyList()
    every { mockCollection.get() } returns Tasks.forResult(mockSnapshot)

    val result = repository.listTokens()

    assertTrue(result.isSuccess)
    assertEquals(0, result.getOrNull()?.size)
  }

  @Test
  fun listTokens_returnsFailureOnFirestoreError() = runTest {
    val exception =
        FirebaseFirestoreException(
            "Permission denied", FirebaseFirestoreException.Code.PERMISSION_DENIED)
    every { mockCollection.get() } returns Tasks.forException(exception)

    val result = repository.listTokens()

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull()?.message?.contains("Permission denied") == true)
  }

  @Test
  fun listTokens_throwsWhenUserNotAuthenticated() = runTest {
    every { auth.currentUser } returns null

    val result = repository.listTokens()

    assertTrue(result.isFailure)
    assertTrue(result.exceptionOrNull() is IllegalStateException)
  }

  @Test
  fun listTokens_filtersOutNullTokens() = runTest {
    val token1 = McpToken(tokenId = "token-1", name = "Token One")

    val mockSnapshot: QuerySnapshot = mockk(relaxed = true)
    val mockDoc1: DocumentSnapshot = mockk(relaxed = true)
    val mockDoc2: DocumentSnapshot = mockk(relaxed = true)

    every { mockDoc1.toObject(McpToken::class.java) } returns token1
    every { mockDoc2.toObject(McpToken::class.java) } returns null
    every { mockSnapshot.documents } returns listOf(mockDoc1, mockDoc2)
    every { mockCollection.get() } returns Tasks.forResult(mockSnapshot)

    val result = repository.listTokens()

    assertTrue(result.isSuccess)
    assertEquals(1, result.getOrNull()?.size)
    assertEquals("token-1", result.getOrNull()?.get(0)?.tokenId)
  }

  @Test
  fun createToken_setsCorrectExpirationDate() = runTest {
    every { mockDocumentRef.set(any()) } returns Tasks.forResult(null)

    val result = repository.createToken("My Token", 30)

    assertTrue(result.isSuccess)
    val token = result.getOrNull()
    assertNotNull(token?.createdAt)
    assertNotNull(token?.expiresAt)
    val createdMillis = token?.createdAt?.toDate()?.time ?: 0
    val expiresMillis = token?.expiresAt?.toDate()?.time ?: 0
    val daysDiff = (expiresMillis - createdMillis) / (24 * 60 * 60 * 1000)
    assertEquals(30, daysDiff)
  }

  @Test
  fun createToken_generatesSecureTokenId() = runTest {
    every { mockDocumentRef.set(any()) } returns Tasks.forResult(null)

    val result1 = repository.createToken("Token 1", 30)
    val result2 = repository.createToken("Token 2", 30)

    assertTrue(result1.isSuccess)
    assertTrue(result2.isSuccess)
    val tokenId1 = result1.getOrNull()?.tokenId
    val tokenId2 = result2.getOrNull()?.tokenId
    assertTrue(tokenId1?.startsWith("mcp_") == true)
    assertTrue(tokenId2?.startsWith("mcp_") == true)
    assertTrue(tokenId1 != tokenId2)
  }
}
