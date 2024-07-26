package org.apps.flexmed.model

data class RecentChat(
    val lastMessage: String? = null,
    val receiverId: String? = null,
    val senderId: String? = null,
    var otherUserId: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
