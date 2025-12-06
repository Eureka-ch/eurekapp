package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.chat.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/** Mock implementation of IdeasRepository for testing. */
class MockIdeasRepository : IdeasRepository {
  private val ideas = mutableMapOf<String, MutableList<Idea>>()
  private val messages = mutableMapOf<String, MutableList<Message>>()
  private val ideasFlow = MutableStateFlow<Map<String, List<Idea>>>(emptyMap())
  private val messagesFlow = MutableStateFlow<Map<String, List<Message>>>(emptyMap())

  override fun getIdeasForProject(projectId: String): Flow<List<Idea>> =
      ideasFlow.map { it[projectId] ?: emptyList() }

  override suspend fun createIdea(idea: Idea): Result<String> {
    return try {
      val projectIdeas = ideas.getOrPut(idea.projectId) { mutableListOf() }
      projectIdeas.add(idea)
      ideasFlow.value = ideas.mapValues { it.value.toList() }
      Result.success(idea.ideaId)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit> {
    return try {
      ideas[projectId]?.removeIf { it.ideaId == ideaId }
      ideasFlow.value = ideas.mapValues { it.value.toList() }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override fun getMessagesForIdea(ideaId: String): Flow<List<Message>> =
      messagesFlow.map { it[ideaId] ?: emptyList() }

  override suspend fun sendMessage(ideaId: String, message: Message): Result<Unit> {
    return try {
      val ideaMessages = messages.getOrPut(ideaId) { mutableListOf() }
      ideaMessages.add(message)
      messagesFlow.value = messages.mapValues { it.value.toList() }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  override suspend fun addParticipant(
      projectId: String,
      ideaId: String,
      userId: String
  ): Result<Unit> {
    return try {
      ideas[projectId]
          ?.find { it.ideaId == ideaId }
          ?.let { idea ->
            val updatedIdea = idea.copy(participantIds = (idea.participantIds + userId).distinct())
            ideas[projectId]?.removeIf { it.ideaId == ideaId }
            ideas[projectId]?.add(updatedIdea)
            ideasFlow.value = ideas.mapValues { it.value.toList() }
          }
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
