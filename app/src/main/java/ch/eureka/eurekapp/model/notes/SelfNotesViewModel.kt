package ch.eureka.eurekapp.model.notes

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ch.eureka.eurekapp.model.data.FirestoreRepositoriesProvider
import ch.eureka.eurekapp.model.data.IdGenerator
import ch.eureka.eurekapp.model.data.chat.Message
import ch.eureka.eurekapp.model.data.note.SelfNotesRepository
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
 */
class SelfNotesViewModel(
    private val repository: SelfNotesRepository =
        FirestoreRepositoriesProvider.selfNotesRepository,
    private val getCurrentUserId: () -> String? = { FirebaseAuth.getInstance().currentUser?.uid },
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

  private val _uiState = MutableStateFlow(SelfNotesUiState())
  val _uiState: StateFlow<SelfNotesUiState> = _uiState.asStateFlow()

  init {
    loadNotes()
  }

  /** Loads notes for the current user from Firestore. */
  private fun loadNotes() {
    val userId = getCurrentUserId()
    if (userId == null) {
      _uiState.value =
          _uiState.value.copy(isLoading = false, errorMsg = "User not authenticated")
      return
    }

    viewModelScope.launch(dispatcher) {
      try {
        repository.getNotesForUser(userId).collect { notes ->
          _uiState.value = _uiState.value.copy(notes = notes, isLoading = false, errorMsg = null)
        }
      } catch (e: Exception) {
        Log.e("SelfNotesViewModel", "Error loading notes", e)
        _uiState.value =
            _uiState.value.copy(isLoading = false, errorMsg = "Failed to load notes: ${e.message}")
      }
    }
  }

  /**
   * Updates the current message being composed.
   *
   * @param text The new message text.
   */
  fun updateMessage(text: String) {
    _uiState.value = _uiState.value.copy(currentMessage = text)
  }

  /**
   * Sends the current message as a new note.
   *
   * @param context Android context for showing toast notifications.
   */
  fun sendNote(context: Context) {
    val currentMessage = _uiState.value.currentMessage.trim()
    if (currentMessage.isEmpty()) {
      return
    }

    val userId = getCurrentUserId()
    if (userId == null) {
      Toast.makeText(context, "User not authenticated", Toast.LENGTH_SHORT).show()
      return
    }

    _uiState.value = _uiState.value.copy(isSending = true)

    viewModelScope.launch(dispatcher) {
      val message =
          Message(
              messageID = IdGenerator.generateMessageId(),
              text = currentMessage,
              senderId = userId,
              createdAt = Timestamp.now(),
              references = emptyList())

      repository.createNote(userId, message).fold(
          onSuccess = {
            _uiState.value =
                _uiState.value.copy(currentMessage = "", isSending = false, errorMsg = null)
          },
          onFailure = { error ->
            Log.e("SelfNotesViewModel", "Error sending note", error)
            _uiState.value =
                _uiState.value.copy(
                    isSending = false, errorMsg = "Failed to send note: ${error.message}")
            // Show toast on main thread
            viewModelScope.launch(Dispatchers.Main) {
              Toast.makeText(context, "Failed to send note", Toast.LENGTH_SHORT).show()
            }
          })
    }
  }

  /** Clears the current error message. */
  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMsg = null)
  }
}
