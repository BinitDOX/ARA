package com.dox.ara.command

data class CommandResponse (
    val isSuccess: Boolean,
    val message: String,
    val getResponse: Boolean,  // Get immediate response from assistant or not
    val sendResponse: Boolean = true, // Send command response to assistant or not
)