package ch.eureka.eurekapp.chatbot

/** Enum representing the type of chatbot engine to use. */
enum class ChatbotEngineType {
  /**
   * Dummy engine for testing and development. Returns predefined responses without making API
   * calls.
   */
  DUMMY,

  /** API-based engine that makes actual API calls to the chatbot service. */
  API
}
