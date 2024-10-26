package de.softmanufaktur.chatgpt.unittestgenerator

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.ui.Messages
import com.solvares.unittestgeneratorplugin.utils.FileUtils
import de.softmanufaktur.chatgpt.ChatGPTClient
import de.softmanufaktur.chatgpt.unittestgenerator.settings.UnitTestGeneratorSettings

class CreateTestCasesAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        val file: VirtualFile? = e.getData(PlatformDataKeys.VIRTUAL_FILE)

        if (file != null && file.name.endsWith(".java")) {
            val className = file.nameWithoutExtension
            val packagePath = file.parent.path.replace("/", ".").substringAfter("src.main.java.")

            // Klasse als String lesen
            val classContent = file.contentsToByteArray().toString(Charsets.UTF_8)

            // Prompt von den Einstellungen abrufen und anpassen
            val prompt = createTestCasePrompt(className, packagePath, classContent)

            // API-Aufruf zu ChatGPT-API mit benutzerdefiniertem Modell
            val settings = UnitTestGeneratorSettings.instance
            val testCases = ChatGPTClient(settings.gptModel).callChatGpt(prompt)

            // Testklasse in das Testverzeichnis speichern
            FileUtils.saveTestClass(e, className, packagePath, testCases)

            // Benachrichtigung an den Nutzer
            Messages.showMessageDialog(
                e.project,
                "Test cases for the class $className were successfully created and saved.",
                "Test cases created",
                Messages.getInformationIcon()
            )
        }
    }

    private fun createTestCasePrompt(className: String, packagePath: String, classContent: String): String {
        val settings = UnitTestGeneratorSettings.instance
        return settings.customPrompt
            .replace("\$className", className)
            .replace("\$packagePath", packagePath)
            .replace("\$classContent", classContent)
    }
}
