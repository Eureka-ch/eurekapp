package ch.eureka.eurekapp.model.data.transcription

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.functions.FirebaseFunctions
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Test suite for CloudFunctionSpeechToTextRepository
 *
 * Note : This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5
 */
class CloudFunctionSpeechToTextRepositoryTest {

  private lateinit var firestore: FirebaseFirestore
  private lateinit var auth: FirebaseAuth
  private lateinit var functions: FirebaseFunctions
  private lateinit var repository: CloudFunctionSpeechToTextRepository

  private lateinit var mockUser: FirebaseUser
  private lateinit var mockCollectionRef: CollectionReference
  private lateinit var mockDocumentRef: DocumentReference
  private lateinit var mockQuery: Query

  private val testProjectId = "project123"
  private val testMeetingId = "meeting456"
  private val testTranscriptionId = "transcription789"
  private val testUserId = "user123"

  @Before
  fun setup() {
    firestore = mockk(relaxed = true)
    auth = mockk(relaxed = true)
    functions = mockk(relaxed = true)
    mockUser = mockk(relaxed = true)
    mockCollectionRef = mockk(relaxed = true)
    mockDocumentRef = mockk(relaxed = true)
    mockQuery = mockk(relaxed = true)

    every { auth.currentUser } returns mockUser
    every { mockUser.uid } returns testUserId

    repository = CloudFunctionSpeechToTextRepository(firestore, auth, functions)
  }

  /** Test that transcribeAudio fails with SecurityException when no user is authenticated */
  @Test
  fun transcribeAudioShouldFailWhenUserNotAuthenticated() = runTest {
    // Mock unauthenticated state
    every { auth.currentUser } returns null

    val result = repository.transcribeAudio("url", testMeetingId, testProjectId, "en-US")

    // Verify the operation failed with SecurityException
    assertTrue("Result should be failure", result.isFailure)
    assertTrue("Should throw SecurityException", result.exceptionOrNull() is SecurityException)
  }

  /** Test that network/Cloud Function exceptions are properly caught and returned as failures */
  @Test
  fun transcribeAudioShouldFailWhenTaskThrowsException() = runTest {
    // Setup Firestore mocks
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document() } returns mockDocumentRef
    every { mockDocumentRef.id } returns testTranscriptionId

    // Mock Cloud Function to throw exception
    val mockCallableRef = mockk<com.google.firebase.functions.HttpsCallableReference>()
    every { functions.getHttpsCallable("transcribeAudio") } returns mockCallableRef

    val exception = Exception("Network timeout")
    val mockTask = Tasks.forException<com.google.firebase.functions.HttpsCallableResult>(exception)
    every { mockCallableRef.call(any()) } returns mockTask

    val result = repository.transcribeAudio("url", testMeetingId, testProjectId, "en-US")

