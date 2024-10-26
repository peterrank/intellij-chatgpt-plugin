package com.solvares.unittestgeneratorplugin

import de.softmanufaktur.chatgpt.ChatGPTClient
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import java.io.BufferedReader
import java.io.InputStreamReader

class ChatGPTClientTest {

    @Test
    fun testGenerateTestCases() {
        val client = ChatGPTClient("gpt-4o")

        // Lese den Prompt aus der Ressourcendatei
        val prompt = this::class.java.classLoader.getResourceAsStream("testprompt.txt")?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
        } ?: throw IllegalStateException("Prompt file not found.")

        val response = client.callChatGpt(prompt)

        // Da dies ein echter API-Aufruf ist, prüfen wir nur, ob eine sinnvolle Antwort zurückkommt
        println("API Response: $response")

        // Sicherstellen, dass die Antwort weder leer noch eine Fehlermeldung ist
        assertTrue(response.isNotEmpty(), "The answer should not be empty.")
        assertFalse(response.contains("Error with the API request"), "There should be no error with the API request.")
    }
}

