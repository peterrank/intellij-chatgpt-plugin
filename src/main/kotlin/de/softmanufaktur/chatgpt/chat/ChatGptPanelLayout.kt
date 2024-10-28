package de.softmanufaktur.chatgpt.chat

import com.intellij.ui.components.JBScrollPane
import javax.swing.*
import java.awt.*
import de.softmanufaktur.chatgpt.VerticalFlowLayout

class ChatGptPanelLayout {

    val chatDisplayPanel = JPanel()
    val promptField = JTextArea()
    val sendButton = JButton("Senden")
    val clearButton = JButton("neuer Chat")
    val statusBar = JLabel("")
    lateinit var splitPane: JSplitPane

    fun setupLayout(): JPanel {
        chatDisplayPanel.layout = VerticalFlowLayout()
        val chatHistoryScrollPane = JBScrollPane(chatDisplayPanel)

        promptField.lineWrap = true
        promptField.initFileDropHandler()

        val promptPanel = JPanel(BorderLayout(10, 10)).apply {
            add(statusBar, BorderLayout.NORTH)
            add(JScrollPane(promptField), BorderLayout.CENTER)
        }

        splitPane = JSplitPane(JSplitPane.VERTICAL_SPLIT, chatHistoryScrollPane, promptPanel)
        splitPane.resizeWeight = 0.7
        splitPane.border = BorderFactory.createEmptyBorder()

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            add(clearButton)
            add(sendButton)
        }

        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(splitPane, BorderLayout.CENTER)
        mainPanel.add(buttonPanel, BorderLayout.SOUTH)
        return mainPanel
    }
}
