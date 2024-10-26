package de.softmanufaktur.chatgpt.chat

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser

private val parser: Parser = Parser.builder().build()
private val renderer: HtmlRenderer = HtmlRenderer.builder().build()

fun convertMarkdownToHtml(text: String): String {
    val document = parser.parse(text)
    return renderer.render(document)
}

