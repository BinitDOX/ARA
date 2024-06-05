package com.dox.ara.responsedto

import com.dox.ara.ui.data.Role

data class MessageResponse (
    val id: Long,
    val quotedId: Long?,
    val content: String,
    val timestamp: Long,
    val from: Role,
)