package com.dox.ara.ui.data

data class ChatItem (
    val id: Long,
    val name: String,
    val imageUri: String?,
    val content: String?,
    val date: String?,
    val from: Role?,
    val status: MessageStatus?,
    val unreadCount: Int? = null,
)

enum class Role {
    ASSISTANT,
    USER,
    SYSTEM
}

enum class MessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

val dummyChatItems = listOf(
    ChatItem(
        id = 1,
        name = "Miku",
        imageUri = "URI-1",
        content = "Hello, how are you?",
        date = "11/22/33",
        from = Role.ASSISTANT,
        status = MessageStatus.DELIVERED,
        unreadCount = 2
    ),
    ChatItem(
        id = 2,
        name = "Jibril",
        imageUri = "URI-2",
        content = "What's up?",
        date = "33/22/11",
        from = Role.ASSISTANT,
        status = MessageStatus.DELIVERED

    ),
    ChatItem(
        id = 3,
        name = "Teto",
        imageUri = "URI-3",
        content = "Moshi Moshi",
        date = "33/22/11",
        from = Role.ASSISTANT,
        status = MessageStatus.READ
    )
)