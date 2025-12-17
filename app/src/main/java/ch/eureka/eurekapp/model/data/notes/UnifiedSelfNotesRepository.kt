/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.model.data.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.model.database.MessageDao
import ch.eureka.eurekapp.model.database.toDomainModel
import ch.eureka.eurekapp.model.database.toEntity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * A repository that implements the strict separation between Local and Cloud storage for Self
 * Notes.
 *
 * This repository manages the data flow based on the user's selected storage mode (Local vs.
 * Cloud). Unlike previous iterations, toggling modes does not trigger data synchronization or
 * migration. Instead, it strictly switches the data source being accessed.
 *
 * **Logic Overview:**
 * - **Local Mode:** All Read/Write operations target the Room Database (`local_notes` table) only.
 * - **Cloud Mode:** All Read/Write operations target Firestore only.
 * - **Offline Behavior:** If the user is in Cloud Mode and offline, write operations (Create,
 *   Update, Delete) are blocked to prevent data inconsistency, prompting the user to switch to
 *   Local Mode if immediate access is needed.
 *
 * @property context The application context, used to check for network connectivity.
 * @property localDao The Data Access Object for interacting with the local Room database.
 * @property firestoreRepo The repository for interacting with the remote Firestore database.
 * @property userPreferences The repository for observing and toggling the storage mode preference.
 * @property auth The Firebase Authentication instance, used to retrieve the current user's ID.
 * @property dispatcher The coroutine dispatcher used for background operations (defaults to
 *   [Dispatchers.IO]).
 */
class UnifiedSelfNotesRepository(
    private val context: Context,
    private val localDao: MessageDao,
    private val firestoreRepo: FirestoreSelfNotesRepository,
    private val userPreferences: UserPreferencesRepository,
    private val auth: FirebaseAuth,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SelfNotesRepository {

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
   * @return `true` if the device has a network connection with internet capabilities, `false`
   *   otherwise.
   */
  private fun isInternetAvailable(): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
  }

  /**
   * Returns a Flow of notes based on the user's currently selected storage mode.
   *
   * This flow reacts dynamically to changes in the
   * [UserPreferencesRepository.isCloudStorageEnabled] preference.
   * - If Cloud is enabled: Returns a flow directly from [FirestoreSelfNotesRepository].
   * - If Local is enabled: Returns a flow directly from [MessageDao], mapping entities to domain
   *   models.
   *
   * @param limit The maximum number of notes to retrieve.
   * @return A [Flow] emitting lists of [Message] objects ordered by creation time.
   */
  @OptIn(ExperimentalCoroutinesApi::class)
  override fun getNotes(limit: Int): Flow<List<Message>> {
    return userPreferences.isCloudStorageEnabled.flatMapLatest { isCloudEnabled ->
      if (isCloudEnabled) {
        // Direct Flow from Firestore
        firestoreRepo.getNotes(limit)
      } else {
        // Direct Flow from Room
        val userId = getCurrentUserId()
        localDao.getMessagesForUser(userId).map { entities -> entities.map { it.toDomainModel() } }
      }
    }
  }

  /**
   * Creates a new note in the currently active storage.
   * - **Cloud Mode:** Checks for internet connectivity. If online, creates the note in Firestore.
   *   If offline, returns failure.
   * - **Local Mode:** Creates the note in the local Room database.
   *
   * @param message The [Message] object containing the note content.
   * @return A [Result] containing the message ID on success, or an exception on failure (e.g.,
   *   offline in cloud mode).
   */
  override suspend fun createNote(message: Message): Result<String> =
      withContext(dispatcher) {
        try {
          val userId = getCurrentUserId()
          val isCloudEnabled = userPreferences.isCloudStorageEnabled.first()
          val messageWithUser = message.copy(senderId = userId)

          if (isCloudEnabled) {
            if (!isInternetAvailable()) {
              return@withContext Result.failure(
                  Exception("You are offline. Switch to Local mode to save notes."))
            }
            // Direct write to Cloud
            firestoreRepo.createNote(messageWithUser)
          } else {
            // Direct write to Local Room
            localDao.insertMessage(messageWithUser.toEntity())
            Result.success(messageWithUser.messageID)
          }
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  /**
   * Updates the content of an existing note in the currently active storage.
   * - **Cloud Mode:** Checks for internet connectivity. If online, updates the note in Firestore.
   *   If offline, returns failure.
   * - **Local Mode:** Updates the note in the local Room database.
   *
   * @param messageId The unique identifier of the note to update.
   * @param newText The new text content for the note.
   * @return A [Result] indicating success or failure.
   */
  override suspend fun updateNote(messageId: String, newText: String): Result<Unit> =
      withContext(dispatcher) {
        try {
          val isCloudEnabled = userPreferences.isCloudStorageEnabled.first()

          if (isCloudEnabled) {
            if (!isInternetAvailable()) {
              return@withContext Result.failure(Exception("Cannot edit cloud notes while offline."))
            }
            firestoreRepo.updateNote(messageId, newText)
          } else {
            val userId = getCurrentUserId()
            localDao.updateMessageText(messageId, userId, newText)
            Result.success(Unit)
          }
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  /**
   * Deletes a note from the currently active storage.
   * - **Cloud Mode:** Checks for internet connectivity. If online, deletes the note from Firestore.
   *   If offline, returns failure.
   * - **Local Mode:** Deletes the note from the local Room database.
   *
   * @param noteId The unique identifier of the note to delete.
   * @return A [Result] indicating success or failure.
   */
  override suspend fun deleteNote(noteId: String): Result<Unit> =
      withContext(dispatcher) {
        try {
          val isCloudEnabled = userPreferences.isCloudStorageEnabled.first()

          if (isCloudEnabled) {
            if (!isInternetAvailable()) {
              return@withContext Result.failure(
                  Exception("Cannot delete cloud notes while offline."))
            }
            firestoreRepo.deleteNote(noteId)
          } else {
            val userId = getCurrentUserId()
            localDao.deleteMessage(noteId, userId)
            Result.success(Unit)
          }
        } catch (e: Exception) {
          Result.failure(e)
        }
      }

  /**
   * Toggles the user's preferred storage mode.
   *
   * This updates the [UserPreferencesRepository]. It serves as a strict view switch and does
   * **not** trigger any background synchronization or data migration between Local and Cloud.
   *
   * @param enableCloud `true` to enable Cloud Mode (Firestore), `false` to enable Local Mode
   *   (Room).
   */
  suspend fun setStorageMode(enableCloud: Boolean) {
    withContext(dispatcher) { userPreferences.setCloudStorageEnabled(enableCloud) }
  }
}
