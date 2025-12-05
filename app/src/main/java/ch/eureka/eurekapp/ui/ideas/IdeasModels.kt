/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import com.google.firebase.Timestamp

/**
 * Data class representing an Idea (conversation with AI). Each Idea is a separate conversation that
 * can be shared with participants.
 *
 * @property ideaId Unique identifier for the idea.
 * @property projectId ID of the project this idea belongs to.
 * @property title Optional title for the idea.
 * @property content Optional description/content for the idea.
 * @property createdBy User ID of the person who created this idea.
 * @property participantIds List of user IDs who can see and participate in this idea.
 * @property createdAt Timestamp when the idea was created.
 * @property lastUpdated Timestamp when the idea was last updated.
 */
data class Idea(
    val ideaId: String = "",
    val projectId: String = "",
    val title: String? = null,
    val content: String? = null,
    val createdBy: String = "",
    val participantIds: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val lastUpdated: Timestamp? = null
)

/** View mode for the Ideas screen. */
enum class IdeasViewMode {
  LIST, // Display list of Ideas
  CONVERSATION // Display conversation of selected Idea
}
