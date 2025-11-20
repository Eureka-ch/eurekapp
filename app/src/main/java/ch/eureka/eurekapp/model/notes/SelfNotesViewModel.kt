package ch.eureka.eurekapp.model.notes

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.note.SelfNotesRepository
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

/*
Co-author: GPT-5 Codex
*/

/**
 * ViewModel for the Self Notes screen.
 *
 * Manages the state of self-notes including loading notes from Firestore, composing new notes, and
 * sending them. Notes are stored per-user and displayed in a chat-like interface.
 *
 * Uses Flow-based state derivation with stateIn for reactive UI updates.
 */
class SelfNotesViewModel(
    private val repository: SelfNotesRepository = FirestoreRepositoriesProvider.selfNotesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _currentMessage = MutableStateFlow("")
  private val _isSending = MutableStateFlow(false)
  private val _errorMsg = MutableStateFlow<String?>(null)

  val uiState: StateFlow<SelfNotesUiState> =
      combine(
              repository
                  .getNotes()
                  .map { notes -> NotesLoadState.Success(notes) as NotesLoadState }
                  .catch { e ->
                    Log.e("SelfNotesViewModel", "Error loading notes", e)
                    emit(NotesLoadState.Error("Failed to load notes: ${e.message}"))
                  },
              _currentMessage,
              _isSending,
              _errorMsg) { notesState, currentMessage, isSending, errorMsg ->
                when (notesState) {
                  is NotesLoadState.Success ->
                      SelfNotesUiState(
                          notes = notesState.notes,
                          isLoading = false,
                          errorMsg = errorMsg,
                          currentMessage = currentMessage,
                          isSending = isSending)
                  is NotesLoadState.Error ->
                      SelfNotesUiState(
                          notes = emptyList(),
                          isLoading = false,
                          errorMsg = errorMsg ?: notesState.message,
                          currentMessage = currentMessage,
                          isSending = isSending)
                }
              }
          .stateIn(
              scope = viewModelScope,
              started = SharingStarted.WhileSubscribed(5000),
              initialValue = SelfNotesUiState(isLoading = true))

  /**
   * Updates the current message being composed.
   *
   * @param text The new message text.
   */
  fun updateMessage(text: String) {
    _currentMessage.value = text
  }

  /** Sends the current message as a new note. */
  fun sendNote() {
    val currentMessage = _currentMessage.value.trim()
    if (currentMessage.isEmpty()) {
      return
    }

    _isSending.value = true

    viewModelScope.launch(dispatcher) {
      val message =
          Message(
              messageID = IdGenerator.generateMessageId(),
              text = currentMessage,
              senderId = "", // Will be set by the repository based on authenticated user
              createdAt = Timestamp.now(),
              references = emptyList())

      repository
          .createNote(message)
          .fold(
              onSuccess = {
                _currentMessage.value = ""
                _isSending.value = false
                _errorMsg.value = null
              },
              onFailure = { error ->
                _isSending.value = false
                _errorMsg.value = "Failed to send note: ${error.message}"
              })
    }
  }

  /** Clears the current error message. */
  fun clearError() {
    _errorMsg.value = null
  }
}

/** Sealed class representing the loading state of notes. */
private sealed class NotesLoadState {
  data class Success(val notes: List<Message>) : NotesLoadState()

  data class Error(val message: String) : NotesLoadState()
}
