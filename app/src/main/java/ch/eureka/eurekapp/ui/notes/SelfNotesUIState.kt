/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.notes

import ch.eureka.eurekapp.model.data.chat.Message

/**
 * UI state for the Self Notes screen.
 *
 * @property notes List of notes (messages) ordered by creation time.
 * @property isLoading Whether notes are currently being loaded from the active repository (Local or
 *   Cloud).
 * @property errorMsg Error message to display, or null if no error.
 * @property currentMessage The text of the message currently being composed.
 * @property isSending Whether a note is currently being saved (created or updated) to the active
 *   storage.
 * @property isCloudStorageEnabled Whether the user is currently viewing Cloud Storage (true) or
 *   Local Storage (false).
 * @property editingMessageId The ID of the message currently being edited, or null if in "Create"
 *   mode.
 * @property selectedNoteIds The set of IDs currently selected for bulk action (e.g. deletion).
 */
data class SelfNotesUIState(
    val notes: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val errorMsg: String? = null,
    val currentMessage: String = "",
    val isSending: Boolean = false,
    val isCloudStorageEnabled: Boolean = false,
    val editingMessageId: String? = null,
    val selectedNoteIds: Set<String> = emptySet()
) {
  /** Helper property to check if the user is in multi-selection mode. */
  val isSelectionMode: Boolean
    get() = selectedNoteIds.isNotEmpty()
}
