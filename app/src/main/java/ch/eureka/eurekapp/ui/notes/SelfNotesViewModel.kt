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
  private val _editingMessageId = MutableStateFlow<String?>(null)

  /** The single source of truth for the UI state. */
  @Suppress("UNCHECKED_CAST") // Safe casts for combine array
  val uiState: StateFlow<SelfNotesUIState> =
      combine(
              repository
                  .getNotes()
                  .map { notes -> NotesLoadState.Success(notes) as NotesLoadState }
                  .catch { e -> emit(NotesLoadState.Error(e.message ?: "Error")) },
              _currentMessage,
              _isSending,
              _infoMsg,
              userPrefs.isCloudStorageEnabled,
              _editingMessageId) { args ->
                val notesState = args[0] as NotesLoadState
                val currentMessage = args[1] as String
                val isSending = args[2] as Boolean
                val errorMsg = args[3] as String?
                val isCloud = args[4] as Boolean
                val editingId = args[5] as String?

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
                    isCloudStorageEnabled = isCloud,
                    editingMessageId = editingId)
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

  /**
   * Enters "Edit Mode" for a specific note. Populates the input field with the note's text.
   *
   * @param note The note to edit.
   */
  fun startEditing(note: Message) {
    _editingMessageId.value = note.messageID
    _currentMessage.value = note.text
  }

  /** Cancels the current edit operation and clears the input. */
  fun cancelEditing() {
    _editingMessageId.value = null
    _currentMessage.value = ""
  }

  /**
   * Deletes a note.
   *
   * @param note The note to delete.
   */
  fun deleteNote(note: Message) {
    viewModelScope.launch(dispatcher) {
      repository.deleteNote(note.messageID).onFailure { e ->
        _infoMsg.value = "Failed to delete: ${e.message}"
      }
      if (_editingMessageId.value == note.messageID) {
        cancelEditing()
      }
    }
  }

  /** Creates and saves a new note, OR updates the existing note if in edit mode. */
  fun sendNote() {
    val currentMessageText = _currentMessage.value.trim()
    if (currentMessageText.isEmpty()) return
    if (currentMessageText.length > 5000) {
      _infoMsg.value = "Note too long (max 5000 characters)"
      return
    }

    _isSending.value = true
    val editingId = _editingMessageId.value

    viewModelScope.launch(dispatcher) {
      val result =
          if (editingId != null) {
            repository.updateNote(editingId, currentMessageText)
          } else {
            val message =
                Message(
                    messageID = IdGenerator.generateMessageId(),
                    text = currentMessageText,
                    createdAt = Timestamp.now())
            repository.createNote(message).map { Unit }
          }

      result.fold(
          onSuccess = {
            _currentMessage.value = ""
            _editingMessageId.value = null
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
