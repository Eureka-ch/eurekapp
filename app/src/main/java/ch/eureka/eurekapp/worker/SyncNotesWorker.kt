/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import com.google.firebase.auth.FirebaseAuthException
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A background worker that handles the synchronization of local self-notes to the cloud.
 *
 * This worker is scheduled by the [ch.eureka.eurekapp.ui.notes.SelfNotesViewModel] when a note is
 * created while offline or when the app needs to ensure data integrity. It checks for any notes in
 * the local database marked as `pendingSync` and attempts to upload them to Firestore.
 *
 * @param context The application context, provided by WorkManager.
 * @param params Parameters for the worker, provided by WorkManager.
 */
class SyncNotesWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

  override suspend fun doWork(): Result =
      withContext(Dispatchers.IO) {
        try {
          val repository = RepositoriesProvider.unifiedSelfNotesRepository

          repository.syncPendingNotes()
          Result.success()
        } catch (e: FirebaseAuthException) {
          Log.e("SyncNotesWorker", "Auth failed - won't retry", e)
          Result.failure()
        } catch (e: IOException) {
          Log.e("SyncNotesWorker", "Network error on attempt $runAttemptCount", e)
          if (runAttemptCount < 3) Result.retry() else Result.failure()
        } catch (e: Exception) {
          Log.e("SyncNotesWorker", "Unexpected error - won't retry", e)
          Result.failure()
        }
      }
}
