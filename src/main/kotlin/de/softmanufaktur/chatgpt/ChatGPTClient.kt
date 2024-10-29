package de.softmanufaktur.chatgpt

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.StringReader
import java.util.concurrent.TimeUnit

class ChatGPTClient(private val model: String) {

    private val client = OkHttpClient.Builder()
        .callTimeout(120, TimeUnit.SECONDS)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()

    fun callChatGpt(prompt: String): String {
        return callChatGpt(singlePromptToHistory(prompt), false) { partialResponse -> print(partialResponse) }
    }

    fun callChatGpt(conversionHistory: List<Map<String, String>>, useStreaming: Boolean, onPartialResponse: (String) -> Unit): String {
        val apiKey = System.getenv("OPENAI_API_KEY")

        val requestBodyMap = mapOf(
            "model" to model,
            "messages" to conversionHistory,
            "stream" to useStreaming
        )

        val requestBody = gson.toJson(requestBodyMap)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()


        if(useStreaming) {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return "Error with the API request: ${response.message}"
                }

                response.body?.source()?.let { source ->
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line()
                        if (line != null) {
                            // Process the received line (depending on your streaming response structure)
                            val partialContent = extractPartialContentFromLine(line)
                            // Call the UI update or any processing on each part
                            print(partialContent)
                            onPartialResponse(partialContent)
                        }
                    }
                }
            }
            return ""
        } else {
            val response: Response = client.newCall(request).execute()
            return if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: "No response from the API"
                extractContentFromResponse(responseBody)
            } else {
                "Error with the API request: ${response.message}"
            }
        }
    }

    fun singlePromptToHistory(prompt: String): List<Map<String, String>> {
        return listOf(mapOf("role" to "user", "content" to prompt))
    }

    private fun extractPartialContentFromLine(line: String): String {
        return try {
            val cleanedLine = line.removePrefix("data: ")
            val reader = JsonReader(StringReader(cleanedLine))
            reader.isLenient = true // set lenient mode to true

            val jsonObject = JsonParser.parseReader(reader).asJsonObject
            val choices = jsonObject.getAsJsonArray("choices")

            if (choices.size() > 0) {
                var content = choices[0].asJsonObject.getAsJsonObject("delta").get("content").asString
                return content
            } else {
                ""
            }
        } catch (e: Exception) {
            // Return an empty string if JSON parsing fails
            ""
        }
    }


    fun extractContentFromResponse(responseBody: String): String {
        val jsonObject = JsonParser.parseString(responseBody).asJsonObject
        val choices = jsonObject.getAsJsonArray("choices")

        if (choices.size() > 0) {
            var content = choices[0].asJsonObject.getAsJsonObject("message").get("content").asString
            return content
        } else {
            return "No content in the response"
        }
    }
}
