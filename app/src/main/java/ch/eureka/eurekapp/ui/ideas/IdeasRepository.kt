package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.chat.Message
import kotlinx.coroutines.flow.Flow

/** Repository interface for Ideas (conversations with AI about projects). */
interface IdeasRepository {
  /** Get all ideas for a project where current user is a participant */
  fun getIdeasForProject(projectId: String): Flow<List<Idea>>

  /** Create a new idea */
  suspend fun createIdea(idea: Idea): Result<String>

  /** Delete an idea */
  suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit>

  /** Get all messages for an idea */
  fun getMessagesForIdea(ideaId: String): Flow<List<Message>>

  /** Send a message in an idea conversation */
  suspend fun sendMessage(ideaId: String, message: Message): Result<Unit>

  /** Add a participant to an idea */
  suspend fun addParticipant(projectId: String, ideaId: String, userId: String): Result<Unit>
}