    // Verify the exception was caught and wrapped in Result.failure
    assertTrue("Result should be failure", result.isFailure)
    assertEquals("Network timeout", result.exceptionOrNull()?.message)
  }

  /** Test that Firestore snapshot listener emits list of transcriptions for a meeting */
  @Test
  fun getTranscriptionsForMeetingShouldEmitTranscriptions() = runTest {
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    // Setup Firestore query mocks
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery

    val mockSnapshot = mockk<QuerySnapshot>()
    val mockDoc = mockk<DocumentSnapshot>()

    // Create test transcription object
    val transcription =
        AudioTranscription(
            transcriptionId = "trans1",
            meetingId = testMeetingId,
            projectId = testProjectId,
            audioDownloadUrl = "url1",
            transcriptionText = "Hello world",
            status = TranscriptionStatus.COMPLETED,
            createdAt = Timestamp.now(),
            createdBy = testUserId)

    // Mock snapshot to return our test transcription
    every { mockSnapshot.documents } returns listOf(mockDoc)
    every { mockDoc.toObject(AudioTranscription::class.java) } returns transcription
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }

    // Collect the first emitted value from the flow
    val flow = repository.getTranscriptionsForMeeting(testProjectId, testMeetingId)
    val emittedList = flow.first()

    // Verify the transcription was emitted correctly
    assertEquals(1, emittedList.size)
    assertEquals("trans1", emittedList[0].transcriptionId)
  }

  /** Test that null snapshots return empty lists instead of causing errors */
  @Test
  fun getTranscriptionsForMeetingShouldEmitEmptyListWhenSnapshotIsNull() = runTest {
    // Capture the listener that will be registered with Firestore
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery

    // Simulate Firestore returning null snapshot (edge case in network issues or query errors)
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(null, null) // Both snapshot and error are null
          mockListenerRegistration
        }

    val flow = repository.getTranscriptionsForMeeting(testProjectId, testMeetingId)
    val emittedList = flow.first()

    // Verify graceful handling: empty list instead of crash
    assertEquals(0, emittedList.size)
  }

  /** Test that a specific transcription can be retrieved by ID via snapshot listener */
  @Test
  fun getTranscriptionByIdShouldEmitTranscriptionWhenItExists() = runTest {
    // Setup listener slot to capture the Firestore snapshot listener
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    // Mock Firestore document reference for specific transcription
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document(testTranscriptionId) } returns mockDocumentRef

    val mockSnapshot = mockk<DocumentSnapshot>()
    // Create a completed transcription with sample text
    val transcription =
        AudioTranscription(
            transcriptionId = testTranscriptionId,
            meetingId = testMeetingId,
            projectId = testProjectId,
            audioDownloadUrl = "url",
            transcriptionText = "Test transcription",
            status = TranscriptionStatus.COMPLETED,
            createdAt = Timestamp.now(),
            createdBy = testUserId)

    // Mock Firestore to return the transcription when snapshot is converted
    every { mockSnapshot.toObject(AudioTranscription::class.java) } returns transcription
    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }

    // Collect the first emission from the flow (real-time listener)
    val flow = repository.getTranscriptionById(testProjectId, testMeetingId, testTranscriptionId)
    val emitted = flow.first()

    // Verify the correct transcription was emitted with all fields intact
    assertEquals(testTranscriptionId, emitted?.transcriptionId)
    assertEquals("Test transcription", emitted?.transcriptionText)
  }

  /** Test successful deletion of a transcription from Firestore */
  @Test
  fun deleteTranscriptionShouldReturnSuccessWhenDeletionSucceeds() = runTest {
    // Setup Firestore path and document reference
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document(testTranscriptionId) } returns mockDocumentRef

    // Mock successful deletion (Tasks.forResult with null = success for Void return type)
    val mockTask = Tasks.forResult<Void>(null)
    every { mockDocumentRef.delete() } returns mockTask

    val result = repository.deleteTranscription(testProjectId, testMeetingId, testTranscriptionId)

    // Verify deletion succeeded and delete() was called exactly once
    assertTrue("Result should be success", result.isSuccess)
    verify { mockDocumentRef.delete() }
  }

  /** Test that Firestore deletion errors are caught and returned as failures */
  @Test
  fun deleteTranscriptionShouldReturnFailureWhenDeletionFails() = runTest {
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document(testTranscriptionId) } returns mockDocumentRef

    // Simulate network/Firestore error during deletion
    val exception = Exception("Network error")
    val mockTask = Tasks.forException<Void>(exception)
    every { mockDocumentRef.delete() } returns mockTask

    val result = repository.deleteTranscription(testProjectId, testMeetingId, testTranscriptionId)

    // Verify the error is wrapped in Result.failure with original message preserved
    assertTrue("Result should be failure", result.isFailure)
    assertEquals("Network error", result.exceptionOrNull()?.message)
  }

  /** Test that non-existent or invalid documents emit null instead of throwing errors */
  @Test
  fun getTranscriptionByIdShouldEmitNullWhenDocumentDoesNotExist() = runTest {
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document(testTranscriptionId) } returns mockDocumentRef

    val mockSnapshot = mockk<DocumentSnapshot>()
    // Simulate document not existing or failing to deserialize (toObject returns null)
    // This can happen when: 1) Document ID doesn't exist, 2) Document has invalid schema
    every { mockSnapshot.toObject(AudioTranscription::class.java) } returns null
    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }

    val flow = repository.getTranscriptionById(testProjectId, testMeetingId, testTranscriptionId)
    val emitted = flow.first()

    // Verify null is emitted (allows UI to handle gracefully, e.g., show "not found")
    assertEquals(null, emitted)
  }

  /** Test that Firestore errors in snapshot listener close the flow with the error */
  @Test
  fun getTranscriptionByIdShouldPropagateErrorWhenSnapshotListenerFails() = runTest {
    val listenerSlot = slot<EventListener<DocumentSnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document(testTranscriptionId) } returns mockDocumentRef

    // Simulate Firestore error (e.g., permission denied, network timeout)
    val mockError = mockk<FirebaseFirestoreException>(relaxed = true)
    every { mockError.message } returns "PERMISSION_DENIED: Missing or insufficient permissions"
    every { mockDocumentRef.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(null, mockError) // Pass error to listener
          mockListenerRegistration
        }

    val flow = repository.getTranscriptionById(testProjectId, testMeetingId, testTranscriptionId)

    // Verify that the error is propagated (flow should throw when collecting)
    val exception = runCatching { flow.first() }.exceptionOrNull()
    assertNotNull("Flow should close with exception", exception)
    assertTrue(
        "Exception should be FirebaseFirestoreException", exception is FirebaseFirestoreException)
  }

  /** Test that malformed/null documents in snapshots are filtered out of results */
  @Test
  fun getTranscriptionsForMeetingShouldFilterNullTranscriptions() = runTest {
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery

    val mockSnapshot = mockk<QuerySnapshot>()
    val mockDoc1 = mockk<DocumentSnapshot>()
    val mockDoc2 = mockk<DocumentSnapshot>()

    val transcription =
        AudioTranscription(
            transcriptionId = "trans1",
            meetingId = testMeetingId,
            projectId = testProjectId,
            audioDownloadUrl = "url1",
            transcriptionText = "Hello world",
            status = TranscriptionStatus.COMPLETED,
            createdAt = Timestamp.now(),
            createdBy = testUserId)

    // Mock snapshot with 2 documents: one valid, one null (malformed)
    every { mockSnapshot.documents } returns listOf(mockDoc1, mockDoc2)
    every { mockDoc1.toObject(AudioTranscription::class.java) } returns transcription
    every { mockDoc2.toObject(AudioTranscription::class.java) } returns
        null // Simulates malformed document
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(mockSnapshot, null)
          mockListenerRegistration
        }

    val flow = repository.getTranscriptionsForMeeting(testProjectId, testMeetingId)
    val emittedList = flow.first()

    // Verify only the valid transcription was emitted (null was filtered out)
    assertEquals(1, emittedList.size)
    assertEquals("trans1", emittedList[0].transcriptionId)
  }

  /** Test that Firestore errors in query snapshot listener close the flow with the error */
  @Test
  fun getTranscriptionsForMeetingShouldPropagateErrorWhenSnapshotListenerFails() = runTest {
    val listenerSlot = slot<EventListener<QuerySnapshot>>()
    val mockListenerRegistration = mockk<ListenerRegistration>(relaxed = true)

    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.orderBy("createdAt", Query.Direction.DESCENDING) } returns mockQuery

    // Simulate Firestore query error (e.g., index not found, permission denied)
    val mockError = mockk<FirebaseFirestoreException>(relaxed = true)
    every { mockError.message } returns "FAILED_PRECONDITION: The query requires an index"
    every { mockQuery.addSnapshotListener(capture(listenerSlot)) } answers
        {
          listenerSlot.captured.onEvent(null, mockError) // Pass error to listener
          mockListenerRegistration
        }

    val flow = repository.getTranscriptionsForMeeting(testProjectId, testMeetingId)

    // Verify that the error is propagated (flow should throw when collecting)
    val exception = runCatching { flow.first() }.exceptionOrNull()
    assertNotNull("Flow should close with exception", exception)
    assertTrue(
        "Exception should be FirebaseFirestoreException", exception is FirebaseFirestoreException)
  }

  /** Test the happy path: Cloud Function succeeds and returns transcriptionId */
  @Test
  fun transcribeAudioShouldSucceedWhenCloudFunctionReturnsSuccess() = runTest {
    // Setup Firestore mocks
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document() } returns mockDocumentRef
    every { mockDocumentRef.id } returns testTranscriptionId

    // Mock successful Cloud Function call
    val mockCallableRef = mockk<com.google.firebase.functions.HttpsCallableReference>()
    every { functions.getHttpsCallable("transcribeAudio") } returns mockCallableRef

    val mockResult = mockk<com.google.firebase.functions.HttpsCallableResult>()
    // Valid response with success=true and transcriptionId
    val responseData: Map<String, Any> =
        mapOf("success" to true, "transcriptionId" to testTranscriptionId, "message" to "Success")
    every { mockResult.getData() } returns responseData

    val mockTask = Tasks.forResult(mockResult)
    every { mockCallableRef.call(any()) } returns mockTask

    val result =
        repository.transcribeAudio(
            "https://storage.googleapis.com/test-bucket/meeting-audio.wav",
            testMeetingId,
            testProjectId,
            "en-US")

    // Verify success and correct transcriptionId returned
    assertTrue("Result should be success", result.isSuccess)
    assertEquals(testTranscriptionId, result.getOrNull())
  }

  /** Test error handling when Cloud Function returns unexpected response type (not a Map) */
  @Test
  fun transcribeAudioShouldFailWhenCloudFunctionReturnsInvalidResponseFormat() = runTest {
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document() } returns mockDocumentRef
    every { mockDocumentRef.id } returns testTranscriptionId

    val mockCallableRef = mockk<com.google.firebase.functions.HttpsCallableReference>()
    every { functions.getHttpsCallable("transcribeAudio") } returns mockCallableRef

    val mockResult = mockk<com.google.firebase.functions.HttpsCallableResult>()
    // Return a String instead of expected Map - this is invalid
    every { mockResult.getData() } returns "Invalid string response"

    val mockTask = Tasks.forResult(mockResult)
    every { mockCallableRef.call(any()) } returns mockTask

    val result =
        repository.transcribeAudio(
            "https://storage.googleapis.com/test-bucket/meeting-audio.wav",
            testMeetingId,
            testProjectId,
            "en-US")

    // Verify IllegalStateException is thrown for invalid response type
    assertTrue("Result should be failure", result.isFailure)
    assertTrue(
        "Should throw IllegalStateException", result.exceptionOrNull() is IllegalStateException)
    assertEquals("Invalid response from Cloud Function", result.exceptionOrNull()?.message)
  }

  /** Test error handling when Cloud Function explicitly returns success=false with error message */
  @Test
  fun transcribeAudioShouldFailWhenCloudFunctionReturnsSuccessFalse() = runTest {
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document() } returns mockDocumentRef
    every { mockDocumentRef.id } returns testTranscriptionId

    val mockCallableRef = mockk<com.google.firebase.functions.HttpsCallableReference>()
    every { functions.getHttpsCallable("transcribeAudio") } returns mockCallableRef

    val mockResult = mockk<com.google.firebase.functions.HttpsCallableResult>()
    // Cloud Function returned success=false with error message
    val responseData: Map<String, Any> =
        mapOf("success" to false, "error" to "Invalid audio format")
    every { mockResult.getData() } returns responseData

    val mockTask = Tasks.forResult(mockResult)
    every { mockCallableRef.call(any()) } returns mockTask

    val result =
        repository.transcribeAudio(
            "https://storage.googleapis.com/test-bucket/meeting-audio.wav",
            testMeetingId,
            testProjectId,
            "en-US")

    // Verify error message from Cloud Function is propagated
    assertTrue("Result should be failure", result.isFailure)
    assertEquals(
        "Cloud Function returned success=false: Invalid audio format",
        result.exceptionOrNull()?.message)
  }

  /** Test error handling when Cloud Function response is missing required transcriptionId field */
  @Test
  fun transcribeAudioShouldFailWhenCloudFunctionResponseMissingTranscriptionId() = runTest {
    val transcriptionsPath = FirestorePaths.transcriptionsPath(testProjectId, testMeetingId)
    every { firestore.collection(transcriptionsPath) } returns mockCollectionRef
    every { mockCollectionRef.document() } returns mockDocumentRef
    every { mockDocumentRef.id } returns testTranscriptionId

    val mockCallableRef = mockk<com.google.firebase.functions.HttpsCallableReference>()
    every { functions.getHttpsCallable("transcribeAudio") } returns mockCallableRef

    val mockResult = mockk<com.google.firebase.functions.HttpsCallableResult>()
    // Response has success=true but is missing the required transcriptionId field
    val responseData: Map<String, Any> =
        mapOf("success" to true, "message" to "Transcription started")
    every { mockResult.getData() } returns responseData

    val mockTask = Tasks.forResult(mockResult)
    every { mockCallableRef.call(any()) } returns mockTask

    val result =
        repository.transcribeAudio(
            "https://storage.googleapis.com/test-bucket/meeting-audio.wav",
            testMeetingId,
            testProjectId,
            "en-US")

    // Verify IllegalStateException for missing required field
    assertTrue("Result should be failure", result.isFailure)
    assertTrue(
        "Should throw IllegalStateException", result.exceptionOrNull() is IllegalStateException)
    assertEquals(
        "Missing transcriptionId in Cloud Function response", result.exceptionOrNull()?.message)
  }
}
