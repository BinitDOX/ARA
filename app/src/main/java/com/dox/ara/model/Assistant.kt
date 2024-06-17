package com.dox.ara.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assistant")
data class Assistant (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val about: String,
    val imageUri: String,
    val prompt: String,  // Instruction prompt
    val color: String,
    val edgeVoice: String,
    val edgePitch: Int,
    val rvcVoice: String,
) {
    companion object {
        fun getEmptyAssistant() = Assistant(
            id = 0,
            name = "",
            about = "",
            imageUri = "",
            prompt = "",
            color = "",
            edgeVoice = "",
            edgePitch = 0,
            rvcVoice = ""
        )
    }
}