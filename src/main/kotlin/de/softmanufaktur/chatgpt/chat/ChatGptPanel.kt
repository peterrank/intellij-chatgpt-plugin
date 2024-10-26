package de.softmanufaktur.chatgpt.chat

import com.intellij.openapi.components.Service
import com.intellij.ui.components.JBScrollPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.*
import javax.swing.*
import de.softmanufaktur.chatgpt.ChatGPTClient
import de.softmanufaktur.chatgpt.VerticalFlowLayout

@Service
class ChatGptPanel : JPanel() {

    private val conversationHistory: MutableList<Map<String, String>> = mutableListOf()
    private val chatDisplayPanel = JPanel()
    private val promptField: JTextArea = JTextArea()
    private val sendButton: JButton = JButton("Senden")
    private val clearButton: JButton = JButton("neuer Chat")
    private val statusBar: JLabel = JLabel("")

    private lateinit var splitPane: JSplitPane // Changed to var to allow for initialization later

    private var selectedModel: String = "gpt-4o"

    init {
        layout = BorderLayout()
        promptField.initFileDropHandler() // Correct way to call the file drop handler on promptField

        // Load selected model
        selectedModel = loadSelectedModel()

        // Create menu bar with model selector
        val modelSelector = createModelSelector()
        val menuBar = JMenuBar()
        menuBar.add(JLabel("Modell: ") as Component) // Ensure component is added correctly
        menuBar.add(modelSelector as Component)
        add(menuBar, BorderLayout.NORTH)

        setupLayout()

        sendButton.addActionListener { sendPrompt() }
        clearButton.addActionListener { clearConversationHistory() }

        promptField.lineWrap = true
    }

    private fun setupLayout() {
        chatDisplayPanel.layout = VerticalFlowLayout()
        val chatHistoryScrollPane = JBScrollPane(chatDisplayPanel)

        val promptPanel = JPanel().apply {
            layout = BorderLayout(10, 10)
            add(statusBar, BorderLayout.NORTH)
            add(JScrollPane(promptField), BorderLayout.CENTER)
        }

        splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, chatHistoryScrollPane, promptPanel) // Initialize here
        splitPane.resizeWeight = 0.7
        splitPane.border = BorderFactory.createEmptyBorder()

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(clearButton)
            add(sendButton)
        }

        add(splitPane, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)
    }

    private fun createModelSelector(): JComboBox<String> {
        val modelSelector = JComboBox(arrayOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-3"))
        modelSelector.selectedItem = selectedModel // Set the initial selected model
        modelSelector.addActionListener {
            selectedModel = modelSelector.selectedItem as String
            statusBar.text = "Modell gewechselt zu: $selectedModel"
            saveSelectedModel(selectedModel) // Save the selected model
        }
        return modelSelector
    }

    private fun updateChatDisplayPanel() {
        chatDisplayPanel.removeAll()
        for (message in conversationHistory) {
            val role = message["role"] ?: ""
            val content = message["content"] ?: ""

            val maxBubbleWidth = chatDisplayPanel.width - 50
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

            statusBar.text = "Anfrage mit Modell $selectedModel. Bitte warten..."
            promptField.text = ""
            println("Modell: $selectedModel")
            CoroutineScope(Dispatchers.Main).launch {
                val client = ChatGPTClient(selectedModel)
                val response = withContext(Dispatchers.IO) {
                    client.callChatGpt(conversationHistory)
                }
                conversationHistory.add(mapOf("role" to "assistant", "content" to response))
                updateChatDisplayPanel()

                statusBar.text = ""
            }
        }
    }

    private fun clearConversationHistory() {
        conversationHistory.clear()
        promptField.text = ""
        updateChatDisplayPanel()
    }
}