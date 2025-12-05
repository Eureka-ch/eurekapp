/* Portions of this file were written with the help of GPT-5 Codex and Gemini. */
package ch.eureka.eurekapp.ui.ideas

import ch.eureka.eurekapp.model.data.chat.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * Configurable mock implementation of IdeasRepository for testing.
 *
 * Allows tests to configure idea data, flows, and error scenarios.
 * Pattern follows MockTaskRepository and MockProjectRepository.
 */
class MockIdeasRepository : IdeasRepository {
  private val ideasByProject = mutableMapOf<String, Flow<List<Idea>>>()
  private val messagesByIdea = mutableMapOf<String, Flow<List<Message>>>()
  private var createIdeaResult: Result<String> = Result.success("mock-idea-id")
  private var addParticipantResult: Result<Unit> = Result.success(Unit)
  private var sendMessageResult: Result<Unit> = Result.success(Unit)

  // Track method calls for verification
  val createIdeaCalls = mutableListOf<Idea>()
  val getIdeasForProjectCalls = mutableListOf<String>()
  val getMessagesForIdeaCalls = mutableListOf<String>()
  val sendMessageCalls = mutableListOf<Pair<String, Message>>()
  val addParticipantCalls = mutableListOf<Triple<String, String, String>>() // projectId, ideaId, userId

  /** Configure ideas returned by getIdeasForProject() */
  fun setIdeasForProject(projectId: String, flow: Flow<List<Idea>>) {
    ideasByProject[projectId] = flow
  }

  /** Configure messages returned by getMessagesForIdea() */
  fun setMessagesForIdea(ideaId: String, flow: Flow<List<Message>>) {
    messagesByIdea[ideaId] = flow
  }

  /** Configure the result returned by createIdea() */
  fun setCreateIdeaResult(result: Result<String>) {
    createIdeaResult = result
  }

  /** Configure the result returned by addParticipant() */
  fun setAddParticipantResult(result: Result<Unit>) {
    addParticipantResult = result
  }

  /** Configure the result returned by sendMessage() */
  fun setSendMessageResult(result: Result<Unit>) {
    sendMessageResult = result
  }

  /** Clear all configuration */
  fun reset() {
    ideasByProject.clear()
    messagesByIdea.clear()
    createIdeaResult = Result.success("mock-idea-id")
    addParticipantResult = Result.success(Unit)
    sendMessageResult = Result.success(Unit)
    createIdeaCalls.clear()
    getIdeasForProjectCalls.clear()
    getMessagesForIdeaCalls.clear()
    sendMessageCalls.clear()
    addParticipantCalls.clear()
  }

  override fun getIdeasForProject(projectId: String): Flow<List<Idea>> {
    getIdeasForProjectCalls.add(projectId)
    return ideasByProject[projectId] ?: flowOf(emptyList())
  }

  override suspend fun createIdea(idea: Idea): Result<String> {
    createIdeaCalls.add(idea)
    return createIdeaResult
  }

  override suspend fun deleteIdea(projectId: String, ideaId: String): Result<Unit> {
    return Result.success(Unit)
  }

  override fun getMessagesForIdea(ideaId: String): Flow<List<Message>> {
    getMessagesForIdeaCalls.add(ideaId)
    return messagesByIdea[ideaId] ?: flowOf(emptyList())
  }

  override suspend fun sendMessage(ideaId: String, message: Message): Result<Unit> {
    sendMessageCalls.add(ideaId to message)
    return sendMessageResult
  }

  override suspend fun addParticipant(projectId: String, ideaId: String, userId: String): Result<Unit> {
    addParticipantCalls.add(Triple(projectId, ideaId, userId))
    return addParticipantResult
  }
}

