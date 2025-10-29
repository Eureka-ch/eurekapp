package ch.eureka.eurekapp.model.data.transcription

import ch.eureka.eurekapp.model.data.FirestorePaths
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Speech-to-Text Repository using Firebase Cloud Functions
 *
 * This calls a Cloud function to handle transcription server side.
 *
 * Note : minor parts of this file were written by GPT-5 (ChatGPT)
 * Co-author : GPT-5
 */
class CloudFunctionSpeechToTextRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val functions: FirebaseFunctions = FirebaseFunctions.getInstance()
) : SpeechToTextRepository {

  override suspend fun transcribeAudio(
      audioDownloadUrl: String,
      meetingId: String,
      projectId: String,
      languageCode: String
  ): Result<String> = runCatching {
    auth.currentUser?.uid ?: throw SecurityException("User must be authenticated")

    val transcriptionId =
        firestore.collection(FirestorePaths.transcriptionsPath(projectId, meetingId)).document().id

    val data =
        hashMapOf(
            "audioDownloadUrl" to audioDownloadUrl,
            "meetingId" to meetingId,
            "projectId" to projectId,
            "languageCode" to languageCode,
            "transcriptionId" to transcriptionId)

    val result = functions.getHttpsCallable("transcribeAudio").call(data).await()

    val resultData =
        result.data as? Map<*, *>
            ?: throw IllegalStateException("Invalid response from Cloud Function")

    val success = resultData["success"] as? Boolean ?: false
    if (!success) {
      val errorMessage = resultData["error"] as? String ?: "Unknown error"
      throw Exception("Cloud Function returned success=false: $errorMessage")
    }

    val returnedTranscriptionId =
        resultData["transcriptionId"] as? String
            ?: throw IllegalStateException("Missing transcriptionId in Cloud Function response")

    returnedTranscriptionId
  }

  override fun getTranscriptionsForMeeting(
      projectId: String,
      meetingId: String
  ): Flow<List<AudioTranscription>> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.transcriptionsPath(projectId, meetingId))
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val transcriptions =
                  snapshot?.documents?.mapNotNull { it.toObject(AudioTranscription::class.java) }
                      ?: emptyList()
              trySend(transcriptions)
            }
    awaitClose { listener.remove() }
  }

  override fun getTranscriptionById(
      projectId: String,
      meetingId: String,
      transcriptionId: String
  ): Flow<AudioTranscription?> = callbackFlow {
    val listener =
        firestore
            .collection(FirestorePaths.transcriptionsPath(projectId, meetingId))
            .document(transcriptionId)
            .addSnapshotListener { snapshot, error ->
              if (error != null) {
                close(error)
                return@addSnapshotListener
              }
              val transcription = snapshot?.toObject(AudioTranscription::class.java)
              trySend(transcription)
            }
    awaitClose { listener.remove() }
  }

  override suspend fun deleteTranscription(
      projectId: String,
      meetingId: String,
      transcriptionId: String
  ): Result<Unit> = runCatching {
    firestore
        .collection(FirestorePaths.transcriptionsPath(projectId, meetingId))
        .document(transcriptionId)
        .delete()
        .await()
  }
}
