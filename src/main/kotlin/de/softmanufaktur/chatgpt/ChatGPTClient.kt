package de.softmanufaktur.chatgpt

import com.google.gson.Gson
import com.google.gson.JsonParser
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
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
        return callChatGpt(singlePromptToHistory(prompt))
    }

    fun callChatGpt(conversionHistory: List<Map<String, String>>): String {
        val apiKey = System.getenv("OPENAI_API_KEY")

        val requestBodyMap = mapOf(
            "model" to model,
            "messages" to conversionHistory
        )

        val requestBody = gson.toJson(requestBodyMap)

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response: Response = client.newCall(request).execute()
        return if (response.isSuccessful) {
            val responseBody = response.body?.string() ?: "No response from the API"
            println(responseBody);
            extractContentFromResponse(responseBody)
        } else {
            "Error with the API request: ${response.message}"
        }
    }

    fun singlePromptToHistory(prompt: String): List<Map<String, String>> {
        return listOf(mapOf("role" to "user", "content" to prompt))
    }

    fun extractContentFromResponse(responseBody: String): String {
        val jsonObject = JsonParser.parseString(responseBody).asJsonObject
        val choices = jsonObject.getAsJsonArray("choices")

        if (choices.size() > 0) {
            var content = choices[0].asJsonObject.getAsJsonObject("message").get("content").asString
            if (content.startsWith("```java\n")) {
                content = content.removePrefix("```java\n")
            }
            if (content.endsWith("```")) {
                content = content.removeSuffix("```")
            }
            return content
        } else {
            return "No content in the response"
        }
    }
}
