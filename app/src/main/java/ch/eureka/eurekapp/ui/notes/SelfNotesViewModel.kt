/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ch.eureka.eurekapp.model.connection.ConnectivityObserverProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.notes.UnifiedSelfNotesRepository
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
import ch.eureka.eurekapp.worker.SyncNotesWorker
import com.google.firebase.Timestamp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Self Notes screen.
 *
 * This ViewModel manages the UI state for the "Note to Self" feature. It handles:
 * - Loading notes from the [UnifiedSelfNotesRepository].
 * - Creating and sending new notes.
 * - Toggling between Local-only and Cloud storage modes.
 * - observing network connectivity to trigger synchronization of offline notes.
 * - Scheduling background work for robust synchronization.
 *
 * @property repository The repository for managing notes (Unified Local + Cloud).
 * @property userPrefs The repository for accessing user preferences (storage mode).
 * @property workManager The system WorkManager for scheduling background sync tasks.
 * @property dispatcher The coroutine dispatcher for background operations (default: IO).
 */
class SelfNotesViewModel
@JvmOverloads
constructor(
    private val repository: UnifiedSelfNotesRepository =
        RepositoriesProvider.unifiedSelfNotesRepository,
    private val userPrefs: UserPreferencesRepository =
        RepositoriesProvider.userPreferencesRepository,
    private val workManager: WorkManager = RepositoriesProvider.workManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _infoMsg = MutableStateFlow<String?>(null)

  /**
   * The single source of truth for the UI state.
   *
   * Combines data from multiple flows:
   * - Notes from the repository
   * - Current text input
   * - Sending status
   * - Error/Status messages
   * - User's cloud storage preference
   */
  val uiState: StateFlow<SelfNotesUIState> =
      combine(
              repository
                  .getNotes()
                  .map { notes -> NotesLoadState.Success(notes) as NotesLoadState }
                  .catch { e -> emit(NotesLoadState.Error(e.message ?: "Error")) },
              _currentMessage,
              _isSending,
              _infoMsg,
              userPrefs.isCloudStorageEnabled) {
                  notesState,
                  currentMessage,
                  isSending,
                  errorMsg,
                  isCloud ->
                val notes =
                    when (notesState) {
                      is NotesLoadState.Success -> notesState.notes
                      is NotesLoadState.Error -> emptyList()
                    }

                val displayedError =
                    if (notesState is NotesLoadState.Error) {
                      notesState.message
                    } else {
                      errorMsg
                    }

                SelfNotesUIState(
                    notes = notes,
                    isLoading = false,
                    errorMsg = displayedError,
                    currentMessage = currentMessage,
                    isSending = isSending,
                    isCloudStorageEnabled = isCloud)
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = SelfNotesUIState(isLoading = true))

  init {
    // Monitor Connectivity to trigger Sync
    viewModelScope.launch {
      try {
        // Safe call in case ConnectivityObserver isn't initialized
        ConnectivityObserverProvider.connectivityObserver.isConnected.collect { isOnline ->
          if (isOnline) {
            val syncedCount = repository.syncPendingNotes()
            if (syncedCount > 0) {
              _infoMsg.value = "Back online: Uploaded $syncedCount notes"
            }
          }
        }
      } catch (e: Exception) {
        Log.e("SelfNotesViewModel", "Connectivity Observer error", e)
      }
    }
  }

  /**
   * Toggles the storage mode between Local-only and Cloud.
   *
   * When enabling Cloud mode, this triggers an immediate sync of existing local notes. Updates the
   * [_infoMsg] flow with a status message upon completion.
   *
   * @param enableCloud True to enable Cloud storage/sync, False for Local-only.
   */
  fun toggleStorageMode(enableCloud: Boolean) {
    viewModelScope.launch {
      val syncedCount = repository.setStorageMode(enableCloud)
      if (enableCloud) {
        if (syncedCount > 0) {
          _infoMsg.value = "Switched to Cloud: Synced $syncedCount notes"
        } else {
          _infoMsg.value = "Switched to Cloud Storage"
        }
      } else {
        _infoMsg.value = "Switched to Local Storage (Private)"
      }
    }
  }

  /**
   * Updates the text of the message currently being composed.
   *
   * @param text The new text entered by the user.
   */
  fun updateMessage(text: String) {
    _currentMessage.value = text
  }

  /**
   * Creates and saves a new note.
   *
   * The note is created with the current input text. It is saved via the repository, which handles
   * local storage and potential cloud upload. If successful, clears the input and schedules a
   * background worker for redundancy.
   */
  fun sendNote() {
    val currentMessage = _currentMessage.value.trim()
    if (currentMessage.isEmpty()) return
    if (currentMessage.length > 5000) {
      _infoMsg.value = "Note too long (max 5000 characters)"
      return
    }

    _isSending.value = true

    viewModelScope.launch(dispatcher) {
      val message =
          Message(
              messageID = IdGenerator.generateMessageId(),
              text = currentMessage,
              createdAt = Timestamp.now())

      repository
          .createNote(message)
          .fold(
              onSuccess = {
                _currentMessage.value = ""
                _isSending.value = false
                _infoMsg.value = null
                if (uiState.value.isCloudStorageEnabled) {
                  scheduleWorker()
                }
              },
              onFailure = { error ->
                _isSending.value = false
                _infoMsg.value = "Error: ${error.message}"
              })
    }
  }

  /**
   * Schedules a [SyncNotesWorker] to run when the device has network connectivity.
   *
   * This acts as a "safety net" to ensure notes are uploaded eventually, even if the app is closed
   * or the immediate upload fails. Uses [ExistingWorkPolicy.KEEP] to avoid duplicate scheduled
   * jobs.
   */
  private fun scheduleWorker() {
    val request =
        OneTimeWorkRequestBuilder<SyncNotesWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

    workManager.enqueueUniqueWork("SyncNotes", ExistingWorkPolicy.KEEP, request)
  }

  /** Clears the current error or status message. */
  fun clearError() {
    _infoMsg.value = null
  }
}

/**
 * Sealed class representing the loading state of notes. Used internally to map the repository Flow
 * result to the UI state.
 */
private sealed class NotesLoadState {
  /**
   * State indicating notes loaded successfully.
   *
   * @property notes The list of loaded messages.
   */
  data class Success(val notes: List<Message>) : NotesLoadState()
  /**
   * State indicating an error occurred while loading notes.
   *
   * @property message The error message describing what went wrong.
   */
  data class Error(val message: String) : NotesLoadState()
}
