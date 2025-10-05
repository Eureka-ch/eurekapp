package ch.eureka.eurekapp.chatbot

/**
 * Interface for chatbot data sources.
 * Implementations can provide dummy responses or make actual API calls.
 */
interface ChatbotDataSource {
    /**
     * Sends a message to the chatbot and receives a response.
     *
     * @param prompt The text prompt to send
     * @param imageContext Optional image context as ByteArray
     * @return The chatbot's response text
     * @throws Exception if the message cannot be sent
     */
    suspend fun sendMessage(prompt: String, imageContext: ByteArray? = null): String
}
