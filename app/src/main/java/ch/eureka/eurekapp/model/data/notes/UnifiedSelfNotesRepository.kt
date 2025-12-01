/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.model.database.MessageDao
import ch.eureka.eurekapp.model.database.entities.MessageEntity
import ch.eureka.eurekapp.model.database.toDomainModel
import ch.eureka.eurekapp.model.database.toEntity
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

  // Scope for background sync operations (independent of UI lifecycle)
  private val repositoryScope = CoroutineScope(SupervisorJob() + dispatcher)

  init {
    startObservingRemoteNotes()
  }

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

  /**
   * Starts observing remote Firestore notes when Cloud Mode is enabled. This ensures that notes
   * from other devices (or restored notes) are synced down to Local DB.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  private fun startObservingRemoteNotes() {
    repositoryScope.launch {
      userPreferences.isCloudStorageEnabled
          .flatMapLatest { isCloudEnabled ->
            if (isCloudEnabled && auth.currentUser != null) {
              firestoreRepo.getNotes()
            } else {
              emptyFlow()
            }
          }
          .collectLatest { remoteNotes -> saveRemoteNotesLocally(remoteNotes) }
    }
  }

  /**
   * Saves a list of notes fetched from the remote source (Firestore) into the local database.
   *
   * This function implements a conflict resolution strategy where **Local Edits Win**. It checks
   * for any local notes that are currently pending sync (i.e., user has edited them but they
   * haven't been uploaded yet) and ensures they are NOT overwritten by the incoming cloud data.
   *
   * @param remoteNotes The list of notes retrieved from Firestore.
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
                      isPrivacyLocalOnly = false)
            } else {
              remoteMessage.toEntity().copy(isPendingSync = false, isPrivacyLocalOnly = false)
            }
        notesToUpsert.add(entity)
      }

      if (notesToUpsert.isNotEmpty()) {
        localDao.insertMessages(notesToUpsert)
      }
    } catch (e: Exception) {
      Log.e("UnifiedRepo", "Error syncing down remote notes", e)
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
            handleCloudSync(messageWithUser, userId)
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

          // Determine if we need to sync this update to cloud
          val isCloudNote = !existingEntity.isPrivacyLocalOnly

          // Update Locally
          localDao.updateMessageText(messageId, userId, newText, isPendingSync = isCloudNote)

          // Update Cloud (if applicable and online)
          if (isCloudNote) {
            if (isInternetAvailable()) {
              val cloudResult = firestoreRepo.updateNote(messageId, newText)
              if (cloudResult.isSuccess) {
                localDao.markAsSynced(messageId, userId)
              } else {
                Log.e(
                    "UnifiedRepo",
                    "Cloud update failed, note remains pending sync",
                    cloudResult.exceptionOrNull())
              }
            } else {
              Log.d("UnifiedRepo", "Offline: Note update stored locally, pending sync.")
            }
          }
          Result.success(Unit)
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

        // Delete Locally
        localDao.deleteMessage(noteId, userId)

        // Delete from Cloud (Attempt best effort)
        if (isInternetAvailable()) {
          val result = firestoreRepo.deleteNote(noteId)
          if (result.isFailure) {
            Log.e(
                "UnifiedSelfNotesRepository",
                "Cloud deletion failed: $noteId",
                result.exceptionOrNull())
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
        val userId = getCurrentUserId()

        if (enableCloud) {
          localDao.makeAllMessagesPublicForUser(userId)
          if (isInternetAvailable()) {
            syncedCount = syncPendingNotes()
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
