/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.eureka.eurekapp.model.data.RepositoriesProvider
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
        } catch (e: Exception) {
          e.printStackTrace()
          Result.retry()
        }
      }
}
