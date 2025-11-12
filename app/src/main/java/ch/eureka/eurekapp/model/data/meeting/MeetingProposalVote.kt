/* Portions of this file were written with the help of Gemini */
package ch.eureka.eurekapp.model.data.meeting

/**
 * Represents a single user's complete vote for one [MeetingProposal]. This objects tells WHO voted
 * and WHAT was their preference(s).
 *
 * @property userId The ID of the user who cast this vote.
 * @property formatPreferences The user's format choice(s).
 */
data class MeetingProposalVote(
    val userId: String = "",
    val formatPreferences: List<MeetingFormat> = emptyList(),
)
