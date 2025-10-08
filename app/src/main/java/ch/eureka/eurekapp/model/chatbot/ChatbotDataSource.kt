package ch.eureka.eurekapp.chatbot

/**
 * Interface for chatbot data sources. Implementations can provide dummy responses or make actual
 * API calls.
 */
interface ChatbotDataSource {
  /**
   * Sends a prompt and context to the chatbot and receives a response.
   *
   * @param systemPrompt The system prompt defining the chatbot's behavior
   * @param context The context string to provide to the chatbot
   * @return The chatbot's response text
   * @throws Exception if the message cannot be sent
   */
  suspend fun sendMessage(systemPrompt: String, context: String): String
}
