package de.softmanufaktur.chatgpt.chat

import com.intellij.execution.multilaunch.design.components.RoundedCornerBorder
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.util.preferredWidth
import org.apache.commons.text.StringEscapeUtils
import java.awt.*
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.StringSelection
import java.io.File
import javax.swing.*
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLEditorKit

fun copyToClipboard(text: String) {
    val unescapedText = StringEscapeUtils.unescapeHtml4(text)
    val stringSelection = StringSelection(unescapedText)
    val clipboard: Clipboard = Toolkit.getDefaultToolkit().systemClipboard
    clipboard.setContents(stringSelection, null)
}

fun takeoverToCode(text: String, project: Project) {
    // Schritt 1: Unescape HTML-Entities im Text
    val unescapedText = StringEscapeUtils.unescapeHtml4(text)

    // Schritt 2: Den Klassennamen und das Paket ermitteln
    val packageName = Regex("package\\s+([\\w\\.]+);").find(unescapedText)?.groups?.get(1)?.value
    val className = Regex("class\\s+(\\w+)").find(unescapedText)?.groups?.get(1)?.value

    if (packageName == null || className == null) {
        throw IllegalArgumentException("Package-Name oder Klassenname konnte nicht ermittelt werden.")
    }

    // Schritt 3: Verzeichnisstruktur aus dem Paketnamen aufbauen
    val baseDirectory = project.basePath
    val projectStructurePath = "src/main/kotlin/"  // Basisverzeichnis des Projekts
    val packagePath = packageName.replace(".", "/")  // Paket in Pfad umwandeln
    val targetDirectory = File(baseDirectory, projectStructurePath + packagePath)

    // Schritt 4: Verzeichnis erstellen, falls nicht vorhanden
    if (!targetDirectory.exists()) {
        targetDirectory.mkdirs()
    }

    // Schritt 5: Die Datei speichern
    val outputFile = File(targetDirectory, "$className.java")
    outputFile.writeText(unescapedText)

    println("Datei gespeichert unter: ${outputFile.absolutePath}")
}

fun createMessageBubble(bubbleText: String, isUser: Boolean, maxBubbleWidth: Int, project: Project): JPanel {
    val htmlBubbleText = convertMarkdownToHtml(bubbleText)
    //println("bubbleText is $htmlBubbleText");
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
                addRule(".copylinkLine {text-align: center;}")
                addRule(".copylinkWrapper {background-color: white; padding: 5px; margin: 10px; border: 1px solid red;}")
                addRule(".copylink { text-decoration: underline; color: blue;}")
            }
        }
        text = "<html><body>$htmlBubbleText</body></html>"
        maximumSize = Dimension(maxBubbleWidth, Integer.MAX_VALUE)
        preferredSize = Dimension(maxBubbleWidth, preferredSize.height)
        border = null
    }


    val scrollPane = JBScrollPane(editorPane).apply {
        maximumSize = Dimension(maxBubbleWidth, Integer.MAX_VALUE)
        preferredSize = Dimension(maxBubbleWidth, preferredSize.height)
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
            } else if (e.description.startsWith("java://takeover/")) {
                val idString = e.description.removePrefix("java://takeover/")
                val id = idString.toIntOrNull()
                val codeSnippet = id?.let { getCodeSnippetById(it) }
                if (codeSnippet != null) {
                    takeoverToCode(codeSnippet, project)
                } else {
                    println("Code snippet not found for ID: $id")
                }
            }
        }
    }

    return bubblePanel
}

