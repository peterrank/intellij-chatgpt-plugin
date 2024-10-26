package de.softmanufaktur.chatgpt.chat

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

private val properties = Properties()
private val configFile = File("config.properties")

fun loadSelectedModel(): String {
    return if (configFile.exists()) {
        FileInputStream(configFile).use {
            properties.load(it)
            properties.getProperty("selectedModel", "gpt-3.5-turbo")
        }
    } else {
        "gpt-3.5-turbo"
    }
}

fun saveSelectedModel(selectedModel: String) {
    properties["selectedModel"] = selectedModel
    FileOutputStream(configFile).use {
        properties.store(it, null)
    }
}