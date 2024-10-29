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
    val modifiedHtmlString = wrapPreCodeTagsWithDiv(htmlString)
    return modifiedHtmlString
}

fun wrapPreCodeTagsWithDiv(html: String): String {
    // Regex to match <pre> tag followed by <code> tag with optional attributes
    val preCodeRegex = Regex("<pre>\\s*\\n*<code(\\s[^>]*)?>(.*?)</code>\\s*</pre>", RegexOption.DOT_MATCHES_ALL)
    return preCodeRegex.replace(html) { matchResult ->
        val codeAttributes = matchResult.groups[1]?.value ?: ""
        val codeContent = matchResult.groups[2]?.value ?: ""
        val codeId = storeCodeSnippet(codeContent)
        val language = codeAttributes.replace("class=\"language-", "").replace("\"", "")

        """
            <div>
                <pre >
                    <code>$codeContent</code>                    
                </pre>
                <div class="copylinkLine">
                     <span class="copylinkWrapper">
                        <a class="copylink" href="java://copy/${codeId}">$language Code kopieren</a>
                    </span>
                    &nbsp;
                    <span class="copylinkWrapper">
                        <a class="copylink" href="java://takeover/${codeId}">$language Code übernehmen</a>
                    </span>
                <div>
            <div>
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