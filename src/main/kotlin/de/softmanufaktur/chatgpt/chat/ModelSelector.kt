package de.softmanufaktur.chatgpt.chat

import com.intellij.openapi.ui.ComboBox
import javax.swing.*

class ModelSelector(private val statusBar: JLabel) {

    var selectedModel: String = loadSelectedModel()

    fun createModelSelector(): JComboBox<String> {
        val modelSelector = ComboBox(arrayOf("gpt-4o", "gpt-4", "gpt-3.5-turbo", "gpt-3"))
        modelSelector.selectedItem = selectedModel
        modelSelector.addActionListener {
            selectedModel = modelSelector.selectedItem as String
            statusBar.text = "Modell gewechselt zu: $selectedModel"
            saveSelectedModel(selectedModel)
        }
        return modelSelector
    }

    private fun saveSelectedModel(model: String) {
        saveSelectedModel(model)
    }

}
