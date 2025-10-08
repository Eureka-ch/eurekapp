package ch.eureka.eurekapp.chatbot

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * API-based implementation of ChatbotDataSource. Makes actual API calls to the chatbot service.
 *
 * @property apiUrl The URL of the chatbot API endpoint
 * @property apiKey The API key for authentication (if required)
 */
class ChatbotApiDataSource(private val apiUrl: String, private val apiKey: String? = null) :
    ChatbotDataSource {

  companion object {
    private const val TIMEOUT_MS = 60000
    private const val METHOD_POST = "POST"
    private const val HEADER_CONTENT_TYPE = "Content-Type"
    private const val HEADER_AUTHORIZATION = "Authorization"
    private const val CONTENT_TYPE_JSON = "application/json"
  }

  override suspend fun sendMessage(systemPrompt: String, context: String): String {
    return withContext(Dispatchers.IO) {
      val connection = URL(apiUrl).openConnection() as HttpURLConnection

      try {
        connection.apply {
          requestMethod = METHOD_POST
          setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
          apiKey?.let { setRequestProperty(HEADER_AUTHORIZATION, "Bearer $it") }
          doOutput = true
          doInput = true
          connectTimeout = TIMEOUT_MS
          readTimeout = TIMEOUT_MS
        }

        val requestBody = buildRequestBody(systemPrompt, context)

        OutputStreamWriter(connection.outputStream).use { writer ->
          writer.write(requestBody)
          writer.flush()
        }

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
          BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            val response = reader.readText()
            parseResponse(response)
          }
        } else {
          val errorMessage =
              BufferedReader(InputStreamReader(connection.errorStream)).use { it.readText() }
          throw Exception("API error: $responseCode - $errorMessage")
        }
      } finally {
        connection.disconnect()
      }
    }
  }

  private fun buildRequestBody(systemPrompt: String, context: String): String {
    val jsonObject = JSONObject()
    val messages = JSONArray()

    messages.put(
        JSONObject().apply {
          put("role", "system")
          put("content", systemPrompt)
        })

    messages.put(
        JSONObject().apply {
          put("role", "user")
          put("content", context)
        })

    jsonObject.put("model", "deepseek-chat")
    jsonObject.put("messages", messages)

    return jsonObject.toString()
  }

  private fun parseResponse(response: String): String {
    val jsonObject = JSONObject(response)
    val choices = jsonObject.getJSONArray("choices")

    if (choices.length() > 0) {
      val firstChoice = choices.getJSONObject(0)
      return firstChoice.getJSONObject("message").getString("content")
    } else {
      throw Exception("Empty choices array in DeepSeek response")
    }
  }
}
