package de.softmanufaktur.chatgpt.chat

import com.intellij.execution.multilaunch.design.components.RoundedCornerBorder
import com.intellij.openapi.components.Service
import com.intellij.ui.components.JBScrollPane
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import de.softmanufaktur.chatgpt.ChatGPTClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*
import javax.swing.*
import javax.swing.text.html.HTMLEditorKit

@Service
class ChatGptPanel : JPanel() {

    private val conversationHistory: MutableList<Map<String, String>> = mutableListOf()
    private val chatDisplayPanel = JPanel()
    private val promptField: JTextArea = JTextArea()
    private val sendButton: JButton = JButton("Senden")
    private val clearButton: JButton = JButton("neuer Chat")
    private val splitPane: JSplitPane
    private val statusBar: JLabel = JLabel("")

    private var selectedModel: String = "gpt-4o"
    private val properties = Properties()
    private val configFile = File("config.properties")

    private val parser: Parser = Parser.builder().build()
    private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

    init {
        layout = BorderLayout()

        // Initialisiere das Drag-and-Drop für das Eingabefeld
        promptField.transferHandler = object : TransferHandler() {
            override fun canImport(support: TransferSupport): Boolean {
                // Prüfe, ob die Daten vom Typ einer Datei sind
                return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
            }

            override fun importData(support: TransferSupport): Boolean {
                if (!canImport(support)) {
                    return false
                }

                // Verarbeite die gezogenen Dateien
                val transferable = support.transferable
                val data = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>

                if (data.size == 1 && data[0] is File) {
                    val file = data[0] as File
                    val fileText = file.readText()

                    try {
                        // Füge den Dateiinhalt ab der aktuellen Cursorposition ein
                        val cursorPosition = promptField.caretPosition
                        promptField.insert(fileText, cursorPosition)
                    } catch (ex: Exception) {
                        ex.printStackTrace()
                        return false
                    }
                    return true
                }
                return false
            }
        }

        // Laden der gespeicherten Modellwahl
        loadSelectedModel()

        // Erstelle die Menüleiste und das Dropdown direkt in der Menüleiste
        val menuBar = JMenuBar()
        val modelSelector = JComboBox(arrayOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-3"))

        // Setze den gespeicherten Wert als Auswahl
        modelSelector.selectedItem = selectedModel

        // ActionListener für das Dropdown-Menü
        modelSelector.addActionListener {
            selectedModel = modelSelector.selectedItem as String
            statusBar.text = "Modell gewechselt zu: $selectedModel"
            saveSelectedModel()  // Speichere das ausgewählte Modell
        }

        // Füge das Dropdown direkt zur Menüleiste hinzu
        menuBar.add(JLabel("Modell: "))
        menuBar.add(modelSelector)

        add(menuBar, BorderLayout.NORTH)

        // Setze Layout und Komponenten
        chatDisplayPanel.layout = BoxLayout(chatDisplayPanel, BoxLayout.Y_AXIS)
        val chatHistoryScrollPane = JBScrollPane(chatDisplayPanel)

        val promptPanel = JPanel()
        promptPanel.layout = BorderLayout(10,10);
        promptPanel.add(statusBar, BorderLayout.NORTH)
        promptPanel.add(JScrollPane(promptField), BorderLayout.CENTER)


        splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, chatHistoryScrollPane, promptPanel)
        splitPane.resizeWeight = 0.7
        splitPane.border = BorderFactory.createEmptyBorder()

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        buttonPanel.add(clearButton)
        buttonPanel.add(sendButton)


        add(splitPane, BorderLayout.CENTER)
        add(buttonPanel, BorderLayout.SOUTH)

        sendButton.addActionListener {
            sendPrompt()
        }

        clearButton.addActionListener {
            clearConversationHistory()
        }

        promptField.lineWrap = true
    }

    private fun createMessageBubble(bubbleText: String, isUser: Boolean): JPanel {
        val htmlBubbleText = convertMarkdownToHtml(bubbleText)

        val bubblePanel = JPanel().apply {
            layout = BorderLayout()
            background = if (isUser) Color(204, 255, 204) else Color(204, 229, 255)
            isOpaque = false
            border = RoundedCornerBorder(15)

            val availableWidth = this@ChatGptPanel.width - 100
            val prefHeight = if (preferredSize.height > 300) {
                preferredSize.height + 100
            } else {
                300
            }
            preferredSize = Dimension(availableWidth, prefHeight)

            alignmentX = if (isUser) Component.LEFT_ALIGNMENT else Component.RIGHT_ALIGNMENT
        }

        val editorPane = JEditorPane()
        editorPane.isEditable = false
        editorPane.contentType = "text/html"

        val htmlEditorKit = HTMLEditorKit()
        editorPane.editorKit = htmlEditorKit

        htmlEditorKit.styleSheet.apply {
            addRule("body { font-family: Arial; font-size: 12px; color: white; background-color: black; padding: 10px}")
            addRule("pre { background-color: white; color: black; border: 1px solid white; padding: 10px; margin: 10px; }")
            addRule("code { background-color: white; color: black; border: 1px solid white; margin: 10px; padding: 5px; overflow-x: auto; white-space: pre; }")
            addRule("h3 { font-size: 1.5em; }")
        }

        val htmlText = "<html><body>$htmlBubbleText</body></html>"
        editorPane.text = htmlText

        editorPane.setSize(bubblePanel.preferredSize.width, Short.MAX_VALUE.toInt())
        val preferredHeight = editorPane.preferredSize.height
        bubblePanel.preferredSize = Dimension(bubblePanel.preferredSize.width, preferredHeight)

        val scrollPane = JBScrollPane(editorPane)
        scrollPane.preferredSize = bubblePanel.preferredSize
        bubblePanel.add(scrollPane, BorderLayout.CENTER)

        return bubblePanel
    }

    private fun updateChatDisplayPanel() {
        chatDisplayPanel.removeAll()
        for (message in conversationHistory) {
            val role = message["role"]
            val content = message["content"]

            val bubble = createMessageBubble(content ?: "", role == "user")

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
    }

    private fun convertMarkdownToHtml(text: String): String {
        val document = parser.parse(text)
        return renderer.render(document)
    }

    private fun sendPrompt() {
        val prompt = promptField.text.trim()
        if (prompt.isNotBlank()) {
            conversationHistory.add(mapOf("role" to "user", "content" to prompt))
            updateChatDisplayPanel()

            statusBar.text = "Anfrage mit Modell $selectedModel. Bitte warten..."
            println("Modell: $selectedModel")
            CoroutineScope(Dispatchers.Main).launch {

                val client = ChatGPTClient(selectedModel)

                val response = withContext(Dispatchers.IO) {
                    client.callChatGpt(conversationHistory)
                }

                conversationHistory.add(mapOf("role" to "assistant", "content" to response))

                updateChatDisplayPanel()
                promptField.text = ""
                statusBar.text = ""
            }
        }
    }

    private fun clearConversationHistory() {
        conversationHistory.clear()
        promptField.text = ""
        updateChatDisplayPanel()
    }

    private fun loadSelectedModel() {
        if (configFile.exists()) {
            FileInputStream(configFile).use {
                properties.load(it)
                selectedModel = properties.getProperty("selectedModel", "gpt-3.5-turbo")
            }
        } else {
            selectedModel = "gpt-3.5-turbo"
        }
    }

    private fun saveSelectedModel() {
        properties["selectedModel"] = selectedModel
        FileOutputStream(configFile).use {
            properties.store(it, null)
        }
    }
}