package ch.eureka.eurekapp.model.chatbot

/**
 * Use case for sending messages to the chatbot. Encapsulates the business logic for message
 * validation and sending.
 *
 * @property repository The chatbot repository
 */
class SendMessageUseCase(private val repository: ChatbotRepository) {
  /**
   * Executes the use case to send a prompt and context to the chatbot.
   *
   * @param systemPrompt The system prompt defining the chatbot's behavior
   * @param context The context string to provide to the chatbot
   * @return Result containing the bot's response text or an error
   */
  suspend operator fun invoke(systemPrompt: String, context: String): Result<String> {
    return try {
      if (context.isBlank()) {
        return Result.failure(IllegalArgumentException("Context cannot be blank"))
      }

      val response = repository.sendMessage(systemPrompt, context)
      Result.success(response)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }
}
