package de.softmanufaktur.chatgpt.chat

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser

// Globale HashMap für die Zuordnung der IDs zu den Code-Abschnitten
private val codeMap = mutableMapOf<Int, String>()
private var idCounter = 0

private val parser: Parser = Parser.builder().build()
private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

fun convertMarkdownToHtml(text: String): String {
    val document = parser.parse(text)
    val htmlString = renderer.render(document)
    val modifiedHtmlString = wrapCodeTagsWithDiv(htmlString)
    return modifiedHtmlString
}

fun wrapCodeTagsWithDiv(html: String): String {
    val codeRegex = Regex("<code>(.*?)</code>", RegexOption.DOT_MATCHES_ALL)
    return codeRegex.replace(html) { matchResult ->
        val codeContent = matchResult.groups[1]?.value ?: ""
        val codeId = storeCodeSnippet(codeContent)
        """
        <div>
            <code>${codeContent}</code>
            <button type='button' onclick='window.location.href="java://copy/${codeId}"'>Kopieren</button>
        </div>
        """
    }
}

// Funktion zum Speichern eines Code-Abschnittes und Erzeugen einer eindeutigen ID
private fun storeCodeSnippet(code: String): Int {
    val id = idCounter++
    codeMap[id] = code
    return id
}

// Funktion zum Abrufen des Codes aufgrund der ID
fun getCodeSnippetById(id: Int): String? {
    return codeMap[id]
}