/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.model.database.MessageDao
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import ch.eureka.eurekapp.model.database.toDomainModel
import ch.eureka.eurekapp.model.database.toEntity
import ch.eureka.eurekapp.worker.SyncNotesWorker
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Simple data class to report synchronization results. */
data class SyncStats(val upserts: Int = 0, val deletes: Int = 0) {
  val total: Int
    get() = upserts + deletes
}

/**
 * A unified repository that manages self-notes across Local Storage (Room) and Cloud (Firestore).
 *
 * This repository implements an "Offline-First" architecture. It serves data primarily from the
 * local database (Room) to ensure instant UI updates and offline accessibility. Background
 * synchronization logic handles pushing changes to the cloud (Firestore) and pulling remote changes
 * down, respecting user privacy settings (Local vs. Cloud storage modes).
 *
 * @property context The application context used to check network connectivity.
 * @property localDao The Data Access Object for interacting with the local Room database.
 * @property firestoreRepo The repository for interacting with the remote Firestore database.
 * @property userPreferences The repository for managing user preferences, such as the storage mode
 *   toggle.
 * @property auth The Firebase Authentication instance used to retrieve the current user's ID.
 * @property dispatcher The coroutine dispatcher for background operations (default: IO).
 */
class UnifiedSelfNotesRepository(
    private val context: Context,
    private val localDao: MessageDao,
    private val firestoreRepo: FirestoreSelfNotesRepository,
    private val userPreferences: UserPreferencesRepository,
    private val auth: FirebaseAuth,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SelfNotesRepository {

  private val repositoryScope = CoroutineScope(SupervisorJob() + dispatcher)

  init {
    startObservingRemoteNotes()
  }

  /**
   * Retrieves the current authenticated user's ID.
   *
   * @return The unique User ID (UID) string.
   * @throws IllegalStateException If no user is currently signed in.
   */
  private fun getCurrentUserId(): String {
    return auth.currentUser?.uid ?: throw IllegalStateException("User must be authenticated")
  }

  /**
   * Checks if the device currently has an active internet connection.
   *
   * @return True if the device is connected to the internet, false otherwise.
   */
  private fun isInternetAvailable(): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  /**
   * Starts observing remote Firestore notes when Cloud Mode is enabled.
   *
   * This function sets up a background flow that listens to the user's storage preference. If Cloud
   * Mode is enabled, it subscribes to real-time updates from Firestore and passes incoming data to
   * [saveRemoteNotesLocally].
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun startObservingRemoteNotes() {
    repositoryScope.launch {
      try {
        userPreferences.isCloudStorageEnabled
            .flatMapLatest { isCloudEnabled ->
              // Only sync if enabled AND user is logged in
              if (isCloudEnabled && auth.currentUser != null) {
                firestoreRepo.getNotes()
              } else {
                emptyFlow()
              }
            }
            .collectLatest { remoteNotes -> saveRemoteNotesLocally(remoteNotes) }
      } catch (e: Exception) {
        Log.e("UnifiedSelfNotesRepository", "Failed to start observing remote notes", e)
      }
    }
  }

  /**
   * Saves a list of notes fetched from the remote source (Firestore) into the local database.
   *
   * This function implements a conflict resolution strategy where **Local Edits Win**. It checks
   * for any local notes that are currently pending sync (i.e., user has edited them but they
   * haven't been uploaded yet) and ensures they are NOT overwritten by the incoming cloud data.
   *
   * @param remoteNotes The list of [Message] objects retrieved from Firestore.
   */
  private fun saveRemoteNotesLocally(remoteNotes: List<Message>) {
    if (remoteNotes.isEmpty()) return

    try {
      val userId = getCurrentUserId()

      val pendingIds = localDao.getPendingSyncMessageIds(userId).toSet()

      val notesToUpsert = ArrayList<MessageEntity>()

      for (remoteMessage in remoteNotes) {
        if (remoteMessage.messageID in pendingIds) continue

        val existingEntity = localDao.getMessageById(remoteMessage.messageID, userId)

        val entity =
            if (existingEntity != null) {
              remoteMessage
                  .toEntity()
                  .copy(
                      localId = existingEntity.localId,
                      isPendingSync = false,
                      isPrivacyLocalOnly = false,
                      isDeleted = false)
            } else {
              remoteMessage
                  .toEntity()
                  .copy(isPendingSync = false, isPrivacyLocalOnly = false, isDeleted = false)
            }
        notesToUpsert.add(entity)
      }

      if (notesToUpsert.isNotEmpty()) {
        localDao.insertMessages(notesToUpsert)
      }
    } catch (e: Exception) {
      Log.e("UnifiedSelfNotesRepository", "Error syncing down remote notes", e)
    }
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
          check(userId.isNotEmpty()) { "User not logged in" }

          val isCloudEnabled = userPreferences.isCloudStorageEnabled.first()
          val messageWithUser = message.copy(senderId = userId)

          saveNoteLocally(messageWithUser, isCloudEnabled)

          if (isCloudEnabled) {
            if (isInternetAvailable()) {
              handleCloudUpsert(messageWithUser, userId)
            } else {
              scheduleWorker()
            }
          }
          Result.success(message.messageID)
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  override suspend fun updateNote(messageId: String, newText: String): Result<Unit> =
      withContext(dispatcher) {
        try {
          val userId = getCurrentUserId()
          val existingEntity =
              localDao.getMessageById(messageId, userId)
                  ?: return@withContext Result.failure(Exception("Note not found"))

          val isCloudNote = !existingEntity.isPrivacyLocalOnly

          localDao.updateMessageText(messageId, userId, newText, isPendingSync = isCloudNote)

          if (isCloudNote) {
            if (isInternetAvailable()) {
              val cloudResult = firestoreRepo.updateNote(messageId, newText)
              if (cloudResult.isSuccess) {
                localDao.markAsSynced(messageId, userId)
              } else {
                scheduleWorker()
              }
            } else {
              scheduleWorker()
            }
          }
          Result.success(Unit)
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  /**
   * Helper function to save a note to the local database.
   *
   * @param message The domain message object.
   * @param isCloudEnabled Whether cloud storage is currently enabled (determines privacy flags).
   */
  private fun saveNoteLocally(message: Message, isCloudEnabled: Boolean) {
    val entity =
        message
            .toEntity()
            .copy(
                isPendingSync = isCloudEnabled,
                isPrivacyLocalOnly = !isCloudEnabled,
                isDeleted = false)
    localDao.insertMessage(entity)
  }

  /**
   * Handles the immediate upload (upsert) of a note to Firestore.
   *
   * @param message The message to upload.
   * @param userId The ID of the user.
   * @throws Exception if the network call fails.
   */
  private suspend fun handleCloudUpsert(message: Message, userId: String) {
    if (!isInternetAvailable()) return

    val cloudResult = firestoreRepo.createNote(message)

    if (cloudResult.isFailure) {
      scheduleWorker()
      throw cloudResult.exceptionOrNull()!!
    }

    finalizeSyncWithRollback(message.messageID, userId)
  }

  /**
   * Marks a note as synced locally after a successful cloud operation. If this local update fails,
   * it attempts to rollback the cloud operation to maintain consistency.
   *
   * @param noteId The ID of the note.
   * @param userId The ID of the user.
   */
  private suspend fun finalizeSyncWithRollback(noteId: String, userId: String) {
    try {
      localDao.markAsSynced(noteId, userId)
    } catch (e: Exception) {
      Log.e("UnifiedSelfNotesRepository", "Failed to mark synced. Rolling back upload.", e)
      performRollback(noteId)
      throw e // Re-throw to ensure the main function returns Result.failure
    }
  }

  /**
   * Rolls back a cloud creation by deleting the note from Firestore. Used when the local database
   * fails to update the sync status.
   *
   * @param noteId The ID of the note to delete from cloud.
   */
  private suspend fun performRollback(noteId: String) {
    try {
      firestoreRepo.deleteNote(noteId)
    } catch (deleteEx: Exception) {
      Log.e("UnifiedSelfNotesRepository", "Failed to rollback upload!", deleteEx)
    }
  }

  /**
   * Deletes a note.
   *
   * Behavior depends on the note type:
   * - **Local-Only:** Immediately deletes the row from the local database.
   * - **Cloud Note:** Performs a "Soft Delete" locally (hides from UI, marks pending sync) and
   *   attempts to delete from Firestore. If offline, the worker handles the cloud deletion later.
   *
   * @param noteId The ID of the note to delete.
   * @return A [Result] indicating success.
   */
  override suspend fun deleteNote(noteId: String): Result<Unit> =
      withContext(dispatcher) {
        val userId = getCurrentUserId()

        val existingEntity =
            localDao.getMessageById(noteId, userId) ?: return@withContext Result.success(Unit)

        if (existingEntity.isPrivacyLocalOnly) {
          localDao.deleteMessage(noteId, userId)
          return@withContext Result.success(Unit)
        }

        localDao.markAsDeleted(noteId, userId)

        if (isInternetAvailable()) {
          val result = firestoreRepo.deleteNote(noteId)
          if (result.isSuccess) {
            localDao.deleteMessage(noteId, userId)
          } else {
            Log.e(
                "UnifiedSelfNotesRepository",
                "Cloud delete failed, scheduling worker",
                result.exceptionOrNull())
            scheduleWorker()
          }
        } else {
          scheduleWorker()
        }
        Result.success(Unit)
      }

  /**
   * Updates the user's preference for storage location (Local-only vs. Cloud).
   * - **Switching to Cloud:** Marks all local notes as pending sync and triggers an upload.
   * - **Switching to Local:** Deletes all notes from the Cloud (for privacy) and marks local notes
   *   as private.
   *
   * @param enableCloud True to enable Cloud storage; False to revert to Local-only.
   * @return The number of notes successfully synced to the cloud during this operation (0 if
   *   disabling cloud).
   */
  suspend fun setStorageMode(enableCloud: Boolean): SyncStats =
      withContext(dispatcher) {
        userPreferences.setCloudStorageEnabled(enableCloud)

        var stats = SyncStats(0, 0)
        val userId = getCurrentUserId()

        if (enableCloud) {
          localDao.makeAllMessagesPublicForUser(userId)
          if (isInternetAvailable()) {
            stats = syncPendingNotes()
          }
        } else {
          if (isInternetAvailable()) {
            try {
              val cloudNotes = firestoreRepo.getNotes().first()
              cloudNotes.forEach { note -> firestoreRepo.deleteNote(note.messageID) }
              Log.d(
                  "UnifiedSelfNotesRepository",
                  "Deleted ${cloudNotes.size} notes from cloud due to local switch.")
            } catch (e: Exception) {
              Log.e("UnifiedSelfNotesRepository", "Failed to clear cloud notes", e)
            }
          }

          val localEntities = localDao.getMessagesForUser(userId).first()
          val privateEntities =
              localEntities.map { it.copy(isPrivacyLocalOnly = true, isPendingSync = false) }
          if (privateEntities.isNotEmpty()) {
            localDao.insertMessages(privateEntities)
          }
        }
        stats
      }

  /**
   * Uploads (or deletes) all notes marked as 'isPendingSync' to the cloud. This is called by the
   * [SyncNotesWorker] for background synchronization.
   *
   * It handles two cases based on the pending note's state:
   * 1. **Soft Deleted (`isDeleted = 1`):** Deletes from Firestore, then hard deletes locally.
   * 2. **Created/Updated:** Upserts to Firestore, then marks as synced locally.
   *
   * @return The number of notes successfully processed (uploaded or deleted).
   */
  suspend fun syncPendingNotes(): SyncStats {
    return withContext(dispatcher) {
      if (!isInternetAvailable()) return@withContext SyncStats(0, 0)

      val userId = getCurrentUserId()
      if (userId.isEmpty()) return@withContext SyncStats(0, 0)

      val pendingNotes = localDao.getPendingSyncMessages(userId)
      if (pendingNotes.isNotEmpty()) {
        Log.d("UnifiedSelfNotesRepository", "Processing ${pendingNotes.size} pending items")
      }

      var successDeletes = 0
      var successUpserts = 0

      pendingNotes.forEach { entity ->
        if (entity.isDeleted) {
          val result = firestoreRepo.deleteNote(entity.messageId)
          if (result.isSuccess) {
            localDao.deleteMessage(entity.messageId, userId)
            successDeletes++
          } else {
            Log.e("UnifiedSelfNotesRepository", "Retry failed for delete: ${entity.messageId}")
          }
        } else {
          val domainMessage = entity.toDomainModel()
          val result = firestoreRepo.createNote(domainMessage)
          if (result.isSuccess) {
            localDao.markAsSynced(entity.messageId, userId)
            successUpserts++
          } else {
            Log.e("UnifiedSelfNotesRepository", "Retry failed for upsert: ${entity.messageId}")
          }
        }
      }
      SyncStats(upserts = successUpserts, deletes = successDeletes)
    }
  }

  /**
   * Schedules a [SyncNotesWorker] to run as soon as the network is connected. Used when an
   * operation fails due to lack of internet or API errors.
   */
  private fun scheduleWorker() {
    val request =
        OneTimeWorkRequestBuilder<SyncNotesWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

    WorkManager.getInstance(context)
        .enqueueUniqueWork("SyncNotes", ExistingWorkPolicy.KEEP, request)
  }
}
