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

  /** The single source of truth for the UI state. */
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

  /** Creates and saves a new note. */
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

private sealed class NotesLoadState {
  data class Success(val notes: List<Message>) : NotesLoadState()

  data class Error(val message: String) : NotesLoadState()
}
