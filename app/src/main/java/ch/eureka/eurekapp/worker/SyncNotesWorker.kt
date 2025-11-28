/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import com.google.firebase.auth.FirebaseAuthException
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * A background worker that handles the synchronization of local self-notes to the cloud.
 *
 * @param context The application context, provided by WorkManager.
 * @param params Parameters for the worker, provided by WorkManager.
 * @param dispatcher Default dispatcher used in [SyncNotesWorker].
 */
class SyncNotesWorker(
    context: Context,
    params: WorkerParameters,
    val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result =
      withContext(dispatcher) {
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
