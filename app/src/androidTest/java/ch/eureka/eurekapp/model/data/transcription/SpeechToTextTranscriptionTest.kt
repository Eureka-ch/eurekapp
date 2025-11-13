package ch.eureka.eurekapp.model.data.transcription

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.eureka.eurekapp.utils.FirebaseEmulator
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.functions.functions
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

    // Use FirebaseEmulator
    val functions = Firebase.functions
    if (FirebaseEmulator.isRunning) {
      functions.useEmulator(FirebaseEmulator.HOST, 5001)
    }

    // Sign in anonymously for testing (Cloud Function requires authentication)
    if (FirebaseEmulator.auth.currentUser == null) {
      FirebaseEmulator.auth.signInAnonymously().await()
    }

    repository =
        CloudFunctionSpeechToTextRepository(
            firestore = FirebaseEmulator.firestore,
            auth = FirebaseEmulator.auth,
            functions = functions)
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
              kotlinx.coroutines.delay(500)
            }
          }
        } else {
          kotlinx.coroutines.delay(500)
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

    // Wait for Firestore to update with retry
    val transcriptions =
        withTimeout(5000) {
          var result =
              repository.getTranscriptionsForMeeting(testProjectId, "$testMeetingId-list").first()
          while (result.isEmpty()) {
            kotlinx.coroutines.delay(200)
            result =
                repository.getTranscriptionsForMeeting(testProjectId, "$testMeetingId-list").first()
          }
          result
        }

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

    // Verify it's gone with retry
    val transcription =
        withTimeout(5000) {
          var result =
              repository
                  .getTranscriptionById(testProjectId, "$testMeetingId-delete", transcriptionId)
                  .first()
          while (result != null) {
            kotlinx.coroutines.delay(200)
            result =
                repository
                    .getTranscriptionById(testProjectId, "$testMeetingId-delete", transcriptionId)
                    .first()
          }
          result
        }

    assertNull("Transcription should be null after deletion", transcription)
  }
}
