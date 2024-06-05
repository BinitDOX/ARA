package com.dox.ara.requestdto

data class AssistantRequest (
    val id: Long,
    val name: String,
    val prompt: String,
    val edgeVoice: String,
    val edgePitch: Int,
    val rvcVoice: String,
)