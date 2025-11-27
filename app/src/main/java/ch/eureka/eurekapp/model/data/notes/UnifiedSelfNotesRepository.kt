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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * A unified repository that manages self-notes across Local Storage (Room) and Cloud (Firestore).
 *
 * It implements an Offline-First architecture:
 * 1. UI reads from Local Database (Single Source of Truth).
 * 2. Writes go to Local Database first.
 * 3. If Cloud Mode is ON, it attempts to sync to Firestore in the background.
 *
 * @property context The application context used to check network connectivity.
 * @property localDao The Data Access Object for interacting with the local Room database.
 * @property firestoreRepo The repository for interacting with the remote Firestore database.
 * @property userPreferences The repository for managing user preferences, such as the storage mode
 *   toggle.
 * @property auth The Firebase Authentication instance used to retrieve the current user's ID.
 */
class UnifiedSelfNotesRepository(
    private val context: Context,
    private val localDao: MessageDao,
    private val firestoreRepo: FirestoreSelfNotesRepository,
    private val userPreferences: UserPreferencesRepository,
    private val auth: FirebaseAuth
) : SelfNotesRepository {

  private fun getCurrentUserId(): String {
    return auth.currentUser?.uid ?: ""
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
      withContext(Dispatchers.IO) {
        try {
          val userId = getCurrentUserId()
          if (userId.isEmpty()) throw IllegalStateException("User not logged in")

          val messageWithUser = message.copy(senderId = userId)
          val isCloudEnabled = userPreferences.isCloudStorageEnabled.first()

          val entity =
              messageWithUser
                  .toEntity()
                  .copy(isPendingSync = isCloudEnabled, isPrivacyLocalOnly = !isCloudEnabled)

          // Always save locally first (Offline capability)
          localDao.insertMessage(entity)

          // Only attempt Cloud upload if Cloud is enabled AND we are online.
          if (isCloudEnabled) {
            if (isInternetAvailable()) {
              val result =
                  try {
                    firestoreRepo.createNote(messageWithUser)
                  } catch (e: Exception) {
                    return@withContext Result.failure(e)
                  }
              if (result.isSuccess) {
                try {
                  localDao.markAsSynced(message.messageID, userId)
                } catch (e: Exception) {
                  Log.e(
                      "UnifiedSelfNotesRepository",
                      "Failed to mark synced. Rolling back upload.",
                      e)
                  try {
                    firestoreRepo.deleteNote(message.messageID)
                  } catch (deleteEx: Exception) {
                    Log.e("UnifiedSelfNotesRepository", "Failed to rollback upload!", deleteEx)
                  }
                  throw e
                }
              }
            } else {
              Log.d("UnifiedSelfNotesRepository", "Offline: Note stored locally, pending sync.")
            }
          }

          Result.success(message.messageID)
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  override suspend fun deleteNote(noteId: String): Result<Unit> =
      withContext(Dispatchers.IO) {
        val userId = getCurrentUserId()
        localDao.deleteMessage(noteId, userId)
        // Only try to delete from cloud if online
        if (isInternetAvailable()) {
          firestoreRepo.deleteNote(noteId)
        }
        Result.success(Unit)
      }

  /**
   * Updates the user's preference for storage location (Local-only vs. Cloud).
   *
   * This function handles the logic for switching modes:
   * - Saves the new preference to DataStore.
   * - If switching TO Cloud mode (`enableCloud = true`), it finds all existing local notes that
   *   were previously marked as "private/local-only", marks them as pending sync, and immediately
   *   attempts to upload them.
   *
   * This method is main-safe as it wraps blocking database calls in [Dispatchers.IO].
   *
   * @param enableCloud True to enable Cloud storage and sync; False to revert to Local-only
   *   storage.
   * @return The number of notes successfully synced to the cloud during this operation (0 if
   *   disabling cloud).
   */
  suspend fun setStorageMode(enableCloud: Boolean): Int =
      withContext(Dispatchers.IO) {
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
   * This method queries the local database for notes that are pending upload (and not marked as
   * private). It attempts to upload each note to Firestore. If successful, it updates the local
   * database to mark the note as synced.
   *
   * This method is main-safe as it wraps blocking database calls in [Dispatchers.IO].
   *
   * @return The number of notes successfully uploaded.
   */
  suspend fun syncPendingNotes(): Int {
    // Explicitly switch to IO here to be safe regardless of where this is called from (Worker or
    // Main)
    return withContext(Dispatchers.IO) {
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
        }
      }
      successCount
    }
  }
}
