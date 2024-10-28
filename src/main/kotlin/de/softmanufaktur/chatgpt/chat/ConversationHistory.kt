package de.softmanufaktur.chatgpt.chat

class ConversationHistory {
    private val history: MutableList<Map<String, String>> = mutableListOf()

    fun addMessage(role: String, content: String) {
        history.add(mapOf("role" to role, "content" to content))
    }

    fun getAllMessages(): List<Map<String, String>> {
        return history
    }

    fun clear() {
        history.clear()
    }
}
