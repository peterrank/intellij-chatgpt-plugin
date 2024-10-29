package de.softmanufaktur.chatgpt.chat

class ConversationHistory {
    private val history: MutableList<Map<String, String>> = mutableListOf()

    fun addMessage(role: String, content: String) {
        history.add(mapOf("role" to role, "content" to content))
    }

    fun getAllMessages(): List<Map<String, String>> {
        return history
    }

    fun addToLastMessage(content: String)  {
        // Check if there's at least one message in the history
        if (history.isNotEmpty()) {
            // Get the last message and retrieve the role and existing content
            val lastMessage = history.removeAt(history.size - 1)
            val updatedContent = (lastMessage["content"] ?: "") + content
            val role = lastMessage["role"] ?: "unknown"

            // Add the updated message back to the history
            history.add(mapOf("role" to role, "content" to updatedContent))
        }
    }

    fun clear() {
        history.clear()
    }
}
