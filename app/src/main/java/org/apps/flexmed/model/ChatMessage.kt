package org.apps.flexmed.model

data class ChatMessage(
    val id: String? = null,
    val messageId: String? = null,
    val senderId: String? = null,
    val receiverId: String? = null,
    val message: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
