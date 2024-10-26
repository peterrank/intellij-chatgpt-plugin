package de.softmanufaktur.chatgpt.unittestgenerator.settings

import com.intellij.openapi.options.Configurable
import javax.swing.*
import com.intellij.openapi.ui.ComboBox

class SettingsConfigurable : Configurable {

    private var settingsPanel: JPanel? = null
    private var promptField: JTextArea? = null
    private var modelComboBox: ComboBox<String>? = null

    override fun createComponent(): JComponent? {
        settingsPanel = JPanel()
        settingsPanel?.layout = BoxLayout(settingsPanel, BoxLayout.Y_AXIS)

        val promptLabel = JLabel("ChatGPT Prompt Template:")
        promptField = JTextArea(10, 50)
        promptField?.lineWrap = true
        promptField?.wrapStyleWord = true

        val modelLabel = JLabel("ChatGPT Model:")
        modelComboBox = ComboBox(arrayOf(
            "gpt-3.5-turbo",
            "gpt-4",
            "gpt-4-turbo",
            "gpt-4o",
            "gpt-4o-mini"
        ))

        settingsPanel?.add(promptLabel)
        settingsPanel?.add(JScrollPane(promptField))
        settingsPanel?.add(modelLabel)
        settingsPanel?.add(modelComboBox)

        return settingsPanel
    }

    override fun isModified(): Boolean {
        val settings = UnitTestGeneratorSettings.instance
        return promptField?.text != settings.customPrompt || modelComboBox?.selectedItem != settings.gptModel
    }

    override fun apply() {
        val settings = UnitTestGeneratorSettings.instance
        settings.customPrompt = promptField?.text ?: ""
        settings.gptModel = modelComboBox?.selectedItem as String
    }

    override fun reset() {
        val settings = UnitTestGeneratorSettings.instance
        promptField?.text = settings.customPrompt
        modelComboBox?.selectedItem = settings.gptModel
    }

    override fun getDisplayName(): String {
        return "Unit Test Generator Settings"
    }
}
