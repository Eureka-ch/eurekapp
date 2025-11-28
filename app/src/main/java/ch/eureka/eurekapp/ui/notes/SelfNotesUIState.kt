/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.notes

import ch.eureka.eurekapp.model.data.chat.Message

/**
 * UI state for the Self Notes screen.
 *
 * Represents the current state of the self-notes interface including the list of notes, loading
 * state, errors, and the current message being composed.
 *
 * @property notes List of notes (messages) ordered by creation time.
 * @property isLoading Whether notes are currently being loaded from Firestore.
 * @property errorMsg Error message to display, or null if no error.
 * @property currentMessage The text of the message currently being composed.
 * @property isSending Whether a note is currently being sent to Firestore.
 * @property isCloudStorageEnabled Whether the user has opted into Cloud Sync (true) or Local Only
 *   (false).
 */
data class SelfNotesUIState(
    val notes: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val isCloudStorageEnabled: Boolean = false
)
