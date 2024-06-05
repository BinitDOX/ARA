package com.dox.ara.requestdto

import com.dox.ara.ui.data.Role

data class MessageRequest (
    val id: Long,
    val quotedId: Long?,
    val content: String,
    val timestamp: Long,
    val from: Role,

    val chatId: Long,
    val assistantId: Long
)