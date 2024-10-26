package de.softmanufaktur.chatgpt.chat

import com.intellij.execution.multilaunch.design.components.RoundedCornerBorder
import com.intellij.ui.components.JBScrollPane
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit

fun copyToClipboard(text: String) {
    val stringSelection = StringSelection(text)
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}

fun createMessageBubble(bubbleText: String, isUser: Boolean, maxBubbleWidth: Int): JPanel {
    val htmlBubbleText = convertMarkdownToHtml(bubbleText)
    val bubblePanel = JPanel().apply {
        layout = BorderLayout()
        background = if (isUser) Color(204, 255, 204) else Color(204, 229, 255)
        isOpaque = false
        border = RoundedCornerBorder(15)
        alignmentX = if (isUser) Component.LEFT_ALIGNMENT else Component.RIGHT_ALIGNMENT
        maximumSize = Dimension(maxBubbleWidth, Integer.MAX_VALUE)
        size = Dimension(maxBubbleWidth, preferredSize.height)
        preferredSize = Dimension(maxBubbleWidth, preferredSize.height)
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

    editorPane.addHyperlinkListener { e: HyperlinkEvent ->
        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            val url = e.url.toString()
            if (url.startsWith("java://copy/")) {
                val idString = url.removePrefix("java://copy/")
                val id = idString.toIntOrNull()
                val codeSnippet = id?.let { getCodeSnippetById(it) }
                if (codeSnippet != null) {
                    copyToClipboard(codeSnippet)
                } else {
                    println("Code snippet not found for ID: $id")
                }
            }
        }
    }
    return bubblePanel
}