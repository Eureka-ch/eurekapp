/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.model.database.MessageDao
import ch.eureka.eurekapp.model.database.toDomainModel
import ch.eureka.eurekapp.model.database.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * A unified repository that manages self-notes across Local Storage (Room) and Cloud (Firestore).
 *
 * @property context The application context used to check network connectivity.
 * @property localDao The Data Access Object for interacting with the local Room database.
 * @property firestoreRepo The repository for interacting with the remote Firestore database.
 * @property userPreferences The repository for managing user preferences, such as the storage mode
 *   toggle.
 * @property auth The Firebase Authentication instance used to retrieve the current user's ID.
 * @property dispatcher Default dispatcher used in [UnifiedSelfNotesRepository].
 */
class UnifiedSelfNotesRepository(
    private val context: Context,
    private val localDao: MessageDao,
    private val firestoreRepo: FirestoreSelfNotesRepository,
    private val userPreferences: UserPreferencesRepository,
    private val auth: FirebaseAuth,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SelfNotesRepository {

  private fun getCurrentUserId(): String {
    return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
  }

  private fun isInternetAvailable(): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  override fun getNotes(limit: Int): Flow<List<Message>> {
    val userId = getCurrentUserId()
    return localDao.getMessagesForUser(userId).map { entities ->
      entities.map { it.toDomainModel() }
    }
  }

  override suspend fun createNote(message: Message): Result<String> =
      withContext(dispatcher) {
        try {
          val userId = getCurrentUserId()
          check(!(userId.isEmpty())) { "User not logged in" }

          val isCloudEnabled = userPreferences.isCloudStorageEnabled.first()
          val messageWithUser = message.copy(senderId = userId)

          saveNoteLocally(messageWithUser, isCloudEnabled)

          if (isCloudEnabled) {
            handleCloudSync(messageWithUser, userId)
          }
          Result.success(message.messageID)
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  private fun saveNoteLocally(message: Message, isCloudEnabled: Boolean) {
    val entity =
        message
            .toEntity()
            .copy(isPendingSync = isCloudEnabled, isPrivacyLocalOnly = !isCloudEnabled)
    localDao.insertMessage(entity)
  }

  private suspend fun handleCloudSync(message: Message, userId: String) {
    if (!isInternetAvailable()) {
      Log.d("UnifiedSelfNotesRepository", "Offline: Note stored locally, pending sync.")
      return
    }

    val cloudResult = firestoreRepo.createNote(message)

    if (cloudResult.isFailure) {
      throw cloudResult.exceptionOrNull()!!
    }

    finalizeSyncWithRollback(message.messageID, userId)
  }

  private suspend fun finalizeSyncWithRollback(noteId: String, userId: String) {
    try {
      localDao.markAsSynced(noteId, userId)
    } catch (e: Exception) {
      Log.e("UnifiedSelfNotesRepository", "Failed to mark synced. Rolling back upload.", e)
      performRollback(noteId)
      throw e // Re-throw to ensure the main function returns Result.failure
    }
  }

  private suspend fun performRollback(noteId: String) {
    try {
      firestoreRepo.deleteNote(noteId)
    } catch (deleteEx: Exception) {
      Log.e("UnifiedSelfNotesRepository", "Failed to rollback upload!", deleteEx)
    }
  }

  override suspend fun deleteNote(noteId: String): Result<Unit> =
      withContext(dispatcher) {
        val userId = getCurrentUserId()
        localDao.deleteMessage(noteId, userId)
        // Only try to delete from cloud if online
        if (isInternetAvailable()) {
          val result = firestoreRepo.deleteNote(noteId)
          if (result.isFailure) {
            Log.e(
                "UnifiedSelfNotesRepository",
                "Cloud deletion failed: $noteId",
                result.exceptionOrNull())
            return@withContext Result.failure(result.exceptionOrNull()!!)
          }
        }
        Result.success(Unit)
      }

  /**
   * Updates the user's preference for storage location (Local-only vs. Cloud).
   *
   * @param enableCloud True to enable Cloud storage and sync; False to revert to Local-only
   *   storage.
   * @return The number of notes successfully synced to the cloud during this operation (0 if
   *   disabling cloud).
   */
  suspend fun setStorageMode(enableCloud: Boolean): Int =
      withContext(dispatcher) {
        userPreferences.setCloudStorageEnabled(enableCloud)

        var syncedCount = 0
        if (enableCloud) {
          val userId = getCurrentUserId()
          // If switching to Cloud, mark all existing local private notes as "Ready for Sync"
          localDao.makeAllMessagesPublicForUser(userId)
          // Trigger a sync and return the count
          if (isInternetAvailable()) {
            syncedCount = syncPendingNotes()
          }
        }
        syncedCount
      }

  /**
   * Uploads all notes marked as 'isPendingSync' to the cloud.
   *
   * @return The number of notes successfully uploaded.
   */
  suspend fun syncPendingNotes(): Int {
    return withContext(dispatcher) {
      if (!isInternetAvailable()) return@withContext 0

      val userId = getCurrentUserId()
      if (userId.isEmpty()) return@withContext 0

      val pendingNotes = localDao.getPendingSyncMessages(userId)
      if (pendingNotes.isNotEmpty()) {
        Log.d("UnifiedRepo", "Found ${pendingNotes.size} notes to sync")
      }

      var successCount = 0
      pendingNotes.forEach { entity ->
        val domainMessage = entity.toDomainModel()
        val result = firestoreRepo.createNote(domainMessage)
        if (result.isSuccess) {
          localDao.markAsSynced(entity.messageId, userId)
          successCount++
        } else {
          Log.e("UnifiedSelfNotesRepository", "Failed to upload pending notes.")
        }
      }
      successCount
    }
  }
}
