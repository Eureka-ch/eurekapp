/* Portions of this file were written with the help of Gemini. */
package ch.eureka.eurekapp.ui.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.RepositoriesProvider
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.notes.UnifiedSelfNotesRepository
import ch.eureka.eurekapp.model.data.prefs.UserPreferencesRepository
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
 * This ViewModel manages the UI state for the personal notes feature, handling user interactions
 * for creating, editing, and deleting notes. It facilitates the strict separation between Cloud and
 * Local storage modes by delegating operations to the [UnifiedSelfNotesRepository].
 *
 * It combines multiple data streams (notes, loading state, user input, preferences) into a single
 * [SelfNotesUIState] for the UI to consume.
 *
 * @property repository The unified repository for managing self-notes operations.
 * @property userPrefs The repository for accessing user preferences (specifically storage mode).
 * @property dispatcher The coroutine dispatcher for background operations (defaults to
 *   [Dispatchers.IO]).
 */
class SelfNotesViewModel
@JvmOverloads
constructor(
    private val repository: UnifiedSelfNotesRepository =
        RepositoriesProvider.unifiedSelfNotesRepository,
    private val userPrefs: UserPreferencesRepository =
        RepositoriesProvider.userPreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _infoMsg = MutableStateFlow<String?>(null)
  private val _editingMessageId = MutableStateFlow<String?>(null)
  private val _selectedNoteIds = MutableStateFlow<Set<String>>(emptySet())

  /**
   * The single source of truth for the UI state.
   *
   * Combines data from the repository, user preferences, and local ViewModel state to produce a
   * comprehensive [SelfNotesUIState].
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
              userPrefs.isCloudStorageEnabled,
              _editingMessageId,
              _selectedNoteIds) { args ->
                val notesState = args[0] as NotesLoadState
                val currentMessage = args[1] as String
                val isSending = args[2] as Boolean
                val errorMsg = args[3] as String?
                val isCloud = args[4] as Boolean
                val editingId = args[5] as String?
                @Suppress("UNCHECKED_CAST") val selectedIds = args[6] as Set<String>

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

  /**
   * Toggles the storage mode between Local and Cloud.
   *
   * This performs an instant view switch by updating the user preference. It provides feedback to
   * the user about which mode they are now viewing.
   *
   * @param enableCloud `true` to switch to Cloud Mode, `false` to switch to Local Mode.
   */
  fun toggleStorageMode(enableCloud: Boolean) {
    viewModelScope.launch {
      repository.setStorageMode(enableCloud)
      if (enableCloud) {
        _infoMsg.value = "Viewing Cloud Notes"
      } else {
        _infoMsg.value = "Viewing Local Notes"
      }
    }
  }

  /**
   * Updates the text of the message currently being composed or edited.
   *
   * @param text The new text entered by the user.
   */
  fun updateMessage(text: String) {
    _currentMessage.value = text
  }

  /**
   * Toggles the selection state of a specific note.
   *
   * Used for the multi-selection feature (e.g., for bulk deletion).
   *
   * @param noteId The unique ID of the note to select or deselect.
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

  /** Clears all currently selected notes, exiting selection mode. */
  fun clearSelection() {
    _selectedNoteIds.value = emptySet()
  }

  /**
   * Deletes all notes currently selected in the UI.
   *
   * Iterates through the selected IDs and attempts to delete them via the repository. Displays an
   * error message if any deletion fails.
   */
  fun deleteSelectedNotes() {
    val selectedIds = _selectedNoteIds.value.toSet()
    if (selectedIds.isEmpty()) return

    viewModelScope.launch(dispatcher) {
      clearSelection()
      selectedIds.forEach { id ->
        repository.deleteNote(id).onFailure { _infoMsg.value = "Failed to delete: ${it.message}" }
      }
    }
  }

  /**
   * Enters "Edit Mode" for the specified note.
   *
   * Populates the input field with the note's current text and tracks the note ID. Clears any
   * active selection before starting.
   *
   * @param note The [Message] object to be edited.
   */
  fun startEditing(note: Message) {
    clearSelection()
    _editingMessageId.value = note.messageID
    _currentMessage.value = note.text
  }

  /**
   * Cancels the current edit operation.
   *
   * Resets the input field and clears the tracking of the editing message ID, returning the UI to
   * "Create Mode".
   */
  fun cancelEditing() {
    _editingMessageId.value = null
    _currentMessage.value = ""
  }

  /**
   * Saves the current note to the active storage.
   * - If an ID is being edited, it updates the existing note.
   * - If no ID is being edited, it creates a new note with a generated ID.
   *
   * Handles success by resetting the input, and failure by displaying an error message (e.g., if
   * the user tries to save a cloud note while offline).
   */
  fun sendNote() {
    val currentMessageText = _currentMessage.value.trim()
    if (currentMessageText.isEmpty()) return

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
          },
          onFailure = { error ->
            _isSending.value = false
            _infoMsg.value = error.message ?: "Operation failed"
          })
    }
  }

  /** Clears the current informational or error message displayed to the user. */
  fun clearError() {
    _infoMsg.value = null
  }
}

/** Internal sealed class to represent the loading state of notes. */
private sealed class NotesLoadState {
  data class Success(val notes: List<Message>) : NotesLoadState()

  data class Error(val message: String) : NotesLoadState()
}
