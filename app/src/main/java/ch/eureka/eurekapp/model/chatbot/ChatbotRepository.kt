package ch.eureka.eurekapp.model.chatbot

/**
 * Repository for managing chatbot interactions. Delegates message sending to the appropriate data
 * source.
 *
 * @property engineType The type of chatbot engine to use
 */
class ChatbotRepository(private var engineType: ChatbotEngineType = ChatbotEngineType.DUMMY) {
  private val dummyDataSource: ChatbotDataSource = ChatbotDummyDataSource()
  private var apiDataSource: ChatbotDataSource? = null

  /**
   * Configures the API data source.
   *
   * @param apiUrl The URL of the chatbot API endpoint
   * @param apiKey The API key for authentication (if required)
   */
  fun configureApiDataSource(apiUrl: String, apiKey: String? = null) {
    apiDataSource = ChatbotApiDataSource(apiUrl, apiKey)
  }

  /**
   * Sets the engine type to use for chatbot interactions.
   *
   * @param type The engine type (DUMMY or API)
   */
  fun setEngineType(type: ChatbotEngineType) {
    engineType = type
  }

  /**
   * Gets the current engine type.
   *
   * @return The current engine type
   */
  fun getEngineType(): ChatbotEngineType = engineType

  /**
   * Sends a prompt and context to the chatbot.
   *
   * @param systemPrompt The system prompt defining the chatbot's behavior
   * @param context The context string to provide to the chatbot
   * @return The chatbot's response text
   * @throws IllegalStateException if API engine is selected but not configured
   * @throws Exception if the message cannot be sent
   */
  suspend fun sendMessage(systemPrompt: String, context: String): String {
    val dataSource =
        when (engineType) {
          ChatbotEngineType.DUMMY -> dummyDataSource
          ChatbotEngineType.API ->
              apiDataSource
                  ?: throw IllegalStateException(
                      "API data source not configured. Call configureApiDataSource() first.")
        }

    return dataSource.sendMessage(systemPrompt, context)
  }
}
