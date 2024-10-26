package de.softmanufaktur.chatgpt.chat

import com.intellij.execution.multilaunch.design.components.RoundedCornerBorder
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import javax.swing.*
import javax.swing.text.html.HTMLEditorKit

fun createMessageBubble(bubbleText: String, isUser: Boolean, maxBubbleWidth: Int): JPanel {
    val htmlBubbleText = convertMarkdownToHtml(bubbleText)
    val bubblePanel = JPanel().apply {
        layout = BorderLayout()
        background = if (isUser) Color(204, 255, 204) else Color(204, 229, 255)
        isOpaque = false
        border = RoundedCornerBorder(15)
        alignmentX = if (isUser) Component.LEFT_ALIGNMENT else Component.RIGHT_ALIGNMENT
        maximumSize = Dimension(maxBubbleWidth, Integer.MAX_VALUE)
        //size = Dimension(maxBubbleWidth, preferredSize.height)
        //preferredSize = Dimension(maxBubbleWidth, preferredSize.height)
    }

    val editorPane = JEditorPane().apply {
        isEditable = false
        contentType = "text/html"
        editorKit = HTMLEditorKit().apply {
            styleSheet.apply {
                addRule("body { font-family: Arial; font-size: 12px; color: white; background-color: black; padding: 10px}")
                addRule("pre { background-color: white; color: black; border: 1px solid white; padding: 10px; margin: 10px; }")
                addRule("code { background-color: white; color: black; border: 1px solid white; margin: 10px; padding: 5px; overflow-x: auto; white-space: pre; }")
                addRule("h3 { font-size: 1.5em; }")
            }
        }
        text = "<html><body>$htmlBubbleText</body></html>"
    }

    val scrollPane = JBScrollPane(editorPane)
    bubblePanel.add(scrollPane, BorderLayout.CENTER)

    return bubblePanel
}