package de.softmanufaktur.chatgpt.chat

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.intellij.openapi.components.service

class ChatGptToolWindowFactory : ToolWindowFactory, DumbAware {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatGptPanel = project.service<ChatGptPanel>()
        chatGptPanel.project = project
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(chatGptPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}