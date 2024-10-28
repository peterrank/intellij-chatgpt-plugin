// FILE: ChatGptPanel.kt
package de.softmanufaktur.chatgpt.chat

import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.ui.components.JBScrollPane
import de.softmanufaktur.chatgpt.ChatGPTClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.BorderLayout
import java.awt.Component
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetAdapter
import java.awt.dnd.DropTargetDropEvent
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.io.File
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*


@Service
class ChatGptPanel : JPanel() {
    lateinit var project: Project
    private val conversationHistory: MutableList<Map<String, String>> = mutableListOf()
    private val chatLayout = ChatGptPanelLayout()
    private val modelSelector = ModelSelector(chatLayout.statusBar)

    private val chatDisplayPanel get() = chatLayout.chatDisplayPanel
    private val promptField get() = chatLayout.promptField

    init {
        layout = BorderLayout()

        // Create menu bar with model selector
        val modelComboBox = modelSelector.createModelSelector()
        val menuBar = JMenuBar().apply {
            add(JLabel("Modell: ") as Component)
            add(modelComboBox as Component)
            add(JMenuItem("Chat speichern").apply {
                addActionListener { e -> saveChatToFile() }
            })
        }
        add(menuBar, BorderLayout.NORTH)
        add(chatLayout.setupLayout(), BorderLayout.CENTER)

        chatLayout.sendButton.addActionListener { sendPrompt() }
        chatLayout.clearButton.addActionListener { clearConversationHistory() }

        addComponentListener(object : ComponentAdapter() {
            override fun componentResized(e: ComponentEvent?) {
                super.componentResized(e)
                updateChatDisplayPanel()
            }
        })

        // Set up drag and drop for the chatDisplayPanel
        chatDisplayPanel.dropTarget = DropTarget(chatDisplayPanel, object : DropTargetAdapter() {
            override fun drop(event: DropTargetDropEvent) {
                event.acceptDrop(DnDConstants.ACTION_COPY)

                val transferable = event.transferable
                val flavor = DataFlavor.javaFileListFlavor

                if (transferable.isDataFlavorSupported(flavor)) {
                    val files = transferable.getTransferData(flavor) as List<File>
                    for (file in files) {
                        if (file.extension == "json") { // Check if the file is a JSON file
                            loadMessagesFromFile(file)
                            chatLayout.statusBar.text = "Chat aus Datei \"${file.name}\" geladen."
                        }
                    }
                }

                event.dropComplete(true)
            }
        })
    }

    private fun updateChatDisplayPanel() {
        chatDisplayPanel.removeAll()
        for (message in conversationHistory) {
            val role = message["role"] ?: ""
            val content = message["content"] ?: ""

            val maxBubbleWidth = chatDisplayPanel.width - 100
            val bubble = createMessageBubble(content, role == "user", maxBubbleWidth)

            val alignmentPanel = JPanel(BorderLayout())
            if (role == "user") {
                alignmentPanel.add(bubble, BorderLayout.WEST)
            } else {
                alignmentPanel.add(bubble, BorderLayout.EAST)
            }

            chatDisplayPanel.add(Box.createVerticalStrut(10))
            chatDisplayPanel.add(alignmentPanel)
        }

        chatDisplayPanel.revalidate()
        chatDisplayPanel.repaint()

        // Scroll to bottom
        SwingUtilities.invokeLater {
            val scrollPane: JBScrollPane = chatDisplayPanel.parent.parent as JBScrollPane
            val verticalScrollBar = scrollPane.verticalScrollBar
            verticalScrollBar.value = verticalScrollBar.maximum
        }
    }

    private fun sendPrompt() {
        val prompt = promptField.text.trim()
        if (prompt.isNotBlank()) {
            conversationHistory.add(mapOf("role" to "user", "content" to prompt))
            updateChatDisplayPanel()

            chatLayout.statusBar.text = "Anfrage mit Modell ${modelSelector.selectedModel}. Bitte warten..."
            promptField.text = ""
            CoroutineScope(Dispatchers.Main).launch {
                val client = ChatGPTClient(modelSelector.selectedModel)
                val response = withContext(Dispatchers.IO) {
                    client.callChatGpt(conversationHistory)
                }
                conversationHistory.add(mapOf("role" to "assistant", "content" to response))
                updateChatDisplayPanel()

                chatLayout.statusBar.text = ""
            }
        }
    }

    private fun clearConversationHistory() {
        conversationHistory.clear()
        promptField.text = ""
        updateChatDisplayPanel()
    }

    private fun saveChatToFile() {
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
        conversationHistory.forEach { message ->
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

    private fun loadMessagesFromFile(file: File) {
        val jsonString = Files.readString(Paths.get(file.toURI()))
        val jsonMessages = GsonBuilder().create().fromJson(jsonString, JsonArray::class.java)

        for (jsonElement in jsonMessages) {
            val jsonMessage = jsonElement.asJsonObject
            val role = jsonMessage.get("role").asString
            val content = jsonMessage.get("content").asString
            conversationHistory.add(mapOf("role" to role, "content" to content))
        }

        updateChatDisplayPanel()
    }
}
