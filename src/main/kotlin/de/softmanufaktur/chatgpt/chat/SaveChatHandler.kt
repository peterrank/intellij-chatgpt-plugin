package de.softmanufaktur.chatgpt.chat

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class SaveChatHandler(
    private val project: Project,
    private val chatLayout: ChatGptPanelLayout
) {
    fun saveChatToFile(conversationHistory: ConversationHistory) {
        val basePath = project.basePath ?: throw IllegalStateException("Project base path is not available")
        val chatDirectory = File(basePath, "ChatGPT-Prompts")
        if (!chatDirectory.exists()) {
            chatDirectory.mkdirs()
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss")
        val date = Date()
        val fileName = "Chat_${dateFormat.format(date)}.json"

        val chatFile = File(chatDirectory, fileName)
        val fileWriter = FileWriter(chatFile)

        val jsonMessages = JsonArray()
        conversationHistory.getAllMessages().forEach { message ->
            val jsonMessage = JsonObject()
            val role = message["role"] ?: "unknown"
            val content = message["content"] ?: ""
            jsonMessage.addProperty("role", role)
            jsonMessage.addProperty("content", content)
            jsonMessages.add(jsonMessage)
        }

        val gson = GsonBuilder().setPrettyPrinting().create()
        gson.toJson(jsonMessages, fileWriter)
        fileWriter.close()

        chatLayout.statusBar.text = "Chat gespeichert als $fileName"

        ApplicationManager.getApplication().runWriteAction {
            VirtualFileManager.getInstance().asyncRefresh(null)
        }
    }
}
