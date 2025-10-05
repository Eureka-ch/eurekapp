package ch.eureka.eurekapp.chatbot

import kotlinx.coroutines.delay

/**
 * Dummy implementation of ChatbotDataSource for testing and development.
 * Returns predefined responses without making actual API calls.
 */
class ChatbotDummyDataSource : ChatbotDataSource {

    companion object {
        private const val SIMULATED_DELAY_MS = 500L
    }

    override suspend fun sendMessage(prompt: String, imageContext: ByteArray?): String {
        delay(SIMULATED_DELAY_MS)

        return "This is a dummy response for testing."
    }
}
