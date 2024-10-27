package de.softmanufaktur.chatgpt.chat

import com.intellij.execution.multilaunch.design.components.RoundedCornerBorder
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.util.preferredWidth
import org.apache.commons.text.StringEscapeUtils
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit

fun copyToClipboard(text: String) {
    val unescapedText = StringEscapeUtils.unescapeHtml4(text)
    val stringSelection = StringSelection(unescapedText)
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}

fun createMessageBubble(bubbleText: String, isUser: Boolean, maxBubbleWidth: Int): JPanel {
    val htmlBubbleText = convertMarkdownToHtml(bubbleText)
    println("bubbleText is $htmlBubbleText");
    val bubblePanel = JPanel().apply {
        layout = BorderLayout()
        background = if (isUser) Color(37, 105, 51) else Color(0, 64, 128)
        isOpaque = false
        border = RoundedCornerBorder(15)
    }

    val color = if (isUser) "#256933"  else "#004080"
    val editorPane = JEditorPane().apply {
        isEditable = false
        contentType = "text/html"
        editorKit = HTMLEditorKit().apply {
            styleSheet.apply {
                addRule("body { font-family: Arial; font-size: 12px; color: white; background-color: $color; padding: 10px}")
                addRule("pre { background-color: white; color: black; border: 1px solid white; padding: 10px; margin: 10px; }")
                addRule("code { background-color: white; color: black; border: 1px solid white; margin: 10px; padding: 5px; overflow-x: auto; white-space: pre; width: 100%}")
                addRule("h3 { font-size: 1.5em; }")
                addRule(".copylinkLine {text-align: right;}")
                addRule(".copylinkWrapper {background-color: white; padding: 5px; border: 1px solid blue;}")
                addRule(".copylink { text-decoration: none; color: blue;}")
            }
        }
        text = "<html><body>$htmlBubbleText</body></html>"
    }


    val scrollPane = JBScrollPane(editorPane).apply {
        maximumSize = Dimension(maxBubbleWidth, Integer.MAX_VALUE)
        preferredWidth = maxBubbleWidth
        border = null
    }
    bubblePanel.add(scrollPane, BorderLayout.CENTER)

    editorPane.addHyperlinkListener { e: HyperlinkEvent ->
        if (e.eventType == HyperlinkEvent.EventType.ACTIVATED) {
            if (e.description.startsWith("java://copy/")) {
                val idString = e.description.removePrefix("java://copy/")
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

