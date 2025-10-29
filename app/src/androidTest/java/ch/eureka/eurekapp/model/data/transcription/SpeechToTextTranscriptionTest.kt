package ch.eureka.eurekapp.model.data.transcription

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.functions.functions
import com.google.firebase.storage.storage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for Speech-to-Text transcription with real Firebase Storage audio file.
 *
 * Note :This file was partially written by ChatGPT (GPT-5) Co-author : GPT-5
 */
@RunWith(AndroidJUnit4::class)
class SpeechToTextTranscriptionTest {

  private lateinit var repository: SpeechToTextRepository
  private lateinit var context: Context

  // Test audio file uploaded to Firebase Storage
  private val testAudioUrl =
      "https://firebasestorage.googleapis.com/v0/b/eureka-app-ch.firebasestorage.app/o/meetings%2Ftest-project%2Ftest-meeting%2FTest-Eureka.flac?alt=media&token=ee0baa24-0dae-4cc3-b838-d9063b93c8a8"
  private val testProjectId = "test-project"
  private val testMeetingId = "test-meeting"

  @Before
  fun setup() = runBlocking {
    context = ApplicationProvider.getApplicationContext()

    // Configure Firebase emulators BEFORE getting instances
    try {
      Firebase.firestore.useEmulator("10.0.2.2", 8080)
    } catch (e: IllegalStateException) {
      // Already configured - ignore
    }

    try {
      Firebase.storage.useEmulator("10.0.2.2", 9199)
    } catch (e: IllegalStateException) {
      // Already configured - ignore
    }

    val functions = Firebase.functions
    try {
      functions.useEmulator("10.0.2.2", 5001)
    } catch (e: IllegalStateException) {
      // Already configured - ignore
    }

    // Sign in anonymously for testing (Cloud Function requires authentication)
    if (Firebase.auth.currentUser == null) {
      Firebase.auth.signInAnonymously().await()
    }

    repository =
        CloudFunctionSpeechToTextRepository(
            firestore = Firebase.firestore, auth = Firebase.auth, functions = functions)
  }

  @Test
  fun testTranscribeAudio_withRealAudioFile_success() = runBlocking {
    // Call transcription
    val result =
        repository.transcribeAudio(
            audioDownloadUrl = testAudioUrl,
            meetingId = testMeetingId,
            projectId = testProjectId,
            languageCode = "en-US")

    // Verify success
    assertTrue("Transcription should succeed", result.isSuccess)

    val transcriptionId = result.getOrNull()
    assertNotNull("Transcription ID should not be null", transcriptionId)

    // Wait for transcription to complete and verify in Firestore
    withTimeout(60000) { // 60 second timeout
      var transcription: AudioTranscription? = null
      var attempts = 0

      while (transcription == null || transcription.status == TranscriptionStatus.PENDING) {
        attempts++

        transcription =
            repository.getTranscriptionById(testProjectId, testMeetingId, transcriptionId!!).first()

        if (transcription != null) {

          when (transcription.status) {
            TranscriptionStatus.COMPLETED -> {
              // Success! Verify the transcription
              assertNotNull(
                  "Transcription text should not be null", transcription.transcriptionText)
              assertTrue(
                  "Transcription text should not be empty",
                  transcription.transcriptionText.isNotEmpty())
              break
            }
            TranscriptionStatus.FAILED -> {
              fail("Transcription failed: ${transcription.errorMessage}")
            }
            TranscriptionStatus.PENDING -> {
              // Still processing, wait and retry
              kotlinx.coroutines.delay(2000) // Wait 2 seconds
            }
          }
        } else {
          kotlinx.coroutines.delay(2000)
        }
      }
    }
  }

  @Test
  fun testGetTranscriptionsForMeeting_returnsAllTranscriptions() = runBlocking {

    // First create a transcription
    val createResult =
        repository.transcribeAudio(
            audioDownloadUrl = testAudioUrl,
            meetingId = "$testMeetingId-list",
            projectId = testProjectId,
            languageCode = "en-US")

    assertTrue("Should create transcription", createResult.isSuccess)

    // Wait a bit for Firestore to update
    kotlinx.coroutines.delay(2000)

    // Get all transcriptions
    val transcriptions =
        repository.getTranscriptionsForMeeting(testProjectId, "$testMeetingId-list").first()

    assertFalse("Should have at least one transcription", transcriptions.isEmpty())
  }

  @Test
  fun testTranscribeAudio_withInvalidUrl_fails() = runBlocking {
    val result =
        repository.transcribeAudio(
            audioDownloadUrl = "https://invalid-url.com/fake-audio.mp3",
            meetingId = "$testMeetingId-invalid",
            projectId = testProjectId,
            languageCode = "en-US")

    assertTrue("Should fail with invalid URL", result.isFailure)
  }

  @Test
  fun testDeleteTranscription_success() = runBlocking {
    // Create a transcription
    val createResult =
        repository.transcribeAudio(
            audioDownloadUrl = testAudioUrl,
            meetingId = "$testMeetingId-delete",
            projectId = testProjectId,
            languageCode = "en-US")

    assertTrue("Should create transcription", createResult.isSuccess)
    val transcriptionId = createResult.getOrNull()!!

    // Delete it
    val deleteResult =
        repository.deleteTranscription(testProjectId, "$testMeetingId-delete", transcriptionId)

    assertTrue("Should delete successfully", deleteResult.isSuccess)

    // Verify it's gone
    kotlinx.coroutines.delay(1000)
    val transcription =
        repository
            .getTranscriptionById(testProjectId, "$testMeetingId-delete", transcriptionId)
            .first()

    assertNull("Transcription should be null after deletion", transcription)
  }
}
