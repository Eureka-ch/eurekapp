/* Portions of this file were written with the help of Gemini */
package ch.eureka.eurekapp.model.data.meeting

import com.google.firebase.Timestamp

/**
 * Represents a single, votable meeting proposal.
 *
 * @param dateTime The datetime of the meeting proposal.
 * @param votes List of [MeetingProposalVote] for that meeting proposal.
 */
data class MeetingProposal(
    val dateTime: Timestamp = Timestamp.now(),
    val votes: List<MeetingProposalVote> = emptyList()
)
