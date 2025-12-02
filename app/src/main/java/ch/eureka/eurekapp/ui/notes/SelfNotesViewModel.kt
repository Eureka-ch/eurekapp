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
import ch.eureka.eurekapp.model.data.notes.SyncStats
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
 * This ViewModel manages the UI state for the personal notes feature, including:
 * - Loading and displaying notes from the unified repository.
 * - Handling user input for creating and editing notes.
 * - Toggling between Local-Only and Cloud-Sync storage modes.
 * - Managing background synchronization status and error reporting.
 * - Handling multi-selection for bulk actions (deletion).
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
  // Track which message is being edited. Null means creating new.
  private val _editingMessageId = MutableStateFlow<String?>(null)
  // Track selected messages for bulk actions
  private val _selectedNoteIds = MutableStateFlow<Set<String>>(emptySet())

  /** The single source of truth for the UI state. */
  @Suppress("UNCHECKED_CAST")
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
              _editingMessageId,
              _selectedNoteIds) { args ->
                // Combine with >5 args uses vararg and passes an Array<Any?>
                val notesState = args[0] as NotesLoadState
                val currentMessage = args[1] as String
                val isSending = args[2] as Boolean
                val errorMsg = args[3] as String?
                val isCloud = args[4] as Boolean
                val editingId = args[5] as String?
                val selectedIds = args[6] as Set<String>

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
                    editingMessageId = editingId,
                    selectedNoteIds = selectedIds)
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
            val stats = repository.syncPendingNotes()
            if (stats.total > 0) {
              _infoMsg.value = "Back online: ${formatSyncMessage(stats)}"
            }
          }
        }
      } catch (e: Exception) {
        Log.e("SelfNotesViewModel", "Connectivity Observer error", e)
      }
    }
  }

  /**
   * Formats a user-friendly message summarizing the sync operations performed.
   *
   * @param stats The sync statistics containing counts of upserts and deletions.
   * @return A string describing the sync result (e.g., "Synced: 2 sent, 1 deleted").
   */
  private fun formatSyncMessage(stats: SyncStats): String {
    val parts = mutableListOf<String>()
    if (stats.upserts > 0) parts.add("${stats.upserts} sent")
    if (stats.deletes > 0) parts.add("${stats.deletes} deleted")
    return if (parts.isNotEmpty()) "Synced: ${parts.joinToString(", ")}" else "Synced"
  }

  /**
   * Toggles the storage mode between Local-only and Cloud.
   *
   * If switching to Cloud, it triggers a sync of existing local notes. If switching to Local, it
   * ensures cloud privacy by clearing cloud data.
   *
   * @param enableCloud True to enable Cloud storage/sync, False for Local-only.
   */
  fun toggleStorageMode(enableCloud: Boolean) {
    viewModelScope.launch {
      val stats = repository.setStorageMode(enableCloud)
      if (enableCloud) {
        if (stats.total > 0) {
          _infoMsg.value = "Switched to Cloud: ${formatSyncMessage(stats)}"
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
   * Toggles the selection state of a note.
   *
   * Used for multi-selection mode. If the note is already selected, it is deselected. If the
   * resulting selection is empty, the UI automatically exits selection mode.
   *
   * @param noteId The unique ID of the note to toggle.
   */
  fun toggleSelection(noteId: String) {
    val currentSelection = _selectedNoteIds.value.toMutableSet()
    if (currentSelection.contains(noteId)) {
      currentSelection.remove(noteId)
    } else {
      currentSelection.add(noteId)
    }
    _selectedNoteIds.value = currentSelection
  }

  /** Clears all selected notes, effectively exiting selection mode. */
  fun clearSelection() {
    _selectedNoteIds.value = emptySet()
  }

  /**
   * Deletes all currently selected notes.
   *
   * This action is performed optimistically: selection is cleared immediately, and deletions are
   * queued in the background.
   */
  fun deleteSelectedNotes() {
    val selectedIds = _selectedNoteIds.value.toSet()
    if (selectedIds.isEmpty()) return

    viewModelScope.launch(dispatcher) {
      clearSelection()

      selectedIds.forEach { id ->
        repository.deleteNote(id).onFailure {
          Log.e("SelfNotesViewModel", "Error deleting $id: ${it.message}")
        }
      }
    }
  }

  /**
   * Enters "Edit Mode" for a specific note. Populates the input field with the note's text and
   * stores the ID of the note being edited.
   *
   * If selection mode was active, it is cleared first.
   *
   * @param note The note object to edit.
   */
  fun startEditing(note: Message) {
    clearSelection()
    _editingMessageId.value = note.messageID
    _currentMessage.value = note.text
  }

  /**
   * Cancels the current edit operation. Clears the input field and resets the editing state to
   * allow creating new notes.
   */
  fun cancelEditing() {
    _editingMessageId.value = null
    _currentMessage.value = ""
  }

  /**
   * Saves the current note.
   *
   * Behavior depends on the current state:
   * - **Editing Mode:** Updates the existing note with the new text.
   * - **Creation Mode:** Creates a new note with the entered text.
   *
   * In both cases, the operation is synced to the cloud if Cloud Storage is enabled.
   */
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
            // Update existing note
            repository.updateNote(editingId, currentMessageText)
          } else {
            // Create new note
            val message =
                Message(
                    messageID = IdGenerator.generateMessageId(),
                    text = currentMessageText,
                    createdAt = Timestamp.now())
            repository.createNote(message).map {
              Unit
            } // map result to Unit to match update return type
          }

      result.fold(
          onSuccess = {
            _currentMessage.value = ""
            _editingMessageId.value = null // Exit edit mode
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
   * Schedules a one-time background worker to synchronize pending notes. This is used to ensure
   * robust syncing even if the app is closed or network is unavailable.
   */
  private fun scheduleWorker() {
    val request =
        OneTimeWorkRequestBuilder<SyncNotesWorker>()
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

    workManager.enqueueUniqueWork("SyncNotes", ExistingWorkPolicy.KEEP, request)
  }

  /** Clears the current informational or error message displayed to the user. */
  fun clearError() {
    _infoMsg.value = null
  }
}

/**
 * Internal state representation for the asynchronous note loading process. Used to map the
 * Flow<List<Message>> from the repository into a UI-consumable state.
 */
private sealed class NotesLoadState {
  /**
   * Represents a successful load of notes.
   *
   * @property notes The list of loaded messages.
   */
  data class Success(val notes: List<Message>) : NotesLoadState()

  /**
   * Represents a failure during note loading.
   *
   * @property message The error message describing what went wrong.
   */
  data class Error(val message: String) : NotesLoadState()
}
